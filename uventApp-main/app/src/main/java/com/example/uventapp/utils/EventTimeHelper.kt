package com.example.uventapp.utils

import android.util.Log
import java.util.Calendar

/**
 * Helper functions untuk cek status waktu event
 */
object EventTimeHelper {
    
    /**
     * Cek apakah event sudah dimulai (waktu mulai sudah lewat)
     * @param date Format: "d/M/yyyy" atau "dd/MM/yyyy"
     * @param timeStart Format: "HH:mm"
     * @return true jika event sudah mulai
     */
    fun isEventStarted(date: String, timeStart: String): Boolean {
        return try {
            val eventStartCalendar = parseDateTime(date, timeStart)
            val now = Calendar.getInstance()
            
            // Event sudah mulai jika waktu sekarang SETELAH waktu mulai
            now.after(eventStartCalendar)
        } catch (e: Exception) {
            Log.e("EventTimeHelper", "Error parsing start time: date=$date, time=$timeStart", e)
            false // Default: anggap belum mulai jika error
        }
    }
    
    /**
     * Cek apakah event sudah selesai (waktu selesai sudah lewat)
     * @param date Format: "d/M/yyyy" atau "dd/MM/yyyy"
     * @param timeEnd Format: "HH:mm"
     * @return true jika event sudah selesai
     */
    fun isEventFinished(date: String, timeEnd: String): Boolean {
        return try {
            val eventEndCalendar = parseDateTime(date, timeEnd)
            val now = Calendar.getInstance()
            
            // Event selesai jika waktu sekarang SETELAH waktu selesai
            now.after(eventEndCalendar)
        } catch (e: Exception) {
            Log.e("EventTimeHelper", "Error parsing end time: date=$date, time=$timeEnd", e)
            false // Default: anggap belum selesai jika error
        }
    }
    
    /**
     * Cek apakah masih bisa mendaftar event (belum dimulai)
     * @param date Format: "d/M/yyyy" atau "dd/MM/yyyy"
     * @param timeStart Format: "HH:mm"
     * @return true jika masih bisa mendaftar
     */
    fun canRegisterForEvent(date: String, timeStart: String): Boolean {
        return !isEventStarted(date, timeStart)
    }
    
    /**
     * Parse tanggal dan waktu menjadi Calendar object
     * @param date Format: "d/M/yyyy" atau "dd/MM/yyyy"
     * @param time Format: "HH:mm" atau "HH:mm:ss"
     */
    private fun parseDateTime(date: String, time: String): Calendar {
        // Parse tanggal
        val dateParts = date.split("/")
        require(dateParts.size == 3) { "Invalid date format: $date" }
        
        val day = dateParts[0].toInt()
        val month = dateParts[1].toInt()
        val year = dateParts[2].toInt()
        
        // Parse waktu
        val timeParts = time.split(":")
        require(timeParts.size >= 2) { "Invalid time format: $time" }
        
        val hour = timeParts[0].toInt()
        val minute = timeParts[1].toInt()
        
        // Buat Calendar object
        return Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month - 1) // Calendar month is 0-indexed
            set(Calendar.DAY_OF_MONTH, day)
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
    }
    
    /**
     * Get status text untuk event
     * @return "Selesai", "Berlangsung", atau "Akan Datang"
     */
    fun getEventStatus(date: String, timeStart: String, timeEnd: String): String {
        return when {
            isEventFinished(date, timeEnd) -> "Selesai"
            isEventStarted(date, timeStart) -> "Berlangsung"
            else -> "Akan Datang"
        }
    }
}
