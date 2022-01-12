package com.propellerads.sdk.bannedAd.bannerManager.impressionHistory

interface IImpressionHistory {

    fun add(configId: String, timestamp: Long)

    fun get(configId: String): List<Long>
}