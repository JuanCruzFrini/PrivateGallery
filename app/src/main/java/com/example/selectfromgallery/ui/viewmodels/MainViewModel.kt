package com.example.selectfromgallery.ui.viewmodels

import android.content.Context
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

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading:LiveData<Boolean> = _isLoading

    private val _thumbnail = MutableLiveData<ItemEntity>()
    val thumbnail: LiveData<ItemEntity> = _thumbnail

    val _showThumbnail = MutableLiveData<Boolean>()

    fun onCreate(context: Context){
        viewModelScope.launch {
            _isLoading.value = true
            _showThumbnail.value = false
            val result = GetImagesUseCase(context).invoke()

            if (!result.isNullOrEmpty()){
                _listRecycler.postValue(result!!)
                _thumbnail.postValue(result.first())
                _showThumbnail.value = true
                _isLoading.value = false
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