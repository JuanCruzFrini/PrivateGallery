package com.example.selectfromgallery.ui.view


import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import com.example.selectfromgallery.R
import com.example.selectfromgallery.databinding.ActivityRecyclerActivtiyBinding
import com.example.selectfromgallery.domain.adapter.ItemAdapter
import com.example.selectfromgallery.ui.viewmodels.RecyclerViewModel
import kotlinx.android.synthetic.main.activity_recycler_activtiy.*

class RecyclerActivtiy : AppCompatActivity() {

    private lateinit var adapter: ItemAdapter
    private val viewModel: RecyclerViewModel by viewModels()
    private lateinit var binding:ActivityRecyclerActivtiyBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecyclerActivtiyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSwipeRefreshListener()
        setObservers()
        setListeners()
    }

    private fun setSwipeRefreshListener() {
        val swipe = binding.swipeLayout.apply {
            setColorSchemeColors(resources.getColor(R.color.coral))
        }
        swipe.setOnRefreshListener {
            setObservers()
            swipe.isRefreshing = false
        }
    }

    private fun setListeners() {
        cbFav.setOnClickListener {
            if (binding.cbFav.isChecked) viewModel.seeFavs.value = true
            if (!binding.cbFav.isChecked) viewModel.seeFavs.postValue(false)
        }
    }

    private fun setObservers(){
        viewModel.onCreate(this)
        viewModel.listaRecycler.observe(this, Observer {
            adapter = ItemAdapter(it!!)
            binding.rvGallery.adapter = adapter
            binding.rvGallery.layoutManager = GridLayoutManager(this, 2)
        })
        viewModel.isLoading.observe(this, Observer {
            if (it == true) binding.loading.visibility = View.VISIBLE
            if (it == false) binding.loading.visibility = View.GONE
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