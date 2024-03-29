package com.silent.core.podcast

import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties
import com.google.gson.annotations.SerializedName
import com.silent.ilustriscore.core.bean.BaseBean
import java.io.Serializable

typealias podcasts = ArrayList<Podcast>

@IgnoreExtraProperties
data class Podcast(
    @Exclude
    var key: String = "",
    var name: String = "",
    var subscribe: Int = 0,
    var iconURL: String = "",
    var views: Int = 0,
    var hosts: ArrayList<Host> = ArrayList(),
    var youtubeID: String = "",
    var cuts: String = "",
    var uploads: String = "",
    var highLightColor: String = "#ff212121",
    var cover: String? = "",
    var notificationIcon: String = "",
    var slogan: String = "",
    var liveTime: Int = 0,
    @Exclude
    var updating: Boolean = false,
    @Exclude
    var isLive: Boolean = false,
    var subscribers: ArrayList<String> = ArrayList()
) : Serializable, BaseBean(key) {

    init {
        key = id
    }

    companion object {
        val newPodcast = Podcast(key = NEW_PODCAST, name = "Adicionar podcast")
    }
}

const val NEW_PODCAST = "NEWPODCAST"
const val NEW_HOST = "NEWHOST"

data class Host(
    var name: String = "",
    var profilePic: String = "",
    @SerializedName("user")
    var description: String = "",
    var socialUrl: String = "",
) : Serializable {

    companion object {
        val NEWHOST = Host(NEW_HOST)
    }
}