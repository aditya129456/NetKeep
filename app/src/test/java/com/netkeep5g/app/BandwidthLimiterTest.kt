package com.netkeep5g.app

import com.netkeep5g.app.engine.BandwidthLimiter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BandwidthLimiterTest {

    @Test
    fun testInitialRateAndClamping() {
        val limiter = BandwidthLimiter(10)
        assertEquals(10, limiter.getRateKBps())

        limiter.updateRate(50)
        assertEquals(50, limiter.getRateKBps())

        // Test clamping to minimum 1 KB/s
        limiter.updateRate(0)
        assertEquals(1, limiter.getRateKBps())
    }

    @Test
    fun testThrottlingTiming() {
        val limiter = BandwidthLimiter(5) // 5 KB/s = 5000 bytes/sec
        limiter.reset()

        val startNs = System.nanoTime()
        // Drain initial bucket tokens (5000 bytes)
        limiter.throttle(5000)

        // Now throttle an additional 5000 bytes, which should require approx 1 second
        val t1 = System.nanoTime()
        limiter.throttle(5000)
        val elapsedMs = (System.nanoTime() - t1) / 1_000_000L

        // Should have slept approx 900-1100ms
        assertTrue("Expected sleep around 1000ms, was ${elapsedMs}ms", elapsedMs >= 700L)
    }
}
