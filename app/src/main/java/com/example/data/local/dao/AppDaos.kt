package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entities.AppUpdateEntity
import com.example.data.local.entities.BeRealPostEntity
import com.example.data.local.entities.BlockEntity
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
import com.example.data.local.entities.StoryViewEntity
import com.example.data.local.entities.StreakEntity
import com.example.data.local.entities.UserEntity
import com.example.data.model.RelationshipStatus
import com.example.data.model.RelationshipType
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    fun getUserById(id: String): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getUserByIdOnce(id: String): UserEntity?

    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun getUserByUsername(username: String): UserEntity?

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users WHERE isBanned = 0 ORDER BY displayName ASC")
    fun getAllActiveUsers(): Flow<List<UserEntity>>

    @Query("SELECT * FROM users ORDER BY displayName ASC")
    fun getAllUsers(): Flow<List<UserEntity>>

    @Query("SELECT * FROM users WHERE (username LIKE '%' || :query || '%' OR displayName LIKE '%' || :query || '%') AND isBanned = 0")
    fun searchUsers(query: String): Flow<List<UserEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("UPDATE users SET isGhostMode = :isGhost WHERE id = :userId")
    suspend fun setGhostMode(userId: String, isGhost: Boolean)

    @Query("UPDATE users SET isBanned = :isBanned, isSuspended = :isSuspended WHERE id = :userId")
    suspend fun setModerationStatus(userId: String, isBanned: Boolean, isSuspended: Boolean)
}

@Dao
interface LocationDao {
    @Query("SELECT * FROM location_permissions WHERE userId = :userId LIMIT 1")
    fun getLocationPermission(userId: String): Flow<LocationPermissionEntity?>

    @Query("SELECT * FROM location_permissions WHERE userId = :userId LIMIT 1")
    suspend fun getLocationPermissionOnce(userId: String): LocationPermissionEntity?

    @Query("SELECT * FROM location_permissions")
    fun getAllLocations(): Flow<List<LocationPermissionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocationPermission(permission: LocationPermissionEntity)

    @Update
    suspend fun updateLocationPermission(permission: LocationPermissionEntity)

    @Query("UPDATE location_permissions SET ghostMode = :ghostMode WHERE userId = :userId")
    suspend fun setGhostMode(userId: String, ghostMode: Boolean)
}

@Dao
interface RelationshipDao {
    @Query("SELECT * FROM relationships WHERE (userAId = :userId OR userBId = :userId)")
    fun getRelationshipsForUser(userId: String): Flow<List<RelationshipEntity>>

    @Query("SELECT * FROM relationships WHERE ((userAId = :userAId AND userBId = :userBId) OR (userAId = :userBId AND userBId = :userAId)) AND type = :type LIMIT 1")
    suspend fun findRelationship(userAId: String, userBId: String, type: RelationshipType): RelationshipEntity?

    @Query("SELECT * FROM relationships WHERE ((userAId = :userAId AND userBId = :userBId) OR (userAId = :userBId AND userBId = :userAId))")
    suspend fun getRelationshipsBetween(userAId: String, userBId: String): List<RelationshipEntity>

    @Query("SELECT * FROM relationships WHERE status = 'ACTIVE'")
    fun getAllActiveRelationships(): Flow<List<RelationshipEntity>>

    @Query("SELECT * FROM relationships")
    fun getAllRelationships(): Flow<List<RelationshipEntity>>

    @Query("SELECT * FROM relationships WHERE id = :id LIMIT 1")
    suspend fun getRelationshipById(id: String): RelationshipEntity?

    @Query("SELECT * FROM relationships WHERE (userAId = :userId OR userBId = :userId) AND status = :status")
    fun getRelationshipsByStatus(userId: String, status: RelationshipStatus): Flow<List<RelationshipEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRelationship(relationship: RelationshipEntity)

    @Update
    suspend fun updateRelationship(relationship: RelationshipEntity)

    @Query("DELETE FROM relationships WHERE id = :id")
    suspend fun deleteRelationship(id: String)
}

