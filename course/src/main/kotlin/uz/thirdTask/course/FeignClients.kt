package uz.thirdTask.course

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody


@FeignClient("demo", configuration = [Auth2TokenConfiguration::class])
interface DemoService {
    @GetMapping("hello")
    fun getHelloMessageSecure(): String
}

@FeignClient("payment",configuration = [Auth2TokenConfiguration::class])
interface PaymentClient{
    @PostMapping
    fun createPayment(@RequestBody request:PaymentCreateRequest)

}

@FeignClient("user", configuration = [Auth2TokenConfiguration::class])
interface UserClient{
    @GetMapping("{id}")
    fun getUser(@PathVariable id: Long): UserResponse
}