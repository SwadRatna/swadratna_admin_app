package com.swadratna.swadratna_admin.model

import java.time.LocalTime

enum class StaffStatus {
    ACTIVE,
    INACTIVE,
    ON_BREAK
}

data class Staff(
    val id: String,
    val name: String,
    val position: String,
    val status: StaffStatus = StaffStatus.ACTIVE,
    val workingHours: WorkingHours,
    val imageUrl: String? = null
)

data class WorkingHours(
    val startTime: LocalTime,
    val endTime: LocalTime
)