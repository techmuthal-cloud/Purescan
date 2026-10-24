package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface ScanDao {
    @Query("SELECT * FROM scans ORDER BY createdAt DESC")
    fun getAllScans(): Flow<List<ScanEntity>>

    @Query("SELECT * FROM scans ORDER BY createdAt DESC")
    suspend fun getAllScansList(): List<ScanEntity>

    @Query("SELECT * FROM scans WHERE isSaved = 1 ORDER BY createdAt DESC")
    fun getSavedScans(): Flow<List<ScanEntity>>

    @Query("SELECT * FROM scans WHERE safetyTier = :tier ORDER BY createdAt DESC")
    fun getScansBySafetyTier(tier: String): Flow<List<ScanEntity>>

    @Query(
        """
        SELECT * FROM scans
        WHERE title LIKE '%' || :query || '%'
           OR brandName LIKE '%' || :query || '%'
           OR correctedText LIKE '%' || :query || '%'
           OR allergensDetectedJson LIKE '%' || :query || '%'
        ORDER BY createdAt DESC
        """
    )
    fun searchScans(query: String): Flow<List<ScanEntity>>

    @Query("SELECT * FROM scans WHERE id = :scanId LIMIT 1")
    suspend fun getScanById(scanId: String): ScanEntity?

    @Query("SELECT * FROM scans WHERE batchId = :batchId ORDER BY createdAt ASC")
    suspend fun getScansByBatchId(batchId: String): List<ScanEntity>

    @Query("SELECT * FROM extracted_ingredients WHERE scanId = :scanId ORDER BY positionIndex ASC")
    suspend fun getIngredientsForScan(scanId: String): List<ExtractedIngredientEntity>

    @Query("SELECT * FROM report_findings WHERE scanId = :scanId")
    suspend fun getFindingsForScan(scanId: String): List<FindingEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScan(scan: ScanEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIngredients(ingredients: List<ExtractedIngredientEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFindings(findings: List<FindingEntity>)

    @Query("UPDATE scans SET isSaved = :isSaved WHERE id = :scanId")
    suspend fun updateSavedStatus(scanId: String, isSaved: Boolean)

    @Query("DELETE FROM scans WHERE id = :scanId")
    suspend fun deleteScan(scanId: String)

    @Query("DELETE FROM extracted_ingredients WHERE scanId = :scanId")
    suspend fun deleteIngredientsForScan(scanId: String)

    @Query("DELETE FROM report_findings WHERE scanId = :scanId")
    suspend fun deleteFindingsForScan(scanId: String)

    @Transaction
    suspend fun deleteCompleteScan(scanId: String) {
        deleteFindingsForScan(scanId)
        deleteIngredientsForScan(scanId)
        deleteScan(scanId)
    }

    @Query("DELETE FROM extracted_ingredients")
    suspend fun clearAllIngredients()

    @Query("DELETE FROM report_findings")
    suspend fun clearAllFindings()

    @Query("DELETE FROM scans")
    suspend fun clearAllScans()

    @Transaction
    suspend fun clearCompleteHistory() {
        clearAllFindings()
        clearAllIngredients()
        clearAllScans()
    }
}

@Dao
interface UserAccountDao {
    @Query("SELECT * FROM user_accounts ORDER BY lastLoginAt DESC")
    fun getAllAccounts(): Flow<List<UserAccountEntity>>

    @Query("SELECT * FROM user_accounts WHERE LOWER(email) = LOWER(:email) LIMIT 1")
    suspend fun getAccountByEmail(email: String): UserAccountEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAccount(account: UserAccountEntity)
}

@Dao
interface PreferenceDao {
    @Query("SELECT * FROM user_preferences")
    fun getAllPreferences(): Flow<List<PreferenceSettingEntity>>

    @Query("SELECT * FROM user_preferences")
    suspend fun getPreferencesList(): List<PreferenceSettingEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPreferences(preferences: List<PreferenceSettingEntity>)

    @Query("UPDATE user_preferences SET isEnabled = :isEnabled WHERE `key` = :key")
    suspend fun setPreferenceEnabled(key: String, isEnabled: Boolean)
}

@Dao
interface CustomAllergenDao {
    @Query("SELECT * FROM custom_allergens ORDER BY createdAt ASC")
    fun getAllCustomAllergens(): Flow<List<CustomAllergenEntity>>

    @Query("SELECT * FROM custom_allergens WHERE isEnabled = 1")
    suspend fun getActiveCustomAllergens(): List<CustomAllergenEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomAllergen(allergen: CustomAllergenEntity)

    @Query("DELETE FROM custom_allergens WHERE id = :id")
    suspend fun deleteCustomAllergen(id: String)

    @Query("UPDATE custom_allergens SET isEnabled = :isEnabled WHERE id = :id")
    suspend fun toggleCustomAllergen(id: String, isEnabled: Boolean)

    @Query("UPDATE custom_allergens SET severityLevel = :severityLevel WHERE id = :id")
    suspend fun updateAllergenSeverity(id: String, severityLevel: String)
}
