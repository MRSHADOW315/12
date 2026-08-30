package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.ViewCarousel
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults.SecondaryIndicator
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.LiveStreamEntity
import com.example.data.repository.KinSphereRepository
import com.example.ui.hub.MegaEntertainmentHubScreen
import androidx.compose.material.icons.filled.AutoAwesome
import com.example.ui.bereal.BeRealFeedScreen
import com.example.ui.camera.CameraStudioScreen
import com.example.ui.chat.ChatScreen
import com.example.ui.live.FullscreenLiveViewer
import com.example.ui.live.LiveStreamHubScreen
import com.example.ui.map.MapScreen
import com.example.ui.network.NetworkScreen
import com.example.ui.profile.ProfileScreen
import com.example.ui.relationships.RelationshipsScreen
import com.example.ui.shorts.ShortsReelsScreen
import com.example.ui.social.FollowersFollowingScreen
import com.example.ui.stories.StoriesScreen
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceBorder
import com.example.ui.theme.PrimaryLight
import com.example.ui.theme.PrimaryNeon
import com.example.ui.theme.RomanticRed
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.updates.AppUpdatesAndChannelsScreen

sealed class NavTab(val title: String, val icon: ImageVector, val tag: String) {
    object Universe : NavTab("Universe", Icons.Default.AutoAwesome, "tab_universe")
    object Map : NavTab("Map", Icons.Default.Public, "tab_map")
    object Reels : NavTab("Reels", Icons.Default.Videocam, "tab_reels")
    object Live : NavTab("Live", Icons.Default.Radio, "tab_live")
    object BeReal : NavTab("BeReal", Icons.Default.CameraAlt, "tab_bereal")
    object Stories : NavTab("Stories", Icons.Default.ViewCarousel, "tab_stories")
    object Chat : NavTab("Chat", Icons.Default.ChatBubble, "tab_chat")
    object Social : NavTab("Followers", Icons.Default.People, "tab_social")
    object AppDate : NavTab("AppDate", Icons.Default.Campaign, "tab_appdate")
    object Network : NavTab("Network", Icons.Default.Hub, "tab_network")
    object Relations : NavTab("Relations", Icons.Default.Favorite, "tab_relations")
    object Profile : NavTab("Profile", Icons.Default.Person, "tab_profile")
}

