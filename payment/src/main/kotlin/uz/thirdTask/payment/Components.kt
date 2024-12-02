package uz.thirdTask.payment

import com.fasterxml.jackson.databind.ObjectMapper
import feign.Response
import feign.codec.ErrorDecoder
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component


@Component
class FeignErrorDecoder : ErrorDecoder {

    private val logger: Logger = LoggerFactory.getLogger(this.javaClass)
    private val mapper = ObjectMapper()

    override fun decode(methodKey: String, response: Response): Exception {
        return response.run {
            val error = doSafe {
                val jsonNode = mapper.readTree(body().asInputStream())
                val code = jsonNode.get("code")?.asInt()
                val message = jsonNode.get("message")?.asText()
                if (code != null && message != null) {
                    BaseMessage(code, message)
                } else {
                    throw RuntimeException("$jsonNode, status = ${status()}, method = $methodKey")
                }
            }
            if (error != null) {
                FeignErrorException(error.code, error.message)
            } else {
                logger.warn("Feign error: $methodKey ${status()}")
                RuntimeException()
            }
        }
    }
}