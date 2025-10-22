package cr.una.pai.domain

//import cr.una.pai.domain.AppCustomDsl.Companion.customDsl
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpMethod
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.dao.DaoAuthenticationProvider
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import jakarta.annotation.Resource
import java.net.URI
import java.net.URISyntaxException

// ======================================
// 🔓 CONFIG LOCAL (sin seguridad JWT)
// ======================================
@Profile("initlocal")
@Configuration
@EnableWebSecurity
class OpenSecurityConfiguration {

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .cors { it.disable() }
            .authorizeHttpRequests { it.anyRequest().permitAll() }

        return http.build()
    }
}

// ======================================
// 🔐 CONFIG JWT (seguridad activa)
// ======================================
@Profile("!initlocal")
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
class JwtSecurityConfiguration {

    @Value("\${url.user.signup}")
    private val URL_SIGNUP: String? = null

    @Value("\${url.user.login}")
    private val URL_LOGIN: String? = null

    @Value("\${cors.allowed-origins:http://localhost:3000}")
    private val corsAllowedOrigins: String? = null


    @Resource
    private val userDetailsService: AppUserDetailsService? = null

    @Bean
    fun passwordEncoder() = BCryptPasswordEncoder()

    @Bean
    fun authenticationProvider(): DaoAuthenticationProvider {
        val authProvider = DaoAuthenticationProvider()
        authProvider.setUserDetailsService(userDetailsService)
        authProvider.setPasswordEncoder(passwordEncoder())
        return authProvider
    }

    @Bean
    fun authenticationManager(config: AuthenticationConfiguration): AuthenticationManager =
        config.authenticationManager

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val source = UrlBasedCorsConfigurationSource()
        val config = CorsConfiguration().apply {
            allowCredentials = true
            val origins = corsAllowedOrigins
                ?.split(",")
                ?.map { it.trim() }
                ?.filter { it.isNotEmpty() }
                ?.ifEmpty { listOf("http://localhost:3000") }
                ?: listOf("http://localhost:3000")
            allowedOrigins = origins.toMutableList()
            addAllowedHeader("*")
            addAllowedMethod("*")
        }
        source.registerCorsConfiguration("/**", config)
        return source
    }

    @Bean
    fun filterChain(http: HttpSecurity, authConfig: AuthenticationConfiguration): SecurityFilterChain {
        val authManager = authenticationManager(authConfig)
        val publicSignupPath = URL_SIGNUP.toRequestPath("/api/v1/users/signup")
        val publicLoginPath = URL_LOGIN.toRequestPath("/api/v1/users/login")


        http
            .csrf { it.disable() }
            .cors { it.configurationSource(corsConfigurationSource()) }
            .authorizeHttpRequests {
                it
                    .requestMatchers(publicSignupPath).permitAll()
                    .requestMatchers(publicLoginPath).permitAll()
                    .requestMatchers("/api/v1/unsecure/**").permitAll()
                    .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**", "/webjars/**").permitAll()
                    .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                    .requestMatchers("/").permitAll() // Permitir acceso público al endpoint raíz
                    .anyRequest().permitAll()
            }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authenticationProvider(authenticationProvider())
            .addFilter(JwtAuthenticationFilter(authManager))
            .addFilter(JwtAuthorizationFilter(authManager))

        return http.build()
    }

    private fun String?.toRequestPath(defaultPath: String): String {
        val candidate = this?.takeIf { it.isNotBlank() }?.trim() ?: return defaultPath
        return try {
            val uri = URI(candidate)
            if (uri.scheme.isNullOrBlank()) {
                ensureLeadingSlash(candidate)
            } else {
                val path = uri.path?.takeIf { it.isNotBlank() } ?: "/"
                ensureLeadingSlash(path)
            }
        } catch (_: URISyntaxException) {
            ensureLeadingSlash(candidate)
        } catch (_: IllegalArgumentException) {
            ensureLeadingSlash(candidate)
        }
    }

    private fun ensureLeadingSlash(path: String): String =
        if (path.startsWith("/")) path else "/$path"
}
