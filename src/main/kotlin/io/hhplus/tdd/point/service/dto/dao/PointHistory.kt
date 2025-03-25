package io.hhplus.tdd.point.service.dto.dao

data class PointHistory(
    val id: Long,
    val userId: Long,
    val type: TransactionType,
    val amount: Long,
    val timeMillis: Long,
) {
    constructor(userPoint: UserPoint, type: TransactionType, timeMillis: Long) : this(
        id = userPoint.id,
        userId = userPoint.id,
        amount = userPoint.point,
        type = type,
        timeMillis = timeMillis
    )
}

/**
 * 포인트 트랜잭션 종류
 * - CHARGE : 충전
 * - USE : 사용
 */
enum class TransactionType {
    CHARGE, USE
}