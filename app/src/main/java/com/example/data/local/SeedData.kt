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

object SeedData {

    val users = listOf(
        UserEntity(
            id = "user_ahmed",
            username = "ahmed",
            displayName = "Ahmed Al-Mansoor",
            email = "ahmed@example.com",
            passwordHash = "password123",
            avatarUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=150",
            coverUrl = "https://images.unsplash.com/photo-1506744038136-46273834b3fb?w=600",
            bio = "Geographic explorer & tech builder. Living in Baghdad, connected globally.",
            age = 26,
            country = "Iraq",
            countryCode = "IQ",
            city = "Baghdad",
            interests = "Technology, Photography, Travel, Coffee",
            languages = "Arabic, English",
            isGhostMode = true,
            role = UserRole.USER
        ),
        UserEntity(
            id = "user_sara",
            username = "sara",
            displayName = "Sara Dubois",
            email = "sara@example.com",
            passwordHash = "password123",
            avatarUrl = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=150",
            coverUrl = "https://images.unsplash.com/photo-1511739001486-6bfe10ce785f?w=600",
            bio = "Architect & designer in Paris. Cherishing deep human relationships.",
            age = 25,
            country = "France",
            countryCode = "FR",
            city = "Paris",
            interests = "Design, Art, Cinema, Architecture",
            languages = "French, English",
            isGhostMode = true,
            role = UserRole.USER
        ),
        UserEntity(
            id = "user_mohammed",
            username = "mohammed",
            displayName = "Mohammed Khalil",
            email = "mohammed@example.com",
            passwordHash = "password123",
            avatarUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=150",
            coverUrl = "https://images.unsplash.com/photo-1512453979798-5ea266f8880c?w=600",
            bio = "Aviation & fintech engineer in Dubai. Lifelong learner.",
            age = 28,
            country = "United Arab Emirates",
            countryCode = "AE",
            city = "Dubai",
            interests = "Aviation, Skydiving, Coding, Finance",
            languages = "Arabic, English",
            isGhostMode = false,
            role = UserRole.USER
        ),
        UserEntity(
            id = "user_ali",
            username = "ali",
            displayName = "Ali Hassan",
            email = "ali@example.com",
            passwordHash = "password123",
            avatarUrl = "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=150",
            coverUrl = "https://images.unsplash.com/photo-1539650116574-8efeb43e2750?w=600",
            bio = "Civil engineer & historian in Cairo. Family first.",
            age = 31,
            country = "Egypt",
            countryCode = "EG",
            city = "Cairo",
            interests = "History, Sailing, Football, Engineering",
            languages = "Arabic, English",
            isGhostMode = false,
            role = UserRole.USER
        ),
        UserEntity(
            id = "user_elena",
            username = "elena",
            displayName = "Elena Rossi",
            email = "elena@example.com",
            passwordHash = "password123",
            avatarUrl = "https://images.unsplash.com/photo-1517841905240-472988babdf9?w=150",
            coverUrl = "https://images.unsplash.com/photo-1552832230-c0197dd311b5?w=600",
            bio = "Music producer & travel blogger based in Rome.",
            age = 24,
            country = "Italy",
            countryCode = "IT",
            city = "Rome",
            interests = "Music, Culinary, Travel, Nature",
            languages = "Italian, English, Spanish",
            isGhostMode = false,
            role = UserRole.USER
        ),
        UserEntity(
            id = "user_kenji",
            username = "kenji",
            displayName = "Kenji Sato",
            email = "kenji@example.com",
            passwordHash = "password123",
            avatarUrl = "https://images.unsplash.com/photo-1522075469751-3a6694fb2f61?w=150",
            coverUrl = "https://images.unsplash.com/photo-1503899036084-c55cdd92da26?w=600",
            bio = "Robotics researcher in Tokyo. Exploring decentralized networks.",
            age = 29,
            country = "Japan",
            countryCode = "JP",
            city = "Tokyo",
            interests = "Robotics, AI, Gaming, Matcha",
            languages = "Japanese, English",
            isGhostMode = false,
            role = UserRole.USER
        ),
        UserEntity(
            id = "user_admin",
            username = "admin",
            displayName = "KinSphere Trust & Safety",
            email = "admin@kinsphere.app",
            passwordHash = "admin123",
            avatarUrl = "",
            bio = "System Security, Compliance & Moderation Center.",
            age = 35,
            country = "Switzerland",
            countryCode = "CH",
            city = "Geneva",
            role = UserRole.SUPER_ADMIN
        )
    )

