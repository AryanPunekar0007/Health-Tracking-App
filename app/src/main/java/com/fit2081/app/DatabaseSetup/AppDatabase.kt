package com.fit2081.app.DatabaseSetup

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.fit2081.app.DatabaseSetup.DAOs.FoodIntakeDao
import com.fit2081.app.DatabaseSetup.DAOs.NutriCoachTipsDao
import com.fit2081.app.DatabaseSetup.DAOs.PatientDao
import com.fit2081.app.DatabaseSetup.entities.FoodIntake
import com.fit2081.app.DatabaseSetup.entities.NutriCoachTips
import com.fit2081.app.DatabaseSetup.entities.Patient

@Database(
    entities = [Patient::class, FoodIntake::class, NutriCoachTips::class],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun patientDao(): PatientDao
    abstract fun foodIntakeDao(): FoodIntakeDao
    abstract fun nutriCoachTipsDao(): NutriCoachTipsDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                )
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
