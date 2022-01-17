package com.propellerads.sdk

import com.propellerads.sdk.bannerAd.bannerManager.impressionTimeCalculator.ImpressionTimeCalculator
import com.propellerads.sdk.utils.ILogger
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class ImpressionTimeCalculatorUnitTest {

    private val logger = mockk<ILogger>()
    private val calculator = ImpressionTimeCalculator(logger)

    @Before
    fun prepare() {
        every { logger.d(any(), any()) } returns Unit
    }

    @Test
    fun `NO impressions history`() {
        val currentTime = System.currentTimeMillis()
        val interval = 10_000
        val timeout = 5_000
        val maxFrequency = 3
        val capping = 60_000
        val history = emptyList<Long>()

        val nextImpression = calculator.calculateNextImpressionTime(
            interval = interval,
            timeout = timeout,
            maxFrequency = maxFrequency,
            capping = capping,
            history = history,
            addForcedTimeout = false,
            currentTime = currentTime,
        )
        val expectedImpression = currentTime + timeout
        assertEquals(expectedImpression, nextImpression)
    }

    @Test
    fun `Long time WITHOUT impressions`() {
        val currentTime = System.currentTimeMillis()
        val interval = 10_000
        val timeout = 5_000
        val maxFrequency = 3
        val capping = 5_000
        val history = listOf(
            currentTime - 300_000,
            currentTime - 200_000,
            currentTime - 100_000,
        )
        val addForcedTimeout = true
        val nextImpression = calculator.calculateNextImpressionTime(
            interval = interval,
            timeout = timeout,
            maxFrequency = maxFrequency,
            capping = capping,
            history = history,
            addForcedTimeout = addForcedTimeout,
            currentTime = currentTime,
        )
        val expectedImpression = currentTime + timeout
        assertEquals(expectedImpression, nextImpression)
    }

    @Test
    fun `NO impressions in capping`() {
        val currentTime = System.currentTimeMillis()
        val interval = 10_000
        val timeout = 5_000
        val maxFrequency = 3
        val capping = 5_000
        val history = listOf(
            currentTime - 33_000,
            currentTime - 23_000,
            currentTime - 13_000,
            currentTime - 3_000,
        )
        val addForcedTimeout = true
        val nextImpression = calculator.calculateNextImpressionTime(
            interval = interval,
            timeout = timeout,
            maxFrequency = maxFrequency,
            capping = capping,
            history = history,
            addForcedTimeout = addForcedTimeout,
            currentTime = currentTime,
        )
        val expectedImpression = history.last() + interval + timeout
        assertEquals(expectedImpression, nextImpression)
    }

    @Test
    fun `MAX - 1 impressions in capping`() {
        val currentTime = System.currentTimeMillis()
        val interval = 10_000
        val timeout = 5_000
        val maxFrequency = 4
        val capping = 30_000
        val history = listOf(
            currentTime - 33_000,
            currentTime - 23_000,
            currentTime - 13_000,
            currentTime - 3_000,
        )
        val addForcedTimeout = true
        val nextImpression = calculator.calculateNextImpressionTime(
            interval = interval,
            timeout = timeout,
            maxFrequency = maxFrequency,
            capping = capping,
            history = history,
            addForcedTimeout = addForcedTimeout,
            currentTime = currentTime,
        )
        val expectedImpression = history.last() + interval + timeout
        assertEquals(expectedImpression, nextImpression)
    }

    @Test
    fun `MAX - 2 impressions in capping`() {
        val currentTime = System.currentTimeMillis()
        val interval = 10_000
        val timeout = 5_000
        val maxFrequency = 5
        val capping = 30_000
        val history = listOf(
            currentTime - 33_000,
            currentTime - 23_000,
            currentTime - 13_000,
            currentTime - 3_000,
        )
        val addForcedTimeout = true
        val nextImpression = calculator.calculateNextImpressionTime(
            interval = interval,
            timeout = timeout,
            maxFrequency = maxFrequency,
            capping = capping,
            history = history,
            addForcedTimeout = addForcedTimeout,
            currentTime = currentTime,
        )
        val expectedImpression = history.last() + interval + timeout
        assertEquals(expectedImpression, nextImpression)
    }

    @Test
    fun `MAX impressions in capping`() {
        val currentTime = System.currentTimeMillis()
        val interval = 10_000
        val timeout = 5_000
        val maxFrequency = 3
        val capping = 40_000
        val history = listOf(
            currentTime - 33_000,
            currentTime - 23_000,
            currentTime - 13_000,
            currentTime - 3_000,
        )
        val addForcedTimeout = true
        val nextImpression = calculator.calculateNextImpressionTime(
            interval = interval,
            timeout = timeout,
            maxFrequency = maxFrequency,
            capping = capping,
            history = history,
            addForcedTimeout = addForcedTimeout,
            currentTime = currentTime,
        )
        val expectedImpression = history.reversed()[maxFrequency - 1] + capping
        assertEquals(expectedImpression, nextImpression)
    }

    @Test
    fun `MORE than MAX impressions in capping`() {
        val currentTime = System.currentTimeMillis()
        val interval = 10_000
        val timeout = 5_000
        val maxFrequency = 2
        val capping = 40_000
        val history = listOf(
            currentTime - 33_000,
            currentTime - 23_000,
            currentTime - 13_000,
            currentTime - 3_000,
        )
        val addForcedTimeout = true
        val nextImpression = calculator.calculateNextImpressionTime(
            interval = interval,
            timeout = timeout,
            maxFrequency = maxFrequency,
            capping = capping,
            history = history,
            addForcedTimeout = addForcedTimeout,
            currentTime = currentTime,
        )
        val expectedImpression = history.reversed()[maxFrequency - 1] + capping
        assertEquals(expectedImpression, nextImpression)
    }

    @Test
    fun `ZERO capping`() {
        val currentTime = System.currentTimeMillis()
        val interval = 10_000
        val timeout = 5_000
        val maxFrequency = 2
        val capping = 0
        val history = listOf(
            currentTime - 33_000,
            currentTime - 23_000,
            currentTime - 13_000,
            currentTime - 3_000,
        )
        val addForcedTimeout = true
        val nextImpression = calculator.calculateNextImpressionTime(
            interval = interval,
            timeout = timeout,
            maxFrequency = maxFrequency,
            capping = capping,
            history = history,
            addForcedTimeout = addForcedTimeout,
            currentTime = currentTime,
        )
        val expectedImpression = history.last() + interval + timeout
        assertEquals(expectedImpression, nextImpression)
    }

    @Test
    fun `ZERO frequency`() {
        val currentTime = System.currentTimeMillis()
        val interval = 10_000
        val timeout = 5_000
        val maxFrequency = 0
        val capping = 30_000
        val history = listOf(
            currentTime - 33_000,
            currentTime - 23_000,
            currentTime - 13_000,
            currentTime - 3_000,
        )
        val addForcedTimeout = true
        val nextImpression = calculator.calculateNextImpressionTime(
            interval = interval,
            timeout = timeout,
            maxFrequency = maxFrequency,
            capping = capping,
            history = history,
            addForcedTimeout = addForcedTimeout,
            currentTime = currentTime,
        )
        val expectedImpression = history.last() + interval + timeout
        assertEquals(expectedImpression, nextImpression)
    }

    @Test
    fun `ZERO capping ZERO frequency`() {
        val currentTime = System.currentTimeMillis()
        val interval = 10_000
        val timeout = 5_000
        val maxFrequency = 0
        val capping = 0
        val history = listOf(
            currentTime - 33_000,
            currentTime - 23_000,
            currentTime - 13_000,
            currentTime - 3_000,
        )
        val addForcedTimeout = true
        val nextImpression = calculator.calculateNextImpressionTime(
            interval = interval,
            timeout = timeout,
            maxFrequency = maxFrequency,
            capping = capping,
            history = history,
            addForcedTimeout = addForcedTimeout,
            currentTime = currentTime,
        )
        val expectedImpression = history.last() + interval + timeout
        assertEquals(expectedImpression, nextImpression)
    }
}