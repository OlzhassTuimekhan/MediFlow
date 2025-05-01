package kz.olzhass.kolesa.ui.doctors

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kz.olzhass.kolesa.GlobalData
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException


class DoctorsViewModel : ViewModel() {

    private val _allDoctors = MutableLiveData<List<Doctor>>()
    val allDoctors: LiveData<List<Doctor>> = _allDoctors

    private val _allSpecialties = MutableLiveData<List<Specialty>>()
    val allSpecialties: LiveData<List<Specialty>> = _allSpecialties

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val client = OkHttpClient()

    init {
        fetchDoctorsData()
    }

    private fun fetchDoctorsData() {
        val url = "http://${GlobalData.ip}:3000/doctors"
        val request = Request.Builder()
            .url(url)
            .get()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                _errorMessage.postValue("Doctors fetch failed: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    try {
                        val jsonResponse = JSONObject(responseBody ?: "{}")
                        if (jsonResponse.getBoolean("success")) {
                            val doctorsArray = jsonResponse.getJSONArray("doctors")
                            val doctorsList = mutableListOf<Doctor>()
                            val specialtiesSet = mutableSetOf<String>() // Используем Set для уникальности специальностей

                            for (i in 0 until doctorsArray.length()) {
                                val doctorJson = doctorsArray.getJSONObject(i)
                                val doctor = Doctor(
                                    id = doctorJson.getInt("id"),
                                    nameSurname = doctorJson.getString("Name Surname"),
                                    phone = doctorJson.getString("phone"),
                                    specialties = doctorJson.getString("Specialities"),
                                    services = doctorJson.getString("Services")
                                )
                                doctorsList.add(doctor)

                                // Добавляем специальности в Set
                                val specialties = doctor.specialties?.split(", ") ?: emptyList()
                                specialtiesSet.addAll(specialties)
                            }
                            _allDoctors.postValue(doctorsList)

                            // Формируем список специальностей из Set
                            val specialtiesList = specialtiesSet.mapIndexed { index, name ->
                                Specialty(index + 1, name) // Используем индекс + 1 в качестве id
                            }
                            _allSpecialties.postValue(specialtiesList)
                        } else {
                            _errorMessage.postValue("Doctors fetch failed: " + jsonResponse.getString("message"))
                        }
                    } catch (e: Exception) {
                        _errorMessage.postValue("Error parsing doctors data: ${e.message}")
                    }
                } else {
                    _errorMessage.postValue("Doctors fetch failed: ${response.message}")
                }
            }
        })
    }
}