    val locationPermissions = listOf(
        LocationPermissionEntity(
            userId = "user_ahmed",
            visibility = LocationVisibility.NOBODY,
            precision = PrecisionLevel.APPROXIMATE,
            ghostMode = true,
            latitude = 33.3152,
            longitude = 44.3661,
            country = "Iraq",
            countryCode = "IQ",
            city = "Baghdad"
        ),
        LocationPermissionEntity(
            userId = "user_sara",
            visibility = LocationVisibility.NOBODY,
            precision = PrecisionLevel.APPROXIMATE,
            ghostMode = true,
            latitude = 48.8566,
            longitude = 2.3522,
            country = "France",
            countryCode = "FR",
            city = "Paris"
        ),
        LocationPermissionEntity(
            userId = "user_mohammed",
            visibility = LocationVisibility.FRIENDS,
            precision = PrecisionLevel.EXACT,
            ghostMode = false,
            latitude = 25.2048,
            longitude = 55.2708,
            country = "United Arab Emirates",
            countryCode = "AE",
            city = "Dubai"
        ),
        LocationPermissionEntity(
            userId = "user_ali",
            visibility = LocationVisibility.RELATIONSHIPS,
            precision = PrecisionLevel.CITY,
            ghostMode = false,
            latitude = 30.0444,
            longitude = 31.2357,
            country = "Egypt",
            countryCode = "EG",
            city = "Cairo"
        ),
        LocationPermissionEntity(
            userId = "user_elena",
            visibility = LocationVisibility.EVERYONE,
            precision = PrecisionLevel.EXACT,
            ghostMode = false,
            latitude = 41.9028,
            longitude = 12.4964,
            country = "Italy",
            countryCode = "IT",
            city = "Rome"
        ),
        LocationPermissionEntity(
            userId = "user_kenji",
            visibility = LocationVisibility.EVERYONE,
            precision = PrecisionLevel.APPROXIMATE,
            ghostMode = false,
            latitude = 35.6762,
            longitude = 139.6503,
            country = "Japan",
            countryCode = "JP",
            city = "Tokyo"
        )
    )

    val relationships = listOf(
        // Romantic line between Ahmed (Iraq) and Sara (France) with GLOBAL visibility!
        RelationshipEntity(
            id = "rel_ahmed_sara",
            userAId = "user_ahmed",
            userBId = "user_sara",
            type = RelationshipType.ROMANTIC,
            status = RelationshipStatus.ACTIVE,
            visibility = RelationshipVisibility.GLOBAL,
            requestedBy = "user_ahmed"
        ),
        // Friendship line between Ahmed (Iraq) and Mohammed (UAE)
        RelationshipEntity(
            id = "rel_ahmed_mohammed",
            userAId = "user_ahmed",
            userBId = "user_mohammed",
            type = RelationshipType.FRIENDSHIP,
            status = RelationshipStatus.ACTIVE,
            visibility = RelationshipVisibility.GLOBAL,
            requestedBy = "user_ahmed"
        ),
        // Family line between Mohammed (UAE) and Ali (Egypt)
        RelationshipEntity(
            id = "rel_mohammed_ali",
            userAId = "user_mohammed",
            userBId = "user_ali",
            type = RelationshipType.FAMILY,
            status = RelationshipStatus.ACTIVE,
            visibility = RelationshipVisibility.GLOBAL,
            requestedBy = "user_mohammed"
        ),
        // Friendship line between Sara (France) and Elena (Italy)
        RelationshipEntity(
            id = "rel_sara_elena",
            userAId = "user_sara",
            userBId = "user_elena",
            type = RelationshipType.FRIENDSHIP,
            status = RelationshipStatus.ACTIVE,
            visibility = RelationshipVisibility.GLOBAL,
            requestedBy = "user_sara"
        ),
        // Friendship line between Elena (Italy) and Kenji (Japan)
        RelationshipEntity(
            id = "rel_elena_kenji",
            userAId = "user_elena",
            userBId = "user_kenji",
            type = RelationshipType.FRIENDSHIP,
            status = RelationshipStatus.ACTIVE,
            visibility = RelationshipVisibility.GLOBAL,
            requestedBy = "user_elena"
        )
    )

