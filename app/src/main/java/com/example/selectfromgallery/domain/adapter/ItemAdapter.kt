package com.example.selectfromgallery.domain.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.selectfromgallery.R
import com.example.selectfromgallery.data.database.AppDatabase
import com.example.selectfromgallery.data.database.ItemEntity
import com.example.selectfromgallery.ui.view.DetailActivity
import com.example.selectfromgallery.ui.view.hide
import com.example.selectfromgallery.ui.view.show
import kotlinx.android.synthetic.main.item_list.view.*
import kotlinx.coroutines.delay
import java.lang.Thread.sleep
import kotlin.time.Duration.Companion.seconds

open class ItemAdapter(private val itemList:List<ItemEntity>) : RecyclerView.Adapter<ItemAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_list, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        //holder.txt.text = itemList[position].id.toString()
        //holder.imagen.setImageURI(itemList[position].imagen.decodeToString().toUri()

        Glide //al usar glide la carga es mas fluida
            .with(holder.itemView.context)
            .load(itemList[position].imagen.decodeToString().toUri())
            .placeholder(R.drawable.ic_launcher_foreground)
            .error(R.drawable.ic_launcher_foreground).centerCrop()
            .into(holder.imagen)

        if (itemList[position].favorito == true){
            holder.fav.show()//visibility = View.VISIBLE
            holder.noFav.hide()//visibility = View.GONE
        }
    }

    override fun getItemCount(): Int = itemList.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val imagen = itemView.itemImg
        val txt = itemView.itemTxtId
        val fav = itemView.itemFav
        val noFav = itemView.itemNoFav

        val db = AppDatabase.getInstance(itemView.context)

        init {
           setListeners()
        }

        private fun setListeners(){
            imagen.setOnClickListener{ goToDetail() }
            fav.setOnClickListener { fav() }
            noFav.setOnClickListener{ noFav() }
        }

        private fun goToDetail(){
            val context = itemView.context
            context.startActivity(Intent(context, DetailActivity::class.java)
                .putExtra("ID", itemList[adapterPosition].id))
        }

        private fun noFav(){
            fav.setImageResource(R.drawable.fav_icon)
            fav.show()//visibility = View.VISIBLE
            noFav.hide()//visibility = View.GONE
            db!!.itemDao.setFav(itemList[absoluteAdapterPosition].id.toLong(), true)
            fav.animate().alpha(1f).scaleX(2f).scaleX(1f).duration = 500
            noFav.animate().alpha(0f).scaleX(2f).scaleX(1f).duration = 500
        }

        private fun fav(){
            fav.setImageResource(R.drawable.fav_border_icon)
            fav.hide()//visibility = View.GONE
            noFav.show()//visibility = View.VISIBLE
            db!!.itemDao.setFav(itemList[absoluteAdapterPosition].id.toLong(), false)
            fav.animate().alpha(0f).scaleX(2f).scaleX(1f).duration = 500
            noFav.animate().alpha(1f).scaleX(2f).scaleX(1f).duration = 500
        }
    }
}