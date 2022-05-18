package com.example.selectfromgallery.ui.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.selectfromgallery.R
import com.example.selectfromgallery.databinding.ActivityLoginBinding
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.coroutines.*
import java.util.concurrent.Executor

class LoginActivity : AppCompatActivity() {

     lateinit var executor: Executor
     lateinit var biometricPrompt:androidx.biometric.BiometricPrompt
     lateinit var promptInfo: androidx.biometric.BiometricPrompt.PromptInfo

     private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadLogo()
        hideRegistroPad()
        setListeners()
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
                    showUnlockAnim()
                    startActivity(Intent(applicationContext, MainActivity::class.java).putExtra("active", ""))
                    Toast.makeText(applicationContext, "Authentication succeed", Toast.LENGTH_SHORT).show()
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

    private fun showUnlockAnim() {
        binding.unlockAnim.show()//visibility = View.VISIBLE
        binding.unlockAnim.speed *= 3f
        binding.unlockAnim.playAnimation()
    }

    private fun setListeners() {
        binding.btnIngresar.setOnClickListener { verifyPassword() }
        binding.txtRecuperarContraseA.setOnClickListener { startActivity(Intent(this, PasswordRecoverActivity::class.java)) }
        binding.btnLoginBiometrico.setOnClickListener { setBiometricLogin() }
    }

    private fun loadLogo() {
        lifecycleScope.launch {
            async(Dispatchers.Main) {
                Glide.with(applicationContext).load(R.drawable.logo_transparente)
                    .into(findViewById(R.id.imageView))
            }.await()
        }
    }

    private fun hideRegistroPad() {
        title = "Login"
        etClaveRecuperarPassword.hide()//visibility = View.GONE
        etPasswordRepeat.hide()//visibility = View.GONE
        txtClaveRecuperarContraseña.hide()//visibility = View.GONE
        txtContraseñaRepeat.hide()//visibility = View.GONE
        btnLoginBiometrico.show()//visibility = View.VISIBLE
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