    val friendships = listOf(
        FriendshipEntity("f1", "user_ahmed", "user_sara", "ACTIVE", "user_ahmed"),
        FriendshipEntity("f2", "user_ahmed", "user_mohammed", "ACTIVE", "user_ahmed"),
        FriendshipEntity("f3", "user_sara", "user_elena", "ACTIVE", "user_sara"),
        FriendshipEntity("f4", "user_mohammed", "user_ali", "ACTIVE", "user_mohammed"),
        FriendshipEntity("f5", "user_elena", "user_kenji", "ACTIVE", "user_elena")
    )

    val follows = listOf(
        FollowEntity("fol1", "user_ahmed", "user_sara"),
        FollowEntity("fol2", "user_sara", "user_ahmed"),
        FollowEntity("fol3", "user_ahmed", "user_mohammed"),
        FollowEntity("fol4", "user_mohammed", "user_ahmed"),
        FollowEntity("fol5", "user_sara", "user_elena"),
        FollowEntity("fol6", "user_kenji", "user_ahmed"),
        FollowEntity("fol7", "user_ahmed", "user_elena"),
        FollowEntity("fol8", "user_ahmed", "user_kenji"),
        FollowEntity("fol9", "user_ali", "user_ahmed"),
        FollowEntity("fol10", "user_lucas", "user_ahmed")
    )

    val conversations = listOf(
        ConversationEntity(
            id = "conv_ahmed_sara",
            title = "Sara Dubois",
            isGroup = false,
            lastMessageText = "The global relationship line is showing up perfectly across Iraq and France ❤️",
            lastMessageTime = System.currentTimeMillis() - 1000 * 60 * 5
        ),
        ConversationEntity(
            id = "conv_ahmed_mohammed",
            title = "Mohammed Khalil",
            isGroup = false,
            lastMessageText = "Let me know when you test the connection pathfinder across the network!",
            lastMessageTime = System.currentTimeMillis() - 1000 * 60 * 30
        ),
        ConversationEntity(
            id = "conv_global_explorers",
            title = "Global KinSphere Circle",
            isGroup = true,
            lastMessageText = "Welcome everyone to the privacy-first global social network.",
            lastMessageTime = System.currentTimeMillis() - 1000 * 60 * 120
        )
    )

    val conversationMembers = listOf(
        ConversationMemberEntity("conv_ahmed_sara", "user_ahmed"),
        ConversationMemberEntity("conv_ahmed_sara", "user_sara"),
        ConversationMemberEntity("conv_ahmed_mohammed", "user_ahmed"),
        ConversationMemberEntity("conv_ahmed_mohammed", "user_mohammed"),
        ConversationMemberEntity("conv_global_explorers", "user_ahmed"),
        ConversationMemberEntity("conv_global_explorers", "user_sara"),
        ConversationMemberEntity("conv_global_explorers", "user_mohammed"),
        ConversationMemberEntity("conv_global_explorers", "user_ali"),
        ConversationMemberEntity("conv_global_explorers", "user_elena"),
        ConversationMemberEntity("conv_global_explorers", "user_kenji")
    )

