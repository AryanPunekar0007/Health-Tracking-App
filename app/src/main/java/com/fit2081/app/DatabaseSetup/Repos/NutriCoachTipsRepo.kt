package com.fit2081.app.DatabaseSetup.Repos

import android.content.Context
import com.fit2081.app.DatabaseSetup.AppDatabase
import com.fit2081.app.DatabaseSetup.entities.NutriCoachTips
import kotlinx.coroutines.flow.Flow

class NutriCoachTipsRepo(context: Context) {
    private val tipsDao = AppDatabase.getDatabase(context).nutriCoachTipsDao()

    suspend fun insertTip(tip: NutriCoachTips) {
        tipsDao.insertTip(tip)
    }

    fun getTipsByUser(userId: String): Flow<List<NutriCoachTips>> {
        return tipsDao.getTipsByUser(userId)
    }


}
