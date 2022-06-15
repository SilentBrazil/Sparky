package com.silent.core.podcast

import android.os.Parcelable
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.gson.annotations.SerializedName
import com.silent.core.videos.Video
import com.silent.ilustriscore.core.bean.BaseBean
import kotlinx.parcelize.Parcelize
import java.util.*

typealias podcasts = ArrayList<Podcast>

@IgnoreExtraProperties
data class Podcast(
    override var id: String = "",
    var name: String = "",
    var subscribe: Int = 0,
    @SerializedName("thumbnail_url")
    var iconURL: String = "",
    var views: Int = 0,
    var hosts: ArrayList<Host> = ArrayList(),
    var weeklyGuests: ArrayList<Host> = ArrayList(),
    var youtubeID: String = "",
    var cuts: String = "",
    var uploads: String = "",
    var highLightColor: String = "#000",
    var cover: String = "",
    @Exclude var liveVideo: Video? = null
) : BaseBean(id) {
    companion object {
        val newPodcast = Podcast(id = NEW_PODCAST, name = "Adicionar podcast")
    }
}

const val NEW_PODCAST = "NEWPODCAST"
const val NEW_HOST = "NEWHOST"

@Parcelize
data class Host(
    var name: String = "",
    var profilePic: String = "",
    @SerializedName("user")
    var description: String = "",
    var socialUrl: String = "",
    var comingDate: Date? = null
) : Parcelable {

    fun isComingToday(): Boolean {
        comingDate?.let {
            val calendar = Calendar.getInstance()
            val dateCalendar = Calendar.getInstance()
            dateCalendar.time = comingDate
            return calendar[Calendar.DAY_OF_MONTH] == dateCalendar[Calendar.DAY_OF_MONTH]
        }
        return false
    }

    fun isLiveHour(): Boolean {
        comingDate?.let {
            val calendar = Calendar.getInstance()
            val dateCalendar = Calendar.getInstance()
            dateCalendar.time = it
            return calendar[Calendar.HOUR_OF_DAY] >= dateCalendar[Calendar.HOUR_OF_DAY]
        }
        return false
    }

    companion object {
        val NEWHOST = Host(NEW_HOST)
    }
}