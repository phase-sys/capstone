package com.phase.capstone.repo

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await


@Suppress("UNCHECKED_CAST")
class LocationRepository {

    private val auth: FirebaseAuth = Firebase.auth
    private lateinit var dbRef: DatabaseReference

    private val groupRepo = GroupRepository()
    private val userRepo = UserRepository()

    suspend fun getUserLocationFromDB(userId: String): MutableList<LocationGetMeta> {
        dbRef = FirebaseDatabase.getInstance(
            "https://capstoneproject-8478a-default-rtdb.asia-southeast1.firebasedatabase.app/"
        ).reference

        val userLocation: MutableList<LocationGetMeta> = mutableListOf()

        try {
            dbRef.child("location").get().addOnSuccessListener { parent ->
                parent.children.map {
                    if (it.key == auth.currentUser?.uid!! || it.key == userId) {
                        userLocation.add(
                            LocationGetMeta(
                                it.child("nickname").value.toString(),
                                it.child("longitude").value.toString().toDouble(),
                                it.child("latitude").value.toString().toDouble(),
                                java.sql.Timestamp(it.child("timestamp").value.toString().toLong())
                            )
                        )
                    }
                }
            }.await()

        } catch (e: Exception) {
            Log.i("TAG", "getUserLocationFromDB: $e")
        }

        return userLocation
    }

    suspend fun getMemberLocationFromDB(): MutableList<LocationGetMeta> {
        dbRef = FirebaseDatabase.getInstance(
            "https://capstoneproject-8478a-default-rtdb.asia-southeast1.firebasedatabase.app/"
        ).reference

        val memberLocation: MutableList<LocationGetMeta> = mutableListOf()

        try {
            val userMemberList = groupRepo.getMemberList()

            dbRef.child("location").get().addOnSuccessListener { parent ->
                userMemberList.forEach { user ->
                    parent.children.map {
                        if (it.key == user) {
                            memberLocation.add(
                                LocationGetMeta(
                                    it.child("nickname").value.toString(),
                                    it.child("longitude").value.toString().toDouble(),
                                    it.child("latitude").value.toString().toDouble(),
                                    java.sql.Timestamp(it.child("timestamp").value.toString().toLong())
                                )
                            )
                        }
                    }
                }
            }.await()
        } catch (e: Exception) {
            Log.i("TAG", "getMemberLocationFromDB: $e")
        }

        return memberLocation
    }

    suspend fun uploadLocation(longitude: Double, latitude: Double) {
        try {
            uploadLocationDB(auth.currentUser?.uid!!, longitude, latitude)
        } catch (e: Exception) {
            Log.i("TAG", "uploadLocation$e")
        }
    }

    private suspend fun uploadLocationDB(uid: String, longitude: Double, latitude: Double) {
        dbRef = FirebaseDatabase.getInstance(
            "https://capstoneproject-8478a-default-rtdb.asia-southeast1.firebasedatabase.app/"
        )
            .reference

        dbRef.child("location").child(uid).setValue(
            LocationMeta(
                userRepo.getUserState("").nickname,
                longitude,
                latitude,
                ServerValue.TIMESTAMP
            )
        )
    }

    suspend fun getTrackingMemberLocationFromDB(): MutableList<LocationGetMeta> {
        dbRef = FirebaseDatabase.getInstance(
            "https://capstoneproject-8478a-default-rtdb.asia-southeast1.firebasedatabase.app/"
        ).reference

        val trackMemberLocation: MutableList<LocationGetMeta> = mutableListOf()

        try {
            val trackMemberList = groupRepo.getTrackMemberList()

            dbRef.child("location").get().addOnSuccessListener { parent ->
                trackMemberList.forEach { user ->
                    parent.children.map {
                        if (it.key == user && it.key != auth.currentUser?.uid!!) {
                            trackMemberLocation.add(
                                LocationGetMeta(
                                    it.child("nickname").value.toString(),
                                    it.child("longitude").value.toString().toDouble(),
                                    it.child("latitude").value.toString().toDouble(),
                                    java.sql.Timestamp(it.child("timestamp").value.toString().toLong())
                                )
                            )
                        }
                    }
                }
            }.await()
        } catch (e: Exception) {
            Log.i("TAG", "getMemberLocationFromDB: $e")
        }

        return trackMemberLocation
    }

    suspend fun getCurrentUserLocationFromDB(): Map<String, String> {
        dbRef = FirebaseDatabase.getInstance(
            "https://capstoneproject-8478a-default-rtdb.asia-southeast1.firebasedatabase.app/"
        ).reference

        val trackUserLocation = mutableMapOf<String, String>()

        try {
            dbRef.child("location").child(auth.currentUser?.uid!!).get().addOnSuccessListener {
                if (it.exists()) {
                    trackUserLocation["longitude"] = it.child("longitude").value.toString()
                    trackUserLocation["latitude"] = it.child("latitude").value.toString()
                }
            }.await()
        } catch (e: Exception) {
            Log.i("TAG", "getMemberLocationFromDB: $e")
        }

        return trackUserLocation
    }
}
