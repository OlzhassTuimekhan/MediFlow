package kz.olzhass.kolesa.ui.calendar

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kz.olzhass.kolesa.GlobalData
import kz.olzhass.kolesa.ui.appointment.Appointment
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit

class CalendarViewModel : ViewModel() {

    private val _activeAppointments = MutableLiveData<List<Appointment>>()
    val activeAppointments: LiveData<List<Appointment>> get() = _activeAppointments

    private val _previousAppointments = MutableLiveData<List<Appointment>>()
    val previousAppointments: LiveData<List<Appointment>> get() = _previousAppointments

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage
    private var userId: Int = -1

    private val client = OkHttpClient()
    private val outputDateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    private val inputDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())

    fun fetchAppointments(userId: Int) {
        if (userId == -1) {
            _errorMessage.postValue("User ID is not available")
            return
        }

        val url = "http://${GlobalData.ip}:3000/appointments/$userId"
        val request = Request.Builder()
            .url(url)
            .get()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                _errorMessage.postValue("Appointments fetch failed: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    try {
                        val jsonResponse = JSONObject(responseBody ?: "{}")
                        Log.d("CalendarViewModel", "Response Body: $responseBody")
                        if (jsonResponse.getBoolean("success")) {
                            val appointmentsJsonArray = jsonResponse.getJSONArray("appointments")
                            val appointmentsList = mutableListOf<Appointment>()
                            val currentDate = Calendar.getInstance(TimeZone.getTimeZone("Asia/Almaty")).timeInMillis

                            Log.d("CalendarViewModel", "Appointments JSON Array: $appointmentsJsonArray")
                            for (i in 0 until appointmentsJsonArray.length()) {
                                val appJson = appointmentsJsonArray.getJSONObject(i)
                                Log.d("CalendarViewModel", "Appointment JSON Object: $appJson")
                                try {
                                    val appointmentDateStr = appJson.getString("appointment_date")
                                    val parsedDate = inputDateFormat.parse(appointmentDateStr)
                                    val formattedDate = outputDateFormat.format(parsedDate!!)

                                    val appointmentDateStr1 = appJson.getString("appointment_date")
                                    val parsedDate1 = inputDateFormat.parse(appointmentDateStr1)
                                    val formattedDate1 = outputDateFormat.format(parsedDate1!!)

                                    val appointmentTimeMillis = parsedDate?.time ?: 0
                                    val timeDifference = appointmentTimeMillis - currentDate
                                    val daysRemaining = TimeUnit.MILLISECONDS.toDays(timeDifference)

                                    val statusText = if (daysRemaining > 0) {
                                        "$daysRemaining days remaining"
                                    } else if (daysRemaining == 0L) {
                                        "Today"
                                    } else {
                                        "Passed"
                                    }

                                    val appointment = Appointment(
                                        appointment_id = appJson.getInt("appointment_id"),
                                        user_id = appJson.getInt("user_id"),
                                        doctor_id = appJson.getInt("doctor_id"),
                                        appointment_date = formattedDate, // Отформатированная дата
                                        appointment_time = appJson.getString("appointment_time"),
                                        appointment_phone = appJson.getString("appointment_phone"),
                                        appointment_reason = appJson.getString("appointment_reason"),
                                        appointment_status = statusText, // Вычисленный статус
                                        appointment_created_at = formattedDate1,
                                        doctor_name = appJson.getString("doctor_name"),
                                        doctor_phone = appJson.getString("doctor_phone"),
                                        doctor_email = appJson.getString("doctor_email"),
                                        doctor_created_at = appJson.getString("doctor_created_at")
                                    )
                                    appointmentsList.add(appointment)
                                } catch (e: Exception) {
                                    Log.e("CalendarViewModel", "Error processing appointment: ${appJson.toString()}", e)
                                    // Обработка ошибки
                                }
                            }

                            val activeList = mutableListOf<Appointment>()
                            val previousList = mutableListOf<Appointment>()

                            appointmentsList.forEach {
                                val parsedDate = inputDateFormat.parse(
                                    appointmentsJsonArray.getJSONObject(appointmentsList.indexOf(it)).getString("appointment_date")
                                )
                                val appointmentTimeMillis = parsedDate?.time ?: 0

                                if (appointmentTimeMillis > currentDate) {
                                    activeList.add(it)
                                } else {
                                    previousList.add(it)
                                }
                            }

                            _activeAppointments.postValue(activeList)
                            _previousAppointments.postValue(previousList)

                        } else {
                            _errorMessage.postValue("Appointments fetch failed: ${jsonResponse.getString("message")}")
                        }
                    } catch (e: Exception) {
                        _errorMessage.postValue("Error parsing appointments data: ${e.message}")
                    }
                } else {
                    _errorMessage.postValue("Appointments fetch failed: ${response.message}")
                }
            }
        })
    }
}