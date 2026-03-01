package com.cattrack.app.data.local

import androidx.room.*
import com.cattrack.app.data.model.Cat
import kotlinx.coroutines.flow.Flow

@Dao
interface CatDao {

    @Query("SELECT * FROM cats WHERE isActive = 1 ORDER BY createdAt ASC")
    fun getAllActiveCats(): Flow<List<Cat>>

    @Query("SELECT * FROM cats WHERE id = :catId")
    suspend fun getCatById(catId: Long): Cat?

    @Query("SELECT * FROM cats WHERE id = :catId")
    fun getCatByIdFlow(catId: Long): Flow<Cat?>

    @Query("SELECT * FROM cats WHERE deviceId = :deviceId LIMIT 1")
    suspend fun getCatByDeviceId(deviceId: String): Cat?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCat(cat: Cat): Long

    @Update
    suspend fun updateCat(cat: Cat)

    @Query("UPDATE cats SET isActive = 0 WHERE id = :catId")
    suspend fun softDeleteCat(catId: Long)

    @Query("UPDATE cats SET deviceId = :deviceId WHERE id = :catId")
    suspend fun bindDevice(catId: Long, deviceId: String)

    @Query("UPDATE cats SET deviceId = '' WHERE id = :catId")
    suspend fun unbindDevice(catId: Long)

    @Query("UPDATE cats SET weight = :weight WHERE id = :catId")
    suspend fun updateWeight(catId: Long, weight: Float)

    @Query("SELECT COUNT(*) FROM cats WHERE isActive = 1")
    suspend fun getActiveCatCount(): Int
}
