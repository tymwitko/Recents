package com.tymwitko.recents.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
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
            context?.let { RecentAppsAdapter(it, viewModel.getActiveApps(it)) }
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        binding.recyclerView.setHasFixedSize(true)
    }
}