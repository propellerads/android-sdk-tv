package com.propellerads.adapp

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.propellerads.adapp.databinding.ActivityMainBinding
import com.propellerads.sdk.widget.PropellerBannerRequest

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        PropellerBannerRequest(
            "qr_code_1",
            lifecycle,
            supportFragmentManager
        ) { isShow ->
            val label = "${if (isShow) "Show" else "Hide"} banner callback (First Activity)"
            Toast.makeText(this, label, Toast.LENGTH_SHORT).show()
        }

        binding.next.setOnClickListener {
            val intent = Intent(this, SecondActivity::class.java)
            startActivity(intent)
        }
    }
}