package io.hhplus.tdd.point.repo

import io.hhplus.tdd.database.UserPointTable
import io.hhplus.tdd.point.service.dto.dao.UserPoint
import org.springframework.stereotype.Repository

@Repository
class UserPointRepositoryImpl(
    val userPointTable: UserPointTable
) : UserPointRepository {
    override fun selectById(id: Long): UserPoint {
        return userPointTable.selectById(id)
    }

    override fun insertOrUpdate(id: Long, amount: Long): UserPoint {
        return userPointTable.insertOrUpdate(id, amount)
    }
}