package com.example.selectfromgallery.ui.view

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import com.bumptech.glide.Glide
import com.example.selectfromgallery.R
import kotlinx.android.synthetic.main.activity_login.btnIngresar
import kotlinx.android.synthetic.main.activity_password_recover.*

class PasswordRecoverActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_password_recover)

        showAlert()
        setListeners()
        setLogo()
    }

    private fun setLogo() =
        Glide.with(this).load(R.drawable.logo_transparente).into(findViewById(R.id.imageView))

    private fun setListeners() {
        btnIngresar.setOnClickListener { checkRecoverKey() }
    }

    private fun showAlert() {
        AlertDialog.Builder(this)
            .setTitle("Alerta")
            .setMessage("Recuerda que al ingresar de esta manera, se borrará tu contraseña y "+
                    "clave de seguridad actual, por lo que deberás volver a registrarte.\n" +
            "Si ingresaste aquí por error y recuerdas tu contraseña, deberías volver atras e ingresar de la forma habitual.")
            .setPositiveButton("Continuar") {_,_ -> }
            .setNegativeButton("Cancelar") {_,_ -> onBackPressed()}
            .create().show()
    }

    private fun checkRecoverKey() {
        val prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE)
        if (prefs.getString("key", "") == etKeyRecover.text.toString()){
            val editor = prefs.edit()
            editor.remove("key")
            editor.remove("password")
            editor.apply()
            startActivity(Intent(this, MainActivity::class.java))
        } else etKeyRecover.error = "Clave de recuperación incorrecta"
    }
}