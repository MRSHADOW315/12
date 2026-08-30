package com.example.service.privacy

import com.example.data.local.entities.BlockEntity
import com.example.data.local.entities.FollowEntity
import com.example.data.local.entities.FriendshipEntity
import com.example.data.local.entities.LocationPermissionEntity
import com.example.data.local.entities.RelationshipEntity
import com.example.data.local.entities.StoryEntity
import com.example.data.local.entities.UserEntity
import com.example.data.model.LocationVisibility
import com.example.data.model.PrecisionLevel
import com.example.data.model.RelationshipStatus
import com.example.data.model.RelationshipVisibility
import com.example.data.model.StoryVisibility
import kotlin.math.round

data class EffectiveLocation(
    val hasPermission: Boolean,
    val latitude: Double?,
    val longitude: Double?,
    val country: String,
    val countryCode: String,
    val city: String,
    val precision: PrecisionLevel,
    val isGhostMode: Boolean,
    val isBroadCountryOnly: Boolean
)

object PrivacyAuthorizationEngine {

    /**
     * Centralized authorization for viewing location.
     * Implements the core concept: Location vs Relationship decoupling.
     */
    fun evaluateLocationAccess(
        viewerId: String,
        targetUser: UserEntity,
        targetPerm: LocationPermissionEntity?,
        activeRelationshipsBetween: List<RelationshipEntity>,
        activeFriendship: FriendshipEntity?,
        isFollowing: Boolean,
        isBlocked: Boolean
    ): EffectiveLocation {
        val perm = targetPerm ?: LocationPermissionEntity(userId = targetUser.id)
        val isSelf = viewerId == targetUser.id
        val ghostModeActive = targetUser.isGhostMode || perm.ghostMode

        // If self, can always see own location settings
        if (isSelf) {
            return EffectiveLocation(
                hasPermission = true,
                latitude = perm.latitude,
                longitude = perm.longitude,
                country = targetUser.country,
                countryCode = targetUser.countryCode,
                city = targetUser.city,
                precision = perm.precision,
                isGhostMode = ghostModeActive,
                isBroadCountryOnly = false
            )
        }

        // If blocked or target is suspended/banned, strictly no access
        if (isBlocked || targetUser.isBanned || targetUser.isSuspended) {
            return EffectiveLocation(
                hasPermission = false,
                latitude = null,
                longitude = null,
                country = targetUser.country,
                countryCode = targetUser.countryCode,
                city = "",
                precision = PrecisionLevel.COUNTRY,
                isGhostMode = true,
                isBroadCountryOnly = true
            )
        }

        // If Ghost Mode is active or visibility is NOBODY / ONLY_ME
        if (ghostModeActive || perm.visibility == LocationVisibility.NOBODY || perm.visibility == LocationVisibility.ONLY_ME) {
            return EffectiveLocation(
                hasPermission = false,
                latitude = null,
                longitude = null,
                country = targetUser.country,
                countryCode = targetUser.countryCode,
                city = "",
                precision = PrecisionLevel.COUNTRY,
                isGhostMode = ghostModeActive,
                isBroadCountryOnly = true
            )
        }

        // Check social visibility rules
        val isAllowed = when (perm.visibility) {
            LocationVisibility.EVERYONE -> true
            LocationVisibility.FOLLOWERS -> isFollowing
            LocationVisibility.FRIENDS -> activeFriendship?.status == "ACTIVE"
            LocationVisibility.RELATIONSHIPS -> activeRelationshipsBetween.any { it.status == RelationshipStatus.ACTIVE }
            LocationVisibility.NOBODY,
            LocationVisibility.ONLY_ME -> false
        }

        if (!isAllowed) {
            return EffectiveLocation(
                hasPermission = false,
                latitude = null,
                longitude = null,
                country = targetUser.country,
                countryCode = targetUser.countryCode,
                city = "",
                precision = PrecisionLevel.COUNTRY,
                isGhostMode = false,
                isBroadCountryOnly = true
            )
        }

        // User is permitted: Apply Precision Filter
        val (lat, lng) = maskCoordinates(perm.latitude, perm.longitude, perm.precision)

        return EffectiveLocation(
            hasPermission = true,
            latitude = lat,
            longitude = lng,
            country = targetUser.country,
            countryCode = targetUser.countryCode,
            city = if (perm.precision == PrecisionLevel.COUNTRY) "" else targetUser.city,
            precision = perm.precision,
            isGhostMode = false,
            isBroadCountryOnly = perm.precision == PrecisionLevel.COUNTRY
        )
    }

    private fun maskCoordinates(lat: Double?, lng: Double?, precision: PrecisionLevel): Pair<Double?, Double?> {
        if (lat == null || lng == null) return null to null
        return when (precision) {
            PrecisionLevel.EXACT -> lat to lng
            PrecisionLevel.APPROXIMATE -> {
                // Round to ~1 decimal place (~10km area)
                val maskedLat = round(lat * 10.0) / 10.0 + 0.015
                val maskedLng = round(lng * 10.0) / 10.0 + 0.015
                maskedLat to maskedLng
            }
            PrecisionLevel.CITY -> {
                val maskedLat = round(lat * 2.0) / 2.0
                val maskedLng = round(lng * 2.0) / 2.0
                maskedLat to maskedLng
            }
            PrecisionLevel.COUNTRY -> null to null
        }
    }

    /**
     * Relationship Visibility Authorization.
     * Decoupled from location: Relationship line can be GLOBAL even if location is NOBODY!
     */
    fun canViewRelationship(
        viewerId: String,
        relationship: RelationshipEntity,
        isBlocked: Boolean,
        isFriendWithA: Boolean,
        isFriendWithB: Boolean,
        isFollowerOfA: Boolean,
        isFollowerOfB: Boolean,
        hasRelationshipWithA: Boolean,
        hasRelationshipWithB: Boolean
    ): Boolean {
        if (isBlocked) return false

        val isParticipant = viewerId == relationship.userAId || viewerId == relationship.userBId
        if (isParticipant) return true

        // Only ACTIVE relationships are visible to 3rd parties
        if (relationship.status != RelationshipStatus.ACTIVE) return false

        return when (relationship.visibility) {
            RelationshipVisibility.GLOBAL -> true
            RelationshipVisibility.FRIENDS -> isFriendWithA || isFriendWithB
            RelationshipVisibility.FOLLOWERS -> isFollowerOfA || isFollowerOfB
            RelationshipVisibility.RELATIONSHIPS -> hasRelationshipWithA || hasRelationshipWithB
            RelationshipVisibility.PRIVATE -> false
        }
    }

    fun canMessage(viewerId: String, targetUserId: String, isBlocked: Boolean): Boolean {
        if (viewerId == targetUserId) return true
        return !isBlocked
    }

    fun canViewStory(
        viewerId: String,
        story: StoryEntity,
        isBlocked: Boolean,
        isFriend: Boolean,
        isFollowing: Boolean
    ): Boolean {
        if (isBlocked) return false
        if (viewerId == story.userId) return true

        return when (story.visibility) {
            StoryVisibility.EVERYONE -> true
            StoryVisibility.FOLLOWERS -> isFollowing
            StoryVisibility.FRIENDS -> isFriend
            StoryVisibility.SELECTED_USERS,
            StoryVisibility.PRIVATE -> false
        }
    }
}
