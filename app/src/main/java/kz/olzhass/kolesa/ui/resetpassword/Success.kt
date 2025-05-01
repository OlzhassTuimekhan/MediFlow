package kz.olzhass.kolesa.ui.resetpassword

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kz.olzhass.kolesa.ui.login.MainPage
import kz.olzhass.kolesa.databinding.ActivitySuccessBinding

class Success : AppCompatActivity() {
    private lateinit var binding: ActivitySuccessBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySuccessBinding.inflate(layoutInflater)
        supportActionBar?.hide()
        setContentView(binding.root)
        binding.buttonLogin.setOnClickListener {
            val intent = Intent(this@Success, MainPage::class.java)
            startActivity(intent)

        }

    }
}