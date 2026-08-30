package com.example.ui.relationships

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.RelationshipEntity
import com.example.data.local.entities.UserEntity
import com.example.data.model.RelationshipStatus
import com.example.data.model.RelationshipType
import com.example.data.model.RelationshipVisibility
import com.example.data.repository.KinSphereRepository
import com.example.ui.components.RelationshipBadge
import com.example.ui.components.UserAvatar
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceBorder
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.FamilyGreen
import com.example.ui.theme.FriendshipBlue
import com.example.ui.theme.PrimaryLight
import com.example.ui.theme.PrimaryNeon
import com.example.ui.theme.RomanticRed
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.launch

@Composable
fun RelationshipsScreen(
    repository: KinSphereRepository,
    onNavigateToProfile: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val currentUserId by repository.currentUserId.collectAsState()
    val allRelationships by repository.getRelationshipsForUser(currentUserId).collectAsState(initial = emptyList())
    val allUsers by repository.getAllUsers().collectAsState(initial = emptyList())
    val userMap = remember(allUsers) { allUsers.associateBy { it.id } }

    var selectedTab by remember { mutableIntStateOf(0) }
    var showSendRequestDialog by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    val pendingRequests = remember(allRelationships, currentUserId) {
        allRelationships.filter { it.status == RelationshipStatus.PENDING && it.requestedBy != currentUserId }
    }

    val sentRequests = remember(allRelationships, currentUserId) {
        allRelationships.filter { it.status == RelationshipStatus.PENDING && it.requestedBy == currentUserId }
    }

    val activeRelationships = remember(allRelationships) {
        allRelationships.filter { it.status == RelationshipStatus.ACTIVE }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .testTag("relationships_screen_root")
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
                        text = "Relationship Hub",
                        color = TextPrimary,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Mutual consent graph management & visibility controls",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }

                Surface(
                    color = PrimaryNeon,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .clickable { showSendRequestDialog = true }
                        .testTag("send_relationship_request_btn")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Connect", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tabs: Active Bonds (N), Incoming Requests (N), Sent
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = DarkSurfaceVariant,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = PrimaryNeon
                    )
                }
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Active (${activeRelationships.size})", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (selectedTab == 0) TextPrimary else TextSecondary) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Requests (${pendingRequests.size})", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (selectedTab == 1) TextPrimary else TextSecondary) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("Sent (${sentRequests.size})", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (selectedTab == 2) TextPrimary else TextSecondary) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tab Content
            when (selectedTab) {
                0 -> {
                    // Active Relationships
                    if (activeRelationships.isEmpty()) {
                        EmptyState("No active relationships yet. Tap 'Connect' to establish mutual bonds.")
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(activeRelationships) { rel ->
                                val otherId = if (rel.userAId == currentUserId) rel.userBId else rel.userAId
                                val otherUser = userMap[otherId]
                                if (otherUser != null) {
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = DarkSurface),
                                        shape = RoundedCornerShape(16.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, DarkSurfaceBorder),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("active_rel_${rel.id}")
                                    ) {
                                        Column(modifier = Modifier.padding(14.dp)) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    UserAvatar(
                                                        avatarUrl = otherUser.avatarUrl,
                                                        displayName = otherUser.displayName,
                                                        size = 44.dp
                                                    )
                                                    Spacer(modifier = Modifier.width(10.dp))
                                                    Column {
                                                        Text(
                                                            text = otherUser.displayName,
                                                            color = TextPrimary,
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 14.sp
                                                        )
                                                        Text(
                                                            text = "${otherUser.city}, ${otherUser.country}",
                                                            color = TextSecondary,
                                                            fontSize = 11.sp
                                                        )
                                                    }
                                                }

                                                RelationshipBadge(type = rel.type, status = rel.status)
                                            }

                                            Spacer(modifier = Modifier.height(10.dp))

                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Text(
                                                    text = "Map Visibility: ${rel.visibility.label}",
                                                    color = PrimaryLight,
                                                    fontSize = 11.sp
                                                )

                                                IconButton(
                                                    onClick = {
                                                        coroutineScope.launch {
                                                            repository.endRelationship(rel.id)
                                                        }
                                                    },
                                                    modifier = Modifier.size(32.dp)
                                                ) {
                                                    Icon(
                                                        Icons.Default.Delete,
                                                        contentDescription = "End",
                                                        tint = ErrorRed,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                1 -> {
                    // Incoming Requests
                    if (pendingRequests.isEmpty()) {
                        EmptyState("No pending relationship requests.")
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(pendingRequests) { req ->
                                val requester = userMap[req.requestedBy]
                                if (requester != null) {
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = DarkSurface),
                                        shape = RoundedCornerShape(16.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryNeon.copy(alpha = 0.5f)),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("incoming_request_${req.id}")
                                    ) {
                                        Column(modifier = Modifier.padding(14.dp)) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    UserAvatar(
                                                        avatarUrl = requester.avatarUrl,
                                                        displayName = requester.displayName,
                                                        size = 44.dp
                                                    )
                                                    Spacer(modifier = Modifier.width(10.dp))
                                                    Column {
                                                        Text(requester.displayName, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                                        Text("Sent ${req.type.label} request", color = TextSecondary, fontSize = 11.sp)
                                                    }
                                                }

                                                RelationshipBadge(type = req.type)
                                            }

                                            Spacer(modifier = Modifier.height(12.dp))

                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Button(
                                                    onClick = {
                                                        coroutineScope.launch {
                                                            repository.acceptRelationship(req.id)
                                                        }
                                                    },
                                                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                                                    shape = RoundedCornerShape(10.dp),
                                                    modifier = Modifier.weight(1f).testTag("accept_rel_${req.id}")
                                                ) {
                                                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text("Accept", fontSize = 12.sp)
                                                }

                                                OutlinedButton(
                                                    onClick = {
                                                        coroutineScope.launch {
                                                            repository.rejectRelationship(req.id)
                                                        }
                                                    },
                                                    shape = RoundedCornerShape(10.dp),
                                                    modifier = Modifier.weight(1f).testTag("reject_rel_${req.id}")
                                                ) {
                                                    Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp), tint = ErrorRed)
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text("Decline", fontSize = 12.sp, color = ErrorRed)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                2 -> {
                    // Sent Requests
                    if (sentRequests.isEmpty()) {
                        EmptyState("No active outgoing relationship requests.")
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(sentRequests) { req ->
                                val targetId = if (req.userAId == currentUserId) req.userBId else req.userAId
                                val targetUser = userMap[targetId]
                                if (targetUser != null) {
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = DarkSurface),
                                        shape = RoundedCornerShape(16.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            modifier = Modifier.padding(14.dp)
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                UserAvatar(avatarUrl = targetUser.avatarUrl, displayName = targetUser.displayName, size = 40.dp)
                                                Spacer(modifier = Modifier.width(10.dp))
                                                Column {
                                                    Text(targetUser.displayName, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                                    Text("Pending recipient consent...", color = TextMuted, fontSize = 11.sp)
                                                }
                                            }
                                            RelationshipBadge(type = req.type, status = RelationshipStatus.PENDING)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- Send Relationship Request Modal ---
        if (showSendRequestDialog) {
            var selectedUser by remember { mutableStateOf<UserEntity?>(null) }
            var selectedType by remember { mutableStateOf(RelationshipType.ROMANTIC) }
            var selectedVis by remember { mutableStateOf(RelationshipVisibility.GLOBAL) }

            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, PrimaryNeon),
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth(0.92f)
                    .padding(16.dp)
                    .testTag("send_rel_dialog")
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Initiate Relationship", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        IconButton(onClick = { showSendRequestDialog = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Select User:", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(6.dp))

                    LazyColumn(modifier = Modifier.height(120.dp)) {
                        items(allUsers.filter { it.id != currentUserId }) { user ->
                            val isSel = selectedUser?.id == user.id
                            Surface(
                                color = if (isSel) PrimaryNeon.copy(alpha = 0.25f) else DarkSurfaceVariant,
                                shape = RoundedCornerShape(10.dp),
                                border = if (isSel) androidx.compose.foundation.BorderStroke(1.dp, PrimaryNeon) else null,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp)
                                    .clickable { selectedUser = user }
                                    .testTag("select_rel_user_${user.username}")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(8.dp)
                                ) {
                                    UserAvatar(avatarUrl = user.avatarUrl, displayName = user.displayName, size = 30.dp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(user.displayName, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Relationship Type:", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        listOf(RelationshipType.ROMANTIC, RelationshipType.FRIENDSHIP, RelationshipType.FAMILY).forEach { type ->
                            val isSel = selectedType == type
                            Surface(
                                color = if (isSel) PrimaryNeon else DarkSurfaceVariant,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { selectedType = type }
                                    .testTag("type_opt_${type.name}")
                            ) {
                                Text(
                                    text = "${type.symbol} ${type.label}",
                                    color = if (isSel) Color.White else TextSecondary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (selectedUser != null) {
                                coroutineScope.launch {
                                    repository.requestRelationship(
                                        targetUserId = selectedUser!!.id,
                                        type = selectedType,
                                        visibility = selectedVis
                                    )
                                    showSendRequestDialog = false
                                }
                            }
                        },
                        enabled = selectedUser != null,
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryNeon),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("confirm_send_rel_request_btn")
                    ) {
                        Text("Send Mutual Consent Request", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyState(message: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 40.dp)
    ) {
        Icon(Icons.Default.Group, contentDescription = null, tint = TextMuted, modifier = Modifier.size(48.dp))
        Spacer(modifier = Modifier.height(12.dp))
        Text(message, color = TextSecondary, fontSize = 13.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
    }
}
