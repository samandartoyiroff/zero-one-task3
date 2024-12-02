package uz.thirdTask.auth

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Service

interface Authenticator {
    fun authenticate(authentication: Authentication): Authentication
    fun grantType(): GrantType
}

@Service
class PasswordAuthenticator(
    private val internalUserService: InternalUserService
) : Authenticator {

    override fun authenticate(authentication: Authentication): Authentication {
        val username = authentication.principal as String
        val password = authentication.credentials as String

        val authenticationRequestDto = FindByUsernameRequest(username, password)

        val user = internalUserService.findByUsername(authenticationRequestDto)

        if (user.status == UserStatus.PENDING) throw UserPendingException()
        if (user.status == UserStatus.BLOCKED) throw UserBlockedException()
        val result = UsernamePasswordAuthenticationToken(username, null, user.getAuthorities())
        result.details = user
        return result
    }

    override fun grantType() = GrantType.PASSWORD
}
