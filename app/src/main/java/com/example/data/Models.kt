package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "profile")
data class ProfileEntity(
    @PrimaryKey val id: Int = 1,
    val name: String = "Grandpa Ram",
    val age: Int = 78,
    val language: String = "EN", // "EN" (English), "HI" (Hindi), "BN" (Bengali)
    val emergencyPhone: String = "1234567890",
    val isLightMode: Boolean = false
)

@Entity(tableName = "medications")
data class MedicationEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val dosage: String,
    val hour: Int, // 24hr format
    val minute: Int,
    val isActive: Boolean = true,
    val customVoicePhrase: String = "taken"
)

@Entity(tableName = "adherence_logs")
data class AdherenceLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val medicationId: Int,
    val medName: String,
    val scheduledTimestamp: Long,
    val takenTimestamp: Long? = null,
    val status: String, // "PENDING", "TAKEN", "MISSED"
    val confirmationType: String = "NONE" // "NONE", "MANUAL", "VOICE"
)
