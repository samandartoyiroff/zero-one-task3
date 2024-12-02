package uz.thirdTask.payment

import com.fasterxml.jackson.annotation.JsonInclude
import org.jetbrains.annotations.NotNull
import java.math.BigDecimal
import java.util.Date
import javax.validation.constraints.NotBlank

@JsonInclude(JsonInclude.Include.NON_NULL)
data class BaseMessage(
    val code: Int?,
    val message: String?
) {
    companion object {
        val OK = BaseMessage(0, "OK")
    }
}

data class AuthUser(
    val username: String,
    val password: String?
)

data class PaymentCreateRequest(

    @field:NotNull val userId: Long,

    @field:NotNull val courseId: Long,

    @field:NotNull val amount: BigDecimal,

    val date: Date
){
    fun toEntity(): Payment {
        return Payment(userId,courseId,amount,date)
    }
}


data class PaymentResponse(
    val id: Long,
    val userId: Long,
    val courseId: Long,
    val amount: BigDecimal,
    val date: Date
){
    companion object{
        fun toResponse(course: Payment): PaymentResponse {
            course.run {
                return PaymentResponse(id!!,userId,courseId,amount,date)
            }
        }
    }
}

data class UserResponse(
    val id: Long,
    val username: String,
    val fullName: String,
    val role: String,
    val status: String
)

data class CourseResponse(
    val id: Long,
    val name: String,
    val price: BigDecimal,
    val description: String?
)










