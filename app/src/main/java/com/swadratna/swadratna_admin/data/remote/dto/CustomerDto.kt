package com.swadratna.swadratna_admin.data.remote.dto

data class CustomerDto(
    val id: String,
    val name: String?,
    val email: String?,
    val mobile_number: String?,
    val status: String?,
    val blocked: Boolean? = null,
    val deleted: Boolean? = null
)

data class CustomerListResponse(
    val data: List<CustomerDto>,
    val pagination: Pagination?,
    val date_range: DateRangeDto? = null,
    val growth: GrowthDto? = null
)

data class Pagination(
    val page: Int?,
    val limit: Int?,
    val total: Int?,
    val total_pages: Int?,
    val has_next: Boolean?,
    val has_prev: Boolean?
)

data class DateRangeDto(
    val from_date: String?,
    val to_date: String?
)

data class GrowthDto(
    val current_period_count: Int?,
    val growth_percentage: Double?,
    val previous_period: DateRangeDto?,
    val previous_period_count: Int?
)

