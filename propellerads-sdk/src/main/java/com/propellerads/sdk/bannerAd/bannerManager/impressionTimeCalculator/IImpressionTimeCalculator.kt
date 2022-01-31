package com.propellerads.sdk.bannerAd.bannerManager.impressionTimeCalculator

interface IImpressionTimeCalculator {

    fun calculateNextImpressionTime(
        interval: Long,
        timeout: Long,
        maxFrequency: Int,
        capping: Long,
        history: List<Long>,
        addForcedTimeout: Boolean,
        currentTime: Long,
    ): Long?
}