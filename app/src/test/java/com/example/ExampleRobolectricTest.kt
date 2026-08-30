package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.entities.LocationPermissionEntity
import com.example.data.local.entities.RelationshipEntity
import com.example.data.local.entities.UserEntity
import com.example.data.model.LocationVisibility
import com.example.data.model.PrecisionLevel
import com.example.data.model.RelationshipStatus
import com.example.data.model.RelationshipType
import com.example.data.model.RelationshipVisibility
import com.example.service.graph.RelationshipGraphEngine
import com.example.service.privacy.PrivacyAuthorizationEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read app name string resource`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("METOU", appName)
    }

    @Test
    fun `test privacy engine hides coordinates when ghost mode is active`() {
        val ahmed = UserEntity(
            id = "user_ahmed",
            username = "ahmed",
            displayName = "Ahmed",
            email = "ahmed@example.com",
            passwordHash = "pass",
            country = "Iraq",
            countryCode = "IQ",
            city = "Baghdad",
            isGhostMode = true
        )
        val perm = LocationPermissionEntity(
            userId = "user_ahmed",
            visibility = LocationVisibility.NOBODY,
            precision = PrecisionLevel.EXACT,
            ghostMode = true,
            latitude = 33.3152,
            longitude = 44.3661
        )

        val effective = PrivacyAuthorizationEngine.evaluateLocationAccess(
            viewerId = "user_sara",
            targetUser = ahmed,
            targetPerm = perm,
            activeRelationshipsBetween = emptyList(),
            activeFriendship = null,
            isFollowing = false,
            isBlocked = false
        )

        assertFalse("Viewer should not have direct GPS permission", effective.hasPermission)
        assertNull("GPS latitude must be null to protect privacy", effective.latitude)
        assertNull("GPS longitude must be null to protect privacy", effective.longitude)
        assertTrue("Country fallback representation must remain", effective.isBroadCountryOnly)
        assertEquals("IQ", effective.countryCode)
    }

    @Test
    fun `test relationship visibility is decoupled from location`() {
        val rel = RelationshipEntity(
            id = "rel_1",
            userAId = "user_ahmed",
            userBId = "user_sara",
            type = RelationshipType.ROMANTIC,
            status = RelationshipStatus.ACTIVE,
            visibility = RelationshipVisibility.GLOBAL,
            requestedBy = "user_ahmed"
        )

        val canView = PrivacyAuthorizationEngine.canViewRelationship(
            viewerId = "user_third_party",
            relationship = rel,
            isBlocked = false,
            isFriendWithA = false,
            isFriendWithB = false,
            isFollowerOfA = false,
            isFollowerOfB = false,
            hasRelationshipWithA = false,
            hasRelationshipWithB = false
        )

        assertTrue("Global relationship line must be visible to public despite private locations", canView)
    }

    @Test
    fun `test graph engine multi-degree path finding`() {
        val u1 = UserEntity(id = "u1", username = "u1", displayName = "User 1", email = "u1@e.com", passwordHash = "p", country = "Iraq", countryCode = "IQ", city = "Baghdad")
        val u2 = UserEntity(id = "u2", username = "u2", displayName = "User 2", email = "u2@e.com", passwordHash = "p", country = "France", countryCode = "FR", city = "Paris")
        val u3 = UserEntity(id = "u3", username = "u3", displayName = "User 3", email = "u3@e.com", passwordHash = "p", country = "Italy", countryCode = "IT", city = "Rome")

        val userMap = mapOf("u1" to u1, "u2" to u2, "u3" to u3)

        val rels = listOf(
            RelationshipEntity(id = "r1", userAId = "u1", userBId = "u2", type = RelationshipType.ROMANTIC, status = RelationshipStatus.ACTIVE, requestedBy = "u1"),
            RelationshipEntity(id = "r2", userAId = "u2", userBId = "u3", type = RelationshipType.FRIENDSHIP, status = RelationshipStatus.ACTIVE, requestedBy = "u2")
        )

        val path = RelationshipGraphEngine.findConnectionPath(
            startUserId = "u1",
            targetUserId = "u3",
            allUsersMap = userMap,
            activeRelationships = rels
        )

        assertNotNull("Path between u1 and u3 must exist", path)
        assertEquals(2, path?.steps)
        assertEquals(3, path?.users?.size)
        assertEquals("u1", path?.users?.get(0)?.id)
        assertEquals("u2", path?.users?.get(1)?.id)
        assertEquals("u3", path?.users?.get(2)?.id)
    }
}
