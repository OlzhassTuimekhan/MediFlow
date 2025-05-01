package kz.olzhass.kolesa.ui.appointment


data class Appointment(
    val appointment_id: Int,
    val user_id: Int,
    val doctor_id: Int,
    val appointment_date: String,
    val appointment_time: String,
    val appointment_phone: String,
    val appointment_reason: String,
    val appointment_status: String,
    val appointment_created_at: String,
    val doctor_name: String,
    val doctor_phone: String,
    val doctor_email: String,
    val doctor_created_at: String
)
