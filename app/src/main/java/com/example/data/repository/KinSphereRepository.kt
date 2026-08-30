package com.example.data.repository

import com.example.data.local.KinSphereDatabase
import com.example.data.local.SeedData
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
import com.example.service.privacy.EffectiveLocation
import com.example.service.privacy.PrivacyAuthorizationEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.util.UUID

class KinSphereRepository(private val database: KinSphereDatabase) {

    private val userDao = database.userDao()
    private val locationDao = database.locationDao()
    private val relationshipDao = database.relationshipDao()
    private val friendshipDao = database.friendshipDao()
    private val followDao = database.followDao()
    private val chatDao = database.chatDao()
    private val storyDao = database.storyDao()
    private val blockDao = database.blockDao()
    private val reportDao = database.reportDao()
    private val notificationDao = database.notificationDao()
    private val communityEventDao = database.communityEventDao()
    private val streakDao = database.streakDao()
    private val liveStreamDao = database.liveStreamDao()
    private val shortVideoDao = database.shortVideoDao()
    private val beRealDao = database.beRealDao()
    private val appUpdateDao = database.appUpdateDao()
    private val broadcastChannelDao = database.broadcastChannelDao()

    private val _currentUserId = MutableStateFlow("user_ahmed")
    val currentUserId: StateFlow<String> = _currentUserId.asStateFlow()

    init {
        CoroutineScope(Dispatchers.IO).launch {
            seedDatabaseIfEmpty()
        }
    }

    suspend fun seedDatabaseIfEmpty() {
        val existing = userDao.getUserByIdOnce("user_ahmed")
        if (existing == null) {
            for (u in SeedData.users) userDao.insertUser(u)
            for (lp in SeedData.locationPermissions) locationDao.insertLocationPermission(lp)
            for (r in SeedData.relationships) relationshipDao.insertRelationship(r)
            for (f in SeedData.friendships) friendshipDao.insertFriendship(f)
            for (fol in SeedData.follows) followDao.insertFollow(fol)
            for (c in SeedData.conversations) chatDao.insertConversation(c)
            for (m in SeedData.conversationMembers) chatDao.insertMember(m)
            for (msg in SeedData.messages) chatDao.insertMessage(msg)
            for (st in SeedData.stories) storyDao.insertStory(st)
            for (n in SeedData.notifications) notificationDao.insertNotification(n)
            for (evt in SeedData.communityEvents) communityEventDao.insertEvent(evt)
            for (rep in SeedData.reports) reportDao.insertReport(rep)
            for (str in SeedData.streaks) streakDao.insertStreak(str)
            for (live in SeedData.liveStreams) liveStreamDao.insertLiveStream(live)
            for (short in SeedData.shortVideos) shortVideoDao.insertShortVideo(short)
            for (comm in SeedData.shortComments) shortVideoDao.insertComment(comm)
            for (br in SeedData.beRealPosts) beRealDao.insertPost(br)
            for (upd in SeedData.appUpdates) appUpdateDao.insertUpdate(upd)
            for (chan in SeedData.broadcastChannels) broadcastChannelDao.insertChannel(chan)
        }
    }

    fun switchCurrentUser(userId: String) {
        _currentUserId.value = userId
    }

    // --- USER PROFILE & AUTH ---
    fun getCurrentUser(): Flow<UserEntity?> = userDao.getUserById(_currentUserId.value)
    fun getUserById(id: String): Flow<UserEntity?> = userDao.getUserById(id)
    suspend fun getUserByIdOnce(id: String): UserEntity? = userDao.getUserByIdOnce(id)
    fun getAllUsers(): Flow<List<UserEntity>> = userDao.getAllActiveUsers()
    fun searchUsers(query: String): Flow<List<UserEntity>> = userDao.searchUsers(query)

    suspend fun updateProfile(
        displayName: String,
        bio: String,
        country: String,
        city: String,
        interests: String,
        languages: String
    ) {
        val user = userDao.getUserByIdOnce(_currentUserId.value) ?: return
        userDao.updateUser(
            user.copy(
                displayName = displayName,
                bio = bio,
                country = country,
                city = city,
                interests = interests,
                languages = languages
            )
        )
    }

