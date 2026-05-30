package com.example.playback

import android.content.ComponentName
import android.content.Context
import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.example.data.model.Track
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PlaybackState(
    val currentTrack: Track? = null,
    val isPlaying: Boolean = false,
    val currentPosition: Long = 0L,
    val duration: Long = 0L,
    val shuffleModeEnabled: Boolean = false,
    val repeatMode: RepeatMode = RepeatMode.NONE,
    val errorMessage: String? = null
)

enum class RepeatMode {
    NONE, ONE, ALL
}

class PlaybackManager(private val context: Context) {

    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var mediaController: MediaController? = null

    private val _playbackState = MutableStateFlow(PlaybackState())
    val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()

    private var progressJob: Job? = null
    private var currentPlaylistTracks: List<Track> = emptyList()

    init {
        initializeController()
    }

    private fun initializeController() {
        try {
            val sessionToken = SessionToken(context, ComponentName(context, PlaybackService::class.java))
            controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
            controllerFuture?.addListener({
                try {
                    val controller = controllerFuture?.get()
                    if (controller != null) {
                        mediaController = controller
                        setupControllerListener(controller)
                        syncInitialState(controller)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    _playbackState.update { it.copy(errorMessage = "Failed to connect to playback service: ${e.localizedMessage}") }
                }
            }, androidx.core.content.ContextCompat.getMainExecutor(context))
        } catch (e: Exception) {
            e.printStackTrace()
            _playbackState.update { it.copy(errorMessage = "Failed to initialize playback manager: ${e.localizedMessage}") }
        }
    }

    private fun setupControllerListener(controller: MediaController) {
        controller.addListener(object : Player.Listener {
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                updateCurrentTrackFromMediaItem(mediaItem)
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _playbackState.update { it.copy(isPlaying = isPlaying) }
                if (isPlaying) {
                    _playbackState.update { it.copy(errorMessage = null) }
                    startProgressTracker()
                } else {
                    stopProgressTracker()
                }
            }

            override fun onPlaybackStateChanged(state: Int) {
                val duration = mediaController?.duration ?: 0L
                _playbackState.update { 
                    it.copy(
                        duration = if (duration > 0) duration else 0L
                    ) 
                }
            }

            override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
                _playbackState.update { it.copy(shuffleModeEnabled = shuffleModeEnabled) }
            }

            override fun onRepeatModeChanged(repeatMode: Int) {
                val mode = when (repeatMode) {
                    Player.REPEAT_MODE_ONE -> RepeatMode.ONE
                    Player.REPEAT_MODE_ALL -> RepeatMode.ALL
                    else -> RepeatMode.NONE
                }
                _playbackState.update { it.copy(repeatMode = mode) }
            }

            override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                error.printStackTrace()
                _playbackState.update { 
                    it.copy(
                        isPlaying = false, 
                        errorMessage = "Error playing music. Please check your network connection."
                    ) 
                }
            }
        })
    }

    private fun syncInitialState(controller: MediaController) {
        val duration = controller.duration
        val mode = when (controller.repeatMode) {
            Player.REPEAT_MODE_ONE -> RepeatMode.ONE
            Player.REPEAT_MODE_ALL -> RepeatMode.ALL
            else -> RepeatMode.NONE
        }
        _playbackState.update {
            it.copy(
                isPlaying = controller.isPlaying,
                duration = if (duration > 0) duration else 0L,
                shuffleModeEnabled = controller.shuffleModeEnabled,
                repeatMode = mode
            )
        }
        updateCurrentTrackFromMediaItem(controller.currentMediaItem)
        if (controller.isPlaying) {
            startProgressTracker()
        }
    }

    private fun updateCurrentTrackFromMediaItem(mediaItem: MediaItem?) {
        if (mediaItem == null) {
            _playbackState.update { it.copy(currentTrack = null) }
            return
        }
        val matchingTrack = currentPlaylistTracks.find { it.id == mediaItem.mediaId }
        val finalTrack = matchingTrack ?: Track(
            id = mediaItem.mediaId,
            title = mediaItem.mediaMetadata.title?.toString() ?: "Unknown",
            artist = mediaItem.mediaMetadata.artist?.toString() ?: "Unknown",
            album = mediaItem.mediaMetadata.albumTitle?.toString() ?: "Unknown",
            mediaUri = "",
            durationMs = mediaController?.duration ?: 0L
        )
        _playbackState.update { it.copy(currentTrack = finalTrack) }
    }

    private fun startProgressTracker() {
        progressJob?.cancel()
        progressJob = scope.launch {
            while (true) {
                val position = mediaController?.currentPosition ?: 0L
                val duration = mediaController?.duration ?: 0L
                _playbackState.update { 
                    it.copy(
                        currentPosition = position,
                        duration = if (duration > 0) duration else it.duration
                    ) 
                }
                delay(1000)
            }
        }
    }

    private fun stopProgressTracker() {
        progressJob?.cancel()
        progressJob = null
    }

    fun playTrackList(tracks: List<Track>, startTrackIndex: Int = 0) {
        val controller = mediaController ?: return
        currentPlaylistTracks = tracks

        _playbackState.update { it.copy(errorMessage = null) }

        controller.stop()
        controller.clearMediaItems()

        val mediaItems = tracks.map { track ->
            val builder = MediaItem.Builder()
                .setMediaId(track.id)
                .setUri(track.mediaUri)
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle(track.title)
                        .setArtist(track.artist)
                        .setAlbumTitle(track.album)
                        .setArtworkUri(track.albumArtUri?.let { Uri.parse(it) })
                        .build()
                )
            
            // Force stream mime type to allow progressive MPEG decoder to load radio URLs without extensions securely
            if (track.id.startsWith("radio_") || track.mediaUri.startsWith("http")) {
                builder.setMimeType(androidx.media3.common.MimeTypes.AUDIO_MPEG)
            }
            
            builder.build()
        }

        controller.setMediaItems(mediaItems, startTrackIndex, 0L)
        controller.prepare()
        controller.play()
    }

    fun playOrPause() {
        val controller = mediaController ?: return
        if (controller.isPlaying) {
            controller.pause()
        } else {
            if (controller.playbackState == Player.STATE_IDLE) {
                controller.prepare()
            }
            controller.play()
        }
    }

    fun next() {
        mediaController?.seekToNext()
    }

    fun previous() {
        mediaController?.seekToPrevious()
    }

    fun seekTo(positionMs: Long) {
        mediaController?.seekTo(positionMs)
        _playbackState.update { it.copy(currentPosition = positionMs) }
    }

    fun toggleShuffle() {
        val controller = mediaController ?: return
        controller.shuffleModeEnabled = !controller.shuffleModeEnabled
    }

    fun toggleRepeat() {
        val controller = mediaController ?: return
        val nextMode = when (controller.repeatMode) {
            Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ONE
            Player.REPEAT_MODE_ONE -> Player.REPEAT_MODE_ALL
            else -> Player.REPEAT_MODE_OFF
        }
        controller.repeatMode = nextMode
    }

    fun stop() {
        mediaController?.stop()
    }

    fun release() {
        stopProgressTracker()
        controllerFuture?.let {
            MediaController.releaseFuture(it)
        }
        mediaController = null
    }
}
