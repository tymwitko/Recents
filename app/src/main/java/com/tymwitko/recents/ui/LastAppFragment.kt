package com.tymwitko.recents.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.tymwitko.recents.databinding.FragmentLastAppBinding
import com.tymwitko.recents.viewmodels.LastAppViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.component.KoinComponent

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
            context?.packageName?.let { it1 ->
                // viewModel.launchLastApp(context::startActivity, it1)
            }
            activity?.finish()
        }
    }
}