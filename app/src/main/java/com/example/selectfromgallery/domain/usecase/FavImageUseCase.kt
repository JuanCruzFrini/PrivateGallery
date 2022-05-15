package com.example.selectfromgallery.domain.usecase

import android.content.Context
import com.example.selectfromgallery.data.ImageRepository

class FavImageUseCase(context: Context, val id:Long, val favorito:Boolean) {

    private val repository = ImageRepository(context)

    operator fun invoke() = repository.setFav(id, favorito)
}