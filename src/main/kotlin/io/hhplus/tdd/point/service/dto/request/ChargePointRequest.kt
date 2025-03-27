package io.hhplus.tdd.point.service.dto.request

import io.hhplus.tdd.support.constants.PointConstants.Companion.MAX_POINT_VALUE

data class ChargePointRequest(
    val userId: Long,
    val amount: Long
) {
    init {
        require(userId >= 0) { "사용자 ID는 0 이상이어야 합니다" }
        require(amount > 0) { "금액은 양수여야 합니다" }
        require(amount <= MAX_POINT_VALUE) { "충전 금액은 최대 충전 금액인 ${MAX_POINT_VALUE} 를 넘을 수 없습니다." }
    }
} 