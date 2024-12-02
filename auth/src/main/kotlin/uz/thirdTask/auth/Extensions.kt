package uz.thirdTask.auth

import org.springframework.security.core.context.SecurityContext
import org.springframework.security.oauth2.provider.OAuth2Authentication

fun SecurityContext.getAuthorities(): Set<String> {
    return authentication.authorities.map { it.authority }.toSet()
}

fun SecurityContext.getUserId(): Long? {
    if (authentication is OAuth2Authentication) {
        val authentication = (authentication as OAuth2Authentication)

        if (authentication.principal is CustomUserDetails) {
            return (authentication.principal as CustomUserDetails).userDto.id
        } else if (authentication.userAuthentication.details is UserAuthDto) {
            val details = authentication.userAuthentication.details as UserAuthDto
            return details.id
        } else {
            val details = authentication.userAuthentication.details as Map<*, *>
            val userId = details["userId"]
            if (userId is Int) return userId.toLong()
            return userId as Long
        }
    }
    return null
}

fun SecurityContext.getUserOrgId(): Long? {
    if (authentication is OAuth2Authentication) {
        val details = (authentication as OAuth2Authentication).userAuthentication.details as Map<*, *>
        val orgId = details["organizationId"] ?: return null
        return if (orgId is Int) orgId.toLong()
        else orgId as Long
    }
    return null
}