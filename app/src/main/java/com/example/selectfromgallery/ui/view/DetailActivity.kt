package com.example.selectfromgallery.ui.view

import android.app.VoiceInteractor
import android.content.Intent
import android.opengl.Matrix
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.StrictMode
import android.view.ScaleGestureDetector
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.biometric.BiometricPrompt
import androidx.core.net.toUri
import com.bumptech.glide.Glide
import com.example.selectfromgallery.data.database.AppDatabase
import com.example.selectfromgallery.data.database.ItemEntity
import com.example.selectfromgallery.databinding.ActivityDetailBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.Exception

class DetailActivity : AppCompatActivity(){

    private lateinit var binding: ActivityDetailBinding
    private lateinit var imageEntity:ItemEntity
    private lateinit var db:AppDatabase
    private var id:Long = 1L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        id = intent.extras?.get("ID").toString().toLong() ?: 1L
        db = AppDatabase.getInstance(this)!!

        setImage()
        setListeners()
    }

    private fun setImage() {
        imageEntity = db.itemDao.selectById(id)
        Glide.with(this).load(imageEntity.imagen.decodeToString().toUri()).into(binding.image)
        binding.txtDate.text = imageEntity.fecha
    }

    private fun setListeners() {
        binding.btnEnviar.setOnClickListener { send() }
        binding.btnDelete.setOnClickListener { alertDelete() }
        binding.btnInfo.setOnClickListener { showInfo() }
        binding.image.setOnClickListener { if (info) hideExtraInfo() else showExtraInfo() }
    }

    private fun showInfo() {
        AlertDialog.Builder(this)
            .setTitle("Informacion:")
            .setMessage(
                "ID: ${imageEntity.id}\n" +
                        "Favorito: ${imageEntity.favorito}\n" +
                        "Fecha de carga: ${imageEntity.fecha}\n" +
                        "URI: ${imageEntity.imagen.decodeToString()}"
            )
            .setNeutralButton("Aceptar") { _, _ -> }
            .create().show()
    }

    private fun delete(){
        CoroutineScope(Dispatchers.Main).launch {
            withContext(Dispatchers.IO){ db.itemDao.deleteById(id) }
            onBackPressed()
        }
    }

    private fun alertDelete(){
        AlertDialog.Builder(this)
            .setTitle("Adevertencia")
            .setMessage("¿Estas seguro que quieres eliminar este elemento de tu Private Gallery?")
            .setPositiveButton("SI") { _, _ -> delete() }
            .setNegativeButton("No") { _, _ -> }
            .create().show()
    }

    private fun send() {
        StrictMode.setVmPolicy(StrictMode.VmPolicy.Builder().build())
        val intent = Intent(Intent.ACTION_SEND)
            .setType("image/*")
            .putExtra(Intent.EXTRA_STREAM, imageEntity.imagen.decodeToString().toUri())
            .putExtra(Intent.EXTRA_TEXT, "Message sent from Private Gallery")
        val chooser = Intent.createChooser(intent, "Titulo")
        try { startActivity(chooser) }
        catch (e:Exception) { Toast.makeText(this, "Error : ${e.message}", Toast.LENGTH_SHORT).show()}
    }

    var info = true
    private fun showExtraInfo() {
        binding.bottomAppBar.visibility = View.VISIBLE
        binding.txtDate.visibility = View.VISIBLE
        info = true
    }
    private fun hideExtraInfo() {
        binding.bottomAppBar.visibility = View.GONE
        binding.txtDate.visibility = View.GONE
        info = false
    }
}