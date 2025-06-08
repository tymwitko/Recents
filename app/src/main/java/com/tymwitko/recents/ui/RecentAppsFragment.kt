package com.tymwitko.recents.ui

import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.tymwitko.recents.R
import com.tymwitko.recents.databinding.FragmentRecentAppsBinding
import com.tymwitko.recents.exceptions.EmptyAppListException
import com.tymwitko.recents.viewmodels.RecentAppsViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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
        try {
            binding.recyclerView.adapter =
                context?.let {
                    RecentAppsAdapter(
                        it,
                        viewModel.getActiveApps(it),
                        ::killByPackageName
                    )
                }
            binding.recyclerView.layoutManager = LinearLayoutManager(context)
            binding.recyclerView.setHasFixedSize(true)
            binding.kill.setOnClickListener {
                viewModel.killEmAll(context)
            }
        } catch (e: EmptyAppListException) {
            if (ContextCompat.checkSelfPermission(requireContext(), "android.permission.PACKAGE_USAGE_STATS") != PackageManager.PERMISSION_GRANTED)
                binding.errorView.text = getString(R.string.usage_stats_manual)
            else Log.e("TAG", e.stackTraceToString())
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