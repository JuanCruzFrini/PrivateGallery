package com.example.selectfromgallery.ui.viewmodels

import android.content.Context

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.selectfromgallery.data.ImageRepository
import com.example.selectfromgallery.data.database.ItemEntity
import com.example.selectfromgallery.domain.usecase.GetImagesUseCase
import kotlinx.coroutines.launch

class RecyclerActivityViewModel : ViewModel() {

    private val _listRecycler = MutableLiveData<List<ItemEntity>>()
    val listaRecycler:LiveData<List<ItemEntity>> = _listRecycler

    val isLoading = MutableLiveData<Boolean>()

    fun onCreate(context: Context){
        viewModelScope.launch {
            isLoading.value = true
            val result = GetImagesUseCase(context).invoke()

            if (!result.isNullOrEmpty()){
                _listRecycler.postValue(result!!)
                isLoading.value = false
            }
        }
    }
}