@Dao
interface FriendshipDao {
    @Query("SELECT * FROM friendships WHERE (userAId = :userId OR userBId = :userId) AND status = 'ACTIVE'")
    fun getActiveFriendships(userId: String): Flow<List<FriendshipEntity>>

    @Query("SELECT * FROM friendships WHERE (userAId = :userId OR userBId = :userId) AND status = 'ACTIVE'")
    suspend fun getActiveFriendshipsOnce(userId: String): List<FriendshipEntity>

    @Query("SELECT * FROM friendships WHERE userBId = :userId AND status = 'PENDING'")
    fun getPendingReceivedRequests(userId: String): Flow<List<FriendshipEntity>>

    @Query("SELECT * FROM friendships WHERE ((userAId = :userAId AND userBId = :userBId) OR (userAId = :userBId AND userBId = :userAId)) LIMIT 1")
    suspend fun findFriendship(userAId: String, userBId: String): FriendshipEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFriendship(friendship: FriendshipEntity)

    @Update
    suspend fun updateFriendship(friendship: FriendshipEntity)

    @Query("DELETE FROM friendships WHERE id = :id")
    suspend fun deleteFriendship(id: String)
}

@Dao
interface FollowDao {
    @Query("SELECT * FROM follows WHERE followerId = :userId")
    fun getFollowing(userId: String): Flow<List<FollowEntity>>

    @Query("SELECT * FROM follows WHERE followingId = :userId")
    fun getFollowers(userId: String): Flow<List<FollowEntity>>

    @Query("SELECT * FROM follows WHERE followerId = :followerId AND followingId = :followingId LIMIT 1")
    suspend fun findFollow(followerId: String, followingId: String): FollowEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFollow(follow: FollowEntity)

    @Query("DELETE FROM follows WHERE followerId = :followerId AND followingId = :followingId")
    suspend fun deleteFollow(followerId: String, followingId: String)
}

@Dao
interface ChatDao {
    @Query("SELECT c.* FROM conversations c INNER JOIN conversation_members m ON c.id = m.conversationId WHERE m.userId = :userId ORDER BY c.lastMessageTime DESC")
    fun getConversationsForUser(userId: String): Flow<List<ConversationEntity>>

    @Query("SELECT * FROM conversations WHERE id = :id LIMIT 1")
    suspend fun getConversationById(id: String): ConversationEntity?

    @Query("SELECT userId FROM conversation_members WHERE conversationId = :conversationId")
    suspend fun getConversationMemberIds(conversationId: String): List<String>

    @Query("SELECT * FROM messages WHERE conversationId = :conversationId AND isDeleted = 0 ORDER BY createdAt ASC")
    fun getMessagesForConversation(conversationId: String): Flow<List<MessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConversation(conversation: ConversationEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMember(member: ConversationMemberEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity)

    @Update
    suspend fun updateMessage(message: MessageEntity)

    @Query("UPDATE messages SET isDeleted = 1 WHERE id = :messageId")
    suspend fun softDeleteMessage(messageId: String)

    @Query("UPDATE conversations SET lastMessageText = :text, lastMessageTime = :time, updatedAt = :time WHERE id = :conversationId")
    suspend fun updateConversationLastMessage(conversationId: String, text: String, time: Long)
}

@Dao
interface StoryDao {
    @Query("SELECT * FROM stories WHERE expiresAt > :now ORDER BY createdAt DESC")
    fun getActiveStories(now: Long): Flow<List<StoryEntity>>

    @Query("SELECT * FROM stories WHERE userId = :userId AND expiresAt > :now ORDER BY createdAt DESC")
    fun getStoriesByUser(userId: String, now: Long): Flow<List<StoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStory(story: StoryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun recordStoryView(view: StoryViewEntity)

    @Query("SELECT * FROM story_views WHERE storyId = :storyId")
    fun getStoryViews(storyId: String): Flow<List<StoryViewEntity>>

    @Query("UPDATE stories SET viewsCount = viewsCount + 1 WHERE id = :storyId")
    suspend fun incrementViewCount(storyId: String)
}

@Dao
interface BlockDao {
    @Query("SELECT * FROM blocks WHERE blockerId = :userId")
    fun getBlockedUsers(userId: String): Flow<List<BlockEntity>>

