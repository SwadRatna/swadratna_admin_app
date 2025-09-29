package com.swadratna.swadratna_admin.data.remote

import com.squareup.moshi.JsonClass
import com.swadratna.swadratna_admin.data.model.MenuItem

@JsonClass(generateAdapter = true)
data class MenuItemDto(
    val id: Int,
    val name: String,
    val description: String,
    val price: Double,
    val availability: String,
    val isAvailable: Boolean
)

fun MenuItemDto.toDomain() = MenuItem(
    id = id,
    name = name,
    description = description,
    price = price,
    availability = availability,
    isAvailable = isAvailable
)
