package com.example.uventapp.data.model

data class Jurusan(val nama: String)
data class Fakultas(val nama: String, val jurusan: List<String>)

val fakultasList = listOf(
    Fakultas(
        nama = "Fakultas Hukum",
        jurusan = listOf("Hukum")
    ),
    Fakultas(
        nama = "Fakultas Pertanian",
        jurusan = listOf(
            "Agroteknologi",
            "Agribisnis",
            "Ilmu Tanah",
            "Proteksi Tanaman",
            "Penyuluhan Pertanian"
        )
    ),
    Fakultas(
        nama = "Fakultas Kedokteran",
        jurusan = listOf(
            "Kedokteran",
            "Psikologi",
            "Kedokteran Gigi",
            "Ilmu Biomedis"
        )
    ),
    Fakultas(
        nama = "Fakultas MIPA",
        jurusan = listOf(
            "Biologi",
            "Kimia",
            "Fisika",
            "Matematika",
            "Statistika"
        )
    ),
    Fakultas(
        nama = "Fakultas Ekonomi dan Bisnis",
        jurusan = listOf(
            "Akuntansi",
            "Administrasi",
            "Keuangan dan Keuangan",
            "Manajemen Pemasaran",
            "Ekonomi",
            "Manajemen",
            "Ekonomi, Kampus Payakumbuh",
            "Manajemen, Kampus Payakumbuh",
            "Ekonomi Islam",
            "Kewirausahaan"
        )
    ),
    Fakultas(
        nama = "Fakultas Peternakan",
        jurusan = listOf(
            "Peternakan",
            "Peternakan, Kampus Payakumbuh"
        )
    ),
    Fakultas(
        nama = "Fakultas Ilmu Budaya",
        jurusan = listOf(
            "Sejarah",
            "Sastra Indonesia",
            "Sastra Inggris",
            "Sastra Minangkabau",
            "Sastra Jepang",
            "Antropologi"
        )
    ),
    Fakultas(
        nama = "Fakultas ISIP",
        jurusan = listOf(
            "Antropologi Sosial",
            "Ilmu Politik",
            "Administrasi Publik",
            "Hubungan Internasional",
            "Ilmu Komunikasi"
        )
    ),
    Fakultas(
        nama = "Fakultas Teknik",
        jurusan = listOf(
            "Teknik Mesin",
            "Teknik Sipil",
            "Teknik Industri",
            "Teknik Lingkungan",
            "Teknik Elektro",
            "Arsitektur"
        )
    ),
    Fakultas(
        nama = "Fakultas Farmasi",
        jurusan = listOf("Farmasi")
    ),
    Fakultas(
        nama = "Fakultas Teknologi Pertanian",
        jurusan = listOf(
            "Teknik Pertanian dan Biosistem",
            "Teknologi Pangan dan Hasil Pertanian",
            "Teknologi Industri Pertanian"
        )
    ),
    Fakultas(
        nama = "Fakultas Kesehatan Masyarakat",
        jurusan = listOf(
            "Kesehatan Masyarakat",
            "Gizi"
        )
    ),
    Fakultas(
        nama = "Fakultas Keperawatan",
        jurusan = listOf("Keperawatan")
    ),
    Fakultas(
        nama = "Fakultas Kedokteran Gigi",
        jurusan = listOf("Kedokteran Gigi")
    ),
    Fakultas(
        nama = "Fakultas Teknologi Informasi",
        jurusan = listOf(
            "Sistem Komputer",
            "Sistem Informasi",
            "Informatika"
        )
    )
)
