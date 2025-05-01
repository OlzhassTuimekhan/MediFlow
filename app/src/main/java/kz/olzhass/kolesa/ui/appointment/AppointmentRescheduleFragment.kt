package kz.olzhass.kolesa.ui.appointment

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import kz.olzhass.kolesa.GlobalData
import kz.olzhass.kolesa.databinding.FragmentAppointmentRescheduleBinding
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import java.util.Calendar

class AppointmentRescheduleFragment : Fragment() {

    private var _binding: FragmentAppointmentRescheduleBinding? = null
    private val binding get() = _binding!!

    private var appointmentId: Int? = null
    private var doctorName: String? = null
    private var appointmentDate: String? = null
    private var appointmentTime: String? = null
    private var appointmentReason: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAppointmentRescheduleBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Получаем переданные данные
        arguments?.let {
            appointmentId = it.getInt("appointment_id")
            doctorName = it.getString("doctor_name")
            appointmentDate = it.getString("appointment_date")
            appointmentTime = it.getString("appointment_time")
            appointmentReason = it.getString("appointment_reason")

            // Заполняем данные в UI
            binding.tvDoctorName.text = doctorName ?: "Unknown"
            binding.tvSelectedDate.text = appointmentDate
            binding.tvSelectedDate.visibility = View.VISIBLE
            binding.tvSelectedTime.text = appointmentTime
            binding.tvSelectedTime.visibility = View.VISIBLE
            binding.etReason.setText(appointmentReason)
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
            findNavController().popBackStack()
        }
        binding.buttonReschedule.setOnClickListener {
            val newDate = binding.tvSelectedDate.text.toString()
            val newTime = binding.tvSelectedTime.text.toString()
            val reasonToVisit = binding.etReason.text.toString()

            if (newDate.isEmpty() || newTime.isEmpty() || reasonToVisit.isEmpty()) {
                Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Отправляем данные на сервер для переноса записи
            rescheduleAppointment(appointmentId!!, newDate, newTime, reasonToVisit)
        }
    }

    private fun rescheduleAppointment(appointmentId: Int, newDate: String, newTime: String, reasonToVisit: String) {
        // Отправка данных на сервер
        val json = JSONObject().apply {
            put("appointment_id", appointmentId)
            put("new_date", newDate)
            put("new_time", newTime)
            put("reason_to_visit", reasonToVisit)
        }

        val requestBody = RequestBody.create(
            "application/json; charset=utf-8".toMediaTypeOrNull(),
            json.toString()
        )

        val request = Request.Builder()
            .url("http://${GlobalData.ip}:3000/reschedule-appointment")
            .post(requestBody)
            .build()

        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                requireActivity().runOnUiThread {
                    Toast.makeText(requireContext(), "Failed to reschedule appointment: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    requireActivity().runOnUiThread {
                        Toast.makeText(requireContext(), "Appointment rescheduled successfully", Toast.LENGTH_SHORT).show()
                        findNavController().popBackStack()
                    }
                } else {
                    requireActivity().runOnUiThread {
                        Toast.makeText(requireContext(), "Failed to reschedule appointment", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }
}
