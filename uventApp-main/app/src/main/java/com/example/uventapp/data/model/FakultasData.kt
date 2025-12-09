package com.example.uventapp.data.model

data class Jurusan(val nama: String)
data class Fakultas(val nama: String, val jurusan: List<String>)

val fakultasList = listOf(
    Fakultas(
        nama = "Fakultas Teknik",
        jurusan = listOf(
            "Teknik Sipil",
            "Teknik Mesin",
            "Teknik Elektro",
            "Teknik Kimia"
        )
    ),
    Fakultas(
        nama = "Fakultas Ekonomi",
        jurusan = listOf(
            "Manajemen",
            "Akuntansi",
            "Ekonomi Pembangunan"
        )
    ),
    Fakultas(
        nama = "Fakultas Teknologi Informasi",
        jurusan = listOf(
            "Informatika",
            "Sistem Informasi",
            "Data Science"
        )
    ),
    Fakultas(
        nama = "Fakultas Hukum",
        jurusan = listOf(
            "Ilmu Hukum"
        )
    ),
    Fakultas(
        nama = "Fakultas Kedokteran",
        jurusan = listOf(
            "Kedokteran Umum",
            "Kedokteran Gigi"
        )
    )
)
