package com.silent.sparky.features.podcast

import android.animation.ValueAnimator
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.ilustris.animations.*
import com.ilustris.ui.extensions.ERROR_COLOR
import com.ilustris.ui.extensions.showSnackBar
import com.silent.core.component.GroupType
import com.silent.core.component.HostGroup
import com.silent.core.component.HostGroupAdapter
import com.silent.core.podcast.Host
import com.silent.core.podcast.Podcast
import com.silent.core.utils.WebUtils
import com.silent.ilustriscore.core.model.ViewModelBaseState
import com.silent.sparky.R
import com.silent.sparky.databinding.FragmentPodcastBinding
import com.silent.sparky.features.home.adapter.VideoHeaderAdapter
import com.silent.sparky.features.home.data.LiveHeader
import com.silent.sparky.features.home.data.PodcastHeader
import com.silent.sparky.features.podcast.schedule.PodcastScheduleDialog
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.text.NumberFormat

class PodcastFragment : Fragment() {
    private var podcastFragmentBinding: FragmentPodcastBinding? = null
    private val podcastViewModel by viewModel<PodcastViewModel>()
    private val args by navArgs<PodcastFragmentArgs>()
    private var program: Podcast? = null


    override fun onDestroy() {
        super.onDestroy()
        clearFragment()
    }

    override fun onDetach() {
        super.onDetach()
        clearFragment()
    }

    private fun clearFragment() {
        podcastViewModel.viewModelState.removeObservers(this)
        podcastViewModel.podcastState.removeObservers(this)
        podcastViewModel.scheduleState.removeObservers(this)
        podcastViewModel.clearState()
        podcastFragmentBinding = null
    }

