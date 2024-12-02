package uz.thirdTask.gateway

import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter

@EnableResourceServer
@Configuration
class ResourceServerConfig : ResourceServerConfigurerAdapter() {

    @Throws(Exception::class)
    override fun configure(http: HttpSecurity) {
        http.requestMatchers()
            .and()
            .authorizeRequests()
            .antMatchers("/api/v1/*/internal/**").denyAll()
            .antMatchers("/api/v1/auth/oauth/token").permitAll()
            .antMatchers("/api/v1/user/register").permitAll()
            .antMatchers("/**").authenticated()
    }
}