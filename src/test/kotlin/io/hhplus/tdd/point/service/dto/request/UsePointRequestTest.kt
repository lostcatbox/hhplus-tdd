package io.hhplus.tdd.point.service.dto.request

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class UsePointRequestTest {

    @Test
    fun `UsePointRequest 생성 - 유효한 파라미터로 객체 생성 성공`() {
        // given
        val userId = 1L
        val amount = 1000L

        // when
        val request = UsePointRequest(userId, amount)

        // then
        Assertions.assertEquals(userId, request.userId)
        Assertions.assertEquals(amount, request.amount)
    }

    @Test
    fun `UsePointRequest 생성 - 음수 사용자 ID로 생성 시 예외 발생`() {
        // given
        val userId = -1L
        val amount = 1000L

        // when & then
        val exception = Assertions.assertThrows(IllegalArgumentException::class.java) {
            UsePointRequest(userId, amount)
        }

        Assertions.assertEquals("사용자 ID는 0 이상이어야 합니다", exception.message)
    }

    @Test
    fun `UsePointRequest 생성 - 0 사용자 ID는 허용`() {
        // given
        val userId = 0L
        val amount = 1000L

        // when
        val request = UsePointRequest(userId, amount)

        // then
        Assertions.assertEquals(userId, request.userId)
        Assertions.assertEquals(amount, request.amount)
    }

    @ParameterizedTest
    @ValueSource(longs = [0, -1])
    fun `UsePointRequest 생성 - 0 또는 음수 금액으로 생성 시 예외 발생`(invalidAmount: Long) {
        // given
        val userId = 1L

        // when & then
        val exception = Assertions.assertThrows(IllegalArgumentException::class.java) {
            UsePointRequest(userId, invalidAmount)
        }

        Assertions.assertEquals("금액은 양수여야 합니다", exception.message)
    }

}