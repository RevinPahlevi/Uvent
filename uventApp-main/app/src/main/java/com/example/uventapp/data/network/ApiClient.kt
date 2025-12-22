package com.example.uventapp.data.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {

    // --- UNTUK ANDROID EMULATOR: Gunakan 10.0.2.2 untuk akses localhost ---
    // 10.0.2.2 adalah alamat khusus yang mengarah ke localhost laptop Anda dari emulator
    // JANGAN gunakan 'localhost' atau '127.0.0.1' - itu akan merujuk ke emulator itu sendiri
    private const val BASE_URL = "http://192.168.1.119:3000/api/"

    val instance: ApiService by lazy {
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
