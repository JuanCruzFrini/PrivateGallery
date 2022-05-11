package com.example.selectfromgallery.ui.view

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.StrictMode
import android.os.StrictMode.VmPolicy
import android.provider.MediaStore
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.example.selectfromgallery.BuildConfig
import com.example.selectfromgallery.R
import com.example.selectfromgallery.data.database.AppDatabase
import com.example.selectfromgallery.data.database.ItemEntity
import com.example.selectfromgallery.databinding.ActivityMainBinding
import com.example.selectfromgallery.domain.adapter.ItemAdapter
import com.example.selectfromgallery.ui.viewmodels.MainViewModel
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var db:AppDatabase
    private lateinit var binding:ActivityMainBinding

    private lateinit var uri: MutableLiveData<Uri>
    private lateinit var itemSelected:ItemEntity

    private lateinit var adapter:ItemAdapter
    private val viewModel:MainViewModel by viewModels()

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
        binding.btnGallery.setOnClickListener { requestReadExtStorPermission() }
        binding.btnEnviar.setOnClickListener { enviar() }
        binding.btnBorrar.setOnClickListener { borrarImg() }
        binding.btnAbrirRV.setOnClickListener { startActivity(Intent(this, RecyclerActivtiy::class.java)) }
        binding.btnInsert.setOnClickListener { insert() }
        binding.btnCamera.setOnClickListener{ requestCameraPermission() }
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
        viewModel._showThumbnail.observe(this, Observer {
            if (it == true) btnAbrirRV.visibility = View.VISIBLE
            if (it == false) btnAbrirRV.visibility = View.GONE
        })
        uri.observe(this, Observer {
            if (it != Uri.EMPTY){ showSelectedImagePad() }
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
            hideSelectedImagePad()
            viewModel.onCreate(this@MainActivity)
        }
        Toast.makeText(this, "Imagen insertada correctamente", Toast.LENGTH_SHORT).show()
    }

    private fun borrarImg() {
        uri.value = Uri.EMPTY
        hideSelectedImagePad()
        if (file != null) file?.absoluteFile?.delete()
    }

    private fun deleteAll() = AlertDialog.Builder(this)
        .setTitle("ALERTA")
        .setMessage("¿Estas seguro que quieres borrar todo el contenido de la base de datos de SelectFromGallery?")
        .setPositiveButton("Si") { _, _ -> viewModel.delete(this) }
        .setNegativeButton("No") { _, _ -> }
        .create().show()

    private fun enviar() { //enviar img seleccionada de gallery
        val i = Intent(Intent.ACTION_SEND)
            .setType("image/*")
            .putExtra(Intent.EXTRA_STREAM, uri.value)
            .putExtra(Intent.EXTRA_TEXT, "Message sent from PrivateGallery")//.setPackage("com.whatsapp")
        val chooser = Intent(Intent.createChooser(i, "Enviar pic"))
        println("URI = ${uri.value.toString()}")

        try { startActivity(chooser) }
        catch (e: Exception){ Toast.makeText(this, "Error de envio ${e.message}", Toast.LENGTH_SHORT).show() }
    }

    //camera functions
    private fun requestCameraPermission(){
        when (PackageManager.PERMISSION_GRANTED){
            ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) -> { openCamera() }
            else -> requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }
    private val requestCameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) openCamera()
        else Toast.makeText(this, "Permite el uso de Camara para continuar", Toast.LENGTH_SHORT).show()
    }
    private val startForActivityCamera = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result ->
        if (result.resultCode == Activity.RESULT_OK){
            uri.value = file!!.toUri()
            itemSelected = ItemEntity(imagen = uri.value.toString().encodeToByteArray())
            Glide.with(this).load(uri.value).fitCenter().into(binding.image)
            showSelectedImagePad()
            println("FOTO startForCamera = ${uri.value}")
            val palette = createPaletteSync(BitmapFactory.decodeFile(file.toString()))
            val colors:List<Palette.Swatch>? = palette.swatches
            image.setBackgroundColor(colors?.random()?.rgb ?: R.color.purple_500 )
        }
    }

    //Gallery functions
    private fun requestReadExtStorPermission() {//permisos para abrir la gallery
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            when (PackageManager.PERMISSION_GRANTED) {
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) -> {
                    pickPhotoFromGallery()
                }
                else -> requestReadExtStorPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        } else { pickPhotoFromGallery() }
    }
    private val requestReadExtStorPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()){ isGranted ->
        if (isGranted) pickPhotoFromGallery()
        else  Toast.makeText(this, "Permite el uso de Escritura de almacenamiento para continuar", Toast.LENGTH_LONG).show()
    }
    private val startForActivityGallery = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result->
        if (result.resultCode == Activity.RESULT_OK){
            val data = result.data?.data
            if (data == null){
                uri.value = file!!.toUri()
            } else {
                uri.value = data!!
            }
            itemSelected = ItemEntity(imagen = uri.value.toString().encodeToByteArray())
            Glide.with(this).load(uri.value).fitCenter().into(binding.image)
            showSelectedImagePad()
            println("FOTO startForGallery= ${uri.value}")
        }
    }

    private fun pickPhotoFromGallery() { //agarramos la foto de gallery
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).setType("image/*")
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        startForActivityGallery.launch(intent)
        //val intent = Intent(Intent.ACTION_GET_CONTENT)
    }

    //private lateinit var file: File
    private var file:File? = null
    private fun openCamera(){
        //las dos lineas siguientes permiten enviar imagenes recien tomadas
        StrictMode.setVmPolicy(VmPolicy.Builder().build())
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.resolveActivity(packageManager).also {
            createPhotoFile()
            val fileUri:Uri = FileProvider.getUriForFile(
                Objects.requireNonNull(applicationContext), BuildConfig.APPLICATION_ID + ".provider", file!!
            ) //La linea de "fileUri" es impresindible que no se altere para que la app se ejecute correctamente
            intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri)
        }
        startForActivityCamera.launch(intent)
    }

    private fun createPhotoFile() {
        val dir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        file = File.createTempFile("IMG_${System.currentTimeMillis()}",".jpg",dir)
    }

    private fun createPaletteSync(bitmap: Bitmap) : Palette = Palette.from(bitmap).generate()
    private fun createPaletteAsync(bitmap: Bitmap) { Palette.from(bitmap).generate{} }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_overflow, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.borrarBtn -> deleteAll()
            else -> {}
        }
        return true
    }
}