package com.example.ui.components

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.RelationshipStatus
import com.example.data.model.RelationshipType
import com.example.data.model.RelationshipVisibility
import com.example.ui.theme.DarkSurfaceBorder
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.FamilyGreen
import com.example.ui.theme.FamilyGreenSoft
import com.example.ui.theme.FriendshipBlue
import com.example.ui.theme.FriendshipBlueSoft
import com.example.ui.theme.GhostModePurple
import com.example.ui.theme.PrimaryNeon
import com.example.ui.theme.RomanticRed
import com.example.ui.theme.RomanticRedSoft
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun RelationshipBadge(
    type: RelationshipType,
    status: RelationshipStatus = RelationshipStatus.ACTIVE,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor, icon) = when (type) {
        RelationshipType.ROMANTIC -> Triple(RomanticRedSoft, RomanticRed, Icons.Default.Favorite)
        RelationshipType.FRIENDSHIP -> Triple(FriendshipBlueSoft, FriendshipBlue, Icons.Default.Group)
        RelationshipType.FAMILY -> Triple(FamilyGreenSoft, FamilyGreen, Icons.Default.Shield)
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, textColor.copy(alpha = 0.5f)),
        modifier = modifier.testTag("relationship_badge_${type.name}")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = type.label,
                tint = textColor,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "${type.label}${if (status != RelationshipStatus.ACTIVE) " (${status.name})" else ""}",
                color = textColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun UserAvatar(
    avatarUrl: String,
    displayName: String,
    size: Dp = 48.dp,
    hasStory: Boolean = false,
    isGhostMode: Boolean = false,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val ringBrush = if (hasStory) {
        Brush.sweepGradient(listOf(RomanticRed, PrimaryNeon, FriendshipBlue, RomanticRed))
    } else {
        Brush.linearGradient(listOf(PrimaryNeon.copy(alpha = 0.4f), PrimaryNeon.copy(alpha = 0.1f)))
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(size)
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .testTag("user_avatar_${displayName.replace(" ", "_")}")
    ) {
        Box(
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .background(ringBrush)
                .padding(if (hasStory) 2.5.dp else 1.dp)
        ) {
            if (avatarUrl.isNotEmpty()) {
                AsyncImage(
                    model = avatarUrl,
                    contentDescription = displayName,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(CircleShape)
                )
            } else {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(size)
                        .clip(CircleShape)
                        .background(DarkSurfaceVariant)
                ) {
                    Text(
                        text = displayName.take(1).uppercase(),
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = (size.value * 0.4).sp
                    )
                }
            }
        }

        if (isGhostMode) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(size * 0.38f)
                    .align(Alignment.BottomEnd)
                    .clip(CircleShape)
                    .background(GhostModePurple)
                    .border(1.5.dp, Color(0xFF0B0F19), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.VisibilityOff,
                    contentDescription = "Ghost Mode Active",
                    tint = Color.White,
                    modifier = Modifier.size(size * 0.22f)
                )
            }
        }
    }
}

@Composable
fun GhostModeBanner(
    isGhostMode: Boolean,
    onToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isGhostMode) Color(0xFF2E1065) else DarkSurfaceVariant
        ),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isGhostMode) GhostModePurple else DarkSurfaceBorder
        ),
        modifier = modifier
            .fillMaxWidth()
            .testTag("ghost_mode_card")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(if (isGhostMode) GhostModePurple else Color(0xFF334155))
            ) {
                Icon(
                    imageVector = if (isGhostMode) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Ghost Mode",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    if (isGhostMode) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            color = GhostModePurple.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "ACTIVE",
                                color = Color(0xFFE9D5FF),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                Text(
                    text = if (isGhostMode)
                        "Your exact GPS coordinates are hidden. Permitted relationships remain visible."
                    else
                        "Location sharing follows your privacy configuration.",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Switch(
                checked = isGhostMode,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = GhostModePurple
                ),
                modifier = Modifier.testTag("ghost_mode_switch")
            )
        }
    }
}
