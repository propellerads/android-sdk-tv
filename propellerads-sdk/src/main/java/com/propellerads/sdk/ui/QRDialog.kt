package com.propellerads.sdk.ui

import com.propellerads.sdk.R
import com.propellerads.sdk.ui.base.BaseDialog
import com.propellerads.sdk.ui.base.IDialog
import com.propellerads.sdk.widget.PropellerQRDialog

internal class QRDialog : BaseDialog(), PropellerQRDialog {

    companion object {
        fun newInstance(position: IDialog.Position): PropellerQRDialog {
            return QRDialog().apply {
                composeParams(
                    position,
                ).let { bundle -> arguments = bundle }
            }
        }
    }

    override val layout: Int = R.layout.propeller_qr_dialog
}