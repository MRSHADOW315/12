package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import com.example.data.local.KinSphereDatabase
import com.example.data.repository.KinSphereRepository
import com.example.service.firebase.FirebaseManager
import com.example.service.location.LocationService
import com.example.ui.KinSphereMainScreen
import com.example.ui.auth.AuthScreen
import com.example.ui.theme.KinSphereTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize Real Services
        FirebaseManager.initialize(applicationContext)
        LocationService.initialize(applicationContext)

        val database = KinSphereDatabase.getDatabase(applicationContext)
        val repository = KinSphereRepository(database)

        setContent {
            KinSphereTheme {
                val coroutineScope = rememberCoroutineScope()
                val firebaseUser by FirebaseManager.currentUser.collectAsState()

                LaunchedEffect(firebaseUser) {
                    firebaseUser?.uid?.let { uid ->
                        repository.switchCurrentUser(uid)
                        // Fetch profile from Firestore and cache in Room
                        val profileRes = FirebaseManager.fetchUserProfile(uid)
                        profileRes.onSuccess { profile ->
                            if (profile != null) {
                                database.userDao().insertUser(profile)
                            }
                        }
                    }
                }

                if (firebaseUser == null) {
                    AuthScreen(
                        onAuthSuccess = { uid ->
                            repository.switchCurrentUser(uid)
                            coroutineScope.launch {
                                val profileRes = FirebaseManager.fetchUserProfile(uid)
                                profileRes.onSuccess { profile ->
                                    if (profile != null) {
                                        database.userDao().insertUser(profile)
                                    }
                                }
                            }
                        }
                    )
                } else {
                    KinSphereMainScreen(repository = repository)
                }
            }
        }
    }
}
