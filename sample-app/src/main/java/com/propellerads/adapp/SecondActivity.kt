package com.propellerads.adapp

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.propellerads.adapp.databinding.ActivitySecondBinding
import com.propellerads.adapp.fragments.FirstFragment

class SecondActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ActivitySecondBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btn.setOnClickListener {

            val nextFragment = FirstFragment()

            supportFragmentManager
                .beginTransaction()
                .replace(R.id.container, nextFragment, null)
                .addToBackStack(null)
                .commit()

            binding.btn.visibility = View.GONE
        }
    }
}