package com.fit2081.app.DatabaseSetup.Repos

import com.fit2081.app.FruitData
import com.fit2081.app.ui.theme.APIService

class FruitRepo(private val apiService: APIService) {

    suspend fun getFruitInfo(fruitName: String): Result<FruitData> {
        return try {
            val response = apiService.getFruitInfo(fruitName)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
