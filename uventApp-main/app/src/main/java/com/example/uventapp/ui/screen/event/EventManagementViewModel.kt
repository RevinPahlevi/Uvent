package com.example.uventapp.ui.screen.event

import android.content.Context
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.uventapp.data.model.Event
import com.example.uventapp.data.model.Registration
import com.example.uventapp.data.model.Feedback
import com.example.uventapp.data.model.Documentation
// --- IMPORT BARU UNTUK API ---
import com.example.uventapp.data.network.ApiClient
import com.example.uventapp.data.network.CreateEventRequest
import com.example.uventapp.data.network.CreateEventResponse
import com.example.uventapp.data.network.EventResponse
import com.example.uventapp.data.network.GetEventsResponse
import com.example.uventapp.utils.isNetworkAvailable
// --- IMPORT DATA DUMMY EVENT ---
import com.example.uventapp.data.model.dummyEvents
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// (dummyFeedbacks dan dummyDocumentation tetap ada, tidak dihapus)
private val dummyFeedbacks = mutableListOf(
    Feedback(
        id = 100,
        eventId = 1,
        rating = 4,
        review = "Event sangat bagus! Pembicaranya inspiratif dan materi sangat bermanfaat.",
        photoUri = null,
        userName = "Loly Amelia Nurza",
        postDate = "19 Oktober 2025",
        isAnda = true
    ),
    Feedback(
        id = 101,
        eventId = 1,
        rating = 5,
        review = "Event sangat bagus! Pembicaranya inspiratif dan materi sangat bermanfaat.",
        photoUri = null,
        userName = "Revin Ackerman",
        postDate = "17 Oktober 2025",
        isAnda = false
    ),
    Feedback(
        id = 102,
        eventId = 1,
        rating = 3,
        review = "Overall bagus, tapi Materi yang disampaikan kadang terlalu susah dipahami",
        photoUri = null,
        userName = "Aldo Francisco",
        postDate = "19 Oktober 2025",
        isAnda = false
    )
)
private val dummyDocumentation = mutableListOf(
    Documentation(
        id = 200,
        eventId = 1,
        description = "Event sangat bagus! Pembicaranya inspiratif dan materi sangat bermanfaat.",
        photoUri = null,
        userName = "Loly Amelia Nurza",
        postDate = "17 Oktober 2025",
        postTime = "14:30",
        isAnda = false
    ),
    Documentation(
        id = 201,
        eventId = 1,
        description = "Suasana event-nya seru banget!",
        photoUri = null,
        userName = "Aldo Francisco",
        postDate = "17 Oktober 2025",
        postTime = "15:01",
        isAnda = false
    )
)

// --- HELPER UNTUK KONVERSI DATA ---
// Mengubah format tanggal DB (YYYY-MM-DD) ke UI (D/M/YYYY)
private fun reformatDateForUi(dbDate: String?): String {
    if (dbDate == null) return "N/A"
    return try {
        val cleanDate = dbDate.split("T").first()
        val parser = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val formatter = SimpleDateFormat("d/M/yyyy", Locale.getDefault())
        formatter.format(parser.parse(cleanDate) ?: Date())
    } catch (e: Exception) {
        dbDate
    }
}

// Mengubah format waktu DB (HH:mm:ss) ke UI (HH:mm)
private fun reformatTimeForUi(dbTime: String?): String {
    if (dbTime == null) return "N/A"
    return try {
        val parser = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
        formatter.format(parser.parse(dbTime) ?: Date())
    } catch (e: Exception) {
        dbTime
    }
}

// Mengubah 1 EventResponse (dari API) menjadi 1 Event (untuk UI)
private fun EventResponse.toEventModel(): Event {
    return Event(
        id = this.id,
        title = this.title,
        type = this.type ?: "N/A",
        date = reformatDateForUi(this.date),
        timeStart = reformatTimeForUi(this.timeStart),
        timeEnd = reformatTimeForUi(this.timeEnd),
        platformType = this.platformType ?: "N/A",
        locationDetail = this.locationDetail ?: "N/A",
        quota = this.quota?.toString() ?: "0",
        status = this.status ?: "Aktif",
        thumbnailResId = null, // Kita tidak pakai ResId lagi
        thumbnailUri = this.thumbnailUri
    )
}
// ---------------------------------


class EventManagementViewModel : ViewModel() {

    // --- STATE BARU UNTUK EVENT DARI SERVER ---
    private val _allEvents = mutableStateOf<List<Event>>(emptyList())
    val allEvents: State<List<Event>> = _allEvents
    // ------------------------------------------

    private val _createdEvents = mutableStateOf<List<Event>>(emptyList())
    val createdEvents: State<List<Event>> = _createdEvents

    private val _followedEvents = mutableStateListOf<Event>()
    val followedEvents: List<Event> = _followedEvents

    private val _notificationMessage = mutableStateOf<String?>(null)
    val notificationMessage: State<String?> = _notificationMessage

    private val _registrations = mutableStateOf<Map<Int, Registration>>(emptyMap())
    val registrations: State<Map<Int, Registration>> = _registrations

    private val _feedbacks = mutableStateOf<Map<Int, List<Feedback>>>(
        mapOf(1 to dummyFeedbacks)
    )
    val feedbacks: State<Map<Int, List<Feedback>>> = _feedbacks

    private val _documentations = mutableStateOf<Map<Int, List<Documentation>>>(
        mapOf(1 to dummyDocumentation)
    )
    val documentations: State<Map<Int, List<Documentation>>> = _documentations

    private val _likedDocIds = mutableStateOf(setOf<Int>())
    val likedDocIds: State<Set<Int>> = _likedDocIds


