package io.hhplus.tdd.point.controller.dto.request

import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive

data class PointRequest(
    @field:NotNull(message = "금액은 필수입니다")
    @field:Positive(message = "금액은 양수여야 합니다")
    val amount: Long
)