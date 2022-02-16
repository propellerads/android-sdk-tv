package com.propellerads.sdk.bannerAd.bannerManager.impressionHistory

interface IImpressionHistory {

    fun add(configId: String, timestamp: Long)

    fun get(configId: String): List<Long>
}