package com.phase.capstone.service

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.content.Context
import android.os.Looper
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.*
import com.phase.capstone.repo.NetworkHandler
import com.phase.capstone.viewmodels.GroupViewModel
import com.phase.capstone.viewmodels.MapsViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*

class LocationHandler: ViewModel() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val mapsViewModel = MapsViewModel()
    private val groupViewModel = GroupViewModel()
    private val networkHandler = NetworkHandler()

    var listenerActive = false
    fun listenCurrentLocation(context: Context){
        viewModelScope.launch {
            while (listenerActive){
                getCurrentUserLocation(context)
                delay(10000)
            }
        }
    }

    fun listenToGroupLocation(context: Context, CHANNEL_ID: String, notificationManager: NotificationManager){
        viewModelScope.launch {
            while (listenerActive){
                try {
                    groupViewModel.getToTrackGroup(context, CHANNEL_ID, notificationManager)
                } catch (e: Exception) {
                    networkHandler.checkNetwork(context)
                }
                delay(30000)
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun getCurrentUserLocation(context: Context){

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 100).build()

        LocationServices.getFusedLocationProviderClient(context)
            .requestLocationUpdates(locationRequest, object : LocationCallback(){
                override fun onLocationResult(p0: LocationResult) {
                    super.onLocationResult(p0)
                    LocationServices.getFusedLocationProviderClient(context)
                        .removeLocationUpdates(this)
                    if (p0.locations.size > 0){
                        val locIndex = p0.locations.size-1

                        val latitude = p0.locations[locIndex].latitude
                        val longitude = p0.locations[locIndex].longitude

                        try {
                            mapsViewModel.uploadLocation(context, longitude, latitude)
                        } catch (e: Exception) {
                            networkHandler.checkNetwork(context)
                        }

                    }
                }
            }, Looper.getMainLooper())
    }
}