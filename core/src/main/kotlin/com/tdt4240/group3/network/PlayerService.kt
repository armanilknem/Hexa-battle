package com.tdt4240.group3.network

import com.badlogic.gdx.Gdx
import com.tdt4240.group3.config.GameConstants
import com.tdt4240.group3.network.model.Player
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.withTimeout

/** Manages reads and writes to the `players` table. */
object PlayerService {
    private const val TAG = "PlayerService"
    private val client = SupabaseClient.client

    /**
     * Returns the existing player record for [localId], or creates one with [displayName]
     * (or a random guest name if [displayName] is `null`).
     * Returns `null` on error.
     */
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
            Gdx.app.error(TAG, "Failed to get or create player $localId", e)
            null
        }
    }

    /**
     * Updates the `display_name` column for [localId].
     * Returns `true` on success, `false` on error.
     */
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
            Gdx.app.error(TAG, "Failed to update display name for player $localId", e)
            false
        }
    }
}
