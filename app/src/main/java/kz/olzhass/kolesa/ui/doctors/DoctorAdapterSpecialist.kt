package kz.olzhass.kolesa.ui.doctors

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import kz.olzhass.kolesa.R

class DoctorAdapterSpecialist(private var doctors: List<Doctor>) :
    RecyclerView.Adapter<DoctorAdapterSpecialist.DoctorViewHolder>() {

    inner class DoctorViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvDoctorName: TextView = itemView.findViewById(R.id.tvDoctorName)
        val tvSpecialty: TextView = itemView.findViewById(R.id.tvSpecialty)
        val tvPhone: TextView = itemView.findViewById(R.id.tvPhone)
        val tvServices: TextView = itemView.findViewById(R.id.tvServices)
        val btnApply: Button = itemView.findViewById(R.id.btnBookAppointment)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DoctorViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_doctor, parent, false)
        return DoctorViewHolder(view)
    }

    override fun onBindViewHolder(holder: DoctorViewHolder, position: Int) {
        val doctor = doctors[position]
        holder.tvDoctorName.text = doctor.nameSurname ?: "Неизвестно"
        holder.tvSpecialty.text = doctor.specialties ?: "Не указана"
        holder.tvPhone.text = doctor.phone ?: "Нет телефона"
        holder.tvServices.text = doctor.services ?: "Нет услуг"

        holder.btnApply.setOnClickListener {
            val doctor = doctors[position]
            val bundle = Bundle().apply {
                putInt("doctor_id", doctor.id)
                putString("doctor_name", doctor.nameSurname)
                putString("doctor_phone", doctor.phone)
                putString("doctor_specialty", doctor.specialties)
                putString("doctor_services", doctor.services)
            }
            Log.d("DoctorAdapter", "Navigating to Appointment Fragment with doctor: ${doctor.nameSurname}")
            val navController = Navigation.findNavController(holder.itemView)

            navController.navigate(R.id.action_doctorsBySpecialty_to_appointmentFragment, bundle)
        }



    }

    override fun getItemCount(): Int = doctors.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateDoctors(newDoctors: List<Doctor>) {
        Log.d("DoctorAdapter", "Updating doctors list: $newDoctors")
        doctors = newDoctors
        notifyDataSetChanged()
    }

}
