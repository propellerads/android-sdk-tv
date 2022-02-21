package com.propellerads.sdk.bannerAd.ui.base

import androidx.lifecycle.ViewModel
import com.propellerads.sdk.utils.Logger
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.coroutines.CoroutineContext

internal abstract class BaseDialogViewModel : ViewModel(), CoroutineScope {

    private companion object {
        const val TAG = "Banner"
    }

    private val job = Job()
    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.IO

    private val _dismissFlow = MutableStateFlow(false)
    val dismissFlow: StateFlow<Boolean>
        get() = _dismissFlow

    protected fun startAutoDismissTimeout(timeToDismiss: Long) {
        if (timeToDismiss == 0L) return

        val dismissTime = System.currentTimeMillis() + timeToDismiss

        launch {
            while (isActive) {
                delay(1000)
                // Compare with the current time to prevent the timer being paused
                // when the App in the background
                if (System.currentTimeMillis() >= dismissTime) {
                    Logger.d("Dismiss by the Timer", TAG)
                    dismissBanner()
                }
            }
        }
    }

    fun dismissBanner() {
        job.cancelChildren()
        launch {
            _dismissFlow.emit(true)
        }
    }

    override fun onCleared() {
        super.onCleared()
        job.cancel()
    }
}