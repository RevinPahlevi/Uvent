package com.example.uventapp.ui.screen.event

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.uventapp.data.model.Event
import com.example.uventapp.data.model.Registration
import com.example.uventapp.data.model.Feedback
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// --- PERBAIKAN: Data Feedback Dummy disesuaikan dengan UI ---
private val dummyFeedbacks = mutableListOf(
    Feedback(
        id = 100,
        eventId = 1,
        rating = 4,
        review = "Event sangat bagus! Pembicaranya inspiratif dan materi sangat bermanfaat.",
        photoUri = null,
        userName = "Loly Amelia Nurza",
        postDate = "19 Oktober 2025",
        isAnda = true // Loly adalah "Anda"
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
// ----------------------------


class EventManagementViewModel : ViewModel() {

    private val _createdEvents = mutableStateOf<List<Event>>(emptyList())
    val createdEvents: State<List<Event>> = _createdEvents

    private val _followedEvents = mutableStateListOf<Event>()
    val followedEvents: List<Event> = _followedEvents

    private val _notificationMessage = mutableStateOf<String?>(null)
    val notificationMessage: State<String?> = _notificationMessage

    private val _registrations = mutableStateOf<Map<Int, Registration>>(emptyMap())
    val registrations: State<Map<Int, Registration>> = _registrations

    // --- PERBAIKAN: Ubah Map untuk menampung BANYAK feedback per event ---
    private val _feedbacks = mutableStateOf<Map<Int, List<Feedback>>>(
        // Inisialisasi dengan data dummy
        mapOf(1 to dummyFeedbacks)
    )
    val feedbacks: State<Map<Int, List<Feedback>>> = _feedbacks

    /**
     * Mengambil semua feedback untuk eventId tertentu.
     */
    fun getFeedbacksForEvent(eventId: Int): List<Feedback> {
        return _feedbacks.value[eventId] ?: emptyList()
    }

    /**
     * Mengirim feedback baru dari pengguna.
     */
    fun submitFeedback(eventId: Int, feedback: Feedback) {
        val currentFeedbacksMap = _feedbacks.value.toMutableMap()

        // Ambil list yang ada, atau buat list baru jika belum ada
        val currentList = (currentFeedbacksMap[eventId] ?: emptyList()).toMutableList()

        // --- PERBAIKAN LOGIKA: Hapus ulasan "Anda" sebelumnya ---
        currentList.removeIf { it.isAnda }
        // ----------------------------------------------------

        // Tambahkan feedback baru ke list (sebagai "Anda")
        // Kita letakkan di paling atas (index 0)
        currentList.add(0, feedback.copy(isAnda = true))

        // Simpan list baru ke map
        currentFeedbacksMap[eventId] = currentList
        _feedbacks.value = currentFeedbacksMap
    }

    /**
     * Menghapus feedback (hanya feedback milik "Anda").
     */
    fun deleteFeedback(eventId: Int, feedbackId: Int) {
        val currentFeedbacksMap = _feedbacks.value.toMutableMap()
        val currentList = (currentFeedbacksMap[eventId] ?: emptyList()).toMutableList()

        // Hapus feedback berdasarkan ID-nya
        currentList.removeIf { it.id == feedbackId }

        currentFeedbacksMap[eventId] = currentList
        _feedbacks.value = currentFeedbacksMap
    }

    // --- Sisa fungsi ViewModel (tetap sama) ---

    fun registerForEvent(event: Event, data: Registration) {
        if (followedEvents.none { it.id == event.id }) {
            _followedEvents.add(event)
        }

        val currentRegs = _registrations.value.toMutableMap()
        currentRegs[event.id] = data
        _registrations.value = currentRegs
    }

    fun getRegistrationData(eventId: Int): Registration? {
        return _registrations.value[eventId]
    }

    fun updateRegistrationData(eventId: Int, newData: Registration) {
        val currentRegs = _registrations.value.toMutableMap()
        if (currentRegs.containsKey(eventId)) {
            currentRegs[eventId] = newData
            _registrations.value = currentRegs
        }
    }

    fun unfollowEvent(eventId: Int) {
        _followedEvents.removeIf { it.id == eventId }

        val currentRegs = _registrations.value.toMutableMap()
        currentRegs.remove(eventId)
        _registrations.value = currentRegs
    }

    fun addEvent(event: Event) {
        _createdEvents.value = _createdEvents.value + listOf(event)
        _notificationMessage.value = "Event Berhasil Ditambahkan"
    }

    fun updateEvent(updatedEvent: Event) {
        _createdEvents.value = _createdEvents.value.map {
            if (it.id == updatedEvent.id) updatedEvent else it
        }
        _notificationMessage.value = "Event Berhasil DiEdit"
    }

    fun deleteEvent(eventId: Int) {
        _createdEvents.value = _createdEvents.value.filter { it.id != eventId }
        _notificationMessage.value = "Event Berhasil Dihapus"
    }

    fun getEventById(eventId: Int): Event? {
        return _createdEvents.value.find { it.id == eventId }
            ?: _followedEvents.find { it.id == eventId }
    }

    fun clearNotification() {
        _notificationMessage.value = null
    }
}