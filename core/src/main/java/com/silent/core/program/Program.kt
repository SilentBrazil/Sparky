package com.silent.core.program

import com.silent.ilustriscore.core.bean.BaseBean
import java.io.Serializable

data class Program(override var id: String = "",
                   var name: String = "",
                   var iconURL: String = "",
                   var hosts: List<Host> = emptyList(),
                   var instagram: String = "",
                   var twitch: String = "",
                   var twitter: String = "",
                   var youtubeID: String = "",
                   var cuts: String = ""): BaseBean(id)

data class Host(val name: String, val profilePic: String) : Serializable