package kz.olzhass.kolesa.ui.resetpassword

import LoadingDialogFragment
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import kz.olzhass.kolesa.GlobalData
import kz.olzhass.kolesa.ui.login.MainPage
import kz.olzhass.kolesa.databinding.ActivityForgotPasswordBinding
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import okio.IOException
import org.json.JSONObject

class ForgotPassword : AppCompatActivity() {

    private lateinit var binding: ActivityForgotPasswordBinding
    val client = OkHttpClient()
    private var loadingDialog: LoadingDialogFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        with(binding) {
            buttonLogin.setOnClickListener {
                val email = binding.etEmail.text.toString().trim()
                if (email.isEmpty()){
                    binding.etEmail.error = "Please enter your email"
                    return@setOnClickListener
                }
                if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    binding.etEmail.error = "Please enter a valid email"
                    return@setOnClickListener
                }

                forgotPassword(email)

                val intent = Intent(this@ForgotPassword, OTPVerification::class.java)
                intent.putExtra("email", binding.etEmail.text.toString().trim())
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(intent)

            }
            imageButton.setOnClickListener {
                finish()
            }

            tvRegisterLink.setOnClickListener {
                val intent = Intent(this@ForgotPassword, MainPage::class.java)
                startActivity(intent)
            }

        }

    }

    private fun forgotPassword(email: String) {
        val url = "http://${GlobalData.ip}:3000/forgot-password" // Замените на ваш реальный адрес
        showLoading()
        // Формируем JSON-тело запроса
        val json = JSONObject()
        json.put("email", email)

        val requestBody = RequestBody.create(
            "application/json; charset=utf-8".toMediaTypeOrNull(),
            json.toString()
        )

        // Создаём POST-запрос
        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        // Отправляем запрос асинхронно
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    // Например, выводим сообщение об ошибке в TextView
                    hideLoading()
                    binding.tvErrorMessage.visibility = View.VISIBLE
                    binding.tvErrorMessage.text = "Request failed: ${e.message}"

                }
            }

            override fun onResponse(call: Call, response: Response) {
                runOnUiThread {
                    if (response.isSuccessful) {
                        val responseBody = response.body?.string()
                        if (responseBody != null) {
                            val jsonResponse = JSONObject(responseBody)
                            if (jsonResponse.getBoolean("success")) {
                                // Успешно отправлен код на почту
                                // Можно перейти на другой экран или показать сообщение
                                hideLoading()
                                binding.tvErrorMessage.visibility = View.VISIBLE
                                binding.tvErrorMessage.text = "Check your email for the reset code!"

                            } else {
                                // Сервер вернул success=false
                                val msg = jsonResponse.optString("message", "Unknown error")
                                hideLoading()
                                binding.tvErrorMessage.visibility = View.VISIBLE
                                binding.tvErrorMessage.text = "Failed: $msg"

                            }
                        } else {
                            hideLoading()
                            binding.tvErrorMessage.visibility = View.VISIBLE
                            binding.tvErrorMessage.text = "Empty response from server"

                        }
                    } else {
                        hideLoading()
                        binding.tvErrorMessage.visibility = View.VISIBLE
                        binding.tvErrorMessage.text = "Request failed: ${response.message}"

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