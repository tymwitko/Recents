package com.tymwitko.recents

import androidx.appcompat.app.AppCompatActivity
import com.tymwitko.recents.viewmodels.LastAppViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class LastAppActivity : AppCompatActivity() {
    private val viewModel by viewModel<LastAppViewModel>()

    override fun onResume() {
        super.onResume()
        viewModel.launchLastApp(::startActivity,  packageName)
    }
}