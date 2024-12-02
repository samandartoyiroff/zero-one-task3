package uz.thirdTask.auth

import org.springframework.boot.CommandLineRunner
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Component

@Component
class DataLoader(
    private val passwordEncoder: BCryptPasswordEncoder,
    private val authClientRepository: AuthClientRepository
) : CommandLineRunner {
    override fun run(vararg args: String?) {
        val defaultPassword = passwordEncoder.encode("default")
        val adminClientName = "web-admin"
        val webClientName = "web-client"
        val androidClientName = "android-client"
        val iosClientName = "ios-client"

        checkAndCreate(adminClientName, defaultPassword, mutableSetOf("ADMIN"))
        checkAndCreate(webClientName, defaultPassword, mutableSetOf("WEB"))
        checkAndCreate(androidClientName, defaultPassword, mutableSetOf("WEB"))
        checkAndCreate(iosClientName, defaultPassword, mutableSetOf("WEB"))
    }

    private fun checkAndCreate(clientName: String, clientPassword: String, scopes: MutableSet<String>) {
        val grantTypes = mutableSetOf("password", "refresh_token")
        authClientRepository.findByClientId(clientName) ?: authClientRepository.save(
            AuthClient(clientName, clientPassword, 1000000000, 1000000000, grantTypes, scopes)
        )
    }

}