package cr.una.pai.domain

//import cr.una.pai.domain.AppCustomDsl.Companion.customDsl
import cr.una.pai.security.JwtAuthFilter
import jakarta.annotation.Resource
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
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.security.web.authentication.HttpStatusEntryPoint
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
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

    @Value("\${cors.allowed-origins:http://localhost:3000}")
    private val corsAllowedOrigins: String? = null

    @Value("\${api.endpoints.auth:/api/v1/auth}")
    private val AUTH_BASE: String? = null


    @Resource
    private val userDetailsService: AppUserDetailsService? = null

    @Bean
    fun passwordEncoder(): PasswordEncoder = object : PasswordEncoder {
        private val delegate = BCryptPasswordEncoder()

        override fun encode(rawPassword: CharSequence?): String {
            val candidate = rawPassword?.toString() ?: return ""
            return delegate.encode(candidate)
        }

        override fun matches(rawPassword: CharSequence?, encodedPassword: String?): Boolean {
            if (encodedPassword.isNullOrEmpty()) {
                return false
            }
            val candidate = rawPassword?.toString() ?: return false
            return if (encodedPassword.startsWith("$2a$") || encodedPassword.startsWith("$2b$") || encodedPassword.startsWith("$2y$")) {
                delegate.matches(candidate, encodedPassword)
            } else {
                encodedPassword == candidate
            }
        }
    }

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
    fun filterChain(
        http: HttpSecurity,
        jwtAuthFilter: JwtAuthFilter
    ): SecurityFilterChain {
        val publicSignupPath = URL_SIGNUP.toRequestPath("/api/v1/users/signup")
        val authBasePath = AUTH_BASE.toRequestPath("/api/v1/auth").removeSuffix("/")

        http
            .csrf { it.disable() }
            .cors { it.configurationSource(corsConfigurationSource()) }
            .authorizeHttpRequests {
                it
                    .requestMatchers(publicSignupPath).permitAll()
                    .requestMatchers(
                        "${authBasePath.ensureLeadingSlash()}/login",
                        "${authBasePath.ensureLeadingSlash()}/refresh",
                        "${authBasePath.ensureLeadingSlash()}/logout"
                    ).permitAll()
                    .requestMatchers("/api/v1/unsecure/**").permitAll()
                    .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**", "/webjars/**").permitAll()
                    .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                    .requestMatchers("/").permitAll() // Permitir acceso público al endpoint raíz
                    .anyRequest().permitAll()
            }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter::class.java)

        return http.build()
    }

    private fun String?.toRequestPath(defaultPath: String): String {
        val candidate = this?.takeIf { it.isNotBlank() }?.trim() ?: return defaultPath
        return try {
            val uri = URI(candidate)
            if (uri.scheme.isNullOrBlank()) {
                candidate.ensureLeadingSlash()
            } else {
                val path = uri.path?.takeIf { it.isNotBlank() } ?: "/"
                path.ensureLeadingSlash()
            }
        } catch (_: URISyntaxException) {
            candidate.ensureLeadingSlash()
        } catch (_: IllegalArgumentException) {
            candidate.ensureLeadingSlash()
        }
    }

    private fun String.ensureLeadingSlash(): String =
        if (startsWith("/")) this else "/$this"
}
