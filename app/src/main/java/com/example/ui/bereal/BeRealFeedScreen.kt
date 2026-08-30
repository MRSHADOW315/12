package com.example.ui.bereal

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.local.entities.BeRealPostEntity
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
import kotlinx.coroutines.launch

@Composable
fun BeRealFeedScreen(
    repository: KinSphereRepository,
    onTakeBeReal: () -> Unit
) {
    val beRealPosts by repository.getAllBeRealPosts().collectAsState(initial = emptyList())
    val allUsers by repository.getAllUsers().collectAsState(initial = emptyList())
    val userMap = remember(allUsers) { allUsers.associateBy { it.id } }

    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(top = 16.dp)
            .testTag("bereal_feed_screen")
    ) {
        // --- BEREAL DAILY ALERT BANNER ---
        Card(
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.5.dp, StreakFlameYellow),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        color = StreakFlameYellow.copy(alpha = 0.2f),
                        shape = CircleShape,
                        modifier = Modifier.size(42.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("⚠️", fontSize = 20.sp)
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Time to BeReal. ⚡", color = TextPrimary, fontWeight = FontWeight.Black, fontSize = 15.sp)
                        Text("2 min left to capture authentic moment", color = TextSecondary, fontSize = 11.sp)
                    }
                }

                Button(
                    onClick = onTakeBeReal,
                    colors = ButtonDefaults.buttonColors(containerColor = StreakFlameYellow),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("take_bereal_button")
                ) {
                    Text("Post", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // --- POSTS LIST ---
        if (beRealPosts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("📷 No BeReal moments shared today", color = TextSecondary, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = onTakeBeReal,
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryNeon)
                    ) {
                        Text("Post your Dual BeReal", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(beRealPosts) { post ->
                    val user = userMap[post.userId]
                    SingleBeRealCard(
                        post = post,
                        user = user,
                        onReact = { emoji ->
                            coroutineScope.launch { repository.reactBeReal(post.id, emoji) }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun SingleBeRealCard(
    post: BeRealPostEntity,
    user: com.example.data.local.entities.UserEntity?,
    onReact: (String) -> Unit
) {
    var isSwapped by remember { mutableStateOf(false) }
    var selectedReaction by remember { mutableStateOf<String?>(null) }

    val mainImage = if (isSwapped) post.secondaryImageUrl else post.primaryImageUrl
    val pipImage = if (isSwapped) post.primaryImageUrl else post.secondaryImageUrl

    Card(
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, DarkSurfaceBorder),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("bereal_post_card_${post.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header: User avatar, name, late/on-time badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    UserAvatar(
                        avatarUrl = user?.avatarUrl ?: "",
                        displayName = user?.displayName ?: "User",
                        size = 38.dp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = user?.displayName ?: "Explorer",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (post.locationName.isNotBlank()) {
                                Icon(Icons.Default.LocationOn, contentDescription = null, tint = PrimaryNeon, modifier = Modifier.size(11.dp))
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(post.locationName, color = TextSecondary, fontSize = 11.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                            }
                            Text(
                                text = if (post.takenLateSeconds > 0) "${post.takenLateSeconds / 60} min late" else "On time ⚡",
                                color = if (post.takenLateSeconds > 0) StreakFlameOrange else PrimaryNeon,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Surface(
                    color = DarkSurfaceVariant,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = post.filterApplied,
                        color = TextMuted,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Dual Photo View (Main + PiP)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(0.85f)
                    .clip(RoundedCornerShape(16.dp))
            ) {
                // Main Photo
                AsyncImage(
                    model = mainImage,
                    contentDescription = "BeReal Main",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Secondary PiP Photo (Tap to swap!)
                Box(
                    modifier = Modifier
                        .padding(12.dp)
                        .size(width = 95.dp, height = 130.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .border(2.dp, Color.White, RoundedCornerShape(14.dp))
                        .clickable { isSwapped = !isSwapped }
                        .testTag("swap_pip_button_${post.id}")
                ) {
                    AsyncImage(
                        model = pipImage,
                        contentDescription = "BeReal PiP",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Surface(
                        color = Color.Black.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(bottomStart = 6.dp),
                        modifier = Modifier.align(Alignment.TopEnd)
                    ) {
                        Text(
                            text = "🔄 Tap",
                            color = Color.White,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 3.dp, vertical = 1.dp)
                        )
                    }
                }
            }

            // Caption
            if (post.caption.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = post.caption,
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // RealMojis Reaction Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val emojis = listOf("🔥", "😍", "😂", "😲", "👍", "⚡")
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    emojis.forEach { emoji ->
                        val isSelected = selectedReaction == emoji
                        Surface(
                            color = if (isSelected) PrimaryNeon.copy(alpha = 0.25f) else DarkSurfaceVariant,
                            shape = CircleShape,
                            border = BorderStroke(1.dp, if (isSelected) PrimaryNeon else DarkSurfaceBorder),
                            modifier = Modifier
                                .clickable {
                                    selectedReaction = emoji
                                    onReact(emoji)
                                }
                                .testTag("realmoji_${emoji}_${post.id}")
                        ) {
                            Text(
                                text = emoji,
                                fontSize = 16.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
