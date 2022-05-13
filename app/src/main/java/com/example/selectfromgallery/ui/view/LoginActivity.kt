package com.example.selectfromgallery.ui.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.selectfromgallery.R
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        title = "Login"

        btnIngresar.setOnClickListener { verifyPassword() }
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