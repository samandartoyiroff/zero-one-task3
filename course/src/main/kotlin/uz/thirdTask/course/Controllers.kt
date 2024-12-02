package uz.thirdTask.course

import org.springframework.web.bind.annotation.*
import javax.validation.Valid

@RestController
//@RequestMapping()
class CourseController(
    private val courseService: CourseService,
    private val userCourseService: UserCourseService
) {

    @PostMapping
    fun create(@Valid@RequestBody request: CourseCreateRequest) = courseService.create(request)

    @GetMapping("{id}")
    fun getById(@PathVariable id: Long): CourseResponse = courseService.getById(id)

    @GetMapping
    fun getAll() = courseService.getAll()

    @PutMapping("{id}")
    fun update(@PathVariable id: Long, @Valid@RequestBody request: CourseUpdateRequest) = courseService.update( request, id)

    @DeleteMapping("{id}")
    fun delete(@PathVariable id: Long) = courseService.delete(id)


    @PostMapping("register-course-to-user")
    fun registerCourse(@Valid@RequestBody request: UserCourseCreateRequest) = userCourseService.registerCourseToUser(request)

    @GetMapping("get-all-registered-courses")
    fun getAllSoldCourses() = userCourseService.getAll()

    @GetMapping("get-registered-course/{id}")
    fun getSoldCourseById(@PathVariable id: Long) = userCourseService.getById(id)

    @PutMapping("update-registered-course/{id}")
    fun updateSoldCourse(@Valid@RequestBody request: UserCourseUpdateRequest, @PathVariable id: Long) = userCourseService.update(request,id)

    @DeleteMapping("delete-registered-course/{id}")
    fun deleteSoldCourse(@PathVariable id: Long) = userCourseService.delete(id)

    @GetMapping("get-courses-of-user/{userId}")
    fun getAllUserCourses(@PathVariable userId: Long) = userCourseService.getAllUserCourses(userId)


}



