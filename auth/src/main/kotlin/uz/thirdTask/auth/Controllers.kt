package uz.thirdTask.auth

import org.springframework.context.support.ResourceBundleMessageSource
import org.springframework.http.ResponseEntity
import org.springframework.security.oauth2.provider.OAuth2Authentication
import org.springframework.web.bind.annotation.*
import org.springframework.web.bind.annotation.ExceptionHandler

@RestController
@RequestMapping("user")
class UserController(private val authUserInfoService: AuthUserInfoService) {

    @GetMapping("current")
    fun getUser(authentication: OAuth2Authentication) = authUserInfoService.getCurrentUserInfo(authentication)
}

@ControllerAdvice
class ExceptionHandler(private val source: ResourceBundleMessageSource) {

    // Only for badRequest.
    @ExceptionHandler(AuthException::class)
    fun handleDBusinessException(exception: AuthException): ResponseEntity<BaseMessage> {
        return when (exception) {
            is InternalServiceCallException -> ResponseEntity.badRequest()
                .body(BaseMessage(exception.code, exception.errorMessage))

            else -> ResponseEntity.badRequest().body(exception.getErrorMessage(source))
        }
    }
}