package com.agon.app.domain.engine.game

/**
 * Abstraction over System.currentTimeMillis()
 * Inject a fake clock in tests for deterministic behavior
 */
interface GameClock {
    fun now(): Long
}

class SystemClock : GameClock {
    override fun now(): Long = System.currentTimeMillis()
}

/** Use in tests: FakeClock(1000L) */
class FakeClock(private var time: Long = 0L) : GameClock {
    override fun now(): Long = time
    fun advance(ms: Long) { time += ms }
}
