package com.example.selectfromgallery.ui.view

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
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
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.isVisible
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
import kotlinx.coroutines.*
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
        if (!intent.hasExtra("active")){
            login()
            //onStop()
        }
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        uri = MutableLiveData<Uri>()
        uri.value = Uri.EMPTY

        db = AppDatabase.getInstance(this)!!

        setObservers()
        setListeners()
    }

    override fun onBackPressed() {
        AlertDialog.Builder(this)
            .setTitle("Tu sesión se cerrará al salir")
            .setPositiveButton("Continuar") {_, _ -> finishAffinity()}
            .setNegativeButton("Cancelar") { _, _ -> }
            .create().show()
    }

    private fun login() {
        val prefs = getSharedPreferences(getString(com.example.selectfromgallery.R.string.prefs_file), Context.MODE_PRIVATE)
        if (prefs.contains("password")) startActivity(Intent(this, LoginActivity::class.java))
        else startActivity(Intent(this, RegistroActivity::class.java))
    }

    private fun setListeners() {
        binding.btnGallery.setOnClickListener { requestReadExtStorPermission() }
        binding.btnEnviar.setOnClickListener { send() }
        binding.btnBorrar.setOnClickListener { deleteImageSelected() }
        binding.btnOpenRv.setOnClickListener { startActivity(Intent(this, RecyclerActivtiy::class.java)) }
        binding.btnInsert.setOnClickListener { insert() }
        binding.btnCamera.setOnClickListener{ requestCameraPermission() }
        binding.txtVerTodo.setOnClickListener { startActivity(Intent(this, RecyclerActivtiy::class.java)) }
        binding.txtRecientes.setOnClickListener {
            if (binding.mainRv.isVisible) mainRv.hide() else mainRv.show()//visibility = View.VISIBLE
        }
    }

    private fun setObservers(){
        viewModel.onCreate(this)
        viewModel.listaRecycler.observe(this, Observer {
            if (it.size > 9) binding.txtVerTodo.show() else binding.txtVerTodo.hide()
            viewModel.onCreate(this)
            adapter = ItemAdapter(it)
            binding.mainRv.adapter = adapter
            binding.mainRv.layoutManager = GridLayoutManager(this, 3)//LinearLayoutManager(this)
            ViewCompat.setNestedScrollingEnabled(binding.mainRv, false)//agrega fluidez al rv
        })
        viewModel.isLoading.observe(this, Observer {
            if (it == true) binding.MainProgress.show()//visibility = View.VISIBLE
            if (it == false) binding.MainProgress.hide()//visibility = View.GONE
        })
        viewModel.thumbnail.observe(this, Observer {
            Glide.with(this).load(it.imagen.decodeToString().toUri()).fitCenter().circleCrop().into(binding.btnOpenRv)
        })
        viewModel._showThumbnail.observe(this, Observer {
            if (it == true) {
                binding.btnOpenRv.show()
                binding.txtRecientes.show()
                //binding.txtVerTodo.hide()
                binding.txtEmptyListAdvice.hide()
            }
            if (it == false){
                binding.btnOpenRv.hide()
                binding.txtRecientes.hide()
                //binding.txtVerTodo.hide()
                binding.txtEmptyListAdvice.show()
            }
        })
        uri.observe(this, Observer {
            if (it != Uri.EMPTY){ showSelectedImagePad() }
            else hideSelectedImagePad()
        })
    }

    private fun insert() {
        CoroutineScope((Dispatchers.Main)).launch {
            binding.MainProgress.show()//visibility = View.VISIBLE
            withContext(Dispatchers.Default) { db.itemDao.insertItem(itemSelected) }
            binding.MainProgress.hide()//visibility = View.INVISIBLE
            hideSelectedImagePad()
            viewModel.onCreate(this@MainActivity)
        }
        Toast.makeText(this, "Imagen insertada correctamente", Toast.LENGTH_SHORT).show()
    }

    private fun deleteImageSelected() {
        uri.value = Uri.EMPTY
        hideSelectedImagePad()
        if (file != null) file?.absoluteFile?.delete()
        setObservers()
    }

    private fun deleteAll() = AlertDialog.Builder(this)
        .setTitle("ALERTA")
        .setMessage("¿Estas seguro que quieres borrar todo el contenido de la base de datos de SelectFromGallery?")
        .setPositiveButton("Si") { _, _ -> viewModel.delete(this) }
        .setNegativeButton("No") { _, _ -> }
        .create().show()

    private fun send() { //enviar img seleccionada de gallery
        val i = Intent(Intent.ACTION_SEND)
            .setType("image/*")
            .putExtra(Intent.EXTRA_STREAM, uri.value)
            .putExtra(Intent.EXTRA_TEXT, "Message sent from PrivateGallery")//.setPackage("com.whatsapp")
        val chooser = Intent(Intent.createChooser(i, "Enviar pic"))

        try { startActivity(chooser) }
        catch (e: Exception){ Toast.makeText(this, "Error de envio ${e.message}", Toast.LENGTH_SHORT).show() }
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
            itemSelected = ItemEntity(imagen = uri.value.toString().encodeToByteArray(), fecha = Calendar.getInstance().time.toString())
            Glide.with(this).load(uri.value).fitCenter().into(binding.image)
            binding.txtEmptyListAdvice.hide()
            showSelectedImagePad()
        }
    }
    private fun pickPhotoFromGallery() { //agarramos la foto de gallery
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).setType("image/*")
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        startForActivityGallery.launch(intent)
        //val intent = Intent(Intent.ACTION_GET_CONTENT)
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
            itemSelected = ItemEntity(imagen = uri.value.toString().encodeToByteArray(), fecha = Calendar.getInstance().time.toString())
            Glide.with(this).load(uri.value).fitCenter().into(binding.image)
            showSelectedImagePad()
            binding.txtEmptyListAdvice.hide()
            println("FOTO startForCamera = ${uri.value}")
            val palette = createPaletteSync(BitmapFactory.decodeFile(file.toString()))
            val colors:List<Palette.Swatch>? = palette.swatches
            binding.image.setBackgroundColor(colors?.random()?.rgb ?: R.color.purple_500 )
        }
    }
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

    private fun showSelectedImagePad() {
        binding.image.show()//visibility = View.VISIBLE
        binding.btnEnviar.show()//visibility = View.VISIBLE
        binding.btnBorrar.show()//visibility = View.VISIBLE
        binding.btnInsert.show()//visibility = View.VISIBLE
    }
    private fun hideSelectedImagePad() {
        binding.image.hide()//visibility = View.GONE
        binding.btnEnviar.hide()//visibility = View.GONE
        binding.btnBorrar.hide()//visibility = View.GONE
        binding.btnInsert.hide()//visibility = View.GONE
    }

    //menu overflow
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

fun View.show(){ visibility = View.VISIBLE }
fun View.hide(){ visibility = View.GONE }