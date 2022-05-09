package com.example.selectfromgallery.di

import android.content.Context
import androidx.room.ProvidedAutoMigrationSpec
import androidx.room.Room
import com.bumptech.glide.annotation.GlideModule
import com.example.selectfromgallery.data.database.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RoomModule {

    @Singleton
    @Provides
    fun provideRoom(@ApplicationContext context: Context) =
        Room.databaseBuilder(context, AppDatabase::class.java, AppDatabase.DATABASE_NAME).build()

    @Singleton
    @Provides
    fun provideDao(db:AppDatabase) = db.itemDao.getAllItems()
}