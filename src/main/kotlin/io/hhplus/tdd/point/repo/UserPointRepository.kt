package io.hhplus.tdd.point.repo

import io.hhplus.tdd.point.service.dto.dao.UserPoint

interface UserPointRepository {
    fun selectById(id: Long): UserPoint
    fun insertOrUpdate(id: Long, amount: Long): UserPoint
}