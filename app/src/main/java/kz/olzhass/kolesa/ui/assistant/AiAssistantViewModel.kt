package kz.olzhass.kolesa.ui.assistant

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kz.olzhass.kolesa.GlobalData
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit


class AiAssistantViewModel : ViewModel() {

    private val _aiResponse = MutableLiveData<String>()
    val aiResponse: LiveData<String>
        get() = _aiResponse


    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    fun askAI(message: String) {
        val url = "http://${GlobalData.ip}:8000/ask-ai"
        Log.d("AiAssistantViewModel", "URL: $url")

        val json = JSONObject().apply {
            put("message", message)
            put(
                "system_message", "You are a helpful assistant for the MediFlow mobile app. " +
                        "The app has the following sections: - My Documents: you can see all your medical documents. " +
                        "- Appointments: schedule or view doctor appointments. - AI Assistant: chat for health/app questions. " +
                        "- Doctors: list of doctors, specialties, and booking option. - Profile: user settings, personal info, change information about user. " +
                        "When the user describes symptoms, recommend a suitable doctor. Then remind them about booking in the 'Appointments' tab or searching in the 'Doctors' tab. " +
                        "If the user mentions documents, refer them to 'My Documents'. For scheduling, refer them to 'Appointments' or 'Doctors'. " +
                        "For profile or account changes, mention 'Profile'. If the user requests a specific language, answer in that language. " +
                        "Otherwise, default to English. Keep your answers concise, friendly, and helpful."
            )
            put("max_tokens", 512)
            put("temperature", 0.7)
            put("top_p", 0.95)
            put("language_choice", "English")
        }

        val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
        val body = RequestBody.create(mediaType, json.toString())

        val request = Request.Builder()
            .url(url)
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                _aiResponse.postValue("Ошибка запроса: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (it.isSuccessful) {
                        val responseBody = it.body?.string()
                        if (responseBody != null) {
                            val jsonResponse = JSONObject(responseBody)
                            val answer = jsonResponse.getString("answer")
                            _aiResponse.postValue(answer)
                        } else {
                            _aiResponse.postValue("Пустой ответ сервера.")
                        }
                    } else {
                        _aiResponse.postValue("Ошибка сервера: ${response.message}")
                    }
                }
            }
        })
    }
}
