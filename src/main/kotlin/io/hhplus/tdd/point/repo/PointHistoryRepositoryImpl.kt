package io.hhplus.tdd.point.repo

import io.hhplus.tdd.database.PointHistoryTable
import io.hhplus.tdd.point.service.dto.dao.PointHistory
import io.hhplus.tdd.point.service.dto.dao.TransactionType
import org.springframework.stereotype.Repository

@Repository
class PointHistoryRepositoryImpl(
    val pointHistoryTable: PointHistoryTable
) : PointHistoryRepository {
    override fun insert(
        id: Long,
        amount: Long,
        transactionType: TransactionType,
        updateMillis: Long,
    ): PointHistory {
        return pointHistoryTable.insert(id, amount, transactionType, updateMillis)
    }

    override fun selectAllByUserId(userId: Long): List<PointHistory> {
        return pointHistoryTable.selectAllByUserId(userId)
    }
}