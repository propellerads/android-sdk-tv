package com.propellerads.sdk.bannerAd.ui.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filter
import kotlin.coroutines.CoroutineContext

internal abstract class BaseBannerDialog :
    DialogFragment(), IBanner, CoroutineScope {

    private val job = Job()
    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Main

    protected abstract val dismissFlow: Flow<Boolean>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isCancelable = false
        // remove title space
        setStyle(STYLE_NO_TITLE, android.R.style.Theme_DeviceDefault_Dialog)
    }

    private fun getConfig() =
        arguments?.getSerializable(IBannerConfig.CONFIG) as? IBannerConfig

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val bannerConfig = getConfig()
        if (bannerConfig == null) {
            dismissSafely()
            return null
        }

        return configureBanner(inflater, bannerConfig)
    }

    protected abstract fun configureBanner(
        inflater: LayoutInflater,
        bannerConfig: IBannerConfig,
    ): View?

    override fun onResume() {
        super.onResume()
        subscribeOnDismiss()
    }

    private fun subscribeOnDismiss() {
        launch {
            dismissFlow
                .filter { it }
                .collect {
                    dismissSafely()
                }
        }
    }

    override fun show(fragmentManager: FragmentManager) {
        val bannerId = getConfig()?.id
        show(fragmentManager, bannerId)
    }

    protected fun dismissSafely() {
        if (!isStateSaved) {
            dismiss()
        }
    }

    override fun onStop() {
        super.onStop()
        job.cancelChildren()
    }
}