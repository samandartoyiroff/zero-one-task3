package uz.thirdTask.user

import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.context.support.ResourceBundleMessageSource

sealed class DBusinessException : RuntimeException() {

    abstract fun errorCode(): ErrorCode

    open fun getErrorMessageArguments(): Array<Any?>? = null

    fun getErrorMessage(errorMessageSource: ResourceBundleMessageSource): BaseMessage {
        val errorMessage = try {
            errorMessageSource.getMessage(errorCode().name, getErrorMessageArguments(), LocaleContextHolder.getLocale())
        } catch (e: Exception) {
            e.message
        }
        return BaseMessage(errorCode().code, errorMessage)
    }
}

class FeignErrorException(val code: Int?, val errorMessage: String?) : DBusinessException() {
    override fun errorCode() = ErrorCode.FEIGN_ERROR
}

class UserNotFoundException : DBusinessException() {
    override fun errorCode(): ErrorCode = ErrorCode.USER_NOT_FOUND
}

class UserAlreadyExistException : DBusinessException() {
    override fun errorCode(): ErrorCode = ErrorCode.USER_ALREADY_EXISTS
}

class SomethingWentWrongException : DBusinessException(){
    override fun errorCode(): ErrorCode = ErrorCode.SOMETHING_WENT_WRONG
}

class Task3UniversalException(errorCode: ErrorCode) : DBusinessException() {
    override fun errorCode(): ErrorCode {
        return errorCode()
    }
}