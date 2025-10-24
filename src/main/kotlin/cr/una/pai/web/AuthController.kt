package cr.una.pai.web

import cr.una.pai.dto.AuthTokensResponse
import cr.una.pai.dto.LoginRequest
import cr.una.pai.dto.RefreshTokenRequest
import cr.una.pai.service.AuthService
import cr.una.pai.service.AuthService.InvalidCredentialsException
import cr.una.pai.service.AuthService.InvalidRefreshTokenException
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("\${api.endpoints.auth}")
@CrossOrigin(origins = ["\${spring.web.cors.allowed-origins}"])
@Validated
class AuthController(
    private val authService: AuthService
) {

    @PostMapping("/login")
    fun login(@Valid @RequestBody request: LoginRequest): ResponseEntity<AuthTokensResponse> {
        val tokens = authService.login(request)
        return ResponseEntity.ok()
            .header(REFRESH_TOKEN_HEADER, tokens.refreshToken)
            .body(tokens)
    }

    @PostMapping("/refresh")
    fun refresh(@Valid @RequestBody request: RefreshTokenRequest): ResponseEntity<AuthTokensResponse> {
        val tokens = authService.refresh(request)
        return ResponseEntity.ok()
            .header(REFRESH_TOKEN_HEADER, tokens.refreshToken)
            .body(tokens)
    }

    @PostMapping("/logout")
    fun logout(@Valid @RequestBody request: RefreshTokenRequest): ResponseEntity<Void> {
        authService.logout(request)
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build()
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(InvalidCredentialsException::class)
    fun handleInvalidCredentials(ex: InvalidCredentialsException): ResponseEntity<Map<String, String?>> =
        ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(mapOf("error" to ex.message))

    @org.springframework.web.bind.annotation.ExceptionHandler(InvalidRefreshTokenException::class)
    fun handleInvalidRefresh(ex: InvalidRefreshTokenException): ResponseEntity<Map<String, String?>> =
        ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(mapOf("error" to ex.message))
}

private const val REFRESH_TOKEN_HEADER = "X-Refresh-Token"
