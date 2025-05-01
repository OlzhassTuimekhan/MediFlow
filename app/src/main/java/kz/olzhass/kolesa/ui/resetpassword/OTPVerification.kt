package kz.olzhass.kolesa.ui.resetpassword

import LoadingDialogFragment
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kz.olzhass.kolesa.GlobalData
import kz.olzhass.kolesa.databinding.ActivityOtpverificationBinding
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException


class OTPVerification : AppCompatActivity() {

    private lateinit var binding: ActivityOtpverificationBinding
    val client = OkHttpClient()
    private var loadingDialog: LoadingDialogFragment? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        binding = ActivityOtpverificationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val email = intent.getStringExtra("email") ?: ""


        with(binding) {


            buttonLogin.setOnClickListener {
                val digit1 = binding.etDigit1.text.toString().trim()
                val digit2 = binding.etDigit2.text.toString().trim()
                val digit3 = binding.etDigit3.text.toString().trim()
                val digit4 = binding.etDigit4.text.toString().trim()

                if (digit1.isEmpty() || digit2.isEmpty() || digit3.isEmpty() || digit4.isEmpty()) {
                    Toast.makeText(this@OTPVerification, "Введите полный код", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val enteredCode = "$digit1$digit2$digit3$digit4"

                verifyResetCode(email = email, code = enteredCode) {
                    // Код верный, можно перейти к следующему шагу, например открыть экран сброса пароля:
                    val intent = Intent(this@OTPVerification, ResetPassword::class.java)
                    intent.putExtra("email", email)
                    startActivity(intent)
                }
            }


            etDigit1.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    if (s?.length == 1) {
                        etDigit2.requestFocus()
                    }
                }
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            })

            etDigit2.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    if (s?.length == 1) {
                        etDigit3.requestFocus()
                    }
                }
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            })
            etDigit3.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    if (s?.length == 1) {
                        etDigit4.requestFocus()
                    }
                }
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            })
            imageButton.setOnClickListener {
                finish()
            }
            tvRegisterLink.setOnClickListener {
                val intent = Intent(this@OTPVerification, ForgotPassword::class.java)
                startActivity(intent)
            }
        }
    }

    private fun verifyResetCode(email: String, code: String, onVerified: () -> Unit) {
        val url = "http://${GlobalData.ip}:3000/verify-code" // Замените на адрес вашего сервера
        showLoading()
        // Формируем JSON-тело запроса с email и кодом
        val json = JSONObject().apply {
            put("email", email)
            put("code", code)
        }

        val requestBody = RequestBody.create(
            "application/json; charset=utf-8".toMediaTypeOrNull(),
            json.toString()
        )

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@OTPVerification, "Verification failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                runOnUiThread {
                    if (response.isSuccessful) {
                        val responseBody = response.body?.string()
                        if (!responseBody.isNullOrEmpty()) {
                            val jsonResponse = JSONObject(responseBody)
                            if (jsonResponse.getBoolean("success")) {
                                // Код верный – вызываем колбэк для дальнейших действий (например, переходим на экран сброса пароля)
                                hideLoading()
                                onVerified()
                            } else {
                                hideLoading()
                                Toast.makeText(this@OTPVerification, "Invalid code, please try again.", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            hideLoading()
                            Toast.makeText(this@OTPVerification, "An empty response from the server", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        hideLoading()
                        Toast.makeText(this@OTPVerification, "Server Error: ${response.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }
    fun showLoading() {
        if (loadingDialog == null) {
            loadingDialog = LoadingDialogFragment()
            loadingDialog?.show(supportFragmentManager, "loading")
        }
    }

    fun hideLoading() {
        if (loadingDialog != null && loadingDialog?.isAdded == true) {
            loadingDialog?.dismissAllowingStateLoss()
            loadingDialog = null
        }
    }

}