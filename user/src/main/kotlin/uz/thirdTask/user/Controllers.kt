package uz.thirdTask.user

import org.springframework.web.bind.annotation.*
import javax.validation.Valid


@RestController
@RequestMapping("internal")
class InternalController(
    private val service: UserService
) {
    @PostMapping("find-by-username")
    fun findByAuthUser(@RequestBody request: AuthUser) = service.findByAuthUser(request)

}


@RestController
//@RequestMapping("/user")
class UserController(
    private val userService: UserService
) {

    @PostMapping
    fun create(@Valid@RequestBody request: UserCreateRequest) = userService.create(request)

    @GetMapping
    fun getAll() : List<UserResponse> = userService.getAll()

    @GetMapping("{id}")
    fun getById(@PathVariable id: Long): UserResponse = userService.getById(id)

    @PutMapping("{id}")
    fun update(@PathVariable id:Long, @RequestBody request: UserUpdateRequest) = userService.update( request, id)

    @DeleteMapping("{id}")
    fun delete(@PathVariable id: Long) = userService.delete(id)

    @PostMapping("/register")
    fun register(@RequestBody request: UserRegisterRequest) = userService.register(request)

}

@RestController
@RequestMapping("role")
class RoleController(
    private val roleService: RoleService
) {

    @PostMapping
    fun create(@Valid@RequestBody request: RoleCreateRequest) = roleService.create(request)

    @GetMapping
    fun getAll() : List<RoleResponse> = roleService.getAll()

    @GetMapping("{id}")
    fun getById(@PathVariable id: Long): RoleResponse = roleService.getById(id)

    @PutMapping("{id}")
    fun update(@PathVariable id:Long, @RequestBody request: RoleUpdateRequest) = roleService.update( request, id)

    @DeleteMapping("{id}")
    fun delete(@PathVariable id: Long) = roleService.delete(id)

}