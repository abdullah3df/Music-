package com.example.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.example.data.model.Playlist
import com.example.data.model.Track
import com.example.ui.components.LiveEqualizerVisualizer
import com.example.playback.PlaybackState
import com.example.viewmodel.MusicViewModel
import com.example.viewmodel.SortType
import kotlinx.coroutines.launch

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: MusicViewModel,
    onPlaylistClick: (Playlist) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("الأصوات", "الراديو", "القوائم", "المفضلة")

    val tracks by viewModel.allTracks.collectAsState()
    val playlists by viewModel.allPlaylists.collectAsState()
    val favorites by viewModel.favoriteTracks.collectAsState()
    val playbackState by viewModel.playbackState.collectAsState()
    val currentSortType by viewModel.sortType.collectAsState()

    // Show toast message on playback errors
    LaunchedEffect(playbackState.errorMessage) {
        playbackState.errorMessage?.let { error ->
            android.widget.Toast.makeText(context, error, android.widget.Toast.LENGTH_LONG).show()
        }
    }

    val scope = rememberCoroutineScope()
    var isCopyingFile by remember { mutableStateOf(false) }
    var showAddTrackDialog by remember { mutableStateOf(false) }

    var showPlaylistDialog by remember { mutableStateOf(false) }
    var showAddToPlaylistDialog by remember { mutableStateOf<Track?>(null) }
    var showSortMenu by remember { mutableStateOf(false) }

    val isScanning by viewModel.isScanning.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.scanResultEvent.collect { count ->
            if (count > 0) {
                android.widget.Toast.makeText(context, "تم العثور على $count من الملفات الصوتية الجديدة بالجهاز ومزامنتها بنجاح! 🎵", android.widget.Toast.LENGTH_LONG).show()
            } else if (count == 0) {
                android.widget.Toast.makeText(context, "تم فحص الملفات! المكتبة مطابقة لملفات جهازك بالفعل. ✨", android.widget.Toast.LENGTH_SHORT).show()
            } else if (count == -1) {
                android.widget.Toast.makeText(context, "حدث خطأ أثناء فحص ملفات جهازك الصوتية", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Media permissions
    var hasPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_AUDIO) == PackageManager.PERMISSION_GRANTED
            } else {
                ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
            }
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasPermission = isGranted
        if (isGranted) {
            viewModel.scanLocalFiles(context)
        }
    }

    // Automatically trigger local file scan if permission is granted
    LaunchedEffect(hasPermission) {
        if (hasPermission) {
            viewModel.scanLocalFiles(context)
        }
    }

    // Automatically request permissions on launch to enable seamless background scanning
    LaunchedEffect(Unit) {
        if (!hasPermission) {
            val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                Manifest.permission.READ_MEDIA_AUDIO
            } else {
                Manifest.permission.READ_EXTERNAL_STORAGE
            }
            permissionLauncher.launch(permission)
        }
    }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        if (uri != null) {
            isCopyingFile = true
            scope.launch {
                try {
                    val fileName = "local_audio_${System.currentTimeMillis()}.mp3"
                    val localPath = copyUriToInternalStorage(context, uri, fileName)
                    if (localPath != null) {
                        val meta = getAudioMetadata(context, uri)
                        viewModel.addCustomTrack(
                            title = meta.first,
                            artist = meta.second,
                            album = "الملفات الصوتية المحملة",
                            mediaUri = localPath,
                            durationMs = meta.third
                        )
                        android.widget.Toast.makeText(context, "تمت إضافة الملف بنجاح! 🎧", android.widget.Toast.LENGTH_SHORT).show()
                    } else {
                        android.widget.Toast.makeText(context, "فشل في استيراد الملف الصوتي", android.widget.Toast.LENGTH_LONG).show()
                    }
                } catch (e: Exception) {
                    android.widget.Toast.makeText(context, "خطأ أثناء إضافة الملف: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
                } finally {
                    isCopyingFile = false
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .drawBehind {
                // Top-Left luxurious violet aura
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0x2A8D55F3), // translucent primary purple
                            Color.Transparent
                        ),
                        radius = size.width * 1.0f
                    ),
                    center = androidx.compose.ui.geometry.Offset(0f, 0f)
                )
                // Top-Right deep cyan aura for gorgeous high-fidelity interplay
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0x1B00BCD4), // translucent teal/cyan
                            Color.Transparent
                        ),
                        radius = size.width * 0.9f
                    ),
                    center = androidx.compose.ui.geometry.Offset(size.width, size.height * 0.1f)
                )
            }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "AURA MUSIC",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Black,
                                fontSize = 23.sp,
                                letterSpacing = 2.sp,
                                color = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier.testTag("app_title")
                        )
                    },
                    actions = {
                        IconButton(
                            onClick = { viewModel.scanLocalFiles(context) },
                            enabled = !isScanning,
                            modifier = Modifier.testTag("scan_button")
                        ) {
                            if (isScanning) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Filled.Autorenew,
                                    contentDescription = "Scan Audio Files",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        IconButton(
                            onClick = { showSortMenu = true },
                            modifier = Modifier.testTag("sort_button")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.SortByAlpha,
                                contentDescription = "Sort Music",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        DropdownMenu(
                            expanded = showSortMenu,
                            onDismissRequest = { showSortMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("A - Z Title") },
                                onClick = {
                                    viewModel.setSortType(SortType.TITLE)
                                    showSortMenu = false
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Filled.TextFields,
                                        contentDescription = null
                                    )
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Most Played") },
                                onClick = {
                                    viewModel.setSortType(SortType.MOST_PLAYED)
                                    showSortMenu = false
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Filled.TrendingUp,
                                        contentDescription = null
                                    )
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Recently Added") },
                                onClick = {
                                    viewModel.setSortType(SortType.RECENTLY_ADDED)
                                    showSortMenu = false
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Filled.Schedule,
                                        contentDescription = null
                                    )
                                }
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            },
            floatingActionButton = {
                if (selectedTab == 0) {
                    FloatingActionButton(
                        onClick = {
                            if (!hasPermission) {
                                val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    Manifest.permission.READ_MEDIA_AUDIO
                                } else {
                                    Manifest.permission.READ_EXTERNAL_STORAGE
                                }
                                permissionLauncher.launch(permission)
                            } else {
                                viewModel.scanLocalFiles(context)
                            }
                        },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.testTag("add_track_fab")
                    ) {
                        if (isScanning) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Filled.Autorenew, contentDescription = "Scan Device Audio")
                        }
                    }
                } else if (selectedTab == 1) {
                    var showAddRadioDialog by remember { mutableStateOf(false) }
                    FloatingActionButton(
                        onClick = { showAddRadioDialog = true },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.testTag("add_radio_fab")
                    ) {
                        Icon(Icons.Filled.Radio, contentDescription = "Add Radio Station")
                    }
                    if (showAddRadioDialog) {
                        AddRadioStationDialog(
                            onDismiss = { showAddRadioDialog = false },
                            onAdd = { name, url ->
                                viewModel.addRadioStation(name, url)
                                showAddRadioDialog = false
                            }
                        )
                    }
                } else if (selectedTab == 2) {
                    FloatingActionButton(
                        onClick = { showPlaylistDialog = true },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.testTag("create_playlist_fab")
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = "Create Playlist")
                    }
                }
            },
            containerColor = Color.Transparent,
            modifier = modifier
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Customized sliding Pill Tabrow container
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .height(52.dp)
                        .clip(RoundedCornerShape(26.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    tabs.forEachIndexed { index, title ->
                        val isSelected = selectedTab == index
                        val animatedBg by animateColorAsState(
                            targetValue = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                            animationSpec = tween(280, easing = FastOutSlowInEasing),
                            label = "tab_bg"
                        )
                        val animatedTextColor by animateColorAsState(
                            targetValue = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                            animationSpec = tween(280, easing = FastOutSlowInEasing),
                            label = "tab_txt"
                        )

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(22.dp))
                                .background(animatedBg)
                                .clickable(
                                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                                    indication = null,
                                    onClick = { selectedTab = index }
                                )
                                .testTag("tab_$index"),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier.padding(horizontal = 2.dp)
                            ) {
                                val tabIcon = when (index) {
                                    0 -> Icons.Filled.AudioFile
                                    1 -> Icons.Filled.Radio
                                    2 -> Icons.Outlined.QueueMusic
                                    else -> Icons.Filled.Favorite
                                }
                                Icon(
                                    imageVector = tabIcon,
                                    contentDescription = null,
                                    modifier = Modifier.size(15.dp),
                                    tint = animatedTextColor
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        letterSpacing = 0.sp
                                    ),
                                    color = animatedTextColor,
                                    textAlign = TextAlign.Center,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

            Box(modifier = Modifier.fillMaxSize()) {
                when (selectedTab) {
                    0 -> AllSongsTab(
                        tracks = tracks,
                        hasPermission = hasPermission,
                        onRequestPermission = {
                            val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                Manifest.permission.READ_MEDIA_AUDIO
                            } else {
                                Manifest.permission.READ_EXTERNAL_STORAGE
                            }
                            permissionLauncher.launch(permission)
                        },
                        isScanning = isScanning,
                        onScanClick = { viewModel.scanLocalFiles(context) },
                        playbackState = playbackState,
                        onTrackClick = { clicked ->
                            viewModel.playbackManager.playTrackList(tracks, tracks.indexOf(clicked))
                        },
                        onFavoriteClick = { viewModel.toggleFavorite(it.id) },
                        onPlaylistAddClick = { showAddToPlaylistDialog = it },
                        onShareClick = { shareTrack(context, it) },
                        onDeleteClick = { viewModel.deleteTrack(it.id) }
                    )
                    1 -> RadioTab(
                        viewModel = viewModel
                    )
                    2 -> PlaylistsTab(
                        playlists = playlists,
                        onPlaylistClick = onPlaylistClick,
                        onDeleteClick = { viewModel.deletePlaylist(it.id) }
                    )
                    3 -> FavoritesTab(
                        favorites = favorites,
                        playbackState = playbackState,
                        onTrackClick = { clicked ->
                            viewModel.playbackManager.playTrackList(favorites, favorites.indexOf(clicked))
                        },
                        onFavoriteClick = { viewModel.toggleFavorite(it.id) },
                        onPlaylistAddClick = { showAddToPlaylistDialog = it },
                        onShareClick = { shareTrack(context, it) },
                        onDeleteClick = { viewModel.deleteTrack(it.id) }
                    )
                }
            }
        }
    }
}

    // New Playlist Modal
    if (showPlaylistDialog) {
        CreatePlaylistDialog(
            onDismiss = { showPlaylistDialog = false },
            onCreate = { name ->
                viewModel.createPlaylist(name)
                showPlaylistDialog = false
            }
        )
    }

    // Add To Playlist Sheet
    showAddToPlaylistDialog?.let { trackToAdd ->
        AddToPlaylistDialog(
            track = trackToAdd,
            playlists = playlists,
            onDismiss = { showAddToPlaylistDialog = null },
            onPlaylistSelected = { playlist ->
                viewModel.addTrackToPlaylist(playlist.id, trackToAdd.id)
                showAddToPlaylistDialog = null
            }
        )
    }

    // Add Custom/Local Track Dialog
    if (showAddTrackDialog) {
        AddTrackDialog(
            onDismiss = { showAddTrackDialog = false },
            onSelectLocalFile = {
                filePickerLauncher.launch("audio/*")
            },
            onAddRemoteUrl = { title, artist, album, url ->
                viewModel.addCustomTrack(title, artist, album, url)
            },
            isCopying = isCopyingFile
        )
    }
}

