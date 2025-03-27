package io.hhplus.tdd.point.service

import io.hhplus.tdd.point.repo.PointHistoryRepository
import io.hhplus.tdd.point.repo.UserPointRepository
import io.hhplus.tdd.point.service.dto.dao.PointHistory
import io.hhplus.tdd.point.service.dto.dao.TransactionType
import io.hhplus.tdd.point.service.dto.dao.UserPoint
import io.hhplus.tdd.point.service.dto.request.ChargePointRequest
import io.hhplus.tdd.point.service.dto.request.UsePointRequest
import org.springframework.stereotype.Service

@Service
class PointService(
    private val pointHistoryRepository: PointHistoryRepository,
    private val userPointRepository: UserPointRepository
) : PointUseCase {
    override fun getUserPointInfo(userId: Long): UserPoint {
        return userPointRepository.selectById(userId)
    }

    override fun getUserPointHistories(id: Long): List<PointHistory> {
        return pointHistoryRepository.selectAllByUserId(id)
    }

    @Synchronized
    override fun chargePoint(request: ChargePointRequest): UserPoint {
        val userPoint = userPointRepository.selectById(request.userId)
        val chargedPoint = userPoint.chargePoint(request.amount)

        val savedUserPoint = userPointRepository.insertOrUpdate(chargedPoint.id, chargedPoint.point)

        pointHistoryRepository.insert(
            request.userId,
            request.amount,
            TransactionType.CHARGE,
            savedUserPoint.updateMillis
        )

        return savedUserPoint
    }

    @Synchronized
    override fun usePoint(request: UsePointRequest): UserPoint {
        val userPoint = userPointRepository.selectById(request.userId)
        val chargedPoint = userPoint.usePoint(request.amount)

        val savedUserPoint = userPointRepository.insertOrUpdate(chargedPoint.id, chargedPoint.point)

        pointHistoryRepository.insert(request.userId, request.amount, TransactionType.USE, savedUserPoint.updateMillis)
        return savedUserPoint
    }
}