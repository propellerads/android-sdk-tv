package com.propellerads.sdk.bannerAd.bannerManager.impressionHistory

class ImpressionHistory : IImpressionHistory {

    private val impressionsHistory = mutableMapOf<String, List<Long>>()

    @Synchronized
    override fun add(configId: String, uniqueSuffix: String, timestamp: Long) {
        val compositeKey = composeKey(configId, uniqueSuffix)
        val timestamps = impressionsHistory
            .getOrElse(compositeKey) { listOf() }
            .toMutableList()
            .apply { add(timestamp) }
        impressionsHistory[compositeKey] = timestamps
    }

    @Synchronized
    override fun get(configId: String, uniqueSuffix: String): List<Long> {
        val compositeKey = composeKey(configId, uniqueSuffix)
        return impressionsHistory[compositeKey] ?: emptyList()
    }

    private fun composeKey(configId: String, uniqueSuffix: String) =
        "$configId\\_$uniqueSuffix"
}