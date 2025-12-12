package com.swadratna.swadratna_admin.data.model

import com.google.gson.annotations.SerializedName

data class RestaurantProfileRequest(
    @SerializedName("name") val name: String,
    @SerializedName("gst_number") val gstNumber: String,
    @SerializedName("hq_mobile_no") val hqMobileNo: String,
    @SerializedName("fassai_licence_no") val fassaiLicenceNo: String,
    @SerializedName("youtube_url") val youtubeUrl: String,
    @SerializedName("youtube_description") val youtubeDescription: String,
    @SerializedName("hq_email") val hqEmail: String
)
