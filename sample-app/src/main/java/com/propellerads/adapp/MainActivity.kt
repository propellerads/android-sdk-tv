package com.propellerads.adapp

import android.content.Intent
import android.os.Bundle
import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity
import com.propellerads.adapp.databinding.ActivityMainBinding
import com.propellerads.sdk.widget.PropellerBannerRequest

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        WebView.setWebContentsDebuggingEnabled(true)

        PropellerBannerRequest(
            "qr_code_1",
            lifecycle,
            supportFragmentManager
        )

        PropellerBannerRequest(
            "interstitial_test",
            lifecycle,
            supportFragmentManager,
            "main_activity"
        )

        binding.next.setOnClickListener {
            val intent = Intent(this, SecondActivity::class.java)
            startActivity(intent)
        }
    }
}