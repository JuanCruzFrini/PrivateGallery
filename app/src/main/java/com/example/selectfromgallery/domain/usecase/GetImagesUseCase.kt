package com.example.selectfromgallery.domain.usecase

import android.content.Context
import android.media.Image
import com.example.selectfromgallery.data.ImageRepository
import com.example.selectfromgallery.data.database.AppDatabase

class GetImagesUseCase(context: Context){

    private val repository = ImageRepository(context)

    operator fun invoke()  = repository.getAllImagesFromDatabase()

}