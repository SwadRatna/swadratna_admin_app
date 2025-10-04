package com.swadratna.swadratna_admin.data.model

import com.google.gson.annotations.SerializedName
import java.time.LocalTime

enum class StaffStatus {
    @SerializedName("active") ACTIVE,
    @SerializedName("inactive") INACTIVE,
    @SerializedName("on_break") ON_BREAK
}

data class Staff(
    val id: Int,
    val name: String?,
    val position: String?,
    val email: String? = null,
    val phone: String? = null,
    @SerializedName("mobile_number") val mobileNumber: String? = null,
    val address: String? = null,
    val salary: Double? = null,
    @SerializedName("join_date") val joinDate: String? = null,
    val status: StaffStatus = StaffStatus.ACTIVE,
    @SerializedName("working_hours") val workingHours: WorkingHours? = null,
    @SerializedName("shift_timing") val shiftTiming: ShiftTiming? = null,
    @SerializedName("image_url") val imageUrl: String? = null,
    @SerializedName("store_id") val storeId: Int? = null,
    @SerializedName("created_at") val createdAt: String? = null,
    @SerializedName("updated_at") val updatedAt: String? = null
)

data class WorkingHours(
    @SerializedName("start_time") val startTime: String?,
    @SerializedName("end_time") val endTime: String?
) {
    fun getStartTimeAsLocalTime(): LocalTime? {
        return try {
            startTime?.let { LocalTime.parse(it) }
        } catch (e: Exception) {
            null
        }
    }
    
    fun getEndTimeAsLocalTime(): LocalTime? {
        return try {
            endTime?.let { LocalTime.parse(it) }
        } catch (e: Exception) {
            null
        }
    }
}

data class StaffResponse(
    val staff: List<Staff>?,
    val pagination: StaffPagination? = null
)

data class StaffPagination(
    val page: Int,
    val limit: Int,
    val total: Int,
    @SerializedName("total_pages") val totalPages: Int,
    @SerializedName("has_next") val hasNext: Boolean,
    @SerializedName("has_prev") val hasPrev: Boolean
)

data class ShiftTiming(
    @SerializedName("start_time") val startTime: String,
    @SerializedName("end_time") val endTime: String
)

data class CreateStaffRequest(
    val address: String,
    val email: String,
    @SerializedName("join_date") val joinDate: String,
    val name: String,
    val phone: String,
    val role: String,
    val salary: Double,
    @SerializedName("shift_timing") val shiftTiming: ShiftTiming,
    val status: String,
    @SerializedName("store_id") val storeId: Int
)

data class UpdateStaffRequest(
    val address: String,
    val email: String,
    @SerializedName("join_date") val joinDate: String,
    val name: String,
    val phone: String,
    @SerializedName("mobile_number") val mobileNumber: String,
    val role: String,
    val salary: Double,
    @SerializedName("shift_timing") val shiftTiming: ShiftTiming,
    val status: String,
    val password: String? = null
)

data class StaffOperationResponse(
    val success: Boolean,
    val message: String,
    val staff: Staff? = null
)