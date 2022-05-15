package com.example.selectfromgallery.data.database

import androidx.room.*

@Dao
interface ItemDao {

    @Query("SELECT * FROM item_database ORDER BY id DESC")
    fun getAllItems():List<ItemEntity>?

    @Query("SELECT * FROM item_database WHERE id= :id")
    fun selectById(id:Long): ItemEntity

    @Query("DELETE FROM item_database WHERE id= :id")
    fun deleteById(id:Long)

    @Query(value = "DELETE FROM item_database")
    suspend fun deleteAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertItem(item:ItemEntity)

    @Query("UPDATE item_database SET favorito= :favorito WHERE id= :id")
    fun setFav(id: Long, favorito:Boolean)

    @Query("SELECT * FROM item_database WHERE favorito= :state ORDER BY id DESC")
    fun getFavs(state:Boolean):List<ItemEntity>?
}