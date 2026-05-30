package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.Track
import com.example.playback.PlaybackState
import com.example.playback.RepeatMode
import com.example.viewmodel.MusicViewModel
import com.example.ui.components.LiveEqualizerVisualizer

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun PlayerScreen(
    viewModel: MusicViewModel,
    onCollapseClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val playbackState by viewModel.playbackState.collectAsState()
    val track = playbackState.currentTrack

    if (track == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            Text("No song loaded")
        }
        return
    }

    // Interactive slider position tracking
    var isUserSeeking by remember { mutableStateOf(false) }
    var userPositionSelection by remember { mutableLongStateOf(0L) }

    val playPosition = if (isUserSeeking) userPositionSelection else playbackState.currentPosition
    val totalDuration = playbackState.duration

    val infiniteTransition = rememberInfiniteTransition(label = "player_infinite")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
        ),
        label = "pulse"
    )
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    // Premium radial glowing auras
    val glowColor1 = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
    val glowColor2 = MaterialTheme.colorScheme.background
    val backgroundBrush = remember(track.id) {
        Brush.verticalGradient(
            colors = listOf(glowColor1, glowColor2, glowColor2)
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundBrush)
            .testTag("fullscreen_player")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header close arrow
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onCollapseClick,
                    modifier = Modifier.testTag("collapse_player_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.KeyboardArrowDown,
                        contentDescription = "Collapse Player",
                        modifier = Modifier.size(36.dp)
                    )
                }

                Text(
                    text = "NOW PLAYING",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    ),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )

                // Silent divider space to balance the close button
                Spacer(modifier = Modifier.size(48.dp))
            }

            // Big gorgeous album artwork sleeve with slow breathing atmospheric glass outline
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .padding(top = 12.dp, bottom = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                // Background ambient soft shadow glow that breathes with the music
                Box(
                    modifier = Modifier
                        .fillMaxSize(0.85f * if (playbackState.isPlaying) pulseScale else 1f)
                        .shadow(
                            elevation = 32.dp,
                            shape = RoundedCornerShape(28.dp),
                            clip = false,
                            ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = if (playbackState.isPlaying) glowAlpha else 0.25f),
                            spotColor = MaterialTheme.colorScheme.primary.copy(alpha = if (playbackState.isPlaying) glowAlpha else 0.25f)
                        )
                )

                Surface(
                    modifier = Modifier
                        .fillMaxSize(0.90f)
                        .aspectRatio(1f)
                        .shadow(20.dp, shape = RoundedCornerShape(28.dp))
                        .border(
                            1.5.dp,
                            Brush.linearGradient(
                                listOf(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.45f),
                                    Color.Transparent
                                )
                            ),
                            shape = RoundedCornerShape(28.dp)
                        )
                        .clip(RoundedCornerShape(28.dp))
                        .testTag("player_album_art")
                ) {
                    TrackAlbumArt(track = track, modifier = Modifier.fillMaxSize())
                }
            }

            // Song metadata block
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = track.title,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.ExtraBold
                    ),
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("player_track_title")
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = track.artist,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Slider seekBar timeline controls
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            ) {
                if (track.id.startsWith("radio_")) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
                            .border(androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)), RoundedCornerShape(16.dp))
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        LiveEqualizerVisualizer(
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .size(28.dp)
                                .padding(end = 6.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "بث راديو مباشر • LIVE BROADCAST",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                letterSpacing = 0.5.sp
                            )
                        )
                    }
                } else {
                    Slider(
                        value = if (totalDuration > 0) playPosition.toFloat() / totalDuration.toFloat() else 0f,
                        onValueChange = { fraction ->
                            isUserSeeking = true
                            userPositionSelection = (fraction * totalDuration).toLong()
                        },
                        onValueChangeFinished = {
                            isUserSeeking = false
                            viewModel.playbackManager.seekTo(userPositionSelection)
                        },
                        colors = SliderDefaults.colors(
                            activeTrackColor = MaterialTheme.colorScheme.primary,
                            inactiveTrackColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f),
                            thumbColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("player_seekbar")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = formatDuration(playPosition),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                        )
                        Text(
                            text = formatDuration(totalDuration),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                        )
                    }
                }
            }

            // Primary control layout row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 36.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Shuffle action button
                IconButton(
                    onClick = { viewModel.playbackManager.toggleShuffle() },
                    modifier = Modifier.testTag("shuffle_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Shuffle,
                        contentDescription = "Toggle Shuffle",
                        tint = if (playbackState.shuffleModeEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                        modifier = Modifier.size(28.dp)
                    )
                }

                // Previous song
                IconButton(
                    onClick = { viewModel.playbackManager.previous() },
                    modifier = Modifier.testTag("prev_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.SkipPrevious,
                        contentDescription = "Previous Track",
                        modifier = Modifier.size(42.dp)
                    )
                }

                // Main Play Pause floating disk with outer glowing ring
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(90.dp)
                ) {
                    // Outer pulsating glowing ring
                    Box(
                        modifier = Modifier
                            .size(if (playbackState.isPlaying) 82.dp * pulseScale else 76.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = if (playbackState.isPlaying) 0.12f else 0.05f))
                            .border(
                                1.5.dp,
                                MaterialTheme.colorScheme.primary.copy(alpha = if (playbackState.isPlaying) glowAlpha else 0.15f),
                                CircleShape
                            )
                    )

                    Surface(
                        color = MaterialTheme.colorScheme.primary,
                        shape = CircleShape,
                        modifier = Modifier
                            .size(64.dp)
                            .shadow(12.dp, shape = CircleShape)
                            .clickable { viewModel.playbackManager.playOrPause() }
                            .testTag("play_pause_button")
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Icon(
                                imageVector = if (playbackState.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                contentDescription = if (playbackState.isPlaying) "Pause" else "Play",
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }

                // Next song
                IconButton(
                    onClick = { viewModel.playbackManager.next() },
                    modifier = Modifier.testTag("next_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.SkipNext,
                        contentDescription = "Next Track",
                        modifier = Modifier.size(42.dp)
                    )
                }

                // Repeat action button (cycles through NONE, ONE, ALL)
                IconButton(
                    onClick = { viewModel.playbackManager.toggleRepeat() },
                    modifier = Modifier.testTag("repeat_button")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Filled.Repeat,
                            contentDescription = "Toggle Repeat",
                            tint = if (playbackState.repeatMode != RepeatMode.NONE) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                            modifier = Modifier.size(28.dp)
                        )
                        if (playbackState.repeatMode == RepeatMode.ONE) {
                            Text(
                                "1",
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .offset(x = 6.dp, y = ((-4).dp))
                            )
                        }
                    }
                }
            }
        }
    }
}
