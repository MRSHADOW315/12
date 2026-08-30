package com.example.data.model

enum class RelationshipType(val label: String, val colorHex: Long, val symbol: String) {
    ROMANTIC("Romantic", 0xFFFF3B5C, "🔴"),
    FRIENDSHIP("Friendship", 0xFF3B82F6, "🔵"),
    FAMILY("Family", 0xFF10B981, "🟢")
}

enum class RelationshipStatus {
    NONE,
    REQUESTED,
    PENDING,
    ACTIVE,
    REJECTED,
    ENDED,
    BLOCKED
}

enum class RelationshipVisibility(val label: String) {
    PRIVATE("Only Us (Private)"),
    FRIENDS("Friends Only"),
    FOLLOWERS("Followers"),
    RELATIONSHIPS("Relationships Only"),
    GLOBAL("Global (Everyone)")
}

enum class LocationVisibility(val label: String) {
    NOBODY("Nobody (Hidden)"),
    ONLY_ME("Only Me"),
    FOLLOWERS("Followers Only"),
    FRIENDS("Friends Only"),
    RELATIONSHIPS("Relationship Partners Only"),
    EVERYONE("Everyone (Public)")
}

enum class PrecisionLevel(val label: String) {
    EXACT("Exact GPS Coordinates"),
    APPROXIMATE("Approximate Area (~5km)"),
    CITY("City Level Only"),
    COUNTRY("Country Level Only")
}

enum class UserRole {
    USER,
    MODERATOR,
    ADMIN,
    SUPER_ADMIN
}

enum class MessageType {
    TEXT,
    IMAGE,
    VIDEO,
    AUDIO
}

enum class StoryVisibility(val label: String) {
    EVERYONE("Everyone"),
    FOLLOWERS("Followers"),
    FRIENDS("Friends"),
    SELECTED_USERS("Selected Users"),
    PRIVATE("Private")
}

enum class ReportTargetType {
    USER,
    MESSAGE,
    STORY,
    PROFILE,
    RELATIONSHIP_ABUSE
}

enum class ReportReason(val label: String) {
    SPAM("Spam or Advertising"),
    HARASSMENT("Harassment or Bullying"),
    IMPERSONATION("Impersonation"),
    FRAUD("Fraud or Scam"),
    SEXUAL_EXPLOITATION("Inappropriate or Sensitive Content"),
    THREATS("Threats or Violence"),
    HATE("Hate Speech"),
    ILLEGAL_CONTENT("Illegal Content"),
    RELATIONSHIP_COERCION("Unwanted / Coerced Relationship"),
    OTHER("Other")
}

enum class ReportStatus {
    PENDING,
    REVIEWED,
    RESOLVED,
    DISMISSED
}

enum class NotificationType {
    MESSAGE,
    FOLLOW,
    FRIEND_REQUEST,
    FRIEND_ACCEPTED,
    RELATIONSHIP_REQUEST,
    RELATIONSHIP_ACCEPTED,
    RELATIONSHIP_REJECTED,
    STORY_REACTION,
    SYSTEM
}

enum class MapMode {
    WORLD,
    COUNTRY,
    CITY,
    NEARBY,
    MY_NETWORK,
    RELATIONSHIP_PATH
}
