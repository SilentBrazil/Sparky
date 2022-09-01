package com.silent.sparky.features.home.viewmodel

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import com.silent.core.firebase.FirebaseService
import com.silent.core.podcast.Podcast
import com.silent.core.preferences.PreferencesService
import com.silent.core.utils.TOKEN_PREFERENCES
import com.silent.core.videos.Video
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActViewModel(
    application: Application,
    private val firebaseService: FirebaseService,
    private val preferencesService: PreferencesService
) : AndroidViewModel(application) {

    sealed class MainActState {
        data class RetrieveToken(val token: String) : MainActState()
        data class EnteredFullScreen(val isFullScreen: Boolean) : MainActState()
        object RequireLoginState : MainActState()
        object LoginSuccessState : MainActState()
        object LoginErrorState : MainActState()
    }

    sealed class NotificationState {
        data class NavigateToPodcastPush(val podcastId: String, val liveVideo: Video?) :
            NotificationState()

        object NotificationOpened : NotificationState()
        data class RequestNotificationPermission(val permission: String) : NotificationState()
        object NotificationGranted : NotificationState()
        object NotificationRevoked : NotificationState()
    }

    val actState = MutableLiveData<MainActState>()
    val notificationState = MutableLiveData<NotificationState>()

    fun updateState(mainActState: MainActState) {
        actState.value = mainActState
    }

    fun updateNotificationPermission(permissionStatus: Boolean = false) {
        if (permissionStatus) {
            notificationState.value = NotificationState.NotificationGranted
            viewModelScope.launch {
                firebaseService.subscribeToTopic {}
            }
        } else {
            notificationState.value = NotificationState.NotificationRevoked
        }
    }

    fun checkNotifications() {
        val permission = if (Build.VERSION.SDK_INT >= 33) {
            Manifest.permission.POST_NOTIFICATIONS
        } else {
            Manifest.permission.ACCESS_NOTIFICATION_POLICY
        }
        val permissionStatus = ContextCompat.checkSelfPermission(
            getApplication<Application>().applicationContext,
            permission
        ) != PackageManager.PERMISSION_GRANTED
        if (!permissionStatus) {
            notificationState.value = NotificationState.RequestNotificationPermission(permission)
        } else {
            updateNotificationPermission(permissionStatus)
        }
    }

    fun checkToken() {
        viewModelScope.launch(Dispatchers.IO) {
            val token = firebaseService.generateFirebaseToken()
            if (token.isSuccess) {
                preferencesService.editPreference(TOKEN_PREFERENCES, token.success.data)
            }
        }
    }

    fun validatePush(podcastExtra: Podcast?, videoExtra: String?) {
        podcastExtra?.let {
            var video: Video? = null
            try {
                video = Gson().fromJson(videoExtra, Video::class.java)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            Log.i(javaClass.simpleName, "validatePush: payload -> ${podcastExtra} $videoExtra")
            notificationState.value =
                NotificationState.NavigateToPodcastPush(podcastExtra.id, video)
        }
    }

    fun notificationOpen() {
        notificationState.value = NotificationState.NotificationOpened
    }

    fun checkUser() {
        if (FirebaseAuth.getInstance().currentUser == null) {
            updateState(MainActState.RequireLoginState)
        } else {
            updateState(MainActState.LoginSuccessState)
        }
    }

}