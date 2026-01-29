package com.lunacattus.conflux.domain.media

import android.app.PendingIntent
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService

class PlaybackService : MediaSessionService() {

    private var session: MediaSession? = null

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return session
    }

    override fun onCreate() {
        super.onCreate()
        val player = ExoPlayer.Builder(this).build()

        val intent = packageManager.getLaunchIntentForPackage(packageName)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_IMMUTABLE
        )

        session = MediaSession.Builder(this, player)
            .setSessionActivity(pendingIntent)
            .build()
    }

    override fun onDestroy() {
        session?.run {
            release()
            player.release()
        }
        super.onDestroy()
    }

}