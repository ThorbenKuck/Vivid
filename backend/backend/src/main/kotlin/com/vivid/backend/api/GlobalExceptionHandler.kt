package com.vivid.backend.api

import com.vivid.backend.service.exception.ResourceNotFoundException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.authorization.AuthorizationDeniedException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.time.LocalDateTime

private val logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException::class)
    fun handleResourceNotFoundException(ex: ResourceNotFoundException): ResponseEntity<ErrorResponse> {
        val error = ErrorResponse(
            status = HttpStatus.NOT_FOUND.value(),
            message = ex.message ?: "Resource not found",
            timestamp = LocalDateTime.now()
        )
        return ResponseEntity(error, HttpStatus.NOT_FOUND)
    }

    @ExceptionHandler(AuthorizationDeniedException::class)
    fun handleAuthorizationDeniedException(ex: AuthorizationDeniedException): ResponseEntity<ErrorResponse> {
        val error = ErrorResponse(
            status = HttpStatus.FORBIDDEN.value(),
            message = ex.message ?: "Access denied",
            timestamp = LocalDateTime.now()
        )

        return ResponseEntity(error, HttpStatus.FORBIDDEN)
    }

    @ExceptionHandler(Exception::class)
    fun handleGeneralException(ex: Exception): ResponseEntity<ErrorResponse> {
        logger.error("Unexpected error", ex)
        val error = ErrorResponse(
            status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
            message = "An unexpected error occurred",
            timestamp = LocalDateTime.now()
        )
        return ResponseEntity(error, HttpStatus.INTERNAL_SERVER_ERROR)
    }

    @ExceptionHandler(MissingClientRegistrationException::class)
    fun handleMissingRegistration(): ResponseEntity<String> {
        logger.warn("Client registration missing; Client tried to use Vivid API without previous registration")
        return ResponseEntity("Your client is not registered. Please register your client before requesting data from Vivid.", HttpStatus.FORBIDDEN)
    }

    @ExceptionHandler(MissingClientTokenException::class)
    fun handleMissingToken(): ResponseEntity<String> {
        logger.warn("Client token missing; Client tried to use Vivid API without a token")
        return ResponseEntity("You are missing a client token. Please provide a client token.", HttpStatus.FORBIDDEN)
    }
}

data class ErrorResponse(
    val status: Int,
    val message: String,
    val timestamp: LocalDateTime
)

class MissingClientRegistrationException : Exception()

class MissingClientTokenException : Exception()