package uz.thirdTask.auth

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.oauth2.provider.ClientDetails
import org.springframework.security.oauth2.provider.ClientDetailsService
import org.springframework.security.oauth2.provider.OAuth2Authentication
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.*
import java.util.*

@FeignClient("user")
interface InternalUserService {
    @PostMapping("internal/find-by-username")
    fun findByUsername(@RequestBody request: FindByUsernameRequest): UserAuthDto
}

interface AuthUserInfoService {
    fun getCurrentUserInfo(oAuth2Authentication: OAuth2Authentication): Map<*, *>
}


@Service
class AuthClientDetailsService(val authClientRepository: AuthClientRepository) : ClientDetailsService {
    override fun loadClientByClientId(clientId: String?): ClientDetails {
        val authClient = authClientRepository.findByClientId(clientId)
        if (authClient != null) {
            return CustomClientDetails(authClient)
        } else {
            throw IllegalArgumentException()
        }
    }
}

@Service
class CustomUserDetailsService(val internalUserService: InternalUserService) : UserDetailsService {
    override fun loadUserByUsername(username: String): UserDetails {
        return CustomUserDetails(internalUserService.findByUsername(FindByUsernameRequest(username)))
    }
}

@Service
class AuthUserInfoServiceImpl : AuthUserInfoService {
    override fun getCurrentUserInfo(oAuth2Authentication: OAuth2Authentication): Map<*, *> {
        val user = if (oAuth2Authentication.userAuthentication is PreAuthenticatedAuthenticationToken) {
            (oAuth2Authentication.userAuthentication.principal as CustomUserDetails).userDto
        } else {
            oAuth2Authentication.userAuthentication.details as UserAuthDto
        }

        val response = mutableMapOf(
            "userId" to user.id,
            "username" to user.username,
            "name" to user.fullName,
            "role" to user.role,
            "authorities" to oAuth2Authentication.authorities,
            "clientId" to oAuth2Authentication.oAuth2Request.clientId,
        )

        return response
    }
}
