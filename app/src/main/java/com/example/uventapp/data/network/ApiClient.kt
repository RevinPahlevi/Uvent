package com.example.uventapp.data.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {

<<<<<<< HEAD:app/src/main/java/com/example/uventapp/data/network/ApiClient.kt
    // --- UPDATED: Menggunakan IP Laptop Anda yang baru ---
    // Pastikan server backend Anda (Node.js) berjalan di port 3000
    private const val BASE_URL = "http://172.20.10.3/api/"
=======
    // --- UNTUK ANDROID EMULATOR: Gunakan 10.0.2.2 untuk akses localhost ---
    // 10.0.2.2 adalah alamat khusus yang mengarah ke localhost laptop Anda dari emulator
    // JANGAN gunakan 'localhost' atau '127.0.0.1' - itu akan merujuk ke emulator itu sendiri
    private const val BASE_URL = "http://10.44.9.220:3000/api/"
>>>>>>> 82d5226370c90fb58dc7eabd0094e85d78386693:uventApp-main/app/src/main/java/com/example/uventapp/data/network/ApiClient.kt

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
