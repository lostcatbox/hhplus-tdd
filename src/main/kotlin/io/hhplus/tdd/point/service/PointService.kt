package io.hhplus.tdd.point.service

import io.hhplus.tdd.database.PointHistoryTable
import io.hhplus.tdd.database.UserPointTable
import io.hhplus.tdd.point.service.dto.dao.PointHistory
import io.hhplus.tdd.point.service.dto.dao.TransactionType
import io.hhplus.tdd.point.service.dto.dao.UserPoint
import io.hhplus.tdd.point.service.dto.request.ChargePointRequest
import io.hhplus.tdd.point.service.dto.request.UsePointRequest
import org.springframework.stereotype.Service

@Service
class PointService(
    private val pointHistoryTable: PointHistoryTable,
    private val userPointTable: UserPointTable
) : PointUseCase {
    override fun getUserPointInfo(userId: Long): UserPoint {
        return userPointTable.selectById(userId)
    }

    override fun getUserPointHistories(id: Long): List<PointHistory> {
        return pointHistoryTable.selectAllByUserId(id)
    }
    
    override fun chargePoint(request: ChargePointRequest): UserPoint {
        val userPoint = userPointTable.selectById(request.userId)
        val chargedPoint = userPoint.chargePoint(request.amount)

        val savedUserPoint = userPointTable.insertOrUpdate(chargedPoint.id, chargedPoint.point)

        pointHistoryTable.insert(request.userId, request.amount, TransactionType.CHARGE, savedUserPoint.updateMillis)

        return savedUserPoint
    }
    
    override fun usePoint(request: UsePointRequest): UserPoint {
        val userPoint = userPointTable.selectById(request.userId)
        val chargedPoint = userPoint.usePoint(request.amount)

        val savedUserPoint = userPointTable.insertOrUpdate(chargedPoint.id, chargedPoint.point)

        pointHistoryTable.insert(request.userId, request.amount, TransactionType.USE, savedUserPoint.updateMillis)
        return savedUserPoint
    }
}