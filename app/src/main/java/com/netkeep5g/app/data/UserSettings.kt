package com.netkeep5g.app.data

import com.netkeep5g.app.engine.TrafficEngine
import com.netkeep5g.app.model.TrafficMode

data class UserSettings(
    val defaultMode: TrafficMode = TrafficMode.KEEP_ALIVE,
    val defaultSpeedKBps: Int = 5,
    val keepAliveIntervalSec: Int = 30,
    val dataUsageWarningMB: Int = 1000,
    val customContinuousUrl: String = TrafficEngine.DEFAULT_CONTINUOUS_URL,
    val customKeepAliveUrl: String = TrafficEngine.DEFAULT_KEEP_ALIVE_URL,
    val cumulativeBytesAllTime: Long = 0L
)
