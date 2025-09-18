package com.swadratna.swadratna_admin.model

import java.time.LocalDate

enum class AttendanceStatus {
    PRESENT,
    ABSENT,
    LEAVE
}

data class Attendance(
    val id: String,
    val staffId: String,
    val date: LocalDate,
    val status: AttendanceStatus,
    val dailyWage: Double
)

data class AttendanceSummary(
    val totalStaff: Int,
    val presentCount: Int,
    val absentCount: Int,
    val leaveCount: Int
)