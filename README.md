

# 항해 1주차



## 과제 목표
- TDD 도입
- 동시성 이슈 해결
  - 동시성 이슈: 여러 요청이 동시에 들어오는 상황에서 Race Condition(경쟁 조건) 상태




## 심화 과제 목표

- 선택한 언어에 대한 동시성 제어 방식 및 각 적용의 장/단점을 기술한 보고서 작성




### Kotlin, Java 언어에서의 동시성 이슈 해결
- Synchronized, ConcurrentHashMap, ReentrantLock



#### @Synchronized (현재 적용된 방식)

```kotlin
@Synchronized
override fun insertOrUpdate(id: Long, amount: Long): UserPoint {
    val current = selectById(id)
    return userPointTable.insertOrUpdate(id, current.point + amount)
}
```

장점:

- 구현이 간단

- 데드락 위험이 적음

- 모든 동기화 문제를 확실히 해결

단점:

- 성능 저하 (한 번에 하나의 스레드만 접근)

- 분산 환경에서 사용 불가

- 메모리 사용량 증가



#### ReentrantLock

```kotlin
private val lock = ReentrantLock()

fun updatePoint(id: Long, amount: Long): UserPoint {
    lock.lock()
    try {
        val current = selectById(id)
        return userPointTable.insertOrUpdate(id, current.point + amount)
    } finally {
        lock.unlock()
    }
}
```

장점:

- 더 세밀한 제어 가능

- tryLock() 으로 타임아웃 설정 가능

- 인터럽트 처리 가능

단점:

- 구현이 복잡

- 실수로 lock 해제를 놓칠 수 있음

- 분산 환경에서 사용 불가



#### ConcurrentHashMapkotlin

```kotlin
private val pointMap = ConcurrentHashMap<Long, AtomicLong>()

fun updatePoint(id: Long, amount: Long): UserPoint {
    val point = pointMap.computeIfAbsent(id) { AtomicLong(0) }
    point.addAndGet(amount)
    return UserPoint(id, point.get(), System.currentTimeMillis())
}
```

장점:

- 세밀한 동시성 제어

- 높은 성능

- 부분 잠금으로 인한 처리량 향상

단점:

- 복잡한 연산에는 부적합
- 메모리 사용량 증가
- 분산 환경에서 사용 불가



### 인프라 관점에서의 해결
- DB lock 사용
- Redis 사용



#### DB Lock

```sql
SELECT point FROM user_point WHERE id = ? FOR UPDATE
```



장점:

- 데이터 정합성 보장

- 분산 환경에서 사용 가능

- 트랜잭션 관리 용이

단점:

- 성능 저하

- 데드락 가능성

- Lock 범위가 커질 수 있음



#### Redis 기반 분산 락

```kotlin
val lock = redisTemplate.opsForValue()
if (lock.setIfAbsent(key, value, Duration.ofSeconds(3))) {
    try {
        // 포인트 업데이트 로직
    } finally {
        lock.delete(key)
    }
}
```

장점:

- 분산 환경에서 사용 가능

- 높은 성능

- 세밀한 락 제어 가능

단점:

- 추가 인프라 필요

- 구현이 복잡

- 네트워크 지연 영향





## 과제 중 기타 사항




### 과제 해결 중 현재 구조적으로 문제해야할 할일

- 현재 DAO와 DTO가 섞여있어서, 도메인 모델의 비즈니스 로직과 엔티티 모델에 필요한 로직이 통합되어있다-> 책임분리 필요
- 현재 Controller에서 DTO를 그대로 반환하는 중이다. 이는 추후 응답값이 달라져야할때, 도메인 모델까지 영향을 줄수있으므로 필요시 분리 필요
- 현재 폴더 트리 구조는 hhplus.tdd.[database, point, Application.kt]이다. 도메인별로 분리되었지만, point.[Controller, Service, UserPoint. ...]등이 패키지 구조없이 존재한다 -> 패키지 분리 필요
