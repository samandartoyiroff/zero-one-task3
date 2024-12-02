package uz.thirdTask.auth

import com.fasterxml.jackson.databind.ObjectMapper
import feign.Response
import feign.codec.ErrorDecoder
import org.apache.commons.codec.binary.Base64
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.support.ResourceBundleMessageSource
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.common.OAuth2AccessToken
import org.springframework.security.oauth2.common.OAuth2RefreshToken
import org.springframework.security.oauth2.common.exceptions.OAuth2Exception
import org.springframework.security.oauth2.common.util.SerializationUtils
import org.springframework.security.oauth2.provider.OAuth2Authentication
import org.springframework.security.oauth2.provider.error.WebResponseExceptionTranslator
import org.springframework.security.oauth2.provider.token.AuthenticationKeyGenerator
import org.springframework.security.oauth2.provider.token.DefaultAuthenticationKeyGenerator
import org.springframework.security.oauth2.provider.token.TokenStore
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import uz.thirdTask.auth.ErrorCode.GENERAL_API
import java.io.UnsupportedEncodingException
import java.math.BigInteger
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*


object SerializableObjectConverter {
    fun serialize(`object`: OAuth2Authentication?): String {
        val bytes: ByteArray = SerializationUtils.serialize(`object`)
        return Base64.encodeBase64String(bytes)
    }

    fun deserialize(encodedObject: String?): OAuth2Authentication {
        val bytes: ByteArray = Base64.decodeBase64(encodedObject)
        return SerializationUtils.deserialize(bytes)
    }
}

@Component
class MongoTokenStore(
    private val accessTokenRepository: AccessTokenRepository,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val internalUserService: InternalUserService,
) : TokenStore {
    private val realIPHeader = "x-real-ip"
    private val authenticationKeyGenerator: AuthenticationKeyGenerator = DefaultAuthenticationKeyGenerator()

    override fun readAuthentication(token: OAuth2AccessToken?) = readAuthentication(token?.value)

    override fun readAuthentication(token: String?): OAuth2Authentication? {
        return extractTokenKey(token)?.let {
            val accessToken = accessTokenRepository.findByTokenId(it)
            accessToken?.let { currentAccessToken ->
                currentAccessToken.modifiedDate = Date()
                accessTokenRepository.save(currentAccessToken)
            }
            accessToken?.getAuthentication()
        }
    }

    @Transactional
    override fun storeAccessToken(token: OAuth2AccessToken, authentication: OAuth2Authentication) {
        val refreshToken = token.refreshToken?.value
//        readAccessToken(token.value)?.let { removeAccessToken(it) }
        val user = if (authentication.userAuthentication is UsernamePasswordAuthenticationToken) {
            authentication.userAuthentication.details as UserAuthDto
        } else {
            (authentication.userAuthentication.principal as CustomUserDetails).userDto
        }
        checkUserActive(user.username)
        val ipAddress = try {
            val request = (RequestContextHolder.currentRequestAttributes() as ServletRequestAttributes).request
            request.getHeader(realIPHeader) ?: request.remoteAddr
        } catch (e: java.lang.Exception) {
            null
        }

        val authenticationId = authenticationKeyGenerator.extractKey(authentication)
        var accessToken = accessTokenRepository.findByAuthenticationId(authenticationId)
        if (accessToken != null) {
            accessToken.modifiedDate = Date()
            accessToken.token?.expiration?.let {
                if (it.time < Date().time) {
                    accessToken!!.token = token
                    accessToken!!.refreshToken = refreshToken
                }
            }
        } else {
            accessToken = AccessToken(
                user,
                extractTokenKey(token.value),
                token,
                authenticationId,
                if (authentication.isClientOnly) null else authentication.name,
                authentication.oAuth2Request.clientId,
                refreshToken,
                ipAddress
            ).also { it.setAuthentication(authentication) }
        }
        accessTokenRepository.save(accessToken)
    }

    override fun readAccessToken(tokenValue: String): OAuth2AccessToken? {
        return extractTokenKey(tokenValue)?.let {
            val accessToken = accessTokenRepository.findByTokenId(it)
            accessToken?.let { currentAccessToken ->
                currentAccessToken.modifiedDate = Date()
                accessTokenRepository.save(currentAccessToken)
            }
            return accessToken?.token
        }
    }

    override fun removeAccessToken(token: OAuth2AccessToken) {
        extractTokenKey(token.value)?.let { accessTokenRepository.deleteByTokenId(it) }
    }

    override fun storeRefreshToken(token: OAuth2RefreshToken?, authentication: OAuth2Authentication?) {
        val refreshToken =
            RefreshToken(extractTokenKey(token?.value), token).also { it.setAuthentication(authentication!!) }
        refreshTokenRepository.save(refreshToken)
    }

    override fun readRefreshToken(tokenValue: String?): OAuth2RefreshToken? {
        return extractTokenKey(tokenValue)?.let { refreshTokenRepository.findByTokenId(it)?.token }
    }

    override fun readAuthenticationForRefreshToken(token: OAuth2RefreshToken?): OAuth2Authentication? {
        return extractTokenKey(token?.value)?.let { refreshTokenRepository.findByTokenId(it)?.getAuthentication() }
    }

    override fun removeRefreshToken(token: OAuth2RefreshToken?) {
        extractTokenKey(token?.value)?.let { refreshTokenRepository.deleteByTokenId(it) }
    }

    override fun removeAccessTokenUsingRefreshToken(refreshToken: OAuth2RefreshToken?) {
        refreshToken?.value?.let { accessTokenRepository.deleteByRefreshToken(it) }
    }

    override fun getAccessToken(authentication: OAuth2Authentication?): OAuth2AccessToken? {
        return authentication?.let {
            val authenticationId = authenticationKeyGenerator.extractKey(it)
            val accessToken = accessTokenRepository.findByAuthenticationId(authenticationId)
            if (accessToken?.token != null) {
                val readAuth = this.readAuthentication(accessToken.token)
                if (readAuth != null && authenticationId != authenticationKeyGenerator.extractKey(readAuth)) {
                    checkUserActive(accessToken.username!!)
                    this.removeAccessToken(accessToken.token!!)
                    this.storeAccessToken(accessToken.token!!, authentication)
                }
            }
            accessToken?.let { currentAccessToken ->
                checkUserActive(currentAccessToken.username!!)
                currentAccessToken.modifiedDate = Date()
                accessTokenRepository.save(currentAccessToken)
            }
            accessToken?.token
        }
    }

    override fun findTokensByClientIdAndUserName(
        clientId: String,
        userName: String,
    ): MutableCollection<OAuth2AccessToken> {
        return accessTokenRepository
            .findAllByClientIdAndUsername(clientId, userName)
            .filter { it.token != null }
            .map { it.token!! }
            .toMutableList()
    }

    override fun findTokensByClientId(clientId: String): MutableCollection<OAuth2AccessToken> {
        return accessTokenRepository
            .findAllByClientId(clientId)
            .filter { it.token != null }
            .map { it.token!! }
            .toMutableList()
    }

    private fun extractTokenKey(value: String?): String? {
        return if (value == null) {
            null
        } else {
            val digest: MessageDigest = try {
                MessageDigest.getInstance("MD5")
            } catch (var5: NoSuchAlgorithmException) {
                throw IllegalStateException("MD5 algorithm not available.  Fatal (should be in the JDK).")
            }
            try {
                val e = digest.digest(value.toByteArray(charset("UTF-8")))
                String.format("%032x", BigInteger(1, e))
            } catch (var4: UnsupportedEncodingException) {
                throw IllegalStateException("UTF-8 encoding not available.  Fatal (should be in the JDK).")
            }
        }
    }

    private fun checkUserActive(username: String) {
//        if (!internalUserService.isActive(username)) throw NotActiveUserException()
    }
}

