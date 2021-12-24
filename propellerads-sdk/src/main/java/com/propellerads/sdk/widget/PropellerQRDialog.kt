package com.propellerads.sdk.widget

import com.propellerads.sdk.ui.QRDialog
import com.propellerads.sdk.ui.base.IDialog

interface PropellerQRDialog : IDialog {

    companion object {
        fun prepare(position: IDialog.Position = IDialog.Position.Bottom): PropellerQRDialog =
            QRDialog.newInstance(position)
    }
}
