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
import com.example.uventapp.data.network.EventRegistrationRequest
import com.example.uventapp.data.network.EventRegistrationResponse
import com.example.uventapp.data.network.EventResponse
import com.example.uventapp.data.network.FeedbackRequest
import com.example.uventapp.data.network.FeedbackResponse
import com.example.uventapp.data.network.DocumentationRequest
import com.example.uventapp.data.network.DocumentationResponse
import com.example.uventapp.data.network.GetEventsResponse
import com.example.uventapp.data.network.UpdateEventRequest
import com.example.uventapp.data.network.UpdateEventResponse
import com.example.uventapp.data.network.DeleteResponse
import com.example.uventapp.data.network.UpdateFeedbackRequest
import com.example.uventapp.data.network.UpdateFeedbackResponse
import com.example.uventapp.data.network.UpdateDocumentationRequest
import com.example.uventapp.data.network.UpdateDocumentationResponse
import com.example.uventapp.data.network.UploadImageResponse
import com.example.uventapp.utils.isNetworkAvailable
// Removed dummy events import
import android.net.Uri
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
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
    // DEBUG: Log untuk tracking thumbnailUri
    if (this.thumbnailUri != null) {
        Log.d("EventConversion", "Converting event '${this.title}' with thumbnailUri: ${this.thumbnailUri}")
    }
    
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

    private val _isUploading = mutableStateOf(false)
    val isUploading: State<Boolean> = _isUploading

    private val _isLoadingCreatedEvents = mutableStateOf(false)
    val isLoadingCreatedEvents: State<Boolean> = _isLoadingCreatedEvents

    // Fungsi untuk upload gambar ke server dan mendapatkan URL
    fun uploadImage(
        context: Context,
        imageUri: Uri,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        _isUploading.value = true
        Log.d("UploadImage", "=== UPLOAD IMAGE START ===")
        Log.d("UploadImage", "Input imageUri: $imageUri")
        
        try {
            // Buat file sementara dari URI
            val inputStream = context.contentResolver.openInputStream(imageUri)
            val tempFile = File.createTempFile("upload_", ".jpg", context.cacheDir)
            val outputStream = FileOutputStream(tempFile)
            
            inputStream?.copyTo(outputStream)
            inputStream?.close()
            outputStream.close()
            
            Log.d("UploadImage", "Temp file created: ${tempFile.absolutePath}")
            Log.d("UploadImage", "Temp file size: ${tempFile.length()} bytes")
            
            // Siapkan multipart request
            val requestBody = tempFile.asRequestBody("image/*".toMediaTypeOrNull())
            val multipartBody = MultipartBody.Part.createFormData("image", tempFile.name, requestBody)
            
            Log.d("UploadImage", "Sending to API...")
            
            // Panggil API upload
            ApiClient.instance.uploadImage(multipartBody).enqueue(object : Callback<UploadImageResponse> {
                override fun onResponse(
                    call: Call<UploadImageResponse>,
                    response: Response<UploadImageResponse>
                ) {
                    _isUploading.value = false
                    tempFile.delete() // Hapus file sementara
                    
                    Log.d("UploadImage", "Response code: ${response.code()}")
                    Log.d("UploadImage", "Response body: ${response.body()}")
                    
                    val body = response.body()
                    if (response.isSuccessful && body?.status == "success" && body.data != null) {
                        Log.d("UploadImage", "=== UPLOAD SUCCESS ===")
                        Log.d("UploadImage", "Filename: ${body.data.filename}")
                        Log.d("UploadImage", "URL from server: ${body.data.url}")
                        Log.d("UploadImage", "Size: ${body.data.size}")
                        onSuccess(body.data.url)
                    } else {
                        Log.e("UploadImage", "Upload failed: ${response.message()}")
                        Log.e("UploadImage", "Body message: ${body?.message}")
                        onError("Gagal upload gambar: ${body?.message ?: response.message()}")
                    }
                }
                
                override fun onFailure(call: Call<UploadImageResponse>, t: Throwable) {
                    _isUploading.value = false
                    tempFile.delete()
                    Log.e("UploadImage", "Upload error: ${t.message}")
                    Log.e("UploadImage", "Stack trace: ${t.stackTraceToString()}")
                    onError("Error upload: ${t.message}")
                }
            })
        } catch (e: Exception) {
            _isUploading.value = false
            Log.e("UploadImage", "Exception: ${e.message}")
            Log.e("UploadImage", "Stack trace: ${e.stackTraceToString()}")
            onError("Error: ${e.message}")
        }
    }

    // ===== FITUR BARU: Upload file KRS (PDF) =====
    fun uploadKRS(
        context: Context,
        krsUri: Uri,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        _isUploading.value = true
        Log.d("UploadKRS", "=== UPLOAD KRS START ===")
        Log.d("UploadKRS", "Input krsUri: $krsUri")
        
        try {
            // Buat file sementara dari URI
            val inputStream = context.contentResolver.openInputStream(krsUri)
            val tempFile = File.createTempFile("krs_", ".pdf", context.cacheDir)
            val outputStream = FileOutputStream(tempFile)
            
            inputStream?.copyTo(outputStream)
            inputStream?.close()
            outputStream.close()
            
            Log.d("UploadKRS", "Temp file created: ${tempFile.absolutePath}")
            Log.d("UploadKRS", "Temp file size: ${tempFile.length()} bytes")
            
            // Siapkan multipart request untuk PDF
            val requestBody = tempFile.asRequestBody("application/pdf".toMediaTypeOrNull())
            val multipartBody = MultipartBody.Part.createFormData("krs", tempFile.name, requestBody)
            
            Log.d("UploadKRS", "Sending to API...")
            
            // Panggil API upload KRS
            ApiClient.instance.uploadKRS(multipartBody).enqueue(object : Callback<com.example.uventapp.data.network.UploadKRSResponse> {
                override fun onResponse(
                    call: Call<com.example.uventapp.data.network.UploadKRSResponse>,
                    response: Response<com.example.uventapp.data.network.UploadKRSResponse>
                ) {
                    _isUploading.value = false
                    tempFile.delete() // Hapus file sementara
                    
                    Log.d("UploadKRS", "Response code: ${response.code()}")
                    Log.d("UploadKRS", "Response body: ${response.body()}")
                    
                    val body = response.body()
                    if (response.isSuccessful && body?.status == "success" && body.data != null) {
                        Log.d("UploadKRS", "=== UPLOAD KRS SUCCESS ===")
                        Log.d("UploadKRS", "Filename: ${body.data.filename}")
                        Log.d("UploadKRS", "URL from server: ${body.data.url}")
                        Log.d("UploadKRS", "Size: ${body.data.size}")
                        onSuccess(body.data.url)
                    } else {
                        Log.e("UploadKRS", "Upload failed: ${response.message()}")
                        Log.e("UploadKRS", "Body message: ${body?.message}")
                        onError("Gagal upload KRS: ${body?.message ?: response.message()}")
                    }
                }
                
                override fun onFailure(call: Call<com.example.uventapp.data.network.UploadKRSResponse>, t: Throwable) {
                    _isUploading.value = false
                    tempFile.delete()
                    Log.e("UploadKRS", "Upload error: ${t.message}")
                    Log.e("UploadKRS", "Stack trace: ${t.stackTraceToString()}")
                    onError("Error upload KRS: ${t.message}")
                }
            })
        } catch (e: Exception) {
            _isUploading.value = false
            Log.e("UploadKRS", "Exception: ${e.message}")
            Log.e("UploadKRS", "Stack trace: ${e.stackTraceToString()}")
            onError("Error: ${e.message}")
        }
    }
    // ================================================


    fun loadAllEvents(context: Context) {
        Log.d("ViewModel", "=== LOAD ALL EVENTS CALLED ===")
        Log.d("ViewModel", "Current _allEvents size: ${_allEvents.value.size}")
        
        if (!isNetworkAvailable(context)) {
            Log.e("ViewModel", "No network available")
            _notificationMessage.value = "Offline. Tidak dapat memuat event."
            return
        }

        Log.d("ViewModel", "Calling API getAllEvents...")
        ApiClient.instance.getAllEvents().enqueue(object : Callback<GetEventsResponse> {
            override fun onResponse(call: Call<GetEventsResponse>, response: Response<GetEventsResponse>) {
                Log.d("ViewModel", "API Response received: ${response.code()}")
                val body = response.body()
                if (response.isSuccessful && body?.status == "success") {
                    val events = body.data.map { it.toEventModel() }
                    Log.d("ViewModel", "✅ Events loaded from API: ${events.size} events")
                    events.forEach {
                        Log.d("ViewModel", "  - Event: id=${it.id}, title=${it.title}")
                    }
                    _allEvents.value = events
                    Log.d("ViewModel", "_allEvents updated. New size: ${_allEvents.value.size}")
                } else {
                    Log.e("ViewModel", "❌ Gagal load all events: ${response.message()}")
                    Log.e("ViewModel", "Response body status: ${body?.status}")
                    _notificationMessage.value = "Gagal memuat event dari server."
                }
            }
            override fun onFailure(call: Call<GetEventsResponse>, t: Throwable) {
                Log.e("ViewModel", "❌ API Failure: ${t.message}")
                t.printStackTrace()
                _notificationMessage.value = "Gagal terhubung ke server."
            }
        })
    }

    fun loadCreatedEvents(userId: Int, context: Context) {
        if (!isNetworkAvailable(context)) return

        // Clear old data dan set loading state SEBELUM API call
        _isLoadingCreatedEvents.value = true
        _createdEvents.value = emptyList() // Clear data lama untuk hindari flash

        ApiClient.instance.getMyCreatedEvents(userId).enqueue(object : Callback<GetEventsResponse> {
            override fun onResponse(call: Call<GetEventsResponse>, response: Response<GetEventsResponse>) {
                _isLoadingCreatedEvents.value = false
                val body = response.body()
                if (response.isSuccessful && body?.status == "success") {
                    _createdEvents.value = body.data.map { it.toEventModel() }
                    Log.d("ViewModel", "✅ Loaded ${body.data.size} created events")
                } else {
                    Log.e("ViewModel", "❌ Failed to load created events: ${response.message()}")
                }
            }
            override fun onFailure(call: Call<GetEventsResponse>, t: Throwable) {
                _isLoadingCreatedEvents.value = false
                Log.e("ViewModel", "Gagal load created events: ${t.message}")
            }
        })
    }

    // Load event yang diikuti dari backend
    fun loadFollowedEvents(userId: Int, context: Context) {
        if (!isNetworkAvailable(context)) return

        ApiClient.instance.getMyRegistrationsByUserId(userId).enqueue(object : Callback<GetEventsResponse> {
            override fun onResponse(call: Call<GetEventsResponse>, response: Response<GetEventsResponse>) {
                val body = response.body()
                if (response.isSuccessful && body?.status == "success") {
                    // Clear dan populate followedEvents dari API
                    _followedEvents.clear()
                    _followedEvents.addAll(body.data.map { it.toEventModel() })
                    Log.d("ViewModel", "✅ Loaded ${body.data.size} followed events")
                } else {
                    Log.e("ViewModel", "❌ Failed to load followed events: ${response.message()}")
                }
            }
            override fun onFailure(call: Call<GetEventsResponse>, t: Throwable) {
                Log.e("ViewModel", "Gagal load followed events: ${t.message}")
            }
        })
    }

    fun addEvent(event: Event, creatorId: Int?, context: Context) {
        if (!isNetworkAvailable(context)) {
            _notificationMessage.value = "Tidak ada koneksi internet."
            return
        }

        // DEBUG: Log thumbnailUri SEBELUM dikirim ke API
        Log.d("AddEvent", "=== ADD EVENT DEBUG ===")
        Log.d("AddEvent", "Event title: ${event.title}")
        Log.d("AddEvent", "thumbnailUri from Event: ${event.thumbnailUri}")
        Log.d("AddEvent", "thumbnailResId from Event: ${event.thumbnailResId}")

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
        
        // DEBUG: Log data yang dikirim ke API
        Log.d("AddEvent", "Sending to API:")
        Log.d("AddEvent", "  - date: ${event.date}")
        Log.d("AddEvent", "  - timeStart: ${event.timeStart}")
        Log.d("AddEvent", "  - timeEnd: ${event.timeEnd}")
        Log.d("AddEvent", "  - thumbnailUri (in request): ${request.thumbnailUri}")

        ApiClient.instance.createEvent(request).enqueue(object : Callback<CreateEventResponse> {
            override fun onResponse(call: Call<CreateEventResponse>, response: Response<CreateEventResponse>) {
                val body = response.body()
                Log.d("AddEvent", "API Response: ${response.code()} - ${body?.status}")
                if (response.isSuccessful && body?.status == "success") {
                    _notificationMessage.value = "Event Berhasil Ditambahkan"
                    loadAllEvents(context)
                    if (creatorId != null) loadCreatedEvents(creatorId, context)
                } else {
                    Log.e("AddEvent", "API Error: ${body?.message ?: response.message()}")
                    _notificationMessage.value = "Gagal: ${body?.message ?: response.message()}"
                }
            }
            override fun onFailure(call: Call<CreateEventResponse>, t: Throwable) {
                Log.e("AddEvent", "API Failure: ${t.message}")
                _notificationMessage.value = "Gagal terhubung ke server."
            }
        })
    }

    // --- FUNGSI UPDATE EVENT ---
    fun updateEvent(updatedEvent: Event, context: Context) {
        Log.d("UpdateEvent", "=== UPDATE EVENT ===")
        Log.d("UpdateEvent", "Event ID: ${updatedEvent.id}")
        Log.d("UpdateEvent", "Title: ${updatedEvent.title}")
        
        // Update lokal terlebih dahulu agar UI langsung berubah
        _createdEvents.value = _createdEvents.value.map {
            if (it.id == updatedEvent.id) updatedEvent else it
        }
        _allEvents.value = _allEvents.value.map {
            if (it.id == updatedEvent.id) updatedEvent else it
        }

        // Kirim ke API jika ada koneksi
        if (isNetworkAvailable(context)) {
            val request = UpdateEventRequest(
                title = updatedEvent.title,
                type = updatedEvent.type,
                date = updatedEvent.date,
                timeStart = updatedEvent.timeStart,
                timeEnd = updatedEvent.timeEnd,
                platformType = updatedEvent.platformType,
                locationDetail = updatedEvent.locationDetail,
                quota = updatedEvent.quota,
                thumbnailUri = updatedEvent.thumbnailUri
            )

            ApiClient.instance.updateEvent(updatedEvent.id, request).enqueue(object : Callback<UpdateEventResponse> {
                override fun onResponse(
                    call: Call<UpdateEventResponse>,
                    response: Response<UpdateEventResponse>
                ) {
                    val body = response.body()
                    if (response.isSuccessful && body?.status == "success") {
                        _notificationMessage.value = "Event Berhasil Diperbarui"
                        Log.d("UpdateEvent", "Event berhasil diupdate ke server")
                    } else {
                        _notificationMessage.value = body?.message ?: "Gagal memperbarui event"
                        Log.e("UpdateEvent", "Gagal update event: ${body?.message}")
                    }
                }

                override fun onFailure(call: Call<UpdateEventResponse>, t: Throwable) {
                    Log.e("UpdateEvent", "API Failure: ${t.message}")
                    _notificationMessage.value = "Event diperbarui lokal, gagal sinkronisasi ke server"
                }
            })
        } else {
            _notificationMessage.value = "Event Berhasil Diperbarui (Offline)"
        }
    }
    // ----------------------------------------------------

    fun getEventById(eventId: Int): Event? {
        return _createdEvents.value.find { it.id == eventId }
            ?: _allEvents.value.find { it.id == eventId }
            ?: _followedEvents.find { it.id == eventId }
    }

    fun deleteEvent(eventId: Int, context: Context) {
        // Update local state first
        _createdEvents.value = _createdEvents.value.filter { it.id != eventId }
        _allEvents.value = _allEvents.value.filter { it.id != eventId }

        // Sync with API
        if (isNetworkAvailable(context)) {
            ApiClient.instance.deleteEvent(eventId).enqueue(object : Callback<DeleteResponse> {
                override fun onResponse(call: Call<DeleteResponse>, response: Response<DeleteResponse>) {
                    val body = response.body()
                    if (response.isSuccessful && body?.status == "success") {
                        _notificationMessage.value = "Event berhasil dihapus"
                        Log.d("DeleteEvent", "Event deleted from server")
                    } else {
                        _notificationMessage.value = body?.message ?: "Gagal menghapus event dari server"
                        Log.e("DeleteEvent", "Failed: ${body?.message}")
                    }
                }

                override fun onFailure(call: Call<DeleteResponse>, t: Throwable) {
                    Log.e("DeleteEvent", "API Failure: ${t.message}")
                    _notificationMessage.value = "Event dihapus (offline)"
                }
            })
        } else {
            _notificationMessage.value = "Event dihapus (offline)"
        }
    }

    fun getFeedbacksForEvent(eventId: Int): List<Feedback> = _feedbacks.value[eventId] ?: emptyList()
    
    fun submitFeedback(eventId: Int, feedback: Feedback, userId: Int, context: Context) {
        Log.d("SubmitFeedback", "=== SUBMIT FEEDBACK ===")
        Log.d("SubmitFeedback", "eventId: $eventId")
        Log.d("SubmitFeedback", "userId: $userId")
        Log.d("SubmitFeedback", "rating: ${feedback.rating}")
        Log.d("SubmitFeedback", "review: ${feedback.review}")
        
        // PERBAIKAN: Cek apakah feedback dengan ID yang sama sudah ada
        val existingList = _feedbacks.value[eventId] ?: emptyList()
        val existingFeedback = existingList.find { it.id == feedback.id }
        
        val updatedList = if (existingFeedback != null) {
            // Jika sudah ada (edit), ganti feedback lama dengan yang baru
            Log.d("SubmitFeedback", "Mengganti feedback existing dengan ID: ${feedback.id}")
            existingList.map { if (it.id == feedback.id) feedback else it }
        } else {
            // Jika baru, tambahkan ke list
            Log.d("SubmitFeedback", "Menambahkan feedback baru dengan ID: ${feedback.id}")
            existingList + feedback
        }
        
        _feedbacks.value = _feedbacks.value.toMutableMap().apply { put(eventId, updatedList) }

        // Kirim ke API jika ada koneksi
        if (isNetworkAvailable(context)) {
            Log.d("SubmitFeedback", "Network available, sending to API...")
            
            val request = FeedbackRequest(
                eventId = eventId,
                userId = userId,
                rating = feedback.rating,
                review = feedback.review,
                photoUri = feedback.photoUri
            )
            
            Log.d("SubmitFeedback", "Request object: $request")

            ApiClient.instance.createFeedback(request).enqueue(object : Callback<FeedbackResponse> {
                override fun onResponse(
                    call: Call<FeedbackResponse>,
                    response: Response<FeedbackResponse>
                ) {
                    Log.d("SubmitFeedback", "Response code: ${response.code()}")
                    Log.d("SubmitFeedback", "Response body: ${response.body()}")
                    Log.d("SubmitFeedback", "Response errorBody: ${response.errorBody()?.string()}")
                    
                    val body = response.body()
                    if (response.isSuccessful && body?.status == "success") {
                        _notificationMessage.value = "Ulasan berhasil ditambahkan"
                        Log.d("SubmitFeedback", "Feedback berhasil disimpan ke server")
                    } else {
                        _notificationMessage.value = body?.message ?: "Gagal menyimpan ulasan"
                        Log.e("SubmitFeedback", "Gagal submit feedback: ${body?.message}")
                    }
                }

                override fun onFailure(call: Call<FeedbackResponse>, t: Throwable) {
                    Log.e("SubmitFeedback", "API Failure: ${t.message}")
                    t.printStackTrace()
                    _notificationMessage.value = "Ulasan tersimpan lokal, gagal sinkronisasi ke server"
                }
            })
        } else {
            Log.d("SubmitFeedback", "No network, saving locally")
            _notificationMessage.value = "Ulasan ditambahkan (offline)"
        }
    }
    
    fun deleteFeedback(eventId: Int, feedbackId: Int, userId: Int, context: Context) {
        // Update local state first
        val existingList = _feedbacks.value[eventId] ?: emptyList()
        _feedbacks.value = _feedbacks.value.toMutableMap().apply {
            put(eventId, existingList.filter { it.id != feedbackId })
        }

        // Sync with API
        if (isNetworkAvailable(context)) {
            ApiClient.instance.deleteFeedback(feedbackId).enqueue(object : Callback<DeleteResponse> {
                override fun onResponse(call: Call<DeleteResponse>, response: Response<DeleteResponse>) {
                    val body = response.body()
                    if (response.isSuccessful && body?.status == "success") {
                        _notificationMessage.value = "Ulasan berhasil dihapus"
                        Log.d("DeleteFeedback", "Feedback deleted from server")
                    } else {
                        _notificationMessage.value = body?.message ?: "Gagal menghapus ulasan dari server"
                        Log.e("DeleteFeedback", "Failed: ${body?.message}")
                    }
                }

                override fun onFailure(call: Call<DeleteResponse>, t: Throwable) {
                    Log.e("DeleteFeedback", "API Failure: ${t.message}")
                    _notificationMessage.value = "Ulasan dihapus (offline)"
                }
            })
        } else {
            _notificationMessage.value = "Ulasan dihapus (offline)"
        }
    }

    fun getDocumentationForEvent(eventId: Int): List<Documentation> = _documentations.value[eventId] ?: emptyList()
    
    fun submitDocumentation(eventId: Int, doc: Documentation, userId: Int, context: Context) {
        Log.d("SubmitDoc", "=== SUBMIT DOCUMENTATION ===")
        Log.d("SubmitDoc", "eventId: $eventId")
        Log.d("SubmitDoc", "userId: $userId")
        Log.d("SubmitDoc", "description: ${doc.description}")
        
        // Tambahkan ke data lokal terlebih dahulu
        val list = _documentations.value[eventId] ?: emptyList()
        _documentations.value = _documentations.value.toMutableMap().apply { put(eventId, list + doc) }

        // Kirim ke API jika ada koneksi
        if (isNetworkAvailable(context)) {
            Log.d("SubmitDoc", "Network available, sending to API...")
            
            val request = DocumentationRequest(
                eventId = eventId,
                userId = userId,
                description = doc.description,
                photoUri = doc.photoUri
            )

            ApiClient.instance.createDocumentation(request).enqueue(object : Callback<DocumentationResponse> {
                override fun onResponse(
                    call: Call<DocumentationResponse>,
                    response: Response<DocumentationResponse>
                ) {
                    Log.d("SubmitDoc", "Response code: ${response.code()}")
                    val body = response.body()
                    if (response.isSuccessful && body?.status == "success") {
                        _notificationMessage.value = "Dokumentasi berhasil ditambahkan"
                        Log.d("SubmitDoc", "Documentation berhasil disimpan ke server")
                    } else {
                        _notificationMessage.value = body?.message ?: "Gagal menyimpan dokumentasi"
                        Log.e("SubmitDoc", "Gagal submit documentation: ${body?.message}")
                    }
                }

                override fun onFailure(call: Call<DocumentationResponse>, t: Throwable) {
                    Log.e("SubmitDoc", "API Failure: ${t.message}")
                    t.printStackTrace()
                    _notificationMessage.value = "Dokumentasi tersimpan lokal, gagal sinkronisasi ke server"
                }
            })
        } else {
            Log.d("SubmitDoc", "No network, saving locally")
            _notificationMessage.value = "Dokumentasi ditambahkan (offline)"
        }
    }
    
    fun deleteDocumentation(eventId: Int, docId: Int, userId: Int, context: Context) {
        // Update local state first
        val existingList = _documentations.value[eventId] ?: emptyList()
        _documentations.value = _documentations.value.toMutableMap().apply {
            put(eventId, existingList.filter { it.id != docId })
        }

        // Sync with API
        if (isNetworkAvailable(context)) {
            ApiClient.instance.deleteDocumentation(docId).enqueue(object : Callback<DeleteResponse> {
                override fun onResponse(call: Call<DeleteResponse>, response: Response<DeleteResponse>) {
                    val body = response.body()
                    if (response.isSuccessful && body?.status == "success") {
                        _notificationMessage.value = "Dokumentasi berhasil dihapus"
                        Log.d("DeleteDoc", "Documentation deleted from server")
                    } else {
                        _notificationMessage.value = body?.message ?: "Gagal menghapus dokumentasi dari server"
                        Log.e("DeleteDoc", "Failed: ${body?.message}")
                    }
                }

                override fun onFailure(call: Call<DeleteResponse>, t: Throwable) {
                    Log.e("DeleteDoc", "API Failure: ${t.message}")
                    _notificationMessage.value = "Dokumentasi dihapus (offline)"
                }
            })
        } else {
            _notificationMessage.value = "Dokumentasi dihapus (offline)"
        }
    }

    fun toggleDocumentationLike(docId: Int) { /* Logic like */ }

    fun registerForEvent(
        event: Event, 
        data: Registration, 
        userId: Int?, 
        context: Context,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        // Cek apakah sudah terdaftar secara lokal
        if (_followedEvents.any { it.id == event.id }) {
            _notificationMessage.value = "Anda sudah terdaftar di event ini"
            onError("Anda sudah terdaftar di event ini")
            return
        }

        // Kirim ke API jika ada koneksi
        if (isNetworkAvailable(context)) {
            val request = EventRegistrationRequest(
                eventId = data.eventId,
                userId = userId,
                name = data.name,
                nim = data.nim,
                fakultas = data.fakultas,
                jurusan = data.jurusan,
                email = data.email,
                phone = data.phone,
                krsUri = data.krsUri
            )

            ApiClient.instance.registerForEvent(request).enqueue(object : Callback<EventRegistrationResponse> {
                override fun onResponse(
                    call: Call<EventRegistrationResponse>,
                    response: Response<EventRegistrationResponse>
                ) {
                    val body = response.body()
                    
                    if (response.isSuccessful && body?.status == "success") {
                        // Berhasil - tambahkan ke lokal state
                        _followedEvents.add(event)
                        _registrations.value = _registrations.value.toMutableMap().apply { put(event.id, data) }
                        _notificationMessage.value = "Berhasil mendaftar ke ${event.title}"
                        Log.d("ViewModel", "Pendaftaran berhasil disimpan ke server")
                        onSuccess() // Panggil callback sukses
                    } else {
                        // Tangani error termasuk 409 Conflict (sudah terdaftar)
                        val errorMessage = when (response.code()) {
                            409 -> "Anda sudah terdaftar di event ini"
                            else -> body?.message ?: "Gagal mendaftar: ${response.message()}"
                        }
                        _notificationMessage.value = errorMessage
                        Log.e("ViewModel", "Gagal register: code=${response.code()}, message=$errorMessage")
                        onError(errorMessage) // Panggil callback error
                    }
                }

                override fun onFailure(call: Call<EventRegistrationResponse>, t: Throwable) {
                    Log.e("ViewModel", "API Failure: ${t.message}")
                    val errorMsg = "Gagal terhubung ke server"
                    _notificationMessage.value = errorMsg
                    onError(errorMsg) // Panggil callback error
                }
            })
        } else {
            // Offline mode - simpan lokal saja
            _followedEvents.add(event)
            _registrations.value = _registrations.value.toMutableMap().apply { put(event.id, data) }
            _notificationMessage.value = "Berhasil mendaftar ke ${event.title} (offline)"
            onSuccess() // Offline dianggap sukses
        }
    }
    fun getRegistrationData(eventId: Int): Registration? = _registrations.value[eventId]
    fun updateRegistrationData(eventId: Int, newData: Registration) {
        _registrations.value = _registrations.value.toMutableMap().apply { put(eventId, newData) }
    }
    
    fun cancelRegistration(eventId: Int, registrationId: Int, context: Context) {
        // Update local state first
        _followedEvents.removeIf { it.id == eventId }
        _registrations.value = _registrations.value.toMutableMap().apply { remove(eventId) }

        // Sync with API
        if (isNetworkAvailable(context)) {
            ApiClient.instance.cancelRegistration(registrationId).enqueue(object : Callback<DeleteResponse> {
                override fun onResponse(call: Call<DeleteResponse>, response: Response<DeleteResponse>) {
                    val body = response.body()
                    if (response.isSuccessful && body?.status == "success") {
                        _notificationMessage.value = "Pendaftaran berhasil dibatalkan"
                        Log.d("CancelReg", "Registration cancelled from server")
                    } else {
                        _notificationMessage.value = body?.message ?: "Gagal membatalkan pendaftaran dari server"
                        Log.e("CancelReg", "Failed: ${body?.message}")
                    }
                }

                override fun onFailure(call: Call<DeleteResponse>, t: Throwable) {
                    Log.e("CancelReg", "API Failure: ${t.message}")
                    _notificationMessage.value = "Pendaftaran dibatalkan (offline)"
                }
            })
        } else {
            _notificationMessage.value = "Pendaftaran dibatalkan (offline)"
        }
    }
    
    fun unfollowEvent(eventId: Int) {
        _followedEvents.removeIf { it.id == eventId }
        _registrations.value = _registrations.value.toMutableMap().apply { remove(eventId) }
    }
    
    fun clearNotification() { _notificationMessage.value = null }
}
