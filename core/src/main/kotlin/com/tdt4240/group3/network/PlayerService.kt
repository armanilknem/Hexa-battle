package com.tdt4240.group3.network

import com.tdt4240.group3.network.model.Player
import io.github.jan.supabase.postgrest.postgrest

object PlayerService {
    private val postgrest = SupabaseClient.client.postgrest

    suspend fun getOrCreatePlayer(localId: String): Player? {
        return try {
            // Try to find existing player
            val existing = postgrest["players"].select {
                filter { eq("id", localId) }
            }.decodeSingleOrNull<Player>()

            if (existing != null) return existing

            // Create new guest player
            postgrest["players"].insert(
                Player(id = localId, displayName = "Guest")
            ) {
                select()
            }.decodeSingle<Player>()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
