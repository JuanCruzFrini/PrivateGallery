package com.example.selectfromgallery.domain.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.selectfromgallery.R
import com.example.selectfromgallery.data.database.ItemEntity
import com.example.selectfromgallery.ui.view.DetailActivity
import kotlinx.android.synthetic.main.item_list.view.*

class ItemAdapter(private val itemList:List<ItemEntity>) : RecyclerView.Adapter<ItemAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_list, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.txt.text = itemList[position].id.toString()
        //holder.imagen.setImageURI(itemList[position].imagen.decodeToString().toUri()

        Glide //al usar glide la carga es mas fluida
            .with(holder.itemView.context)
            .load(itemList[position].imagen.decodeToString().toUri())
            .placeholder(R.drawable.ic_launcher_foreground)
            .error(R.drawable.ic_launcher_foreground)
            .into(holder.imagen)
    }

    override fun getItemCount(): Int = itemList.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val imagen = itemView.itemImg
        val txt = itemView.itemTxtId

        init {
            itemView.setOnClickListener{
                val context = itemView.context
                context.startActivity(Intent(context, DetailActivity::class.java)
                    .putExtra("ID", itemList[adapterPosition].id))
            }
        }
    }
}