package io.hhplus.tdd.point.controller

import io.hhplus.tdd.point.controller.dto.request.PointRequest
import io.hhplus.tdd.point.service.PointUseCase
import io.hhplus.tdd.point.service.dto.dao.PointHistory
import io.hhplus.tdd.point.service.dto.dao.UserPoint
import io.hhplus.tdd.point.service.dto.request.ChargePointRequest
import io.hhplus.tdd.point.service.dto.request.UsePointRequest
import jakarta.validation.Valid
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/point")
class PointController(
    private val pointUseCase: PointUseCase
) {
    private val logger: Logger = LoggerFactory.getLogger(javaClass)

    @GetMapping("{id}")
    fun point(
        @PathVariable id: Long,
    ): UserPoint {
        require(id >= 0) { "사용자 ID는 0 이상이어야 합니다" }
        return pointUseCase.getUserPointInfo(id)
    }

    @GetMapping("{id}/histories")
    fun history(
        @PathVariable id: Long,
    ): List<PointHistory> {
        require(id >= 0) { "사용자 ID는 0 이상이어야 합니다" }
        return pointUseCase.getUserPointHistories(id)
    }

    @PatchMapping("{id}/charge")
    fun charge(
        @PathVariable id: Long,
        @RequestBody @Valid request: PointRequest
    ): UserPoint {
        require(id >= 0) { "사용자 ID는 0 이상이어야 합니다" }
        return pointUseCase.chargePoint(
            ChargePointRequest(id, request.amount)
        )
    }

    @PatchMapping("{id}/use")
    fun use(
        @PathVariable id: Long,
        @RequestBody @Valid request: PointRequest
    ): UserPoint {
        require(id >= 0) { "사용자 ID는 0 이상이어야 합니다" }
        return pointUseCase.usePoint(
            UsePointRequest(id, request.amount)
        )
    }
} 