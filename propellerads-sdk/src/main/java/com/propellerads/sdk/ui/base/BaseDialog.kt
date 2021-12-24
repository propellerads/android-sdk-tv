package com.propellerads.sdk.ui.base

import android.content.res.Resources
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager

interface IDialog {
    fun show(fragmentManager: FragmentManager)

    enum class Position {
        Bottom, Center
    }
}

internal abstract class BaseDialog : DialogFragment(), IDialog {

    companion object {
        const val POSITION = "POSITION"

        fun composeParams(position: IDialog.Position): Bundle =
            Bundle().apply {
                putSerializable(POSITION, position)
            }
    }

    init {
        isCancelable = false
    }

    abstract val layout: Int

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // remove title space
        setStyle(STYLE_NO_TITLE, android.R.style.Theme_Dialog)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val position = arguments?.getSerializable(POSITION) ?: IDialog.Position.Center
        if (position == IDialog.Position.Bottom) {
            configureBottomDialog()
        }
        return inflater.inflate(layout, container)
    }

    private fun configureBottomDialog() {
        dialog?.window?.apply {

            // move dialog window to the bottom
            setGravity(Gravity.CENTER_HORIZONTAL or Gravity.BOTTOM)

            decorView.apply {

                // remove dialog corner radius
                setBackgroundColor(Color.WHITE)

                // make dialog as wide as TV screen
                val displayMetrics = Resources.getSystem().displayMetrics
                minimumWidth = displayMetrics.widthPixels
            }

            // make dialog content match parent
            setLayout(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT)
        }

    }

    override fun show(fragmentManager: FragmentManager) {
        show(fragmentManager, null)
    }
}