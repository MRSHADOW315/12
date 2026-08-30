package com.example.ui.map

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VisibilityOff
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.LocationPermissionEntity
import com.example.data.local.entities.RelationshipEntity
import com.example.data.local.entities.UserEntity
import com.example.data.model.MapMode
import com.example.data.model.PrecisionLevel
import com.example.data.model.RelationshipStatus
import com.example.data.model.RelationshipType
import com.example.data.repository.KinSphereRepository
import com.example.service.privacy.PrivacyAuthorizationEngine
import com.example.ui.components.RelationshipBadge
import com.example.ui.components.UserAvatar
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceBorder
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.FamilyGreen
import com.example.ui.theme.FriendshipBlue
import com.example.ui.theme.GhostModePurple
import com.example.ui.theme.PrimaryLight
import com.example.ui.theme.PrimaryNeon
import com.example.ui.theme.RomanticRed
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlin.math.sin

data class MapLocationPoint(
    val user: UserEntity,
    val lat: Double,
    val lng: Double,
    val isExactCoordinates: Boolean,
    val isBroadCountryOnly: Boolean,
    val isGhostMode: Boolean
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MapScreen(
    repository: KinSphereRepository,
    onNavigateToChat: (String) -> Unit,
    onNavigateToProfile: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val currentUserId by repository.currentUserId.collectAsState()
    val allUsers by repository.getAllUsers().collectAsState(initial = emptyList())
    val allRelationships by repository.getAllRelationships().collectAsState(initial = emptyList())
    val allLocations by repository.getAllLocations().collectAsState(initial = emptyList())
    val currentUser = allUsers.find { it.id == currentUserId }

    var mapMode by remember { mutableStateOf(MapMode.WORLD) }
    var searchQuery by remember { mutableStateOf("") }
    var showFilterBar by remember { mutableStateOf(true) }

    // Layer filters
    var showRomantic by remember { mutableStateOf(true) }
    var showFriendship by remember { mutableStateOf(true) }
    var showFamily by remember { mutableStateOf(true) }
    var showPeopleMarkers by remember { mutableStateOf(true) }
    var showDensityHalos by remember { mutableStateOf(true) }

    // Selected Relationship or User for Detail Card
    var selectedRelationship by remember { mutableStateOf<RelationshipEntity?>(null) }
    var selectedPoint by remember { mutableStateOf<MapLocationPoint?>(null) }

    // Pan and Zoom states
    var zoom by remember { mutableFloatStateOf(1f) }
    var panX by remember { mutableFloatStateOf(0f) }
    var panY by remember { mutableFloatStateOf(0f) }

    // Prepare Map Points with Privacy Engine applied
    val userMap = allUsers.associateBy { it.id }
    val locationMap = allLocations.associateBy { it.userId }

    val userPoints = remember(allUsers, allLocations, currentUserId) {
        allUsers.mapNotNull { user ->
            val perm = locationMap[user.id]
            val effective = PrivacyAuthorizationEngine.evaluateLocationAccess(
                viewerId = currentUserId,
                targetUser = user,
                targetPerm = perm,
                activeRelationshipsBetween = allRelationships.filter { 
                    (it.userAId == user.id && it.userBId == currentUserId) || 
                    (it.userAId == currentUserId && it.userBId == user.id) 
                },
                activeFriendship = null,
                isFollowing = false,
                isBlocked = false
            )

            // Default country fallbacks for visualization if coords are null
            val (lat, lng) = when {
                effective.latitude != null && effective.longitude != null -> effective.latitude to effective.longitude
                user.countryCode == "IQ" -> 33.3152 to 44.3661
                user.countryCode == "FR" -> 48.8566 to 2.3522
                user.countryCode == "AE" -> 25.2048 to 55.2708
                user.countryCode == "EG" -> 30.0444 to 31.2357
                user.countryCode == "IT" -> 41.9028 to 12.4964
                user.countryCode == "JP" -> 35.6762 to 139.6503
                else -> 20.0 to 0.0
            }

            MapLocationPoint(
                user = user,
                lat = lat,
                lng = lng,
                isExactCoordinates = effective.hasPermission && !effective.isBroadCountryOnly,
                isBroadCountryOnly = effective.isBroadCountryOnly,
                isGhostMode = effective.isGhostMode
            )
        }
    }

    // Filtered Relationships
    val activeRelationships = remember(allRelationships, showRomantic, showFriendship, showFamily, mapMode, currentUserId) {
        allRelationships.filter { rel ->
            if (rel.status != RelationshipStatus.ACTIVE) return@filter false
            val typeAllowed = when (rel.type) {
                RelationshipType.ROMANTIC -> showRomantic
                RelationshipType.FRIENDSHIP -> showFriendship
                RelationshipType.FAMILY -> showFamily
            }
            if (!typeAllowed) return@filter false

            if (mapMode == MapMode.MY_NETWORK) {
                rel.userAId == currentUserId || rel.userBId == currentUserId
            } else {
                true
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .testTag("map_screen_root")
    ) {
        // --- 1. Interactive Custom Canvas World Map ---
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, gestureZoom, _ ->
                        zoom = (zoom * gestureZoom).coerceIn(0.6f, 4.0f)
                        panX += pan.x
                        panY += pan.y
                    }
                }
                .testTag("map_canvas")
        ) {
            val width = size.width
            val height = size.height
            val centerX = width / 2 + panX
            val centerY = height / 2 + panY

            // Draw World Grid & Continents Aesthetics
            drawWorldGrid(centerX, centerY, width, height, zoom)

            // Function to convert GPS (lat, lng) to canvas (x, y) coordinates using Mercator-like projection
            fun gpsToScreen(lat: Double, lng: Double): Offset {
                val scaleFactor = (width / 360f) * zoom
                val x = centerX + (lng.toFloat() * scaleFactor)
                val y = centerY - (lat.toFloat() * scaleFactor * 1.3f)
                return Offset(x, y)
            }

            // Draw Relationship Density Halos if enabled
            if (showDensityHalos) {
                for (pt in userPoints) {
                    val pos = gpsToScreen(pt.lat, pt.lng)
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(PrimaryNeon.copy(alpha = 0.25f), Color.Transparent),
                            center = pos,
                            radius = 60f * zoom
                        ),
                        center = pos,
                        radius = 60f * zoom
                    )
                }
            }

            // Draw Global Relationship Lines (The Defining Feature)
            for (rel in activeRelationships) {
                val userA = userPoints.find { it.user.id == rel.userAId }
                val userB = userPoints.find { it.user.id == rel.userBId }

                if (userA != null && userB != null) {
                    val start = gpsToScreen(userA.lat, userA.lng)
                    val end = gpsToScreen(userB.lat, userB.lng)

                    val color = when (rel.type) {
                        RelationshipType.ROMANTIC -> RomanticRed
                        RelationshipType.FRIENDSHIP -> FriendshipBlue
                        RelationshipType.FAMILY -> FamilyGreen
                    }

                    // Create curved arc
                    val midX = (start.x + end.x) / 2
                    val midY = (start.y + end.y) / 2 - (40f * zoom)
                    val path = Path().apply {
                        moveTo(start.x, start.y)
                        quadraticTo(midX, midY, end.x, end.y)
                    }

                    val pathEffect = when (rel.type) {
                        RelationshipType.ROMANTIC -> null // Solid bold
                        RelationshipType.FRIENDSHIP -> PathEffect.dashPathEffect(floatArrayOf(20f * zoom, 10f * zoom))
                        RelationshipType.FAMILY -> PathEffect.dashPathEffect(floatArrayOf(10f * zoom, 5f * zoom))
                    }

                    // Draw outer glow
                    drawPath(
                        path = path,
                        color = color.copy(alpha = 0.35f),
                        style = Stroke(width = 8f * zoom, cap = StrokeCap.Round, pathEffect = pathEffect)
                    )

                    // Draw main line
                    drawPath(
                        path = path,
                        color = color,
                        style = Stroke(width = 3.5f * zoom, cap = StrokeCap.Round, pathEffect = pathEffect)
                    )

                    // Draw relationship badge symbol in the center of the arc
                    drawCircle(
                        color = DarkSurface,
                        radius = 12f * zoom,
                        center = Offset(midX, midY)
                    )
                    drawCircle(
                        color = color,
                        radius = 10f * zoom,
                        center = Offset(midX, midY),
                        style = Stroke(width = 2f)
                    )
                }
            }

            // Draw User Location Markers
            if (showPeopleMarkers) {
                for (pt in userPoints) {
                    val pos = gpsToScreen(pt.lat, pt.lng)
                    val markerRadius = 14f * zoom

                    // If Broad Country Only (Location disabled or Ghost Mode)
                    if (pt.isBroadCountryOnly || pt.isGhostMode) {
                        // Draw Country Hub Indicator with dashed border
                        drawCircle(
                            color = GhostModePurple.copy(alpha = 0.25f),
                            radius = markerRadius * 1.5f,
                            center = pos
                        )
                        drawCircle(
                            color = GhostModePurple,
                            radius = markerRadius,
                            center = pos,
                            style = Stroke(
                                width = 2f * zoom,
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 4f))
                            )
                        )
                    } else {
                        // Exact or Approximate GPS Marker
                        drawCircle(
                            color = PrimaryNeon,
                            radius = markerRadius,
                            center = pos
                        )
                        drawCircle(
                            color = Color.White,
                            radius = markerRadius * 0.45f,
                            center = pos
                        )
                    }
                }
            }
        }

        // --- 2. Top Header & Search Bar ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Mode Selector Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(DarkSurfaceVariant.copy(alpha = 0.85f))
                    .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(24.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                listOf(
                    MapMode.WORLD to "World",
                    MapMode.NEARBY to "Nearby",
                    MapMode.MY_NETWORK to "My Network",
                    MapMode.RELATIONSHIP_PATH to "Paths"
                ).forEach { (mode, label) ->
                    val isSelected = mapMode == mode
                    Surface(
                        color = if (isSelected) PrimaryNeon else Color.Transparent,
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { mapMode = mode }
                            .testTag("map_tab_$label")
                    ) {
                        Text(
                            text = label,
                            color = if (isSelected) Color.White else TextSecondary,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Search Bar & Filter Toggle
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search country, city, or friend...", fontSize = 13.sp, color = TextMuted) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear", tint = TextSecondary)
                            }
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = DarkSurface.copy(alpha = 0.9f),
                        unfocusedContainerColor = DarkSurface.copy(alpha = 0.8f),
                        focusedBorderColor = PrimaryNeon,
                        unfocusedBorderColor = DarkSurfaceBorder
                    ),
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("map_search_input")
                )

                Spacer(modifier = Modifier.width(8.dp))

                Surface(
                    color = if (showFilterBar) PrimaryNeon else DarkSurfaceVariant,
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkSurfaceBorder),
                    modifier = Modifier
                        .size(52.dp)
                        .clickable { showFilterBar = !showFilterBar }
                        .testTag("map_filter_toggle")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = "Filters",
                            tint = Color.White
                        )
                    }
                }
            }

            // Collapsible Layer Filter Chips
            AnimatedVisibility(visible = showFilterBar) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    FilterChip(
                        selected = showRomantic,
                        onClick = { showRomantic = !showRomantic },
                        label = { Text("🔴 Romantic (${activeRelationships.count { it.type == RelationshipType.ROMANTIC }})", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = RomanticRed.copy(alpha = 0.25f),
                            selectedLabelColor = RomanticRed
                        ),
                        modifier = Modifier.testTag("filter_romantic")
                    )
                    FilterChip(
                        selected = showFriendship,
                        onClick = { showFriendship = !showFriendship },
                        label = { Text("🔵 Friendship (${activeRelationships.count { it.type == RelationshipType.FRIENDSHIP }})", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = FriendshipBlue.copy(alpha = 0.25f),
                            selectedLabelColor = FriendshipBlue
                        ),
                        modifier = Modifier.testTag("filter_friendship")
                    )
                    FilterChip(
                        selected = showFamily,
                        onClick = { showFamily = !showFamily },
                        label = { Text("🟢 Family (${activeRelationships.count { it.type == RelationshipType.FAMILY }})", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = FamilyGreen.copy(alpha = 0.25f),
                            selectedLabelColor = FamilyGreen
                        ),
                        modifier = Modifier.testTag("filter_family")
                    )
                    FilterChip(
                        selected = showPeopleMarkers,
                        onClick = { showPeopleMarkers = !showPeopleMarkers },
                        label = { Text("👥 People", fontSize = 11.sp) },
                        modifier = Modifier.testTag("filter_people")
                    )
                }
            }
        }

        // --- 3. Floating Quick Info / Decoupling Banner ---
        Card(
            colors = CardDefaults.cardColors(containerColor = DarkSurface.copy(alpha = 0.92f)),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkSurfaceBorder),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 20.dp)
                .testTag("map_info_card")
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = PrimaryLight,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Decoupled Relationship & Location Engine",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Lines represent human social connections. Users in Ghost Mode or Hidden Location (e.g. 🇮🇶 Iraq / 🇫🇷 France) maintain visible relationship lines globally without leaking GPS coordinates.",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Quick Relationship List in View
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    activeRelationships.take(2).forEach { rel ->
                        val uA = userMap[rel.userAId]
                        val uB = userMap[rel.userBId]
                        if (uA != null && uB != null) {
                            Surface(
                                color = DarkSurfaceVariant,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { selectedRelationship = rel }
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(6.dp)
                                ) {
                                    Text(
                                        text = "${uA.countryCode} ↔ ${uB.countryCode}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    RelationshipBadge(type = rel.type)
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- 4. Floating Map Navigation & Zoom Controls ---
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 16.dp)
        ) {
            Surface(
                color = DarkSurface.copy(alpha = 0.9f),
                shape = CircleShape,
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkSurfaceBorder),
                modifier = Modifier
                    .size(44.dp)
                    .clickable { zoom = (zoom * 1.3f).coerceAtMost(4f) }
                    .testTag("map_zoom_in")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Add, contentDescription = "Zoom In", tint = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                color = DarkSurface.copy(alpha = 0.9f),
                shape = CircleShape,
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkSurfaceBorder),
                modifier = Modifier
                    .size(44.dp)
                    .clickable { zoom = (zoom / 1.3f).coerceAtLeast(0.6f) }
                    .testTag("map_zoom_out")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Remove, contentDescription = "Zoom Out", tint = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                color = DarkSurface.copy(alpha = 0.9f),
                shape = CircleShape,
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkSurfaceBorder),
                modifier = Modifier
                    .size(44.dp)
                    .clickable {
                        zoom = 1f
                        panX = 0f
                        panY = 0f
                    }
                    .testTag("map_recenter")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Public, contentDescription = "Reset World", tint = PrimaryLight)
                }
            }
        }

        // --- 5. Selected Relationship Inspector Dialog ---
        if (selectedRelationship != null) {
            val rel = selectedRelationship!!
            val uA = userMap[rel.userAId]
            val uB = userMap[rel.userBId]
            if (uA != null && uB != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(20.dp),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, PrimaryNeon),
                    modifier = Modifier
                        .align(Alignment.Center)
                        .fillMaxWidth(0.92f)
                        .padding(16.dp)
                        .testTag("relationship_detail_dialog")
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            RelationshipBadge(type = rel.type, status = rel.status)
                            IconButton(onClick = { selectedRelationship = null }) {
                                Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Visual Connection Representation
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                UserAvatar(avatarUrl = uA.avatarUrl, displayName = uA.displayName, size = 48.dp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(uA.displayName, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                Text("${uA.city}, ${uA.country}", fontSize = 10.sp, color = TextSecondary)
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(rel.type.symbol, fontSize = 20.sp)
                                Text(
                                    text = "══════",
                                    color = when (rel.type) {
                                        RelationshipType.ROMANTIC -> RomanticRed
                                        RelationshipType.FRIENDSHIP -> FriendshipBlue
                                        RelationshipType.FAMILY -> FamilyGreen
                                    },
                                    fontWeight = FontWeight.Bold
                                )
                                Text("Consensual", fontSize = 10.sp, color = PrimaryLight)
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                UserAvatar(avatarUrl = uB.avatarUrl, displayName = uB.displayName, size = 48.dp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(uB.displayName, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                Text("${uB.city}, ${uB.country}", fontSize = 10.sp, color = TextSecondary)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Relationship Visibility: ${rel.visibility.label}",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = {
                                    val otherId = if (rel.userAId == currentUserId) rel.userBId else rel.userAId
                                    selectedRelationship = null
                                    onNavigateToChat(otherId)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryNeon),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Open Chat", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun DrawScope.drawWorldGrid(centerX: Float, centerY: Float, width: Float, height: Float, zoom: Float) {
    val gridColor = DarkSurfaceBorder.copy(alpha = 0.4f)
    val equatorColor = PrimaryNeon.copy(alpha = 0.2f)

    // Latitude parallels
    for (lat in -80..80 step 20) {
        val y = centerY - (lat * (width / 360f) * zoom * 1.3f)
        if (y in 0f..height) {
            drawLine(
                color = if (lat == 0) equatorColor else gridColor,
                start = Offset(0f, y),
                end = Offset(width, y),
                strokeWidth = if (lat == 0) 1.5f else 0.8f
            )
        }
    }

    // Longitude meridians
    for (lng in -180..180 step 30) {
        val x = centerX + (lng * (width / 360f) * zoom)
        if (x in 0f..width) {
            drawLine(
                color = if (lng == 0) equatorColor else gridColor,
                start = Offset(x, 0f),
                end = Offset(x, height),
                strokeWidth = if (lng == 0) 1.5f else 0.8f
            )
        }
    }

    // Stylized continent clusters representation
    val continents = listOf(
        // Europe / Paris (48.8, 2.3)
        Triple(48.85f, 2.35f, 45f * zoom),
        // Middle East / Baghdad (33.3, 44.3)
        Triple(33.31f, 44.36f, 40f * zoom),
        // Gulf / Dubai (25.2, 55.2)
        Triple(25.20f, 55.27f, 35f * zoom),
        // North Africa / Cairo (30.0, 31.2)
        Triple(30.04f, 31.23f, 38f * zoom),
        // Southern Europe / Rome (41.9, 12.4)
        Triple(41.90f, 12.49f, 32f * zoom),
        // East Asia / Tokyo (35.6, 139.6)
        Triple(35.67f, 139.65f, 42f * zoom)
    )

    for ((cLat, cLng, radius) in continents) {
        val scaleFactor = (width / 360f) * zoom
        val x = centerX + (cLng * scaleFactor)
        val y = centerY - (cLat * scaleFactor * 1.3f)

        drawCircle(
            color = DarkSurfaceVariant.copy(alpha = 0.55f),
            radius = radius,
            center = Offset(x, y)
        )
    }
}
