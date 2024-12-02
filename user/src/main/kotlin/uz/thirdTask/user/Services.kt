package uz.thirdTask.user

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service
import javax.management.relation.RoleNotFoundException


interface UserService {
    fun findByAuthUser(request: AuthUser): UserInfo

    fun create(request: UserCreateRequest)

    fun getAll(): List<UserResponse>

    fun getById(id: Long): UserResponse

    fun update(request: UserUpdateRequest, id: Long)

    fun delete(id: Long)

    fun register(request: UserRegisterRequest) : UserResponse
}

@Service
class UserServiceImpl(
    private val userRepository: UserRepository,
    private val passwordEncoder: BCryptPasswordEncoder,
    private val roleRepository: RoleRepository
) : UserService {
    override fun findByAuthUser(request: AuthUser): UserInfo {
        val user = userRepository.findByUsername(request.username) ?: throw RuntimeException("USer not found")

        if (request.password == null) return UserInfo.toDto(user)

        if (passwordEncoder.matches(request.password, user.password)) {
            return UserInfo.toDto(user)
        } else {
            throw RuntimeException("User not found or incorrect password")
        }
    }

    override fun create(request: UserCreateRequest) {
        request.run {
            val user = userRepository.findByUsernameAndDeletedFalse(username)

            if (user != null)
                throw UserAlreadyExistException()

            val role = roleRepository.findByKeyAndDeletedFalse("user")?: throw RoleNotFoundException()

            userRepository.save(this.toEntity(role))
        }
    }

    override fun getAll(): List<UserResponse> {

        return userRepository.findAllNotDeleted().map { UserResponse.toResponse(it) }

    }

    override fun getById(id: Long): UserResponse {

        if(!userRepository.existsById(id)) throw UserNotFoundException()

        return userRepository.findByIdAndDeletedFalse(id)?.let {
            UserResponse.toResponse(it)
        } ?: throw UserNotFoundException()

    }

    override fun update(request: UserUpdateRequest, id: Long) {
        val user = userRepository.findByIdAndDeletedFalse(id) ?: throw UserNotFoundException()
        request.run {
            username?.let {

                val exists = userRepository.existsByUsername(username)
                if (exists) throw UserAlreadyExistException()
                user.username = it
            }
            fullName?.let { user.fullName = it }
            status?.let { user.status = it }
        }

        userRepository.save(user)
    }

    override fun delete(id: Long) {
        userRepository.trash(id)?: throw UserNotFoundException()
    }

    override fun register(request: UserRegisterRequest): UserResponse {
        request.run {
            val user = userRepository.findByUsernameAndDeletedFalse(username)

            if (user != null)
                throw UserAlreadyExistException()

            val role: Role = roleRepository.findByKeyAndDeletedFalse(
                    "US") ?: throw IllegalArgumentException("Role key must not be null")

            val encoded = passwordEncoder.encode(password)

            val saved = userRepository.save(User(username, encoded, role, fullName))
            return UserResponse.toResponse(saved)
        }
    }

}

interface RoleService {

    fun create(request: RoleCreateRequest)

    fun getAll(): List<RoleResponse>

    fun getById(id: Long): RoleResponse

    fun update(request: RoleUpdateRequest, id: Long)

    fun delete(id: Long)
}

@Service
class RoleServiceImpl(
    private val roleRepository: RoleRepository,
): RoleService {
    override fun create(request: RoleCreateRequest) {

        roleRepository.save(request.run { this.toEntity()})

    }

    override fun getAll(): List<RoleResponse> {
      return roleRepository.findAllNotDeleted().map {
          RoleResponse.toResponse(it)
       }
    }

    override fun getById(id: Long): RoleResponse {

        if(!roleRepository.existsById(id)) throw RoleNotFoundException()

       return roleRepository.findByIdAndDeletedFalse(id)?.let {
           RoleResponse.toResponse(it)
        }?: throw RoleNotFoundException()
    }

    override fun update(request: RoleUpdateRequest, id: Long) {

        if(!roleRepository.existsById(id)) throw RoleNotFoundException()

        val role = roleRepository.findByIdAndDeletedFalse(id)?: throw RoleNotFoundException()

        request.run {
            key?.let { role.key = it }
            name?.let { role.name = it }
        }

        roleRepository.save(role)
    }

    override fun delete(id: Long) {

        if(!roleRepository.existsById(id)) throw RoleNotFoundException()

        roleRepository.trash(id)
    }

}
