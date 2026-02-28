package com.swadratna.swadratna_admin.data.model

import com.google.gson.annotations.SerializedName

data class SalesInfoResponse(
    @SerializedName("sales_info") val salesInfo: List<SalesInfoItem>
)

data class SalesInfoItem(
    @SerializedName("name") val name: String,
    @SerializedName("qty") val qty: Int,
    @SerializedName("revenue") val revenue: Double
)
