package com.example.uventapp.data.repository

import com.example.uventapp.data.model.Event

object EventRepository {

    val events = listOf(
        Event(
            id = "1",
            title = "Seminar Nasional Teknologi",
            type = "Seminar",
            date = "12 Januari 2025",
            time = "09:00 - 12:00",
            location = "Auditorium UNAND",
            quota = "200 Peserta",
            status = "Tersedia",
            posterUrl = "https://i.ibb.co/4j3yY5d/eventposter1.jpg"
        ),
        Event(
            id = "2",
            title = "Talkshow - Karir di Bidang IT",
            type = "Talkshow",
            date = "20 Januari 2025",
            time = "13:00 - 15:00",
            location = "Online via Zoom",
            quota = "500 Peserta",
            status = "Tersedia",
            posterUrl = "https://i.ibb.co/GW3Q4sC/eventposter2.jpg"
        ),
        Event(
            id = "3",
            title = "Workshop UI/UX Design",
            type = "Workshop",
            date = "25 Januari 2025",
            time = "08:30 - 12:30",
            location = "Ruang Seminar FTI UNAND",
            quota = "60 Peserta",
            status = "Kuota Habis",
            posterUrl = "https://i.ibb.co/JB5WTbM/eventposter3.jpg"
        )
    )

    fun getEventById(eventId: String?): Event? {
        return events.find { it.id == eventId }
    }
}
