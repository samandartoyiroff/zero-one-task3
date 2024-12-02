package uz.thirdTask.user

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping


@FeignClient("demo", configuration = [Auth2TokenConfiguration::class])
interface DemoService {
    @GetMapping("hello")
    fun getHelloMessageSecure(): String
}