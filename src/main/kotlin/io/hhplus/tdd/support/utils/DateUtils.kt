package io.hhplus.tdd.support.utils

import java.time.LocalDateTime
import java.time.ZoneId

class DateUtils {
    //TODO : 이거 테스트로 분리할수있을텐데 어떻게 하지?
    companion object {
        fun nowDateTimeByMilli(): Long {
            return LocalDateTime.now().atZone(ZoneId.of("Asia/Seoul")).toInstant().toEpochMilli()
        }
    }
}