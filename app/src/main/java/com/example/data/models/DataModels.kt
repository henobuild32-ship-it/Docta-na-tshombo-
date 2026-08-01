package com.example.data.models

enum class UserRole {
    PATIENT,
    PRATICIEN
}

enum class AppointmentType {
    PRESENTIEL,
    TELECONSULTATION
}

enum class AppointmentStatus {
    CONFIRMED,
    CANCELLED,
    COMPLETED
}

data class PractitionerSlot(
    val dayOfWeek: String,
    val time: String,
    val isAvailable: Boolean = true
)
