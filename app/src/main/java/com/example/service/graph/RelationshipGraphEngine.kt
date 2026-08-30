package com.example.service.graph

import com.example.data.local.entities.RelationshipEntity
import com.example.data.local.entities.UserEntity
import com.example.data.model.RelationshipStatus
import com.example.data.model.RelationshipType
import java.util.LinkedList
import java.util.Queue

data class GraphNode(
    val user: UserEntity,
    val distance: Int,
    val relationshipTypeWithParent: RelationshipType?
)

data class ConnectionPath(
    val users: List<UserEntity>,
    val relationships: List<RelationshipEntity>,
    val steps: Int
)

data class NetworkStats(
    val romanticCount: Int,
    val friendshipCount: Int,
    val familyCount: Int,
    val totalDirectConnections: Int,
    val extendedNetworkSize: Int
)

object RelationshipGraphEngine {

    /**
     * Find shortest path between startUser and targetUser using Breadth-First Search (BFS).
     * Respects relationship status (must be ACTIVE).
     */
    fun findConnectionPath(
        startUserId: String,
        targetUserId: String,
        allUsersMap: Map<String, UserEntity>,
        activeRelationships: List<RelationshipEntity>
    ): ConnectionPath? {
        if (startUserId == targetUserId) {
            val user = allUsersMap[startUserId] ?: return null
            return ConnectionPath(listOf(user), emptyList(), 0)
        }

        // Build Adjacency List: userId -> List of (neighborId, RelationshipEntity)
        val adj = mutableMapOf<String, MutableList<Pair<String, RelationshipEntity>>>()
        for (rel in activeRelationships) {
            if (rel.status != RelationshipStatus.ACTIVE) continue
            adj.getOrPut(rel.userAId) { mutableListOf() }.add(rel.userBId to rel)
            adj.getOrPut(rel.userBId) { mutableListOf() }.add(rel.userAId to rel)
        }

        val queue: Queue<String> = LinkedList()
        val visited = mutableSetOf<String>()
        val parent = mutableMapOf<String, Pair<String, RelationshipEntity>>()

        queue.add(startUserId)
        visited.add(startUserId)

        var found = false
        while (queue.isNotEmpty()) {
            val curr = queue.poll() ?: break
            if (curr == targetUserId) {
                found = true
                break
            }

            val neighbors = adj[curr] ?: emptyList()
            for ((neighborId, edge) in neighbors) {
                if (!visited.contains(neighborId)) {
                    visited.add(neighborId)
                    parent[neighborId] = curr to edge
                    queue.add(neighborId)
                }
            }
        }

        if (!found) return null

        // Reconstruct path
        val pathUsers = mutableListOf<UserEntity>()
        val pathEdges = mutableListOf<RelationshipEntity>()
        var curr = targetUserId

        while (curr != startUserId) {
            val user = allUsersMap[curr]
            if (user != null) pathUsers.add(0, user)
            val p = parent[curr] ?: break
            pathEdges.add(0, p.second)
            curr = p.first
        }
        val startUser = allUsersMap[startUserId]
        if (startUser != null) pathUsers.add(0, startUser)

        return ConnectionPath(
            users = pathUsers,
            relationships = pathEdges,
            steps = pathEdges.size
        )
    }

    /**
     * Compute personal network stats for a user
     */
    fun computeNetworkStats(
        userId: String,
        relationships: List<RelationshipEntity>,
        allRelationships: List<RelationshipEntity>
    ): NetworkStats {
        val userRels = relationships.filter { 
            (it.userAId == userId || it.userBId == userId) && it.status == RelationshipStatus.ACTIVE 
        }
        
        val romantic = userRels.count { it.type == RelationshipType.ROMANTIC }
        val friendship = userRels.count { it.type == RelationshipType.FRIENDSHIP }
        val family = userRels.count { it.type == RelationshipType.FAMILY }
        val direct = userRels.size

        // Calculate 2nd degree network size
        val directUserIds = userRels.map { if (it.userAId == userId) it.userBId else it.userAId }.toSet()
        val secondDegreeUserIds = mutableSetOf<String>()
        for (rel in allRelationships) {
            if (rel.status != RelationshipStatus.ACTIVE) continue
            if (directUserIds.contains(rel.userAId) && rel.userBId != userId && !directUserIds.contains(rel.userBId)) {
                secondDegreeUserIds.add(rel.userBId)
            }
            if (directUserIds.contains(rel.userBId) && rel.userAId != userId && !directUserIds.contains(rel.userAId)) {
                secondDegreeUserIds.add(rel.userAId)
            }
        }

        return NetworkStats(
            romanticCount = romantic,
            friendshipCount = friendship,
            familyCount = family,
            totalDirectConnections = direct,
            extendedNetworkSize = direct + secondDegreeUserIds.size
        )
    }

    /**
     * Discover mutual connections between userA and userB
     */
    fun findMutualConnections(
        userAId: String,
        userBId: String,
        activeRelationships: List<RelationshipEntity>,
        allUsersMap: Map<String, UserEntity>
    ): List<UserEntity> {
        val friendsOfA = activeRelationships
            .filter { (it.userAId == userAId || it.userBId == userAId) && it.status == RelationshipStatus.ACTIVE }
            .map { if (it.userAId == userAId) it.userBId else it.userAId }
            .toSet()

        val friendsOfB = activeRelationships
            .filter { (it.userAId == userBId || it.userBId == userBId) && it.status == RelationshipStatus.ACTIVE }
            .map { if (it.userAId == userBId) it.userBId else it.userAId }
            .toSet()

        val mutualIds = friendsOfA.intersect(friendsOfB)
        return mutualIds.mapNotNull { allUsersMap[it] }
    }
}
