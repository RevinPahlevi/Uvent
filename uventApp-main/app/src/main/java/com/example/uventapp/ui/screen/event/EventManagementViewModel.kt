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
import com.example.uventapp.data.network.ApiClient
import com.example.uventapp.data.network.CreateEventRequest
import com.example.uventapp.data.network.CreateEventResponse
import com.example.uventapp.data.network.EventResponse
import com.example.uventapp.data.network.GetEventsResponse
import com.example.uventapp.utils.isNetworkAvailable
import com.example.uventapp.data.model.dummyEvents
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// --- DATA DUMMY (Tidak Berubah) ---
private val dummyFeedbacks = mutableListOf(
    Feedback(
        id = 100, eventId = 1, rating = 4, review = "Event sangat bagus!", photoUri = null, userName = "Loly Amelia", postDate = "19 Oktober 2025", isAnda = true
    ),
    Feedback(
        id = 101, eventId = 1, rating = 5, review = "Sangat inspiratif.", photoUri = null, userName = "Revin Ackerman", postDate = "17 Oktober 2025", isAnda = false
    )
)
private val dummyDocumentation = mutableListOf(
    Documentation(
        id = 200, eventId = 1, description = "Suasana seru!", photoUri = null, userName = "Loly Amelia", postDate = "17 Oktober 2025", postTime = "14:30", isAnda = false
    )
)

// --- HELPER FUNCTION ---
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
        thumbnailResId = null,
        thumbnailUri = this.thumbnailUri,
        creatorId = this.creatorId
    )
}

class EventManagementViewModel : ViewModel() {

    // 1. State untuk SEMUA event (dari server public)
    private val _allEvents = mutableStateOf<List<Event>>(emptyList())
    val allEvents: State<List<Event>> = _allEvents

    // 2. State untuk EVENT SAYA (yang dibuat user) - DIKEMBALIKAN SESUAI PERMINTAAN
    private val _createdEvents = mutableStateOf<List<Event>>(emptyList())
    val createdEvents: State<List<Event>> = _createdEvents

    // State lainnya
    private val _followedEvents = mutableStateListOf<Event>()
    val followedEvents: List<Event> = _followedEvents

    private val _notificationMessage = mutableStateOf<String?>(null)
    val notificationMessage: State<String?> = _notificationMessage

    private val _registrations = mutableStateOf<Map<Int, Registration>>(emptyMap())
    val registrations: State<Map<Int, Registration>> = _registrations

    private val _feedbacks = mutableStateOf<Map<Int, List<Feedback>>>(mapOf(1 to dummyFeedbacks))
    val feedbacks: State<Map<Int, List<Feedback>>> = _feedbacks

    private val _documentations = mutableStateOf<Map<Int, List<Documentation>>>(mapOf(1 to dummyDocumentation))
    val documentations: State<Map<Int, List<Documentation>>> = _documentations

    private val _likedDocIds = mutableStateOf(setOf<Int>())
    val likedDocIds: State<Set<Int>> = _likedDocIds

    /**
     * Mengambil semua event (untuk halaman Home/Event List)
     */
    fun loadAllEvents(context: Context) {
        if (!isNetworkAvailable(context)) {
            _notificationMessage.value = "Offline. Menampilkan data dummy."
            _allEvents.value = dummyEvents
            return
        }

        ApiClient.instance.getAllEvents().enqueue(object : Callback<GetEventsResponse> {
            override fun onResponse(call: Call<GetEventsResponse>, response: Response<GetEventsResponse>) {
                val body = response.body()
                if (response.isSuccessful && body?.status == "success") {
                    val eventsFromApi = body.data.map { it.toEventModel() }
                    // Gabungkan dengan dummy (opsional, agar tidak kosong saat dev)
                    _allEvents.value = (eventsFromApi + dummyEvents).distinctBy { it.id }
                } else {
                    Log.e("ViewModel", "Gagal loadAllEvents: ${response.message()}")
                    _allEvents.value = dummyEvents
                }
            }
            override fun onFailure(call: Call<GetEventsResponse>, t: Throwable) {
                Log.e("ViewModel", "API Failure: ${t.message}")
                _allEvents.value = dummyEvents
            }
        })
    }

    /**
     * Mengambil event khusus yang dibuat oleh User (Untuk Tab 'Dibuat')
     * Dipanggil di MyRegisteredEventScreen
     */
    fun loadCreatedEvents(userId: Int, context: Context) {
        if (!isNetworkAvailable(context)) {
            // Jika offline, filter dari _allEvents lokal saja sebagai fallback
            val currentAll = _allEvents.value
            _createdEvents.value = currentAll.filter { it.creatorId == userId }
            return
        }

        ApiClient.instance.getMyCreatedEvents(userId).enqueue(object : Callback<GetEventsResponse> {
            override fun onResponse(call: Call<GetEventsResponse>, response: Response<GetEventsResponse>) {
                val body = response.body()
                if (response.isSuccessful && body?.status == "success") {
                    val myEvents = body.data.map { it.toEventModel() }
                    _createdEvents.value = myEvents
                } else {
                    Log.e("ViewModel", "Gagal loadCreatedEvents: ${response.message()}")
                }
            }
            override fun onFailure(call: Call<GetEventsResponse>, t: Throwable) {
                Log.e("ViewModel", "API Failure (loadCreatedEvents): ${t.message}")
            }
        })
    }

