package com.netkeep5g.app.model

enum class TrafficMode(val displayName: String, val description: String) {
    KEEP_ALIVE(
        displayName = "Keep-Alive Mode",
        description = "Sends small periodic HTTPS pings to maintain socket and cell tower connection with minimal data usage."
    ),
    CONTINUOUS(
        displayName = "Continuous Download",
        description = "Continuously downloads real chunked data throttled precisely between 1 KB/s and 50 KB/s."
    );

    companion object {
        fun fromName(name: String?): TrafficMode {
            return entries.find { it.name.equals(name, ignoreCase = true) } ?: KEEP_ALIVE
        }
    }
}
