package io.hhplus.tdd.point.service

import io.hhplus.tdd.point.repo.PointHistoryRepository
import io.hhplus.tdd.point.repo.UserPointRepository
import io.hhplus.tdd.point.service.dto.dao.PointHistory
import io.hhplus.tdd.point.service.dto.dao.TransactionType
import io.hhplus.tdd.point.service.dto.dao.UserPoint
import io.hhplus.tdd.point.service.dto.request.ChargePointRequest
import io.hhplus.tdd.point.service.dto.request.UsePointRequest
import org.springframework.stereotype.Service
import java.util.concurrent.locks.ReentrantLock

@Service
class PointService(
    private val pointHistoryRepository: PointHistoryRepository,
    private val userPointRepository: UserPointRepository,

    ) : PointUseCase {
    private val lock = ReentrantLock()
    override fun getUserPointInfo(userId: Long): UserPoint {
        return userPointRepository.selectById(userId)
    }

    override fun getUserPointHistories(id: Long): List<PointHistory> {
        return pointHistoryRepository.selectAllByUserId(id)
    }

    override fun chargePoint(request: ChargePointRequest): UserPoint {
        var savedUserPoint: UserPoint

        lock.lock()
        try {
            val userPoint = userPointRepository.selectById(request.userId)
            val chargedPoint = userPoint.chargePoint(request.amount)
            savedUserPoint = userPointRepository.insertOrUpdate(chargedPoint.id, chargedPoint.point)
        } finally {
            lock.unlock()
        }

        pointHistoryRepository.insert(
            request.userId,
            request.amount,
            TransactionType.CHARGE,
            savedUserPoint.updateMillis
        )

        return savedUserPoint
    }

    override fun usePoint(request: UsePointRequest): UserPoint {
        var savedUserPoint: UserPoint

        lock.lock()
        try {
            val userPoint = userPointRepository.selectById(request.userId)
            val usedPoint = userPoint.usePoint(request.amount)
            savedUserPoint = userPointRepository.insertOrUpdate(usedPoint.id, usedPoint.point)
        } finally {
            lock.unlock()
        }

        pointHistoryRepository.insert(
            request.userId,
            request.amount,
            TransactionType.USE,
            savedUserPoint.updateMillis
        )

        return savedUserPoint
    }
}