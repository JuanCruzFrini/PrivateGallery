package com.example.selectfromgallery.ui.view

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.example.selectfromgallery.R
import com.example.selectfromgallery.data.database.AppDatabase
import com.example.selectfromgallery.data.database.ItemEntity
import com.example.selectfromgallery.databinding.ActivityMainBinding
import com.example.selectfromgallery.domain.adapter.ItemAdapter
import com.example.selectfromgallery.ui.viewmodels.MainViewModel
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import java.lang.Exception

class MainActivity : AppCompatActivity() {

    private lateinit var db:AppDatabase
    private lateinit var binding:ActivityMainBinding

    private lateinit var uri: MutableLiveData<Uri>
    private lateinit var itemSelected:ItemEntity

    private val viewModel:MainViewModel by viewModels()
    private lateinit var adapter:ItemAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        uri = MutableLiveData<Uri>()
        uri.value = Uri.EMPTY

        db = AppDatabase.getInstance(this)!!

        setObservers()
        setListeners()
    }

    private fun setListeners() {
        binding.btnGallery.setOnClickListener { requestPermission() }
        binding.btnEnviar.setOnClickListener { enviar() }
        binding.btnBorrar.setOnClickListener { borrarImg() }
        binding.btnAbrirRV.setOnClickListener { startActivity(Intent(this, RecyclerActivtiy::class.java)) }
        binding.btnInsert.setOnClickListener { insert() }
    }

    private fun setObservers(){
        viewModel.onCreate(this)
        viewModel.listaRecycler.observe(this, Observer {
            viewModel.onCreate(this)
            adapter = ItemAdapter(it)
            mainRv.adapter = adapter
            mainRv.layoutManager = GridLayoutManager(this, 2)//LinearLayoutManager(this)
        })
        viewModel.isLoading.observe(this, Observer {
            if (it == true) MainProgress.visibility = View.VISIBLE
            if (it == false) MainProgress.visibility = View.GONE
        })
        viewModel.thumbnail.observe(this, Observer {
            Glide.with(this).load(it.imagen.decodeToString().toUri()).fitCenter().into(btnAbrirRV)
        })

        uri.observe(this, Observer {
            if (it != Uri.EMPTY) showSelectedImagePad()
            else hideSelectedImagePad()
        })
    }

    private fun showSelectedImagePad() {
        binding.image.visibility = View.VISIBLE
        binding.btnEnviar.visibility = View.VISIBLE
        binding.btnBorrar.visibility = View.VISIBLE
        binding.btnInsert.visibility = View.VISIBLE
    }

    private fun hideSelectedImagePad() {
        binding.image.visibility = View.GONE
        binding.btnEnviar.visibility = View.GONE
        binding.btnBorrar.visibility = View.GONE
        binding.btnInsert.visibility = View.GONE
    }

    private fun insert() {
        CoroutineScope((Dispatchers.Main)).launch {
            MainProgress.visibility = View.VISIBLE
            withContext(Dispatchers.Default) { db.itemDao.insertItem(itemSelected) }
            MainProgress.visibility = View.INVISIBLE
            borrarImg()
            viewModel.onCreate(this@MainActivity)
        }
        Toast.makeText(this, "Imagen insertada correctamente", Toast.LENGTH_SHORT).show()
    }

    private fun borrarImg() {
        uri.value = Uri.EMPTY
        binding.image.setImageResource(R.drawable.ic_launcher_foreground)
        binding.btnEnviar.visibility = View.INVISIBLE
        binding.btnBorrar.visibility = View.INVISIBLE
        binding.btnInsert.visibility = View.INVISIBLE
    }

    private fun requestPermission() {//permisos para abrir la gallery
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            when (PackageManager.PERMISSION_GRANTED) {
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) -> {
                    pickPhotoFromGallery()
                }
                else -> requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        } else { pickPhotoFromGallery() }
    }

    //manejamos si se aceptan o no los permisos del Dialog
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()){ isGranted ->

        if (isGranted) pickPhotoFromGallery()
        else  Toast.makeText(this, "You need to enable the permission", Toast.LENGTH_SHORT).show()
    }

    //manejamos el resultado de la actividad a donde nos dirijimos
    private val startForActivityGallery = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result->

        if (result.resultCode == Activity.RESULT_OK){
            val data = result.data?.data
            uri.value = data!!
            itemSelected = ItemEntity(imagen = uri.value.toString().encodeToByteArray())
            Glide.with(this).load(uri.value).fitCenter().into(binding.image)
            showSelectedImagePad()
        }
    }

    private fun pickPhotoFromGallery() { //agarramos la foto de gallery
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).setType("image/*")
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        startForActivityGallery.launch(intent)
        //val intent = Intent(Intent.ACTION_GET_CONTENT)
    }

    private fun enviar() { //enviar img seleccionada de gallery
        val i = Intent(Intent.ACTION_SEND)
            .setType("image/*")
            .putExtra(Intent.EXTRA_STREAM, uri.value)//.setPackage("com.whatsapp")
        val chooser = Intent(Intent.createChooser(i, "Enviar pic"))

        try { startActivity(chooser) }
        catch (e: Exception){ Toast.makeText(this, "Error de envio ${e.message}", Toast.LENGTH_SHORT).show() }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_overflow, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.borrarBtn -> delete()
            else -> {}
        }
        return true
    }

    private fun delete() = AlertDialog.Builder(this)
        .setTitle("ALERTA")
        .setMessage("¿Estas seguro que quieres borrar todo el contenido de la base de datos de SelectFromGallery?")
        .setPositiveButton("Si") { _, _ ->
            viewModel.delete(this)
        }
        .setNegativeButton("No") { _, _ -> }
        .create().show()
}