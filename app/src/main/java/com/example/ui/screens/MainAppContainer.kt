package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.data.model.Playlist
import com.example.ui.components.MiniPlayer
import com.example.viewmodel.MusicViewModel

sealed class AppScreen {
    object Browse : AppScreen()
    data class PlaylistDetail(val playlist: Playlist) : AppScreen()
}

@Composable
fun MainAppContainer(
    viewModel: MusicViewModel,
    modifier: Modifier = Modifier
) {
    var currentScreen by remember { mutableStateOf<AppScreen>(AppScreen.Browse) }
    var showFullPlayer by remember { mutableStateOf(false) }

    val playbackState by viewModel.playbackState.collectAsState()
    val isTrackLoaded = playbackState.currentTrack != null

    // Support standard device Back Button triggers
    BackHandler(enabled = showFullPlayer || currentScreen is AppScreen.PlaylistDetail) {
        if (showFullPlayer) {
            showFullPlayer = false
        } else if (currentScreen is AppScreen.PlaylistDetail) {
            currentScreen = AppScreen.Browse
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Scaffold(
            containerColor = androidx.compose.ui.graphics.Color.Transparent,
            modifier = Modifier.fillMaxSize()
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Main screen branching
                when (val screen = currentScreen) {
                    is AppScreen.Browse -> {
                        HomeScreen(
                            viewModel = viewModel,
                            onPlaylistClick = { playlist ->
                                currentScreen = AppScreen.PlaylistDetail(playlist)
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    is AppScreen.PlaylistDetail -> {
                        PlaylistDetailScreen(
                            playlist = screen.playlist,
                            viewModel = viewModel,
                            onBackClick = {
                                currentScreen = AppScreen.Browse
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }

        // Floating Glassmorphic MiniPlayer at the bottom
        if (isTrackLoaded && !showFullPlayer) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                MiniPlayer(
                    viewModel = viewModel,
                    onClick = { showFullPlayer = true }
                )
            }
        }

        // Sliding full-bleed player screen overlay
        AnimatedVisibility(
            visible = showFullPlayer,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            PlayerScreen(
                viewModel = viewModel,
                onCollapseClick = { showFullPlayer = false }
            )
        }
    }
}