    suspend fun registerUser(
        username: String,
        displayName: String,
        email: String,
        passwordHash: String,
        country: String,
        city: String
    ): Result<UserEntity> {
        val existingUsername = userDao.getUserByUsername(username)
        if (existingUsername != null) return Result.failure(Exception("Username is already taken."))
        val existingEmail = userDao.getUserByEmail(email)
        if (existingEmail != null) return Result.failure(Exception("Email is already registered."))

        val newUser = UserEntity(
            id = "user_" + UUID.randomUUID().toString().take(8),
            username = username,
            displayName = displayName,
            email = email,
            passwordHash = passwordHash,
            country = country,
            city = city,
            isGhostMode = false
        )
        userDao.insertUser(newUser)
        locationDao.insertLocationPermission(
            LocationPermissionEntity(
                userId = newUser.id,
                visibility = LocationVisibility.NOBODY,
                precision = PrecisionLevel.APPROXIMATE,
                country = country,
                city = city
            )
        )
        _currentUserId.value = newUser.id
        return Result.success(newUser)
    }

    suspend fun loginUser(usernameOrEmail: String, passwordHash: String): Result<UserEntity> {
        val user = userDao.getUserByUsername(usernameOrEmail) ?: userDao.getUserByEmail(usernameOrEmail)
        if (user == null || user.passwordHash != passwordHash) {
            return Result.failure(Exception("Invalid credentials"))
        }
        _currentUserId.value = user.id
        return Result.success(user)
    }

    // --- LOCATION & GHOST MODE ---
    fun getLocationPermission(userId: String): Flow<LocationPermissionEntity?> = locationDao.getLocationPermission(userId)
    fun getAllLocations(): Flow<List<LocationPermissionEntity>> = locationDao.getAllLocations()

    suspend fun updateLocationSettings(
        visibility: LocationVisibility,
        precision: PrecisionLevel,
        ghostMode: Boolean,
        latitude: Double?,
        longitude: Double?
    ) {
        val userId = _currentUserId.value
        val existing = locationDao.getLocationPermissionOnce(userId)
        val user = userDao.getUserByIdOnce(userId)
        val updated = (existing ?: LocationPermissionEntity(userId = userId)).copy(
            visibility = visibility,
            precision = precision,
            ghostMode = ghostMode,
            latitude = latitude ?: existing?.latitude,
            longitude = longitude ?: existing?.longitude,
            country = user?.country ?: existing?.country ?: "",
            city = user?.city ?: existing?.city ?: "",
            lastSeen = System.currentTimeMillis()
        )
        locationDao.insertLocationPermission(updated)
        userDao.setGhostMode(userId, ghostMode)
    }

    suspend fun toggleGhostMode(enabled: Boolean) {
        val userId = _currentUserId.value
        userDao.setGhostMode(userId, enabled)
        locationDao.setGhostMode(userId, enabled)
    }

    // --- RELATIONSHIPS & STATE MACHINE ---
    fun getRelationshipsForUser(userId: String): Flow<List<RelationshipEntity>> = relationshipDao.getRelationshipsForUser(userId)
    fun getAllActiveRelationships(): Flow<List<RelationshipEntity>> = relationshipDao.getAllActiveRelationships()
    fun getAllRelationships(): Flow<List<RelationshipEntity>> = relationshipDao.getAllRelationships()

    /**
     * Request Relationship: Lifecycle NONE -> REQUESTED/PENDING
     */
    suspend fun requestRelationship(
        targetUserId: String,
        type: RelationshipType,
        visibility: RelationshipVisibility = RelationshipVisibility.GLOBAL
    ): Result<RelationshipEntity> {
        val myId = _currentUserId.value
        if (myId == targetUserId) return Result.failure(Exception("Cannot form a relationship with yourself."))

        val isBlocked = blockDao.isBlocked(myId, targetUserId) != null
        if (isBlocked) return Result.failure(Exception("Cannot request relationship: User interaction blocked."))

        val existing = relationshipDao.findRelationship(myId, targetUserId, type)
        if (existing != null && existing.status == RelationshipStatus.ACTIVE) {
            return Result.failure(Exception("An active $type relationship already exists with this user."))
        }

        val rel = RelationshipEntity(
            id = "rel_" + UUID.randomUUID().toString().take(8),
            userAId = myId,
            userBId = targetUserId,
            type = type,
            status = RelationshipStatus.PENDING,
            visibility = visibility,
            requestedBy = myId,
            createdAt = System.currentTimeMillis()
        )
        relationshipDao.insertRelationship(rel)

        // Notify target
        val myUser = userDao.getUserByIdOnce(myId)
        notificationDao.insertNotification(
            NotificationEntity(
                id = UUID.randomUUID().toString(),
                userId = targetUserId,
                type = NotificationType.RELATIONSHIP_REQUEST,
                actorId = myId,
                actorName = myUser?.displayName ?: "Someone",
                title = "New Relationship Request",
                body = "${myUser?.displayName ?: "A user"} sent you a ${type.label} relationship request.",
                targetId = rel.id
            )
        )
        return Result.success(rel)
    }