    // --- FUNGSI DIPERBAIKI (untuk menggabungkan data dummy) ---
    fun loadAllEvents(context: Context) {
        if (!isNetworkAvailable(context)) {
            _notificationMessage.value = "Offline. Gagal memuat event."
            _allEvents.value = dummyEvents // Jika offline, tampilkan dummy
            return
        }

        ApiClient.instance.getAllEvents().enqueue(object : Callback<GetEventsResponse> {
            override fun onResponse(call: Call<GetEventsResponse>, response: Response<GetEventsResponse>) {
                val body = response.body()
                if (response.isSuccessful && body?.status == "success") {

                    // 1. Ambil List<EventResponse> dari API
                    val eventsFromApi = body.data.map { it.toEventModel() }

                    // 2. Gabungkan dengan dummyEvents (pastikan tidak ada duplikat ID)
                    val combinedList = (eventsFromApi + dummyEvents).distinctBy { it.id }

                    // 3. Simpan ke state
                    _allEvents.value = combinedList

                } else {
                    Log.e("ViewModel", "Gagal memuat event: ${response.message()}")
                    _notificationMessage.value = "Gagal memuat event."
                    _allEvents.value = dummyEvents // Tampilkan dummy jika API error
                }
            }

            override fun onFailure(call: Call<GetEventsResponse>, t: Throwable) {
                Log.e("ViewModel", "API Failure (loadAllEvents): ${t.message}")
                _notificationMessage.value = "Gagal terhubung ke server."
                _allEvents.value = dummyEvents // Tampilkan dummy jika server offline
            }
        })
    }
    // -----------------------------------

    // (Fungsi feedback, documentation, registration tetap sama)
    fun getFeedbacksForEvent(eventId: Int): List<Feedback> { return _feedbacks.value[eventId] ?: emptyList() }
    fun submitFeedback(eventId: Int, feedback: Feedback) { /* ... */ }
    fun deleteFeedback(eventId: Int, feedbackId: Int) { /* ... */ }
    fun getDocumentationForEvent(eventId: Int): List<Documentation> { return _documentations.value[eventId] ?: emptyList() }
    fun submitDocumentation(eventId: Int, doc: Documentation) { /* ... */ }
    fun deleteDocumentation(eventId: Int, docId: Int) { /* ... */ }
    fun toggleDocumentationLike(docId: Int) { /* ... */ }
    fun registerForEvent(event: Event, data: Registration) { /* ... */ }
    fun getRegistrationData(eventId: Int): Registration? { return _registrations.value[eventId] }
    fun updateRegistrationData(eventId: Int, newData: Registration) { /* ... */ }
    fun unfollowEvent(eventId: Int) { /* ... */ }

    // --- FUNGSI DIPERBAIKI (untuk update _createdEvents) ---
    fun addEvent(event: Event, context: Context) {
        // 1. Cek koneksi
        if (!isNetworkAvailable(context)) {
            _notificationMessage.value = "Tidak ada koneksi internet."
            return
        }

        // 2. Buat Request Body untuk API
        val request = CreateEventRequest(
            title = event.title,
            type = event.type,
            date = event.date,
            timeStart = event.timeStart,
            timeEnd = event.timeEnd,
            platformType = event.platformType,
            locationDetail = event.locationDetail,
            quota = event.quota,
            status = event.status,
            thumbnailUri = event.thumbnailUri
        )

        // 3. Panggil API
        ApiClient.instance.createEvent(request).enqueue(object : Callback<CreateEventResponse> {
            override fun onResponse(call: Call<CreateEventResponse>, response: Response<CreateEventResponse>) {
                val body = response.body()
                if (response.isSuccessful && body?.status == "success") {
                    _notificationMessage.value = "Event Berhasil Ditambahkan"

                    // Muat ulang semua event (termasuk dummy + API)
                    loadAllEvents(context)

                    // Tambahkan juga ke list _createdEvents (untuk "Event Saya")
                    _createdEvents.value = _createdEvents.value + listOf(event)

                } else {
                    Log.e("ViewModel", "Gagal menambah event: ${response.message()}")
                    _notificationMessage.value = "Gagal menambah event: ${response.message()}"
                }
            }
            override fun onFailure(call: Call<CreateEventResponse>, t: Throwable) {
                Log.e("ViewModel", "API Failure (addEvent): ${t.message}")
                _notificationMessage.value = "Gagal terhubung ke server."
            }
        })
    }
    // ----------------------------------

    fun updateEvent(updatedEvent: Event) {
        // TODO: Buat API untuk Update Event
        _createdEvents.value = _createdEvents.value.map {
            if (it.id == updatedEvent.id) updatedEvent else it
        }
        _notificationMessage.value = "Event Berhasil DiEdit (Lokal)"
    }

    fun deleteEvent(eventId: Int) {
        // TODO: Buat API untuk Delete Event
        _createdEvents.value = _createdEvents.value.filter { it.id != eventId }
        _notificationMessage.value = "Event Berhasil Dihapus (Lokal)"
    }

    // --- FUNGSI DIPERBAIKI (sesuai error "Missing return") ---
    // Tambahkan tipe return : Event?
    fun getEventById(eventId: Int): Event? {
        // Cari di semua list yang mungkin
        return _allEvents.value.find { it.id == eventId } // <-- Daftar gabungan (API + Dummy)
            ?: _createdEvents.value.find { it.id == eventId } // <-- Daftar buatan lokal
            ?: _followedEvents.find { it.id == eventId } // <-- Daftar yang diikuti
            ?: dummyEvents.find { it.id == eventId } // <-- Fallback terakhir
    }
    // -----------------------------------------

    fun clearNotification() {
        _notificationMessage.value = null
    }
}
