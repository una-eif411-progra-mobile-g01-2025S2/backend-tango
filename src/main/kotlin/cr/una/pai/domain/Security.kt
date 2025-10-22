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

object SecurityConstants {
    const val TOKEN_TYPE = "JWT"
    const val TOKEN_ISSUER = "PAI Application"
    const val TOKEN_AUDIENCE = "PAI Users"
    const val TOKEN_LIFETIME: Long = 864000000 // 10 días
    const val TOKEN_PREFIX = "Bearer "
    const val APPLICATION_JSON = "application/json"
    const val UTF_8 = "UTF-8"
    const val TOKEN_SECRET: String =
        "======================PAIApplicationPAIApplicationPAIApplicationPAIApplicationPAIApplicationPAIApplication" +
                "PAIApplication==========================="
}

// ====================================================
// 🔐 FILTRO DE AUTENTICACIÓN (LOGIN)
// ====================================================
class JwtAuthenticationFilter(private val authManager: AuthenticationManager) : UsernamePasswordAuthenticationFilter() {

    init {
        setFilterProcessesUrl("/api/v1/users/login")
    }

    @Throws(AuthenticationException::class)
    override fun attemptAuthentication(request: HttpServletRequest, response: HttpServletResponse): Authentication {
        if (request.method != "POST") throw AuthenticationServiceException("Método no soportado: ${request.method}")

        return try {
            val creds = ObjectMapper().readValue(request.inputStream, UserLoginInputSecurity::class.java)
            val authToken = UsernamePasswordAuthenticationToken(creds.email, creds.password, emptyList())
            authManager.authenticate(authToken)
        } catch (ex: IOException) {
            throw RuntimeException(ex)
        }
    }

    override fun successfulAuthentication(
        request: HttpServletRequest, response: HttpServletResponse,
        chain: FilterChain, authentication: Authentication
    ) {
        val token = Jwts.builder()
            .signWith(key(), SignatureAlgorithm.HS512)
            .setHeaderParam("typ", SecurityConstants.TOKEN_TYPE)
            .setIssuer(SecurityConstants.TOKEN_ISSUER)
            .setAudience(SecurityConstants.TOKEN_AUDIENCE)
            .setSubject(authentication.name)
            .setExpiration(Date(System.currentTimeMillis() + SecurityConstants.TOKEN_LIFETIME))
            .compact()

        response.addHeader(HttpHeaders.AUTHORIZATION, SecurityConstants.TOKEN_PREFIX + token)
        response.addHeader("Access-Control-Expose-Headers", HttpHeaders.AUTHORIZATION)

        val body = mapOf(
            "email" to authentication.name,
            "token" to "${SecurityConstants.TOKEN_PREFIX}$token"
        )

        response.contentType = SecurityConstants.APPLICATION_JSON
        response.characterEncoding = SecurityConstants.UTF_8
        response.writer.print(ObjectMapper().writeValueAsString(body))
        response.writer.flush()

        println("✅ TOKEN GENERADO: Bearer $token")
    }

    private fun key(): Key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(SecurityConstants.TOKEN_SECRET))
}

// ====================================================
// 🔒 FILTRO DE AUTORIZACIÓN (VALIDA TOKEN EN REQUESTS)
// ====================================================
class JwtAuthorizationFilter(authManager: AuthenticationManager) : BasicAuthenticationFilter(authManager) {
    override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, chain: FilterChain) {
        val header = request.getHeader(HttpHeaders.AUTHORIZATION)
        if (header == null || !header.startsWith(SecurityConstants.TOKEN_PREFIX)) {
            chain.doFilter(request, response)
            return
        }

        val token = header.replace(SecurityConstants.TOKEN_PREFIX, "")
        val username = Jwts.parserBuilder()
            .setSigningKey(Keys.hmacShaKeyFor(Decoders.BASE64.decode(SecurityConstants.TOKEN_SECRET)))
            .build()
            .parseClaimsJws(token)
            .body
            .subject

        if (username != null) {
            val auth = UsernamePasswordAuthenticationToken(username, null, emptyList())
            SecurityContextHolder.getContext().authentication = auth
        }

        chain.doFilter(request, response)
    }
}
