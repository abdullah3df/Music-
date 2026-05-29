package com.example.repository

import android.content.ContentUris
import android.content.Context
import android.provider.MediaStore
import com.example.data.local.PlaylistDao
import com.example.data.local.TrackDao
import com.example.data.model.Playlist
import com.example.data.model.PlaylistTrack
import com.example.data.model.Track
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext

class MusicRepository(
    private val trackDao: TrackDao,
    private val playlistDao: PlaylistDao
) {
    val allTracks: Flow<List<Track>> = trackDao.getAllTracks()
    val favoriteTracks: Flow<List<Track>> = trackDao.getFavoriteTracks()
    val allPlaylists: Flow<List<Playlist>> = playlistDao.getAllPlaylists()

    fun getTracksForPlaylist(playlistId: Long): Flow<List<Track>> {
        return playlistDao.getTracksForPlaylist(playlistId)
    }

    suspend fun toggleFavorite(trackId: String) = withContext(Dispatchers.IO) {
        val track = trackDao.getTrackById(trackId)
        if (track != null) {
            trackDao.updateFavoriteStatus(trackId, !track.isFavorite)
        }
    }

    suspend fun incrementPlayCount(trackId: String) = withContext(Dispatchers.IO) {
        trackDao.incrementPlayCount(trackId)
    }

    suspend fun createPlaylist(name: String): Long = withContext(Dispatchers.IO) {
        playlistDao.insertPlaylist(Playlist(name = name))
    }

    suspend fun deletePlaylist(playlistId: Long) = withContext(Dispatchers.IO) {
        playlistDao.deletePlaylist(playlistId)
    }

    suspend fun addTrackToPlaylist(playlistId: Long, trackId: String) = withContext(Dispatchers.IO) {
        val count = playlistDao.getPlaylistTrackCount(playlistId)
        playlistDao.insertPlaylistTrack(
            PlaylistTrack(playlistId = playlistId, trackId = trackId, orderIndex = count)
        )
    }

    suspend fun removeTrackFromPlaylist(playlistId: Long, trackId: String) = withContext(Dispatchers.IO) {
        playlistDao.removeTrackFromPlaylist(playlistId, trackId)
    }

    suspend fun reorderPlaylist(playlistId: Long, trackIds: List<String>) = withContext(Dispatchers.IO) {
        playlistDao.updatePlaylistOrder(playlistId, trackIds)
    }

    suspend fun initDefaultTracksIfEmpty() = withContext(Dispatchers.IO) {
        val existing = allTracks.firstOrNull()
        if (existing.isNullOrEmpty()) {
            val defaults = listOf(
                Track(
                    id = "demo_1",
                    title = "Digital Horizon",
                    artist = "Aether",
                    album = "Synthwave Dreams",
                    mediaUri = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3",
                    albumArtUri = "https://picsum.photos/seed/synthwave/500/500",
                    durationMs = 372000
                ),
                Track(
                    id = "demo_2",
                    title = "Ethereal Echoes",
                    artist = "Starlight Noise",
                    album = "Ambient Space",
                    mediaUri = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3",
                    albumArtUri = "https://picsum.photos/seed/ambient/500/500",
                    durationMs = 423000
                ),
                Track(
                    id = "demo_3",
                    title = "Midnight Jazz Lounge",
                    artist = "The Horns Combo",
                    album = "Cool Blues Vol 1",
                    mediaUri = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-3.mp3",
                    albumArtUri = "https://picsum.photos/seed/jazz/500/500",
                    durationMs = 344000
                ),
                Track(
                    id = "demo_4",
                    title = "Summer Grooves",
                    artist = "Beat Cruiser",
                    album = "House Club Anthems",
                    mediaUri = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-4.mp3",
                    albumArtUri = "https://picsum.photos/seed/house/500/500",
                    durationMs = 302000
                ),
                Track(
                    id = "demo_5",
                    title = "Neon Highway",
                    artist = "Velocity Runner",
                    album = "Retro Drive",
                    mediaUri = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-5.mp3",
                    albumArtUri = "https://picsum.photos/seed/neon/500/500",
                    durationMs = 362000
                )
            )
            trackDao.insertTracks(defaults)
        }
    }

    suspend fun scanDeviceAudio(context: Context) = withContext(Dispatchers.IO) {
        val cursor = context.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            arrayOf(
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.DATE_ADDED
            ),
            "${MediaStore.Audio.Media.IS_MUSIC} != 0",
            null,
            null
        )

        cursor?.use { c ->
            val idColumn = c.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleColumn = c.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistColumn = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val albumColumn = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val durationColumn = c.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val dateColumn = c.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)

            val scannedTracks = mutableListOf<Track>()
            while (c.moveToNext()) {
                val mediaId = c.getLong(idColumn)
                val title = c.getString(titleColumn) ?: "Unknown Track"
                val artist = c.getString(artistColumn) ?: "Unknown Artist"
                val album = c.getString(albumColumn) ?: "Unknown Album"
                val duration = c.getLong(durationColumn)
                val dateAdded = c.getLong(dateColumn) * 1000 // Convert to MS
                val contentUri = ContentUris.withAppendedId(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    mediaId
                ).toString()

                scannedTracks.add(
                    Track(
                        id = "local_$mediaId",
                        title = title,
                        artist = artist,
                        album = album,
                        mediaUri = contentUri,
                        albumArtUri = null, // system Uri for album arts can be derived, or placeholder gradient drawn
                        durationMs = duration,
                        dateAdded = dateAdded
                    )
                )
            }
            if (scannedTracks.isNotEmpty()) {
                trackDao.insertTracks(scannedTracks)
            }
        }
    }
}
