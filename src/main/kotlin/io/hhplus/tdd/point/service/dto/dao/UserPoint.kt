package io.hhplus.tdd.point.service.dto.dao

import io.hhplus.tdd.exceptions.PointAmountNegativeException
import io.hhplus.tdd.exceptions.PointAmountOverMaxValueException
import io.hhplus.tdd.support.constants.PointConstants.Companion.MAX_POINT_VALUE

data class UserPoint(
    val id: Long,
    val point: Long,
    val updateMillis: Long,
) {
    fun chargePoint(amount: Long): UserPoint {
        if (point + amount > MAX_POINT_VALUE) throw PointAmountOverMaxValueException(this.id)
        return this.copy(point = point + amount)
    }

    fun usePoint(amount: Long): UserPoint {
        if (point - amount < 0) throw PointAmountNegativeException(this.id)
        return this.copy(point = point - amount)
    }
}
