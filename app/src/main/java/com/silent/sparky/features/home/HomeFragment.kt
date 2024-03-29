package com.silent.sparky.features.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ilustris.animations.fadeIn
import com.ilustris.animations.fadeOut
import com.silent.core.podcast.HeaderType
import com.silent.core.podcast.Podcast
import com.silent.core.podcast.PodcastHeader
import com.silent.core.utils.WebUtils
import com.silent.core.videos.Video
import com.silent.ilustriscore.core.model.ErrorType
import com.silent.ilustriscore.core.model.ViewModelBaseState
import com.silent.ilustriscore.core.utilities.delayedFunction
import com.silent.navigation.ModuleNavigator
import com.silent.navigation.NavigationUtils
import com.silent.sparky.R
import com.silent.sparky.databinding.HomeFragmentBinding
import com.silent.sparky.features.home.adapter.VideoHeaderAdapter
import com.silent.sparky.features.home.viewmodel.*
import com.silent.sparky.features.live.data.LiveHeader
import com.silent.sparky.features.live.data.VideoMedia
import com.silent.sparky.features.notifications.NotificationActivity
import com.silent.sparky.features.profile.dialog.PREF_TAG
import com.silent.sparky.features.profile.dialog.PreferencesDialogFragment
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class HomeFragment : SearchView.OnQueryTextListener, Fragment() {

    var preferencesDialogFragment: PreferencesDialogFragment? = null
    var homeFragmentBinding: HomeFragmentBinding? = null
    private val homeViewModel: HomeViewModel by viewModel()
    private val mainActViewModel by sharedViewModel<MainActViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setMenuVisibility(false)
        homeFragmentBinding = HomeFragmentBinding.inflate(inflater)
        return homeFragmentBinding?.root
    }

    override fun onStart() {
        super.onStart()
        observeViewModel()
        setupView()
        homeViewModel.getHome()
    }

    private fun openPodcast(id: String, video: String? = null) {
        val bundle = bundleOf("podcast_id" to id, "video_id" to video)
        findNavController().navigate(R.id.action_navigation_home_to_podcastFragment, bundle)
    }

    private fun openChannel(url: String) {
        WebUtils(requireContext()).openYoutubeChannel(url)
    }

    private fun setupView() {
        homeFragmentBinding?.run {
            (requireActivity() as AppCompatActivity?)?.run {
                setSupportActionBar(homeToolbar)
                supportActionBar?.title = ""
            }
            homeSearch.setQuery("", false)
            homeSearch.setOnQueryTextListener(this@HomeFragment)
            homeSearch.setOnCloseListener {
                homeViewModel.getHome()
                return@setOnCloseListener false
            }
            homeSearch.setOnSearchClickListener {
                homeViewModel.searchPodcastAndEpisodes(homeSearch.query.toString())
            }
            val closeButton: View? =
                homeSearch.findViewById(androidx.appcompat.R.id.search_close_btn)
            closeButton?.setOnClickListener {
                homeSearch.setQuery("", false)
                homeViewModel.getHome()
            }

            notificationAnimation.setOnClickListener {
                val intent = Intent(requireContext(), NotificationActivity::class.java)
                requireActivity().startActivity(intent)
            }
        }
    }

    private fun goToManager() {
        NavigationUtils(requireContext()).startModule(ModuleNavigator.MANAGER)
    }



    private fun observeViewModel() {
        homeViewModel.preferencesState.observe(viewLifecycleOwner) {
            when (it) {
                PreferencesState.PreferencesNotSet -> {
                    if (preferencesDialogFragment == null) {
                        preferencesDialogFragment = PreferencesDialogFragment.buildDialog {
                            homeViewModel.getHome()
                        }
                        preferencesDialogFragment?.show(childFragmentManager, PREF_TAG)
                    }

                }
                PreferencesState.WarningNotShowed -> {
                    homeFragmentBinding?.warningView?.root?.fadeIn()
                    homeViewModel.updateWarning()
                }
                PreferencesState.PreferencesDone -> {
                    homeFragmentBinding?.warningView?.root?.fadeOut()
                }
            }
        }
        homeViewModel.homeState.observe(viewLifecycleOwner) {
            when (it) {
                is HomeState.HomeChannelsRetrieved -> {
                    setupHome(it.podcastHeaders)
                }
                HomeState.HomeError -> {
                    homeFragmentBinding?.showError("Ocorreu um erro inesperado ao carregar.") {
                        homeViewModel.getAllData()
                    }

                }

                HomeState.InvalidManager -> {
                    setMenuVisibility(false)
                }
                HomeState.ValidManager -> {
                    setMenuVisibility(true)
                    homeFragmentBinding?.homeAnimation?.setOnClickListener {
                        goToManager()
                    }
                }

                HomeState.LoadingSearch -> {
                    homeFragmentBinding?.run {
                        showLoading()
                    }
                }
                is HomeState.HomeSearchRetrieved -> {
                    if (it.podcastHeader.isNotEmpty()) {
                        setupHome(it.podcastHeader)
                    } else {
                        homeFragmentBinding?.showError("Nenhum resultado encontrado para sua busca.") {
                            homeFragmentBinding?.homeSearch?.setQuery("", false)
                            homeViewModel.getHome()
                        }
                    }
                }
                else -> {}
            }
        }
        homeViewModel.viewModelState.observe(viewLifecycleOwner) {
            when (it) {
                ViewModelBaseState.LoadingState -> {
                    homeFragmentBinding?.showLoading()
                }

                ViewModelBaseState.LoadCompleteState -> {
                    homeFragmentBinding?.stopLoading()
                }

                ViewModelBaseState.RequireAuth -> {
                    if (mainActViewModel.actState.value != MainActViewModel.MainActState.RequireLoginState) {
                        homeFragmentBinding?.showError(getString(R.string.auth_error_message)) {
                            mainActViewModel.updateState(MainActViewModel.MainActState.RequireLoginState)
                        }
                    }

                }
                is ViewModelBaseState.DataListRetrievedState -> {

                }
                is ViewModelBaseState.ErrorState -> {
                    if (it.dataException.code == ErrorType.AUTH) {
                        homeFragmentBinding?.showError("Você precisa estar logado para utilizar o app.") {
                            mainActViewModel.updateState(MainActViewModel.MainActState.RequireLoginState)
                        }
                    } else {
                        homeFragmentBinding?.showError("Ocorreu um erro inesperado(${it.dataException.code.message}") {
                            homeViewModel.getHome()
                        }
                    }
                }
                else -> {}
            }
        }
        homeViewModel.userState.observe(viewLifecycleOwner) {
            when (it) {
                UserState.NewNotificationsState -> {
                    homeFragmentBinding?.notificationAnimation?.playAnimation()
                }
            }
        }
        mainActViewModel.actState.observe(viewLifecycleOwner) {
            when (it) {
                is MainActViewModel.MainActState.LoginSuccessState -> {
                    homeViewModel.getHome()
                }
                else -> {}
            }
        }
        mainActViewModel.notificationState.observe(viewLifecycleOwner) {
            if (it is MainActViewModel.NotificationState.NavigateToPodcastPush) {
                openPodcast(it.podcastId, it.liveVideo)
                mainActViewModel.notificationOpen()
            }
        }

    }

    private fun HomeFragmentBinding.showLoading() {
        homeAnimation.playAnimation()
        homeShimmer.showShimmer(true)
    }

    private fun HomeFragmentBinding.stopLoading() {
        delayedFunction(2500) {
            homeShimmer.stopShimmer()
            homeShimmer.hideShimmer()
        }
    }

    private fun HomeFragmentBinding.showError(message: String, tryAgainClick: () -> Unit) {
        stopLoading()
        errorView.run {
            errorAnimation.playAnimation()
            errorMessage.text = message
            errorButton.setOnClickListener {
                tryAgainClick.invoke()
                root.fadeOut()
            }
            root.fadeIn()
        }

    }

    private fun setupHome(headers: ArrayList<PodcastHeader>) {
        homeFragmentBinding?.run {
            podcastsResumeRecycler.adapter =
                VideoHeaderAdapter(headers, headerSelected = { header ->
                    header.playlistId?.let { id -> openPodcast(id) }
                }, ::openChannel, selectPodcast = {
                    openPodcast(it.id)
                }, { video, podcast, header ->
                    navigateToLive(podcast, video, false)
                })
            podcastsResumeRecycler.layoutManager =
                LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
            stopLoading()
        }
    }

    private fun navigateToLive(
        podcast: Podcast,
        video: Video,
        isLive: Boolean,
        type: HeaderType = HeaderType.VIDEOS
    ) {
        val mediaType = if (isLive) VideoMedia.LIVE else {
            when (type) {
                HeaderType.VIDEOS -> VideoMedia.EPISODE
                HeaderType.CUTS -> VideoMedia.CUT
                else -> VideoMedia.EPISODE
            }
        }
        val liveHeader =
            LiveHeader(
                podcast,
                video,
                mediaType
            )
        val bundle = bundleOf("live_object" to liveHeader)
        findNavController().navigate(R.id.action_navigation_home_to_liveFragment, bundle)
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        query?.let {
            homeViewModel.searchPodcastAndEpisodes(it)
        }
        return false
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        return false
    }
}