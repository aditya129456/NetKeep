package com.netkeep5g.app.model

enum class SpeedPreset(val label: String, val speedKBps: Int) {
    VERY_LOW("Very Low", 1),
    LOW("Low", 5),
    MEDIUM("Medium", 10),
    HIGH("High", 20),
    MAXIMUM("Maximum", 50),
    CUSTOM("Custom", -1);

    val speedKbps: Int
        get() = if (speedKBps > 0) speedKBps * 8 else -1

    companion object {
        fun fromSpeed(speedKBps: Int): SpeedPreset {
            return entries.find { it != CUSTOM && it.speedKBps == speedKBps } ?: CUSTOM
        }

        fun fromName(name: String?): SpeedPreset {
            return entries.find { it.name.equals(name, ignoreCase = true) } ?: LOW
        }
    }
}