    val messages = listOf(
        MessageEntity(
            id = "msg1",
            conversationId = "conv_ahmed_sara",
            senderId = "user_ahmed",
            text = "Bonjour Sara! I enabled Ghost Mode so my exact GPS coordinates remain 100% private.",
            createdAt = System.currentTimeMillis() - 1000 * 60 * 20
        ),
        MessageEntity(
            id = "msg2",
            conversationId = "conv_ahmed_sara",
            senderId = "user_sara",
            text = "Parfait! Our romantic relationship line is still visible on the World Map while our exact locations stay hidden.",
            createdAt = System.currentTimeMillis() - 1000 * 60 * 15
        ),
        MessageEntity(
            id = "msg3",
            conversationId = "conv_ahmed_sara",
            senderId = "user_sara",
            text = "The global relationship line is showing up perfectly across Iraq and France ❤️",
            createdAt = System.currentTimeMillis() - 1000 * 60 * 5
        )
    )

    val stories = listOf(
        StoryEntity(
            id = "story_sara_1",
            userId = "user_sara",
            mediaUrl = "https://images.unsplash.com/photo-1502602898657-3e91760cbb34?w=600",
            textCaption = "Sunset by the Seine 🌇",
            visibility = StoryVisibility.EVERYONE,
            createdAt = System.currentTimeMillis() - 1000 * 60 * 60 * 2,
            viewsCount = 18
        ),
        StoryEntity(
            id = "story_ahmed_1",
            userId = "user_ahmed",
            mediaUrl = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=600",
            textCaption = "Exploring historic architecture under starry skies 🌌",
            visibility = StoryVisibility.EVERYONE,
            createdAt = System.currentTimeMillis() - 1000 * 60 * 60 * 4,
            viewsCount = 34
        ),
        StoryEntity(
            id = "story_mohammed_1",
            userId = "user_mohammed",
            mediaUrl = "https://images.unsplash.com/photo-1512453979798-5ea266f8880c?w=600",
            textCaption = "High above Dubai Marina ✈️",
            visibility = StoryVisibility.FRIENDS,
            createdAt = System.currentTimeMillis() - 1000 * 60 * 60 * 8,
            viewsCount = 22
        ),
        StoryEntity(
            id = "story_elena_1",
            userId = "user_elena",
            mediaUrl = "https://images.unsplash.com/photo-1552832230-c0197dd311b5?w=600",
            textCaption = "Acoustic recording session in Rome 🎵",
            visibility = StoryVisibility.EVERYONE,
            createdAt = System.currentTimeMillis() - 1000 * 60 * 60 * 10,
            viewsCount = 41
        )
    )

    val notifications = listOf(
        NotificationEntity(
            id = "notif_1",
            userId = "user_ahmed",
            type = NotificationType.RELATIONSHIP_ACCEPTED,
            actorId = "user_sara",
            actorName = "Sara Dubois",
            title = "Romantic Relationship Established",
            body = "Sara Dubois accepted your romantic relationship request. Your relationship line is now live on the Global Map!",
            isRead = false,
            targetId = "rel_ahmed_sara"
        ),
        NotificationEntity(
            id = "notif_2",
            userId = "user_ahmed",
            type = NotificationType.FRIEND_ACCEPTED,
            actorId = "user_mohammed",
            actorName = "Mohammed Khalil",
            title = "Friendship Connected",
            body = "Mohammed Khalil joined your circle.",
            isRead = true,
            targetId = "user_mohammed"
        )
    )

    val communityEvents = listOf(
        CommunityEventEntity(
            id = "evt_1",
            title = "Global Creators Summit",
            description = "Connecting digital nomads, artists, and network researchers globally.",
            category = "Technology & Social",
            country = "France",
            city = "Paris",
            latitude = 48.8566,
            longitude = 2.3522,
            eventDate = "October 14, 2026",
            creatorId = "user_sara",
            attendeesCount = 142
        ),
        CommunityEventEntity(
            id = "evt_2",
            title = "Ancient Civilization & Heritage Photowalk",
            description = "A cultural exploration of historical landmarks and relationship storytelling.",
            category = "Culture & Photography",
            country = "Iraq",
            city = "Baghdad",
            latitude = 33.3152,
            longitude = 44.3661,
            eventDate = "November 5, 2026",
            creatorId = "user_ahmed",
            attendeesCount = 88
        ),
        CommunityEventEntity(
            id = "evt_3",
            title = "Future Mobility & AI Expo",
            description = "Discussions on decentralized technologies and autonomous networks.",
            category = "Engineering",
            country = "United Arab Emirates",
            city = "Dubai",
            latitude = 25.2048,
            longitude = 55.2708,
            eventDate = "December 1, 2026",
            creatorId = "user_mohammed",
            attendeesCount = 310
        )
    )

