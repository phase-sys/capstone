package com.phase.capstone.repo

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.phase.capstone.viewmodels.ValidationHandler

class NetworkHandler {
    private val validationHandler = ValidationHandler()

    @SuppressLint("MissingPermission")
    fun checkNetwork(context: Context){
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val capabilities =
            connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        if (capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true ||
            capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true ||
            capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) == true
        ) {
            return
        }
        validationHandler.notifyMessage(context, "Currently not connected to any network", true)
    }
}