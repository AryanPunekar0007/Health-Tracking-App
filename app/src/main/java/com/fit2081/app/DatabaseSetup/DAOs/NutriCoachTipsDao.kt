package com.fit2081.app.DatabaseSetup.DAOs

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.fit2081.app.DatabaseSetup.entities.NutriCoachTips
import kotlinx.coroutines.flow.Flow

@Dao
interface NutriCoachTipsDao {
    @Insert
    suspend fun insertTip(tip: NutriCoachTips)

    @Query("SELECT * FROM nutri_coach_tips WHERE userId = :userId ORDER BY date DESC")
    fun getTipsByUser(userId: String): Flow<List<NutriCoachTips>>
}