    val reports = listOf(
        ReportEntity(
            id = "rep_1",
            reporterId = "user_kenji",
            targetType = ReportTargetType.MESSAGE,
            targetId = "sample_msg_unsolicited",
            reason = ReportReason.SPAM,
            details = "Unsolicited promotional bot link in open group.",
            status = ReportStatus.PENDING
        )
    )

    val streaks = listOf(
        StreakEntity(
            id = "streak_user_ahmed_user_sara",
            userAId = "user_ahmed",
            userBId = "user_sara",
            streakCount = 42,
            bestStreakCount = 42,
            lastInteractionTimestamp = System.currentTimeMillis() - (2 * 60 * 60 * 1000L), // 2 hours ago
            lastSenderId = "user_sara",
            userAInteractedToday = true,
            userBInteractedToday = true,
            expiresAt = System.currentTimeMillis() + (22 * 60 * 60 * 1000L),
            isFrozen = false,
            freezeTokensLeft = 2,
            lastStreakDate = "2026-08-29"
        ),
        StreakEntity(
            id = "streak_user_ahmed_user_mohammed",
            userAId = "user_ahmed",
            userBId = "user_mohammed",
            streakCount = 15,
            bestStreakCount = 18,
            lastInteractionTimestamp = System.currentTimeMillis() - (16 * 60 * 60 * 1000L), // 16 hours ago
            lastSenderId = "user_ahmed",
            userAInteractedToday = true,
            userBInteractedToday = false,
            expiresAt = System.currentTimeMillis() + (8 * 60 * 60 * 1000L), // 8 hours left
            isFrozen = false,
            freezeTokensLeft = 1,
            lastStreakDate = "2026-08-29"
        ),
        StreakEntity(
            id = "streak_user_ahmed_user_ali",
            userAId = "user_ahmed",
            userBId = "user_ali",
            streakCount = 7,
            bestStreakCount = 10,
            lastInteractionTimestamp = System.currentTimeMillis() - (21 * 60 * 60 * 1000L), // 21 hours ago
            lastSenderId = "user_ali",
            userAInteractedToday = false,
            userBInteractedToday = true,
            expiresAt = System.currentTimeMillis() + (3 * 60 * 60 * 1000L), // 3 hours left (Expiring soon! ⏳)
            isFrozen = false,
            freezeTokensLeft = 2,
            lastStreakDate = "2026-08-28"
        ),
        StreakEntity(
            id = "streak_user_ahmed_user_elena",
            userAId = "user_ahmed",
            userBId = "user_elena",
            streakCount = 3,
            bestStreakCount = 3,
            lastInteractionTimestamp = System.currentTimeMillis() - (12 * 60 * 60 * 1000L),
            lastSenderId = "user_ahmed",
            userAInteractedToday = true,
            userBInteractedToday = false,
            expiresAt = System.currentTimeMillis() + (36 * 60 * 60 * 1000L),
            isFrozen = true, // Frozen streak ❄️
            freezeTokensLeft = 1,
            lastStreakDate = "2026-08-29"
        ),
        StreakEntity(
            id = "streak_user_mohammed_user_kenji",
            userAId = "user_mohammed",
            userBId = "user_kenji",
            streakCount = 12,
            bestStreakCount = 14,
            lastInteractionTimestamp = System.currentTimeMillis() - (5 * 60 * 60 * 1000L),
            lastSenderId = "user_kenji",
            userAInteractedToday = true,
            userBInteractedToday = true,
            expiresAt = System.currentTimeMillis() + (19 * 60 * 60 * 1000L),
            isFrozen = false,
            freezeTokensLeft = 2,
            lastStreakDate = "2026-08-29"
        )
    )

