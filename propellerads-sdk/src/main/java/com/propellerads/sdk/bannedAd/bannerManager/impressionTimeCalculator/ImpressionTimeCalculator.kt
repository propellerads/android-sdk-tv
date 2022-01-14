package com.propellerads.sdk.bannedAd.bannerManager.impressionTimeCalculator

import com.propellerads.sdk.utils.ILogger
import kotlin.math.max

internal class ImpressionTimeCalculator(
    private val logger: ILogger
) : IImpressionTimeCalculator {

    private companion object {
        const val TAG = "Banner"
    }

    override fun calculateNextImpressionTime(
        interval: Int,
        timeout: Int,
        maxFrequency: Int,
        capping: Int,
        history: List<Long>,
        addForcedTimeout: Boolean,
        currentTime: Long,
    ): Long {

        // if no previous impressions then schedule the next one after initial timeout
        if (history.isEmpty()) {
            logger.d("No previous impressions. Schedule the next with the initial timeout", TAG)
            return currentTime + timeout
        }

        val forcedTimeout = if (addForcedTimeout) timeout else 0

        // calculate next impression time from the last impression
        val lastWithInterval = history.last() + interval + forcedTimeout
        val minNextImpressionTime = currentTime + forcedTimeout
        val nextImpressionTime = max(lastWithInterval, minNextImpressionTime)

        // if capping or frequency parameters are not presented then return nextImpressionTime
        if (maxFrequency == 0 || capping == 0) {
            logger.d("No frequency rules. Schedule the next with the regular interval", TAG)
            return nextImpressionTime
        }

        // calculate impressions frequency in the current capping period
        val cappingStart = nextImpressionTime - capping
        val reversedHistory = history.reversed()
        val impressionsInCapping = reversedHistory.fold(
            initial = 0,
            { impressionsCount, timestamp ->
                if (timestamp >= cappingStart)
                    impressionsCount.inc() else return@fold impressionsCount
            }
        )

        // if impressions frequency not exceeded then return nextImpressionTime
        return if (impressionsInCapping < maxFrequency) {
            logger.d("Impressions: $impressionsInCapping, but max is $maxFrequency. Schedule the next with the regular interval", TAG)
            nextImpressionTime
        } else {
            // Otherwise calculate the new capping period start time
            val newCappingStart = reversedHistory[maxFrequency - 1]
            logger.d("Max frequency is exceeded. Schedule the next impression for the new capping period", TAG)
            newCappingStart + capping
        }
    }
}