package kz.olzhass.kolesa.ui.appointment

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kz.olzhass.kolesa.GlobalData
import kz.olzhass.kolesa.R
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException


class AppointmentsAdapter(private val context: Context, private val appointments: MutableList<Appointment>, private val onRescheduleClick: (Appointment) -> Unit) : RecyclerView.Adapter<AppointmentsAdapter.AppointmentViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppointmentViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_appointment, parent, false)
        return AppointmentViewHolder(view)
    }

    override fun onBindViewHolder(holder: AppointmentViewHolder, position: Int) {
        val appointment = appointments[position]

        holder.doctorName.text = appointment.doctor_name
        holder.appointmentDate.text = appointment.appointment_date
        holder.appointmentTime.text = appointment.appointment_time
        holder.appointmentRemaining.text = appointment.appointment_status
        holder.appointmentPhone.text = appointment.appointment_phone
        holder.appointmentEmail.text = appointment.appointment_reason
        holder.appointmentCreatedAt.text = appointment.appointment_created_at

        holder.btnReschedule.setOnClickListener {
            onRescheduleClick(appointment)
        }

        holder.btnCancel.setOnClickListener {
            // Показываем диалог с подтверждением
            showCancelDialog(appointment, position)
        }
    }

    override fun getItemCount(): Int {
        return appointments.size
    }

    inner class AppointmentViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val doctorName: TextView = view.findViewById(R.id.tvDoctorName)
        val appointmentDate: TextView = view.findViewById(R.id.tvDate)
        val appointmentTime: TextView = view.findViewById(R.id.tvTime)
        val appointmentRemaining: TextView = view.findViewById(R.id.tvRemaining)
        val appointmentPhone: TextView = view.findViewById(R.id.tvPhone)
        val appointmentEmail: TextView = view.findViewById(R.id.tvEmail)
        val appointmentCreatedAt: TextView = view.findViewById(R.id.tvCreatedAt)
        val btnReschedule: Button = view.findViewById(R.id.btnReschedule)
        val btnCancel: Button = view.findViewById(R.id.btnCancel)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateList(newList: List<Appointment>) {
        appointments.clear()
        appointments.addAll(newList)
        notifyDataSetChanged()
    }

    private fun showCancelDialog(appointment: Appointment, position: Int) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Подтверждение")
        builder.setMessage("Вы уверены, что хотите отменить встречу с Dr. ${appointment.doctor_name}?")

        builder.setPositiveButton("Да") { _, _ ->
            removeAppointment(appointment, position)
        }

        builder.setNegativeButton("Нет") { dialog, _ ->
            dialog.dismiss()
        }

        builder.show()
    }

    private fun removeAppointment(appointment: Appointment, position: Int) {
        appointments.removeAt(position)
        notifyItemRemoved(position)

        cancelAppointment(appointment.appointment_id)
    }

    private fun cancelAppointment(appointmentId: Int) {
        val url = "http://${GlobalData.ip}:3000/delete-appointment"

        val json = JSONObject().apply {
            put("appointment_id", appointmentId)
        }

        val requestBody = RequestBody.create(
            "application/json; charset=utf-8".toMediaTypeOrNull(),
            json.toString()
        )

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        val client = OkHttpClient()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("AppointmentsAdapter", "Error cancelling appointment: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    Log.d("AppointmentsAdapter", "Appointment cancelled successfully")
                } else {
                    Log.e("AppointmentsAdapter", "Failed to cancel appointment: ${response.message}")
                }
            }
        })
    }
}

