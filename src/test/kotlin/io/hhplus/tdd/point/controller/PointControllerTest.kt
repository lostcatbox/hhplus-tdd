package io.hhplus.tdd.point

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import io.hhplus.tdd.point.controller.PointController
import io.hhplus.tdd.point.controller.dto.request.PointRequest
import io.hhplus.tdd.point.service.PointUseCase
import io.hhplus.tdd.point.service.dto.dao.PointHistory
import io.hhplus.tdd.point.service.dto.dao.TransactionType
import io.hhplus.tdd.point.service.dto.dao.UserPoint
import io.hhplus.tdd.support.utils.DateUtils
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
class ValidationExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleMethodArgumentNotValidException(e: MethodArgumentNotValidException): ResponseEntity<String> {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(e.bindingResult.allErrors.firstOrNull()?.defaultMessage)
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(e: IllegalArgumentException): ResponseEntity<String> {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.message)
    }
}

class PointControllerTest {

    private lateinit var mockMvc: MockMvc
    private val pointUseCase: PointUseCase = mockk()
    private lateinit var pointController: PointController
    private val objectMapper = ObjectMapper().registerModule(KotlinModule.Builder().build())

    private val validUserId = 1L
    private val validAmount = 1000L
    private val mockUserPoint = UserPoint(validUserId, validAmount, DateUtils.nowDateTimeByMilli())

    @BeforeEach
    fun setUp() {
        pointController = PointController(pointUseCase)
        mockMvc = MockMvcBuilders
            .standaloneSetup(pointController)
            .setControllerAdvice(ValidationExceptionHandler())
            .build()
    }

    @Test
    fun `유효한 ID로 포인트 조회시 유저 포인트 정보를 반환한다`() {
        every { pointUseCase.getUserPointInfo(validUserId) } returns mockUserPoint

        mockMvc.perform(get("/point/$validUserId"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(validUserId))
            .andExpect(jsonPath("$.point").value(validAmount))
    }

    @Test
    fun `유효한 ID와 금액으로 포인트 충전시 업데이트된 유저 포인트 정보를 반환한다`() {
        val request = PointRequest(amount = validAmount)
        every { pointUseCase.chargePoint(any()) } returns mockUserPoint

        mockMvc.perform(
            patch("/point/$validUserId/charge")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(validUserId))
            .andExpect(jsonPath("$.point").value(validAmount))
    }

    @Test
    fun `유효한 ID와 금액으로 포인트 사용시 업데이트된 유저 포인트 정보를 반환한다`() {
        val request = PointRequest(amount = validAmount)
        every { pointUseCase.usePoint(any()) } returns mockUserPoint

        mockMvc.perform(
            patch("/point/$validUserId/use")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(validUserId))
            .andExpect(jsonPath("$.point").value(validAmount))
    }

    @Test
    fun `유효한 ID로 히스토리 조회시 포인트 내역을 반환한다`() {
        val mockHistories = listOf(
            PointHistory(1, validUserId, TransactionType.CHARGE, validAmount, DateUtils.nowDateTimeByMilli())
        )

        every { pointUseCase.getUserPointHistories(validUserId) } returns mockHistories

        mockMvc.perform(get("/point/$validUserId/histories"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[0].userId").value(validUserId))
            .andExpect(jsonPath("$[0].amount").value(validAmount))
    }

    @Test
    fun `포인트 충전시 금액이 null이면 400 에러를 반환한다`() {
        mockMvc.perform(
            patch("/point/$validUserId/charge")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}")
        )  // 빈 JSON 객체 (amount 없음)
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `포인트 사용시 금액이 null이면 400 에러를 반환한다`() {
        mockMvc.perform(
            patch("/point/$validUserId/use")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}")
        )  // 빈 JSON 객체 (amount 없음)
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `포인트 충전시 금액이 음수이면 400 에러를 반환한다`() {
        val request = PointRequest(amount = -1000L)

        mockMvc.perform(
            patch("/point/$validUserId/charge")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `포인트 사용시 금액이 음수이면 400 에러를 반환한다`() {
        val request = PointRequest(amount = -1000L)

        mockMvc.perform(
            patch("/point/$validUserId/use")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isBadRequest)
    }
} 