    /**
     * Accept Relationship: Lifecycle PENDING -> ACTIVE (Both users consented!)
     */
    suspend fun acceptRelationship(relationshipId: String): Result<Unit> {
        val rel = relationshipDao.getRelationshipById(relationshipId) ?: return Result.failure(Exception("Not found"))
        val myId = _currentUserId.value
        if (rel.requestedBy == myId) {
            return Result.failure(Exception("Only the recipient can accept the relationship request."))
        }

        val updated = rel.copy(
            status = RelationshipStatus.ACTIVE,
            updatedAt = System.currentTimeMillis()
        )
        relationshipDao.updateRelationship(updated)

        // Also ensure friendship is created if type is FRIENDSHIP
        if (rel.type == RelationshipType.FRIENDSHIP) {
            val existingF = friendshipDao.findFriendship(rel.userAId, rel.userBId)
            if (existingF == null) {
                friendshipDao.insertFriendship(
                    FriendshipEntity(
                        id = UUID.randomUUID().toString(),
                        userAId = rel.userAId,
                        userBId = rel.userBId,
                        status = "ACTIVE",
                        requestedBy = rel.requestedBy
                    )
                )
            }
        }

        // Notify requester
        val myUser = userDao.getUserByIdOnce(myId)
        val targetId = if (rel.userAId == myId) rel.userBId else rel.userAId
        notificationDao.insertNotification(
            NotificationEntity(
                id = UUID.randomUUID().toString(),
                userId = targetId,
                type = NotificationType.RELATIONSHIP_ACCEPTED,
                actorId = myId,
                actorName = myUser?.displayName ?: "User",
                title = "Relationship Accepted",
                body = "${myUser?.displayName} accepted your ${rel.type.label} relationship request.",
                targetId = rel.id
            )
        )
        return Result.success(Unit)
    }

