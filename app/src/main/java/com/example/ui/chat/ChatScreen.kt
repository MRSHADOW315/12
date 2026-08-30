package com.example.ui.chat

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Timer
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.ConversationEntity
import com.example.data.local.entities.MessageEntity
import com.example.data.local.entities.StreakEntity
import com.example.data.local.entities.UserEntity
import com.example.data.model.MessageType
import com.example.data.repository.KinSphereRepository
import com.example.ui.components.SendStreakSnapDialog
import com.example.ui.components.StreakDetailsDialog
import com.example.ui.components.StreakFlameBadge
import com.example.ui.components.StreakFlameOrange
import com.example.ui.components.StreaksTray
import com.example.ui.components.UserAvatar
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceBorder
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.GhostModePurple
import com.example.ui.theme.PrimaryLight
import com.example.ui.theme.PrimaryNeon
import com.example.ui.theme.RomanticRed
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ChatScreen(
    repository: KinSphereRepository,
    initialTargetUserId: String? = null,
    onNavigateToProfile: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val currentUserId by repository.currentUserId.collectAsState()
    val conversations by repository.getConversations().collectAsState(initial = emptyList())
    val allUsers by repository.getAllUsers().collectAsState(initial = emptyList())
    val userMap = remember(allUsers) { allUsers.associateBy { it.id } }
    val userStreaks by repository.getStreaksForCurrentUser().collectAsState(initial = emptyList())

    var activeConversationId by remember { mutableStateOf<String?>(null) }
    var activeTargetUserId by remember { mutableStateOf<String?>(null) }

    var selectedStreakForDialog by remember { mutableStateOf<StreakEntity?>(null) }
    var streakSnapTargetUser by remember { mutableStateOf<UserEntity?>(null) }

    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(initialTargetUserId) {
        if (!initialTargetUserId.isNullOrEmpty()) {
            activeTargetUserId = initialTargetUserId
            val convId = repository.startOrGetDirectConversation(initialTargetUserId)
            activeConversationId = convId
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .testTag("chat_screen_root")
    ) {
        if (activeConversationId == null) {
            // --- Conversations Inbox List ---
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "Encrypted Messages",
                    color = TextPrimary,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Zero-leak communication with active friendship streaks 🔥",
                    color = TextSecondary,
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Streaks Horizontal Tray
                StreaksTray(
                    streaks = userStreaks,
                    userMap = userMap,
                    currentUserId = currentUserId,
                    onStreakClick = { streak ->
                        selectedStreakForDialog = streak
                    },
                    onQuickSendStreak = { targetId ->
                        streakSnapTargetUser = userMap[targetId]
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Recent Chats",
                    color = TextMuted,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(conversations) { conv ->
                        // Determine the other user in 1-on-1 chats
                        val friendUser = allUsers.find { it.displayName == conv.title }
                        val matchingStreak = userStreaks.find {
                            (it.userAId == currentUserId && it.userBId == friendUser?.id) ||
                            (it.userBId == currentUserId && it.userAId == friendUser?.id)
                        }

                        Card(
                            colors = CardDefaults.cardColors(containerColor = DarkSurface),
                            shape = RoundedCornerShape(16.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, DarkSurfaceBorder),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    activeConversationId = conv.id
                                    activeTargetUserId = friendUser?.id
                                }
                                .testTag("conversation_item_${conv.id}")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(12.dp)
                            ) {
                                Box(contentAlignment = Alignment.BottomEnd) {
                                    UserAvatar(
                                        avatarUrl = friendUser?.avatarUrl ?: "",
                                        displayName = conv.title,
                                        size = 48.dp
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = conv.title,
                                                color = TextPrimary,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp
                                            )
                                            if (matchingStreak != null) {
                                                Spacer(modifier = Modifier.width(6.dp))
                                                StreakFlameBadge(
                                                    streak = matchingStreak,
                                                    onClick = { selectedStreakForDialog = matchingStreak }
                                                )
                                            }
                                        }
                                        Text(
                                            text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(conv.lastMessageTime)),
                                            color = TextMuted,
                                            fontSize = 11.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = conv.lastMessageText ?: "Tap to start conversation...",
                                        color = TextSecondary,
                                        fontSize = 12.sp,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // --- Active Chat Room View ---
            val messages by repository.getMessages(activeConversationId!!).collectAsState(initial = emptyList())
            var messageInput by remember { mutableStateOf("") }
            var isDisappearingMode by remember { mutableStateOf(false) }
            val listState = rememberLazyListState()

            val targetUser = if (activeTargetUserId != null) userMap[activeTargetUserId] else null
            val activeStreak = userStreaks.find {
                (it.userAId == currentUserId && it.userBId == activeTargetUserId) ||
                (it.userBId == currentUserId && it.userAId == activeTargetUserId)
            }

            LaunchedEffect(messages.size) {
                if (messages.isNotEmpty()) {
                    listState.animateScrollToItem(messages.size - 1)
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
            ) {
                // Room Header with Streak Badge & Disappearing toggle
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DarkSurface)
                        .border(1.dp, DarkSurfaceBorder)
                        .padding(horizontal = 8.dp, vertical = 8.dp)
                ) {
                    IconButton(onClick = {
                        activeConversationId = null
                        activeTargetUserId = null
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                if (activeTargetUserId != null) onNavigateToProfile(activeTargetUserId!!)
                            }
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = targetUser?.displayName ?: "Direct Message",
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            if (activeStreak != null) {
                                Spacer(modifier = Modifier.width(6.dp))
                                StreakFlameBadge(
                                    streak = activeStreak,
                                    onClick = { selectedStreakForDialog = activeStreak }
                                )
                            }
                        }
                        Text(
                            text = if (isDisappearingMode) "⏱ Disappearing Mode ON (60s)" else "● End-to-end encrypted",
                            color = if (isDisappearingMode) GhostModePurple else PrimaryLight,
                            fontSize = 11.sp
                        )
                    }

                    // Quick Snap / Streak Button
                    if (activeTargetUserId != null) {
                        IconButton(
                            onClick = { streakSnapTargetUser = targetUser },
                            modifier = Modifier.testTag("chat_header_streak_snap_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Bolt,
                                contentDescription = "Streak Snap",
                                tint = StreakFlameOrange
                            )
                        }
                    }

                    IconButton(
                        onClick = { isDisappearingMode = !isDisappearingMode },
                        modifier = Modifier.testTag("toggle_disappearing_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = "Disappearing",
                            tint = if (isDisappearingMode) GhostModePurple else TextSecondary
                        )
                    }
                }

                // Messages Thread
                LazyColumn(
                    state = listState,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    items(messages) { msg ->
                        val isMe = msg.senderId == currentUserId
                        val isStreakSnap = msg.text.contains("STREAK SNAP")

                        Row(
                            horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = when {
                                        isStreakSnap -> StreakFlameOrange
                                        isMe -> PrimaryNeon
                                        else -> DarkSurfaceVariant
                                    }
                                ),
                                shape = RoundedCornerShape(
                                    topStart = 16.dp,
                                    topEnd = 16.dp,
                                    bottomStart = if (isMe) 16.dp else 4.dp,
                                    bottomEnd = if (isMe) 4.dp else 16.dp
                                ),
                                modifier = Modifier
                                    .fillMaxWidth(0.82f)
                                    .testTag("chat_msg_${msg.id}")
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    if (isStreakSnap) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(bottom = 4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.LocalFireDepartment,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "STREAK SNAP MOMENT",
                                                color = Color.White,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Black
                                            )
                                        }
                                    }

                                    if (msg.mediaType == MessageType.AUDIO) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White)
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Voice Note (0:14)", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }
                                    } else {
                                        Text(
                                            text = msg.text,
                                            color = Color.White,
                                            fontSize = 13.sp,
                                            lineHeight = 18.sp
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.End,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        if (msg.isDisappearing) {
                                            Icon(
                                                Icons.Default.Timer,
                                                contentDescription = null,
                                                tint = Color.White.copy(alpha = 0.7f),
                                                modifier = Modifier.size(10.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                        }
                                        Text(
                                            text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(msg.createdAt)),
                                            color = Color.White.copy(alpha = 0.75f),
                                            fontSize = 10.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Message Composer Bar
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DarkSurface)
                        .border(1.dp, DarkSurfaceBorder)
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    // Quick streak snap icon
                    Surface(
                        color = StreakFlameOrange.copy(alpha = 0.15f),
                        shape = CircleShape,
                        modifier = Modifier
                            .size(38.dp)
                            .clickable {
                                if (activeTargetUserId != null) {
                                    streakSnapTargetUser = targetUser
                                }
                            }
                            .testTag("composer_snap_btn")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Bolt,
                                contentDescription = "Quick Snap",
                                tint = StreakFlameOrange,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    OutlinedTextField(
                        value = messageInput,
                        onValueChange = { messageInput = it },
                        placeholder = { Text("Send a message...", color = TextMuted, fontSize = 13.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = DarkSurfaceVariant,
                            unfocusedContainerColor = DarkSurfaceVariant,
                            focusedBorderColor = PrimaryNeon,
                            unfocusedBorderColor = Color.Transparent,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(24.dp),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("chat_text_input")
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Surface(
                        color = PrimaryNeon,
                        shape = CircleShape,
                        modifier = Modifier
                            .size(44.dp)
                            .clickable {
                                if (messageInput.isNotBlank()) {
                                    val textToSend = messageInput
                                    messageInput = ""
                                    coroutineScope.launch {
                                        repository.sendMessage(
                                            conversationId = activeConversationId!!,
                                            text = textToSend,
                                            isDisappearing = isDisappearingMode
                                        )
                                    }
                                }
                            }
                            .testTag("send_msg_btn")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = Color.White, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }

        // Streak Details Dialog Modal
        if (selectedStreakForDialog != null) {
            val otherId = if (selectedStreakForDialog!!.userAId == currentUserId) selectedStreakForDialog!!.userBId else selectedStreakForDialog!!.userAId
            val otherUser = userMap[otherId]

            StreakDetailsDialog(
                streak = selectedStreakForDialog!!,
                otherUser = otherUser,
                currentUserId = currentUserId,
                onDismiss = { selectedStreakForDialog = null },
                onSendStreakSnap = {
                    streakSnapTargetUser = otherUser
                },
                onFreezeStreak = {
                    coroutineScope.launch {
                        repository.freezeStreak(otherId)
                        val updated = repository.getStreakBetweenOnce(otherId)
                        selectedStreakForDialog = updated
                    }
                },
                onRestoreStreak = {
                    coroutineScope.launch {
                        repository.restoreStreak(otherId)
                        val updated = repository.getStreakBetweenOnce(otherId)
                        selectedStreakForDialog = updated
                    }
                }
            )
        }

        // Send Streak Snap Dialog Modal
        if (streakSnapTargetUser != null) {
            val matchingStreak = userStreaks.find {
                (it.userAId == currentUserId && it.userBId == streakSnapTargetUser!!.id) ||
                (it.userBId == currentUserId && it.userAId == streakSnapTargetUser!!.id)
            }
            val count = matchingStreak?.streakCount ?: 0

            SendStreakSnapDialog(
                targetUser = streakSnapTargetUser,
                currentStreakCount = count,
                onDismiss = { streakSnapTargetUser = null },
                onSendSnap = { caption, emoji ->
                    coroutineScope.launch {
                        repository.sendStreakSnap(
                            targetUserId = streakSnapTargetUser!!.id,
                            caption = caption,
                            emojiBadge = emoji
                        )
                        // If we are currently in chat list, open the conversation directly
                        val convId = repository.startOrGetDirectConversation(streakSnapTargetUser!!.id)
                        activeConversationId = convId
                        activeTargetUserId = streakSnapTargetUser!!.id
                    }
                }
            )
        }
    }
}
