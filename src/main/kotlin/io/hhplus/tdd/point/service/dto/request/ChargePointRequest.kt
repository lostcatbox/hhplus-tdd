package io.hhplus.tdd.point.service.dto.request

data class ChargePointRequest(
    val userId: Long,
    val amount: Long
) {
    init {
        require(userId >= 0) { "사용자 ID는 0 이상이어야 합니다" }
        require(amount > 0) { "금액은 양수여야 합니다" }
    }
} 