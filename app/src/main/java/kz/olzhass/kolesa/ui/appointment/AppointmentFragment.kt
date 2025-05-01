package kz.olzhass.kolesa.ui.appointment

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.navigation.fragment.findNavController
import kz.olzhass.kolesa.GlobalData
import kz.olzhass.kolesa.R
import kz.olzhass.kolesa.databinding.FragmentAppointmentBinding
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import okio.IOException
import org.json.JSONObject
import java.util.Calendar

//Appointment fragments
class AppointmentFragment : Fragment() {

    private var _binding: FragmentAppointmentBinding? = null
    private val binding get() = _binding!!
    private var doctor_id: Int? = null
    private var doctorName: String? = null
    private var doctorSpecialty: String? = null
    private var doctorPhone: String? = null
    private var doctorServices: String? = null
    val client = OkHttpClient()
    private var userId: Int = -1

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _appointmentData = MutableLiveData<AppointmentData?>()
    val appointmentData: LiveData<AppointmentData?> get() = _appointmentData

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAppointmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sharedPreferences = requireContext().getSharedPreferences("user_prefs", AppCompatActivity.MODE_PRIVATE)
        userId = sharedPreferences.getInt("user_id", -1)

        arguments?.let {
            doctor_id = it.getInt("doctor_id")
            doctorName = it.getString("doctor_name")
            doctorSpecialty = it.getString("doctor_specialty")
            doctorPhone = it.getString("doctor_phone")
            doctorServices = it.getString("doctor_services")

            // Применяем эти данные к TextView
            binding.tvDoctorName.text = doctorName ?: "Неизвестно"
            binding.tvSpecialty.text = doctorSpecialty ?: "Не указана"
            binding.tvPhone.text = doctorPhone ?: "Нет телефона"
            binding.tvServices.text = doctorServices ?: "Нет услуг"
        }

        binding.btnPickDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(
                requireContext(),
                DatePickerDialog.OnDateSetListener { _, selectedYear, selectedMonth, selectedDay ->
                    val selectedDate = "$selectedDay/${selectedMonth + 1}/$selectedYear"
                    binding.tvSelectedDate.text = selectedDate
                    binding.tvSelectedDate.visibility = View.VISIBLE
                },
                year, month, day
            )
            datePickerDialog.show()
        }

        binding.btnPickTime.setOnClickListener {
            val calendar = Calendar.getInstance()
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)

            val timePickerDialog = TimePickerDialog(
                requireContext(),
                TimePickerDialog.OnTimeSetListener { _, selectedHour, selectedMinute ->
                    val selectedTime = "$selectedHour:${selectedMinute.toString().padStart(2, '0')}"
                    binding.tvSelectedTime.text = selectedTime
                    binding.tvSelectedTime.visibility = View.VISIBLE
                },
                hour, minute, true
            )
            timePickerDialog.show()
        }
        binding.imageButton.setOnClickListener {
            findNavController().popBackStack()  // Возвращаемся на предыдущий фрагмент
        }
        binding.buttonApply.setOnClickListener {
            Log.d("AppointmentFragment", "User ID: $userId, Doctor ID: $doctor_id, Date: ${binding.tvSelectedDate.text}, Time: ${binding.tvSelectedTime.text}, Phone: $doctorPhone, Reason: ${binding.etReason.text}, ")

            doctor_id?.let { it1 ->
                createAppointment(userId,
                    it1, binding.tvSelectedDate.text.toString(), binding.tvSelectedTime.text.toString(), doctorPhone, binding.etReason.text.toString())
            }

            //Logic for success appointment
            Toast.makeText(requireContext(), "Запись успешно создана", Toast.LENGTH_SHORT).show()
            findNavController().navigate(R.id.action_AppointmentFragment_to_activesFragment)


        }
    }

    fun createAppointment(
        userId: Int,
        doctorId: Int,
        appointmentDate: String,
        appointmentTime: String,
        phone: String?,
        reason: String?
    ) {
        if (userId == -1 || doctorId == -1 || appointmentDate.isEmpty() || appointmentTime.isEmpty()) {
            _errorMessage.postValue("Missing required fields")
            return
        }

        _isLoading.postValue(true)

        val url = "http://${GlobalData.ip}:3000/create-appointment"

        val json = JSONObject().apply {
            put("user_id", userId)
            put("doctor_id", doctorId)
            put("appointment_date", appointmentDate)
            put("appointment_time", appointmentTime)
            put("phone", phone)
            put("reason", reason)
            put("status", "Pending")
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
                _isLoading.postValue(false)
                _errorMessage.postValue("Appointment creation failed: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                _isLoading.postValue(false)
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    try {
                        // Логируем весь ответ
                        Log.d("AppointmentFragment", "Response Body: $responseBody")

                        val jsonResponse = JSONObject(responseBody ?: "{}")

                        // Проверяем success
                        if (jsonResponse.getBoolean("success")) {
                            val appointmentJson = jsonResponse.getJSONObject("appointment")

                            // Логируем данные о записи
                            Log.d("AppointmentFragment", "Appointment Data: $appointmentJson")

                            val appointmentId = appointmentJson.getInt("id")
                            val appointmentDate = appointmentJson.getString("appointment_date")
                            val appointmentTime = appointmentJson.getString("appointment_time")

                            // Обновляем данные о записи
                            _appointmentData.postValue(AppointmentData(appointmentId, appointmentDate, appointmentTime))
                        } else {
                            _errorMessage.postValue("Appointment creation failed: ${jsonResponse.getString("message")}")
                        }
                    } catch (e: Exception) {
                        _errorMessage.postValue("Error parsing appointment data: ${e.message}")
                    }
                } else {
                    _errorMessage.postValue("Appointment creation failed: ${response.message}")
                }
            }

        })
    }

}