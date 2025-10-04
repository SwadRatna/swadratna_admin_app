package com.swadratna.swadratna_admin.data.model

import com.google.gson.annotations.SerializedName
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class Store(
    val id: Int,
    @SerializedName("restaurant_name") val name: String,
    val address: StoreAddress,
    @SerializedName("restaurant_id") val restaurantId: Int,
    @SerializedName("tenant_id") val tenantId: Int,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("updated_at") val updatedAt: String,
    val status: String,
    @SerializedName("location_mobile_number") val locationMobileNumber: String,
    @SerializedName("number_of_tables") val numberOfTables: Int,
    @SerializedName("image_url") val imageUrl: String? = null
) {
    fun getFormattedCreationDate(): String {
        return createdAt.split("T")[0]
    }
    
    fun getFullAddress(): String {
        return with(address) {
            listOfNotNull(
                plotNo.takeIf { it.isNotEmpty() },
                poBoxNo.takeIf { it.isNotEmpty() },
                street1.takeIf { it.isNotEmpty() },
                street2.takeIf { it.isNotEmpty() },
                locality.takeIf { it.isNotEmpty() },
                city.takeIf { it.isNotEmpty() },
                pincode.takeIf { it.isNotEmpty() }
            ).joinToString(", ")
        }
    }
}

data class StoreAddress(
    @SerializedName("plot_no") val plotNo: String,
    @SerializedName("po_box_no") val poBoxNo: String,
    @SerializedName("street_1") val street1: String,
    @SerializedName("street_2") val street2: String,
    val locality: String,
    val city: String,
    val pincode: String,
    val landmark: String
)

data class StoreResponse(
    val stores: List<Store>,
    val pagination: StorePagination
)

data class StorePagination(
    val page: Int,
    val limit: Int,
    val total: Int,
    @SerializedName("total_pages") val totalPages: Int,
    @SerializedName("has_next") val hasNext: Boolean,
    @SerializedName("has_prev") val hasPrev: Boolean
)

data class StoreStats(
    val totalSales: Double,
    val totalOrders: Int,
    val averageOrderValue: Double,
    val topSellingItems: List<String>
)

data class CreateStoreRequest(
    val address: StoreAddress,
    val status: String,
    @SerializedName("location_mobile_number") val locationMobileNumber: String,
    @SerializedName("restaurant_id") val restaurantId: Int,
    @SerializedName("number_of_tables") val numberOfTables: Int
)

data class UpdateStoreRequest(
    val address: StoreAddress,
    val status: String,
    @SerializedName("location_mobile_number") val locationMobileNumber: String,
    @SerializedName("restaurant_id") val restaurantId: Int,
    @SerializedName("number_of_tables") val numberOfTables: Int
)