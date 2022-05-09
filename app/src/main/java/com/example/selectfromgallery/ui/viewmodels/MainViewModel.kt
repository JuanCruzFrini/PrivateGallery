package com.example.selectfromgallery.ui.viewmodels

import android.content.Context
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.selectfromgallery.data.ImageRepository
import com.example.selectfromgallery.data.database.ItemEntity
import com.example.selectfromgallery.domain.usecase.GetImagesUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {

    private val _listRecycler = MutableLiveData<List<ItemEntity>>()
    val listaRecycler: LiveData<List<ItemEntity>> = _listRecycler

    val isLoading = MutableLiveData<Boolean>()

    private val _thumbnail = MutableLiveData<ItemEntity>()
    val thumbnail: LiveData<ItemEntity> = _thumbnail

    fun onCreate(context: Context){
        viewModelScope.launch {
            isLoading.value = true
            val result = GetImagesUseCase(context).invoke()

            if (!result.isNullOrEmpty()){
                _listRecycler.postValue(result!!)
                _thumbnail.postValue(result.first())
                isLoading.value = false
            }
        }
    }

    fun delete(context: Context){
        viewModelScope.launch(Dispatchers.IO) {
            ImageRepository(context).deleteAll()
            _listRecycler.postValue(GetImagesUseCase(context).invoke())
        }
    }
}