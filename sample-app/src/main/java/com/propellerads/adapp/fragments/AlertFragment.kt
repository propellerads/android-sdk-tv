package com.propellerads.adapp.fragments

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment

class AlertFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        AlertDialog.Builder(requireContext())
            .setTitle("Hello World!")
            .setMessage("It is the Alert Dialog")
            .setPositiveButton("Ok") { _, _ -> }
            .create()

}