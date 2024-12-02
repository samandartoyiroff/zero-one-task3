package uz.thirdTask.auth

enum class ErrorCode(val code: Int) {  // Need fix Error Code
    GENERAL_API(200),
    CLIENT_ACCESS(201),
    INCORRECT_PASSWORD(202),
    INVALID_GRANT_TYPE(203),
    SERVICE_EXCEPTION(204),
    ILLEGAL_TOKEN(205),
    NOT_ACTIVE_USER(206),
    FEIGN_ERROR(207),
    INTERNAL_EXCEPTION(208),
    SESSION_NOT_FOUND(209),
    USER_PENDING(20),
    USER_BLOCKED(30)
}

enum class GrantType(val value: String) {
    REFRESH_TOKEN("refresh_token"),
    PASSWORD("password");

    companion object {
        private val keysMap = GrantType.values().associateBy { it.value }
        fun toGrantType(key: String): GrantType? {
            return keysMap[key]
        }
    }
}

enum class UserStatus {
    INACTIVE,
    BLOCKED,
    ACTIVE,
    PENDING
}