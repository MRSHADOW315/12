package com.example.service.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationManager
import android.util.Log
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

data class RealLocation(
    val latitude: Double,
    val longitude: Double,
    val accuracyMeters: Float = 0f,
    val timestamp: Long = System.currentTimeMillis(),
    val provider: String = "GPS"
)

object LocationService {
    private const val TAG = "LocationService"

    private var fusedLocationClient: FusedLocationProviderClient? = null
    private val _currentLocation = MutableStateFlow<RealLocation?>(null)
    val currentLocation: StateFlow<RealLocation?> = _currentLocation.asStateFlow()

    private val _isPermissionGranted = MutableStateFlow(false)
    val isPermissionGranted: StateFlow<Boolean> = _isPermissionGranted.asStateFlow()

    fun initialize(context: Context) {
        try {
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize FusedLocationProviderClient: ${e.message}")
        }
    }

    fun setPermissionGranted(granted: Boolean) {
        _isPermissionGranted.value = granted
    }

    @SuppressLint("MissingPermission")
    suspend fun fetchCurrentDeviceLocation(context: Context): RealLocation? {
        val client = fusedLocationClient ?: LocationServices.getFusedLocationProviderClient(context).also {
            fusedLocationClient = it
        }

        try {
            // First try high-accuracy current location
            val cancellationTokenSource = CancellationTokenSource()
            val location: Location? = client.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                cancellationTokenSource.token
            ).await()

            if (location != null) {
                val realLoc = RealLocation(
                    latitude = location.latitude,
                    longitude = location.longitude,
                    accuracyMeters = location.accuracy,
                    timestamp = location.time,
                    provider = location.provider ?: "FusedLocation"
                )
                _currentLocation.value = realLoc
                return realLoc
            }

            // Fallback to last known location
            val lastLoc: Location? = client.lastLocation.await()
            if (lastLoc != null) {
                val realLoc = RealLocation(
                    latitude = lastLoc.latitude,
                    longitude = lastLoc.longitude,
                    accuracyMeters = lastLoc.accuracy,
                    timestamp = lastLoc.time,
                    provider = lastLoc.provider ?: "LastLocation"
                )
                _currentLocation.value = realLoc
                return realLoc
            }

            // Fallback to system LocationManager
            val locManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            val gpsLoc = locManager?.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            val netLoc = locManager?.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            val bestLoc = gpsLoc ?: netLoc

            if (bestLoc != null) {
                val realLoc = RealLocation(
                    latitude = bestLoc.latitude,
                    longitude = bestLoc.longitude,
                    accuracyMeters = bestLoc.accuracy,
                    timestamp = bestLoc.time,
                    provider = bestLoc.provider ?: "SystemLocation"
                )
                _currentLocation.value = realLoc
                return realLoc
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching device location: ${e.message}")
        }
        return _currentLocation.value
    }

    /**
     * Calculate real geographic distance in kilometers using the Haversine formula.
     */
    fun calculateDistanceKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371.0 // Earth radius in km
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }
}
