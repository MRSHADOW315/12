package com.example.ui.camera

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.FlashAuto
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.ViewCarousel
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.repository.KinSphereRepository
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceBorder
import com.example.ui.theme.PrimaryNeon
import com.example.ui.theme.RomanticRed
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class CameraMode(val label: String) {
    PHOTO("PHOTO"),
    VIDEO("VIDEO"),
    BEREAL("DUAL BEREAL")
}

data class CameraFilter(
    val id: String,
    val name: String,
    val emoji: String,
    val tintColor: Color,
    val description: String
)

val CAMERA_FILTERS = listOf(
    CameraFilter("raw", "Normal", "✨", Color.Transparent, "Natural daylight clarity"),
    CameraFilter("cyberpunk", "Cyberpunk", "🌆", Color(0xFF00E5FF).copy(alpha = 0.25f), "Neon cyan & magenta night contrast"),
    CameraFilter("vintage", "Vintage 90s", "🎞️", Color(0xFFD4A373).copy(alpha = 0.3f), "Warm film grain & nostalgic warmth"),
    CameraFilter("golden", "Golden Hour", "🌅", Color(0xFFFFB703).copy(alpha = 0.28f), "Radiant sunset amber glow"),
    CameraFilter("emerald", "Emerald", "🌿", Color(0xFF2EC4B6).copy(alpha = 0.22f), "Deep cinematic teal-green hues"),
    CameraFilter("noir", "Film Noir", "🎬", Color.Black.copy(alpha = 0.35f), "High drama monochrome contrast"),
    CameraFilter("vaporwave", "Vaporwave", "💜", Color(0xFFE056FD).copy(alpha = 0.25f), "Retro 80s synth dreamscape"),
    CameraFilter("anime", "Anime Bloom", "🌸", Color(0xFFFF758C).copy(alpha = 0.22f), "Soft diffused glow & vibrant highlights")
)

