package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Refresh
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.local.entities.StreakEntity
import com.example.data.local.entities.UserEntity
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceBorder
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.GhostModePurple
import com.example.ui.theme.PrimaryLight
import com.example.ui.theme.PrimaryNeon
import com.example.ui.theme.RomanticRed
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.util.concurrent.TimeUnit

val StreakFlameOrange = Color(0xFFFF6D00)
val StreakFlameYellow = Color(0xFFFFD600)
val StreakIceBlue = Color(0xFF00E5FF)
val StreakWarningHourglass = Color(0xFFFFAB00)

/**
 * Compact Streak Flame Badge for List items & chat headers
 */
@Composable
fun StreakFlameBadge(
    streak: StreakEntity,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val now = System.currentTimeMillis()
    val remainingMillis = (streak.expiresAt - now).coerceAtLeast(0L)
    val remainingHours = TimeUnit.MILLISECONDS.toHours(remainingMillis)
    val isExpiring = !streak.isFrozen && remainingHours < 6 && streak.streakCount > 0

    val infiniteTransition = rememberInfiniteTransition(label = "flame_pulse")
    val scaleAnim by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "flame_scale"
    )

    val badgeBrush = when {
        streak.isFrozen -> Brush.horizontalGradient(listOf(Color(0xFF0091EA), StreakIceBlue))
        isExpiring -> Brush.horizontalGradient(listOf(Color(0xFFDD2C00), StreakWarningHourglass))
        else -> Brush.horizontalGradient(listOf(StreakFlameOrange, StreakFlameYellow))
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color.Transparent,
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(badgeBrush)
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .testTag("streak_badge_${streak.id}")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
        ) {
            when {
                streak.isFrozen -> {
                    Icon(
                        imageVector = Icons.Default.AcUnit,
                        contentDescription = "Frozen Streak",
                        tint = Color.White,
                        modifier = Modifier.size(13.dp)
                    )
                }
                isExpiring -> {
                    Icon(
                        imageVector = Icons.Default.HourglassTop,
                        contentDescription = "Expiring Streak",
                        tint = Color.White,
                        modifier = Modifier
                            .size(13.dp)
                            .scale(scaleAnim)
                    )
                }
                else -> {
                    Icon(
                        imageVector = Icons.Default.LocalFireDepartment,
                        contentDescription = "Streak Flame",
                        tint = Color.White,
                        modifier = Modifier
                            .size(14.dp)
                            .scale(scaleAnim)
                    )
                }
            }
            Spacer(modifier = Modifier.width(3.dp))
            Text(
                text = "${streak.streakCount}",
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Black
            )
            if (isExpiring) {
                Text(
                    text = " ${remainingHours}h",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/**
 * Horizontal Streaks Bar at the top of the Chat screen
 */
@Composable
fun StreaksTray(
    streaks: List<StreakEntity>,
    userMap: Map<String, UserEntity>,
    currentUserId: String,
    onStreakClick: (StreakEntity) -> Unit,
    onQuickSendStreak: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (streaks.isEmpty()) return

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .testTag("streaks_tray_container")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.LocalFireDepartment,
                    contentDescription = null,
                    tint = StreakFlameOrange,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Active Streaks",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
            Text(
                text = "${streaks.size} Connections Active 🔥",
                color = TextSecondary,
                fontSize = 11.sp
            )
        }

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
        ) {
            items(streaks) { streak ->
                val otherUserId = if (streak.userAId == currentUserId) streak.userBId else streak.userAId
                val otherUser = userMap[otherUserId]
                val now = System.currentTimeMillis()
                val isExpiring = !streak.isFrozen && (streak.expiresAt - now) < (6 * 3600 * 1000L)

                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isExpiring) StreakWarningHourglass.copy(alpha = 0.6f) else DarkSurfaceBorder
                    ),
                    modifier = Modifier
                        .clickable { onStreakClick(streak) }
                        .testTag("streak_tray_item_${streak.id}")
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(10.dp)
                    ) {
                        Box(contentAlignment = Alignment.BottomEnd) {
                            UserAvatar(
                                avatarUrl = otherUser?.avatarUrl ?: "",
                                displayName = otherUser?.displayName ?: "Friend",
                                size = 44.dp
                            )
                            StreakFlameBadge(streak = streak)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = otherUser?.displayName?.split(" ")?.firstOrNull() ?: "Friend",
                            color = TextPrimary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp,
                            maxLines = 1
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = PrimaryNeon.copy(alpha = 0.15f),
                            modifier = Modifier
                                .clickable { onQuickSendStreak(otherUserId) }
                                .testTag("quick_send_streak_${otherUserId}")
                        ) {
                            Text(
                                text = "⚡ Snap",
                                color = PrimaryLight,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Rich Streak Details Dialog / Modal
 */
@Composable
fun StreakDetailsDialog(
    streak: StreakEntity,
    otherUser: UserEntity?,
    currentUserId: String,
    onDismiss: () -> Unit,
    onSendStreakSnap: () -> Unit,
    onFreezeStreak: () -> Unit,
    onRestoreStreak: () -> Unit
) {
    val now = System.currentTimeMillis()
    val remainingMillis = (streak.expiresAt - now).coerceAtLeast(0L)
    val remainingHours = TimeUnit.MILLISECONDS.toHours(remainingMillis)
    val remainingMinutes = TimeUnit.MILLISECONDS.toMinutes(remainingMillis) % 60
    val progress = (remainingMillis.toFloat() / (24 * 3600 * 1000L).toFloat()).coerceIn(0f, 1f)

    val isUserA = currentUserId == streak.userAId
    val userSentToday = if (isUserA) streak.userAInteractedToday else streak.userBInteractedToday
    val partnerSentToday = if (isUserA) streak.userBInteractedToday else streak.userAInteractedToday

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(24.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkSurfaceBorder),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("streak_details_dialog")
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(20.dp)
            ) {
                // Header Row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "KinSphere Streak",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Giant Animated Streak Display
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(88.dp)
                        .background(
                            brush = if (streak.isFrozen) {
                                Brush.radialGradient(listOf(StreakIceBlue.copy(alpha = 0.3f), Color.Transparent))
                            } else {
                                Brush.radialGradient(listOf(StreakFlameOrange.copy(alpha = 0.35f), Color.Transparent))
                            },
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        imageVector = if (streak.isFrozen) Icons.Default.AcUnit else Icons.Default.LocalFireDepartment,
                        contentDescription = null,
                        tint = if (streak.isFrozen) StreakIceBlue else StreakFlameOrange,
                        modifier = Modifier.size(54.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "${streak.streakCount} Day Streak!",
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 24.sp
                )
                Text(
                    text = if (streak.isFrozen) "❄️ Streak is Frozen (Safe for 48h)"
                    else if (remainingHours < 6) "⏳ Expiring Soon — Keep it alive!"
                    else "🔥 You and ${otherUser?.displayName ?: "Friend"} are on fire!",
                    color = if (streak.isFrozen) StreakIceBlue else if (remainingHours < 6) StreakWarningHourglass else TextSecondary,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Daily Checkmark Matrix
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("You", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (userSentToday) "Sent today" else "Waiting for you",
                                    color = if (userSentToday) SuccessGreen else StreakWarningHourglass,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = if (userSentToday) Icons.Default.CheckCircle else Icons.Default.HourglassTop,
                                    contentDescription = null,
                                    tint = if (userSentToday) SuccessGreen else StreakWarningHourglass,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(otherUser?.displayName ?: "Friend", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (partnerSentToday) "Replied today" else "Pending response",
                                    color = if (partnerSentToday) SuccessGreen else TextMuted,
                                    fontSize = 12.sp
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = if (partnerSentToday) Icons.Default.CheckCircle else Icons.Default.HourglassTop,
                                    contentDescription = null,
                                    tint = if (partnerSentToday) SuccessGreen else TextMuted,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Time Remaining Bar
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Streak Expiration", color = TextMuted, fontSize = 11.sp)
                        Text("${remainingHours}h ${remainingMinutes}m left", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { progress },
                        color = if (remainingHours < 6) StreakWarningHourglass else PrimaryNeon,
                        trackColor = DarkSurfaceVariant,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Streak Stats Badges
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(8.dp)
                        ) {
                            Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = StreakFlameYellow, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("Best Record", color = TextMuted, fontSize = 10.sp)
                            Text("${streak.bestStreakCount} Days", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }

                    Card(
                        colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(8.dp)
                        ) {
                            Icon(Icons.Default.AcUnit, contentDescription = null, tint = StreakIceBlue, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("Freeze Tokens", color = TextMuted, fontSize = 10.sp)
                            Text("${streak.freezeTokensLeft} Left", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action Buttons
                Button(
                    onClick = {
                        onSendStreakSnap()
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryNeon),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("send_streak_snap_btn")
                ) {
                    Icon(Icons.Default.Bolt, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Send Streak Snap 🔥", color = Color.White, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = onFreezeStreak,
                        enabled = streak.freezeTokensLeft > 0 && !streak.isFrozen,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DarkSurfaceVariant,
                            contentColor = StreakIceBlue
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("freeze_streak_btn")
                    ) {
                        Icon(Icons.Default.AcUnit, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (streak.isFrozen) "Frozen" else "Freeze (1❄️)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = onRestoreStreak,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DarkSurfaceVariant,
                            contentColor = PrimaryLight
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("restore_streak_btn")
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Restore ⚡", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

/**
 * Dedicated Send Streak Snap Composer
 */
@Composable
fun SendStreakSnapDialog(
    targetUser: UserEntity?,
    currentStreakCount: Int,
    onDismiss: () -> Unit,
    onSendSnap: (caption: String, emojiBadge: String) -> Unit
) {
    var caption by remember { mutableStateOf("🔥 DAY ${currentStreakCount + 1} STREAK!") }
    var selectedEmoji by remember { mutableStateOf("🔥") }

    val emojis = listOf("🔥", "⚡", "✨", "❤️", "🚀", "🎉")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(24.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkSurfaceBorder),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("send_streak_dialog")
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(20.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Send Streak Snap",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Camera/Snap Card Preview
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xFF4A148C), Color(0xFFE65100))
                            )
                        )
                        .padding(16.dp)
                ) {
                    Column(
                        verticalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Surface(
                                color = Color.Black.copy(alpha = 0.4f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = "$selectedEmoji DAY ${currentStreakCount + 1}",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Black,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                            Text(
                                text = "To: ${targetUser?.displayName ?: "Friend"}",
                                color = Color.White.copy(alpha = 0.85f),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Text(
                            text = caption.ifBlank { "Daily Streak Snap!" },
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Text(
                            text = "⚡ KinSphere Instant Snap",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 10.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Emoji Stickers Row
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(emojis) { emoji ->
                        val isSelected = selectedEmoji == emoji
                        Surface(
                            shape = CircleShape,
                            color = if (isSelected) PrimaryNeon.copy(alpha = 0.25f) else DarkSurfaceVariant,
                            border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, PrimaryNeon) else null,
                            modifier = Modifier
                                .size(40.dp)
                                .clickable { selectedEmoji = emoji }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(text = emoji, fontSize = 20.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = caption,
                    onValueChange = { caption = it },
                    label = { Text("Snap Caption") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = DarkSurfaceVariant,
                        unfocusedContainerColor = DarkSurfaceVariant,
                        focusedBorderColor = PrimaryNeon,
                        unfocusedBorderColor = DarkSurfaceBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("streak_caption_input")
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        onSendSnap(caption, selectedEmoji)
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryNeon),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("submit_streak_snap_btn")
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Send Streak Snap 🔥", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
