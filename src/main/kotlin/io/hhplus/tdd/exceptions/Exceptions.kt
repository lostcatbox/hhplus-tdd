package io.hhplus.tdd.exceptions

//Point 도메인 Exception
open class PointException : RuntimeException()
class PointAmountNegativeException(val id: Long) : PointException()
class PointAmountOverMaxValueException(val id: Long) : PointException()