    val liveStreams = listOf(
        LiveStreamEntity(
            id = "live_stream_sara",
            hostUserId = "user_sara",
            title = "Paris Sunset DJ Set & City Q&A 🎧🇫🇷",
            category = "Music & Hangout",
            coverUrl = "https://images.unsplash.com/photo-1502602898657-3e91760cbb34?w=600",
            viewerCount = 1420,
            likesCount = 8940,
            isLive = true,
            startedAt = System.currentTimeMillis() - 1000 * 60 * 25,
            tags = "Paris, Sunset, HouseMusic, Travel"
        ),
        LiveStreamEntity(
            id = "live_stream_kenji",
            hostUserId = "user_kenji",
            title = "Night Walk in Shibuya & Cyberpunk Photography 📸⚡",
            category = "Creative & Photo",
            coverUrl = "https://images.unsplash.com/photo-1503899036084-c55cdd92da26?w=600",
            viewerCount = 2890,
            likesCount = 19450,
            isLive = true,
            startedAt = System.currentTimeMillis() - 1000 * 60 * 45,
            tags = "Tokyo, Neon, Cyberpunk, StreetArt"
        ),
        LiveStreamEntity(
            id = "live_stream_elena",
            hostUserId = "user_elena",
            title = "Authentic Roman Pasta Cooking Masterclass 🍝🇮🇹",
            category = "Culinary & Food",
            coverUrl = "https://images.unsplash.com/photo-1534447677768-be436bb09401?w=600",
            viewerCount = 890,
            likesCount = 4320,
            isLive = true,
            startedAt = System.currentTimeMillis() - 1000 * 60 * 10,
            tags = "Rome, Pasta, Cooking, Foodie"
        )
    )

    val shortVideos = listOf(
        ShortVideoEntity(
            id = "short_1",
            creatorId = "user_sara",
            videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
            thumbnailUrl = "https://images.unsplash.com/photo-1502602898657-3e91760cbb34?w=800",
            caption = "Golden hour reflecting on the Seine river in Paris ✨ Who wants to visit this autumn? 🍂 #paris #travel #goldenhour",
            soundTitle = "Midnight in Montmartre (Lofi Remix)",
            soundArtist = "Sara Vibes",
            likesCount = 24500,
            commentsCount = 382,
            sharesCount = 1204,
            isLikedByMe = true,
            isSavedByMe = true,
            filterName = "Golden Hour",
            tags = "#paris #travel #autumn"
        ),
        ShortVideoEntity(
            id = "short_2",
            creatorId = "user_kenji",
            videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4",
            thumbnailUrl = "https://images.unsplash.com/photo-1503899036084-c55cdd92da26?w=800",
            caption = "Rainy neon reflections in Shinjuku crossing 🌧️⚡ Shot on 35mm lens with Cyberpunk filter! #tokyo #neon #cyberpunk",
            soundTitle = "Tokyo Drift Synthwave 2026",
            soundArtist = "Kenji Beats",
            likesCount = 48900,
            commentsCount = 612,
            sharesCount = 3450,
            isLikedByMe = false,
            isSavedByMe = false,
            filterName = "Cyberpunk Neon",
            tags = "#tokyo #cinematic #night"
        ),
        ShortVideoEntity(
            id = "short_3",
            creatorId = "user_elena",
            videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4",
            thumbnailUrl = "https://images.unsplash.com/photo-1534447677768-be436bb09401?w=800",
            caption = "Making fresh handmade Tagliatelle in Rome with Nonna! 🍝 The secret is double 00 flour! #italianfood #pasta #rome",
            soundTitle = "Tarantella Napoletana Accordion",
            soundArtist = "Elena Rossi",
            likesCount = 15300,
            commentsCount = 189,
            sharesCount = 920,
            isLikedByMe = true,
            isSavedByMe = false,
            filterName = "Vintage 90s",
            tags = "#cooking #handmade #foodie"
        ),
        ShortVideoEntity(
            id = "short_4",
            creatorId = "user_ahmed",
            videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerJoyBlazes.mp4",
            thumbnailUrl = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=800",
            caption = "Ancient Tigris river sunset in Baghdad 🌇 Preserving history through modern lens! #baghdad #history #iraq",
            soundTitle = "Oud Melodies & Ambient Winds",
            soundArtist = "Ahmed Heritage",
            likesCount = 31200,
            commentsCount = 450,
            sharesCount = 1890,
            isLikedByMe = true,
            isSavedByMe = true,
            filterName = "Emerald Glow",
            tags = "#heritage #culture #iraq"
        )
    )