    /**
     * Reject Relationship: Lifecycle PENDING -> REJECTED
     */
    suspend fun rejectRelationship(relationshipId: String): Result<Unit> {
        val rel = relationshipDao.getRelationshipById(relationshipId) ?: return Result.failure(Exception("Not found"))
        val updated = rel.copy(
            status = RelationshipStatus.REJECTED,
            endedAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        relationshipDao.updateRelationship(updated)
        return Result.success(Unit)
    }

    /**
     * End Relationship: Lifecycle ACTIVE -> ENDED
     */
    suspend fun endRelationship(relationshipId: String): Result<Unit> {
        val rel = relationshipDao.getRelationshipById(relationshipId) ?: return Result.failure(Exception("Not found"))
        val updated = rel.copy(
            status = RelationshipStatus.ENDED,
            endedAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        relationshipDao.updateRelationship(updated)
        return Result.success(Unit)
    }

    // --- FRIENDSHIPS & FOLLOWS ---
    fun getActiveFriends(userId: String): Flow<List<FriendshipEntity>> = friendshipDao.getActiveFriendships(userId)

    suspend fun sendFriendRequest(targetUserId: String) {
        val myId = _currentUserId.value
        val existing = friendshipDao.findFriendship(myId, targetUserId)
        if (existing == null) {
            friendshipDao.insertFriendship(
                FriendshipEntity(
                    id = UUID.randomUUID().toString(),
                    userAId = myId,
                    userBId = targetUserId,
                    status = "PENDING",
                    requestedBy = myId
                )
            )
        }
    }

    suspend fun acceptFriendRequest(targetUserId: String) {
        val myId = _currentUserId.value
        val existing = friendshipDao.findFriendship(myId, targetUserId)
        if (existing != null) {
            friendshipDao.updateFriendship(existing.copy(status = "ACTIVE"))
        }
    }

    suspend fun toggleFollow(targetUserId: String) {
        val myId = _currentUserId.value
        val existing = followDao.findFollow(myId, targetUserId)
        if (existing != null) {
            followDao.deleteFollow(myId, targetUserId)
        } else {
            followDao.insertFollow(
                FollowEntity(
                    id = UUID.randomUUID().toString(),
                    followerId = myId,
                    followingId = targetUserId
                )
            )
        }
    }

    // --- CHAT & MESSAGING ---
    fun getConversations(): Flow<List<ConversationEntity>> = chatDao.getConversationsForUser(_currentUserId.value)
    fun getMessages(conversationId: String): Flow<List<MessageEntity>> = chatDao.getMessagesForConversation(conversationId)

    suspend fun sendMessage(
        conversationId: String,
        text: String,
        mediaUrl: String = "",
        mediaType: MessageType = MessageType.TEXT,
        isDisappearing: Boolean = false
    ) {
        val myId = _currentUserId.value
        val message = MessageEntity(
            id = "msg_" + UUID.randomUUID().toString().take(8),
            conversationId = conversationId,
            senderId = myId,
            text = text,
            mediaUrl = mediaUrl,
            mediaType = mediaType,
            isDisappearing = isDisappearing,
            expiresAt = if (isDisappearing) System.currentTimeMillis() + (60 * 1000L) else null,
            createdAt = System.currentTimeMillis()
        )
        chatDao.insertMessage(message)
        chatDao.updateConversationLastMessage(
            conversationId = conversationId,
            text = if (mediaType != MessageType.TEXT) "[${mediaType.name}] $text" else text,
            time = System.currentTimeMillis()
        )

        // Automatically update streak if 1-on-1 direct conversation
        val memberIds = chatDao.getConversationMemberIds(conversationId)
        val otherMemberId = memberIds.find { it != myId }
        if (otherMemberId != null && memberIds.size == 2) {
            recordStreakInteraction(otherMemberId)
        }
    }

    suspend fun startOrGetDirectConversation(targetUserId: String): String {
        val myId = _currentUserId.value
        val convId = "conv_${if (myId < targetUserId) "${myId}_${targetUserId}" else "${targetUserId}_${myId}"}"
        val existing = chatDao.getConversationById(convId)
        if (existing == null) {
            val targetUser = userDao.getUserByIdOnce(targetUserId)
            chatDao.insertConversation(
                ConversationEntity(
                    id = convId,
                    title = targetUser?.displayName ?: "Direct Chat",
                    isGroup = false
                )
            )
            chatDao.insertMember(ConversationMemberEntity(convId, myId))
            chatDao.insertMember(ConversationMemberEntity(convId, targetUserId))
        }
        return convId
    }

    // --- STREAKS (SNAPCHAT-STYLE INTERACTION ENGINE) ---
    fun getStreaksForCurrentUser(): Flow<List<StreakEntity>> = streakDao.getStreaksForUser(_currentUserId.value)
    fun getStreakBetween(targetUserId: String): Flow<StreakEntity?> = streakDao.getStreakBetween(_currentUserId.value, targetUserId)
    suspend fun getStreakBetweenOnce(targetUserId: String): StreakEntity? = streakDao.getStreakBetweenOnce(_currentUserId.value, targetUserId)

    suspend fun recordStreakInteraction(targetUserId: String): StreakEntity {
        val myId = _currentUserId.value
        if (myId == targetUserId) {
            return StreakEntity(id = "self", userAId = myId, userBId = targetUserId)
        }
        val streak = streakDao.getStreakBetweenOnce(myId, targetUserId)
        val now = System.currentTimeMillis()
        val todayDate = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date(now))

        val uA = if (myId < targetUserId) myId else targetUserId
        val uB = if (myId < targetUserId) targetUserId else myId
        val isUserA = (myId == uA)

        val updated = if (streak == null) {
            val newStreak = StreakEntity(
                id = "streak_${uA}_${uB}",
                userAId = uA,
                userBId = uB,
                streakCount = 1,
                bestStreakCount = 1,
                lastInteractionTimestamp = now,
                lastSenderId = myId,
                userAInteractedToday = isUserA,
                userBInteractedToday = !isUserA,
                expiresAt = now + (24 * 60 * 60 * 1000L),
                isFrozen = false,
                freezeTokensLeft = 2,
                lastStreakDate = todayDate,
                createdAt = now
            )
            streakDao.insertStreak(newStreak)
            newStreak
        } else {
            val isNewDate = streak.lastStreakDate != todayDate
            val alreadyInteractedToday = if (isUserA) streak.userAInteractedToday else streak.userBInteractedToday

            val otherInteractedToday = if (isUserA) {
                if (isNewDate) false else streak.userBInteractedToday
            } else {
                if (isNewDate) false else streak.userAInteractedToday
            }

            var count = streak.streakCount
            val isExpired = (now > streak.expiresAt && !streak.isFrozen)

            if (isExpired || count == 0) {
                count = 1
            } else if (!alreadyInteractedToday && otherInteractedToday) {
                // Both parties have now touched the streak in this active cycle! Streak goes up!
                count = streak.streakCount + 1
            }

            val newBest = maxOf(streak.bestStreakCount, count)
            val newExpiresAt = now + (24 * 60 * 60 * 1000L)

            val modified = streak.copy(
                streakCount = count,
                bestStreakCount = newBest,
                lastInteractionTimestamp = now,
                lastSenderId = myId,
                userAInteractedToday = if (isUserA) true else (if (isNewDate) false else streak.userAInteractedToday),
                userBInteractedToday = if (!isUserA) true else (if (isNewDate) false else streak.userBInteractedToday),
                expiresAt = newExpiresAt,
                isFrozen = false, // Active interaction melts freeze
                lastStreakDate = todayDate
            )
            streakDao.updateStreak(modified)
            modified
        }
        return updated
    }

    suspend fun freezeStreak(targetUserId: String): Boolean {
        val myId = _currentUserId.value
        val streak = streakDao.getStreakBetweenOnce(myId, targetUserId) ?: return false
        if (streak.freezeTokensLeft <= 0 || streak.isFrozen) return false

        val updated = streak.copy(
            isFrozen = true,
            freezeTokensLeft = streak.freezeTokensLeft - 1,
            expiresAt = System.currentTimeMillis() + (48 * 60 * 60 * 1000L)
        )
        streakDao.updateStreak(updated)
        return true
    }

    suspend fun restoreStreak(targetUserId: String): Boolean {
        val myId = _currentUserId.value
        val streak = streakDao.getStreakBetweenOnce(myId, targetUserId) ?: return false
        val restoredCount = if (streak.bestStreakCount > 0) streak.bestStreakCount else 1
        val updated = streak.copy(
            streakCount = restoredCount,
            expiresAt = System.currentTimeMillis() + (24 * 60 * 60 * 1000L),
            isFrozen = false
        )
        streakDao.updateStreak(updated)
        return true
    }

    suspend fun sendStreakSnap(targetUserId: String, caption: String, emojiBadge: String = "🔥") {
        val myId = _currentUserId.value
        val convId = startOrGetDirectConversation(targetUserId)
        val fullText = "$emojiBadge STREAK SNAP: ${caption.ifBlank { "Keeping our flame alive!" }}"
        
        sendMessage(
            conversationId = convId,
            text = fullText,
            mediaType = MessageType.IMAGE,
            mediaUrl = "https://images.unsplash.com/photo-1518199266791-5375a83190b7?w=500"
        )
        recordStreakInteraction(targetUserId)
    }

    // --- STORIES ---
    fun getActiveStories(): Flow<List<StoryEntity>> = storyDao.getActiveStories(System.currentTimeMillis())

    suspend fun postStory(mediaUrl: String, textCaption: String, visibility: StoryVisibility = StoryVisibility.EVERYONE) {
        val story = StoryEntity(
            id = "story_" + UUID.randomUUID().toString().take(8),
            userId = _currentUserId.value,
            mediaUrl = mediaUrl,
            textCaption = textCaption,
            visibility = visibility
        )
        storyDao.insertStory(story)
    }

    suspend fun createStory(mediaUrl: String, caption: String) {
        postStory(mediaUrl, caption)
    }

    suspend fun recordStoryView(storyId: String) {
        val myId = _currentUserId.value
        storyDao.recordStoryView(
            StoryViewEntity(
                id = UUID.randomUUID().toString(),
                storyId = storyId,
                viewerId = myId
            )
        )
        storyDao.incrementViewCount(storyId)
    }

    // --- BLOCKING & REPORTING ---
    suspend fun blockUser(targetUserId: String) {
        val myId = _currentUserId.value
        blockDao.insertBlock(
            BlockEntity(
                id = UUID.randomUUID().toString(),
                blockerId = myId,
                blockedId = targetUserId
            )
        )
    }

    suspend fun unblockUser(targetUserId: String) {
        val myId = _currentUserId.value
        blockDao.deleteBlock(myId, targetUserId)
    }

    suspend fun submitReport(targetType: ReportTargetType, targetId: String, reason: ReportReason, details: String) {
        val report = ReportEntity(
            id = "rep_" + UUID.randomUUID().toString().take(8),
            reporterId = _currentUserId.value,
            targetType = targetType,
            targetId = targetId,
            reason = reason,
            details = details,
            status = ReportStatus.PENDING
        )
        reportDao.insertReport(report)
    }

    // --- NOTIFICATIONS & COMMUNITY ---
    fun getNotifications(): Flow<List<NotificationEntity>> = notificationDao.getNotificationsForUser(_currentUserId.value)
    fun getUnreadNotifCount(): Flow<Int> = notificationDao.getUnreadCount(_currentUserId.value)
    suspend fun markNotificationsRead() = notificationDao.markAllAsRead(_currentUserId.value)
    fun getCommunityEvents(): Flow<List<CommunityEventEntity>> = communityEventDao.getAllEvents()

    // --- FOLLOWS & RELATIONSHIP GRAPH ---
    fun getFollowers(userId: String = _currentUserId.value): Flow<List<FollowEntity>> = followDao.getFollowers(userId)
    fun getFollowing(userId: String = _currentUserId.value): Flow<List<FollowEntity>> = followDao.getFollowing(userId)
    suspend fun removeFollower(followerUserId: String) {
        followDao.deleteFollow(followerUserId, _currentUserId.value)
    }

    // --- LIVE STREAMS HUB ---
    fun getActiveLiveStreams(): Flow<List<LiveStreamEntity>> = liveStreamDao.getActiveLiveStreams()
    fun getLiveStream(streamId: String): Flow<LiveStreamEntity?> = liveStreamDao.getLiveStreamById(streamId)
    suspend fun getLiveStreamOnce(streamId: String): LiveStreamEntity? = liveStreamDao.getLiveStreamByIdOnce(streamId)

    suspend fun startLiveStream(title: String, category: String = "Chat & Hangout", tags: String = "Live, Community"): LiveStreamEntity {
        val myId = _currentUserId.value
        val stream = LiveStreamEntity(
            id = "live_" + UUID.randomUUID().toString().take(8),
            hostUserId = myId,
            title = title,
            category = category,
            coverUrl = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=600",
            viewerCount = 1,
            likesCount = 0,
            isLive = true,
            startedAt = System.currentTimeMillis(),
            tags = tags
        )
        liveStreamDao.insertLiveStream(stream)
        return stream
    }

    suspend fun sendLiveLike(streamId: String, count: Int = 1) {
        liveStreamDao.incrementLikes(streamId, count)
    }

    suspend fun updateLiveViewerCount(streamId: String, count: Int) {
        liveStreamDao.updateViewerCount(streamId, count)
    }

    suspend fun endLiveStream(streamId: String) {
        liveStreamDao.endLiveStream(streamId)
    }

    // --- SHORT VIDEOS & REELS ---
    fun getAllShortVideos(): Flow<List<ShortVideoEntity>> = shortVideoDao.getAllShortVideos()
    fun getShortVideosByCreator(creatorId: String): Flow<List<ShortVideoEntity>> = shortVideoDao.getShortVideosByCreator(creatorId)
    fun getShortComments(shortId: String): Flow<List<ShortCommentEntity>> = shortVideoDao.getCommentsForShort(shortId)

    suspend fun toggleLikeShort(shortId: String) {
        val shorts = shortVideoDao.getAllShortVideos().first()
        val short = shorts.find { it.id == shortId } ?: return
        val newLiked = !short.isLikedByMe
        val delta = if (newLiked) 1 else -1
        shortVideoDao.toggleLike(shortId, newLiked, delta)
    }

    suspend fun toggleSaveShort(shortId: String) {
        val shorts = shortVideoDao.getAllShortVideos().first()
        val short = shorts.find { it.id == shortId } ?: return
        shortVideoDao.toggleSave(shortId, !short.isSavedByMe)
    }

    suspend fun shareShort(shortId: String) {
        shortVideoDao.incrementShare(shortId)
    }

    suspend fun postShortComment(shortId: String, text: String) {
        val myId = _currentUserId.value
        val user = userDao.getUserByIdOnce(myId)
        val comment = ShortCommentEntity(
            id = "scomm_" + UUID.randomUUID().toString().take(8),
            shortId = shortId,
            userId = myId,
            userName = user?.displayName ?: "Explorer",
            userAvatar = user?.avatarUrl ?: "",
            text = text,
            likesCount = 0,
            createdAt = System.currentTimeMillis()
        )
        shortVideoDao.insertComment(comment)
        shortVideoDao.incrementCommentCount(shortId)
    }

    suspend fun uploadShortVideo(
        caption: String,
        videoUrl: String = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
        thumbnailUrl: String = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=800",
        soundTitle: String = "Original Audio - KinSphere",
        soundArtist: String = "You",
        filterName: String = "Normal",
        tags: String = "#viral #reels #kinsphere"
    ): ShortVideoEntity {
        val myId = _currentUserId.value
        val short = ShortVideoEntity(
            id = "short_" + UUID.randomUUID().toString().take(8),
            creatorId = myId,
            videoUrl = videoUrl,
            thumbnailUrl = thumbnailUrl,
            caption = caption,
            soundTitle = soundTitle,
            soundArtist = soundArtist,
            likesCount = 0,
            commentsCount = 0,
            sharesCount = 0,
            isLikedByMe = false,
            isSavedByMe = false,
            filterName = filterName,
            tags = tags,
            createdAt = System.currentTimeMillis()
        )
        shortVideoDao.insertShortVideo(short)
        return short
    }

    // --- BEREAL DUAL-CAMERA DAILY POSTS ---
    fun getAllBeRealPosts(): Flow<List<BeRealPostEntity>> = beRealDao.getAllBeRealPosts()
    fun getBeRealPostsByUser(userId: String): Flow<List<BeRealPostEntity>> = beRealDao.getPostsByUser(userId)

    suspend fun reactBeReal(postId: String, emoji: String) {
        val post = beRealDao.getPostById(postId) ?: return
        beRealDao.updatePost(post.copy(realMojisJson = "{\"$emoji\": 1}"))
    }

    suspend fun postBeReal(
        primaryImageUrl: String,
        secondaryImageUrl: String,
        caption: String,
        filterApplied: String = "BeReal Raw Dual",
        locationName: String = "Paris, France",
        takenLateSeconds: Int = 0
    ): BeRealPostEntity {
        val myId = _currentUserId.value
        val post = BeRealPostEntity(
            id = "bereal_" + UUID.randomUUID().toString().take(8),
            userId = myId,
            primaryImageUrl = primaryImageUrl,
            secondaryImageUrl = secondaryImageUrl,
            caption = caption,
            takenLateSeconds = takenLateSeconds,
            locationName = locationName,
            realMojisJson = "{\"🔥\": 1, \"😍\": 1}",
            filterApplied = filterApplied,
            createdAt = System.currentTimeMillis()
        )
        beRealDao.insertPost(post)
        return post
    }

    // --- APP UPDATES & CHANGELOG ---
    fun getAllAppUpdates(): Flow<List<AppUpdateEntity>> = appUpdateDao.getAllUpdates()

    // --- TELEGRAM BROADCAST CHANNELS ---
    fun getAllBroadcastChannels(): Flow<List<BroadcastChannelEntity>> = broadcastChannelDao.getAllChannels()
    fun getJoinedBroadcastChannels(): Flow<List<BroadcastChannelEntity>> = broadcastChannelDao.getJoinedChannels()

    suspend fun toggleJoinBroadcastChannel(channelId: String) {
        val channel = broadcastChannelDao.getChannelById(channelId) ?: return
        val newJoined = !channel.isJoined
        val delta = if (newJoined) 1 else -1
        broadcastChannelDao.toggleJoin(channelId, newJoined, delta)
    }

    // --- ADMIN DASHBOARD ---
    fun getAllReports(): Flow<List<ReportEntity>> = reportDao.getAllReports()
    suspend fun resolveReport(reportId: String, status: ReportStatus, moderatorNotes: String) {
        val rep = reportDao.getAllReports().first().find { it.id == reportId } ?: return
        reportDao.updateReport(rep.copy(status = status, moderatorNotes = moderatorNotes))
    }
    suspend fun suspendUser(userId: String, isSuspended: Boolean, isBanned: Boolean) {
        userDao.setModerationStatus(userId, isBanned, isSuspended)
    }
}

