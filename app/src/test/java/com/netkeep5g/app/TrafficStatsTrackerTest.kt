package com.netkeep5g.app

import com.netkeep5g.app.engine.TrafficStatsTracker
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TrafficStatsTrackerTest {

    @Test
    fun testByteTrackingAndCounters() {
        val tracker = TrafficStatsTracker()
        tracker.startSession()

        tracker.addBytes(1024)
        tracker.addBytes(2048)
        assertEquals(3072L, tracker.getTotalBytes())

        tracker.recordSuccess()
        tracker.recordSuccess()
        tracker.recordFailure()

        assertEquals(2L, tracker.getSuccessfulRequests())
        assertEquals(1L, tracker.getFailedRequests())

        assertTrue(tracker.getSessionStartTimeMs() > 0L)

        tracker.reset()
        assertEquals(0L, tracker.getTotalBytes())
        assertEquals(0L, tracker.getSuccessfulRequests())
        assertEquals(0L, tracker.getFailedRequests())
    }
}
