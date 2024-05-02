package com.phase.capstone.repo

data class LocationMeta (
    val nickname: String,
    val longitude: Double,
    val latitude: Double,
    val timestamp: MutableMap<String, String>
)