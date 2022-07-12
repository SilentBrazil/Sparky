package com.silent.sparky.features.podcast

import android.app.Application
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.RecyclerView
import com.silent.core.podcast.Host
import com.silent.core.podcast.Podcast
import com.silent.core.podcast.PodcastService
import com.silent.core.videos.CutService
import com.silent.core.videos.Video
import com.silent.core.videos.VideoService
import com.silent.ilustriscore.core.model.BaseViewModel
import com.silent.sparky.features.home.data.PodcastHeader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*


class PodcastViewModel(
    application: Application,
    override val service: PodcastService,
    private val videoService: VideoService,
    private val cutService: CutService
) : BaseViewModel<Podcast>(application) {

    sealed class PodcastState {
        object PodcastFailedState : PodcastState()
        data class PodcastDataRetrieved(
            val podcast: Podcast,
            val headers: ArrayList<PodcastHeader>
        ) : PodcastState()

        data class UpdateHeader(val position: Int, val videos: List<Video>, val lastIndex: Int) :
            PodcastState()
    }

    sealed class ScheduleState {
        data class TodayGuestState(
            val guests: List<Host>,
            val position: Int,
            val podcast: Podcast
        ) : ScheduleState()
    }

    val channelState = MutableLiveData<PodcastState>()
    val scheduleState = MutableLiveData<ScheduleState>()

    private fun getHeader(
        title: String,
        playlistId: String,
        videos: List<Video>,
        orientation: Int,
        highlightColor: String,
        subtitle: String
    ) = PodcastHeader(
        title = title,
        playlistId = playlistId,
        videos = ArrayList(videos),
        orientation = orientation,
        highLightColor = highlightColor,
        subTitle = subtitle
    )

    fun getChannelData(podcastID: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val headers = ArrayList<PodcastHeader>()
                val podcast = service.getSingleData(podcastID).success.data as Podcast
                val uploads = videoService.getPodcastVideos(podcastID).success.data as ArrayList<Video>
                val cuts = cutService.getPodcastCuts(podcastID).success.data as ArrayList<Video>

                if (uploads.isNotEmpty()) {
                    uploads.map { it.podcast = podcast }
                    headers.add(
                        getHeader(
                            "Episódios",
                            podcast.uploads,
                            uploads.sortedByDescending { it.publishedAt },
                            if (cuts.isEmpty()) RecyclerView.VERTICAL else RecyclerView.HORIZONTAL,
                            podcast.highLightColor,
                            podcast.name
                        )
                    )
                }
                if (cuts.isNotEmpty()) {
                    cuts.map { it.podcast = podcast }
                    headers.add(
                        getHeader(
                            "Cortes do ${podcast.name}",
                            podcast.cuts,
                            cuts.sortedByDescending { it.publishedAt },
                            RecyclerView.VERTICAL,
                            podcast.highLightColor,
                            podcast.name
                        )
                    )
                }
                val sortedGuests = podcast.weeklyGuests.sortedBy { it.comingDate }
                podcast.weeklyGuests = ArrayList(sortedGuests)
                channelState.postValue(
                    PodcastState.PodcastDataRetrieved(
                        podcast,
                        headers
                    )
                )
            } catch (e: Exception) {
                e.printStackTrace()
                channelState.postValue(PodcastState.PodcastFailedState)
            }
        }
    }

    fun checkSchedule(podcast: Podcast) {
        val calendar = Calendar.getInstance()
        podcast.weeklyGuests.forEachIndexed { index, host ->
            val hostCalendar = Calendar.getInstance()
            hostCalendar.time = host.comingDate
            if (hostCalendar[Calendar.DAY_OF_MONTH] == calendar[Calendar.DAY_OF_MONTH]) {
                scheduleState.postValue(
                    ScheduleState.TodayGuestState(
                        podcast.weeklyGuests,
                        index,
                        podcast
                    )
                )
            }
        }
    }

}