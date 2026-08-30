package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.data.model.LocationVisibility
import com.example.data.model.MessageType
import com.example.data.model.NotificationType
import com.example.data.model.PrecisionLevel
import com.example.data.model.RelationshipStatus
import com.example.data.model.RelationshipType
import com.example.data.model.RelationshipVisibility
import com.example.data.model.ReportReason
import com.example.data.model.ReportStatus
import com.example.data.model.ReportTargetType
import com.example.data.model.StoryVisibility
import com.example.data.model.UserRole

@Entity(
    tableName = "users",
    indices = [
        Index(value = ["username"], unique = true),
        Index(value = ["email"], unique = true)
    ]
)
data class UserEntity(
    @PrimaryKey val id: String,
    val username: String,
    val displayName: String,
    val email: String,
    val passwordHash: String = "",
    val avatarUrl: String = "",
    val coverUrl: String = "",
    val bio: String = "",
    val age: Int = 24,
    val country: String = "France",
    val countryCode: String = "FR",
    val city: String = "Paris",
    val interests: String = "Travel, Photography, Tech",
    val languages: String = "English, French",
    val isGhostMode: Boolean = false,
    val isVerified: Boolean = false,
    val role: UserRole = UserRole.USER,
    val isSuspended: Boolean = false,
    val isBanned: Boolean = false,
    val lastActiveAt: Long = System.currentTimeMillis(),
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "location_permissions",
    indices = [Index(value = ["userId"], unique = true)]
)
data class LocationPermissionEntity(
    @PrimaryKey val userId: String,
    val visibility: LocationVisibility = LocationVisibility.NOBODY,
    val precision: PrecisionLevel = PrecisionLevel.APPROXIMATE,
    val ghostMode: Boolean = false,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val country: String = "",
    val countryCode: String = "",
    val city: String = "",
    val lastSeen: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "relationships",
    indices = [
        Index(value = ["userAId", "userBId", "type"], unique = true),
        Index(value = ["status"]),
        Index(value = ["userAId"]),
        Index(value = ["userBId"])
    ]
)
data class RelationshipEntity(
    @PrimaryKey val id: String,
    val userAId: String,
    val userBId: String,
    val type: RelationshipType,
    val status: RelationshipStatus,
    val visibility: RelationshipVisibility = RelationshipVisibility.GLOBAL,
    val requestedBy: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val endedAt: Long? = null,
    val metadata: String = ""
)

@Entity(
    tableName = "friendships",
    indices = [
        Index(value = ["userAId", "userBId"], unique = true),
        Index(value = ["status"])
    ]
)
data class FriendshipEntity(
    @PrimaryKey val id: String,
    val userAId: String,
    val userBId: String,
    val status: String, // PENDING, ACTIVE, REJECTED, BLOCKED
    val requestedBy: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "follows",
    indices = [
        Index(value = ["followerId", "followingId"], unique = true)
    ]
)
data class FollowEntity(
    @PrimaryKey val id: String,
    val followerId: String,
    val followingId: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "conversations"
)
data class ConversationEntity(
    @PrimaryKey val id: String,
    val title: String = "",
    val isGroup: Boolean = false,
    val lastMessageText: String? = null,
    val lastMessageTime: Long = System.currentTimeMillis(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "conversation_members",
    primaryKeys = ["conversationId", "userId"],
    indices = [Index(value = ["userId"])]
)
data class ConversationMemberEntity(
    val conversationId: String,
    val userId: String,
    val joinedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "messages",
    indices = [
        Index(value = ["conversationId"]),
        Index(value = ["createdAt"])
    ]
)
data class MessageEntity(
    @PrimaryKey val id: String,
    val conversationId: String,
    val senderId: String,
    val text: String,
    val mediaUrl: String = "",
    val mediaType: MessageType = MessageType.TEXT,
    val replyToMessageId: String? = null,
    val reactionsJson: String = "{}", // Map of emoji -> list of userIds
    val isDisappearing: Boolean = false,
    val expiresAt: Long? = null,
    val isEdited: Boolean = false,
    val isDeleted: Boolean = false,
    val readByJson: String = "[]",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "stories",
    indices = [
        Index(value = ["userId"]),
        Index(value = ["expiresAt"])
    ]
)
data class StoryEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val mediaUrl: String = "",
    val textCaption: String = "",
    val backgroundColorHex: Long = 0xFF1E293B,
    val visibility: StoryVisibility = StoryVisibility.EVERYONE,
    val createdAt: Long = System.currentTimeMillis(),
    val expiresAt: Long = System.currentTimeMillis() + (24 * 60 * 60 * 1000L),
    val viewsCount: Int = 0
)

@Entity(
    tableName = "story_views",
    indices = [
        Index(value = ["storyId", "viewerId"], unique = true)
    ]
)
data class StoryViewEntity(
    @PrimaryKey val id: String,
    val storyId: String,
    val viewerId: String,
    val viewedAt: Long = System.currentTimeMillis(),
    val reaction: String? = null
)

