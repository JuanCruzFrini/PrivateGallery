package com.example.selectfromgallery.ui.view


import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import com.example.selectfromgallery.R
import com.example.selectfromgallery.domain.adapter.ItemAdapter
import com.example.selectfromgallery.ui.viewmodels.RecyclerViewModel
import kotlinx.android.synthetic.main.activity_recycler_activtiy.*

class RecyclerActivtiy : AppCompatActivity() {

    private lateinit var adapter: ItemAdapter
    private val viewModel: RecyclerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recycler_activtiy)

        setObservers()
        setListeners()
    }

    private fun setListeners() {
        cbFav.setOnClickListener {
            if (cbFav.isChecked) viewModel.seeFavs.value = true
            if (!cbFav.isChecked) viewModel.seeFavs.postValue(false)
        }
    }

    private fun setObservers(){
        viewModel.onCreate(this)
        viewModel.listaRecycler.observe(this, Observer {
                adapter = ItemAdapter(it!!)
                RV.adapter = adapter
                RV.layoutManager = GridLayoutManager(this, 2)
        })
        viewModel.isLoading.observe(this, Observer {
            if (it == true) loading.visibility = View.VISIBLE
            if (it == false) loading.visibility = View.GONE
        })
        viewModel.seeFavs.observe(this, Observer {
            if (it == true) viewModel.getFavs(this)
            if (it == false) viewModel.onCreate(this)
        })
        /*viewModel.emptyFavList.observe(this, Observer {
            if (it == true) txtEmptyListAdvice.visibility = View.VISIBLE
            if (it == false) txtEmptyListAdvice.visibility = View.GONE
        })*/
    }
}