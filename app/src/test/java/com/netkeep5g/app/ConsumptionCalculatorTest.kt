package com.netkeep5g.app

import com.netkeep5g.app.model.ConsumptionEstimate
import org.junit.Assert.assertEquals
import org.junit.Test

class ConsumptionCalculatorTest {

    @Test
    fun testConsumptionRequirementsTable() {
        // Table in prompt:
        // 1 KB/s -> 86.4 MB
        // 5 KB/s -> 432 MB
        // 10 KB/s -> 864 MB
        // 20 KB/s -> 1.728 GB
        // 50 KB/s -> 4.32 GB

        val est1 = ConsumptionEstimate.calculate(1)
        assertEquals(86_400_000L, est1.dailyBytes)
        assertEquals("86.40 MB", ConsumptionEstimate.formatBytes(est1.dailyBytes))

        val est5 = ConsumptionEstimate.calculate(5)
        assertEquals(432_000_000L, est5.dailyBytes)
        assertEquals("432.00 MB", ConsumptionEstimate.formatBytes(est5.dailyBytes))

        val est10 = ConsumptionEstimate.calculate(10)
        assertEquals(864_000_000L, est10.dailyBytes)
        assertEquals("864.00 MB", ConsumptionEstimate.formatBytes(est10.dailyBytes))

        val est20 = ConsumptionEstimate.calculate(20)
        assertEquals(1_728_000_000L, est20.dailyBytes)
        assertEquals("1.728 GB", ConsumptionEstimate.formatBytes(est20.dailyBytes))

        val est50 = ConsumptionEstimate.calculate(50)
        assertEquals(4_320_000_000L, est50.dailyBytes)
        assertEquals("4.320 GB", ConsumptionEstimate.formatBytes(est50.dailyBytes))
    }

    @Test
    fun testHourlyAndMonthly() {
        val est = ConsumptionEstimate.calculate(10) // 10 KB/s
        // 10 KB/s * 3600 = 36,000 KB = 36 MB / hour
        assertEquals(36_000_000L, est.hourlyBytes)
        assertEquals("36.00 MB", ConsumptionEstimate.formatBytes(est.hourlyBytes))

        // 864 MB * 30 days = 25.92 GB
        assertEquals(864_000_000L * 30L, est.monthlyBytes)
        assertEquals("25.920 GB", ConsumptionEstimate.formatBytes(est.monthlyBytes))
    }
}
