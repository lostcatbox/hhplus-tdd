package io.hhplus.tdd.point.service

import io.hhplus.tdd.database.PointHistoryTable
import io.hhplus.tdd.database.UserPointTable
import io.hhplus.tdd.exceptions.PointAmountNegativeException
import io.hhplus.tdd.point.service.dto.dao.TransactionType
import io.hhplus.tdd.point.service.dto.request.ChargePointRequest
import io.hhplus.tdd.point.service.dto.request.UsePointRequest
import io.hhplus.tdd.support.utils.DateUtils
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

// TODO : 통합테스트 시에, 매번 테스트 시 빈 초기화가 필요하다. userPointTable, pointHistoryTable를 비울수있는 함수가없다. -> 어떻게 해야할까? -> 현재는 userId를 테스트마다 유일하게 한다.
//  현재 조건에서 -> 다른 좋은 방법은?
@SpringBootTest
class PointServiceIntegrationTest {

    private val validUserId = 1L
    private val validAmount = 1000L

    @Autowired
    private lateinit var pointService: PointService

    @Autowired
    private lateinit var userPointTable: UserPointTable

    @Autowired
    private lateinit var pointHistoryTable: PointHistoryTable

    @BeforeEach
    fun setUp() {
        //initValidUserPoint = 0
        userPointTable.insertOrUpdate(validUserId, 0L)
    }

    // TODO : 간단한 성공 통합테스트 정도는 필요한가?
    @Test
    fun `getUserPointInfo - 유저의 현재 포인트 조회 성공`() {
        // given
        userPointTable.insertOrUpdate(validUserId, validAmount)

        //when
        pointService.getUserPointInfo(validUserId)

        //then
        val result = userPointTable.selectById(validUserId)

        assertEquals(validUserId, result.id)
        assertEquals(validAmount, result.point)
    }

    // TODO : 간단한 성공 통합테스트 정도는 필요한가?
    @Test
    fun `getUserPointHistories - 유저의 현재 포인트 이력 리스트 조회 성공`() {
        // given

        val expectedAmount = 9999999L
        pointHistoryTable.insert(
            validUserId,
            expectedAmount,
            TransactionType.CHARGE,
            DateUtils.nowDateTimeByMilli()
        )

        // when
        val resultList = pointService.getUserPointHistories(validUserId)

        // then
        assertTrue(resultList.any { history ->
            history.userId == validUserId &&
                    history.amount == expectedAmount &&
                    history.type == TransactionType.CHARGE
        })
    }


    @Test
    fun `chargePoint - 100개의 동시 1000L 포인트 충전 요청 후 100000L 포인트 조회 성공`() {
        // given
        val testUserId = validUserId + 1

        val request = ChargePointRequest(testUserId, validAmount)
        val threadCount = 100
        val executorService = Executors.newFixedThreadPool(threadCount)
        val latch = CountDownLatch(threadCount)
        val expectedTotalAmount = validAmount * threadCount

        // when - 100개의 스레드에서 동시에 포인트 충전 요청
        repeat(threadCount) {
            executorService.submit {
                try {
                    pointService.chargePoint(request)
                } finally {
                    latch.countDown()
                }
            }
        }

        // 모든 요청이 완료될 때까지 대기
        latch.await(1000, TimeUnit.SECONDS)
        executorService.shutdown()

        // then - 최종 포인트 잔액 확인
        val finalPoint = pointService.getUserPointInfo(testUserId)
        assertEquals(expectedTotalAmount, finalPoint.point)


    }

    @Test
    fun `usePoint - 1000L 포인트에서 100개의 동시 1L 포인트 사용 요청 시 결과 900L포인트 조회 성공`() {
        // given - 포인트 사용 요청 및 차감된 포인트 DB 응답 설정
        val testUserId = validUserId + 2
        val chargePointRequest = ChargePointRequest(testUserId, validAmount)
        pointService.chargePoint(chargePointRequest)

        val USE_POINT = 1L

        val threadCount = 100
        val executorService = Executors.newFixedThreadPool(threadCount)
        val latch = CountDownLatch(threadCount)
        val expectedTotalAmount = validAmount - USE_POINT * threadCount


        // when - 100개의 스레드에서 동시에 포인트 사용 요청
        val usePointRequest = UsePointRequest(testUserId, USE_POINT)

        repeat(threadCount) {
            executorService.submit {
                try {
                    pointService.usePoint(usePointRequest)
                } finally {
                    latch.countDown()
                }
            }
        }

        // 모든 요청이 완료될 때까지 대기
        latch.await(1000, TimeUnit.SECONDS)
        executorService.shutdown()

        // then - 최종 포인트 잔액 확인
        val finalPoint = pointService.getUserPointInfo(testUserId)
        assertEquals(expectedTotalAmount, finalPoint.point)

        // then - 최종 충전 히스토리 확인
        val userPointHistories = pointService.getUserPointHistories(testUserId)
        assertEquals(threadCount + 1, userPointHistories.size)

    }

