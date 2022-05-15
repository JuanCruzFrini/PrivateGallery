package com.example.selectfromgallery.data

import android.content.Context
import androidx.room.Room
import com.example.selectfromgallery.data.database.AppDatabase
import com.example.selectfromgallery.data.database.ItemDao
import com.example.selectfromgallery.data.database.ItemEntity
import dagger.hilt.android.internal.modules.ApplicationContextModule
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlin.coroutines.coroutineContext

class ImageRepository(context: Context) {

    val db = AppDatabase.getInstance(context)
    val itemDao = db!!.itemDao

    fun getAllImagesFromDatabase(): List<ItemEntity>? {
        return itemDao.getAllItems()
    }

    suspend fun deleteAll(){
        itemDao.deleteAll()
    }

    fun setFav(id:Long, favorito:Boolean ){
        itemDao.setFav(id,favorito)
    }
}
