package com.example.selectfromgallery.ui.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.selectfromgallery.R
import kotlinx.android.synthetic.main.activity_login.*

class RegistroActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        welcomeDialog()
        setListeners()
        adaptView()
    }

    private fun welcomeDialog() =
        AlertDialog.Builder(this)
            .setTitle("Te damos la bienvenida a Private Gallery")
            .setMessage("Somos una aplicación que te ayuda a resguardar tus fotografias de manera privada, en una base de datos alterna, " +
                    "que no ofrece lectura desde la galeria por defecto de tu telefono.\n" +
                    "Registra tus credenciales de seguridad para comenzar a usar Private Gallery.\n" +
                    "Recuerda que de todas formas podras usar tu huella dactilar para ingresar, para una mayor comodidad.")
            .setNeutralButton("Continuar"){_,_ ->}
            .create().show()

    private fun setListeners() {
        btnIngresar.setOnClickListener { registerUser() }
    }

    private fun adaptView() {
        title = "Registrate"
        txtContraseña.text = "Ingresa una contraseña"
        btnIngresar.text = "registrar"
        txtRecuperarContraseña.visibility = View.GONE
    }

    private fun registerUser() {
        val pw = etPassword.text.toString()
        val keyRec = etClaveRecuperarPassword.text.toString()
        if (pw.length in 8..20) {
            if (pw == etPasswordRepeat.text.toString()) {
                if (keyRec.length in 4..12) {
                    getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE).edit().let {
                        it.putString("password", pw)
                        it.putString("key", keyRec)
                        it.apply()
                    }
                    startActivity(Intent(this, MainActivity::class.java).putExtra("active", ""))
                } else etClaveRecuperarPassword.error = "La clave de recuperación debe contener al menos 4 caracteres, sin exeder los 15"
            } else etPasswordRepeat.error = "Las contraseñas no coinciden"
        } else { etPassword.error = "La contraseña debe contener al menos 8 caracteres, sin exeder los 20" }
    }

    override fun onBackPressed() = finishAffinity()
}
