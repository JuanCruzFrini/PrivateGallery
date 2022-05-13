package com.example.selectfromgallery.ui.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.selectfromgallery.R
import kotlinx.android.synthetic.main.activity_login.*

class RegistroActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        title = "Registrate"

        txtContraseña.text = "Ingresa una contraseña"
        btnIngresar.text = "registrar"
        btnIngresar.setOnClickListener { registerUser() }
    }

    private fun registerUser() {
        val et = etPassword.text.toString()
        if (et.length in 8..20) {
            val editor = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE).edit()
            editor.putString("password", et)
            editor.apply()
            startActivity(Intent(this, MainActivity::class.java).putExtra("active", ""))
        } else { etPassword.error = "La contraseña debe contener al menos 8 caracteres, sin exeder los 20" }
    }
}