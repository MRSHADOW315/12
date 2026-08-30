package com.example.ui.live

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.local.entities.LiveStreamEntity
import com.example.data.local.entities.UserEntity
import com.example.data.repository.KinSphereRepository
import com.example.ui.components.UserAvatar
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceBorder
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.PrimaryNeon
import com.example.ui.theme.RomanticRed
import com.example.ui.theme.StreakFlameOrange
import com.example.ui.theme.StreakFlameYellow
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

data class LiveCommentItem(
    val id: String,
    val senderName: String,
    val senderAvatar: String,
    val text: String
)

data class FloatingHeart(
    val id: Long,
    val offsetX: Float,
    val color: Color,
    val scale: Float
)

data class VirtualGift(
    val name: String,
    val emoji: String,
    val diamonds: Int
)

val VIRTUAL_GIFTS = listOf(
    VirtualGift("Rose", "🌹", 10),
    VirtualGift("Sparkler", "✨", 50),
    VirtualGift("Diamond", "💎", 100),
    VirtualGift("Rocket", "🚀", 500),
    VirtualGift("Crown", "👑", 1000)
)

@Composable
fun LiveStreamHubScreen(
    repository: KinSphereRepository,
    onOpenLiveViewer: (LiveStreamEntity) -> Unit
) {
    val liveStreams by repository.getActiveLiveStreams().collectAsState(initial = emptyList())
    val allUsers by repository.getAllUsers().collectAsState(initial = emptyList())
    val userMap = remember(allUsers) { allUsers.associateBy { it.id } }

    var showGoLiveDialog by remember { mutableStateOf(false) }
    var liveTitle by remember { mutableStateOf("") }
    var liveCategory by remember { mutableStateOf("Chat & Hangout") }
    var liveTags by remember { mutableStateOf("#Live #Paris #Community") }
    val coroutineScope = rememberCoroutineScope()

    val categories = listOf("All Live", "Music & Hangout", "Creative & Photo", "Culinary & Food", "Tech & Gaming")
    var selectedCategory by remember { mutableStateOf("All Live") }

    val filteredStreams = remember(liveStreams, selectedCategory) {
        if (selectedCategory == "All Live") liveStreams
        else liveStreams.filter { it.category == selectedCategory }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(top = 16.dp)
            .testTag("live_stream_hub_screen")
    ) {
        // --- HEADER ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(RomanticRed)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "KinSphere LIVE",
                        color = TextPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = "${liveStreams.size} active broadcasts worldwide",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }

            Button(
                onClick = { showGoLiveDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = RomanticRed),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("go_live_button")
            ) {
                Icon(Icons.Default.Radio, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Go LIVE", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Category Pills
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(categories) { cat ->
                val isSelected = selectedCategory == cat
                Surface(
                    color = if (isSelected) RomanticRed else DarkSurface,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, if (isSelected) RomanticRed else DarkSurfaceBorder),
                    modifier = Modifier.clickable { selectedCategory = cat }
                ) {
                    Text(
                        text = cat,
                        color = if (isSelected) Color.White else TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (filteredStreams.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🔴 No live streams in this category", color = TextSecondary, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = { showGoLiveDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = RomanticRed),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Be the first to Go Live!", color = Color.White)
                    }
                }
            }
        } else {
            // Live Stream Cards Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredStreams) { stream ->
                    val hostUser = userMap[stream.hostUserId]
                    Card(
                        colors = CardDefaults.cardColors(containerColor = DarkSurface),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, DarkSurfaceBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenLiveViewer(stream) }
                            .testTag("live_stream_card_${stream.id}")
                    ) {
                        Column {
                            // Video Stream Poster
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(0.85f)
                                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                            ) {
                                AsyncImage(
                                    model = stream.coverUrl.ifBlank { hostUser?.coverUrl ?: "https://images.unsplash.com/photo-1502602898657-3e91760cbb34?w=600" },
                                    contentDescription = stream.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )

                                // Dark vignette
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            Brush.verticalGradient(
                                                listOf(Color.Black.copy(alpha = 0.5f), Color.Transparent, Color.Black.copy(alpha = 0.8f))
                                            )
                                        )
                                )

                                // Live Badge
                                Surface(
                                    color = RomanticRed,
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .padding(8.dp)
                                        .align(Alignment.TopStart)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .clip(CircleShape)
                                                .background(Color.White)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("LIVE", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                // Viewers Count
                                Surface(
                                    color = Color.Black.copy(alpha = 0.6f),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .padding(8.dp)
                                        .align(Alignment.TopEnd)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Icon(Icons.Default.Visibility, contentDescription = null, tint = Color.White, modifier = Modifier.size(11.dp))
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text("${stream.viewerCount}", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                // Category Pill
                                Surface(
                                    color = PrimaryNeon.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier
                                        .padding(8.dp)
                                        .align(Alignment.BottomStart)
                                ) {
                                    Text(
                                        text = stream.category,
                                        color = PrimaryNeon,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            // Info Area
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = stream.title,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(6.dp))

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    UserAvatar(
                                        avatarUrl = hostUser?.avatarUrl ?: "",
                                        displayName = hostUser?.displayName ?: "Host",
                                        size = 20.dp
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = hostUser?.displayName ?: "Streamer",
                                        color = TextSecondary,
                                        fontSize = 11.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- GO LIVE SETUP DIALOG ---
        if (showGoLiveDialog) {
            AlertDialog(
                onDismissRequest = { showGoLiveDialog = false },
                containerColor = DarkSurface,
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Radio, contentDescription = null, tint = RomanticRed)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Start Live Broadcast 🎙️", color = TextPrimary, fontWeight = FontWeight.Bold)
                    }
                },
                text = {
                    Column {
                        OutlinedTextField(
                            value = liveTitle,
                            onValueChange = { liveTitle = it },
                            placeholder = { Text("Stream Title (e.g. Late Night Coding & Hangout)", color = TextMuted) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedBorderColor = RomanticRed,
                                unfocusedBorderColor = DarkSurfaceBorder
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text("Select Category:", color = TextSecondary, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(6.dp))

                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(listOf("Chat & Hangout", "Music & Hangout", "Creative & Photo", "Culinary & Food", "Tech & Gaming")) { cat ->
                                val isSel = liveCategory == cat
                                Surface(
                                    color = if (isSel) RomanticRed else DarkSurfaceVariant,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.clickable { liveCategory = cat }
                                ) {
                                    Text(
                                        text = cat,
                                        color = if (isSel) Color.White else TextSecondary,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                val created = repository.startLiveStream(
                                    title = liveTitle.ifBlank { "My Live Broadcast 🚀" },
                                    category = liveCategory,
                                    tags = liveTags
                                )
                                showGoLiveDialog = false
                                onOpenLiveViewer(created)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = RomanticRed),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Go LIVE Now", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    Button(
                        onClick = { showGoLiveDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                    ) {
                        Text("Cancel", color = TextSecondary)
                    }
                }
            )
        }
    }
}

@Composable
fun FullscreenLiveViewer(
    stream: LiveStreamEntity,
    repository: KinSphereRepository,
    onClose: () -> Unit
) {
    val allUsers by repository.getAllUsers().collectAsState(initial = emptyList())
    val userMap = remember(allUsers) { allUsers.associateBy { it.id } }
    val hostUser = userMap[stream.hostUserId]

    var liveLikesCount by remember { mutableIntStateOf(stream.likesCount) }
    var commentText by remember { mutableStateOf("") }
    val commentsList = remember {
        mutableStateListOf(
            LiveCommentItem("c1", "Ahmed Al-Mansoor", "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=200", "Hello from Baghdad! Audio is super clear! 🔥"),
            LiveCommentItem("c2", "Elena Rossi", "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=200", "Sending love from Rome! ❤️✨"),
            LiveCommentItem("c3", "Mohammed Khalil", "https://images.unsplash.com/photo-1570295999919-56ceb5ecca61?w=200", "Awesome background vibe! 🎧🚀")
        )
    }

    val floatingHearts = remember { mutableStateListOf<FloatingHeart>() }
    var showGiftSheet by remember { mutableStateOf(false) }
    var latestGiftReceived by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    // Function to add floating heart
    fun triggerHeart() {
        val newHeart = FloatingHeart(
            id = System.currentTimeMillis() + Random.nextLong(1000),
            offsetX = Random.nextFloat() * 120f - 60f,
            color = listOf(RomanticRed, PrimaryNeon, StreakFlameOrange, Color(0xFFFF758C)).random(),
            scale = Random.nextFloat() * 0.4f + 0.8f
        )
        floatingHearts.add(newHeart)
        liveLikesCount++
        coroutineScope.launch {
            repository.sendLiveLike(stream.id, 1)
            delay(2500)
            floatingHearts.remove(newHeart)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .testTag("fullscreen_live_viewer_${stream.id}")
    ) {
        // --- LIVE VIDEO SIMULATION CANVAS ---
        AsyncImage(
            model = stream.coverUrl.ifBlank { "https://images.unsplash.com/photo-1502602898657-3e91760cbb34?w=1000" },
            contentDescription = "Live Video Feed",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures(
                        onDoubleTap = { triggerHeart() }
                    )
                }
        )

        // Gradient overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.Black.copy(alpha = 0.7f),
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.85f)
                        )
                    )
                )
        )

        // --- TOP STREAM HEADER ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 40.dp, start = 16.dp, end = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Host Pill
            Surface(
                color = Color.Black.copy(alpha = 0.6f),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    UserAvatar(
                        avatarUrl = hostUser?.avatarUrl ?: "",
                        displayName = hostUser?.displayName ?: "Host",
                        size = 32.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = hostUser?.displayName ?: "Host",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = stream.title,
                            color = TextSecondary,
                            fontSize = 10.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.width(110.dp)
                        )
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Live Viewers Badge
                Surface(
                    color = RomanticRed,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Default.FiberManualRecord, contentDescription = null, tint = Color.White, modifier = Modifier.size(10.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("LIVE", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("${stream.viewerCount}", color = Color.White, fontSize = 11.sp)
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = onClose,
                    modifier = Modifier.background(Color.Black.copy(alpha = 0.5f), CircleShape)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close Live", tint = Color.White)
                }
            }
        }

        // --- FLOATING HEARTS ANIMATION TOWER ---
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 90.dp, end = 20.dp)
                .size(140.dp, 280.dp)
        ) {
            floatingHearts.forEach { heart ->
                val animProgress = remember { Animatable(0f) }
                LaunchedEffect(heart.id) {
                    animProgress.animateTo(
                        targetValue = 1f,
                        animationSpec = tween(durationMillis = 2200, easing = LinearEasing)
                    )
                }
                val yOffset = -(animProgress.value * 240.dp.value)
                val alpha = 1f - animProgress.value

                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = null,
                    tint = heart.color.copy(alpha = alpha),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .offset(x = heart.offsetX.dp, y = yOffset.dp)
                        .scale(heart.scale * (1f + (animProgress.value * 0.3f)))
                        .size(28.dp)
                )
            }
        }

        // --- GIFT CELEBRATION TOAST BANNER ---
        if (latestGiftReceived != null) {
            Surface(
                color = StreakFlameOrange.copy(alpha = 0.95f),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(16.dp)
            ) {
                Text(
                    text = latestGiftReceived ?: "",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp)
                )
            }
        }

        // --- BOTTOM LIVE CHAT & ACTION BAR ---
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp)
        ) {
            // Comments Scroll Overlay (Latest 4 comments)
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(commentsList.takeLast(6)) { item ->
                    Surface(
                        color = Color.Black.copy(alpha = 0.55f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.width(300.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                        ) {
                            UserAvatar(avatarUrl = item.senderAvatar, displayName = item.senderName, size = 20.dp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(item.senderName, color = PrimaryNeon, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Text(item.text, color = Color.White, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Comment Composer & Quick Reaction Buttons
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = commentText,
                    onValueChange = { commentText = it },
                    placeholder = { Text("Comment live...", color = TextMuted, fontSize = 12.sp) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = RomanticRed,
                        unfocusedBorderColor = DarkSurfaceBorder,
                        focusedContainerColor = Color.Black.copy(alpha = 0.6f),
                        unfocusedContainerColor = Color.Black.copy(alpha = 0.6f)
                    ),
                    shape = RoundedCornerShape(24.dp),
                    trailingIcon = {
                        if (commentText.isNotBlank()) {
                            IconButton(
                                onClick = {
                                    commentsList.add(
                                        LiveCommentItem(
                                            id = "c_${System.currentTimeMillis()}",
                                            senderName = "You",
                                            senderAvatar = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=200",
                                            text = commentText
                                        )
                                    )
                                    commentText = ""
                                }
                            ) {
                                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = PrimaryNeon)
                            }
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Virtual Gift Button
                IconButton(
                    onClick = { showGiftSheet = true },
                    modifier = Modifier
                        .size(46.dp)
                        .background(StreakFlameOrange, CircleShape)
                        .testTag("live_gift_button")
                ) {
                    Icon(Icons.Default.CardGiftcard, contentDescription = "Send Gift", tint = Color.White)
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Floating Heart Burst Button
                IconButton(
                    onClick = { triggerHeart() },
                    modifier = Modifier
                        .size(46.dp)
                        .background(RomanticRed, CircleShape)
                        .testTag("live_heart_button")
                ) {
                    Icon(Icons.Default.Favorite, contentDescription = "Like", tint = Color.White)
                }
            }
        }

        // --- VIRTUAL GIFTS SELECTION MODAL ---
        if (showGiftSheet) {
            AlertDialog(
                onDismissRequest = { showGiftSheet = false },
                containerColor = DarkSurface,
                title = {
                    Text("Send Stream Gift 🎁", color = TextPrimary, fontWeight = FontWeight.Bold)
                },
                text = {
                    Row(
                        horizontalArrangement = Arrangement.SpaceAround,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        VIRTUAL_GIFTS.forEach { gift ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .clickable {
                                        latestGiftReceived = "You sent ${gift.name} ${gift.emoji}!"
                                        showGiftSheet = false
                                        triggerHeart()
                                        coroutineScope.launch {
                                            delay(3000)
                                            latestGiftReceived = null
                                        }
                                    }
                                    .padding(6.dp)
                            ) {
                                Text(gift.emoji, fontSize = 32.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(gift.name, color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Text("${gift.diamonds} 💎", color = StreakFlameYellow, fontSize = 10.sp)
                            }
                        }
                    }
                },
                confirmButton = {},
                dismissButton = {
                    Button(
                        onClick = { showGiftSheet = false },
                        colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant)
                    ) {
                        Text("Close", color = TextSecondary)
                    }
                }
            )
        }
    }
}
