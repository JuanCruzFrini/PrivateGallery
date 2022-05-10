package com.example.selectfromgallery.data.database

import androidx.room.*

@Dao
interface ItemDao {

    @Query("SELECT * FROM item_database ORDER BY id DESC")
    fun getAllItems():List<ItemEntity>?

    @Query("DELETE FROM item_database WHERE id= :id")
    fun deleteById(id:Long)

    @Query(value = "DELETE FROM item_database")
    suspend fun deleteAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertItem(item:ItemEntity)
}