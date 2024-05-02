package com.phase.capstone.viewmodels

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.phase.capstone.repo.LocationGetMeta
import com.phase.capstone.repo.LocationRepository
import com.phase.capstone.repo.NetworkHandler
import com.phase.capstone.service.LocationForegroundService
import kotlinx.coroutines.launch

class MapsViewModel: ViewModel() {

    private val networkHandler = NetworkHandler()
    private val locRepo = LocationRepository()

    fun getUserCurrentLocation(activity: Activity, context: Context){
        viewModelScope.launch {
            try {
                val intent = Intent(context, LocationForegroundService::class.java)
                activity.startForegroundService(intent)
            } catch (e: Exception) {
                networkHandler.checkNetwork(context)
            }
        }
    }

    private val _homeLocation = MutableLiveData<LatLng>()
    val homeLocation = _homeLocation

    fun uploadLocation(context: Context, longitude: Double, latitude: Double) {
        viewModelScope.launch {
            try {
                locRepo.uploadLocation(longitude, latitude)
            } catch (e: Exception) {
                networkHandler.checkNetwork(context)
            }
        }
    }

    private val _memberLocation = MutableLiveData<List<LocationGetMeta>>()
    val memberLocation = _memberLocation

    fun getLocationFromDB(context: Context) {
        viewModelScope.launch {
            try {
                _memberLocation.value = locRepo.getMemberLocationFromDB()
            } catch (e: Exception) {
                networkHandler.checkNetwork(context)
            }
        }
    }

    private val _userLocation = MutableLiveData<List<LocationGetMeta>>()
    val userLocation = _userLocation

    fun getUserLocationFromDB(context: Context, userId: String) {
        viewModelScope.launch {
            try {
                _userLocation.value = locRepo.getUserLocationFromDB(userId)
            } catch (e: Exception) {
                networkHandler.checkNetwork(context)
            }
        }
    }
}