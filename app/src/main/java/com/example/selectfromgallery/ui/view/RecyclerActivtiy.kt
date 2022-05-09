package com.example.selectfromgallery.ui.view


import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import com.example.selectfromgallery.R
import com.example.selectfromgallery.domain.adapter.ItemAdapter
import com.example.selectfromgallery.ui.viewmodels.RecyclerActivityViewModel
import kotlinx.android.synthetic.main.activity_recycler_activtiy.*

class RecyclerActivtiy : AppCompatActivity() {

    private lateinit var adapter: ItemAdapter
    private val viewModel: RecyclerActivityViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recycler_activtiy)

        setObservers()
    }

    private fun setObservers(){
        viewModel.onCreate(this)
        viewModel.listaRecycler.observe(this, Observer {
            adapter = ItemAdapter(it)
            RV.adapter = adapter
            RV.layoutManager = GridLayoutManager(this, 2)
        })

        viewModel.isLoading.observe(this, Observer {
            if (it == true) loading.visibility = View.VISIBLE
            if (it == false) loading.visibility = View.GONE
        })
    }
}