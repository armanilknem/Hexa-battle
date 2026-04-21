package com.tdt4240.group3.network

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.postgrest.Postgrest

object SupabaseClient {
    // supabaseKey is the publishable anon/client key — safe to ship in client apps.
    // Row-level security on the Supabase project enforces access control server-side.
    val client by lazy {
        createSupabaseClient(
            supabaseUrl = "https://wbubvspwwbxhucokvflo.supabase.co",
            supabaseKey = "sb_publishable_1oIBGk7uexuWhZ6tULmb4g_Pu7rI9fS"
        ) {
            install(Realtime)
            install(Postgrest)
        }
    }
}
