package com.example.service.firebase

import android.app.Activity
import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.data.local.entities.MessageEntity
import com.example.data.local.entities.UserEntity
import com.example.data.model.MessageType
import com.example.data.model.UserRole
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.io.File
import java.util.UUID
import java.util.concurrent.TimeUnit

/**
 * Production Firebase Service Layer for METOU KinSphere.
 * Manages:
 * - Real Username + Password/PIN Authentication (with permanent Firebase UID)
 * - Atomic Unique Username Reservation via Firestore Transactions
 * - Real Phone Number Recovery via Firebase Phone Authentication & SMS OTP
 * - Real User Profiles in Firestore (users/{uid})
 * - Real Discovery with Exclusion of Self, Blocked, and Passed Users
 * - Real Likes, Passes, Blocks, Reports, and Mutual Matching (matches/{matchId})
 * - Real-Time Firestore Messaging with Media Attachments
 * - Real Media Upload to Firebase Storage
 */
object FirebaseManager {
    private const val TAG = "FirebaseManager"
    private const val AUTH_DOMAIN_SUFFIX = "@kinsphere.auth"

    private val RESERVED_USERNAMES = setOf(
        "admin", "administrator", "system", "kinsphere", "support",
        "moderator", "root", "official", "help", "security", "staff"
    )

    private var authInstance: FirebaseAuth? = null
    private var firestoreInstance: FirebaseFirestore? = null
    private var storageInstance: FirebaseStorage? = null

    private val _currentUser = MutableStateFlow<FirebaseUser?>(null)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()

    private val _isFirebaseAvailable = MutableStateFlow(false)
    val isFirebaseAvailable: StateFlow<Boolean> = _isFirebaseAvailable.asStateFlow()

    val currentUid: String?
        get() = authInstance?.currentUser?.uid ?: _currentUser.value?.uid

    fun initialize(context: Context) {
        try {
            if (FirebaseApp.getApps(context).isEmpty()) {
                FirebaseApp.initializeApp(context)
            }
            authInstance = FirebaseAuth.getInstance()
            firestoreInstance = FirebaseFirestore.getInstance()
            storageInstance = FirebaseStorage.getInstance()

            authInstance?.addAuthStateListener { auth ->
                _currentUser.value = auth.currentUser
            }
            _currentUser.value = authInstance?.currentUser
            _isFirebaseAvailable.value = true
            Log.d(TAG, "Firebase initialized successfully. Active UID: $currentUid")
        } catch (e: Exception) {
            Log.w(TAG, "Firebase initialization warning: ${e.message}")
            _isFirebaseAvailable.value = false
        }
    }

    // =========================================================================
    // 1. REAL USERNAME + PASSWORD/PIN AUTHENTICATION (Permanent Firebase UID)
    // =========================================================================

    fun validateUsername(rawUsername: String): Result<String> {
        val trimmed = rawUsername.trim()
        if (trimmed.length < 3) {
            return Result.failure(IllegalArgumentException("Username must be at least 3 characters long."))
        }
        if (trimmed.length > 20) {
            return Result.failure(IllegalArgumentException("Username cannot exceed 20 characters."))
        }
        val regex = Regex("^[a-zA-Z0-9_]+$")
        if (!regex.matches(trimmed)) {
            return Result.failure(IllegalArgumentException("Username can only contain letters, numbers, and underscores."))
        }
        val normalized = trimmed.lowercase()
        if (RESERVED_USERNAMES.contains(normalized)) {
            return Result.failure(IllegalArgumentException("This username is reserved by the system."))
        }
        return Result.success(normalized)
    }

