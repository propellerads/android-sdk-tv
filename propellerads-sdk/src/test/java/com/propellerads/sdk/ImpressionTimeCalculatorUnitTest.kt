package com.propellerads.sdk

import com.propellerads.sdk.bannedAd.bannerManager.impressionTimeCalculator.ImpressionTimeCalculator
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
        val interval = 10_000
        val timeout = 5_000
        val maxFrequency = 3
        val capping = 60_000
        val history = emptyList<Long>()
        val time = System.currentTimeMillis()

        val nextImpression = calculator.calculateNextImpressionTime(
            interval = interval,
            timeout = timeout,
            maxFrequency = maxFrequency,
            capping = capping,
            history = history,
        )
        val expectedImpression = time + timeout
        assertEquals(expectedImpression, nextImpression)
    }

    @Test
    fun `Long time WITHOUT impressions`() {
        val time = System.currentTimeMillis()
        val interval = 10_000
        val timeout = 5_000
        val maxFrequency = 3
        val capping = 5_000
        val history = listOf(
            time - 300_000,
            time - 200_000,
            time - 100_000,
        )
        val nextImpression = calculator.calculateNextImpressionTime(
            interval = interval,
            timeout = timeout,
            maxFrequency = maxFrequency,
            capping = capping,
            history = history,
        )
        assertEquals(time, nextImpression)
    }

    @Test
    fun `NO impressions in capping`() {
        val time = System.currentTimeMillis()
        val interval = 10_000
        val timeout = 5_000
        val maxFrequency = 3
        val capping = 5_000
        val history = listOf(
            time - 33_000,
            time - 23_000,
            time - 13_000,
            time - 3_000,
        )
        val nextImpression = calculator.calculateNextImpressionTime(
            interval = interval,
            timeout = timeout,
            maxFrequency = maxFrequency,
            capping = capping,
            history = history,
        )
        val expectedImpression = history.last() + interval
        assertEquals(expectedImpression, nextImpression)
    }

    @Test
    fun `MAX - 1 impressions in capping`() {
        val time = System.currentTimeMillis()
        val interval = 10_000
        val timeout = 5_000
        val maxFrequency = 4
        val capping = 30_000
        val history = listOf(
            time - 33_000,
            time - 23_000,
            time - 13_000,
            time - 3_000,
        )
        val nextImpression = calculator.calculateNextImpressionTime(
            interval = interval,
            timeout = timeout,
            maxFrequency = maxFrequency,
            capping = capping,
            history = history,
        )
        val expectedImpression = history.last() + interval
        assertEquals(expectedImpression, nextImpression)
    }

    @Test
    fun `MAX - 2 impressions in capping`() {
        val time = System.currentTimeMillis()
        val interval = 10_000
        val timeout = 5_000
        val maxFrequency = 5
        val capping = 30_000
        val history = listOf(
            time - 33_000,
            time - 23_000,
            time - 13_000,
            time - 3_000,
        )
        val nextImpression = calculator.calculateNextImpressionTime(
            interval = interval,
            timeout = timeout,
            maxFrequency = maxFrequency,
            capping = capping,
            history = history,
        )
        val expectedImpression = history.last() + interval
        assertEquals(expectedImpression, nextImpression)
    }

    @Test
    fun `MAX impressions in capping`() {
        val time = System.currentTimeMillis()
        val interval = 10_000
        val timeout = 5_000
        val maxFrequency = 3
        val capping = 30_000
        val history = listOf(
            time - 33_000,
            time - 23_000,
            time - 13_000,
            time - 3_000,
        )
        val nextImpression = calculator.calculateNextImpressionTime(
            interval = interval,
            timeout = timeout,
            maxFrequency = maxFrequency,
            capping = capping,
            history = history,
        )
        val expectedImpression = history.reversed()[maxFrequency - 1] + capping
        assertEquals(expectedImpression, nextImpression)
    }

    @Test
    fun `MORE than MAX impressions in capping`() {
        val time = System.currentTimeMillis()
        val interval = 10_000
        val timeout = 5_000
        val maxFrequency = 2
        val capping = 30_000
        val history = listOf(
            time - 33_000,
            time - 23_000,
            time - 13_000,
            time - 3_000,
        )
        val nextImpression = calculator.calculateNextImpressionTime(
            interval = interval,
            timeout = timeout,
            maxFrequency = maxFrequency,
            capping = capping,
            history = history,
        )
        val expectedImpression = history.reversed()[maxFrequency - 1] + capping
        assertEquals(expectedImpression, nextImpression)
    }

    @Test
    fun `ZERO capping`() {
        val time = System.currentTimeMillis()
        val interval = 10_000
        val timeout = 5_000
        val maxFrequency = 2
        val capping = 0
        val history = listOf(
            time - 33_000,
            time - 23_000,
            time - 13_000,
            time - 3_000,
        )
        val nextImpression = calculator.calculateNextImpressionTime(
            interval = interval,
            timeout = timeout,
            maxFrequency = maxFrequency,
            capping = capping,
            history = history,
        )
        val expectedImpression = history.last() + interval
        assertEquals(expectedImpression, nextImpression)
    }

    @Test
    fun `ZERO frequency`() {
        val time = System.currentTimeMillis()
        val interval = 10_000
        val timeout = 5_000
        val maxFrequency = 0
        val capping = 30_000
        val history = listOf(
            time - 33_000,
            time - 23_000,
            time - 13_000,
            time - 3_000,
        )
        val nextImpression = calculator.calculateNextImpressionTime(
            interval = interval,
            timeout = timeout,
            maxFrequency = maxFrequency,
            capping = capping,
            history = history,
        )
        val expectedImpression = history.last() + interval
        assertEquals(expectedImpression, nextImpression)
    }

    @Test
    fun `ZERO capping ZERO frequency`() {
        val time = System.currentTimeMillis()
        val interval = 10_000
        val timeout = 5_000
        val maxFrequency = 0
        val capping = 0
        val history = listOf(
            time - 33_000,
            time - 23_000,
            time - 13_000,
            time - 3_000,
        )
        val nextImpression = calculator.calculateNextImpressionTime(
            interval = interval,
            timeout = timeout,
            maxFrequency = maxFrequency,
            capping = capping,
            history = history,
        )
        val expectedImpression = history.last() + interval
        assertEquals(expectedImpression, nextImpression)
    }
}