package com.example.selectfromgallery.data.database

import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "item_database")
data class ItemEntity(

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    @NonNull
    val id:Int= 0,

    @ColumnInfo(name = "imagen")
    @NonNull
    val imagen: ByteArray,

    @ColumnInfo(name = "fecha")
    val fecha:String
)