    private fun onSelectHeader(podcastHeader: PodcastHeader) {
        findNavController().navigate(
            R.id.action_podcastFragment_to_playlistFragment,
            bundleOf("header" to podcastHeader)
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        podcastFragmentBinding = FragmentPodcastBinding.inflate(inflater)
        return podcastFragmentBinding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeViewModel()
    }

    override fun onStart() {
        super.onStart()
        podcastViewModel.getPodcastData(args.podcastId)
    }

    private fun animateSubscriberCount(count: Int) {
        val animator = ValueAnimator()
        animator.run {
            setObjectValues(0, count)
            addUpdateListener {
                podcastFragmentBinding?.subscriberCount?.text =
                    NumberFormat.getInstance().format(it.animatedValue.toString().toInt())
            }
            duration = 10000
            start()
        }

    }

    private fun setupPodcast(podcast: Podcast, headers: ArrayList<PodcastHeader>, isFavorite: Boolean) {
        program = podcast
        podcastFragmentBinding?.run {
            loading.popOut()
            (activity as AppCompatActivity?)?.run {
                setSupportActionBar(programToolbar)
                supportActionBar?.setDisplayHomeAsUpEnabled(true)
                programToolbar.setNavigationOnClickListener {
                    findNavController().popBackStack()
                }
            }
            programName.text = podcast.name
            if (podcast.highLightColor.isNotEmpty()) {
                programIcon.borderColor = Color.parseColor(podcast.highLightColor)
            }
            Glide.with(requireContext()).load(podcast.iconURL).into(programIcon)
            Glide.with(requireContext()).load(podcast.cover).into(podcastCover)
            programIcon.setOnLongClickListener { _ ->
                WebUtils(requireContext()).openYoutubeChannel(podcast.youtubeID)
                false
            }
            val hostHeaders = ArrayList<HostGroup>()
            if (podcast.hosts.isNotEmpty()) {
                hostHeaders.add(HostGroup("Hosts", podcast.hosts))
            }
            if (podcast.weeklyGuests.isNotEmpty()) {
                hostHeaders.add(
                    HostGroup(
                        "Próximos convidados",
                        podcast.weeklyGuests,
                        GroupType.GUESTS
                    )
                )
            }
            hostsRecyclerView.adapter = HostGroupAdapter(hostHeaders, false, { host, groupType ->

                    when (groupType) {
                        GroupType.HOSTS -> {
                            if (host.socialUrl.isNotEmpty()) {
                                WebUtils(requireContext()).openInstagram(host.socialUrl)
                            }
                        }
                        GroupType.GUESTS -> {
                            if (host.socialUrl.isNotEmpty()) {
                                val liveHeader = LiveHeader(podcast, host.name, host.socialUrl)
                                val bundle = bundleOf("live_object" to liveHeader)
                                findNavController().navigate(
                                    R.id.action_podcastFragment_to_liveFragment,
                                    bundle
                                )
                            }
                        }
                    }


                }, podcast.highLightColor)
            appBar.slideInBottom()
            channelVideos.slideInRight()
            animateSubscriberCount(podcast.subscribe)
            setupHeaders(headers)
            if (podcast.weeklyGuests.isNotEmpty()) {
                podcastViewModel.checkSchedule(podcast)
            }
            favoritePodcast.backgroundTintList = ColorStateList.valueOf(Color.parseColor(podcast.highLightColor))
            favoritePodcast.isChecked = isFavorite
            favoritePodcast.setOnCheckedChangeListener { buttonView, isChecked ->
                podcastViewModel.favoritePodcast(podcast.id, isChecked)
            }
            podcastSearch.setOnSearchClickListener {
                podcastViewModel.searchEpisodesAndCuts(podcast, podcastSearch.query.toString())
            }
            podcastSearch.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    query?.let { podcastViewModel.searchEpisodesAndCuts(podcast, it) } ?: podcastViewModel.getPodcastData(args.podcastId)
                    return false
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                   return false
                }

            })
            val closeButton: View? = podcastSearch.findViewById(androidx.appcompat.R.id.search_close_btn)
            closeButton?.setOnClickListener {
                podcastSearch.setQuery("", false)
                podcastViewModel.getPodcastData(args.podcastId)
            }
        }
    }

    private fun navigateToLive(podcast: Podcast, host: Host) {
        val liveHeader = LiveHeader(podcast, host.name, host.socialUrl)
        val bundle = bundleOf("live_object" to liveHeader)
        findNavController().navigate(R.id.action_podcastFragment_to_liveFragment, bundle)
    }

    private fun FragmentPodcastBinding.showLoading() {
        loading.fadeIn()
        appBar.fadeOut()
        channelVideos.fadeOut()
    }

    private fun observeViewModel() {
        podcastViewModel.podcastState.observe(viewLifecycleOwner) {
            when (it) {
                is PodcastViewModel.PodcastState.PodcastDataRetrieved -> {
                    setupPodcast(it.podcast, it.headers, it.isFavorite)
                }
                PodcastViewModel.PodcastState.PodcastFailedState -> {
                    requireView().showSnackBar("Ocorreu um erro ao obter os vídeos")
                    podcastFragmentBinding?.loading?.fadeOut()
                }
                is PodcastViewModel.PodcastState.RetrieveSearch -> {
                    setupHeaders(it.headers)
                }
            }
        }
        podcastViewModel.scheduleState.observe(viewLifecycleOwner) {
            when (it) {
                is PodcastViewModel.ScheduleState.TodayGuestState -> {
                    PodcastScheduleDialog(
                        requireContext(),
                        it.position,
                        it.guests,
                        it.podcast.highLightColor
                    ) { host ->
                        navigateToLive(it.podcast, host)
                    }.buildDialog()
                }
            }
        }
        podcastViewModel.viewModelState.observe(viewLifecycleOwner) {
            when (it) {
                ViewModelBaseState.LoadingState -> {
                    podcastFragmentBinding?.showLoading()
                }
                is ViewModelBaseState.ErrorState -> {
                    requireView().showSnackBar(
                        "Ocorreu ao obter dados do podcast",
                        backColor = ContextCompat.getColor(requireContext(), ERROR_COLOR)
                    )
                }
            }
        }
    }

    private fun setupHeaders(headers: ArrayList<PodcastHeader>) {
       podcastFragmentBinding?.channelVideos?.adapter = VideoHeaderAdapter(headers, ::onSelectHeader)
    }


}