    @Test
    // TODO : 통합테스트 중에 given 설정시 pointService.chargePoint()메서드를 사용했는데, 통합테스트 대상을 given에 사용해도되나?
    fun `usePoint - 1000L 포인트에서 100개의 동시 100L 포인트 사용 요청 시 결과 10개의 요청 처리 성공 및 90개의 요청은 PointAmountNegativeException 실패 확인`() {
        // given
        val testUserId = validUserId + 3

        val chargePointRequest = ChargePointRequest(testUserId, validAmount)
        pointService.chargePoint(chargePointRequest)

        val USE_POINT = 100L
        val threadCount = 100
        val executorService = Executors.newFixedThreadPool(threadCount)
        val latch = CountDownLatch(threadCount)

        // 성공/실패 카운터
        val successCount = AtomicInteger(0)
        val failCount = AtomicInteger(0)

        // when
        val usePointRequest = UsePointRequest(testUserId, USE_POINT)

        repeat(threadCount) {
            executorService.submit {
                try {
                    pointService.usePoint(usePointRequest)
                    successCount.incrementAndGet()
                } catch (e: PointAmountNegativeException) {
                    failCount.incrementAndGet()
                } finally {
                    latch.countDown()
                }
            }
        }

        // 모든 요청이 완료될 때까지 대기
        latch.await(10, TimeUnit.SECONDS)
        executorService.shutdown()

        // then
        val finalPoint = pointService.getUserPointInfo(testUserId)

        // 성공/실패 횟수 검증
        assertEquals(10, successCount.get(), "성공한 요청 수가 10개여야 합니다")
        assertEquals(90, failCount.get(), "실패한 요청 수가 90개여야 합니다")

        // 최종 포인트 잔액 확인 (1000 - (100 * 10) = 0)
        assertEquals(0, finalPoint.point)

        // then - 최종  히스토리 확인
        val userPointHistories = pointService.getUserPointHistories(testUserId)
        assertEquals(10 + 1, userPointHistories.size)
    }

    @Test
    fun `chargePoint + usePoint - 1000L 포인트에서 100개의 동시 1000L 포인트 충전 요청 + 100개의 동시 10L 포인트 사용 요청 및 100000+1000-1000 결과 조회 성공`() {
        // given
        val testUserId = validUserId + 4

        val charge = ChargePointRequest(testUserId, validAmount)
        pointService.chargePoint(charge)

        val CHARGE_POINT = 1000L
        val USE_POINT = 10L
        val threadCount = 200
        val executorService = Executors.newFixedThreadPool(threadCount)
        val latch = CountDownLatch(threadCount)

        // 성공/실패 카운터
        val successCount = AtomicInteger(0)
        val failCount = AtomicInteger(0)

        // when
        val usePointRequest = UsePointRequest(testUserId, USE_POINT)
        val chargePointRequest = ChargePointRequest(testUserId, CHARGE_POINT)

        repeat(100) {
            executorService.submit {
                try {
                    pointService.usePoint(usePointRequest)
                    successCount.incrementAndGet()
                } catch (e: PointAmountNegativeException) {
                    failCount.incrementAndGet()
                } finally {
                    latch.countDown()
                }
            }
        }
        repeat(100) {
            executorService.submit {
                try {
                    pointService.chargePoint(chargePointRequest)
                    successCount.incrementAndGet()
                } catch (e: PointAmountNegativeException) {
                    failCount.incrementAndGet()
                } finally {
                    latch.countDown()
                }
            }
        }

        // 모든 요청이 완료될 때까지 대기
        latch.await(10, TimeUnit.SECONDS)
        executorService.shutdown()

        // then
        val finalPoint = pointService.getUserPointInfo(testUserId)

        // 성공/실패 횟수 검증

        // 최종 포인트 잔액 확인 100000+1000-1000
        assertEquals(100000, finalPoint.point)
        // then - 최종  히스토리 확인
        val userPointHistories = pointService.getUserPointHistories(testUserId)
        assertEquals(100 + 100 + 1, userPointHistories.size)

    }

} 