package io.hhplus.tdd.point.service.dto.request

import io.hhplus.tdd.support.constants.PointConstants.Companion.MAX_POINT_VALUE
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class ChargePointRequestTest {


    @Test
    fun `ChargePointRequest 생성 - 유효한 파라미터로 객체 생성 성공`() {
        // given
        val userId = 1L
        val amount = 1000L

        // when
        val request = ChargePointRequest(userId, amount)

        // then
        Assertions.assertEquals(userId, request.userId)
        Assertions.assertEquals(amount, request.amount)
    }

    @Test
    fun `ChargePointRequest 생성 - 음수 사용자 ID로 생성 시 예외 발생`() {
        // given
        val userId = -1L
        val amount = 1000L

        // when & then
        val exception = Assertions.assertThrows(IllegalArgumentException::class.java) {
            ChargePointRequest(userId, amount)
        }

        Assertions.assertEquals("사용자 ID는 0 이상이어야 합니다", exception.message)
    }

    @Test
    fun `ChargePointRequest 생성 - 0 사용자 ID는 허용`() {
        // given
        val userId = 0L
        val amount = 1000L

        // when
        val request = ChargePointRequest(userId, amount)

        // then
        Assertions.assertEquals(userId, request.userId)
        Assertions.assertEquals(amount, request.amount)
    }

    @ParameterizedTest
    @ValueSource(longs = [0, -1])
    fun `ChargePointRequest 생성 - 0 또는 음수 금액으로 생성 시 예외 발생`(invalidAmount: Long) {
        // given
        val userId = 1L

        // when & then
        val exception = Assertions.assertThrows(IllegalArgumentException::class.java) {
            ChargePointRequest(userId, invalidAmount)
        }

        Assertions.assertEquals("금액은 양수여야 합니다", exception.message)
    }

    @ParameterizedTest
    @ValueSource(longs = [MAX_POINT_VALUE + 1])
    fun `ChargePointRequest 생성 - 충전 최대값 MAX_POINT_VALUE 초과한 금액으로 생성 시 예외 발생`(invalidAmount: Long) {
        // given
        val userId = 1L

        // when & then
        val exception = Assertions.assertThrows(IllegalArgumentException::class.java) {
            ChargePointRequest(userId, invalidAmount)
        }

        Assertions.assertEquals("충전 금액은 최대 충전 금액인 100000000 를 넘을 수 없습니다.", exception.message)
    }
}