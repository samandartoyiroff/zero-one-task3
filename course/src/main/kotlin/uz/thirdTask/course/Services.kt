package uz.thirdTask.course

import org.springframework.stereotype.Service
import java.util.Date
import javax.transaction.Transactional

interface CourseService {

    fun create(request: CourseCreateRequest)

    fun getAll(): List<CourseResponse>

    fun getById(id: Long): CourseResponse

    fun update(request: CourseUpdateRequest, id: Long)

    fun delete(id: Long)


}

@Service
class CourseServiceImpl(
    private val courseRepository: CourseRepository,
) : CourseService {

    override fun create(request: CourseCreateRequest) {

        request.run {
          courseRepository.save(this.toEntity())
        }

    }

    override fun getAll(): List<CourseResponse> {
       return courseRepository.findAllNotDeleted().map {
            CourseResponse.toResponse(it)
        }
    }

    override fun getById(id: Long): CourseResponse {
        if(!courseRepository.existsById(id)) throw CourseNotFoundException()
       return courseRepository.findByIdAndDeletedFalse(id)?.let {
            CourseResponse.toResponse(it)
        }?: throw CourseNotFoundException()
    }

    override fun update(request: CourseUpdateRequest, id: Long) {

        val course = courseRepository.findByIdAndDeletedFalse(id) ?: throw CourseNotFoundException()
        request.name?.let { course.name = it }
        request.price?.let { course.price = it }
        request.description?.let { course.description = it }
        courseRepository.save(course)

    }

    override fun delete(id: Long) {

        courseRepository.trash(id)?: throw CourseNotFoundException()

    }

}

interface UserCourseService {

    fun registerCourseToUser(request: UserCourseCreateRequest)

    fun getAll(): List<UserCourseResponse>

    fun getById(id: Long): UserCourseResponse

    fun update(request: UserCourseUpdateRequest, id: Long)

    fun delete(id: Long)

    fun getAllUserCourses(userId: Long): List<UserCourseResponse>


}

@Service
class UserCourseServiceImpl(
   private val courseRepository: CourseRepository,
   private val userCourseRepository: UserCourseRepository,
   private val paymentClient: PaymentClient,
   private val userClient: UserClient
): UserCourseService {

    @Transactional
    override fun registerCourseToUser(request: UserCourseCreateRequest) {

        request.run {

           val course = courseRepository.findByIdAndDeletedFalse(courseId)?: throw CourseNotFoundException()
            val user = userClient.getUser(userId)

            if(user.id!=userId) throw SomethingWentWrongException()

            userCourseRepository.save(this.toEntity(course))

            paymentClient.createPayment(PaymentCreateRequest(userId,courseId,course.price,Date()))

        }

    }

    override fun getAll(): List<UserCourseResponse> {
       return userCourseRepository.findAllNotDeleted().map {
            UserCourseResponse.toResponse(it)
        }
    }

    override fun getById(id: Long): UserCourseResponse {
       if(!userCourseRepository.existsById(id)) throw CourseNotFoundException()

        val userCourse = userCourseRepository.findByIdAndDeletedFalse(id) ?: throw CourseNotFoundException()

        return UserCourseResponse.toResponse(userCourse)
    }

    override fun update(request: UserCourseUpdateRequest, id: Long) {

        val userCourse = userCourseRepository.findByIdAndDeletedFalse(id) ?: throw CourseNotFoundException()

        request.run {
            userCourse.apply {
                userId = request.userId ?: userId
                courseId = request.courseId ?: courseId
            }
        }

        userCourseRepository.save(userCourse)

    }

    override fun delete(id: Long) {

        userCourseRepository.trash(id) ?: throw CourseNotFoundException()

    }

    override fun getAllUserCourses(userId: Long): List<UserCourseResponse> {

        return userCourseRepository.findAllByUserIdAndDeletedFalse(userId).map {
            UserCourseResponse.toResponse(it)
        }

    }
}

