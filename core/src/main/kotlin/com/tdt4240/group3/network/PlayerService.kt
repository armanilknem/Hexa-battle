package com.tdt4240.group3.network

import com.tdt4240.group3.network.model.Player
import io.github.jan.supabase.postgrest.from

object PlayerService {
    private val client = SupabaseClient.client

    suspend fun getOrCreatePlayer(localId: String, displayName: String? = null): Player? {
        return try {
            // Try to find existing player
            val existing = client.from("players").select {
                filter { eq("id", localId) }
            }.decodeSingleOrNull<Player>()

            if (existing != null) return existing

            // Create new player with provided name or fallback
            val name = displayName ?: "Guest${(1000..9999).random()}"
            client.from("players").insert(
                Player(id = localId, displayName = name)
            ) {
                select()
            }.decodeSingle<Player>()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
