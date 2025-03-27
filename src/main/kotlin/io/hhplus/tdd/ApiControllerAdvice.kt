package io.hhplus.tdd

import io.hhplus.tdd.exceptions.PointAmountNegativeException
import io.hhplus.tdd.exceptions.PointAmountOverMaxValueException
import io.hhplus.tdd.support.constants.PointConstants.Companion.MAX_POINT_VALUE
import jakarta.validation.ConstraintViolationException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

data class ErrorResponse(val code: String, val message: String)

@RestControllerAdvice
class ApiControllerAdvice : ResponseEntityExceptionHandler() {
    private val logger: Logger = LoggerFactory.getLogger(javaClass)

    @ExceptionHandler(PointAmountNegativeException::class)
    fun handlePointAmountNegativeException(e: PointAmountNegativeException): ResponseEntity<ErrorResponse> {
        return ResponseEntity(
            ErrorResponse("400", "UserPoint.point는 point 사용 후 음수가 될 수 없습니다. fail UserPoint.id : ${e.id}"),
            HttpStatus.BAD_REQUEST,
        )
    }

    @ExceptionHandler(PointAmountOverMaxValueException::class)
    fun handlePointAmountOverMaxValueException(e: PointAmountOverMaxValueException): ResponseEntity<ErrorResponse> {
        return ResponseEntity(
            ErrorResponse("400", "UserPoint.point의 합은 ${MAX_POINT_VALUE} 를 넘을 수 없습니다. fail UserPoint.id : ${e.id}"),
            HttpStatus.BAD_REQUEST,
        )
    }

    @ExceptionHandler(ConstraintViolationException::class)
    fun handleValidationException(e: ConstraintViolationException): ResponseEntity<ErrorResponse> {
        return ResponseEntity(
            ErrorResponse("400", "${e.message}"),
            HttpStatus.BAD_REQUEST,
        )
    }

    @ExceptionHandler(Exception::class)
    fun handleException(e: Exception): ResponseEntity<ErrorResponse> {
        return ResponseEntity(
            ErrorResponse("500", "에러가 발생했습니다."),
            HttpStatus.INTERNAL_SERVER_ERROR,
        )
    }

}