package com.silent.sparky.features.podcast.adapter

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ilustris.animations.fadeIn
import com.silent.core.utils.WebUtils
import com.silent.core.videos.Video
import com.silent.ilustriscore.core.utilities.gone
import com.silent.sparky.R
import com.silent.sparky.databinding.VideoPreviewBinding

class VideosAdapter(
    val playlistVideos: List<Video>,
    private val highlightColor: String? = null
) : RecyclerView.Adapter<VideosAdapter.VideoViewHolder>() {

    inner class VideoViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        fun bind() {
            val video = playlistVideos[bindingAdapterPosition]
            VideoPreviewBinding.bind(itemView).run {
                highlightColor?.let {
                    videoCard.setStrokeColor(ColorStateList.valueOf(Color.parseColor(it)))
                }
                videoCard.setOnClickListener {
                    WebUtils(itemView.context).openYoutubeVideo(video.youtubeID)
                }
                Glide.with(itemView.context).load(video.thumbnailUrl)
                    .error(R.drawable.ic_iconmonstr_connection_1).into(videoThumb)
                title.text = video.title
                try {
                    /*val date = LocalDate.parse(video.publishedAt, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                            .format(DateTimeFormatter.ofPattern("YYYY-MM-DDThh:mm:ss.sZ"))*/
                    //publishDate.text = date
                } catch (e: Exception) {
                    publishDate.gone()
                }
                root.fadeIn()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.video_preview, parent, false)
        return VideoViewHolder(view)
    }

    override fun onBindViewHolder(holder: VideoViewHolder, position: Int) {
        holder.bind()
    }

    override fun getItemCount(): Int = playlistVideos.count()


}