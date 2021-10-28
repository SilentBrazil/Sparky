package com.silent.sparky.program.adapter

import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.silent.core.utils.WebUtils
import com.silent.core.youtube.PlaylistResource
import com.silent.ilustriscore.core.utilities.DateFormats
import com.silent.ilustriscore.core.utilities.formatDate
import com.silent.sparky.R
import com.silent.sparky.databinding.VideoPreviewBinding

class VideosAdapter(val playlistVideos: List<PlaylistResource>): RecyclerView.Adapter<VideosAdapter.VideoViewHolder>() {

    inner class VideoViewHolder(val videoPreviewsBinding: VideoPreviewBinding): RecyclerView.ViewHolder(videoPreviewsBinding.root) {

        fun bind() {
            videoPreviewsBinding.run {
                val video = playlistVideos[adapterPosition].snippet
                videoCard.setOnClickListener {
                    WebUtils(root.context).openYoutubeVideo(video.resourceId.videoId)
                }
                try {
                    Glide.with(root.context).load(video.thumbnails.standard.url).into(videoThumb)
                } catch (e: Exception) {
                    e.printStackTrace()
                    Log.e(javaClass.simpleName, "bind: erro ao carregar thumbnail de vídeo $video" )
                }
                title.text = video.title
                publishDate.text = video.publishedAt.formatDate(video.publishedAt, DateFormats.DD_OF_MM_FROM_YYYY.format)
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
        val bind: VideoPreviewBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.video_preview, parent,false)
        return VideoViewHolder(bind)
    }

    override fun onBindViewHolder(holder: VideoViewHolder, position: Int) {
        holder.bind()
    }

    override fun getItemCount(): Int = playlistVideos.count()


}