    val shortComments = listOf(
        ShortCommentEntity(
            id = "scomm_1",
            shortId = "short_1",
            userId = "user_ahmed",
            userName = "Ahmed Al-Mansoor",
            userAvatar = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=200",
            text = "The lighting in this clip is absolutely breathtaking! 🔥",
            likesCount = 42,
            createdAt = System.currentTimeMillis() - 1000 * 60 * 60
        ),
        ShortCommentEntity(
            id = "scomm_2",
            shortId = "short_1",
            userId = "user_elena",
            userName = "Elena Rossi",
            userAvatar = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=200",
            text = "Sara you need to take me there next month! ❤️✨",
            likesCount = 18,
            createdAt = System.currentTimeMillis() - 1000 * 60 * 45
        ),
        ShortCommentEntity(
            id = "scomm_3",
            shortId = "short_2",
            userId = "user_mohammed",
            userName = "Mohammed Khalil",
            userAvatar = "https://images.unsplash.com/photo-1570295999919-56ceb5ecca61?w=200",
            text = "Cyberpunk vibes are unreal here bro! Which camera rig? 🚀",
            likesCount = 29,
            createdAt = System.currentTimeMillis() - 1000 * 60 * 30
        )
    )

    val beRealPosts = listOf(
        BeRealPostEntity(
            id = "bereal_1",
            userId = "user_sara",
            primaryImageUrl = "https://images.unsplash.com/photo-1502602898657-3e91760cbb34?w=600",
            secondaryImageUrl = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=300",
            caption = "Caught in the coffee shop writing lyrics ☕✨",
            takenLateSeconds = 0, // On time!
            locationName = "Le Marais, Paris",
            realMojisJson = "{\"😍\": 14, \"🔥\": 8, \"😂\": 3, \"👍\": 19}",
            filterApplied = "BeReal Raw Dual",
            createdAt = System.currentTimeMillis() - 1000 * 60 * 75
        ),
        BeRealPostEntity(
            id = "bereal_2",
            userId = "user_kenji",
            primaryImageUrl = "https://images.unsplash.com/photo-1503899036084-c55cdd92da26?w=600",
            secondaryImageUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=300",
            caption = "Late night darkroom photo develop session 🎞️",
            takenLateSeconds = 180, // 3 mins late
            locationName = "Shibuya, Tokyo",
            realMojisJson = "{\"🔥\": 22, \"😮\": 7, \"⚡\": 12}",
            filterApplied = "Cyberpunk BeReal",
            createdAt = System.currentTimeMillis() - 1000 * 60 * 120
        ),
        BeRealPostEntity(
            id = "bereal_3",
            userId = "user_elena",
            primaryImageUrl = "https://images.unsplash.com/photo-1534447677768-be436bb09401?w=600",
            secondaryImageUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=300",
            caption = "Espresso break before dinner rush ☕🇮🇹",
            takenLateSeconds = 0,
            locationName = "Trastevere, Rome",
            realMojisJson = "{\"❤️\": 31, \"😍\": 15, \"👍\": 11}",
            filterApplied = "Vintage 90s Dual",
            createdAt = System.currentTimeMillis() - 1000 * 60 * 190
        )
    )

