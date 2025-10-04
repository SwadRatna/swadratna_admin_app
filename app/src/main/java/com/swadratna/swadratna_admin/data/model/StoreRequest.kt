package com.swadratna.swadratna_admin.data.model

import com.google.gson.annotations.SerializedName

data class StoreRequest(
    val address: StoreAddressRequest,
    val status: String,
    @SerializedName("location_mobile_number") val locationMobileNumber: String,
    @SerializedName("restaurant_id") val restaurantId: Int,
    @SerializedName("number_of_tables") val numberOfTables: Int
)

data class StoreAddressRequest(
    @SerializedName("plot_no") val plotNo: String,
    @SerializedName("po_box_no") val poBoxNo: String,
    @SerializedName("street_1") val street1: String,
    @SerializedName("street_2") val street2: String,
    val locality: String,
    val city: String,
    val pincode: String,
    val landmark: String
)