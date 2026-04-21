package com.tdt4240.group3.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** DTO for a row in the `players` table. [id] is the Supabase auth / device UUID. */
@Serializable
data class Player(
    val id: String? = null,
    @SerialName("display_name") val displayName: String,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null
)