@Composable
fun AllSongsTab(
    tracks: List<Track>,
    hasPermission: Boolean,
    onRequestPermission: () -> Unit,
    isScanning: Boolean,
    onScanClick: () -> Unit,
    playbackState: PlaybackState,
    onTrackClick: (Track) -> Unit,
    onFavoriteClick: (Track) -> Unit,
    onPlaylistAddClick: (Track) -> Unit,
    onShareClick: (Track) -> Unit,
    onDeleteClick: (Track) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        if (!hasPermission) {
            Card(
                onClick = onRequestPermission,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("permission_banner")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.AudioFile,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "فحص ملفات الصوت المحلية",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "اضغط هنا للسماح بالوصول ومزامنة ملفات الصوت الموجودة في جهازك تلقائيًا.",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        } else {
            // Interactive dynamic scanning promotional card when permission is granted
            Card(
                onClick = onScanClick,
                enabled = !isScanning,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isScanning) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.5.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Filled.Autorenew,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            if (isScanning) "جاري البحث والمزامنة الآلية..." else "مسح ومزامنة جهازك تلقائياً",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            if (isScanning) "فضلاً انتظر لعدة ثوانٍ حتى ننتهي من فحص وتحديث مكتبتك الصوتية من الذاكرة..." else "اضغط هنا للبحث الآلي في التحميلات والمجلدات وتحديث كافة ملفاتك الصوتية.",
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }

        if (tracks.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isScanning) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "جاري فحص وتحديث أصوات جهازك...",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "يتم الآن البحث تلقائياً عن كافة الملفات الصوتية المخزنة والمحملة على جهازك ومزامنتها...",
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Outlined.AudioFile,
                            contentDescription = null,
                            modifier = Modifier.size(72.dp),
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "لا توجد أصوات مضافة",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "لم يتم العثور على أي ملفات صوتية بعد. اضغط على الزر أدناه لمسح ومزامنة جهازك بالكامل تلقائياً.",
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                        )
                        if (!hasPermission) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = onRequestPermission,
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("السماح بالوصول والفحص التلقائي")
                            }
                        } else {
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = onScanClick,
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Filled.Autorenew, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("البدء بالفحص والمزامنة التلقائية")
                            }
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(bottom = 90.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(tracks, key = { it.id }) { track ->
                    TrackRowItem(
                        track = track,
                        isPlaying = playbackState.currentTrack?.id == track.id,
                        onTrackClick = { onTrackClick(track) },
                        onFavoriteClick = { onFavoriteClick(track) },
                        onPlaylistAddClick = { onPlaylistAddClick(track) },
                        onShareClick = { onShareClick(track) },
                        onDeleteClick = { onDeleteClick(track) }
                    )
                }
            }
        }
    }
}

