package com.propellerads.sdk.bannedAd.bannerManager.impressionTimeCalculator

interface IImpressionTimeCalculator {

    fun calculateNextImpressionTime(
        interval: Int,
        timeout: Int,
        maxFrequency: Int,
        capping: Int,
        history: List<Long>,
    ): Long
}