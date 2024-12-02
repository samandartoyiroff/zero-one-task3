package uz.thirdTask.course

import org.springframework.security.core.context.SecurityContext
import org.springframework.security.oauth2.provider.OAuth2Authentication
import java.util.function.Supplier

fun SecurityContext.getUserId(): Long? {
    val userId = getDetail("userId") ?: return null
    if (userId is Int) return userId.toLong()
    return userId as Long
}


fun SecurityContext.getRole(): String? {
    val role = getDetail("role") ?: return null
    return role as String
}

fun SecurityContext.getEmail(): String? {
    val email = getDetail("email") ?: return null
    return email as String
}

fun SecurityContext.isEmailVerified(): Boolean? {
    val emailVerified = getDetail("emailVerified") ?: return null
    return emailVerified as Boolean
}

fun SecurityContext.pinflNotNull(): String? {
    val pinfl = getDetail("pinfl") ?: return null
    return pinfl as String
}

fun SecurityContext.getDetail(key: String): Any? {
    if (authentication is OAuth2Authentication) {
        val details = (authentication as OAuth2Authentication).userAuthentication.details as Map<*, *>
        return details[key]
    }
    return null
}

fun SecurityContext.getPlatform(): String? {
    if (authentication is OAuth2Authentication) {
        val details = (authentication as OAuth2Authentication).userAuthentication.details as Map<*, *>
        return details["deviceType"] as String
    }
    return null
}

fun SecurityContext.getDeviceId(): String {
    if (authentication is OAuth2Authentication) {
        val details = (authentication as OAuth2Authentication).userAuthentication.details as Map<*, *>
        val username = details["username"] as String
        return username.split(":")[1]
    }
    return ""
}

fun Boolean.throwIfTrue(action: Supplier<out Throwable>) {
    if (this) throw action.get()
}

fun Boolean.throwIfFalse(action: Supplier<out Throwable>) {
    if (!this) throw action.get()
}

fun <T> doSafe(func: () -> T): T? {
    return try {
        func()
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

fun String.maskedPhoneNumber() = this.replaceRange(6, 9, "***")

