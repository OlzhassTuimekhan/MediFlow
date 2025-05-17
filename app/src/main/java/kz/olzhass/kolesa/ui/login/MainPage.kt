package kz.olzhass.kolesa.ui.login

import LoadingDialogFragment
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.Patterns
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import kz.olzhass.kolesa.GlobalData
import kz.olzhass.kolesa.MainActivity
import kz.olzhass.kolesa.MyDocuments
import kz.olzhass.kolesa.R
import kz.olzhass.kolesa.databinding.ActivityMainPageBinding
import kz.olzhass.kolesa.ui.register.RegisterPage
import kz.olzhass.kolesa.ui.resetpassword.ForgotPassword
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException


class MainPage : AppCompatActivity() {
    private lateinit var binding: ActivityMainPageBinding
    val client = OkHttpClient()
    private var isPasswordVisible = false
    private var loadingDialog: LoadingDialogFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = ActivityMainPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        with(binding) {
            imageButton.setOnClickListener {
                val intent = Intent(this@MainPage, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
            buttonLogin.setOnClickListener {

                val email = binding.etEmail.text.toString().trim()

                if (!isEmailValid(email)) {
                    binding.etEmail.error = "Invalid email address"
                } else {
                    val email = etEmail.text.toString()
                    val password = etPassword.text.toString()
                    login(email, password)
                }


            }
            tvRegisterLink.setOnClickListener {
                val intent = Intent(this@MainPage, RegisterPage::class.java)
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
            }

            tvForgotPassword.setOnClickListener {
                val intent = Intent(this@MainPage, ForgotPassword::class.java)
                startActivity(intent)
            }
        }
    }

    //Login function
    private fun login(email: String, password: String) {
        val url = "http://${GlobalData.ip}:3000/login"
        showLoading()
        val json = JSONObject().apply {
            put("email", email)
            put("password", password)
        }
        val body = RequestBody.create("application/json; charset=utf-8".toMediaTypeOrNull(), json.toString())

        val request = Request.Builder()
            .url(url)
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    binding.tvErrorMessage.visibility = View.VISIBLE
                    hideLoading()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                runOnUiThread {
                    if (response.isSuccessful) {
                        val responseBody = response.body?.string()
                        val jsonResponse = JSONObject(responseBody)
                        if (jsonResponse.getBoolean("success")) {
                            val token = jsonResponse.getString("token")
                            val userId = jsonResponse.getInt("userId")
                            GlobalData.userId = userId
                            GlobalData.token = token

                            val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
                            val editor = sharedPreferences.edit()
                            editor.putString(
                                "auth_token",
                                token
                            )
                            editor.putInt("user_id", userId)
                            editor.apply()
                            val intent = Intent(this@MainPage, MyDocuments::class.java)
                            startActivity(intent)
                            hideLoading()
                            finish()
                        } else {
                            binding.tvErrorMessage.visibility = View.VISIBLE
                            hideLoading()
                        }
                    } else {
                        binding.tvErrorMessage.visibility = View.VISIBLE
                        hideLoading()
                    }
                }
            }
        })
    }


    //Show/Hide Password
    private fun togglePasswordVisibility(
        editText: EditText,
        icon: ImageView,
        isVisible: Boolean
    ) {
        if (isVisible) {
            editText.transformationMethod = HideReturnsTransformationMethod.getInstance()
            icon.setImageResource(R.drawable.ic_eye_on)
        } else {
            editText.transformationMethod = PasswordTransformationMethod.getInstance()
            icon.setImageResource(R.drawable.ic_eye_off)
        }
        editText.setSelection(editText.text.length)
    }

    //Email Valid
    private fun isEmailValid(email: String): Boolean {
        return !TextUtils.isEmpty(email) &&
                Patterns.EMAIL_ADDRESS.matcher(email).matches()
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