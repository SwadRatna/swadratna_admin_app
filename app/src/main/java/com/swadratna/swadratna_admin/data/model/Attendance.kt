package com.swadratna.swadratna_admin.data.model

import com.google.gson.annotations.SerializedName

data class AttendanceResponse(
    @SerializedName("attendance")
    val attendance: List<AttendanceDay>
)

data class AttendanceDay(
    @SerializedName("date")
    val date: String,
    @SerializedName("staff")
    val staff: List<AttendanceStaff>,
    @SerializedName("summary")
    val summary: AttendanceSummary
)

data class AttendanceStaff(
    @SerializedName("staff_id")
    val staffId: Int,
    @SerializedName("name")
    val name: String,
    @SerializedName("status")
    val status: String,
    @SerializedName("check_in")
    val checkIn: String?,
    @SerializedName("check_out")
    val checkOut: String?,
    @SerializedName("total_hours")
    val totalHours: Double,
    @SerializedName("is_late")
    val isLate: Boolean
)

data class AttendanceSummary(
    @SerializedName("total")
    val total: Int,
    @SerializedName("present")
    val present: Int,
    @SerializedName("absent")
    val absent: Int,
    @SerializedName("on_leave")
    val onLeave: Int
)
