package kz.olzhass.kolesa.ui.home

import android.util.Base64
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kz.olzhass.kolesa.GlobalData
import kz.olzhass.kolesa.ui.documents.DocumentData
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

class HomeViewModel : ViewModel() {

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _documentData = MutableLiveData<List<DocumentData>?>()
    val documentData: LiveData<List<DocumentData>?> get() = _documentData

    private val client = OkHttpClient()

    private val _text = MutableLiveData<String>().apply {
        value = "This is home Fragment"
    }
    val text: LiveData<String> = _text

    fun fetchDocumentDetails(clientId: Int) {
        if (clientId == -1) {
            _errorMessage.postValue("Client ID is not available")
            return
        }

        _isLoading.postValue(true)

        val url = "http://${GlobalData.ip}:3000/document-details/$clientId"
        Log.d("ProfileViewModel", "Request URL: $url") // Логируем URL запроса

        val request = Request.Builder()
            .url(url)
            .get()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                _isLoading.postValue(false)
                _errorMessage.postValue("Documents fetch failed: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                _isLoading.postValue(false)
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    Log.d("ProfileViewModel", "Response Body: $responseBody") // Логируем тело ответа

                    try {
                        val jsonResponse = JSONObject(responseBody ?: "{}")
                        if (jsonResponse.getBoolean("success")) {
                            val documentsJsonArray = jsonResponse.getJSONArray("documents")
                            val documentsList = mutableListOf<DocumentData>()

                            for (i in 0 until documentsJsonArray.length()) {
                                val docJson = documentsJsonArray.getJSONObject(i)

                                val base64String: String = docJson.optString("base64data")
                                Log.d("ProfileViewModel", "Base64 String Length: ${base64String.length}")

                                if (base64String.isNullOrEmpty()) {
                                    Log.e("ProfileViewModel", "Document data is empty for document ${docJson.getString("doctor_name")}")
                                    continue
                                }

                                // Декодируем строку Base64 в массив байтов
                                try {
                                    val decodedBytes = Base64.decode(base64String, Base64.DEFAULT)
                                    Log.d("ProfileViewModel", "Decoded Bytes Length: ${decodedBytes.size}")

                                    val documentDetail = DocumentData(
                                        doctorName = docJson.getString("doctor_name"),
                                        documentData = decodedBytes,  // Сохраняем декодированные данные
                                        documentType = docJson.optString("doc_type"),
                                        documentDate = docJson.getString("document_date")
                                    )

                                    // Добавляем документ в список
                                    documentsList.add(documentDetail)
                                    Log.d("ProfileViewModel", "Added document for ${docJson.getString("doctor_name")}")
                                } catch (e: IllegalArgumentException) {
                                    // В случае ошибки декодирования
                                    Log.e("ProfileViewModel", "Failed to decode document data for ${docJson.getString("doctor_name")}: ${e.message}")
                                    continue
                                }
                            }

                            // Обновляем данные в LiveData
                            if (documentsList.isNotEmpty()) {
                                _documentData.postValue(documentsList)
                                Log.d("ProfileViewModel", "Document list posted. Size: ${documentsList.size}")
                            } else {
                                _errorMessage.postValue("No valid documents found.")
                            }

                        } else {
                            _errorMessage.postValue("Documents fetch failed: ${jsonResponse.getString("message")}")
                        }

                    } catch (e: Exception) {
                        _errorMessage.postValue("Error parsing documents data: ${e.message}")
                        Log.e("ProfileViewModel", "Error parsing documents data: ${e.message}")
                    }
                } else {
                    Log.d("PROFILE_VIEW_MODEL", "Response not successful: ${response.message}") // Логируем ошибку ответа
                    _errorMessage.postValue("Documents fetch failed: ${response.message}")
                }
            }
        })
    }
}