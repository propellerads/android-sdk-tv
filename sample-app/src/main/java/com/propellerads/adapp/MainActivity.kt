package com.propellerads.adapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.propellerads.adapp.databinding.ActivityMainBinding
import com.propellerads.sdk.widget.PropellerBannerRequest

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        PropellerBannerRequest(
            "test_banner",
            lifecycle,
            supportFragmentManager
        )
    }
}