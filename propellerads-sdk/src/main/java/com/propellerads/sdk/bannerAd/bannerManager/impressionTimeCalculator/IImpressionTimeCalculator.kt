package com.propellerads.sdk.bannerAd.bannerManager.impressionTimeCalculator

interface IImpressionTimeCalculator {

    fun calculateNextImpressionTime(
        interval: Int,
        timeout: Int,
        maxFrequency: Int,
        capping: Int,
        history: List<Long>,
        addForcedTimeout: Boolean,
        currentTime: Long,
    ): Long
}