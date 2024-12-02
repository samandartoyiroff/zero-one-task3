package uz.thirdTask.auth

import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.context.support.ResourceBundleMessageSource
import java.util.*

sealed class AuthException(message: String? = null) : RuntimeException(message) {
    abstract fun errorCode(): ErrorCode
    protected open fun getErrorMessageArguments(): Array<Any?>? = null

    fun getOAuth2Exception(messageSource: ResourceBundleMessageSource): CustomOAuth2Exception {
        val locale = Locale(getHeaderLocale() ?: LocaleContextHolder.getLocale().language)

        val message = messageSource.getMessage(errorCode().name, getErrorMessageArguments(), locale)

        return CustomOAuth2Exception(errorCode().code, message)
    }

    fun getErrorMessage(errorMessageSource: ResourceBundleMessageSource): BaseMessage {
        val errorMessage = try {
            errorMessageSource.getMessage(errorCode().name, getErrorMessageArguments(), LocaleContextHolder.getLocale())
        } catch (e: Exception) {
            e.message ?: "Error"
        }
        return BaseMessage(errorCode().code, errorMessage)
    }
}

class GeneralApiException(message: String?) : AuthException(message) {
    override fun errorCode() = ErrorCode.GENERAL_API
    override fun getErrorMessageArguments(): Array<Any?>? = message?.let { arrayOf(it) }
}

class FeignCallException : AuthException() {
    override fun errorCode() = ErrorCode.FEIGN_ERROR
}

class InternalServiceCallException(val code: Int, val errorMessage: String) : AuthException() {
    override fun errorCode() = ErrorCode.INTERNAL_EXCEPTION
}

class SessionNotFoundException : AuthException() {
    override fun errorCode() = ErrorCode.SESSION_NOT_FOUND
}

class ClientAccessException : AuthException() {
    override fun errorCode() = ErrorCode.CLIENT_ACCESS
}

class IncorrectPasswordException : AuthException() {
    override fun errorCode() = ErrorCode.INCORRECT_PASSWORD
}

class InvalidGrantTypeException(private val grantType: String) : AuthException() {
    override fun errorCode() = ErrorCode.INVALID_GRANT_TYPE
    override fun getErrorMessageArguments(): Array<Any?> = arrayOf(grantType)
}

class ServiceException : AuthException() {
    override fun errorCode() = ErrorCode.SERVICE_EXCEPTION
}

class IllegalTokenException : AuthException() {
    override fun errorCode() = ErrorCode.ILLEGAL_TOKEN
}

class NotActiveUserException : AuthException() {
    override fun errorCode() = ErrorCode.NOT_ACTIVE_USER
}

class UserBlockedException() : AuthException() {
    override fun errorCode() = ErrorCode.USER_BLOCKED
}

class UserPendingException() : AuthException() {
    override fun errorCode() = ErrorCode.USER_PENDING
}



