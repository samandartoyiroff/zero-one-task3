package uz.thirdTask.auth

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import org.springframework.security.core.AuthenticationException
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import java.util.*

fun getUserId() = SecurityContextHolder.getContext().getUserId()!!

fun currentUserName() = SecurityContextHolder.getContext().authentication.principal as String

fun getAuthorities() = SecurityContextHolder.getContext().getAuthorities()

fun getHeaderLocale(headerName: String = "hl") = getHeader(headerName)


fun getHeader(headerName: String): String? {
    return try {
        val request = (RequestContextHolder.currentRequestAttributes() as ServletRequestAttributes).request
        request.getHeader(headerName)
    } catch (e: java.lang.Exception) {
        println("${Date()} - e.message = ${e.message}")
        null
    }
}

fun <T> doSafe(func: () -> T): T? {
    try {
        return func()
    } catch (e: AuthenticationException) {
        throw e
    } catch (e: AuthException) {
        throw e
    } catch (e: Exception) {
        e.printStackTrace()
        throw e
//        return null
    }
}

class OAuth2ExceptionJackson3Serializer : StdSerializer<CustomOAuth2Exception>(CustomOAuth2Exception::class.java) {
    override fun serialize(value: CustomOAuth2Exception, jgen: JsonGenerator, provider: SerializerProvider) {
        jgen.writeStartObject()
        jgen.writeNumberField("code", value.code)
        jgen.writeStringField("message", value.message)
        jgen.writeEndObject()
    }
}
