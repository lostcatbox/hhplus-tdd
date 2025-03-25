package io.hhplus.tdd.point.service

import io.hhplus.tdd.point.service.dto.dao.PointHistory
import io.hhplus.tdd.point.service.dto.dao.UserPoint
import io.hhplus.tdd.point.service.dto.request.ChargePointRequest
import io.hhplus.tdd.point.service.dto.request.UsePointRequest

interface PointUseCase {
    fun getUserPointInfo(userId: Long): UserPoint

    fun getUserPointHistories(id: Long): List<PointHistory>
    
    fun chargePoint(request: ChargePointRequest): UserPoint

    fun usePoint(request: UsePointRequest): UserPoint
}