package uz.thirdTask.auth

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.oauth2.common.exceptions.OAuth2Exception
import org.springframework.security.oauth2.provider.ClientDetails
import java.io.Serializable
import java.util.*

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
data class BaseMessage(var code: Int, var message: String) {
    companion object {
        val OK = BaseMessage(0, "OK")
    }
}

@JsonSerialize(using = OAuth2ExceptionJackson3Serializer::class)
data class CustomOAuth2Exception(
    val code: Int,
    val errorMessage: String
) : OAuth2Exception(errorMessage)

data class UserAuthDto(
    val id: Long,
    val username: String,
    val fullName: String,
    val role: String,
    val status: UserStatus
    //extra info
) : Serializable {
    fun getAuthorities(): List<SimpleGrantedAuthority> {
        return mutableListOf(role).map { SimpleGrantedAuthority(it) }
    }
}

class CustomUserDetails(val userDto: UserAuthDto) : UserDetails, Serializable {
    override fun getAuthorities() = userDto.getAuthorities()
    override fun isEnabled() = userDto.status == UserStatus.ACTIVE
    override fun getUsername() = userDto.username
    override fun isCredentialsNonExpired() = userDto.status == UserStatus.ACTIVE
    override fun getPassword() = "no-password"
    override fun isAccountNonExpired() = userDto.status == UserStatus.ACTIVE
    override fun isAccountNonLocked() = userDto.status == UserStatus.ACTIVE
}

class CustomClientDetails(private val authClient: AuthClient) : ClientDetails {
    override fun isSecretRequired() = true
    override fun getAdditionalInformation(): MutableMap<String, Any> = mutableMapOf()
    override fun getAccessTokenValiditySeconds() = authClient.accessTokenValidity
    override fun getResourceIds() = authClient.resources
    override fun getClientId() = authClient.clientId
    override fun isAutoApprove(scope: String?) = true
    override fun getAuthorities() = authClient.scopes.map { SimpleGrantedAuthority(it) }
    override fun getRefreshTokenValiditySeconds() = authClient.refreshTokenValidity
    override fun getClientSecret() = authClient.clientSecret
    override fun getRegisteredRedirectUri() = authClient.redirectUris
    override fun isScoped() = false
    override fun getScope() = authClient.scopes
    override fun getAuthorizedGrantTypes() = authClient.grantTypes
}

data class FindByUsernameRequest(
    val username: String,
    val password: String? = null
)


data class DeleteTokenByDevice(
    val userId: Long,
    val deviceId: String
)
