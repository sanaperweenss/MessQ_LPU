package com.example.messqlpu.data.firebase

import com.example.messqlpu.domain.model.*
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

/**
 * FirebaseManager provides a unified production interface for:
 * 1. Firebase Authentication (Email/Password, Anonymous, Sign out)
 * 2. Cloud Firestore Real-time Collections (/Users, /DiningHalls, /Queues, /Tokens, /Orders)
 * 3. Firebase Cloud Messaging (FCM tokens, push event emission)
 * 4. Resilient Offline Fallback (never crashes when cloud is unreachable or unconfigured)
 */
object FirebaseManager {

    private const val TAG = "MessQ_Firebase"

    private fun logD(tag: String, msg: String) {
        try {
            android.util.Log.d(tag, msg)
        } catch (_: Throwable) {
            println("[$tag] $msg")
        }
    }

    private fun logW(tag: String, msg: String) {
        try {
            android.util.Log.w(tag, msg)
        } catch (_: Throwable) {
            System.err.println("[$tag] $msg")
        }
    }

    // Safe Firebase Auth instance
    val auth: FirebaseAuth? by lazy {
        try {
            FirebaseAuth.getInstance()
        } catch (e: Throwable) {
            logW(TAG, "FirebaseAuth initialization fallback: ${e.message}")
            null
        }
    }

    // Safe Cloud Firestore instance
    val firestore: FirebaseFirestore? by lazy {
        try {
            FirebaseFirestore.getInstance()
        } catch (e: Throwable) {
            logW(TAG, "FirebaseFirestore initialization fallback: ${e.message}")
            null
        }
    }

    // In-app Push Notification Event Bus (mirrors FCM Push Notifications)
    private val _fcmNotificationFlow = MutableSharedFlow<FcmNotificationPayload>(replay = 1)
    val fcmNotificationFlow: SharedFlow<FcmNotificationPayload> = _fcmNotificationFlow.asSharedFlow()

    data class FcmNotificationPayload(
        val title: String,
        val message: String,
        val type: String,
        val timestamp: Long = System.currentTimeMillis()
    )

    // Current Active User Profile session
    private var _currentUser: UserProfile? = UserProfile()
    val currentUser: UserProfile? get() = _currentUser

    // Cached FCM device token
    private var cachedFcmToken: String? = null

