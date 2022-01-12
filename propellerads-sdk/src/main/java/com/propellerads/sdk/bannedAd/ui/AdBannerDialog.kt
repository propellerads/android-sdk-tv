package com.propellerads.sdk.bannedAd.ui

import android.content.res.Resources
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.propellerads.sdk.databinding.PropellerBannerQrBinding
import com.propellerads.sdk.repository.BannerAppearance
import com.propellerads.sdk.repository.BannerConfig
import com.propellerads.sdk.repository.BannerGravity
import com.propellerads.sdk.utils.Colors
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

internal class AdBannerDialog private constructor() :
    DialogFragment(), IAdBanner, CoroutineScope {

    companion object {
        const val DISMISS_THRESHOLD = 500L

        fun build(config: BannerConfig) = AdBannerDialog().apply {
            arguments = Bundle().apply {
                putSerializable(IAdBanner.CONFIG, config)
            }
        }
    }

    init {
        isCancelable = false
    }

    private val job = Job()
    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Main

    private var dismissTime = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // remove title space
        setStyle(STYLE_NO_TITLE, android.R.style.Theme_Dialog)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val config = arguments?.getSerializable(IAdBanner.CONFIG) as? BannerConfig
            ?: return View(context)

        configureDialogParams(config.appearance)
        configureAutoDismiss(config.appearance)

        val binding = when (config.appearance.layoutTemplate) {
            "qr_code_3_1" -> PropellerBannerQrBinding.inflate(inflater)
            else -> PropellerBannerQrBinding.inflate(inflater)    // todo: figure out what to do
        }.apply { configure(config) }

        return binding.root
    }

    private fun configureDialogParams(appearance: BannerAppearance) {

        dialog?.window?.apply {

            // Configure vertical gravity
            when (appearance.gravity) {
                BannerGravity.TOP -> Gravity.TOP
                BannerGravity.BOTTOM -> Gravity.BOTTOM
                BannerGravity.CENTER -> null
            }?.let { verticalGravity ->
                setGravity(Gravity.CENTER_HORIZONTAL or verticalGravity)
            }

            decorView.apply {

                if (!appearance.hasRoundedCorners) {
                    // remove dialog corner radius
                    val background = Colors.from(appearance.backgroundColor)
                    setBackgroundColor(background)
                }

                val displayMetrics = Resources.getSystem().displayMetrics
                if (appearance.isFullWidth) {
                    minimumWidth = displayMetrics.widthPixels
                }
                if (appearance.isFullHeight) {
                    minimumHeight = displayMetrics.heightPixels
                }
            }

            fun getSize(isFull: Boolean) =
                if (isFull) FrameLayout.LayoutParams.MATCH_PARENT
                else FrameLayout.LayoutParams.WRAP_CONTENT

            setLayout(
                getSize(appearance.isFullWidth),
                getSize(appearance.isFullHeight)
            )
        }
    }

    private fun configureAutoDismiss(appearance: BannerAppearance) {
        val dismissValue = appearance.dismissTimerValue
        if (dismissValue == 0L) return

        dismissTime = System.currentTimeMillis() + dismissValue
        scheduleDismiss()
    }

    override fun onResume() {
        super.onResume()
        scheduleDismiss()
    }

    private fun scheduleDismiss() {
        if (dismissTime > 0L) {
            if (dismissTime - DISMISS_THRESHOLD < System.currentTimeMillis()) {
                dismiss()
            } else {
                launch {
                    val timeout = dismissTime - System.currentTimeMillis()
                    delay(timeout)
                    dismiss()
                }
            }
        }
    }

    override fun show(fragmentManager: FragmentManager) {
        show(fragmentManager, null)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        job.cancel()
    }
}