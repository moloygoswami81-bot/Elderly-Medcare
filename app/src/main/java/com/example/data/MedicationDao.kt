package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicationDao {
    // Profile
    @Query("SELECT * FROM profile WHERE id = 1 LIMIT 1")
    fun getProfileFlow(): Flow<ProfileEntity?>

    @Query("SELECT * FROM profile WHERE id = 1 LIMIT 1")
    suspend fun getProfile(): ProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: ProfileEntity)

    // Medications
    @Query("SELECT * FROM medications ORDER BY hour ASC, minute ASC")
    fun getAllMedicationsFlow(): Flow<List<MedicationEntity>>

    @Query("SELECT * FROM medications ORDER BY hour ASC, minute ASC")
    suspend fun getAllMedications(): List<MedicationEntity>

    @Query("SELECT * FROM medications WHERE isActive = 1")
    suspend fun getActiveMedications(): List<MedicationEntity>

    @Query("SELECT * FROM medications WHERE id = :id LIMIT 1")
    suspend fun getMedicationById(id: Int): MedicationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedication(medication: MedicationEntity): Long

    @Update
    suspend fun updateMedication(medication: MedicationEntity)

    @Delete
    suspend fun deleteMedication(medication: MedicationEntity)

    @Query("DELETE FROM medications WHERE id = :id")
    suspend fun deleteMedicationById(id: Int)

    // Adherence Logs
    @Query("SELECT * FROM adherence_logs ORDER BY scheduledTimestamp DESC")
    fun getAllAdherenceLogsFlow(): Flow<List<AdherenceLogEntity>>

    @Query("SELECT * FROM adherence_logs ORDER BY scheduledTimestamp DESC")
    suspend fun getAllAdherenceLogs(): List<AdherenceLogEntity>

    @Query("SELECT * FROM adherence_logs WHERE status = 'PENDING' ORDER BY scheduledTimestamp ASC")
    suspend fun getPendingLogs(): List<AdherenceLogEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAdherenceLog(log: AdherenceLogEntity): Long

    @Update
    suspend fun updateAdherenceLog(log: AdherenceLogEntity)

    @Query("SELECT * FROM adherence_logs WHERE medicationId = :medicationId AND scheduledTimestamp = :scheduledTimestamp LIMIT 1")
    suspend fun getLogForMedicationAt(medicationId: Int, scheduledTimestamp: Long): AdherenceLogEntity?

    @Query("DELETE FROM adherence_logs")
    suspend fun clearAllLogs()
}
