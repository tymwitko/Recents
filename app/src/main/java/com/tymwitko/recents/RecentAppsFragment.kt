package com.tymwitko.recents

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.tymwitko.recents.databinding.FragmentRecentAppsBinding
import org.koin.core.component.KoinComponent
import org.koin.androidx.viewmodel.ext.android.viewModel

class RecentAppsFragment : Fragment(), KoinComponent {

    private val viewModel by viewModel<RecentAppsViewModel>()
    private lateinit var binding: FragmentRecentAppsBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRecentAppsBinding.inflate(inflater, container, false)
        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        context?.let {
            viewModel.launchLastApp(it)
            activity?.finish()
        }
    }
}