package kz.olzhass.kolesa.ui.doctors

data class Doctor(
    val id: Int,
    val nameSurname: String,
    val phone: String,
    val specialties: String,
    val services: String?
)