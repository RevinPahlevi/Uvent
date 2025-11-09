package com.example.uventapp.ui.screen.event

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
// PERBAIKAN: Impor Event & dummyEvents dari file model terpusat
import com.example.uventapp.data.model.Event
import com.example.uventapp.data.model.dummyEvents

/**
 * CATATAN: data class Event dan dummyEvents LOKAL TELAH DIHAPUS.
 * Kita sekarang menggunakan definisi dari /data/model/Event.kt
 */

/**
 * ViewModel untuk mengelola state dari Modul 1: Manajemen Event.
 * Ini menangani logika untuk Create, Read, Update, Delete (CRUD) event
 * yang dibuat oleh pengguna.
 */
class EventManagementViewModel : ViewModel() {

    // Menyimpan daftar event yang telah dibuat oleh pengguna.
    // Diinisialisasi dengan data dummy yang diimpor.
    private val _createdEvents = mutableStateOf(dummyEvents)
    val createdEvents: State<List<Event>> = _createdEvents // Tipe <Event> sekarang jelas

    // Menyimpan pesan notifikasi untuk ditampilkan di UI
    private val _notificationMessage = mutableStateOf<String?>(null)
    val notificationMessage: State<String?> = _notificationMessage

    /**
     * Menambahkan event baru ke dalam daftar createdEvents.
     * Dipanggil dari AddEventScreen.
     */
    fun addEvent(event: Event) {
        // Menambahkan event baru ke awal daftar
        _createdEvents.value = listOf(event) + _createdEvents.value
        // Menyiapkan pesan notifikasi
        _notificationMessage.value = "Event Berhasil Ditambahkan"
    }

    /**
     * Memperbarui event yang sudah ada di dalam daftar.
     * Dipanggil dari EditEventScreen.
     */
    fun updateEvent(updatedEvent: Event) {
        _createdEvents.value = _createdEvents.value.map { event -> // Tipe 'event' sekarang jelas
            // Cari event berdasarkan ID, jika ketemu, ganti dengan data baru
            if (event.id == updatedEvent.id) updatedEvent else event
        }
        // Menyiapkan pesan notifikasi
        _notificationMessage.value = "Event Berhasil DiEdit"
    }

    /**
     * Menghapus event dari daftar berdasarkan ID.
     * Dipanggil dari MyEventsScreen.
     */
    fun deleteEvent(eventId: Int) {
        _createdEvents.value = _createdEvents.value.filter { event -> // Tipe 'event' sekarang jelas
            event.id != eventId
        }
        // Menyiapkan pesan notifikasi
        _notificationMessage.value = "Event Berhasil Dihapus"
    }

    /**
     * Mengambil satu event berdasarkan ID.
     * Digunakan oleh EditEventScreen untuk mengisi form dengan data yang ada.
     */
    fun getEventById(eventId: Int): Event? {
        // Cari event di dalam daftar
        return _createdEvents.value.find { event -> // Tipe 'event' sekarang jelas
            event.id == eventId
        }
    }

    /**
     * Menghapus pesan notifikasi setelah ditampilkan.
     * Dipanggil dari UI (Composable) setelah Snackbar/Toast selesai.
     */
    fun clearNotification() {
        _notificationMessage.value = null
    }
}