@Composable
fun PlaylistsTab(
    playlists: List<Playlist>,
    onPlaylistClick: (Playlist) -> Unit,
    onDeleteClick: (Playlist) -> Unit
) {
    if (playlists.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.QueueMusic,
                    contentDescription = null,
                    modifier = Modifier.size(72.dp),
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "No Playlists Yet",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Click the floating + button to create your first music compilation.",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
            }
        }
    } else {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 90.dp, top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(playlists, key = { it.id }) { playlist ->
                PlaylistGridCard(
                    playlist = playlist,
                    onClick = { onPlaylistClick(playlist) },
                    onDeleteClick = { onDeleteClick(playlist) }
                )
            }
        }
    }
}

@Composable
fun FavoritesTab(
    favorites: List<Track>,
    playbackState: PlaybackState,
    onTrackClick: (Track) -> Unit,
    onFavoriteClick: (Track) -> Unit,
    onPlaylistAddClick: (Track) -> Unit,
    onShareClick: (Track) -> Unit,
    onDeleteClick: (Track) -> Unit
) {
    if (favorites.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.FavoriteBorder,
                    contentDescription = null,
                    modifier = Modifier.size(72.dp),
                    tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "قائمة المفضلة فارغة",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "هل أعجبك صوت أو تلاوة؟ اضغط على شعار القلب (❤️) بجانب أي مسار لإضافته إلى المفضلة.",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
            }
        }
    } else {
        LazyColumn(
            contentPadding = PaddingValues(bottom = 90.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(favorites, key = { it.id }) { track ->
                TrackRowItem(
                    track = track,
                    isPlaying = playbackState.currentTrack?.id == track.id,
                    onTrackClick = { onTrackClick(track) },
                    onFavoriteClick = { onFavoriteClick(track) },
                    onPlaylistAddClick = { onPlaylistAddClick(track) },
                    onShareClick = { onShareClick(track) },
                    onDeleteClick = { onDeleteClick(track) }
                )
            }
        }
    }
}

