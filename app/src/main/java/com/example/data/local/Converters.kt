package com.example.data.local

import androidx.room.TypeConverter
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

class Converters {
    @TypeConverter
    fun fromRelationshipType(value: RelationshipType): String = value.name
    @TypeConverter
    fun toRelationshipType(value: String): RelationshipType = RelationshipType.valueOf(value)

    @TypeConverter
    fun fromRelationshipStatus(value: RelationshipStatus): String = value.name
    @TypeConverter
    fun toRelationshipStatus(value: String): RelationshipStatus = RelationshipStatus.valueOf(value)

    @TypeConverter
    fun fromRelationshipVisibility(value: RelationshipVisibility): String = value.name
    @TypeConverter
    fun toRelationshipVisibility(value: String): RelationshipVisibility = RelationshipVisibility.valueOf(value)

    @TypeConverter
    fun fromLocationVisibility(value: LocationVisibility): String = value.name
    @TypeConverter
    fun toLocationVisibility(value: String): LocationVisibility = LocationVisibility.valueOf(value)

    @TypeConverter
    fun fromPrecisionLevel(value: PrecisionLevel): String = value.name
    @TypeConverter
    fun toPrecisionLevel(value: String): PrecisionLevel = PrecisionLevel.valueOf(value)

    @TypeConverter
    fun fromUserRole(value: UserRole): String = value.name
    @TypeConverter
    fun toUserRole(value: String): UserRole = UserRole.valueOf(value)

    @TypeConverter
    fun fromMessageType(value: MessageType): String = value.name
    @TypeConverter
    fun toMessageType(value: String): MessageType = MessageType.valueOf(value)

    @TypeConverter
    fun fromStoryVisibility(value: StoryVisibility): String = value.name
    @TypeConverter
    fun toStoryVisibility(value: String): StoryVisibility = StoryVisibility.valueOf(value)

    @TypeConverter
    fun fromReportTargetType(value: ReportTargetType): String = value.name
    @TypeConverter
    fun toReportTargetType(value: String): ReportTargetType = ReportTargetType.valueOf(value)

    @TypeConverter
    fun fromReportReason(value: ReportReason): String = value.name
    @TypeConverter
    fun toReportReason(value: String): ReportReason = ReportReason.valueOf(value)

    @TypeConverter
    fun fromReportStatus(value: ReportStatus): String = value.name
    @TypeConverter
    fun toReportStatus(value: String): ReportStatus = ReportStatus.valueOf(value)

    @TypeConverter
    fun fromNotificationType(value: NotificationType): String = value.name
    @TypeConverter
    fun toNotificationType(value: String): NotificationType = NotificationType.valueOf(value)
}
