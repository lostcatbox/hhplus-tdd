package io.hhplus.tdd.point.service

import io.hhplus.tdd.database.PointHistoryTable
import io.hhplus.tdd.database.UserPointTable
import io.hhplus.tdd.point.service.dto.dao.TransactionType
import io.hhplus.tdd.point.service.dto.dao.UserPoint
import io.hhplus.tdd.point.service.dto.request.ChargePointRequest
import io.hhplus.tdd.point.service.dto.request.UsePointRequest
import io.hhplus.tdd.support.utils.DateUtils
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class PointServiceTest {

    private lateinit var pointService: PointService
    private val pointHistoryTable: PointHistoryTable = mockk(relaxed = true)
    private val userPointTable: UserPointTable = mockk(relaxed = true)

    private val validUserId = 1L
    private val validAmount = 1000L
    private val currentTimeMillis = DateUtils.nowDateTimeByMilli()
    private val mockUserPoint = UserPoint(validUserId, validAmount, currentTimeMillis)
    private val updatedUserPoint = UserPoint(validUserId, validAmount * 2, currentTimeMillis)

    @BeforeEach
    fun setUp() {
        // 각 테스트 전에 모든 mock 동작을 초기화
        clearAllMocks()
        pointService = PointService(pointHistoryTable, userPointTable)
    }

    @Test
    fun `chargePoint - 유효한 요청으로 포인트 충전 성공`() {
        // given - 포인트 충전 요청 및 DB 응답 설정
        val request = ChargePointRequest(validUserId, validAmount)
        every { userPointTable.insertOrUpdate(any(), any()) } returns updatedUserPoint
        every { userPointTable.selectById(any()) } returns mockUserPoint

        // when - 포인트 충전 서비스 호출
        val result = pointService.chargePoint(request)

        // then - 결과 및 각 메소드 호출 검증
        assertEquals(updatedUserPoint, result)
        verify(exactly = 1) { userPointTable.selectById(validUserId) }
        verify(exactly = 1) { userPointTable.insertOrUpdate(validUserId, validAmount * 2) }
        verify(exactly = 1) { pointHistoryTable.insert(validUserId, validAmount, TransactionType.CHARGE, any()) }
    }

    @Test
    fun `usePoint - 유효한 요청으로 포인트 사용 성공`() {
        // given - 포인트 사용 요청 및 차감된 포인트 DB 응답 설정
        val request = UsePointRequest(validUserId, validAmount)
        val updatedPoint = mockUserPoint.point - validAmount
        val usedUserPoint = UserPoint(validUserId, updatedPoint, currentTimeMillis)
        every { userPointTable.insertOrUpdate(any(), any()) } returns usedUserPoint
        every { userPointTable.selectById(any()) } returns mockUserPoint

        // when - 포인트 사용 서비스 호출
        val result = pointService.usePoint(request)

        // then - 결과 및 각 메소드 호출 검증
        assertEquals(usedUserPoint, result)
        verify(exactly = 1) { userPointTable.selectById(validUserId) }
        verify(exactly = 1) { userPointTable.insertOrUpdate(validUserId, updatedPoint) }
        verify(exactly = 1) { pointHistoryTable.insert(validUserId, validAmount, TransactionType.USE, any()) }
    }
} 