package com.silent.manager.features.newpodcast.fragments.hosts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.silent.manager.R
import com.silent.manager.features.newpodcast.NewPodcastViewModel
import com.silent.manager.states.HostState

class HostsFormFragment : Fragment() {

    val newPodcastViewModel: NewPodcastViewModel by lazy {
        ViewModelProvider(requireActivity())[NewPodcastViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_hosts_data, container, false)
    }

    private fun observeViewModel() {
        newPodcastViewModel.hostState.observe(this, {
            when (it) {
                is HostState.HostInstagramRetrieve -> TODO()
            }
        })
    }

}