@Composable
fun TrackRowItem(
    track: Track,
    isPlaying: Boolean,
    onTrackClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    onPlaylistAddClick: () -> Unit,
    onShareClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val durationText = formatDuration(track.durationMs)
    var showMenu by remember { mutableStateOf(false) }

    val containerColor = if (isPlaying) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.22f)
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.12f)
    }

    val borderColor = if (isPlaying) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.45f)
    } else {
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
        onClick = onTrackClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .shadow(if (isPlaying) 6.dp else 0.dp, shape = RoundedCornerShape(16.dp))
            .testTag("track_item_${track.id}")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(12.dp))
            ) {
                TrackAlbumArt(
                    track = track,
                    modifier = Modifier.fillMaxSize()
                )
                if (isPlaying) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.35f)),
                        contentAlignment = Alignment.Center
                    ) {
                        LiveEqualizerVisualizer(
                            color = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = track.title,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (isPlaying) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground
                    )
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${track.artist} • ${track.album}",
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isPlaying) MaterialTheme.colorScheme.primary.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.55f)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = durationText,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                modifier = Modifier.padding(end = 4.dp)
            )

            IconButton(onClick = onFavoriteClick) {
                Icon(
                    imageVector = if (track.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = "Toggle Favorite",
                    tint = if (track.isFavorite) Color(0xFFFF5252) else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.45f),
                    modifier = Modifier.size(22.dp)
                )
            }

            Box {
                IconButton(onClick = { showMenu = true }, modifier = Modifier.testTag("track_menu_button_${track.id}")) {
                    Icon(
                        imageVector = Icons.Filled.MoreVert,
                        contentDescription = "More options",
                        tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                        modifier = Modifier.size(22.dp)
                    )
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("إضافة إلى قائمة التشغيل") },
                        onClick = {
                            showMenu = false
                            onPlaylistAddClick()
                        },
                        leadingIcon = {
                            Icon(Icons.Filled.PlaylistAdd, contentDescription = null)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("مشاركة الأغنية") },
                        onClick = {
                            showMenu = false
                            onShareClick()
                        },
                        leadingIcon = {
                            Icon(Icons.Filled.Share, contentDescription = null)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("حذف الأغنية", color = MaterialTheme.colorScheme.error) },
                        onClick = {
                            showMenu = false
                            onDeleteClick()
                        },
                        leadingIcon = {
                            Icon(Icons.Filled.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun PlaylistGridCard(
    playlist: Playlist,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .shadow(4.dp, shape = RoundedCornerShape(16.dp))
            .testTag("playlist_card_${playlist.id}")
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Large vinyl-like folder graphics
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            Brush.sweepGradient(
                                listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.secondary,
                                    MaterialTheme.colorScheme.primary
                                )
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.QueueMusic,
                        tint = Color.White,
                        contentDescription = null
                    )
                }

                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = playlist.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Curator Playlist",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                }
            }

            IconButton(
                onClick = onDeleteClick,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.DeleteOutline,
                    contentDescription = "Delete Playlist",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
fun TrackAlbumArt(
    track: Track,
    modifier: Modifier = Modifier
) {
    if (track.id.startsWith("radio_")) {
        Box(
            modifier = modifier
                .background(Brush.linearGradient(listOf(
                    MaterialTheme.colorScheme.primaryContainer,
                    MaterialTheme.colorScheme.secondaryContainer
                )))
                .padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Radio,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.fillMaxSize(0.5f)
            )
        }
    } else if (!track.albumArtUri.isNullOrEmpty()) {
        AsyncImage(
            model = track.albumArtUri,
            contentDescription = "Album Art",
            contentScale = ContentScale.Crop,
            modifier = modifier
        )
    } else {
        // Dynamic generative fallback sleeve gradient
        val character = if (track.title.isNotEmpty()) track.title.substring(0, 1).uppercase() else "M"
        val colors = listOf(
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.tertiaryContainer,
            MaterialTheme.colorScheme.secondaryContainer
        )
        Box(
            modifier = modifier
                .background(Brush.linearGradient(colors))
                .padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = character,
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    }
}

@Composable
fun CreatePlaylistDialog(
    onDismiss: () -> Unit,
    onCreate: (String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            tonalElevation = 6.dp,
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.width(320.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "New Playlist",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))
                TextField(
                    value = name,
                    onValueChange = { name = it },
                    placeholder = { Text("Relaxing Melodies") },
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("playlist_name_field")
                )
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { if (name.isNotBlank()) onCreate(name) },
                        enabled = name.isNotBlank(),
                        modifier = Modifier.testTag("playlist_save_button")
                    ) {
                        Text("Create")
                    }
                }
            }
        }
    }
}

