package com.fit2081.app.ui.theme

import com.fit2081.app.FruitData
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path



interface APIService {

    // AI help for APIService Interface
    // I used ChatGPT (https://chat.openai.com/) to understand how to save tips using MutableLiveData
    // The tool was used to provide insights on how to use it and in what context

    @GET("api/fruit/{fruitName}")
    suspend fun getFruitInfo(@Path("fruitName") fruitName: String): FruitData

    companion object {

        private const val BASE_URL = "https://www.fruityvice.com/"  // base url (where the info will be obtained from)

        fun create(): APIService {   // Creates a Retrofit instance for the API
            val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())  // Use Gson for JSON parsing
                .build()
            return retrofit.create(APIService::class.java)
        }
    }
}