    /**
     * Menambah Event Baru
     */
    fun addEvent(event: Event, creatorId: Int?, context: Context) {
        if (!isNetworkAvailable(context)) {
            _notificationMessage.value = "Tidak ada koneksi internet."
            return
        }

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
            thumbnailUri = event.thumbnailUri,
            creatorId = creatorId
        )

        ApiClient.instance.createEvent(request).enqueue(object : Callback<CreateEventResponse> {
            override fun onResponse(call: Call<CreateEventResponse>, response: Response<CreateEventResponse>) {
                val body = response.body()
                if (response.isSuccessful && body?.status == "success") {
                    _notificationMessage.value = "Event Berhasil Ditambahkan"

                    // Refresh data setelah berhasil nambah
                    loadAllEvents(context)
                    if (creatorId != null) {
                        loadCreatedEvents(creatorId, context)
                    }
                } else {
                    val errorMsg = body?.message ?: response.message()
                    _notificationMessage.value = "Gagal: $errorMsg"
                    Log.e("ViewModel", "Gagal addEvent: $errorMsg")
                }
            }
            override fun onFailure(call: Call<CreateEventResponse>, t: Throwable) {
                _notificationMessage.value = "Gagal terhubung ke server."
                Log.e("ViewModel", "API Failure: ${t.message}")
            }
        })
    }

    // --- Fungsi Lokal Lainnya (Update, Delete, Feedback, dll) ---
    fun getEventById(eventId: Int): Event? {
        // Cari di createdEvents, allEvents, lalu followedEvents
        return _createdEvents.value.find { it.id == eventId }
            ?: _allEvents.value.find { it.id == eventId }
            ?: _followedEvents.find { it.id == eventId }
            ?: dummyEvents.find { it.id == eventId }
    }

    fun updateEvent(updatedEvent: Event) {
        // Update di list lokal sementara (Idealnya panggil API Update)
        _createdEvents.value = _createdEvents.value.map { if (it.id == updatedEvent.id) updatedEvent else it }
        _allEvents.value = _allEvents.value.map { if (it.id == updatedEvent.id) updatedEvent else it }
        _notificationMessage.value = "Event Diperbarui (Lokal)"
    }

    fun deleteEvent(eventId: Int) {
        // Hapus dari list lokal sementara (Idealnya panggil API Delete)
        _createdEvents.value = _createdEvents.value.filter { it.id != eventId }
        _allEvents.value = _allEvents.value.filter { it.id != eventId }
        _notificationMessage.value = "Event Dihapus (Lokal)"
    }

    // Feedback & Documentation Helper
    fun getFeedbacksForEvent(eventId: Int): List<Feedback> { return _feedbacks.value[eventId] ?: emptyList() }

    fun submitFeedback(eventId: Int, feedback: Feedback) {
        val currentList = _feedbacks.value[eventId] ?: emptyList()
        val newList = currentList + feedback
        _feedbacks.value = _feedbacks.value.toMutableMap().apply { put(eventId, newList) }
    }

    fun deleteFeedback(eventId: Int, feedbackId: Int) {
        val currentList = _feedbacks.value[eventId] ?: return
        val newList = currentList.filter { it.id != feedbackId }
        _feedbacks.value = _feedbacks.value.toMutableMap().apply { put(eventId, newList) }
    }

    fun getDocumentationForEvent(eventId: Int): List<Documentation> { return _documentations.value[eventId] ?: emptyList() }

    fun submitDocumentation(eventId: Int, doc: Documentation) {
        val currentList = _documentations.value[eventId] ?: emptyList()
        val newList = if (currentList.any { it.id == doc.id }) {
            currentList.map { if (it.id == doc.id) doc else it } // Edit
        } else {
            currentList + doc // Add
        }
        _documentations.value = _documentations.value.toMutableMap().apply { put(eventId, newList) }
    }

    fun deleteDocumentation(eventId: Int, docId: Int) {
        val currentList = _documentations.value[eventId] ?: return
        val newList = currentList.filter { it.id != docId }
        _documentations.value = _documentations.value.toMutableMap().apply { put(eventId, newList) }
    }

    fun toggleDocumentationLike(docId: Int) {
        val currentLikes = _likedDocIds.value.toMutableSet()
        if (currentLikes.contains(docId)) currentLikes.remove(docId) else currentLikes.add(docId)
        _likedDocIds.value = currentLikes
    }

    // Registration & Follow
    fun registerForEvent(event: Event, data: Registration) {
        if (_followedEvents.none { it.id == event.id }) {
            _followedEvents.add(event)
            _registrations.value = _registrations.value.toMutableMap().apply { put(event.id, data) }
            _notificationMessage.value = "Berhasil mendaftar ke ${event.title}"
        }
    }

    fun getRegistrationData(eventId: Int): Registration? { return _registrations.value[eventId] }

    fun updateRegistrationData(eventId: Int, newData: Registration) {
        _registrations.value = _registrations.value.toMutableMap().apply { put(eventId, newData) }
    }

    fun unfollowEvent(eventId: Int) {
        _followedEvents.removeIf { it.id == eventId }
        _registrations.value = _registrations.value.toMutableMap().apply { remove(eventId) }
    }

    fun clearNotification() {
        _notificationMessage.value = null
    }
}
