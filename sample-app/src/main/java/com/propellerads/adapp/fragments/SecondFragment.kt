package com.propellerads.adapp.fragments

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.propellerads.adapp.databinding.FragmentSecondBinding
import com.propellerads.sdk.widget.PropellerBannerRequest

class SecondFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentSecondBinding.inflate(inflater, container, false)

        binding.btn.setOnClickListener {
            AlertFragment()
                .show(childFragmentManager, null)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            PropellerBannerRequest(
                "interstitial_test",
                lifecycle,
                childFragmentManager,
                "second_fragment"
            ) {}
        }

        return binding.root
    }
}