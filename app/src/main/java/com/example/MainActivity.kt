package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.data.local.KinSphereDatabase
import com.example.data.repository.KinSphereRepository
import com.example.ui.KinSphereMainScreen
import com.example.ui.theme.KinSphereTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = KinSphereDatabase.getDatabase(applicationContext)
        val repository = KinSphereRepository(database)

        setContent {
            KinSphereTheme {
                KinSphereMainScreen(repository = repository)
            }
        }
    }
}