@Entity(
    tableName = "blocks",
    indices = [
        Index(value = ["blockerId", "blockedId"], unique = true)
    ]
)
data class BlockEntity(
    @PrimaryKey val id: String,
    val blockerId: String,
    val blockedId: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "reports",
    indices = [Index(value = ["status"])]
)
data class ReportEntity(
    @PrimaryKey val id: String,
    val reporterId: String,
    val targetType: ReportTargetType,
    val targetId: String,
    val reason: ReportReason,
    val details: String,
    val status: ReportStatus = ReportStatus.PENDING,
    val moderatorNotes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "notifications",
    indices = [
        Index(value = ["userId"]),
        Index(value = ["createdAt"])
    ]
)
data class NotificationEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val type: NotificationType,
    val actorId: String,
    val actorName: String,
    val title: String,
    val body: String,
    val isRead: Boolean = false,
    val targetId: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "community_events")
data class CommunityEventEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val category: String,
    val country: String,
    val city: String,
    val latitude: Double,
    val longitude: Double,
    val eventDate: String,
    val creatorId: String,
    val attendeesCount: Int = 12
)

@Entity(
    tableName = "streaks",
    indices = [
        Index(value = ["userAId", "userBId"], unique = true),
        Index(value = ["userAId"]),
        Index(value = ["userBId"])
    ]
)
data class StreakEntity(
    @PrimaryKey val id: String,
    val userAId: String,
    val userBId: String,
    val streakCount: Int = 1,
    val bestStreakCount: Int = 1,
    val lastInteractionTimestamp: Long = System.currentTimeMillis(),
    val lastSenderId: String = "",
    val userAInteractedToday: Boolean = true,
    val userBInteractedToday: Boolean = false,
    val expiresAt: Long = System.currentTimeMillis() + (24 * 60 * 60 * 1000L),
    val isFrozen: Boolean = false,
    val freezeTokensLeft: Int = 2,
    val lastStreakDate: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "live_streams",
    indices = [
        Index(value = ["hostUserId"]),
        Index(value = ["isLive"])
    ]
)
data class LiveStreamEntity(
    @PrimaryKey val id: String,
    val hostUserId: String,
    val title: String,
    val category: String = "Chat & Hangout",
    val coverUrl: String = "",
    val viewerCount: Int = 1,
    val likesCount: Int = 0,
    val isLive: Boolean = true,
    val startedAt: Long = System.currentTimeMillis(),
    val playbackUrl: String = "",
    val tags: String = "Live, Community, Video"
)

@Entity(
    tableName = "short_videos",
    indices = [
        Index(value = ["creatorId"]),
        Index(value = ["createdAt"])
    ]
)
data class ShortVideoEntity(
    @PrimaryKey val id: String,
    val creatorId: String,
    val videoUrl: String,
    val thumbnailUrl: String,
    val caption: String,
    val soundTitle: String = "Original Audio",
    val soundArtist: String = "KinSphere Sounds",
    val likesCount: Int = 0,
    val commentsCount: Int = 0,
    val sharesCount: Int = 0,
    val isLikedByMe: Boolean = false,
    val isSavedByMe: Boolean = false,
    val tags: String = "#viral #reels",
    val filterName: String = "Normal",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "short_comments",
    indices = [Index(value = ["shortId"])]
)
data class ShortCommentEntity(
    @PrimaryKey val id: String,
    val shortId: String,
    val userId: String,
    val userName: String,
    val userAvatar: String,
    val text: String,
    val likesCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "bereal_posts",
    indices = [
        Index(value = ["userId"]),
        Index(value = ["createdAt"])
    ]
)
data class BeRealPostEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val primaryImageUrl: String, // Back camera (Environment / Scene)
    val secondaryImageUrl: String, // Front camera (Selfie / Reaction)
    val caption: String = "",
    val takenLateSeconds: Int = 0, // 0 = On Time, >0 = e.g. 120s late
    val locationName: String = "Paris, France",
    val realMojisJson: String = "{}", // Map of emoji -> count or list of user avatars
    val filterApplied: String = "BeReal Raw",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "app_updates")
data class AppUpdateEntity(
    @PrimaryKey val version: String,
    val title: String,
    val releaseDate: String,
    val highlightsJson: String, // List of feature bullet points
    val downloadSizeMb: Double = 32.4,
    val isInstalled: Boolean = true,
    val isCurrentVersion: Boolean = false
)

@Entity(tableName = "broadcast_channels")
data class BroadcastChannelEntity(
    @PrimaryKey val id: String,
    val title: String,
    val handle: String,
    val description: String,
    val avatarUrl: String,
    val subscribersCount: Int,
    val lastPostText: String,
    val lastPostTime: Long,
    val isJoined: Boolean = false,
    val verifiedBadge: Boolean = true
)


