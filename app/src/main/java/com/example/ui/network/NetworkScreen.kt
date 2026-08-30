package com.example.ui.network

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.LinearScale
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.RelationshipEntity
import com.example.data.local.entities.UserEntity
import com.example.data.model.RelationshipStatus
import com.example.data.model.RelationshipType
import com.example.data.repository.KinSphereRepository
import com.example.service.graph.ConnectionPath
import com.example.service.graph.RelationshipGraphEngine
import com.example.ui.components.RelationshipBadge
import com.example.ui.components.UserAvatar
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceBorder
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.FamilyGreen
import com.example.ui.theme.FriendshipBlue
import com.example.ui.theme.PrimaryLight
import com.example.ui.theme.PrimaryNeon
import com.example.ui.theme.RomanticRed
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun NetworkScreen(
    repository: KinSphereRepository,
    onNavigateToChat: (String) -> Unit,
    onNavigateToProfile: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val currentUserId by repository.currentUserId.collectAsState()
    val allUsers by repository.getAllUsers().collectAsState(initial = emptyList())
    val allRelationships by repository.getAllRelationships().collectAsState(initial = emptyList())

    val currentUser = allUsers.find { it.id == currentUserId }
    val userMap = remember(allUsers) { allUsers.associateBy { it.id } }

    var targetUserForPath by remember { mutableStateOf<UserEntity?>(null) }
    var selectedNodeUser by remember { mutableStateOf<UserEntity?>(null) }
    var showPathFinderSheet by remember { mutableStateOf(false) }

    var zoom by remember { mutableFloatStateOf(1f) }
    var panX by remember { mutableFloatStateOf(0f) }
    var panY by remember { mutableFloatStateOf(0f) }

    val activeRelationships = remember(allRelationships) {
        allRelationships.filter { it.status == RelationshipStatus.ACTIVE }
    }

    val networkStats = remember(currentUserId, allRelationships) {
        RelationshipGraphEngine.computeNetworkStats(
            userId = currentUserId,
            relationships = allRelationships,
            allRelationships = allRelationships
        )
    }

    // Direct connections of current user
    val directConnections = remember(currentUserId, activeRelationships) {
        activeRelationships.filter { it.userAId == currentUserId || it.userBId == currentUserId }
    }

    // Shortest path calculation if path finding is requested
    val computedPath: ConnectionPath? = remember(currentUserId, targetUserForPath, activeRelationships, userMap) {
        if (targetUserForPath != null) {
            RelationshipGraphEngine.findConnectionPath(
                startUserId = currentUserId,
                targetUserId = targetUserForPath!!.id,
                allUsersMap = userMap,
                activeRelationships = activeRelationships
            )
        } else null
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .testTag("network_screen_root")
    ) {
        // --- 1. Graph Canvas ---
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, gestureZoom, _ ->
                        zoom = (zoom * gestureZoom).coerceIn(0.5f, 3.0f)
                        panX += pan.x
                        panY += pan.y
                    }
                }
                .testTag("network_graph_canvas")
        ) {
            val width = size.width
            val height = size.height
            val centerX = width / 2 + panX
            val centerY = height / 2 + panY

            // Draw concentric network orbit rings
            val ringRadius1 = 130f * zoom
            val ringRadius2 = 240f * zoom
            drawCircle(color = DarkSurfaceBorder.copy(alpha = 0.5f), radius = ringRadius1, center = Offset(centerX, centerY), style = Stroke(width = 1f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f))))
            drawCircle(color = DarkSurfaceBorder.copy(alpha = 0.3f), radius = ringRadius2, center = Offset(centerX, centerY), style = Stroke(width = 1f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f))))

            // Draw direct connection lines from Center (YOU)
            val count = directConnections.size
            directConnections.forEachIndexed { index, rel ->
                val otherId = if (rel.userAId == currentUserId) rel.userBId else rel.userAId
                val angle = (2 * Math.PI * index / (if (count > 0) count else 1)).toFloat() - (Math.PI / 2).toFloat()
                val nodeX = centerX + ringRadius1 * cos(angle)
                val nodeY = centerY + ringRadius1 * sin(angle)

                val lineColor = when (rel.type) {
                    RelationshipType.ROMANTIC -> RomanticRed
                    RelationshipType.FRIENDSHIP -> FriendshipBlue
                    RelationshipType.FAMILY -> FamilyGreen
                }

                // Draw edge
                drawLine(
                    color = lineColor,
                    start = Offset(centerX, centerY),
                    end = Offset(nodeX, nodeY),
                    strokeWidth = 3f * zoom,
                    cap = StrokeCap.Round
                )

                // Draw connection node
                drawCircle(
                    color = lineColor.copy(alpha = 0.3f),
                    radius = 24f * zoom,
                    center = Offset(nodeX, nodeY)
                )
                drawCircle(
                    color = DarkSurface,
                    radius = 18f * zoom,
                    center = Offset(nodeX, nodeY)
                )
                drawCircle(
                    color = lineColor,
                    radius = 18f * zoom,
                    center = Offset(nodeX, nodeY),
                    style = Stroke(width = 2f)
                )
            }

            // Draw Center Node (YOU)
            drawCircle(
                color = PrimaryNeon.copy(alpha = 0.3f),
                radius = 36f * zoom,
                center = Offset(centerX, centerY)
            )
            drawCircle(
                color = PrimaryNeon,
                radius = 26f * zoom,
                center = Offset(centerX, centerY)
            )
        }

        // --- 2. Top Header & Network Stats ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text(
                        text = "Global Relationship Graph",
                        color = TextPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Visualizing mutual human bonds & connection degrees",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }

                Surface(
                    color = PrimaryNeon.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryNeon),
                    modifier = Modifier
                        .clickable { showPathFinderSheet = true }
                        .testTag("path_finder_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.LinearScale, contentDescription = null, tint = PrimaryLight, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Find Path", color = PrimaryLight, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Network Stats Cards
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                StatPill("🔴 Romantic", "${networkStats.romanticCount}", RomanticRed, Modifier.weight(1f))
                StatPill("🔵 Friends", "${networkStats.friendshipCount}", FriendshipBlue, Modifier.weight(1f))
                StatPill("🟢 Family", "${networkStats.familyCount}", FamilyGreen, Modifier.weight(1f))
                StatPill("🌐 Extended", "${networkStats.extendedNetworkSize}", PrimaryLight, Modifier.weight(1f))
            }
        }

        // --- 3. Bottom Direct Connection Nodes Carousel ---
        Card(
            colors = CardDefaults.cardColors(containerColor = DarkSurface.copy(alpha = 0.94f)),
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkSurfaceBorder),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .testTag("direct_connections_card")
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Direct Consented Connections (${directConnections.size})",
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(10.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(directConnections) { rel ->
                        val otherId = if (rel.userAId == currentUserId) rel.userBId else rel.userAId
                        val otherUser = userMap[otherId]
                        if (otherUser != null) {
                            Surface(
                                color = DarkSurfaceVariant,
                                shape = RoundedCornerShape(14.dp),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    when (rel.type) {
                                        RelationshipType.ROMANTIC -> RomanticRed.copy(alpha = 0.5f)
                                        RelationshipType.FRIENDSHIP -> FriendshipBlue.copy(alpha = 0.5f)
                                        RelationshipType.FAMILY -> FamilyGreen.copy(alpha = 0.5f)
                                    }
                                ),
                                modifier = Modifier
                                    .clickable { selectedNodeUser = otherUser }
                                    .testTag("node_${otherUser.username}")
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.padding(10.dp)
                                ) {
                                    UserAvatar(
                                        avatarUrl = otherUser.avatarUrl,
                                        displayName = otherUser.displayName,
                                        size = 40.dp
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = otherUser.displayName.split(" ").first(),
                                        color = TextPrimary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    RelationshipBadge(type = rel.type)
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- 4. Path Finder Modal / Sheet ---
        if (showPathFinderSheet) {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, PrimaryNeon),
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth(0.92f)
                    .padding(16.dp)
                    .testTag("path_finder_dialog")
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Hub, contentDescription = null, tint = PrimaryLight)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Connection Path Finder", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        }
                        IconButton(onClick = {
                            showPathFinderSheet = false
                            targetUserForPath = null
                        }) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Select any user in the global graph to discover the relationship path:",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(allUsers.filter { it.id != currentUserId }) { target ->
                            val isSelected = targetUserForPath?.id == target.id
                            Surface(
                                color = if (isSelected) PrimaryNeon else DarkSurfaceVariant,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .clickable { targetUserForPath = target }
                                    .testTag("path_target_${target.username}")
                            ) {
                                Text(
                                    text = target.displayName,
                                    color = if (isSelected) Color.White else TextPrimary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Computed Path Result
                    if (targetUserForPath != null) {
                        if (computedPath != null) {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = "«${computedPath.steps} relationship step${if (computedPath.steps > 1) "s" else ""} away»",
                                        color = SuccessGreen(),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))

                                    // Render Visual Step Sequence: YOU -> AHMED -> SARA -> ALI
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        computedPath.users.forEachIndexed { idx, u ->
                                            Text(
                                                text = if (u.id == currentUserId) "YOU" else u.displayName.split(" ").first().uppercase(),
                                                color = if (u.id == currentUserId) PrimaryLight else TextPrimary,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp
                                            )
                                            if (idx < computedPath.users.size - 1) {
                                                Icon(
                                                    Icons.AutoMirrored.Filled.ArrowForward,
                                                    contentDescription = null,
                                                    tint = PrimaryNeon,
                                                    modifier = Modifier.size(12.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            Text(
                                text = "No active connection path found through public/permitted relationships.",
                                color = TextMuted,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatPill(label: String, value: String, accent: Color, modifier: Modifier = Modifier) {
    Surface(
        color = DarkSurfaceVariant,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, accent.copy(alpha = 0.4f)),
        modifier = modifier
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)
        ) {
            Text(value, color = accent, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(label, color = TextSecondary, fontSize = 10.sp, maxLines = 1)
        }
    }
}

private fun SuccessGreen(): Color = Color(0xFF22C55E)
