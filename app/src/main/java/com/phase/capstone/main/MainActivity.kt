package com.phase.capstone.main

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.fondesa.kpermissions.allGranted
import com.fondesa.kpermissions.anyPermanentlyDenied
import com.fondesa.kpermissions.extension.permissionsBuilder
import com.fondesa.kpermissions.extension.send
import com.phase.capstone.R
import com.phase.capstone.databinding.ActivityMainBinding
import com.phase.capstone.viewmodels.MapsViewModel
import com.phase.capstone.viewmodels.UserViewModel

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private val mapsViewModel: MapsViewModel by viewModels()
    private val userViewModel: UserViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupNav()
        uploadToken()
        requestPermission()
    }

    private fun setupNav() {
        val navController = findNavController(R.id.mainFragContainer)
        binding.navView.setupWithNavController(navController)

        binding.navView.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.navigation_home -> {
                    navController.popBackStack()
                    navController.navigate(R.id.navigation_home)
                }
                R.id.navigation_maps -> {
                    navController.popBackStack()
                    navController.navigate(R.id.navigation_maps)
                }
                R.id.navigation_profile -> {
                    navController.popBackStack()
                    navController.navigate(R.id.navigation_profile)
                }
            }
            true
        }
    }

    private fun requestPermission(){
        val request = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionsBuilder(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.POST_NOTIFICATIONS
            ).build()
        } else {
            permissionsBuilder(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ).build()
        }

        if (request.checkStatus().allGranted()){
            mapsViewModel.getUserCurrentLocation(this@MainActivity, applicationContext)
        } else {
            request.send { result ->
                if (result.allGranted()) {
                    this@MainActivity.recreate()
                }
                if (result.anyPermanentlyDenied()) {
                    Toast.makeText(
                        applicationContext,
                        "Permission permanently denied, you could grant permission in the settings of the app",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun uploadToken() {
        userViewModel.uploadToken()
    }
}