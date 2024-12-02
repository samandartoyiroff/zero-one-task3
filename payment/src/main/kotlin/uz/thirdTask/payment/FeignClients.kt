package uz.thirdTask.payment

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable


@FeignClient("demo", configuration = [Auth2TokenConfiguration::class])
interface DemoService {
    @GetMapping("hello")
    fun getHelloMessageSecure(): String
}

@FeignClient("users", configuration = [Auth2TokenConfiguration::class])
interface UserClient{
    @GetMapping("{id}")
    fun getUser(@PathVariable id: Long): UserResponse
}

@FeignClient("course", configuration = [Auth2TokenConfiguration::class])
interface CourseClient{
    @GetMapping("{id}")
    fun getCourse(@PathVariable id: Long): CourseResponse
}