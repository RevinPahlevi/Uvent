package com.example.uventapp.ui.screen.event

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateListOf // Import baru
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.uventapp.data.model.Event
// dummyEvents tidak lagi dipakai di sini

class EventManagementViewModel : ViewModel() {

    // --- PERBAIKAN: Dimulai dengan daftar kosong ---
    // 'dummyEvents' dihapus dari inisialisasi
    private val _createdEvents = mutableStateOf<List<Event>>(emptyList())
    val createdEvents: State<List<Event>> = _createdEvents
    // ---------------------------------------------

    // --- DAFTAR BARU: Event yang DIIKUTI ---
    private val _followedEvents = mutableStateListOf<Event>()
    val followedEvents: List<Event> = _followedEvents
    // ----------------------------------------

    private val _notificationMessage = mutableStateOf<String?>(null)
    val notificationMessage: State<String?> = _notificationMessage

    // --- FUNGSI BARU: Untuk mendaftar event ---
    /**
     * Menambahkan event ke daftar event yang diikuti.
     * Dipanggil dari RegistrationFormScreen.
     */
    fun addFollowedEvent(event: Event) {
        // Hanya tambahkan jika belum ada di daftar
        if (followedEvents.none { it.id == event.id }) {
            _followedEvents.add(event)
        }
    }
    // ------------------------------------------

    /**
     * Menambahkan event baru ke dalam daftar createdEvents.
     * Dipanggil dari AddEventScreen.
     */
    fun addEvent(event: Event) {
        _createdEvents.value = listOf(event) + _createdEvents.value
        _notificationMessage.value = "Event Berhasil Ditambahkan"
    }

    /**
     * Memperbarui event yang sudah ada di dalam daftar createdEvents.
     * Dipanggil dari EditEventScreen.
     */
    fun updateEvent(updatedEvent: Event) {
        _createdEvents.value = _createdEvents.value.map {
            if (it.id == updatedEvent.id) updatedEvent else it
        }
        _notificationMessage.value = "Event Berhasil DiEdit"
    }

    /**
     * Menghapus event dari daftar createdEvents.
     * Dipanggil dari MyRegisteredEventScreen (tab "Dibuat").
     */
    fun deleteEvent(eventId: Int) {
        _createdEvents.value = _createdEvents.value.filter { it.id != eventId }
        _notificationMessage.value = "Event Berhasil Dihapus"
    }

    /**
     * Mengambil satu event berdasarkan ID dari daftar createdEvents ATAU followedEvents.
     */
    fun getEventById(eventId: Int): Event? {
        // Cari di kedua daftar
        return _createdEvents.value.find { it.id == eventId } ?: followedEvents.find { it.id == eventId }
    }

    /**
     * Menghapus pesan notifikasi setelah ditampilkan.
     */
    fun clearNotification() {
        _notificationMessage.value = null
    }
}