package uz.thirdTask.course

import com.fasterxml.jackson.annotation.JsonInclude
import org.jetbrains.annotations.NotNull
import java.math.BigDecimal
import java.util.*
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


data class CourseCreateRequest(
    @field:NotNull
    @field:NotBlank(message = "Course name cannot be null and blank")
    val name: String,

    @field:NotNull
    val price: BigDecimal,

    val description: String
){
    fun toEntity(): Course {
        return Course(name,price,description)
    }
}

data class CourseUpdateRequest(
    val name: String?,
    val price: BigDecimal?,
    val description: String?
)

data class CourseResponse(
    val id: Long,
    val name: String,
    val price: BigDecimal,
    val description: String?
){
    companion object{
        fun toResponse(course: Course): CourseResponse {
            course.run {
                return CourseResponse(id!!,name,price,description)
            }
        }
    }
}

data class UserCourseCreateRequest(
    @field:NotNull
    val userId: Long,
    @field:NotNull
    val courseId: Long,
){
    fun toEntity(course: Course): UserCourse {
        return UserCourse(userId,course)
    }
}

data class UserCourseUpdateRequest(
    val userId: Long?,
    var courseId: Long?,
)

data class UserCourseResponse(
    val id: Long,
    val userId: Long,
    val courseId: Long
){
    companion object{
        fun toResponse(userCourse: UserCourse): UserCourseResponse {
            return userCourse.run {
                UserCourseResponse(id!!, userId, course.id!!)
            }
        }
    }
}

data class PaymentCreateRequest(
    @field:NotNull
    val userId: Long,
    @field:NotNull
    val courseId: Long,
    @field:NotNull
    val amount: BigDecimal,
    val date: Date
)

data class PaymentResponse(
    val id: Long,
    val userId: Long,
    val courseId: Long,
    val amount: BigDecimal,
    val date: Date
)

data class UserResponse(
    val id: Long,
    val username: String,
    val fullName: String,
    val role: String,
    val status: String
)










