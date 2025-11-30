package com.swadratna.swadratna_admin.data.model

import com.google.gson.annotations.SerializedName

data class SalesResponse(
    @SerializedName("pagination") val pagination: Pagination? = null,
    @SerializedName("sales") val sales: List<SaleDto>? = null,
    @SerializedName("summary") val summary: Summary? = null
)

data class Pagination(
    @SerializedName("current_page") val currentPage: Int? = null,
    @SerializedName("has_next") val hasNext: Boolean? = null,
    @SerializedName("has_prev") val hasPrev: Boolean? = null,
    @SerializedName("per_page") val perPage: Int? = null,
    @SerializedName("total_count") val totalCount: Int? = null,
    @SerializedName("total_pages") val totalPages: Int? = null
)

data class SaleDto(
    @SerializedName("id") val id: Long? = null,
    @SerializedName("bill_number") val billNumber: String? = null,
    @SerializedName("order_type") val orderType: String? = null,
    @SerializedName("amount") val amount: Double? = null,
    @SerializedName("status") val status: String? = null,
    @SerializedName("payment_mode") val paymentMode: String? = null,
    @SerializedName("created_at") val createdAt: String? = null,
    @SerializedName("edited_by") val editedBy: String? = null,
    @SerializedName("edited_at") val editedAt: String? = null
)

data class Summary(
    @SerializedName("count") val count: Int? = null,
    @SerializedName("total_amount") val totalAmount: Double? = null
)
