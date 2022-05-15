package com.example.selectfromgallery.ui.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.selectfromgallery.PasswordRecoverActivity
import com.example.selectfromgallery.R
import kotlinx.android.synthetic.main.activity_login.*
import java.util.concurrent.Executor

class LoginActivity : AppCompatActivity() {

     lateinit var executor: Executor
     lateinit var biometricPrompt:androidx.biometric.BiometricPrompt
     lateinit var promptInfo: androidx.biometric.BiometricPrompt.PromptInfo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        hideRegistroPad()
        setListeners()
        loadLogo()
        setBiometricLogin()
    }

    private fun setBiometricLogin() {
        executor = ContextCompat.getMainExecutor(this)
        biometricPrompt = androidx.biometric.BiometricPrompt(this, executor,
            object : androidx.biometric.BiometricPrompt.AuthenticationCallback() {

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    Toast.makeText(applicationContext, "Authentication error: $errString", Toast.LENGTH_SHORT).show()
                }
                override fun onAuthenticationSucceeded(result: androidx.biometric.BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    startActivity(Intent(applicationContext, MainActivity::class.java).putExtra("active", ""))
                    Toast.makeText(applicationContext, "Authentication succeed\nWelcome!", Toast.LENGTH_SHORT).show()
                }
                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                }
            })
        promptInfo = androidx.biometric.BiometricPrompt.PromptInfo.Builder()
            .setTitle("Private Gallery's biometric login")
            .setSubtitle("Log in using your fingerprint")
            .setNegativeButtonText("Use password instead")
            .build()

        biometricPrompt.authenticate(promptInfo)
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