    @Query("SELECT * FROM blocks WHERE (blockerId = :userAId AND blockedId = :userBId) OR (blockerId = :userBId AND blockedId = :userAId) LIMIT 1")
    suspend fun isBlocked(userAId: String, userBId: String): BlockEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBlock(block: BlockEntity)

    @Query("DELETE FROM blocks WHERE blockerId = :blockerId AND blockedId = :blockedId")
    suspend fun deleteBlock(blockerId: String, blockedId: String)
}

@Dao
interface ReportDao {
    @Query("SELECT * FROM reports ORDER BY createdAt DESC")
    fun getAllReports(): Flow<List<ReportEntity>>

    @Query("SELECT * FROM reports WHERE status = :status ORDER BY createdAt DESC")
    fun getReportsByStatus(status: String): Flow<List<ReportEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReport(report: ReportEntity)

    @Update
    suspend fun updateReport(report: ReportEntity)
}

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notifications WHERE userId = :userId ORDER BY createdAt DESC")
    fun getNotificationsForUser(userId: String): Flow<List<NotificationEntity>>

    @Query("SELECT COUNT(*) FROM notifications WHERE userId = :userId AND isRead = 0")
    fun getUnreadCount(userId: String): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity)

    @Query("UPDATE notifications SET isRead = 1 WHERE userId = :userId")
    suspend fun markAllAsRead(userId: String)
}

@Dao
interface CommunityEventDao {
    @Query("SELECT * FROM community_events ORDER BY attendeesCount DESC")
    fun getAllEvents(): Flow<List<CommunityEventEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: CommunityEventEntity)
}

@Dao
interface StreakDao {
    @Query("SELECT * FROM streaks WHERE (userAId = :userId OR userBId = :userId) ORDER BY streakCount DESC")
    fun getStreaksForUser(userId: String): Flow<List<StreakEntity>>

    @Query("SELECT * FROM streaks WHERE (userAId = :userAId AND userBId = :userBId) OR (userAId = :userBId AND userBId = :userAId) LIMIT 1")
    fun getStreakBetween(userAId: String, userBId: String): Flow<StreakEntity?>

    @Query("SELECT * FROM streaks WHERE (userAId = :userAId AND userBId = :userBId) OR (userAId = :userBId AND userBId = :userAId) LIMIT 1")
    suspend fun getStreakBetweenOnce(userAId: String, userBId: String): StreakEntity?

    @Query("SELECT * FROM streaks WHERE id = :id LIMIT 1")
    suspend fun getStreakById(id: String): StreakEntity?

    @Query("SELECT * FROM streaks")
    fun getAllStreaks(): Flow<List<StreakEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStreak(streak: StreakEntity)

    @Update
    suspend fun updateStreak(streak: StreakEntity)

    @Query("DELETE FROM streaks WHERE id = :id")
    suspend fun deleteStreak(id: String)
}

@Dao
interface LiveStreamDao {
    @Query("SELECT * FROM live_streams WHERE isLive = 1 ORDER BY viewerCount DESC, startedAt DESC")
    fun getActiveLiveStreams(): Flow<List<LiveStreamEntity>>

    @Query("SELECT * FROM live_streams WHERE id = :id LIMIT 1")
    fun getLiveStreamById(id: String): Flow<LiveStreamEntity?>

    @Query("SELECT * FROM live_streams WHERE id = :id LIMIT 1")
    suspend fun getLiveStreamByIdOnce(id: String): LiveStreamEntity?

    @Query("SELECT * FROM live_streams WHERE hostUserId = :userId ORDER BY startedAt DESC LIMIT 1")
    suspend fun getLatestStreamByUser(userId: String): LiveStreamEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLiveStream(liveStream: LiveStreamEntity)

    @Update
    suspend fun updateLiveStream(liveStream: LiveStreamEntity)

