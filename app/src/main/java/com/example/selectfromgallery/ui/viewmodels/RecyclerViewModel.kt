package com.example.selectfromgallery.ui.viewmodels

import android.content.Context

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.selectfromgallery.data.database.ItemEntity
import com.example.selectfromgallery.domain.usecase.GetFavImagesUseCase
import com.example.selectfromgallery.domain.usecase.GetImagesUseCase
import kotlinx.coroutines.launch

class RecyclerViewModel : ViewModel() {

    private val _listRecycler = MutableLiveData<List<ItemEntity>?>()
    val listaRecycler:LiveData<List<ItemEntity>?> = _listRecycler

    val isLoading = MutableLiveData<Boolean>()

    val seeFavs = MutableLiveData<Boolean>()

    val emptyList = MutableLiveData<Boolean>()
    val emptyFavList = MutableLiveData<Boolean>()

    fun onCreate(context: Context){
        viewModelScope.launch {
            isLoading.value = true
            val result = GetImagesUseCase(context).invoke()

            if (!result.isNullOrEmpty()){
                _listRecycler.postValue(result!!)
                isLoading.value = false
                emptyList.value = false
            }
            if (result.isNullOrEmpty()) {
                _listRecycler.postValue(result)
                emptyList.value = true
                isLoading.value = false
            }
        }
    }

    fun getFavs(context: Context){
        viewModelScope.launch {
            isLoading.value = true
            val result = GetFavImagesUseCase(context).invoke()

            if (!result.isNullOrEmpty()){
                _listRecycler.postValue(result!!)
                emptyList.value = false
                isLoading.value = false
            }
            if (result.isNullOrEmpty()) {
                _listRecycler.postValue(result)
                emptyFavList.value = true
                isLoading.value = false
            }
        }
    }
}