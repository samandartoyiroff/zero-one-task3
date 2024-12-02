package uz.thirdTask.payment

import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.context.support.ResourceBundleMessageSource
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.ResponseStatus

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

class CourseNotFoundException : DBusinessException() {
    override fun errorCode(): ErrorCode = ErrorCode.COURSE_NOT_FOUND
}
class PaymentNotFoundException : DBusinessException() {
    override fun errorCode(): ErrorCode = ErrorCode.COURSE_NOT_FOUND
}

class CourseAlreadyExistException : DBusinessException() {
    override fun errorCode(): ErrorCode = ErrorCode.COURSE_ALREADY_EXISTS
}

class SomethingWentWrongException : DBusinessException(){
    override fun errorCode(): ErrorCode = ErrorCode.SOMETHING_WENT_WRONG
}

class Task3UniversalException(errorCode: ErrorCode) : DBusinessException() {
    override fun errorCode(): ErrorCode {
        return errorCode()
    }
}



@ControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(CourseNotFoundException::class)
    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    @ResponseBody
    fun handleCourseNotFoundException(ex: CourseNotFoundException): BaseMessage {
        return BaseMessage(HttpStatus.NOT_FOUND.value(), ex.message)
    }

    @ExceptionHandler(Exception::class)
    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    fun handleGeneralException(ex: Exception): BaseMessage {
        return BaseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), ex.message ?: "An error occurred")
    }

}