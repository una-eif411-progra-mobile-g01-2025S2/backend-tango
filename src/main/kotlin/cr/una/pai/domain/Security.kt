package cr.una.pai.domain

import com.fasterxml.jackson.databind.ObjectMapper
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpHeaders
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.AuthenticationServiceException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.AuthenticationException
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter
import cr.una.pai.dto.UserLoginInputSecurity
import java.io.IOException
import java.security.Key
import java.util.*
import cr.una.pai.dto.UserResult

// ====================== Constantes ======================
object SecurityConstants {
    const val TOKEN_TYPE = "JWT"
    const val TOKEN_ISSUER = "secure-api"
    const val TOKEN_AUDIENCE = "secure-app"
    const val TOKEN_LIFETIME: Long = 864000000 // 10 días
    const val TOKEN_PREFIX = "Bearer "
    const val APPLICATION_JSON = "application/json"
    const val UTF_8 = "UTF-8"
    const val TOKEN_SECRET: String =
        "======================MikeEducationMikeEducationMikeEducationMikeEducationMikeEducationMikeEducation" +
                "MikeEducation==========================="
}

// ====================== Authentication Filter ======================
class JwtAuthenticationFilter(authenticationManager: AuthenticationManager) : UsernamePasswordAuthenticationFilter() {

    private val authManager: AuthenticationManager = authenticationManager

    init {
        setFilterProcessesUrl("/v1/users/login") // URL de login
    }

    @Throws(AuthenticationException::class)
    override fun attemptAuthentication(
        request: HttpServletRequest,
        response: HttpServletResponse
    ): Authentication {

        if (request.method != "POST") {
            throw AuthenticationServiceException("Authentication method not supported: ${request.method}")
        }

        return try {
            // Leemos el DTO de login con email y password
            val userLoginInput = ObjectMapper()
                .readValue(request.inputStream, UserLoginInputSecurity::class.java)

            val authToken = UsernamePasswordAuthenticationToken(
                userLoginInput.email,
                userLoginInput.password,
                ArrayList() // sin roles todavía, Spring los cargará después si es necesario
            )
            authManager.authenticate(authToken)

        } catch (ex: IOException) {
            throw RuntimeException(ex)
        }
    }

    override fun successfulAuthentication(
        request: HttpServletRequest, response: HttpServletResponse,
        filterChain: FilterChain, authentication: Authentication
    ) {

        val objectMapper = ObjectMapper()

        // Creamos el token JWT
        val token = Jwts.builder()
            .signWith(key(), SignatureAlgorithm.HS512)
            .setHeaderParam("typ", SecurityConstants.TOKEN_TYPE)
            .setIssuer(SecurityConstants.TOKEN_ISSUER)
            .setAudience(SecurityConstants.TOKEN_AUDIENCE)
            .setSubject(authentication.name) // email del usuario
            .setExpiration(Date(System.currentTimeMillis() + SecurityConstants.TOKEN_LIFETIME))
            .compact()

        // Agregamos el token en la cabecera
        response.addHeader(HttpHeaders.AUTHORIZATION, SecurityConstants.TOKEN_PREFIX + token)

        // También devolvemos el usuario logueado en el body si quieres
        val principal = UserResult(
            id = UUID.randomUUID(), // aquí podrías mapear tu User real
            fullName = authentication.name,
            email = authentication.name,
            degree = null,
            yearOfStudy = null,
            university = null,
            roles = emptyList(),
            roleIds = emptyList()
        )

        response.contentType = SecurityConstants.APPLICATION_JSON
        response.characterEncoding = SecurityConstants.UTF_8
        response.writer.print(objectMapper.writeValueAsString(principal))
        response.writer.flush()
    }
}

// ====================== Authorization Filter ======================
class JwtAuthorizationFilter(authenticationManager: AuthenticationManager) :
    BasicAuthenticationFilter(authenticationManager) {

    @Throws(IOException::class)
    override fun doFilterInternal(
        request: HttpServletRequest, response: HttpServletResponse,
        filterChain: FilterChain
    ) {

        var authorizationToken = request.getHeader(HttpHeaders.AUTHORIZATION)

        if (authorizationToken != null && authorizationToken.startsWith(SecurityConstants.TOKEN_PREFIX)) {
            authorizationToken = authorizationToken.replaceFirst(SecurityConstants.TOKEN_PREFIX.toRegex(), "")
            val email: String =
                Jwts.parserBuilder().setSigningKey(key()).build().parseClaimsJws(authorizationToken).body.subject

            LoggedUser.logIn(email)

            SecurityContextHolder.getContext().authentication =
                UsernamePasswordAuthenticationToken(email, null, emptyList())
        }

        filterChain.doFilter(request, response)
    }
}

// ====================== Helper para el JWT ======================
private fun key(): Key {
    return Keys.hmacShaKeyFor(Decoders.BASE64.decode(SecurityConstants.TOKEN_SECRET))
}

// ====================== Holder del usuario logueado ======================
object LoggedUser {
    private val userHolder = ThreadLocal<String>()
    fun logIn(user: String) {
        userHolder.set(user)
    }

    fun logOut() {
        userHolder.remove()
    }

    fun get(): String? {
        return userHolder.get()
    }
}
