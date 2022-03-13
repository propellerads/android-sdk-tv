package com.propellerads.sdk.bannerAd.bannerManager.impressionHistory

interface IImpressionHistory {

    fun add(configId: String, uniqueSuffix: String, timestamp: Long)

    fun get(configId: String, uniqueSuffix: String): List<Long>
}