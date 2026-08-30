package com.example.ui.stories

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import com.example.data.local.entities.StoryEntity
import com.example.data.local.entities.UserEntity
import com.example.data.model.StoryVisibility
import com.example.data.repository.KinSphereRepository
import com.example.ui.components.UserAvatar
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceBorder
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.PrimaryLight
import com.example.ui.theme.PrimaryNeon
import com.example.ui.theme.RomanticRed
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun StoriesScreen(
    repository: KinSphereRepository,
    onNavigateToChat: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val currentUserId by repository.currentUserId.collectAsState()
    val allStories by repository.getActiveStories().collectAsState(initial = emptyList())
    val allUsers by repository.getAllUsers().collectAsState(initial = emptyList())
    val userMap = remember(allUsers) { allUsers.associateBy { it.id } }

    var selectedStory by remember { mutableStateOf<StoryEntity?>(null) }
    var showCreateStoryDialog by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .testTag("stories_screen_root")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(16.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text(
                        text = "KinSphere Moments",
                        color = TextPrimary,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Ephemeral 24h stories with real-time reactions",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }

                Surface(
                    color = PrimaryNeon,
                    shape = CircleShape,
                    modifier = Modifier
                        .size(44.dp)
                        .clickable { showCreateStoryDialog = true }
                        .testTag("create_story_button")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Add, contentDescription = "Add Story", tint = Color.White)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Stories Tray (Snapchat-style avatars)
            Text(
                text = "Recent Friends",
                color = TextSecondary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Add your own story item
                item {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable { showCreateStoryDialog = true }
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(60.dp)
                                .clip(CircleShape)
                                .background(DarkSurfaceVariant)
                                .border(1.5.dp, PrimaryNeon, CircleShape)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = PrimaryLight)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Add Story", fontSize = 11.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
                    }
                }

                items(allStories) { story ->
                    val author = userMap[story.userId]
                    if (author != null) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clickable {
                                    selectedStory = story
                                    coroutineScope.launch { repository.recordStoryView(story.id) }
                                }
                                .testTag("story_item_${author.username}")
                        ) {
                            UserAvatar(
                                avatarUrl = author.avatarUrl,
                                displayName = author.displayName,
                                size = 60.dp,
                                hasStory = true
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = author.displayName.split(" ").first(),
                                fontSize = 11.sp,
                                color = TextPrimary,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Story Grid Feed
            Text(
                text = "Discover Feed",
                color = TextSecondary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(10.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(allStories) { story ->
                    val author = userMap[story.userId]
                    Card(
                        colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .height(200.dp)
                            .clickable {
                                selectedStory = story
                                coroutineScope.launch { repository.recordStoryView(story.id) }
                            }
                            .testTag("story_card_${story.id}")
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            if (story.mediaUrl.isNotEmpty()) {
                                AsyncImage(
                                    model = story.mediaUrl,
                                    contentDescription = story.textCaption,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }

                            // Dark overlay gradient
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f))
                                        )
                                    )
                            )

                            // Bottom Caption & Author
                            Column(
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(10.dp)
                            ) {
                                if (author != null) {
                                    Text(
                                        text = author.displayName,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                                Text(
                                    text = story.textCaption,
                                    color = Color.White.copy(alpha = 0.85f),
                                    fontSize = 11.sp,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }

        // --- Full Screen Story Viewer Dialog ---
        if (selectedStory != null) {
            val story = selectedStory!!
            val author = userMap[story.userId]

            var progress by remember { mutableFloatStateOf(0f) }
            LaunchedEffect(story.id) {
                progress = 0f
                val totalSteps = 100
                for (i in 1..totalSteps) {
                    delay(50)
                    progress = i / 100f
                }
                selectedStory = null // Auto advance/close
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
                    .testTag("full_story_viewer")
            ) {
                if (story.mediaUrl.isNotEmpty()) {
                    AsyncImage(
                        model = story.mediaUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // Top Progress Bar & Close
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(16.dp)
                ) {
                    LinearProgressIndicator(
                        progress = { progress },
                        color = Color.White,
                        trackColor = Color.White.copy(alpha = 0.3f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(3.dp)
                            .clip(CircleShape)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (author != null) {
                                UserAvatar(avatarUrl = author.avatarUrl, displayName = author.displayName, size = 36.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(author.displayName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text("2h ago • ${story.visibility.label}", color = Color.White.copy(alpha = 0.7f), fontSize = 10.sp)
                                }
                            }
                        }

                        IconButton(onClick = { selectedStory = null }) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                        }
                    }
                }

                // Bottom Caption & Reaction Bar
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    if (story.textCaption.isNotEmpty()) {
                        Surface(
                            color = Color.Black.copy(alpha = 0.6f),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.padding(bottom = 12.dp)
                        ) {
                            Text(
                                text = story.textCaption,
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                            )
                        }
                    }

                    // Quick Reaction Row
                    Row(
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        listOf("❤️", "🔥", "😂", "😮", "👏").forEach { emoji ->
                            Surface(
                                color = Color.White.copy(alpha = 0.2f),
                                shape = CircleShape,
                                modifier = Modifier
                                    .size(44.dp)
                                    .clickable {
                                        // React & send
                                        selectedStory = null
                                    }
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(emoji, fontSize = 20.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- Create Story Dialog ---
        if (showCreateStoryDialog) {
            var storyCaption by remember { mutableStateOf("") }
            var selectedVisibility by remember { mutableStateOf(StoryVisibility.EVERYONE) }

            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryNeon),
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth(0.92f)
                    .padding(16.dp)
                    .testTag("create_story_dialog")
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Post to Story", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        IconButton(onClick = { showCreateStoryDialog = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = storyCaption,
                        onValueChange = { storyCaption = it },
                        placeholder = { Text("What's happening right now?", color = TextMuted, fontSize = 13.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = DarkSurfaceVariant,
                            unfocusedContainerColor = DarkSurfaceVariant,
                            focusedBorderColor = PrimaryNeon,
                            unfocusedBorderColor = DarkSurfaceBorder
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .testTag("story_caption_input")
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Text("Story Privacy", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        listOf(StoryVisibility.EVERYONE, StoryVisibility.FRIENDS, StoryVisibility.FOLLOWERS).forEach { vis ->
                            val isSel = selectedVisibility == vis
                            Surface(
                                color = if (isSel) PrimaryNeon else DarkSurfaceVariant,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { selectedVisibility = vis }
                            ) {
                                Text(
                                    text = vis.label.split(" ").first(),
                                    color = if (isSel) Color.White else TextSecondary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            coroutineScope.launch {
                                repository.postStory(
                                    mediaUrl = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=600",
                                    textCaption = storyCaption,
                                    visibility = selectedVisibility
                                )
                                showCreateStoryDialog = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryNeon),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("publish_story_btn")
                    ) {
                        Text("Share to KinSphere", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
