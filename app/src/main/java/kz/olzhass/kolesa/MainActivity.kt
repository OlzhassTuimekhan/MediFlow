package kz.olzhass.kolesa

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import kz.olzhass.kolesa.databinding.ActivityMainBinding
import kz.olzhass.kolesa.ui.login.MainPage
import kz.olzhass.kolesa.ui.register.RegisterPage


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val token = sharedPreferences.getString("auth_token", null)


        // Если токен существует, переходим на главную страницу
        if (token != null) {
            val intent = Intent(
                this@MainActivity,
                MyDocuments::class.java
            )
            startActivity(intent)
            finish()
        }


        with(binding) {
            buttonLogin.setOnClickListener {
                val intent = Intent(this@MainActivity, MainPage::class.java)
                startActivity(intent)
            }
            buttonRegister.setOnClickListener {
                val intent = Intent(this@MainActivity, RegisterPage::class.java)
                startActivity(intent)
            }
        }

        Log.d("Main", "Created")

    }


    override fun onStart() {
        Log.d("Main", "Started")
        super.onStart()
    }

     override fun onPause() {
        Log.d("Main", "Paused")
        super.onPause()

    }

    override fun onResume() {
        Log.d("Main", "Resumed")
        super.onResume()
    }
}