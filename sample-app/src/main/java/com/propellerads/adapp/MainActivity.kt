package com.propellerads.adapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.propellerads.adapp.databinding.ActivityMainBinding
import com.propellerads.sdk.ui.base.IDialog
import com.propellerads.sdk.widget.PropellerQRDialog

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val qrDialog = PropellerQRDialog
            .prepare(IDialog.Position.Bottom)

        binding.qrDialogTestBtn.setOnClickListener {
            qrDialog.show(supportFragmentManager)
        }
    }
}

