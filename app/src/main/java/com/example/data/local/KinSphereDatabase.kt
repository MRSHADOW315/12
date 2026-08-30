package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.data.local.dao.AppUpdateDao
import com.example.data.local.dao.BeRealDao
import com.example.data.local.dao.BlockDao
import com.example.data.local.dao.BroadcastChannelDao
import com.example.data.local.dao.ChatDao
import com.example.data.local.dao.CommunityEventDao
import com.example.data.local.dao.FollowDao
import com.example.data.local.dao.FriendshipDao
import com.example.data.local.dao.LiveStreamDao
import com.example.data.local.dao.LocationDao
import com.example.data.local.dao.NotificationDao
import com.example.data.local.dao.RelationshipDao
import com.example.data.local.dao.ReportDao
import com.example.data.local.dao.ShortVideoDao
import com.example.data.local.dao.StoryDao
import com.example.data.local.dao.StreakDao
import com.example.data.local.dao.UserDao
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

@Database(
    entities = [
        UserEntity::class,
        LocationPermissionEntity::class,
        RelationshipEntity::class,
        FriendshipEntity::class,
        FollowEntity::class,
        ConversationEntity::class,
        ConversationMemberEntity::class,
        MessageEntity::class,
        StoryEntity::class,
        StoryViewEntity::class,
        StreakEntity::class,
        BlockEntity::class,
        ReportEntity::class,
        NotificationEntity::class,
        CommunityEventEntity::class,
        LiveStreamEntity::class,
        ShortVideoEntity::class,
        ShortCommentEntity::class,
        BeRealPostEntity::class,
        AppUpdateEntity::class,
        BroadcastChannelEntity::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class KinSphereDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun locationDao(): LocationDao
    abstract fun relationshipDao(): RelationshipDao
    abstract fun friendshipDao(): FriendshipDao
    abstract fun followDao(): FollowDao
    abstract fun chatDao(): ChatDao
    abstract fun streakDao(): StreakDao
    abstract fun storyDao(): StoryDao
    abstract fun blockDao(): BlockDao
    abstract fun reportDao(): ReportDao
    abstract fun notificationDao(): NotificationDao
    abstract fun communityEventDao(): CommunityEventDao
    abstract fun liveStreamDao(): LiveStreamDao
    abstract fun shortVideoDao(): ShortVideoDao
    abstract fun beRealDao(): BeRealDao
    abstract fun appUpdateDao(): AppUpdateDao
    abstract fun broadcastChannelDao(): BroadcastChannelDao

    companion object {
        @Volatile
        private var INSTANCE: KinSphereDatabase? = null

        fun getDatabase(context: Context): KinSphereDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    KinSphereDatabase::class.java,
                    "metou_social.db"
                ).fallbackToDestructiveMigration(true)
                 .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