@Component
class CustomAuthenticationProvider(
    authenticators: List<Authenticator>
) : AuthenticationProvider {
    private val authenticatorsMap: Map<GrantType, Authenticator> = authenticators.associateBy { it.grantType() }
    private val grantTypeKey = "grant_type"

    override fun authenticate(authentication: Authentication?): Authentication {
        if (authentication is UsernamePasswordAuthenticationToken && authentication.details is Map<*, *>) {
            val details = authentication.details as Map<*, *>
            val grantTypeValue = details[grantTypeKey] as String
            val grantType = GrantType.toGrantType(grantTypeValue)
                ?: throw InvalidGrantTypeException(grantTypeValue)
            return authenticatorsMap[grantType]?.run {
                doSafe {
                    authenticate(authentication)
                } ?: throw ServiceException()
            } ?: throw InvalidGrantTypeException(grantTypeValue)
        } else throw IllegalTokenException()
    }

    override fun supports(authentication: Class<*>?): Boolean {
        return authentication == UsernamePasswordAuthenticationToken::class.java
    }
}

@Component
class FeignErrorDecoder : ErrorDecoder {

    private val logger: Logger = LoggerFactory.getLogger(this.javaClass)
    private val mapper = ObjectMapper()

    override fun decode(methodKey: String, response: Response): Exception {
        return response.run {
            val error = doSafe {
                val jsonNode = mapper.readTree(body().asInputStream())
                val code = jsonNode.get("code")?.asInt()
                val message = jsonNode.get("message")?.asText()

                if (code != null && message != null) {
                    BaseMessage(code, message)
                } else {
                    throw RuntimeException("$jsonNode, status = ${status()}, method = $methodKey")
                }
            }
            if (error != null) {
                InternalServiceCallException(error.code, error.message)
            } else {
                logger.warn("Feign error: $methodKey ${status()}")
                FeignCallException()
            }
        }
    }
}

@Component
class CustomOAuthTranslator(
    private val messageSource: ResourceBundleMessageSource
) : WebResponseExceptionTranslator<OAuth2Exception> {
    val logger: Logger = LoggerFactory.getLogger(this.javaClass)

    override fun translate(e: java.lang.Exception): ResponseEntity<OAuth2Exception> {
        when (e) {
            is InternalServiceCallException -> {
                return ResponseEntity.badRequest().body(CustomOAuth2Exception(e.code ?: 200, e.errorMessage ?: ""))
            }

            is AuthException -> {
                return ResponseEntity.badRequest().body(e.getOAuth2Exception(messageSource))
            }

            else -> {
                logger.warn("Auth exception: [${e.stackTraceToString()}]")
            }
        }

        return ResponseEntity.badRequest().body(CustomOAuth2Exception(GENERAL_API.code, e.message ?: "Error!"))
    }
}