package com.tdt4240.group3.network

import com.tdt4240.group3.config.GameConstants
import com.tdt4240.group3.network.model.Player
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.withTimeout

object PlayerService {
    private val client = SupabaseClient.client

    suspend fun getOrCreatePlayer(localId: String, displayName: String? = null): Player? {
        return try {
            withTimeout(GameConstants.NETWORK_TIMEOUT_MS) {
                val existing = client.from("players").select {
                    filter { eq("id", localId) }
                }.decodeSingleOrNull<Player>()

                if (existing != null) return@withTimeout existing

                val name = displayName ?: "Guest${(1000..9999).random()}"
                client.from("players").insert(
                    Player(id = localId, displayName = name)
                ) {
                    select()
                }.decodeSingle<Player>()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun updateDisplayName(localId: String, newName: String): Boolean {
        return try {
            withTimeout(GameConstants.NETWORK_TIMEOUT_MS) {
                client.from("players").update({
                    set("display_name", newName)
                }) {
                    filter {
                        eq("id", localId)
                    }
                }
                true
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