    fun initFcm() {
        try {
            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val token = task.result
                    cachedFcmToken = token
                    logD(TAG, "Firebase Cloud Messaging Token ready: $token")
                } else {
                    logW(TAG, "FCM token fetch failed: ${task.exception?.message}")
                }
            }
        } catch (e: Throwable) {
            logW(TAG, "FirebaseMessaging not initialized: ${e.message}")
        }
    }

    // ==========================================
    // 1. FIREBASE AUTHENTICATION
    // ==========================================

    fun parseAuthErrorMessage(e: Throwable): String {
        val msg = e.message?.lowercase() ?: ""
        return when {
            e is FirebaseAuthInvalidUserException || msg.contains("user-not-found") || msg.contains("no user record") ->
                "No account found with this email. Please tap 'Create Account' below to sign up."
            e is FirebaseAuthInvalidCredentialsException || msg.contains("wrong-password") || msg.contains("invalid-credential") || msg.contains("password is invalid") ->
                "Incorrect password or account not registered yet. Tap 'Create Account' if you are new."
            e is FirebaseAuthUserCollisionException || msg.contains("email-already-in-use") ->
                "An account already exists with this email. Please Sign In instead."
            e is FirebaseAuthWeakPasswordException || msg.contains("weak-password") ->
                "Password is too weak. Please use at least 6 characters."
            msg.contains("badly formatted") || msg.contains("invalid-email") ->
                "Please enter a valid email address."
            e is FirebaseNetworkException || msg.contains("network") || msg.contains("unable to resolve host") ->
                "Network error. Please check your internet connection."
            msg.contains("too-many-requests") || msg.contains("blocked") ->
                "Too many failed attempts. Please try again later."
            else -> e.localizedMessage ?: "Authentication failed. Please check your details."
        }
    }

    suspend fun signInWithEmail(emailOrId: String, pass: String): Result<UserProfile> = withContext(Dispatchers.IO) {
        val fbAuth = auth ?: return@withContext Result.failure(Exception("Firebase Auth is unavailable. Check configuration."))
        val email = if (emailOrId.contains("@")) emailOrId.trim() else "${emailOrId.trim()}@lpu.in"
        val password = pass.trim()
        try {
            val authResult = fbAuth.signInWithEmailAndPassword(email, password).await()
            val uid = authResult.user?.uid ?: return@withContext Result.failure(Exception("Failed to retrieve user ID."))
            val fetched = fetchUserProfile(uid) ?: fetchUserProfile(email)
            val profile = fetched ?: UserProfile(
                studentId = if (emailOrId.contains("@")) emailOrId.substringBefore("@") else emailOrId.trim(),
                name = authResult.user?.displayName?.ifBlank { null }
                    ?: email.substringBefore("@").replace(".", " ")
                        .split(" ").filter { it.isNotBlank() }
                        .joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } }
                        .ifBlank { "Student User" },
                email = email,
                role = "Student"
            )
            _currentUser = profile
            syncUserProfile(profile)
            logD(TAG, "Firebase Auth sign-in successful: $uid")
            Result.success(profile)
        } catch (e: Throwable) {
            logW(TAG, "Firebase Auth sign-in failed: ${e.message}")
            Result.failure(Exception(parseAuthErrorMessage(e)))
        }
    }

    suspend fun signUpWithEmail(email: String, pass: String, profile: UserProfile): Result<UserProfile> = withContext(Dispatchers.IO) {
        val fbAuth = auth ?: return@withContext Result.failure(Exception("Firebase Auth is unavailable."))
        try {
            val result = fbAuth.createUserWithEmailAndPassword(email.trim(), pass.trim()).await()
            val uid = result.user?.uid ?: profile.studentId
            val finalProfile = profile.copy(studentId = uid)
            syncUserProfile(finalProfile)
            _currentUser = finalProfile
            logD(TAG, "Firebase Auth user created: $uid")
            Result.success(finalProfile)
        } catch (e: Throwable) {
            logW(TAG, "Firebase Auth sign-up error: ${e.message}")
            Result.failure(Exception(parseAuthErrorMessage(e)))
        }
    }

    suspend fun signInWithGoogleToken(idToken: String): Result<UserProfile> = withContext(Dispatchers.IO) {
        val fbAuth = auth ?: return@withContext Result.failure(Exception("Firebase Auth is unavailable."))
        try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = fbAuth.signInWithCredential(credential).await()
            val user = authResult.user ?: return@withContext Result.failure(Exception("Failed to retrieve Google User."))
            val uid = user.uid
            val email = user.email ?: ""
            val name = user.displayName?.ifBlank { null }
                ?: email.substringBefore("@").replace(".", " ")
                    .split(" ").filter { it.isNotBlank() }
                    .joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } }
                    .ifBlank { "Google User" }

            val fetched = fetchUserProfile(uid) ?: fetchUserProfile(email)
            val profile = fetched ?: UserProfile(
                studentId = email.substringBefore("@").ifBlank { uid.take(8) },
                name = name,
                email = email,
                role = "Student",
                avatarId = "avatar_scholar"
            )
            _currentUser = profile
            syncUserProfile(profile)
            logD(TAG, "Firebase Auth Google sign-in successful: $uid")
            Result.success(profile)
        } catch (e: Throwable) {
            logW(TAG, "Firebase Auth Google sign-in failed: ${e.message}")
            Result.failure(Exception(parseAuthErrorMessage(e)))
        }
    }

    suspend fun signInAnonymously(): Boolean = withContext(Dispatchers.IO) {
        val fbAuth = auth ?: return@withContext true
        try {
            fbAuth.signInAnonymously().await()
            logD(TAG, "Firebase Auth anonymous sign-in success: ${fbAuth.currentUser?.uid}")
            true
        } catch (e: Throwable) {
            logW(TAG, "Anonymous sign-in fallback: ${e.message}")
            true
        }
    }

    fun signOut() {
        try {
            auth?.signOut()
        } catch (e: Throwable) {
            logW(TAG, "Sign out error: ${e.message}")
        }
        _currentUser = null
    }

    fun updateCurrentUser(user: UserProfile) {
        _currentUser = user
        // Sync asynchronously to Firestore
        CoroutineScope(Dispatchers.IO).launch {
            syncUserProfile(user)
        }
    }

    fun isUserLoggedIn(): Boolean = _currentUser != null

    // ==========================================
    // 2. CLOUD FIRESTORE SYNCHRONIZATION
    // ==========================================

    /**
     * Persist user profile and preferences to /Users/{studentId}
     */
    suspend fun syncUserProfile(profile: UserProfile) = withContext(Dispatchers.IO) {
        val db = firestore ?: return@withContext
        try {
            val userMap = hashMapOf(
                "studentId" to profile.studentId,
                "name" to profile.name,
                "email" to profile.email,
                "role" to profile.role,
                "department" to profile.department,
                "hostel" to profile.hostel,
                "dietaryPreference" to profile.dietaryPreference.name,
                "darkModeEnabled" to profile.darkModeEnabled,
                "notificationsEnabled" to profile.notificationsEnabled,
                "locationAccessEnabled" to profile.locationAccessEnabled,
                "avatarId" to profile.avatarId,
                "fcmToken" to (cachedFcmToken ?: ""),
                "updatedAt" to System.currentTimeMillis()
            )
            val docId = if (profile.email.isNotBlank()) profile.email else profile.studentId
            db.collection("Users").document(docId)
                .set(userMap, SetOptions.merge())
                .await()
            logD(TAG, "Synced UserProfile to Firestore: $docId")
        } catch (e: Throwable) {
            logW(TAG, "Firestore syncUserProfile skipped: ${e.message}")
        }
    }

    /**
     * Retrieve user profile from Firestore /Users/{docId}
     */
    suspend fun fetchUserProfile(emailOrId: String): UserProfile? = withContext(Dispatchers.IO) {
        val db = firestore ?: return@withContext null
        try {
            val docId = if (emailOrId.contains("@")) emailOrId else "$emailOrId@lpu.in"
            val snapshot = db.collection("Users").document(docId).get().await()
            if (snapshot.exists()) {
                val dietaryStr = snapshot.getString("dietaryPreference") ?: DietaryPreference.VEG.name
                val dietary = try { DietaryPreference.valueOf(dietaryStr) } catch (_: Throwable) { DietaryPreference.VEG }
                val profile = UserProfile(
                    studentId = snapshot.getString("studentId") ?: emailOrId,
                    name = snapshot.getString("name") ?: emailOrId.substringBefore("@").replaceFirstChar { it.uppercase() },
                    email = snapshot.getString("email") ?: docId,
                    role = snapshot.getString("role") ?: "Student",
                    department = snapshot.getString("department") ?: "School of Computer Science & Eng.",
                    hostel = snapshot.getString("hostel") ?: "Campus Hostel",
                    dietaryPreference = dietary,
                    darkModeEnabled = snapshot.getBoolean("darkModeEnabled") ?: false,
                    notificationsEnabled = snapshot.getBoolean("notificationsEnabled") ?: true,
                    locationAccessEnabled = snapshot.getBoolean("locationAccessEnabled") ?: true,
                    avatarId = snapshot.getString("avatarId") ?: "avatar_scholar"
                )
                logD(TAG, "Loaded user profile from Firestore: ${profile.name} (${profile.email})")
                return@withContext profile
            }
        } catch (e: Throwable) {
            logW(TAG, "fetchUserProfile error: ${e.message}")
        }
        null
    }

    /**
     * Real-time listener for Dining Hall crowd and queue state from /DiningHalls
     */
    fun listenToDiningHalls(onUpdate: (List<Map<String, Any>>) -> Unit): ListenerRegistration? {
        val db = firestore ?: return null
        return try {
            db.collection("DiningHalls").addSnapshotListener { snapshot, error ->
                if (error != null) {
                    logW(TAG, "DiningHalls listen failed: ${error.message}")
                    return@addSnapshotListener
                }
                if (snapshot != null && !snapshot.isEmpty) {
                    val halls = snapshot.documents.mapNotNull { it.data }
                    onUpdate(halls)
                }
            }
        } catch (e: Throwable) {
            logW(TAG, "listenToDiningHalls error: ${e.message}")
            null
        }
    }

    /**
     * Real-time listener for live queue status at a dining hall from /Queues/{diningHallId}
     */
    fun listenToQueue(
        diningHallId: String,
        onUpdate: (currentServingToken: Int, lastIssuedToken: Int, waitTime: Int) -> Unit
    ): ListenerRegistration? {
        val db = firestore ?: return null
        return try {
            db.collection("Queues").document(diningHallId).addSnapshotListener { snapshot, error ->
                if (error != null) {
                    logW(TAG, "Queue listen failed: ${error.message}")
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    val serving = (snapshot.getLong("currentServingToken") ?: 124).toInt()
                    val lastIssued = (snapshot.getLong("lastIssuedToken") ?: 147).toInt()
                    val waitTime = (snapshot.getLong("waitTimeMinutes") ?: 8).toInt()
                    onUpdate(serving, lastIssued, waitTime)
                }
            }
        } catch (e: Throwable) {
            logW(TAG, "listenToQueue error: ${e.message}")
            null
        }
    }

    /**
     * Sync user's active queue token to /Tokens/{tokenId}
     */
    suspend fun syncQueueToken(token: QueueToken) = withContext(Dispatchers.IO) {
        val db = firestore ?: return@withContext
        try {
            val tokenMap = hashMapOf(
                "tokenNumber" to token.tokenNumber,
                "diningHallId" to token.diningHallId,
                "diningHallName" to token.diningHallName,
                "studentId" to token.studentId,
                "studentName" to token.studentName,
                "status" to token.status.name,
                "currentServingToken" to token.currentServingToken,
                "remainingAhead" to token.remainingAhead,
                "estimatedWaitMinutes" to token.estimatedWaitMinutes,
                "timestamp" to System.currentTimeMillis()
            )
            val docId = "${token.diningHallId}_${token.tokenNumber}"
            db.collection("Tokens").document(docId)
                .set(tokenMap, SetOptions.merge())
                .await()
            logD(TAG, "Synced QueueToken #${token.tokenNumber} to Firestore")
        } catch (e: Throwable) {
            logW(TAG, "syncQueueToken skipped: ${e.message}")
        }
    }

    /**
     * Admin method to update queue tokens in Firestore /Queues/{diningHallId}
     */
    suspend fun updateAdminQueueInFirestore(diningHallId: String, currentServing: Int, lastIssued: Int) = withContext(Dispatchers.IO) {
        val db = firestore ?: return@withContext
        try {
            val queueMap = hashMapOf(
                "diningHallId" to diningHallId,
                "currentServingToken" to currentServing,
                "lastIssuedToken" to lastIssued,
                "updatedAt" to System.currentTimeMillis()
            )
            db.collection("Queues").document(diningHallId)
                .set(queueMap, SetOptions.merge())
                .await()
            logD(TAG, "Admin updated queue in Firestore: serving #$currentServing")
        } catch (e: Throwable) {
            logW(TAG, "updateAdminQueueInFirestore skipped: ${e.message}")
        }
    }

    /**
     * Save confirmed order to Firestore /Orders/{orderId}
     */
    suspend fun syncOrderToFirestore(order: Order) = withContext(Dispatchers.IO) {
        val db = firestore ?: return@withContext
        try {
            val orderMap = hashMapOf(
                "orderId" to order.orderId,
                "orderNumber" to order.orderNumber,
                "studentId" to order.studentId,
                "diningHallId" to order.diningHallId,
                "diningHallName" to order.diningHallName,
                "itemCount" to order.items.size,
                "totalAmount" to order.totalAmount,
                "status" to order.status.name,
                "pickupTime" to order.pickupTime,
                "orderDate" to order.orderDate,
                "qrVerificationCode" to order.qrVerificationCode,
                "tokenNumber" to order.tokenNumber,
                "createdAt" to System.currentTimeMillis()
            )
            db.collection("Orders").document(order.orderId)
                .set(orderMap, SetOptions.merge())
                .await()
            logD(TAG, "Synced Order ${order.orderNumber} to Firestore")
        } catch (e: Throwable) {
            logW(TAG, "syncOrderToFirestore skipped: ${e.message}")
        }
    }

    // ==========================================
    // 3. FIREBASE CLOUD MESSAGING (FCM)
    // ==========================================

    fun setFcmToken(token: String) {
        cachedFcmToken = token
        _currentUser?.let { profile ->
            CoroutineScope(Dispatchers.IO).launch {
                syncUserProfile(profile)
            }
        }
    }

    fun getCachedFcmToken(): String? = cachedFcmToken

    private val _notificationsListState = MutableStateFlow<List<com.example.messqlpu.presentation.screens.notifications.NotificationItem>>(
        listOf(
            com.example.messqlpu.presentation.screens.notifications.NotificationItem("n1", "Now Calling: Token #124", "Your token #147 is only 23 numbers away at Main Dining Hall. Head towards counter 2.", "5m ago", "QUEUE"),
            com.example.messqlpu.presentation.screens.notifications.NotificationItem("n2", "Order Ready for Pickup!", "Your meal order #MQ-8921 is freshly packed and ready at Central Mess Express window.", "25m ago", "ORDER"),
            com.example.messqlpu.presentation.screens.notifications.NotificationItem("n3", "Crowd Surge Alert", "Food Street occupancy exceeded 85%. Consider visiting Cafe Express (4 min wait).", "1h ago", "CROWD"),
            com.example.messqlpu.presentation.screens.notifications.NotificationItem("n4", "Special Lunch Menu Active", "Punjabi Rajma Chawal with Desi Ghee is live today at Main Mess counters.", "3h ago", "MENU")
        )
    )
    val notificationsListFlow: StateFlow<List<com.example.messqlpu.presentation.screens.notifications.NotificationItem>> = _notificationsListState.asStateFlow()

    suspend fun emitNotification(title: String, message: String, type: String = "INFO") {
        val newItem = com.example.messqlpu.presentation.screens.notifications.NotificationItem(
            id = "notif_" + System.currentTimeMillis(),
            title = title,
            message = message,
            time = "Just now",
            type = type
        )
        _notificationsListState.update { listOf(newItem) + it }
        _fcmNotificationFlow.emit(
            FcmNotificationPayload(title = title, message = message, type = type)
        )
        try {
            val context = com.example.messqlpu.MessQApplication.instance
            showSystemNotification(context, title, message)
        } catch (e: Throwable) {
            logW(TAG, "showSystemNotification failed: ${e.message}")
        }
    }

    private fun showSystemNotification(context: android.content.Context, title: String, message: String) {
        val notificationManager = context.getSystemService(android.content.Context.NOTIFICATION_SERVICE) as? android.app.NotificationManager ?: return
        val channelId = "messq_lpu_notifications"

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = android.app.NotificationChannel(
                channelId,
                "MessQ Dining & Queue Alerts",
                android.app.NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Live token call and meal order status notifications"
                enableLights(true)
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val intent = android.content.Intent(context, com.example.messqlpu.MainActivity::class.java).apply {
            flags = android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP or android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = android.app.PendingIntent.getActivity(
            context,
            0,
            intent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )

        val mascotBitmap = try {
            android.graphics.BitmapFactory.decodeResource(context.resources, com.example.messqlpu.R.drawable.messq_mascot)
        } catch (_: Throwable) { null }

        val notification = androidx.core.app.NotificationCompat.Builder(context, channelId)
            .setSmallIcon(com.example.messqlpu.R.drawable.ic_notification_mascot)
            .apply {
                if (mascotBitmap != null) {
                    setLargeIcon(mascotBitmap)
                }
            }
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(androidx.core.app.NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(androidx.core.app.NotificationCompat.PRIORITY_HIGH)
            .setDefaults(androidx.core.app.NotificationCompat.DEFAULT_ALL)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify((System.currentTimeMillis() % 100000).toInt(), notification)
    }
}
