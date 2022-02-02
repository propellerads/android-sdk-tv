package com.propellerads.sdk.bannerAd.bannerManager.impressionHistory

class ImpressionHistory : IImpressionHistory {

    private val impressionsHistory = mutableMapOf<String, List<Long>>()

    @Synchronized
    override fun add(configId: String, timestamp: Long) {
        val timestamps = impressionsHistory
            .getOrElse(configId, { listOf() })
            .toMutableList()
            .apply { add(timestamp) }
        impressionsHistory[configId] = timestamps
    }

    @Synchronized
    override fun get(configId: String) =
        impressionsHistory[configId] ?: emptyList()
}