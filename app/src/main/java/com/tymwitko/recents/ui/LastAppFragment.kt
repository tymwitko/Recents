package com.tymwitko.recents.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.tymwitko.recents.databinding.FragmentLastAppBinding
import com.tymwitko.recents.viewmodels.LastAppViewModel
import org.koin.core.component.KoinComponent
import org.koin.androidx.viewmodel.ext.android.viewModel

class LastAppFragment : Fragment(), KoinComponent {

    private val viewModel by viewModel<LastAppViewModel>()
    private lateinit var binding: FragmentLastAppBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLastAppBinding.inflate(inflater, container, false)
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