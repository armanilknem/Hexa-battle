package com.tdt4240.group3.network

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.postgrest.Postgrest

object SupabaseClient {
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
