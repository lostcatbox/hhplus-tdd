package io.hhplus.tdd.point.repo

import io.hhplus.tdd.point.service.dto.dao.PointHistory
import io.hhplus.tdd.point.service.dto.dao.TransactionType

interface PointHistoryRepository {
    fun insert(
        id: Long,
        amount: Long,
        transactionType: TransactionType,
        updateMillis: Long,
    ): PointHistory

    fun selectAllByUserId(userId: Long): List<PointHistory>
}