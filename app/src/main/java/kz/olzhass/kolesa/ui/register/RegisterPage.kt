package kz.olzhass.kolesa.ui.register

import LoadingDialogFragment
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.Patterns
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import kz.olzhass.kolesa.GlobalData
import kz.olzhass.kolesa.ui.login.MainPage
import kz.olzhass.kolesa.R
import kz.olzhass.kolesa.ui.resetpassword.Success
import kz.olzhass.kolesa.databinding.ActivityRegisterPageBinding
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
//SCROLL PAGE ADD
class RegisterPage : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterPageBinding
    val client = OkHttpClient()

    // Отслеживаем текущее состояние видимости для каждого поля
    private var isPasswordVisible = false
    private var isPassword1Visible = false
    private var loadingDialog: LoadingDialogFragment? = null


    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        binding = ActivityRegisterPageBinding.inflate(layoutInflater)
        setContentView(binding.root)


        with(binding) {
            buttonLogin.setOnClickListener {
                val email = binding.etEmail.text.toString().trim()
                val password = binding.etPassword.text.toString()
                val confirm  = binding.etPassword1.text.toString()
                val name = binding.etName.text.toString()
                val number = binding.etNumber.text.toString()

                // Проверяем сначала email (пример, если у вас есть уже isEmailValid())
                if (!isEmailValid(email)) {
                    binding.etEmail.error = "Invalid email address"
                    return@setOnClickListener
                }

                // Проверяем пароли
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

                register(email, password, name, number)
            }
            imageButton.setOnClickListener {
                finish()
            }
            tvRegisterLink.setOnClickListener {
                val intent = Intent(this@RegisterPage, MainPage::class.java)
                startActivity(intent)
            }

            ivTogglePassword.setOnClickListener {
                // Слушатель для первой иконки
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
        }

        val etNumber: EditText = findViewById(R.id.etNumber)

        etNumber.addTextChangedListener(object : TextWatcher {
            private var mFormatting = false
            private var mClearing = false
            private var lastLength = 0

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                lastLength = s?.length ?: 0
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (mFormatting || mClearing) {
                    return
                }

                mFormatting = true

                val formattedNumber = formatPhoneNumber(s.toString())
                s?.replace(0, s.length, formattedNumber)

                mFormatting = false
            }

            private fun formatPhoneNumber(number: String): String {
                var digits = number.replace("[^\\d]".toRegex(), "")

                if (digits.startsWith("7")) {
                    digits = "7" + digits.substring(1)
                }

                return when {
                    digits.length <= 1 -> "+" + digits
                    digits.length <= 4 -> "+" + digits.substring(0, 1) + " (" + digits.substring(1)
                    digits.length <= 7 -> "+" + digits.substring(0, 1) + " (" + digits.substring(1, 4) + ") " + digits.substring(4)
                    digits.length <= 9 -> "+" + digits.substring(0, 1) + " (" + digits.substring(1, 4) + ") " + digits.substring(4, 7) + "-" + digits.substring(7)
                    digits.length <= 11 -> "+" + digits.substring(0, 1) + " (" + digits.substring(1, 4) + ") " + digits.substring(4, 7) + "-" + digits.substring(7, 9) + "-" + digits.substring(9)
                    else -> "+" + digits.substring(0, 1) + " (" + digits.substring(1, 4) + ") " + digits.substring(4, 7) + "-" + digits.substring(7, 9) + "-" + digits.substring(9, 11)
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

    private fun isEmailValid(email: String): Boolean {
        return !TextUtils.isEmpty(email) &&
                Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun register(email: String, password: String, name: String, number: String) {
        val url = "http://${GlobalData.ip}:3000/register" // Укажи адрес твоего сервера
        showLoading()

        val json = JSONObject().apply {
            put("email", email)
            put("password", password)
            put("name", name)
            put("number", number)
        }

        val body = RequestBody.create("application/json; charset=utf-8".toMediaTypeOrNull(), json.toString())

        val request = Request.Builder()
            .url(url)
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    binding.tvErrorMessage.text = "Register failed: ${e.message}"
                    binding.tvErrorMessage.visibility = View.VISIBLE
                    hideLoading()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                runOnUiThread {
                    if (response.isSuccessful) {
                        val responseBody = response.body?.string()
                        val jsonResponse = JSONObject(responseBody ?: "")
                        if (jsonResponse.optBoolean("success", false)) {
                            GlobalData.userId = jsonResponse.getInt("userId")
                            val intent = Intent(this@RegisterPage, Success::class.java)
                            startActivity(intent)
                        } else {
                            binding.tvErrorMessage.text = "Register failed: ${jsonResponse.optString("message")}"
                            binding.tvErrorMessage.visibility = View.VISIBLE
                        }
                    } else {
                        binding.tvErrorMessage.text = "Register failed: ${response.message}"
                        binding.tvErrorMessage.visibility = View.VISIBLE
                    }
                    hideLoading()
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