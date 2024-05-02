package com.phase.capstone.repo

import java.sql.Timestamp

data class LocationGetMeta(
    val nickname: String,
    val longitude: Double,
    val latitude: Double,
    val timestamp: Timestamp
)
