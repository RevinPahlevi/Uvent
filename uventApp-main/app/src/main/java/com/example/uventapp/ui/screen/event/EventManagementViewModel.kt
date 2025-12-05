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

// --- DATA DUMMY ---
private val dummyFeedbacks = mutableListOf(
    Feedback(id = 100, eventId = 1, rating = 4, review = "Event bagus!", photoUri = null, userName = "Loly", postDate = "19/10/2025", isAnda = true)
)
private val dummyDocumentation = mutableListOf(
    Documentation(id = 200, eventId = 1, description = "Seru!", photoUri = null, userName = "Loly", postDate = "17/10/2025", postTime = "14:30", isAnda = false)
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

    private val _allEvents = mutableStateOf<List<Event>>(emptyList())
    val allEvents: State<List<Event>> = _allEvents

    private val _createdEvents = mutableStateOf<List<Event>>(emptyList())
    val createdEvents: State<List<Event>> = _createdEvents

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

    fun loadAllEvents(context: Context) {
        // Selalu set dummyEvents sebagai data awal
        if (_allEvents.value.isEmpty()) {
            _allEvents.value = dummyEvents
        }
        
        if (!isNetworkAvailable(context)) {
            _notificationMessage.value = "Offline. Menggunakan data lokal."
            _allEvents.value = dummyEvents
            return
        }

        ApiClient.instance.getAllEvents().enqueue(object : Callback<GetEventsResponse> {
            override fun onResponse(call: Call<GetEventsResponse>, response: Response<GetEventsResponse>) {
                val body = response.body()
                if (response.isSuccessful && body?.status == "success") {
                    val eventsFromApi = body.data.map { it.toEventModel() }
                    _allEvents.value = (eventsFromApi + dummyEvents).distinctBy { it.id }
                } else {
                    Log.e("ViewModel", "Gagal load all events: ${response.message()}")
                    // Fallback ke dummyEvents jika belum ada data
                    if (_allEvents.value.isEmpty()) {
                        _allEvents.value = dummyEvents
                    }
                }
            }
            override fun onFailure(call: Call<GetEventsResponse>, t: Throwable) {
                Log.e("ViewModel", "API Failure: ${t.message}")
                // Fallback ke dummyEvents saat API gagal
                if (_allEvents.value.isEmpty()) {
                    _allEvents.value = dummyEvents
                }
            }
        })
    }

    fun loadCreatedEvents(userId: Int, context: Context) {
        if (!isNetworkAvailable(context)) return

        ApiClient.instance.getMyCreatedEvents(userId).enqueue(object : Callback<GetEventsResponse> {
            override fun onResponse(call: Call<GetEventsResponse>, response: Response<GetEventsResponse>) {
                val body = response.body()
                if (response.isSuccessful && body?.status == "success") {
                    _createdEvents.value = body.data.map { it.toEventModel() }
                }
            }
            override fun onFailure(call: Call<GetEventsResponse>, t: Throwable) {
                Log.e("ViewModel", "Gagal load created events: ${t.message}")
            }
        })
    }

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
                    loadAllEvents(context)
                    if (creatorId != null) loadCreatedEvents(creatorId, context)
                } else {
                    _notificationMessage.value = "Gagal: ${body?.message ?: response.message()}"
                }
            }
            override fun onFailure(call: Call<CreateEventResponse>, t: Throwable) {
                _notificationMessage.value = "Gagal terhubung ke server."
            }
        })
    }

    // --- FUNGSI UPDATE EVENT (YANG SEBELUMNYA HILANG) ---
    fun updateEvent(updatedEvent: Event) {
        // Karena endpoint update API belum ada, kita update list lokal saja agar UI berubah
        _createdEvents.value = _createdEvents.value.map {
            if (it.id == updatedEvent.id) updatedEvent else it
        }
        _allEvents.value = _allEvents.value.map {
            if (it.id == updatedEvent.id) updatedEvent else it
        }
        _notificationMessage.value = "Event Berhasil Diperbarui (Lokal)"
    }
    // ----------------------------------------------------

    fun getEventById(eventId: Int): Event? {
        return _createdEvents.value.find { it.id == eventId }
            ?: _allEvents.value.find { it.id == eventId }
            ?: _followedEvents.find { it.id == eventId }
            ?: dummyEvents.find { it.id == eventId }
    }

    fun deleteEvent(eventId: Int) {
        _createdEvents.value = _createdEvents.value.filter { it.id != eventId }
        _allEvents.value = _allEvents.value.filter { it.id != eventId }
        _notificationMessage.value = "Event Dihapus (Lokal)"
    }

    fun getFeedbacksForEvent(eventId: Int): List<Feedback> = _feedbacks.value[eventId] ?: emptyList()
    fun submitFeedback(eventId: Int, feedback: Feedback) {
        val list = _feedbacks.value[eventId] ?: emptyList()
        _feedbacks.value = _feedbacks.value.toMutableMap().apply { put(eventId, list + feedback) }
    }
    fun deleteFeedback(eventId: Int, feedbackId: Int) { /* Logic delete feedback */ }

    fun getDocumentationForEvent(eventId: Int): List<Documentation> = _documentations.value[eventId] ?: emptyList()
    fun submitDocumentation(eventId: Int, doc: Documentation) {
        val list = _documentations.value[eventId] ?: emptyList()
        _documentations.value = _documentations.value.toMutableMap().apply { put(eventId, list + doc) }
    }
    fun deleteDocumentation(eventId: Int, docId: Int) { /* Logic delete doc */ }
    fun toggleDocumentationLike(docId: Int) { /* Logic like */ }

    fun registerForEvent(event: Event, data: Registration) {
        if (_followedEvents.none { it.id == event.id }) {
            _followedEvents.add(event)
            _registrations.value = _registrations.value.toMutableMap().apply { put(event.id, data) }
            _notificationMessage.value = "Berhasil mendaftar ke ${event.title}"
        }
    }
    fun getRegistrationData(eventId: Int): Registration? = _registrations.value[eventId]
    fun updateRegistrationData(eventId: Int, newData: Registration) {
        _registrations.value = _registrations.value.toMutableMap().apply { put(eventId, newData) }
    }
    fun unfollowEvent(eventId: Int) {
        _followedEvents.removeIf { it.id == eventId }
    }
    fun clearNotification() { _notificationMessage.value = null }
}
