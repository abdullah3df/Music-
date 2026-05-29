package com.example.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.Playlist
import com.example.data.model.Track
import com.example.playback.PlaybackManager
import com.example.playback.PlaybackState
import com.example.repository.MusicRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class SortType {
    TITLE, RECENTLY_ADDED, MOST_PLAYED
}

class MusicViewModel(
    private val repository: MusicRepository,
    val playbackManager: PlaybackManager
) : ViewModel() {

    // Sorting State
    private val _sortType = MutableStateFlow(SortType.TITLE)
    val sortType: StateFlow<SortType> = _sortType.asStateFlow()

    // Database Streams
    private val _allTracksDb = repository.allTracks
    private val _favoriteTracksDb = repository.favoriteTracks
    val allPlaylists: StateFlow<List<Playlist>> = repository.allPlaylists
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Sorted track stream for "All Songs" list
    val allTracks: StateFlow<List<Track>> = combine(_allTracksDb, _sortType) { tracks, sort ->
        when (sort) {
            SortType.TITLE -> tracks.sortedBy { it.title.lowercase() }
            SortType.RECENTLY_ADDED -> tracks.sortedByDescending { it.dateAdded }
            SortType.MOST_PLAYED -> tracks.sortedByDescending { it.playCount }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Sorted track stream for "Favorites" list
    val favoriteTracks: StateFlow<List<Track>> = combine(_favoriteTracksDb, _sortType) { tracks, sort ->
        when (sort) {
            SortType.TITLE -> tracks.sortedBy { it.title.lowercase() }
            SortType.RECENTLY_ADDED -> tracks.sortedByDescending { it.dateAdded }
            SortType.MOST_PLAYED -> tracks.sortedByDescending { it.playCount }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Currently focused Playlist details
    private val _activePlaylistId = MutableStateFlow<Long?>(null)
    val activePlaylistId: StateFlow<Long?> = _activePlaylistId.asStateFlow()

    private val _playlistTracksDb = MutableStateFlow<List<Track>>(emptyList())
    // Sorted playlist tracks list
    val playlistTracks: StateFlow<List<Track>> = combine(_playlistTracksDb, _sortType) { tracks, sort ->
        when (sort) {
            SortType.TITLE -> tracks.sortedBy { it.title.lowercase() }
            SortType.RECENTLY_ADDED -> tracks.sortedByDescending { it.dateAdded }
            SortType.MOST_PLAYED -> tracks.sortedByDescending { it.playCount }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Realtime playback state matching the ExoPlayer MediaController
    val playbackState: StateFlow<PlaybackState> = playbackManager.playbackState

    init {
        viewModelScope.launch {
            // Check for empty DB and insert sound helix standard samples
            repository.initDefaultTracksIfEmpty()
        }

        // Listener to record playing statistics (most played)
        viewModelScope.launch {
            var lastTrackedId: String? = null
            playbackState.collect { state ->
                val track = state.currentTrack
                if (state.isPlaying && track != null) {
                    if (track.id != lastTrackedId) {
                        lastTrackedId = track.id
                        repository.incrementPlayCount(track.id)
                    }
                } else if (track == null) {
                    lastTrackedId = null
                }
            }
        }
    }

    fun setSortType(type: SortType) {
        _sortType.value = type
    }

    // Toggle favoriting
    fun toggleFavorite(trackId: String) {
        viewModelScope.launch {
            repository.toggleFavorite(trackId)
        }
    }

    // Load active playlist tracks
    fun setActivePlaylist(playlistId: Long?) {
        _activePlaylistId.value = playlistId
        if (playlistId != null) {
            viewModelScope.launch {
                repository.getTracksForPlaylist(playlistId).collect { tracks ->
                    _playlistTracksDb.value = tracks
                }
            }
        } else {
            _playlistTracksDb.value = emptyList()
        }
    }

    // Creates playlist
    fun createPlaylist(name: String) {
        viewModelScope.launch {
            repository.createPlaylist(name)
        }
    }

    // Delete playlist
    fun deletePlaylist(playlistId: Long) {
        viewModelScope.launch {
            repository.deletePlaylist(playlistId)
            if (_activePlaylistId.value == playlistId) {
                setActivePlaylist(null)
            }
        }
    }

    // Adds song to playlist
    fun addTrackToPlaylist(playlistId: Long, trackId: String) {
        viewModelScope.launch {
            repository.addTrackToPlaylist(playlistId, trackId)
            // Trigger refresh if we're currently viewing this playlist
            if (_activePlaylistId.value == playlistId) {
                setActivePlaylist(playlistId)
            }
        }
    }

    // Removes song from playlist
    fun removeTrackFromPlaylist(playlistId: Long, trackId: String) {
        viewModelScope.launch {
            repository.removeTrackFromPlaylist(playlistId, trackId)
            if (_activePlaylistId.value == playlistId) {
                setActivePlaylist(playlistId)
            }
        }
    }

    // Reorder inside play track
    fun reorderPlaylistTracks(playlistId: Long, fromIndex: Int, toIndex: Int) {
        val tracksList = _playlistTracksDb.value.toMutableList()
        if (fromIndex in tracksList.indices && toIndex in tracksList.indices) {
            val moved = tracksList.removeAt(fromIndex)
            tracksList.add(toIndex, moved)
            _playlistTracksDb.value = tracksList
            viewModelScope.launch {
                repository.reorderPlaylist(playlistId, tracksList.map { it.id })
            }
        }
    }

    // Move track up or down strictly
    fun moveTrack(playlistId: Long, trackId: String, moveUp: Boolean) {
        val tracksList = _playlistTracksDb.value.map { it.id }.toMutableList()
        val index = tracksList.indexOf(trackId)
        if (index == -1) return
        val targetIndex = if (moveUp) index - 1 else index + 1
        if (targetIndex in tracksList.indices) {
            val element = tracksList.removeAt(index)
            tracksList.add(targetIndex, element)
            viewModelScope.launch {
                repository.reorderPlaylist(playlistId, tracksList)
                setActivePlaylist(playlistId)
            }
        }
    }

    // Scans local files
    fun scanLocalFiles(context: Context) {
        viewModelScope.launch {
            repository.scanDeviceAudio(context)
        }
    }

    override fun onCleared() {
        playbackManager.release()
        super.onCleared()
    }

    // Standard factory implementation API
    class Factory(private val context: Context) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MusicViewModel::class.java)) {
                val database = AppDatabase.getDatabase(context)
                val repository = MusicRepository(database.trackDao(), database.playlistDao())
                val playbackManager = PlaybackManager(context)
                @Suppress("UNCHECKED_CAST")
                return MusicViewModel(repository, playbackManager) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
