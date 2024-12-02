package uz.thirdTask.user

import org.springframework.boot.CommandLineRunner
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Component

@Component
class DataLoader(
    private val passwordEncoder: BCryptPasswordEncoder
) : CommandLineRunner {
    override fun run(vararg args: String?) {

    }
}
