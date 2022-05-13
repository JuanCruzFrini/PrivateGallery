package com.example.selectfromgallery.ui.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.selectfromgallery.PasswordRecoverActivity
import com.example.selectfromgallery.R
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        hideRegistroPad()
        setListeners()
        loadLogo()
    }

    private fun setListeners() {
        btnIngresar.setOnClickListener { verifyPassword() }
        txtRecuperarContraseña.setOnClickListener { startActivity(Intent(this, PasswordRecoverActivity::class.java)) }
    }

    private fun loadLogo() =
        Glide.with(applicationContext).load(R.drawable.logo_transparente).into(findViewById(R.id.imageView))

    private fun hideRegistroPad() {
        title = "Login"
        etClaveRecuperarPassword.visibility = View.GONE
        etPasswordRepeat.visibility = View.GONE
        txtClaveRecuperarContraseña.visibility = View.GONE
        txtContraseñaRepeat.visibility = View.GONE
    }

    private fun verifyPassword() {
        val prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE)
        if (etPassword.text.toString() == prefs.getString("password", "")){
            startActivity(Intent(this, MainActivity::class.java).putExtra("active", ""))
        }
        else etPassword.error = "Contraseña incorrecta."
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finishAffinity()
    }
}