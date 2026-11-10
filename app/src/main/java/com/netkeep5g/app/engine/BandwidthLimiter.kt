package com.netkeep5g.app.engine

import kotlin.math.max
import kotlin.math.min

/**
 * High-precision Token Bucket Bandwidth Limiter.
 * Regulates throughput between 1 KB/s and 50 KB/s without holding data in memory.
 */
class BandwidthLimiter(initialRateKBps: Int) {

    @Volatile
    private var rateBytesPerSec: Long = max(1, initialRateKBps).toLong() * 1000L

    // Bucket capacity equals 1 second worth of traffic to absorb slight OS scheduling jitter
    @Volatile
    private var maxBucketCapacity: Double = rateBytesPerSec.toDouble()

    private var availableTokens: Double = maxBucketCapacity
    private var lastReplenishTimeNs: Long = System.nanoTime()

    private val lock = Any()

    /**
     * Dynamically update the bandwidth rate while running.
     */
    fun updateRate(newRateKBps: Int) {
        synchronized(lock) {
            val clamped = newRateKBps.coerceIn(1, 100)
            rateBytesPerSec = clamped.toLong() * 1000L
            maxBucketCapacity = rateBytesPerSec.toDouble()
            availableTokens = min(availableTokens, maxBucketCapacity)
        }
    }

    fun getRateKBps(): Int {
        return (rateBytesPerSec / 1000L).toInt()
    }

    /**
     * Throttles execution so that reading [bytes] conforms to the target rate.
     * Suspends / sleeps calling thread for the required nanoseconds if tokens are exhausted.
     */
    fun throttle(bytes: Int) {
        if (bytes <= 0) return

        var sleepNanos: Long = 0

        synchronized(lock) {
            val now = System.nanoTime()
            val elapsedNs = now - lastReplenishTimeNs
            lastReplenishTimeNs = now

            // Replenish tokens based on elapsed nanoseconds
            val tokensToAdd = (elapsedNs.toDouble() / 1_000_000_000.0) * rateBytesPerSec
            availableTokens = min(maxBucketCapacity, availableTokens + tokensToAdd)

            if (availableTokens < bytes) {
                val deficit = bytes - availableTokens
                // Time needed to generate the deficit tokens in nanoseconds
                val requiredNs = ((deficit / rateBytesPerSec.toDouble()) * 1_000_000_000.0).toLong()
                sleepNanos = max(0L, requiredNs)
                // Deduct tokens including deficit (will be accounted for after sleep)
                availableTokens = 0.0
            } else {
                availableTokens -= bytes
            }
        }

        if (sleepNanos > 0) {
            val millis = sleepNanos / 1_000_000L
            val nanos = (sleepNanos % 1_000_000L).toInt()
            try {
                if (millis > 0 || nanos > 0) {
                    Thread.sleep(millis, nanos)
                }
            } catch (e: InterruptedException) {
                Thread.currentThread().interrupt()
            }
        }
    }

    fun reset() {
        synchronized(lock) {
            availableTokens = maxBucketCapacity
            lastReplenishTimeNs = System.nanoTime()
        }
    }
}
