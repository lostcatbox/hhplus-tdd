package io.hhplus.tdd.point.service.dto.dao

import io.hhplus.tdd.exceptions.PointAmountNegativeException
import io.hhplus.tdd.exceptions.PointAmountOverMaxValueException
import io.hhplus.tdd.support.constants.PointConstants.Companion.MAX_POINT_VALUE
import io.hhplus.tdd.support.utils.DateUtils
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class UserPointTest {


    private val userPoint = UserPoint(
        id = 1L,
        point = 10000L,
        updateMillis = DateUtils.nowDateTimeByMilli()
    )

    @Test
    fun `UserPoint 충전 시 새로운 객체 반환 테스트`() {
        // given
        val chargingPoint = 0L

        //when
        val chargePoint = userPoint.chargePoint(chargingPoint)

        //then
        assertFalse(userPoint === chargePoint) // 객체의 메모리 주소값을 비교한다.
        assertEquals(userPoint, chargePoint) //equals 호출된다. data class 이므로 자동으로 재정의되어있음
    }

    @Test
    fun `UserPoint 충전 후 기존 포인트와 충전포인트 합해진 객체 반환 성공테스트`() {
        // given
        val chargingPoint = 1000L


        //when
        val chargePoint = userPoint.chargePoint(chargingPoint)

        //then
        assertEquals(userPoint.id, chargePoint.id)
        assertEquals(userPoint.point + chargingPoint, chargePoint.point)
    }

    @Test
    fun `UserPoint 사용 시 기존금액 - 사용금액에 대한 객체 반환 성공 테스트`() {
        // given
        val usePoint = 1000L


        //when
        val chargePoint = userPoint.usePoint(usePoint)

        //then
        assertEquals(userPoint.id, chargePoint.id)
        assertEquals(userPoint.point - usePoint, chargePoint.point)
    }

    @Test
    fun `UserPoint 사용 시 기존금액 - 사용금액 = 0보다 작은결과에 대한 객체 반환 실패 테스트`() {
        val usePoint = 100000L
        // given


        // when & then
        assertThrows(PointAmountNegativeException::class.java) {
            userPoint.usePoint(usePoint)
        }
    }

    @Test
    fun `UserPoint 사용 시 기존금액 + 사용금액 = MAX_POINT_VALUE 보다 큰결과에 대한 객체 반환 실패 테스트`() {
        // given
        val maxPoint = MAX_POINT_VALUE


        // when & then
        assertThrows(PointAmountOverMaxValueException::class.java) {
            userPoint.chargePoint(maxPoint)
        }
    }
}