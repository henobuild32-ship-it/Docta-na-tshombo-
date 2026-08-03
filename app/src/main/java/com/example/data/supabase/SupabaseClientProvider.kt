package com.example.data.supabase

import com.example.BuildConfig
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.realtime.realtime
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.storage
import kotlinx.serialization.json.Json

/**
 * Client Supabase partagé par toute l'application.
 * Auth, PostgREST, Realtime et Storage sont installés une seule fois.
 * Le JSON est configuré pour tolérer les colonnes inconnues et les clés snake_case.
 */
object SupabaseClientProvider {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
        coerceInputValues = true
    }

    val client: SupabaseClient by lazy {
        createSupabaseClient(
            supabaseUrl = BuildConfig.SUPABASE_URL,
            supabaseKey = BuildConfig.SUPABASE_ANON_KEY
        ) {
            defaultSerializer = io.github.jan.supabase.serializer.KotlinXSerializer(json)
            install(Auth)
            install(Postgrest) {
                defaultSchema = "public"
            }
            install(Realtime)
            install(Storage)
        }
    }

    val auth: io.github.jan.supabase.auth.Auth get() = client.auth
    val postgrest: io.github.jan.supabase.postgrest.Postgrest get() = client.postgrest
    val realtime: io.github.jan.supabase.realtime.Realtime get() = client.realtime
    val storage: io.github.jan.supabase.storage.Storage get() = client.storage
}
