package com.propellerads.adapp.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.propellerads.adapp.R
import com.propellerads.adapp.databinding.FragmentFirstBinding
import com.propellerads.sdk.widget.PropellerBannerRequest

class FirstFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        PropellerBannerRequest(
            "qr_code_2",
            lifecycle,
            childFragmentManager
        ) { isShow ->
            val label = "${if (isShow) "Show" else "Hide"} banner callback (First Fragment)"
            Toast.makeText(context, label, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentFirstBinding.inflate(inflater, container, false)

        binding.btn.setOnClickListener {
            val nextFragment = SecondFragment()

            parentFragmentManager
                .beginTransaction()
                .replace(R.id.container, nextFragment, null)
                .addToBackStack(null)
                .commit()
        }

        return binding.root
    }
}