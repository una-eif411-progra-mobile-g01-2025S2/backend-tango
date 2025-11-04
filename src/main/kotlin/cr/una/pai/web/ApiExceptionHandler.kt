package cr.una.pai.web

import org.slf4j.LoggerFactory
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class ApiExceptionHandler {

    private val logger = LoggerFactory.getLogger(ApiExceptionHandler::class.java)

    data class ErrorResponse(val error: String)

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgument(ex: IllegalArgumentException): ResponseEntity<ErrorResponse> {
        logger.debug("Illegal argument received", ex)
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponse(ex.message ?: "Solicitud inválida"))
    }

    @ExceptionHandler(DataIntegrityViolationException::class)
    fun handleDataIntegrity(ex: DataIntegrityViolationException): ResponseEntity<ErrorResponse> {
        logger.debug("Data integrity violation", ex)
        val message = "No se pudo completar la operación porque los datos ya existen o son inválidos"
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(ErrorResponse(message))
    }

    @ExceptionHandler(Exception::class)
    fun handleGeneric(ex: Exception): ResponseEntity<ErrorResponse> {
        logger.error("Unexpected error", ex)
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ErrorResponse("Se produjo un error inesperado"))
    }
}