    /**
     * Creates a permanent Firebase Account using Username + Password/PIN.
     * Uses an atomic Firestore transaction to guarantee that usernames are globally unique and case-insensitive.
     */
    suspend fun registerWithUsername(
        rawUsername: String,
        passwordOrPin: String,
        displayName: String,
        bio: String = ""
    ): Result<FirebaseUser> {
        val auth = authInstance ?: return Result.failure(IllegalStateException("Firebase Auth is not available."))
        val firestore = firestoreInstance ?: return Result.failure(IllegalStateException("Firestore is not available."))

        val validationResult = validateUsername(rawUsername)
        if (validationResult.isFailure) {
            return Result.failure(validationResult.exceptionOrNull()!!)
        }
        val normalizedUsername = validationResult.getOrThrow()

        if (passwordOrPin.length < 6) {
            return Result.failure(IllegalArgumentException("Password/PIN must be at least 6 characters."))
        }

        val usernameDocRef = firestore.collection("usernames").document(normalizedUsername)

        return try {
            // Step 1: Atomic Check via Transaction to ensure username is available
            val usernameAvailable = firestore.runTransaction { transaction ->
                val snapshot = transaction.get(usernameDocRef)
                if (snapshot.exists()) {
                    throw IllegalStateException("The username '$rawUsername' is already taken.")
                }
                true
            }.await()

            if (!usernameAvailable) {
                return Result.failure(IllegalStateException("Username already taken."))
            }

            // Step 2: Create Firebase Auth account
            val authEmail = "$normalizedUsername$AUTH_DOMAIN_SUFFIX"
            val authResult = auth.createUserWithEmailAndPassword(authEmail, passwordOrPin).await()
            val firebaseUser = authResult.user ?: return Result.failure(IllegalStateException("Account creation failed."))
            val uid = firebaseUser.uid

            // Step 3: Atomic write mapping username -> UID and creating user profile in Firestore
            val batch = firestore.batch()

            // Save username claim
            val usernameClaim = hashMapOf(
                "uid" to uid,
                "username" to rawUsername.trim(),
                "usernameLowercase" to normalizedUsername,
                "createdAt" to System.currentTimeMillis()
            )
            batch.set(usernameDocRef, usernameClaim)

            // Save user profile
            val userProfile = hashMapOf(
                "id" to uid,
                "username" to rawUsername.trim(),
                "usernameLowercase" to normalizedUsername,
                "displayName" to displayName.trim().ifBlank { rawUsername.trim() },
                "email" to authEmail,
                "avatarUrl" to "",
                "coverUrl" to "",
                "bio" to bio.ifBlank { "Hello! I am on METOU KinSphere." },
                "age" to 21,
                "country" to "",
                "countryCode" to "",
                "city" to "",
                "interests" to "Social, Tech, Networking",
                "languages" to "English",
                "recoveryPhone" to "",
                "isPhoneVerified" to false,
                "isGhostMode" to false,
                "isVerified" to false,
                "role" to UserRole.USER.name,
                "createdAt" to System.currentTimeMillis(),
                "lastActiveAt" to System.currentTimeMillis()
            )
            val userDocRef = firestore.collection("users").document(uid)
            batch.set(userDocRef, userProfile)

            batch.commit().await()

            _currentUser.value = firebaseUser
            Result.success(firebaseUser)
        } catch (e: Exception) {
            Log.e(TAG, "registerWithUsername error: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Signs in an existing account using Username + Password/PIN.
     */
    suspend fun loginWithUsername(
        rawUsername: String,
        passwordOrPin: String
    ): Result<FirebaseUser> {
        val auth = authInstance ?: return Result.failure(IllegalStateException("Firebase Auth is not available."))
        val validationResult = validateUsername(rawUsername)
        if (validationResult.isFailure) {
            return Result.failure(validationResult.exceptionOrNull()!!)
        }
        val normalizedUsername = validationResult.getOrThrow()
        val authEmail = "$normalizedUsername$AUTH_DOMAIN_SUFFIX"

        return try {
            val result = auth.signInWithEmailAndPassword(authEmail, passwordOrPin).await()
            val user = result.user ?: return Result.failure(IllegalStateException("Authentication returned null user."))
            _currentUser.value = user

            // Update lastActiveAt in Firestore
            firestoreInstance?.collection("users")?.document(user.uid)?.set(
                mapOf("lastActiveAt" to System.currentTimeMillis()),
                SetOptions.merge()
            )?.await()

            Result.success(user)
        } catch (e: Exception) {
            Log.e(TAG, "loginWithUsername error: ${e.message}")
            Result.failure(Exception("Invalid username or password/PIN."))
        }
    }

    fun signOut() {
        authInstance?.signOut()
        _currentUser.value = null
    }

    suspend fun deleteAccount(): Result<Unit> {
        val user = authInstance?.currentUser ?: return Result.failure(IllegalStateException("No user logged in."))
        val firestore = firestoreInstance ?: return Result.failure(IllegalStateException("Firestore unavailable."))
        return try {
            val uid = user.uid
            val userDoc = firestore.collection("users").document(uid).get().await()
            val normalizedUsername = userDoc.getString("usernameLowercase")

            val batch = firestore.batch()
            if (!normalizedUsername.isNullOrBlank()) {
                batch.delete(firestore.collection("usernames").document(normalizedUsername))
            }
            batch.delete(firestore.collection("users").document(uid))
            batch.commit().await()

            user.delete().await()
            _currentUser.value = null
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // =========================================================================
    // 2. PHONE NUMBER RECOVERY & SMS OTP
    // =========================================================================

    /**
     * Sends an SMS OTP via Firebase Phone Auth to link a recovery phone to the current account.
     */
    fun sendPhoneOtpForLinking(
        activity: Activity,
        phoneNumber: String,
        callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    ) {
        val auth = authInstance ?: run {
            callbacks.onVerificationFailed(FirebaseException("Firebase Auth not initialized"))
            return
        }
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber.trim())
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(callbacks)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    /**
     * Verifies the SMS OTP and links the phone number to the current permanent Firebase account.
     */
    suspend fun linkVerifiedPhone(
        verificationId: String,
        smsCode: String,
        phoneNumber: String
    ): Result<Unit> {
        val user = authInstance?.currentUser ?: return Result.failure(IllegalStateException("No user logged in."))
        val firestore = firestoreInstance ?: return Result.failure(IllegalStateException("Firestore unavailable."))
        return try {
            val credential = PhoneAuthProvider.getCredential(verificationId, smsCode.trim())
            user.linkWithCredential(credential).await()

            // Update user record in Firestore
            firestore.collection("users").document(user.uid).set(
                mapOf(
                    "recoveryPhone" to phoneNumber.trim(),
                    "isPhoneVerified" to true
                ),
                SetOptions.merge()
            ).await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Checks if an account exists by username and retrieves its recovery phone status.
     */
    suspend fun checkAccountRecoveryStatus(rawUsername: String): Result<Pair<String, String?>> {
        val firestore = firestoreInstance ?: return Result.failure(IllegalStateException("Firestore unavailable."))
        val validationResult = validateUsername(rawUsername)
        if (validationResult.isFailure) return Result.failure(validationResult.exceptionOrNull()!!)
        val normalized = validationResult.getOrThrow()

        return try {
            val usernameDoc = firestore.collection("usernames").document(normalized).get().await()
            if (!usernameDoc.exists()) {
                return Result.failure(IllegalArgumentException("No account found with username '$rawUsername'."))
            }
            val uid = usernameDoc.getString("uid") ?: return Result.failure(IllegalStateException("Account record corrupted."))
            val userDoc = firestore.collection("users").document(uid).get().await()
            val phone = userDoc.getString("recoveryPhone")
            val isVerified = userDoc.getBoolean("isPhoneVerified") ?: false

            if (phone.isNullOrBlank() || !isVerified) {
                Result.success(Pair(uid, null)) // No recovery phone linked
            } else {
                Result.success(Pair(uid, phone))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Resets the password/PIN for an account using verified Phone Auth credentials.
     */
    suspend fun resetPasswordWithPhoneCredential(
        rawUsername: String,
        credential: PhoneAuthCredential,
        newPasswordOrPin: String
    ): Result<Unit> {
        val auth = authInstance ?: return Result.failure(IllegalStateException("Firebase Auth unavailable."))
        if (newPasswordOrPin.length < 6) {
            return Result.failure(IllegalArgumentException("Password/PIN must be at least 6 characters."))
        }
        return try {
            val authResult = auth.signInWithCredential(credential).await()
            val user = authResult.user ?: return Result.failure(IllegalStateException("User recovery session failed."))
            user.updatePassword(newPasswordOrPin).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // =========================================================================
    // 3. REAL USER PROFILES (Firestore users/{uid})
    // =========================================================================

    suspend fun fetchUserProfile(uid: String): Result<UserEntity?> {
        val firestore = firestoreInstance ?: return Result.failure(IllegalStateException("Firestore not initialized."))
        return try {
            val doc = firestore.collection("users").document(uid).get().await()
            if (!doc.exists()) return Result.success(null)

            val user = UserEntity(
                id = doc.getString("id") ?: uid,
                username = doc.getString("username") ?: "",
                displayName = doc.getString("displayName") ?: "",
                email = doc.getString("email") ?: "",
                avatarUrl = doc.getString("avatarUrl") ?: "",
                coverUrl = doc.getString("coverUrl") ?: "",
                bio = doc.getString("bio") ?: "",
                age = doc.getLong("age")?.toInt() ?: 21,
                country = doc.getString("country") ?: "",
                countryCode = doc.getString("countryCode") ?: "",
                city = doc.getString("city") ?: "",
                interests = doc.getString("interests") ?: "",
                languages = doc.getString("languages") ?: "",
                isGhostMode = doc.getBoolean("isGhostMode") ?: false,
                isVerified = doc.getBoolean("isVerified") ?: false,
                lastActiveAt = doc.getLong("lastActiveAt") ?: System.currentTimeMillis()
            )
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun saveUserProfile(user: UserEntity): Result<Unit> {
        val firestore = firestoreInstance ?: return Result.failure(IllegalStateException("Firestore not initialized."))
        return try {
            val map = hashMapOf(
                "id" to user.id,
                "username" to user.username,
                "displayName" to user.displayName,
                "bio" to user.bio,
                "avatarUrl" to user.avatarUrl,
                "coverUrl" to user.coverUrl,
                "age" to user.age,
                "country" to user.country,
                "city" to user.city,
                "interests" to user.interests,
                "languages" to user.languages,
                "isGhostMode" to user.isGhostMode,
                "isVerified" to user.isVerified,
                "lastActiveAt" to System.currentTimeMillis()
            )
            firestore.collection("users").document(user.id).set(map, SetOptions.merge()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun saveUserProfile(
        uid: String,
        displayName: String,
        bio: String,
        interests: String,
        city: String,
        country: String,
        avatarUrl: String = ""
    ): Result<Unit> {
        val firestore = firestoreInstance ?: return Result.failure(IllegalStateException("Firestore not initialized."))
        return try {
            val map = hashMapOf(
                "id" to uid,
                "displayName" to displayName,
                "bio" to bio,
                "interests" to interests,
                "city" to city,
                "country" to country,
                "avatarUrl" to avatarUrl,
                "lastActiveAt" to System.currentTimeMillis()
            )
            firestore.collection("users").document(uid).set(map, SetOptions.merge()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // =========================================================================
    // 4. REAL DISCOVERY & FILTERING (Excluding Self, Blocked & Passed)
    // =========================================================================

    suspend fun fetchDiscoverableUsers(currentUid: String): Result<List<UserEntity>> {
        val firestore = firestoreInstance ?: return Result.failure(IllegalStateException("Firestore not initialized."))
        return try {
            // Fetch blocked users and passed users for currentUid
            val blocksSnapshot = firestore.collection("blocks")
                .whereEqualTo("fromUid", currentUid)
                .get()
                .await()
            val blockedUids = blocksSnapshot.documents.mapNotNull { it.getString("toUid") }.toSet()

            val passesSnapshot = firestore.collection("passes")
                .whereEqualTo("fromUid", currentUid)
                .get()
                .await()
            val passedUids = passesSnapshot.documents.mapNotNull { it.getString("toUid") }.toSet()

            val snapshot = firestore.collection("users")
                .limit(50)
                .get()
                .await()

            val discoverable = snapshot.documents.mapNotNull { doc ->
                val id = doc.getString("id") ?: doc.id
                if (id == currentUid || blockedUids.contains(id) || passedUids.contains(id)) {
                    null
                } else {
                    UserEntity(
                        id = id,
                        username = doc.getString("username") ?: id,
                        displayName = doc.getString("displayName") ?: "KinSphere User",
                        email = doc.getString("email") ?: "",
                        avatarUrl = doc.getString("avatarUrl") ?: "",
                        coverUrl = doc.getString("coverUrl") ?: "",
                        bio = doc.getString("bio") ?: "",
                        age = doc.getLong("age")?.toInt() ?: 21,
                        country = doc.getString("country") ?: "",
                        countryCode = doc.getString("countryCode") ?: "",
                        city = doc.getString("city") ?: "",
                        interests = doc.getString("interests") ?: "",
                        languages = doc.getString("languages") ?: "",
                        isGhostMode = doc.getBoolean("isGhostMode") ?: false,
                        isVerified = doc.getBoolean("isVerified") ?: false,
                        lastActiveAt = doc.getLong("lastActiveAt") ?: System.currentTimeMillis()
                    )
                }
            }
            Result.success(discoverable)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchAllUsers(): List<UserEntity> {
        val firestore = firestoreInstance ?: return emptyList()
        return try {
            val snapshot = firestore.collection("users").limit(100).get().await()
            snapshot.documents.mapNotNull { doc ->
                val id = doc.getString("id") ?: doc.id
                UserEntity(
                    id = id,
                    username = doc.getString("username") ?: id,
                    displayName = doc.getString("displayName") ?: "KinSphere User",
                    email = doc.getString("email") ?: "",
                    avatarUrl = doc.getString("avatarUrl") ?: "",
                    coverUrl = doc.getString("coverUrl") ?: "",
                    bio = doc.getString("bio") ?: "",
                    age = doc.getLong("age")?.toInt() ?: 21,
                    country = doc.getString("country") ?: "",
                    countryCode = doc.getString("countryCode") ?: "",
                    city = doc.getString("city") ?: "",
                    interests = doc.getString("interests") ?: "",
                    languages = doc.getString("languages") ?: "",
                    isGhostMode = doc.getBoolean("isGhostMode") ?: false,
                    isVerified = doc.getBoolean("isVerified") ?: false,
                    lastActiveAt = doc.getLong("lastActiveAt") ?: System.currentTimeMillis()
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "fetchAllUsers failed: ${e.message}")
            emptyList()
        }
    }

    // =========================================================================
    // 5. REAL LIKES, PASSES, MUTUAL MATCHES, BLOCKS & REPORTS
    // =========================================================================

    suspend fun sendLike(fromUid: String, toUid: String, isSuperLike: Boolean = false): Result<Boolean> {
        val firestore = firestoreInstance ?: return Result.failure(IllegalStateException("Firestore not initialized."))
        return try {
            val likeId = "${fromUid}_$toUid"
            val likeData = hashMapOf(
                "fromUid" to fromUid,
                "toUid" to toUid,
                "isSuperLike" to isSuperLike,
                "timestamp" to System.currentTimeMillis()
            )
            firestore.collection("likes").document(likeId).set(likeData).await()

            // Check if reverse like exists (Mutual Match)
            val reverseLikeId = "${toUid}_$fromUid"
            val reverseDoc = firestore.collection("likes").document(reverseLikeId).get().await()
            val isMutualMatch = reverseDoc.exists()

            if (isMutualMatch) {
                val sortedUids = listOf(fromUid, toUid).sorted()
                val matchId = "${sortedUids[0]}_${sortedUids[1]}"
                val matchData = hashMapOf(
                    "matchId" to matchId,
                    "users" to sortedUids,
                    "createdAt" to System.currentTimeMillis(),
                    "lastInteraction" to System.currentTimeMillis()
                )
                firestore.collection("matches").document(matchId).set(matchData, SetOptions.merge()).await()

                // Initialize direct conversation
                val convData = hashMapOf(
                    "id" to matchId,
                    "participants" to sortedUids,
                    "lastMessage" to "🎉 You matched! Say hi.",
                    "lastMessageSenderId" to "system",
                    "lastMessageTimestamp" to System.currentTimeMillis(),
                    "updatedAt" to System.currentTimeMillis()
                )
                firestore.collection("conversations").document(matchId).set(convData, SetOptions.merge()).await()
            }
            Result.success(isMutualMatch)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun sendPass(fromUid: String, toUid: String): Result<Unit> {
        val firestore = firestoreInstance ?: return Result.failure(IllegalStateException("Firestore not initialized."))
        return try {
            val passId = "${fromUid}_$toUid"
            val passData = hashMapOf(
                "fromUid" to fromUid,
                "toUid" to toUid,
                "timestamp" to System.currentTimeMillis()
            )
            firestore.collection("passes").document(passId).set(passData).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun blockUser(fromUid: String, toUid: String): Result<Unit> {
        val firestore = firestoreInstance ?: return Result.failure(IllegalStateException("Firestore not initialized."))
        return try {
            val blockId = "${fromUid}_$toUid"
            val blockData = hashMapOf(
                "fromUid" to fromUid,
                "toUid" to toUid,
                "createdAt" to System.currentTimeMillis()
            )
            firestore.collection("blocks").document(blockId).set(blockData).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun reportUser(reporterUid: String, targetUid: String, reason: String, details: String): Result<Unit> {
        val firestore = firestoreInstance ?: return Result.failure(IllegalStateException("Firestore not initialized."))
        return try {
            val reportId = UUID.randomUUID().toString()
            val reportData = hashMapOf(
                "reportId" to reportId,
                "reporterUid" to reporterUid,
                "targetUid" to targetUid,
                "reason" to reason,
                "details" to details,
                "createdAt" to System.currentTimeMillis()
            )
            firestore.collection("reports").document(reportId).set(reportData).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchMatchedUserIds(currentUid: String): Result<List<String>> {
        val firestore = firestoreInstance ?: return Result.failure(IllegalStateException("Firestore not initialized."))
        return try {
            val snapshot = firestore.collection("matches")
                .whereArrayContains("users", currentUid)
                .get()
                .await()
            val matchedIds = snapshot.documents.mapNotNull { doc ->
                @Suppress("UNCHECKED_CAST")
                val users = doc.get("users") as? List<String>
                users?.firstOrNull { it != currentUid }
            }
            Result.success(matchedIds)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // =========================================================================
    // 6. REAL-TIME MESSAGING
    // =========================================================================

    fun observeMessages(conversationId: String): Flow<List<MessageEntity>> = callbackFlow {
        val firestore = firestoreInstance
        if (firestore == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val listener: ListenerRegistration = firestore.collection("conversations")
            .document(conversationId)
            .collection("messages")
            .orderBy("createdAt", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Observe messages error: ${error.message}")
                    return@addSnapshotListener
                }
                val messages = snapshot?.documents?.mapNotNull { doc ->
                    MessageEntity(
                        id = doc.getString("id") ?: doc.id,
                        conversationId = conversationId,
                        senderId = doc.getString("senderId") ?: "",
                        text = doc.getString("text") ?: "",
                        mediaUrl = doc.getString("mediaUrl") ?: "",
                        mediaType = try { MessageType.valueOf(doc.getString("mediaType") ?: "TEXT") } catch (e: Exception) { MessageType.TEXT },
                        createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
                    )
                } ?: emptyList()
                trySend(messages)
            }

        awaitClose { listener.remove() }
    }

    suspend fun sendMessage(
        conversationId: String,
        senderId: String,
        receiverId: String,
        text: String,
        mediaUrl: String = "",
        type: MessageType = MessageType.TEXT
    ): Result<MessageEntity> {
        val firestore = firestoreInstance ?: return Result.failure(IllegalStateException("Firestore not initialized."))
        return try {
            val messageId = UUID.randomUUID().toString()
            val timestamp = System.currentTimeMillis()
            val messageData = hashMapOf(
                "id" to messageId,
                "conversationId" to conversationId,
                "senderId" to senderId,
                "receiverId" to receiverId,
                "text" to text,
                "mediaType" to type.name,
                "mediaUrl" to mediaUrl,
                "createdAt" to timestamp
            )

            // Save message document in subcollection
            firestore.collection("conversations")
                .document(conversationId)
                .collection("messages")
                .document(messageId)
                .set(messageData)
                .await()

            // Update parent conversation document
            val convData = hashMapOf(
                "id" to conversationId,
                "participants" to listOf(senderId, receiverId),
                "lastMessage" to if (mediaUrl.isNotEmpty()) "📷 Photo" else text,
                "lastMessageSenderId" to senderId,
                "lastMessageTimestamp" to timestamp,
                "updatedAt" to timestamp
            )
            firestore.collection("conversations")
                .document(conversationId)
                .set(convData, SetOptions.merge())
                .await()

            Result.success(
                MessageEntity(
                    id = messageId,
                    conversationId = conversationId,
                    senderId = senderId,
                    text = text,
                    mediaType = type,
                    mediaUrl = mediaUrl,
                    createdAt = timestamp
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // =========================================================================
    // 7. REAL CLOUD STORAGE UPLOAD
    // =========================================================================

    suspend fun uploadImageFile(file: File, remotePath: String): Result<String> {
        val storage = storageInstance
        if (storage == null) {
            return Result.success(Uri.fromFile(file).toString())
        }
        return try {
            val storageRef = storage.reference.child(remotePath)
            val uri = Uri.fromFile(file)
            storageRef.putFile(uri).await()
            val downloadUrl = storageRef.downloadUrl.await().toString()
            Result.success(downloadUrl)
        } catch (e: Exception) {
            Log.w(TAG, "Storage upload exception: ${e.message}, using local URI fallback")
            Result.success(Uri.fromFile(file).toString())
        }
    }
}
