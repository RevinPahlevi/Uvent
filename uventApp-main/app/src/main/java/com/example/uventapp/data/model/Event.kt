package com.example.uventapp.data.model

data class Event(
    val id: String,
    val title: String,
    val type: String,
    val date: String,
    val time: String,
    val location: String,
    val quota: String,
    val status: String,
    val posterUrl: String
)

val dummyEvents = listOf(
    Event(
        id = "1",
        title = "Business Talkshow: Future Entrepreneur",
        type = "Talkshow",
        date = "Kamis, 16 Oktober 2025",
        time = "09:00 - 12:00",
        location = "Auditorium Unand",
        quota = "500 Peserta",
        status = "Gratis",
        posterUrl = "https://i.ibb.co/4j3yY5d/eventposter1.jpg"
    ),
    Event(
        id = "2",
        title = "Seminar Nasional Teknologi & Inovasi",
        type = "Seminar",
        date = "Kamis, 23 Oktober 2025",
        time = "08:30 - 12:00",
        location = "Convention Hall FTI UNAND",
        quota = "300 Peserta",
        status = "Tersedia",
        posterUrl = "https://i.ibb.co/GW3Q4sC/eventposter2.jpg"
    ),
    Event(
        id = "3",
        title = "Workshop UI/UX Design for Beginner",
        type = "Workshop",
        date = "Jumat, 31 Oktober 2025",
        time = "13:00 - 16:00",
        location = "Lab Multimedia FTI UNAND",
        quota = "60 Peserta",
        status = "Kuota Habis",
        posterUrl = "https://i.ibb.co/JB5WTbM/eventposter3.jpg"
    ),
    Event(
        id = "4",
        title = "Bootcamp: Data Science & AI",
        type = "Bootcamp",
        date = "Senin, 03 November 2025",
        time = "09:00 - 17:00",
        location = "Aula Fakultas MIPA Unand",
        quota = "120 Peserta",
        status = "Early Bird",
        posterUrl = "https://i.ibb.co/C9wC8bV/eventposter4.jpg"
    ),
    Event(
        id = "5",
        title = "Webinar: Career in Tech Industry",
        type = "Webinar",
        date = "Selasa, 11 November 2025",
        time = "19:00 - 21:00 WIB",
        location = "Zoom Meeting",
        quota = "1000 Peserta",
        status = "Gratis",
        posterUrl = "https://i.ibb.co/pjtq3kC/eventposter5.jpg"
    )
)
