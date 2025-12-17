package com.swadratna.swadratna_admin.data.remote.dto

data class CustomerDto(
    val id: String,
    val name: String?,
    val email: String?,
    val phone: String?,
    val status: String?,
    val blocked: Boolean? = null,
    val deleted: Boolean? = null
)

data class CustomerListResponse(
    val data: List<CustomerDto>,
    val pagination: Pagination?
)

data class Pagination(
    val page: Int?,
    val limit: Int?,
    val total: Int?,
    val total_pages: Int?,
    val has_next: Boolean?,
    val has_prev: Boolean?
)

