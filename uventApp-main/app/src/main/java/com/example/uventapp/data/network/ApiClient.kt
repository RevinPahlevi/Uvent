package com.example.uventapp.data.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {

    // --- PERBAIKAN DI SINI ---
    // IP Address Anda sudah dimasukkan.
    // Pastikan HP Anda dan Laptop Anda terhubung ke jaringan WiFi yang SAMA.
    private const val BASE_URL = "http://192.168.1.40:3000/api/"

    val instance: ApiService by lazy {
        // Ini akan nge-print request & response API di Logcat (sangat membantu)
        val logging = HttpLoggingInterceptor()
        logging.setLevel(HttpLoggingInterceptor.Level.BODY)

        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        retrofit.create(ApiService::class.java)
    }
}
