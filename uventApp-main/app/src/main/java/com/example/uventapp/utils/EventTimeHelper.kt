package com.example.uventapp.utils

import android.util.Log
import java.util.Calendar

object EventTimeHelper {

    fun isEventStarted(date: String, timeStart: String): Boolean {
        return try {
            val eventStartCalendar = parseDateTime(date, timeStart)
            val now = Calendar.getInstance()

            now.after(eventStartCalendar)
        } catch (e: Exception) {
            Log.e("EventTimeHelper", "Error parsing start time: date=$date, time=$timeStart", e)
            false
        }
    }

    fun isEventFinished(date: String, timeEnd: String): Boolean {
        return try {
            val eventEndCalendar = parseDateTime(date, timeEnd)
            val now = Calendar.getInstance()

            now.after(eventEndCalendar)
        } catch (e: Exception) {
            Log.e("EventTimeHelper", "Error parsing end time: date=$date, time=$timeEnd", e)
            false
        }
    }

    fun canRegisterForEvent(date: String, timeStart: String): Boolean {
        return !isEventStarted(date, timeStart)
    }

    private fun parseDateTime(date: String, time: String): Calendar {
        val dateParts = date.split("/")
        require(dateParts.size == 3) { "Invalid date format: $date" }

        val day = dateParts[0].toInt()
        val month = dateParts[1].toInt()
        val year = dateParts[2].toInt()

        val timeParts = time.split(":")
        require(timeParts.size >= 2) { "Invalid time format: $time" }

        val hour = timeParts[0].toInt()
        val minute = timeParts[1].toInt()

        return Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month - 1)
            set(Calendar.DAY_OF_MONTH, day)
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
    }

    fun getEventStatus(date: String, timeStart: String, timeEnd: String): String {
        return when {
            isEventFinished(date, timeEnd) -> "Selesai"
            isEventStarted(date, timeStart) -> "Berlangsung"
            else -> "Akan Datang"
        }
    }
}
