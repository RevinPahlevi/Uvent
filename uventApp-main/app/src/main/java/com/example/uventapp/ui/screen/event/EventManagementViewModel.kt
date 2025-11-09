package com.example.uventapp.ui.screen.event

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateListOf // Import baru
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.uventapp.data.model.Event
// --- PERBAIKAN: IMPORT Registration, BUKAN RegistrationData ---
import com.example.uventapp.data.model.Registration

class EventManagementViewModel : ViewModel() {

    private val _createdEvents = mutableStateOf<List<Event>>(emptyList())
    val createdEvents: State<List<Event>> = _createdEvents

    private val _followedEvents = mutableStateListOf<Event>()
    val followedEvents: List<Event> = _followedEvents

    private val _notificationMessage = mutableStateOf<String?>(null)
    val notificationMessage: State<String?> = _notificationMessage

    // --- LOGIKA BARU UNTUK MENYIMPAN DATA PENDAFTARAN ---
    // --- PERBAIKAN: Menggunakan Map<Int, Registration> ---
    private val _registrations = mutableStateOf<Map<Int, Registration>>(emptyMap())
    val registrations: State<Map<Int, Registration>> = _registrations

    /**
     * Dipanggil dari RegistrationFormScreen.
     * Mendaftarkan event DAN menyimpan data pendaftarannya.
     */
    fun registerForEvent(event: Event, data: Registration) { // <-- Perbaikan tipe
        // 1. Tambahkan event ke daftar "diikuti"
        if (followedEvents.none { it.id == event.id }) {
            _followedEvents.add(event)
        }

        // 2. Simpan data pendaftaran ke Map
        val currentRegs = _registrations.value.toMutableMap()
        currentRegs[event.id] = data
        _registrations.value = currentRegs
    }

    /**
     * Dipanggil dari EditRegistrationScreen untuk mengambil data.
     */
    fun getRegistrationData(eventId: Int): Registration? { // <-- Perbaikan tipe
        return _registrations.value[eventId]
    }

    /**
     * Dipanggil dari EditRegistrationScreen untuk menyimpan perubahan.
     */
    fun updateRegistrationData(eventId: Int, newData: Registration) { // <-- Perbaikan tipe
        val currentRegs = _registrations.value.toMutableMap()
        if (currentRegs.containsKey(eventId)) {
            currentRegs[eventId] = newData
            _registrations.value = currentRegs
        }
    }
    // ---------------------------------------------------

    /**
     * Menambahkan event baru ke dalam daftar createdEvents.
     * Dipanggil dari AddEventScreen.
     */
    fun addEvent(event: Event) {
        // Menggunakan " + listOf(event)" agar ditambahkan di AKHIR list
        _createdEvents.value = _createdEvents.value + listOf(event)
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
        // Cari di kedua daftar (dummyEvents ditambahkan di screen)
        return _createdEvents.value.find { it.id == eventId } ?: followedEvents.find { it.id == eventId }
    }

    /**
     * Menghapus pesan notifikasi setelah ditampilkan.
     */
    fun clearNotification() {
        _notificationMessage.value = null
    }
}