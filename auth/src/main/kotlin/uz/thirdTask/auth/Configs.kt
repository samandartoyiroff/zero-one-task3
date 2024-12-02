package uz.thirdTask.auth

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.support.ResourceBundleMessageSource
import org.springframework.http.HttpMethod
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer
import org.springframework.security.oauth2.provider.*
import org.springframework.security.oauth2.provider.token.AbstractTokenGranter
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import java.util.*


@Configuration
class WebMvcConfig : WebMvcConfigurer {

    @Bean
    fun errorMessageSource() = ResourceBundleMessageSource().apply {
        setDefaultEncoding(Charsets.UTF_8.name())
        setBasename("error")
    }
}

@Configuration
class WebSecurityConfig(val authenticationProvider: CustomAuthenticationProvider) : WebSecurityConfigurerAdapter() {

    @Throws(Exception::class)
    override fun configure(auth: AuthenticationManagerBuilder) {
        auth.authenticationProvider(authenticationProvider)
    }

    @Bean
    @Throws(Exception::class)
    override fun authenticationManagerBean(): AuthenticationManager = super.authenticationManagerBean()

    @Bean
    fun passwordEncoder() = BCryptPasswordEncoder()

    override fun configure(http: HttpSecurity) {
        http
            .sessionManagement()
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .csrf()?.disable()
    }
}

@EnableResourceServer
@Configuration
class ResourceServerConfig : ResourceServerConfigurerAdapter() {
    override fun configure(resources: ResourceServerSecurityConfigurer?) {
        super.configure(resources)
    }

    override fun configure(http: HttpSecurity) {

        http
            .requestMatchers()
            .and()
            .authorizeRequests()
            .antMatchers(HttpMethod.DELETE, "/token").permitAll()
            .antMatchers("/internal/**","/swagger-ui/").permitAll()
            .antMatchers(HttpMethod.POST, "/oauth/eimzo").permitAll()
            .antMatchers(HttpMethod.GET, "/eimzo/*").permitAll()
            .antMatchers("/o-session/**").authenticated()
            .antMatchers("/**").authenticated()
    }
}

@EnableAuthorizationServer
@Configuration
class AuthorizationConfig(
    private val authenticationManager: AuthenticationManager,
    private val authClientDetailsService: AuthClientDetailsService,
    private val passwordEncoder: BCryptPasswordEncoder,
    private val userDetailsService: CustomUserDetailsService,
    private val mongoTokenStore: MongoTokenStore,
    private val translator: CustomOAuthTranslator
) : AuthorizationServerConfigurerAdapter() {

    override fun configure(clients: ClientDetailsServiceConfigurer) {
        clients.withClientDetails(authClientDetailsService)
    }

    override fun configure(endpoints: AuthorizationServerEndpointsConfigurer) {
        val compositeTokenGranter = CompositeTokenGranter(
            mutableListOf(
                endpoints.tokenGranter,
                *GrantType.values().map {
                    endpoints.run {
                        AuthTokenGranter(
                            authenticationManager, tokenServices, clientDetailsService, oAuth2RequestFactory, it.value
                        )
                    }
                }.toTypedArray(),
            )
        )

        endpoints
            .tokenStore(mongoTokenStore)
            .userDetailsService(userDetailsService)
            .authenticationManager(authenticationManager)
            .reuseRefreshTokens(false)
            .tokenGranter(compositeTokenGranter)
            .exceptionTranslator(translator)
    }

    @Throws(Exception::class)
    override fun configure(oauthServer: AuthorizationServerSecurityConfigurer) {
        oauthServer
            .tokenKeyAccess("permitAll()")
            .checkTokenAccess("isAuthenticated()")
            .passwordEncoder(passwordEncoder)
            .allowFormAuthenticationForClients()
    }


}

class AuthTokenGranter(
    private val authenticationManager: AuthenticationManager,
    authorizationServerTokenServices: AuthorizationServerTokenServices,
    clientDetailsService: ClientDetailsService,
    requestFactory: OAuth2RequestFactory,
    grantType: String
) : AbstractTokenGranter(authorizationServerTokenServices, clientDetailsService, requestFactory, grantType) {

    override fun getOAuth2Authentication(client: ClientDetails?, tokenRequest: TokenRequest?): OAuth2Authentication {
        val value = tokenRequest?.requestParameters?.get(getGrantTypeKey(tokenRequest))
        val userAuthentication = UsernamePasswordAuthenticationToken(value, null)
        userAuthentication.details = tokenRequest?.requestParameters
        return OAuth2Authentication(
            tokenRequest?.createOAuth2Request(client!!),
            authenticationManager.authenticate(userAuthentication)
        )
    }

    private fun getGrantTypeKey(tokenRequest: TokenRequest?): String {
        if (tokenRequest == null) throw Exception("TokenRequest null")
        GrantType.values().forEach {
            if (it.value == tokenRequest.grantType) return it.value
        }
        return tokenRequest.grantType
    }
}