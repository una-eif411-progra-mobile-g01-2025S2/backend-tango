package cr.una.pai.security

import io.jsonwebtoken.JwtException
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthFilter(
    private val jwtService: JwtService,
    private val userDetailsService: UserDetailsService
) : OncePerRequestFilter() {

    private val logger = LoggerFactory.getLogger(javaClass)

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val header = request.getHeader(HttpHeaders.AUTHORIZATION)
        if (header.isNullOrBlank() || !header.startsWith(BEARER_PREFIX)) {
            filterChain.doFilter(request, response)
            return
        }

        val token = header.removePrefix(BEARER_PREFIX).trim()
        try {
            val claims = jwtService.parseAndValidate(token)
            val tokenType = claims[CLAIM_TYPE] as? String
            if (tokenType != jwtService.accessTokenType()) {
                filterChain.doFilter(request, response)
                return
            }

            if (SecurityContextHolder.getContext().authentication == null) {
                val email = claims[CLAIM_EMAIL] as? String
                if (!email.isNullOrBlank()) {
                    val userDetails = userDetailsService.loadUserByUsername(email)
                    val authentication = UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.authorities
                    ).apply {
                        details = WebAuthenticationDetailsSource().buildDetails(request)
                    }
                    SecurityContextHolder.getContext().authentication = authentication
                }
            }
        } catch (ex: JwtException) {
            logger.debug("Invalid JWT received: ${ex.message}")
        } catch (ex: IllegalArgumentException) {
            logger.debug("Invalid JWT payload: ${ex.message}")
        }

        filterChain.doFilter(request, response)
    }

    companion object {
        private const val BEARER_PREFIX = "Bearer "
        private const val CLAIM_TYPE = "typ"
        private const val CLAIM_EMAIL = "email"
    }
}
