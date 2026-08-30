package com.example.data.local

import com.example.data.local.entities.AppUpdateEntity
import com.example.data.local.entities.BeRealPostEntity
import com.example.data.local.entities.BroadcastChannelEntity
import com.example.data.local.entities.CommunityEventEntity
import com.example.data.local.entities.ConversationEntity
import com.example.data.local.entities.ConversationMemberEntity
import com.example.data.local.entities.FollowEntity
import com.example.data.local.entities.FriendshipEntity
import com.example.data.local.entities.LiveStreamEntity
import com.example.data.local.entities.LocationPermissionEntity
import com.example.data.local.entities.MessageEntity
import com.example.data.local.entities.NotificationEntity
import com.example.data.local.entities.RelationshipEntity
import com.example.data.local.entities.ReportEntity
import com.example.data.local.entities.ShortCommentEntity
import com.example.data.local.entities.ShortVideoEntity
import com.example.data.local.entities.StoryEntity
import com.example.data.local.entities.StreakEntity
import com.example.data.local.entities.UserEntity

/**
 * Production starter state: No fake people, no fake messages, no fake profiles.
 * Real data is loaded dynamically from Firebase Auth and Firestore.
 */
object SeedData {
    val users: List<UserEntity> = emptyList()
    val locationPermissions: List<LocationPermissionEntity> = emptyList()
    val relationships: List<RelationshipEntity> = emptyList()
    val friendships: List<FriendshipEntity> = emptyList()
    val follows: List<FollowEntity> = emptyList()
    val conversations: List<ConversationEntity> = emptyList()
    val conversationMembers: List<ConversationMemberEntity> = emptyList()
    val messages: List<MessageEntity> = emptyList()
    val stories: List<StoryEntity> = emptyList()
    val notifications: List<NotificationEntity> = emptyList()
    val communityEvents: List<CommunityEventEntity> = emptyList()
    val reports: List<ReportEntity> = emptyList()
    val streaks: List<StreakEntity> = emptyList()
    val liveStreams: List<LiveStreamEntity> = emptyList()
    val shortVideos: List<ShortVideoEntity> = emptyList()
    val shortComments: List<ShortCommentEntity> = emptyList()
    val beRealPosts: List<BeRealPostEntity> = emptyList()
    val appUpdates: List<AppUpdateEntity> = emptyList()
    val broadcastChannels: List<BroadcastChannelEntity> = emptyList()
}