@Composable
fun KinSphereMainScreen(
    repository: KinSphereRepository
) {
    var currentTab by remember { mutableStateOf<NavTab>(NavTab.Universe) }
    var targetChatUserId by remember { mutableStateOf<String?>(null) }
    var isCameraStudioOpen by remember { mutableStateOf(false) }
    var activeFullscreenLiveStream by remember { mutableStateOf<LiveStreamEntity?>(null) }

    val unreadNotifs by repository.getUnreadNotifCount().collectAsState(initial = 0)

    val navTabs = listOf(
        NavTab.Universe,
        NavTab.Map,
        NavTab.Reels,
        NavTab.Live,
        NavTab.BeReal,
        NavTab.Stories,
        NavTab.Chat,
        NavTab.Social,
        NavTab.AppDate,
        NavTab.Network,
        NavTab.Relations,
        NavTab.Profile
    )

    if (isCameraStudioOpen) {
        CameraStudioScreen(
            onClose = { isCameraStudioOpen = false },
            onMediaUploaded = { uploadedUrl ->
                isCameraStudioOpen = false
            }
        )
    } else if (activeFullscreenLiveStream != null) {
        FullscreenLiveViewer(
            stream = activeFullscreenLiveStream!!,
            repository = repository,
            onClose = { activeFullscreenLiveStream = null }
        )
    } else {
        Scaffold(
            bottomBar = {
                ScrollableTabRow(
                    selectedTabIndex = navTabs.indexOf(currentTab).coerceAtLeast(0),
                    containerColor = DarkSurface,
                    contentColor = PrimaryNeon,
                    edgePadding = 8.dp,
                    indicator = { tabPositions ->
                        val index = navTabs.indexOf(currentTab).coerceAtLeast(0)
                        SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[index]),
                            color = if (currentTab == NavTab.Live) RomanticRed else PrimaryNeon
                        )
                    },
                    modifier = Modifier.testTag("bottom_nav_bar")
                ) {
                    navTabs.forEach { tab ->
                        val isSelected = currentTab == tab
                        Tab(
                            selected = isSelected,
                            onClick = {
                                currentTab = tab
                                if (tab != NavTab.Chat) targetChatUserId = null
                            },
                            icon = {
                                Icon(
                                    imageVector = tab.icon,
                                    contentDescription = tab.title,
                                    tint = when {
                                        isSelected && tab == NavTab.Live -> RomanticRed
                                        isSelected -> PrimaryNeon
                                        else -> TextSecondary
                                    },
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            text = {
                                Text(
                                    text = tab.title,
                                    color = when {
                                        isSelected && tab == NavTab.Live -> RomanticRed
                                        isSelected -> PrimaryLight
                                        else -> TextMuted
                                    },
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            modifier = Modifier.testTag(tab.tag)
                        )
                    }
                }
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { isCameraStudioOpen = true },
                    containerColor = PrimaryNeon,
                    contentColor = Color.Black,
                    shape = CircleShape,
                    modifier = Modifier.testTag("main_camera_fab")
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = "Camera Studio")
                }
            },
            containerColor = DarkBackground
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                when (currentTab) {
                    NavTab.Universe -> MegaEntertainmentHubScreen(
                        repository = repository
                    )
                    NavTab.Map -> MapScreen(
                        repository = repository,
                        onNavigateToChat = { userId ->
                            targetChatUserId = userId
                            currentTab = NavTab.Chat
                        },
                        onNavigateToProfile = { currentTab = NavTab.Profile }
                    )
                    NavTab.Reels -> ShortsReelsScreen(
                        repository = repository,
                        onOpenLiveHub = { currentTab = NavTab.Live },
                        onOpenCameraStudio = { isCameraStudioOpen = true }
                    )
                    NavTab.Live -> LiveStreamHubScreen(
                        repository = repository,
                        onOpenLiveViewer = { stream ->
                            activeFullscreenLiveStream = stream
                        }
                    )
                    NavTab.BeReal -> BeRealFeedScreen(
                        repository = repository,
                        onTakeBeReal = { isCameraStudioOpen = true }
                    )
                    NavTab.Stories -> StoriesScreen(
                        repository = repository,
                        onNavigateToChat = { userId ->
                            targetChatUserId = userId
                            currentTab = NavTab.Chat
                        }
                    )
                    NavTab.Chat -> ChatScreen(
                        repository = repository,
                        initialTargetUserId = targetChatUserId,
                        onNavigateToProfile = { currentTab = NavTab.Profile }
                    )
                    NavTab.Social -> FollowersFollowingScreen(
                        repository = repository,
                        onOpenDirectChat = { userId ->
                            targetChatUserId = userId
                            currentTab = NavTab.Chat
                        },
                        onOpenUserProfile = { currentTab = NavTab.Profile }
                    )
                    NavTab.AppDate -> AppUpdatesAndChannelsScreen(
                        repository = repository
                    )
                    NavTab.Network -> NetworkScreen(
                        repository = repository,
                        onNavigateToChat = { userId ->
                            targetChatUserId = userId
                            currentTab = NavTab.Chat
                        },
                        onNavigateToProfile = { currentTab = NavTab.Profile }
                    )
                    NavTab.Relations -> RelationshipsScreen(
                        repository = repository,
                        onNavigateToProfile = { currentTab = NavTab.Profile }
                    )
                    NavTab.Profile -> ProfileScreen(
                        repository = repository
                    )
                }
            }
        }
    }
}

