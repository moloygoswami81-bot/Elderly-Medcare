package com.example.data

import kotlinx.coroutines.flow.Flow

class MedicationRepository(private val medicationDao: MedicationDao) {
    val profileFlow: Flow<ProfileEntity?> = medicationDao.getProfileFlow()
    val allMedicationsFlow: Flow<List<MedicationEntity>> = medicationDao.getAllMedicationsFlow()
    val allAdherenceLogsFlow: Flow<List<AdherenceLogEntity>> = medicationDao.getAllAdherenceLogsFlow()

    suspend fun getProfile(): ProfileEntity? = medicationDao.getProfile()
    suspend fun saveProfile(profile: ProfileEntity) {
        medicationDao.insertProfile(profile)
    }

    suspend fun getAllMedications(): List<MedicationEntity> = medicationDao.getAllMedications()
    suspend fun getActiveMedications(): List<MedicationEntity> = medicationDao.getActiveMedications()
    suspend fun getMedicationById(id: Int): MedicationEntity? = medicationDao.getMedicationById(id)
    suspend fun saveMedication(medication: MedicationEntity): Long = medicationDao.insertMedication(medication)
    suspend fun updateMedication(medication: MedicationEntity) {
        medicationDao.updateMedication(medication)
    }

    suspend fun deleteMedication(medication: MedicationEntity) {
        medicationDao.deleteMedication(medication)
    }

    suspend fun deleteMedicationById(id: Int) {
        medicationDao.deleteMedicationById(id)
    }

    suspend fun getAllAdherenceLogs(): List<AdherenceLogEntity> = medicationDao.getAllAdherenceLogs()
    suspend fun getPendingLogs(): List<AdherenceLogEntity> = medicationDao.getPendingLogs()
    suspend fun saveAdherenceLog(log: AdherenceLogEntity): Long = medicationDao.insertAdherenceLog(log)
    suspend fun updateAdherenceLog(log: AdherenceLogEntity) {
        medicationDao.updateAdherenceLog(log)
    }

    suspend fun getLogForMedicationAt(medicationId: Int, scheduledTimestamp: Long): AdherenceLogEntity? {
        return medicationDao.getLogForMedicationAt(medicationId, scheduledTimestamp)
    }

    suspend fun clearAllLogs() {
        medicationDao.clearAllLogs()
    }
}
