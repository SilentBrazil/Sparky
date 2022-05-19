package com.silent.sparky.data

import androidx.recyclerview.widget.RecyclerView
import com.silent.core.videos.Video

typealias programSections = ArrayList<PodcastHeader>

data class PodcastHeader(
    val title: String,
    val icon: String? = null,
    val highLightColor: String? = null,
    val videos: List<Video>,
    val playlistId: String,
    val channelURL: String? = null,
    val orientation: Int = RecyclerView.VERTICAL,
    val seeMore: Boolean = false,
    val scrollAnimation: Boolean = false
)