@Composable
fun CameraStudioScreen(
    repository: KinSphereRepository,
    onClose: () -> Unit,
    onNavigateToStory: (() -> Unit)? = null,
    onNavigateToShorts: (() -> Unit)? = null,
    onNavigateToBeReal: (() -> Unit)? = null
) {
    var cameraMode by remember { mutableStateOf(CameraMode.PHOTO) }
    var selectedFilter by remember { mutableStateOf(CAMERA_FILTERS[0]) }
    var isFrontCamera by remember { mutableStateOf(false) }
    var flashMode by remember { mutableIntStateOf(0) } // 0: Auto, 1: On, 2: Off
    var timerSeconds by remember { mutableIntStateOf(0) } // 0, 3, 10
    var countdownValue by remember { mutableIntStateOf(0) }
    var isCountingDown by remember { mutableStateOf(false) }
    var speedMultiplier by remember { mutableStateOf("1x") }

    // Video Recording State
    var isRecording by remember { mutableStateOf(false) }
    var recordingDurationSeconds by remember { mutableIntStateOf(0) }

    // Captured Media State
    var capturedPreviewUrl by remember { mutableStateOf<String?>(null) }
    var capturedSecondaryUrl by remember { mutableStateOf<String?>(null) } // For Dual BeReal
    var showPostDialog by remember { mutableStateOf(false) }
    var postCaption by remember { mutableStateOf("") }
    var isSubmittingPost by remember { mutableStateOf(false) }

    // BeReal PiP Swap State
    var isPipSwapped by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    // Timer countdown effect
    LaunchedEffect(isCountingDown) {
        if (isCountingDown && timerSeconds > 0) {
            countdownValue = timerSeconds
            while (countdownValue > 0) {
                delay(1000)
                countdownValue--
            }
            isCountingDown = false
            // Trigger capture
            capturedPreviewUrl = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=1000"
            if (cameraMode == CameraMode.BEREAL) {
                capturedSecondaryUrl = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=500"
            }
            showPostDialog = true
        }
    }

    // Video recording timer
    LaunchedEffect(isRecording) {
        if (isRecording) {
            recordingDurationSeconds = 0
            while (isRecording) {
                delay(1000)
                recordingDurationSeconds++
            }
        }
    }

    val samplePrimaryStream = if (isFrontCamera) {
        "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=1000"
    } else {
        "https://images.unsplash.com/photo-1502602898657-3e91760cbb34?w=1000"
    }

    val sampleSecondaryStream = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=500"

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .testTag("camera_studio_screen")
    ) {
        // --- LIVE CAMERA VIEWPORT WITH SELECTED FILTER APPLIED ---
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(0.dp))
        ) {
            val mainImage = if (cameraMode == CameraMode.BEREAL && isPipSwapped) sampleSecondaryStream else samplePrimaryStream
            AsyncImage(
                model = mainImage,
                contentDescription = "Camera Live View",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // AR Color Filter Shader Overlay
            if (selectedFilter.id != "raw") {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    selectedFilter.tintColor,
                                    selectedFilter.tintColor.copy(alpha = selectedFilter.tintColor.alpha * 0.7f),
                                    Color.Black.copy(alpha = 0.4f)
                                )
                            )
                        )
                )
            }

            // DUAL-CAMERA BEREAL Picture-in-Picture Frame
            if (cameraMode == CameraMode.BEREAL) {
                val pipImage = if (isPipSwapped) samplePrimaryStream else sampleSecondaryStream
                Box(
                    modifier = Modifier
                        .padding(top = 90.dp, start = 20.dp)
                        .size(width = 110.dp, height = 150.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .border(2.dp, Color.White, RoundedCornerShape(16.dp))
                        .clickable { isPipSwapped = !isPipSwapped }
                        .testTag("bereal_pip_window")
                ) {
                    AsyncImage(
                        model = pipImage,
                        contentDescription = "Selfie PiP",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Surface(
                        color = Color.Black.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(bottomStart = 8.dp),
                        modifier = Modifier.align(Alignment.TopEnd)
                    ) {
                        Text(
                            text = "🔄 Swap",
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            // Dark gradient vignette for top & bottom controls
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .align(Alignment.TopCenter)
                    .background(Brush.verticalGradient(listOf(Color.Black.copy(alpha = 0.8f), Color.Transparent)))
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .align(Alignment.BottomCenter)
                    .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.95f))))
            )
        }

        // --- TOP ACTION BAR ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 40.dp, start = 16.dp, end = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onClose,
                modifier = Modifier
                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                    .testTag("camera_close_button")
            ) {
                Icon(Icons.Default.Close, contentDescription = "Close Camera", tint = Color.White)
            }

            // Flash & Timer and Speed pills
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                // Flash Toggle
                Surface(
                    color = Color.Black.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.clickable { flashMode = (flashMode + 1) % 3 }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = when (flashMode) {
                                1 -> Icons.Default.FlashOn
                                2 -> Icons.Default.FlashOff
                                else -> Icons.Default.FlashAuto
                            },
                            contentDescription = "Flash",
                            tint = if (flashMode == 1) PrimaryNeon else Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = when (flashMode) {
                                1 -> "ON"
                                2 -> "OFF"
                                else -> "AUTO"
                            },
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Timer Delay
                Surface(
                    color = Color.Black.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.clickable {
                        timerSeconds = when (timerSeconds) {
                            0 -> 3
                            3 -> 10
                            else -> 0
                        }
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Timer, contentDescription = "Timer", tint = if (timerSeconds > 0) PrimaryNeon else Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (timerSeconds == 0) "OFF" else "${timerSeconds}s",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Speed Selector
                Surface(
                    color = Color.Black.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.clickable {
                        speedMultiplier = when (speedMultiplier) {
                            "1x" -> "2x"
                            "2x" -> "0.5x"
                            else -> "1x"
                        }
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Speed, contentDescription = "Speed", tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(speedMultiplier, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Flip Camera
            IconButton(
                onClick = { isFrontCamera = !isFrontCamera },
                modifier = Modifier
                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                    .testTag("camera_flip_button")
            ) {
                Icon(Icons.Default.Cameraswitch, contentDescription = "Flip Camera", tint = Color.White)
            }
        }

        // --- COUNTDOWN OVERLAY ---
        if (isCountingDown) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$countdownValue",
                    color = PrimaryNeon,
                    fontSize = 90.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }

        // --- RECORDING DURATION INDICATOR (VIDEO MODE) ---
        if (isRecording) {
            Surface(
                color = RomanticRed,
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 100.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(Color.White, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    val mins = recordingDurationSeconds / 60
                    val secs = recordingDurationSeconds % 60
                    Text(
                        text = String.format("%02d:%02d REC", mins, secs),
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // --- BOTTOM CONTROLS & FILTER CAROUSEL ---
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Filter Selection Carousel
            Text(
                text = "${selectedFilter.emoji} ${selectedFilter.name} Filter",
                color = PrimaryNeon,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                items(CAMERA_FILTERS) { filter ->
                    val isSelected = selectedFilter.id == filter.id
                    Surface(
                        color = if (isSelected) PrimaryNeon else DarkSurface.copy(alpha = 0.85f),
                        shape = RoundedCornerShape(20.dp),
                        border = BorderStroke(1.dp, if (isSelected) PrimaryNeon else DarkSurfaceBorder),
                        modifier = Modifier
                            .clickable { selectedFilter = filter }
                            .testTag("filter_item_${filter.id}")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                        ) {
                            Text(text = filter.emoji, fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = filter.name,
                                color = if (isSelected) Color.Black else Color.White,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Main Shutter Button Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Gallery Thumbnail Shortcut
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.5.dp, Color.White.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                        .clickable {
                            capturedPreviewUrl = "https://images.unsplash.com/photo-1502602898657-3e91760cbb34?w=800"
                            showPostDialog = true
                        }
                ) {
                    AsyncImage(
                        model = "https://images.unsplash.com/photo-1502602898657-3e91760cbb34?w=200",
                        contentDescription = "Gallery",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // Shutter Button
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .border(4.dp, if (isRecording) RomanticRed else Color.White, CircleShape)
                        .padding(6.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                isRecording -> RomanticRed
                                cameraMode == CameraMode.VIDEO -> RomanticRed
                                cameraMode == CameraMode.BEREAL -> Color.White
                                else -> PrimaryNeon
                            }
                        )
                        .clickable {
                            if (timerSeconds > 0 && !isCountingDown) {
                                isCountingDown = true
                            } else if (cameraMode == CameraMode.VIDEO) {
                                if (isRecording) {
                                    isRecording = false
                                    capturedPreviewUrl = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=800"
                                    showPostDialog = true
                                } else {
                                    isRecording = true
                                }
                            } else {
                                // Instant Photo / Dual BeReal Capture
                                capturedPreviewUrl = if (isFrontCamera) samplePrimaryStream else "https://images.unsplash.com/photo-1502602898657-3e91760cbb34?w=1000"
                                if (cameraMode == CameraMode.BEREAL) {
                                    capturedSecondaryUrl = sampleSecondaryStream
                                }
                                showPostDialog = true
                            }
                        }
                        .testTag("camera_shutter_button"),
                    contentAlignment = Alignment.Center
                ) {
                    if (cameraMode == CameraMode.VIDEO && !isRecording) {
                        Icon(Icons.Default.Videocam, contentDescription = "Record", tint = Color.White)
                    } else if (cameraMode == CameraMode.BEREAL) {
                        Text("2📷", fontSize = 20.sp)
                    } else if (!isRecording) {
                        Icon(Icons.Default.CameraAlt, contentDescription = "Snap", tint = Color.Black)
                    }
                }

                // AR Sticker / Filter Quick Toggle
                IconButton(
                    onClick = {
                        val nextIdx = (CAMERA_FILTERS.indexOf(selectedFilter) + 1) % CAMERA_FILTERS.size
                        selectedFilter = CAMERA_FILTERS[nextIdx]
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = "Next Filter", tint = PrimaryNeon)
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Camera Mode Selector: PHOTO | VIDEO | BEREAL
            Row(
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CameraMode.values().forEach { mode ->
                    val isSelected = cameraMode == mode
                    Text(
                        text = mode.label,
                        color = if (isSelected) PrimaryNeon else TextMuted,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        modifier = Modifier
                            .clickable {
                                if (!isRecording) cameraMode = mode
                            }
                            .testTag("mode_${mode.name.lowercase()}")
                    )
                }
            }
        }

        // --- POST / PUBLISH MODAL DIALOG ---
        if (showPostDialog && capturedPreviewUrl != null) {
            AlertDialog(
                onDismissRequest = {
                    showPostDialog = false
                    capturedPreviewUrl = null
                    capturedSecondaryUrl = null
                },
                containerColor = DarkSurface,
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = when (cameraMode) {
                                CameraMode.BEREAL -> "Publish BeReal Dual ✨"
                                CameraMode.VIDEO -> "Publish Short Reel 🎬"
                                else -> "Share Photo Moment 📸"
                            },
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        )
                    }
                },
                text = {
                    Column {
                        // Thumbnail Preview
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                                .clip(RoundedCornerShape(12.dp))
                        ) {
                            AsyncImage(
                                model = capturedPreviewUrl,
                                contentDescription = "Preview",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            if (capturedSecondaryUrl != null) {
                                Box(
                                    modifier = Modifier
                                        .padding(8.dp)
                                        .size(50.dp, 70.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .border(1.5.dp, Color.White, RoundedCornerShape(8.dp))
                                ) {
                                    AsyncImage(
                                        model = capturedSecondaryUrl,
                                        contentDescription = "Selfie PiP",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            }
                            Surface(
                                color = Color.Black.copy(alpha = 0.7f),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(8.dp)
                            ) {
                                Text(
                                    text = "Filter: ${selectedFilter.name}",
                                    color = PrimaryNeon,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = postCaption,
                            onValueChange = { postCaption = it },
                            placeholder = { Text("Write a caption with #hashtags...", color = TextMuted) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedBorderColor = PrimaryNeon,
                                unfocusedBorderColor = DarkSurfaceBorder
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Text("Choose where to publish:", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(8.dp))

                        // Target Destination Buttons
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Button(
                                onClick = {
                                    isSubmittingPost = true
                                    coroutineScope.launch {
                                        if (cameraMode == CameraMode.BEREAL) {
                                            repository.postBeReal(
                                                primaryImageUrl = capturedPreviewUrl ?: "",
                                                secondaryImageUrl = capturedSecondaryUrl ?: "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=500",
                                                caption = postCaption.ifBlank { "Dual camera real moment ✨" },
                                                filterApplied = selectedFilter.name
                                            )
                                            onNavigateToBeReal?.invoke()
                                        } else if (cameraMode == CameraMode.VIDEO) {
                                            repository.uploadShortVideo(
                                                caption = postCaption.ifBlank { "New reel from Camera Studio! 🔥" },
                                                filterName = selectedFilter.name
                                            )
                                            onNavigateToShorts?.invoke()
                                        } else {
                                            repository.createStory(
                                                mediaUrl = capturedPreviewUrl ?: "",
                                                caption = postCaption.ifBlank { "Story captured with ${selectedFilter.name} filter 📸" }
                                            )
                                            onNavigateToStory?.invoke()
                                        }
                                        isSubmittingPost = false
                                        showPostDialog = false
                                        onClose()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryNeon),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("publish_confirm_button")
                            ) {
                                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isSubmittingPost) "Posting..." else "Post Now",
                                    color = Color.Black,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                },
                confirmButton = {},
                dismissButton = {
                    OutlinedButton(
                        onClick = { showPostDialog = false },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Retake")
                    }
                }
            )
        }
    }
}
