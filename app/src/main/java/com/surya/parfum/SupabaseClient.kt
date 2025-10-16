package com.surya.parfum

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.storage

object SupabaseClient {
    private const val SUPABASE_URL = "https://dwrrlzrvvqzdwysdspay.supabase.co" // Tetap sama
    private const val SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImR3cnJsenJ2dnF6ZHd5c2RzcGF5Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjA2Mjc3OTEsImV4cCI6MjA3NjIwMzc5MX0.s3sz52ThF9EmvCQG-z9-84eUD04ebdnu29sGa0jmtfw" // Tetap sama

    val client = createSupabaseClient(
        supabaseUrl = SUPABASE_URL,
        supabaseKey = SUPABASE_KEY
    ) {
        install(Storage) // Daftarkan modul Storage
    }
}