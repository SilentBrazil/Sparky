package com.silent.sparky

import android.app.Application
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.multidex.MultiDex
import cat.ereza.customactivityoncrash.config.CaocConfig
import com.google.firebase.firestore.ktx.firestoreSettings
import com.silent.core.notifications.SparkyNotificationChannel
import com.silent.sparky.di.appModule
import com.silent.sparky.features.ErrorActivity
import com.silent.sparky.features.cuts.di.cutsModule
import com.silent.sparky.features.home.di.homeModule
import com.silent.sparky.features.podcast.di.podcastModule
import com.silent.sparky.features.profile.di.profileModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.loadKoinModules
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class SparkyApplication : Application() {


    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger(Level.ERROR)
            androidContext(applicationContext)
            modules(appModule)

        }
        CaocConfig.Builder.create()
            .backgroundMode(CaocConfig.BACKGROUND_MODE_SILENT)
            .trackActivities(true)
            .errorActivity(ErrorActivity::class.java).apply()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(SparkyNotificationChannel.createChannel(applicationContext))
        }

    }



    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }
}