package com.netkeep5g.app.data

import com.netkeep5g.app.model.TrafficState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object TrafficStateRepository {

    private val _trafficState = MutableStateFlow(TrafficState())
    val trafficState: StateFlow<TrafficState> = _trafficState.asStateFlow()

    fun updateState(newState: TrafficState) {
        _trafficState.value = newState
    }

    fun updateRunning(isRunning: Boolean) {
        _trafficState.value = _trafficState.value.copy(isRunning = isRunning)
    }

    fun resetSession() {
        val current = _trafficState.value
        _trafficState.value = current.copy(
            totalBytesDownloaded = 0L,
            successfulRequests = 0L,
            failedRequests = 0L,
            actualSpeedKBps = 0.0,
            elapsedDurationSeconds = 0L,
            startTimeMs = if (current.isRunning) System.currentTimeMillis() else 0L,
            statusMessage = if (current.isRunning) "Running" else "Idle",
            lastError = null
        )
    }
}
