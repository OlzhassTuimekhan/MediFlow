package kz.olzhass.kolesa.ui.resetpassword

import LoadingDialogFragment
import android.content.Intent
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kz.olzhass.kolesa.GlobalData
import kz.olzhass.kolesa.R
import kz.olzhass.kolesa.databinding.ActivityResetPasswordBinding
import kz.olzhass.kolesa.ui.login.MainPage
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import okhttp3.MediaType.Companion.toMediaTypeOrNull as toMediaTypeOrNull1

class ResetPassword : AppCompatActivity() {

    private lateinit var binding: ActivityResetPasswordBinding
    val client = OkHttpClient()
    var isPasswordVisible = false
    var isPassword1Visible = false
    private var loadingDialog: LoadingDialogFragment? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.hide()
        binding = ActivityResetPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val email = intent.getStringExtra("email")?: ""
        with(binding) {

            imageButton.setOnClickListener {
                finish()
            }
            buttonLogin.setOnClickListener {
                val password = binding.etPassword.text.toString()
                val confirm = binding.etPassword1.text.toString()
//                 Проверяем пароли
                if (password.isEmpty()) {
                    binding.etPassword.error = "Please enter a password"
                    return@setOnClickListener
                }
                if (confirm.isEmpty()) {
                    binding.etPassword1.error = "Please confirm the password"
                    return@setOnClickListener
                }
                if (password != confirm) {
                    binding.etPassword1.error = "Passwords do not match"
                    return@setOnClickListener
                }
                resetPassword(email, password)
            }

            ivTogglePassword.setOnClickListener {
                isPasswordVisible = !isPasswordVisible  // переключаем флаг
                togglePasswordVisibility(
                    binding.etPassword,
                    binding.ivTogglePassword,
                    isPasswordVisible
                )

                // Слушатель для второй иконки
                ivTogglePassword1.setOnClickListener {
                    isPassword1Visible = !isPassword1Visible
                    togglePasswordVisibility(
                        binding.etPassword1,
                        binding.ivTogglePassword1,
                        isPassword1Visible
                    )
                }
            }



            imageButton.setOnClickListener {
                finish()
            }
        }
    }

    private fun resetPassword(email: String, newPassword: String) {
        val url = "http://${GlobalData.ip}:3000/reset-password"  // Замените на свой адрес сервера
        showLoading()
        // Формируем JSON-тело запроса
        val json = JSONObject().apply {
            put("email", email)
            put("newPassword", newPassword)
        }

        // Создаём RequestBody
        val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull1()
        val requestBody = RequestBody.create(mediaType, json.toString())

        // Формируем POST-запрос
        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        // Отправляем запрос асинхронно
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    // Ошибка сети или иная ошибка
                    Toast.makeText(
                        this@ResetPassword,
                        "Reset password failed: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    hideLoading()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                runOnUiThread {
                    if (response.isSuccessful) {
                        val responseBody = response.body?.string()
                        if (!responseBody.isNullOrEmpty()) {
                            val jsonResponse = JSONObject(responseBody)
                            if (jsonResponse.getBoolean("success")) {
                                // Пароль сброшен успешно
                                Toast.makeText(
                                    this@ResetPassword,
                                    "Password has been reset successfully!",
                                    Toast.LENGTH_SHORT
                                ).show()
                                hideLoading()

                                 val intent = Intent(this@ResetPassword, MainPage::class.java)
                                 startActivity(intent)
                            } else {
                                // Сервер вернул success=false
                                val msg = jsonResponse.optString("message", "Unknown error")
                                Toast.makeText(
                                    this@ResetPassword,
                                    "Reset failed: $msg",
                                    Toast.LENGTH_SHORT
                                ).show()
                                hideLoading()
                            }
                        } else {
                            // Пустой ответ
                            Toast.makeText(
                                this@ResetPassword,
                                "Empty response from server",
                                Toast.LENGTH_SHORT
                            ).show()
                            hideLoading()
                        }
                    } else {
                        // Ответ сервера не OK (например, 400/404/500)
                        Toast.makeText(
                            this@ResetPassword,
                            "Reset password request failed: ${response.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                        hideLoading()
                    }
                }
            }
        })
    }
    private fun togglePasswordVisibility(
        editText: EditText,
        icon: ImageView,
        isVisible: Boolean
    ) {
        if (isVisible) {
            // Показываем пароль
            editText.transformationMethod = HideReturnsTransformationMethod.getInstance()
            icon.setImageResource(R.drawable.ic_eye_on) // иконка «глаз открыт»
        } else {
            // Скрываем пароль
            editText.transformationMethod = PasswordTransformationMethod.getInstance()
            icon.setImageResource(R.drawable.ic_eye_off) // иконка «глаз закрыт»
        }
        // Переводим курсор в конец поля, чтобы не сбивался текст
        editText.setSelection(editText.text.length)
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