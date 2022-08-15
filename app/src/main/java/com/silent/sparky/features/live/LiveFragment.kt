package com.silent.sparky.features.live

import android.animation.ValueAnimator
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.options.IFramePlayerOptions
import com.silent.core.podcast.Podcast
import com.silent.core.utils.ImageUtils
import com.silent.sparky.R
import com.silent.sparky.databinding.PodcastLiveFragmentBinding
import com.silent.sparky.features.live.components.SparkyPlayerController
import java.text.NumberFormat


class LiveFragment : Fragment() {

    private var podcastLiveFragmentBinding: PodcastLiveFragmentBinding? = null
    private val args by navArgs<LiveFragmentArgs>()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        podcastLiveFragmentBinding = PodcastLiveFragmentBinding.inflate(inflater)
        return podcastLiveFragmentBinding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        podcastLiveFragmentBinding = PodcastLiveFragmentBinding.bind(view)
        setupView()
    }

    private fun setupView() {
        podcastLiveFragmentBinding?.run {
            val live = args.liveObject
            initializePlayer(
                Color.parseColor(live.podcast.highLightColor),
                live.videoID,
                ImageUtils.getNotificationIcon(live.podcast.notificationIcon).drawable,
                args.liveObject.isLiveVideo
            )
            setupPodcast(live.podcast)
            liveTitle.text = live.title
            liveDescription.text = live.description
            collapseButton.setOnClickListener {
                findNavController().popBackStack()
            }
            if (live.isLiveVideo) {
                liveCard.setStrokeColor(ColorStateList.valueOf(Color.parseColor(live.podcast.highLightColor)))
            }
        }
    }

    private fun PodcastLiveFragmentBinding.setupPodcast(podcast: Podcast) {
        podcastName.text = podcast.name
        Glide.with(requireContext()).load(podcast.iconURL).into(podcastIcon)
        podcastName.setOnClickListener {
            val bundle = bundleOf("podcast_id" to podcast.id, "live_video" to null)
            findNavController().navigate(R.id.action_liveFragment_to_podcastFragment, bundle)
        }
        podcastIcon.setOnClickListener {
            val bundle = bundleOf("podcast_id" to podcast.id, "live_video" to null)
            findNavController().navigate(R.id.action_liveFragment_to_podcastFragment, bundle)
        }
        podcastSubscribers.text = "${podcast.subscribe} inscritos."
        val animator = ValueAnimator()
        animator.run {
            setObjectValues(0, podcast.subscribe)
            addUpdateListener {
                podcastSubscribers.text =
                    NumberFormat.getInstance()
                        .format(it.animatedValue.toString().toInt()) + " inscritos"
            }
            duration = 2000
            start()
        }
        podcastIcon.borderColor = Color.parseColor(podcast.highLightColor)
    }

    private fun PodcastLiveFragmentBinding.initializePlayer(
        highlightColor: Int,
        videoId: String,
        podcastIcon: Int,
        isLive: Boolean
    ) {
        val playerUi = livePlayer.inflateCustomPlayerUi(R.layout.custom_player_layout)
        val listener = object : AbstractYouTubePlayerListener() {
            override fun onReady(youTubePlayer: YouTubePlayer) {
                super.onReady(youTubePlayer)
                val customplayerUiController = SparkyPlayerController(
                    playerUi,
                    livePlayer,
                    youTubePlayer,
                    highlightColor,
                    podcastIcon,
                    videoId,
                    isLive
                )
                youTubePlayer.addListener(customplayerUiController)
            }
        }
        val options: IFramePlayerOptions = IFramePlayerOptions.Builder()
            .controls(0).build()
        livePlayer.initialize(listener, false, options)
    }

    override fun onDetach() {
        super.onDetach()
        podcastLiveFragmentBinding?.livePlayer?.release()
    }

    override fun onDestroy() {
        super.onDestroy()
        podcastLiveFragmentBinding?.livePlayer?.release()
    }
}