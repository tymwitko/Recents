package com.tymwitko.recents.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.tymwitko.recents.databinding.FragmentRecentAppsBinding
import com.tymwitko.recents.viewmodels.RecentAppsViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.component.KoinComponent

class RecentAppsFragment : Fragment(), KoinComponent {

    private val viewModel by viewModel<RecentAppsViewModel>()
    private lateinit var binding: FragmentRecentAppsBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRecentAppsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        binding.recyclerView.adapter =
            context?.let { RecentAppsAdapter(it, viewModel.getActiveApps(it), ::killByPackageName) }
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        binding.recyclerView.setHasFixedSize(true)
        binding.kill.setOnClickListener {
            viewModel.killEmAll(context)
        }
    }

    private fun killByPackageName(packageName: String) {
        val packageInfo = context?.packageManager?.getPackageInfo(packageName, 0)
        packageInfo?.let {
            CoroutineScope(Dispatchers.IO).launch {
                val message =
                    if (viewModel.killByPackageInfo(it)) "Killed $packageName"
                    else "Failed to kill $packageName"
                context?.let { it1 ->
                    Snackbar
                        .make(it1, binding.root, message, Snackbar.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }
}