package com.example.selectfromgallery.data.database

import android.content.Context
import androidx.room.*

@Database(entities = [ItemEntity::class], version = 1, exportSchema = false )
@TypeConverters
abstract class AppDatabase : RoomDatabase() {
    abstract val itemDao:ItemDao

    companion object {
        const val DATABASE_NAME = "db-imagenes"

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase? {
            INSTANCE ?: synchronized(this) {
                INSTANCE = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                ).allowMainThreadQueries().build()
            }
            return INSTANCE
        }
    }
}