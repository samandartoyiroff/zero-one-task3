package uz.thirdTask.user

import com.fasterxml.jackson.annotation.JsonInclude
import org.jetbrains.annotations.NotNull

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

data class UserInfo(
    val id: Long,
    val username: String,
    val fullName: String,
    val role: String,
    val status: UserStatus
    //extra info
) {
    companion object {
        fun toDto(user: User) = user.run { UserInfo(id!!, username, fullName, role.key, status) }
    }
}


data class RoleCreateRequest(
    val key: String,
    val name: String
){
    fun toEntity() : Role {
        return Role (key,name)
    }
}

data class RoleUpdateRequest(
    val key: String?,
    val name: String?,
)

data class RoleResponse(
    val id: Long,
    val key: String,
    val name: String
){
    companion object {
        fun toResponse(role: Role): RoleResponse {
            role.run {
                return RoleResponse(id!!,key, name)
            }
        }
    }
}

data class UserRegisterRequest(
    @field:NotNull val username: String,
    @field:NotNull val password: String,
    @field:NotNull val fullName: String
)

data class UserCreateRequest(
    val username: String,
    val password: String,
    val fullName: String,
    val role: String?,
    val status: UserStatus?
) {
        fun toEntity(role: Role): User {
          return  User( username, password, role,fullName,)
        }
}

data class UserUpdateRequest(
    val username: String?,
    val password: String?,
    val fullName: String?,
    val roleKey: String?,
    val status: UserStatus?
)

data class UserResponse(
    val id: Long,
    val username: String,
    val fullName: String,
    val role: String,
    val status: String
){
    companion object {
        fun toResponse(user: User): UserResponse {
            user.run {
                return UserResponse(id!!, username, fullName, role.toString(), status.toString())
            }
        }
    }
}