    val appUpdates = listOf(
        AppUpdateEntity(
            version = "v3.2.0 (Latest)",
            title = "All-In-One Social SuperApp Suite",
            releaseDate = "August 2026",
            highlightsJson = "[\"🔴 Live Stream Broadcasting & Interactive Viewer Hub with floating hearts and gift tipping\", \"🎬 Short Videos / Reels Vertical Feed with animated sound disc, double-tap heart likes, and comments modal\", \"📸 Pro Camera Studio with Real-time AR Color Filters, Video Recording, Timer, Speed controls\", \"👥 Follower & Following Social Graph Hub with search, one-tap follow/unfollow and mutual discovery\", \"⚠️ BeReal Dual-Camera Daily Capture mode with RealMoji reactions\", \"📢 Telegram Broadcast Channels & Voice Audio Lounges\", \"🔥 Snapchat-Style Streaks Engine with Flame badges & freeze tokens\"]",
            downloadSizeMb = 38.5,
            isInstalled = true,
            isCurrentVersion = true
        ),
        AppUpdateEntity(
            version = "v3.1.0",
            title = "Streaks Engine & Snap Delivery",
            releaseDate = "July 2026",
            highlightsJson = "[\"🔥 Bilateral streak counters with flame animation\", \"❄️ Freeze streak tokens and streak recovery engine\", \"⚡ Direct photo snaps with custom captions\"]",
            downloadSizeMb = 24.1,
            isInstalled = true,
            isCurrentVersion = false
        ),
        AppUpdateEntity(
            version = "v3.0.0",
            title = "Global KinSphere World Map & Trust-Safety Core",
            releaseDate = "June 2026",
            highlightsJson = "[\"🗺️ Interactive Arc GIS & Mercator World Map\", \"🛡️ Multi-tier Privacy Authorization Engine\", \"🚨 Trust & Safety reporting & moderation panel\"]",
            downloadSizeMb = 45.2,
            isInstalled = true,
            isCurrentVersion = false
        )
    )

    val broadcastChannels = listOf(
        BroadcastChannelEntity(
            id = "chan_official",
            title = "KinSphere Official 🚀",
            handle = "@kinsphere_app",
            description = "Official announcements, feature drops, community AMAs and release highlights.",
            avatarUrl = "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=200",
            subscribersCount = 145000,
            lastPostText = "🎉 Welcome to KinSphere v3.2! LIVE streams, Shorts Reels, AR Camera Studio, and BeReal dual feeds are now live!",
            lastPostTime = System.currentTimeMillis() - 1000 * 60 * 15,
            isJoined = true,
            verifiedBadge = true
        ),
        BroadcastChannelEntity(
            id = "chan_tech",
            title = "Tech & AI Daily ⚡",
            handle = "@tech_daily_ai",
            description = "Cutting-edge artificial intelligence, robotics, mobile dev & computing breakthroughs.",
            avatarUrl = "https://images.unsplash.com/photo-1518770660439-4636190af475?w=200",
            subscribersCount = 89200,
            lastPostText = "⚡ Next-gen generative models now support instant video stylization on on-device neural engines.",
            lastPostTime = System.currentTimeMillis() - 1000 * 60 * 80,
            isJoined = true,
            verifiedBadge = true
        ),
        BroadcastChannelEntity(
            id = "chan_photo",
            title = "Global Photography & Cinematic 📸",
            handle = "@cinematic_shots",
            description = "Curated street photography, golden hour captures and preset recipes from around the globe.",
            avatarUrl = "https://images.unsplash.com/photo-1503899036084-c55cdd92da26?w=200",
            subscribersCount = 62400,
            lastPostText = "📸 Tip: Shoot at f/1.8 with 1/125s shutter under rain reflections for natural cyberpunk bokeh!",
            lastPostTime = System.currentTimeMillis() - 1000 * 60 * 140,
            isJoined = false,
            verifiedBadge = true
        ),
        BroadcastChannelEntity(
            id = "chan_music",
            title = "Underground Sound & Lofi Lounge 🎧",
            handle = "@lofi_lounge",
            description = "Curated ambient tracks, synthwave beats, and bedroom producer releases.",
            avatarUrl = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=200",
            subscribersCount = 41300,
            lastPostText = "🎧 New weekly chillhop playlist is streaming now! Tune in on the Live Hub.",
            lastPostTime = System.currentTimeMillis() - 1000 * 60 * 200,
            isJoined = false,
            verifiedBadge = false
        )
    )

}