@Composable
fun AddToPlaylistDialog(
    track: Track,
    playlists: List<Playlist>,
    onDismiss: () -> Unit,
    onPlaylistSelected: (Playlist) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            tonalElevation = 6.dp,
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.width(320.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    "Add to Playlist",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                Text(
                    "Select which list to host '${track.title}'",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                if (playlists.isEmpty()) {
                    Text(
                        "No playlists available. Create one first inside the Playlists tab.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .heightIn(max = 240.dp)
                            .fillMaxWidth()
                    ) {
                        items(playlists) { playlist ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onPlaylistSelected(playlist) }
                                    .padding(vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.QueueMusic,
                                    tint = MaterialTheme.colorScheme.primary,
                                    contentDescription = null
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Text(
                                    playlist.name,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Close")
                    }
                }
            }
        }
    }
}

@SuppressLint("DefaultLocale")
fun formatDuration(ms: Long): String {
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%02d:%02d", minutes, seconds)
}

fun shareTrack(context: Context, track: Track) {
    val shareBody = "Listening to Aura Music:\n🎵 ${track.title}\n👥 Artist: ${track.artist}\n💿 Album: ${track.album}\n" +
            if (track.mediaUri.startsWith("http")) "🔗 Link: ${track.mediaUri}" else ""
    val sendIntent = android.content.Intent().apply {
        action = android.content.Intent.ACTION_SEND
        putExtra(android.content.Intent.EXTRA_TEXT, shareBody)
        type = "text/plain"
    }
    val shareIntent = android.content.Intent.createChooser(sendIntent, "Share Track Via")
    context.startActivity(shareIntent)
}

@Composable
fun RadioTab(
    viewModel: MusicViewModel,
    modifier: Modifier = Modifier
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val radioStations by viewModel.allRadioStations.collectAsState()
    val playbackState by viewModel.playbackState.collectAsState()

    if (radioStations.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
                Icon(
                    imageVector = Icons.Filled.Radio,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "لم يتم العثور على محطات راديو",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
                Text(
                    "انقر على الزر بالأسفل لإضافة محطة مخصصة",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    } else {
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "راديو الإنترنت المباشر 📻",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            items(radioStations) { station ->
                val isPlayingThis = playbackState.isPlaying && playbackState.currentTrack?.id == station.id
                val isCurrentThis = playbackState.currentTrack?.id == station.id

                Card(
                    onClick = { viewModel.playRadioStation(station) },
                    shape = RoundedCornerShape(20.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        width = 1.dp,
                        color = if (isCurrentThis) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                    ),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isCurrentThis) {
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.22f)
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)
                        }
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("radio_station_card_${station.id}")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // High-fidelity audio playing equalizer or static radio logo box
                        if (isPlayingThis) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)),
                                contentAlignment = Alignment.Center
                            ) {
                                LiveEqualizerVisualizer(
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(4.dp)
                                )
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Radio,
                                    tint = if (isCurrentThis) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                    modifier = Modifier.size(24.dp),
                                    contentDescription = null
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = station.name,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isCurrentThis) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(if (isPlayingThis) Color(0xFF4CAF50) else Color.Gray)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isPlayingThis) "البث مباشر مفعّل" else "جاهز للبث",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                                )
                            }
                        }

                        IconButton(
                            onClick = {
                                if (isPlayingThis) {
                                    viewModel.playbackManager.playOrPause()
                                } else {
                                    viewModel.playRadioStation(station)
                                }
                            }
                        ) {
                            Icon(
                                imageVector = if (isPlayingThis) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                tint = if (isCurrentThis) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                                contentDescription = "Play"
                            )
                        }

                        if (station.isCustom) {
                            IconButton(onClick = { viewModel.deleteRadioStation(station.id) }) {
                                Icon(
                                    imageVector = Icons.Filled.Delete,
                                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                                    contentDescription = "Delete Status"
                                )
                            }
                        }
                    }
                }
            }
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
fun AddRadioStationDialog(
    onDismiss: () -> Unit,
    onAdd: (String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var url by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("إضافة إذاعة راديو جديدة") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        errorMsg = null
                    },
                    label = { Text("اسم إذاعة الراديو") },
                    placeholder = { Text("مثال: إذاعة القرآن الكريم") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = url,
                    onValueChange = {
                        url = it
                        errorMsg = null
                    },
                    label = { Text("رابط البث المباشر (URL)") },
                    placeholder = { Text("https://example.com/stream.mp3") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                errorMsg?.let { msg ->
                    Text(
                        text = msg,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank() || url.isBlank()) {
                        errorMsg = "يرجى ملء كافة الحقول المطلوبة!"
                    } else if (!url.startsWith("http://") && !url.startsWith("https://") && !url.contains(".")) {
                        errorMsg = "يرجى إدخال رابط بث صحيح!"
                    } else {
                        onAdd(name.trim(), url.trim())
                    }
                }
            ) {
                Text("إضافة")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
}

@Composable
fun AddTrackDialog(
    onDismiss: () -> Unit,
    onSelectLocalFile: () -> Unit,
    onAddRemoteUrl: (String, String, String, String) -> Unit,
    isCopying: Boolean
) {
    var selectedOption by remember { mutableIntStateOf(0) } // 0: Local File, 1: Remote Link
    
    // Remote Link Fields
    var title by remember { mutableStateOf("") }
    var artist by remember { mutableStateOf("") }
    var album by remember { mutableStateOf("") }
    var url by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            tonalElevation = 8.dp,
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.width(340.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "إضافة ملف صوتي جديد",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Modern Pill Option Selector
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(50))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    val optionStyleSelected = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                    val optionStyleUnselected = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Button(
                        onClick = { selectedOption = 0 },
                        colors = if (selectedOption == 0) optionStyleSelected else optionStyleUnselected,
                        shape = RoundedCornerShape(50),
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        Text("ملف محلي", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                    }

                    Button(
                        onClick = { selectedOption = 1 },
                        colors = if (selectedOption == 1) optionStyleSelected else optionStyleUnselected,
                        shape = RoundedCornerShape(50),
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        Text("رابط صوتي", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                if (selectedOption == 0) {
                    // LOCAL FILE PICKER UI
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AudioFile,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "اختر ملفاً صوتياً من جهازك (مثال: MP3/M4A)",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        if (isCopying) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("جاري نسخ واستيراد الملف...", style = MaterialTheme.typography.bodySmall)
                        } else {
                            Button(
                                onClick = {
                                    onSelectLocalFile()
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Filled.Launch, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("فتح مستكشف الملفات")
                            }
                        }
                    }
                } else {
                    // REMOTE URL STREAM UI
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it; errorMsg = null },
                            label = { Text("عنوان الصوت") },
                            placeholder = { Text("مثال: سوره الفاتحة") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = artist,
                            onValueChange = { artist = it; errorMsg = null },
                            label = { Text("اسم القارئ / المنشد") },
                            placeholder = { Text("مثال: ماهر المعيقلي") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = album,
                            onValueChange = { album = it },
                            label = { Text("الألبوم / التصنيف (اختياري)") },
                            placeholder = { Text("مثال: القرآن الكريم") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = url,
                            onValueChange = { url = it; errorMsg = null },
                            label = { Text("رابط الصوت المباشر (URL)") },
                            placeholder = { Text("https://example.com/sound.mp3") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        errorMsg?.let { msg ->
                            Text(
                                text = msg,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("إلغاء")
                    }
                    if (selectedOption == 1) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (title.isBlank() || artist.isBlank() || url.isBlank()) {
                                    errorMsg = "يرجى تعبئة كافة الحقول المطلوبة!"
                                } else if (!url.startsWith("http://") && !url.startsWith("https://") && !url.contains(".")) {
                                    errorMsg = "يرجى إدخال رابط بث صحيح ومباشر!"
                                } else {
                                    onAddRemoteUrl(title, artist, if (album.isBlank()) "الشبكة" else album, url)
                                    onDismiss()
                                }
                            }
                        ) {
                            Text("إضافة")
                        }
                    }
                }
            }
        }
    }
}

private fun copyUriToInternalStorage(context: Context, uri: android.net.Uri, fileName: String): String? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return null
        val outputFile = java.io.File(context.filesDir, fileName)
        inputStream.use { input ->
            outputFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        outputFile.absolutePath
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

private fun getAudioMetadata(context: Context, uri: android.net.Uri): Triple<String, String, Long> {
    var title = "أثر صوّتي محلي"
    var artist = "قاريء مجهول"
    var duration = 0L
    try {
        val retriever = android.media.MediaMetadataRetriever()
        retriever.setDataSource(context, uri)
        val extractedTitle = retriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_TITLE)
        if (!extractedTitle.isNullOrBlank()) {
            title = extractedTitle
        }
        val extractedArtist = retriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_ARTIST)
        if (!extractedArtist.isNullOrBlank()) {
            artist = extractedArtist
        }
        val durStr = retriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_DURATION)
        duration = durStr?.toLongOrNull() ?: 0L
        retriever.release()
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return Triple(title, artist, duration)
}