    @Query("UPDATE live_streams SET likesCount = likesCount + :count WHERE id = :id")
    suspend fun incrementLikes(id: String, count: Int = 1)

    @Query("UPDATE live_streams SET viewerCount = :count WHERE id = :id")
    suspend fun updateViewerCount(id: String, count: Int)

    @Query("UPDATE live_streams SET isLive = 0 WHERE id = :id")
    suspend fun endLiveStream(id: String)
}

@Dao
interface ShortVideoDao {
    @Query("SELECT * FROM short_videos ORDER BY createdAt DESC")
    fun getAllShortVideos(): Flow<List<ShortVideoEntity>>

    @Query("SELECT * FROM short_videos WHERE creatorId = :creatorId ORDER BY createdAt DESC")
    fun getShortVideosByCreator(creatorId: String): Flow<List<ShortVideoEntity>>

    @Query("SELECT * FROM short_videos WHERE id = :id LIMIT 1")
    suspend fun getShortVideoById(id: String): ShortVideoEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShortVideo(video: ShortVideoEntity)

    @Update
    suspend fun updateShortVideo(video: ShortVideoEntity)

    @Query("UPDATE short_videos SET isLikedByMe = :liked, likesCount = likesCount + :delta WHERE id = :id")
    suspend fun toggleLike(id: String, liked: Boolean, delta: Int)

    @Query("UPDATE short_videos SET isSavedByMe = :saved WHERE id = :id")
    suspend fun toggleSave(id: String, saved: Boolean)

    @Query("UPDATE short_videos SET sharesCount = sharesCount + 1 WHERE id = :id")
    suspend fun incrementShare(id: String)

    @Query("SELECT * FROM short_comments WHERE shortId = :shortId ORDER BY createdAt ASC")
    fun getCommentsForShort(shortId: String): Flow<List<ShortCommentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComment(comment: ShortCommentEntity)

    @Query("UPDATE short_videos SET commentsCount = commentsCount + 1 WHERE id = :shortId")
    suspend fun incrementCommentCount(shortId: String)
}

@Dao
interface BeRealDao {
    @Query("SELECT * FROM bereal_posts ORDER BY createdAt DESC")
    fun getAllBeRealPosts(): Flow<List<BeRealPostEntity>>

    @Query("SELECT * FROM bereal_posts WHERE userId = :userId ORDER BY createdAt DESC")
    fun getPostsByUser(userId: String): Flow<List<BeRealPostEntity>>

    @Query("SELECT * FROM bereal_posts WHERE id = :id LIMIT 1")
    suspend fun getPostById(id: String): BeRealPostEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPost(post: BeRealPostEntity)

    @Update
    suspend fun updatePost(post: BeRealPostEntity)
}

@Dao
interface AppUpdateDao {
    @Query("SELECT * FROM app_updates ORDER BY releaseDate DESC")
    fun getAllUpdates(): Flow<List<AppUpdateEntity>>

    @Query("SELECT * FROM app_updates WHERE isCurrentVersion = 1 LIMIT 1")
    fun getCurrentVersion(): Flow<AppUpdateEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUpdate(update: AppUpdateEntity)
}

@Dao
interface BroadcastChannelDao {
    @Query("SELECT * FROM broadcast_channels ORDER BY subscribersCount DESC")
    fun getAllChannels(): Flow<List<BroadcastChannelEntity>>

    @Query("SELECT * FROM broadcast_channels WHERE isJoined = 1 ORDER BY lastPostTime DESC")
    fun getJoinedChannels(): Flow<List<BroadcastChannelEntity>>

    @Query("SELECT * FROM broadcast_channels WHERE id = :id LIMIT 1")
    suspend fun getChannelById(id: String): BroadcastChannelEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChannel(channel: BroadcastChannelEntity)

    @Query("UPDATE broadcast_channels SET isJoined = :joined, subscribersCount = subscribersCount + :delta WHERE id = :id")
    suspend fun toggleJoin(id: String, joined: Boolean, delta: Int)
}


