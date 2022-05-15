package com.example.selectfromgallery.domain.usecase

import android.content.Context
import com.example.selectfromgallery.data.ImageRepository

class GetFavImagesUseCase(context:Context) {

    val repository = ImageRepository(context)
    operator fun invoke() = repository.getFavs()
}