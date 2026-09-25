package com.example.data.remote.supabase

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * Retrofit interface defining PostgREST endpoints for all 16 MedTime entities.
 * Supabase PostgREST maps HTTP verbs and headers to PostgreSQL operations:
 * - 'Prefer: resolution=merge-duplicates' executes PostgreSQL ON CONFLICT DO UPDATE (UPSERT)
 * - 'select=*' fetches full records
 */
interface SupabaseApiService {

    // ----------------------------------------------------
    // Health Check / Connection Test
    // ----------------------------------------------------
    @GET("rest/v1/users")
    suspend fun testConnection(
        @Query("select") select: String = "id",
        @Query("limit") limit: Int = 1
    ): Response<ResponseBody>

    // ----------------------------------------------------
    // 1. Users
    // ----------------------------------------------------
    @GET("rest/v1/users")
    suspend fun getUsers(@Query("select") select: String = "*"): List<UserDto>

    @POST("rest/v1/users")
    @Headers("Prefer: resolution=merge-duplicates")
    suspend fun upsertUsers(@Body users: List<UserDto>): Response<ResponseBody>

    @POST("rest/v1/users")
    @Headers("Prefer: resolution=merge-duplicates")
    suspend fun upsertUser(@Body user: UserDto): Response<ResponseBody>

    @DELETE("rest/v1/users")
    suspend fun deleteUser(@Query("id") idFilter: String): Response<ResponseBody>

    // ----------------------------------------------------
    // 2. Medicines
    // ----------------------------------------------------
    @GET("rest/v1/medicines")
    suspend fun getMedicines(@Query("select") select: String = "*"): List<MedicineDto>

    @GET("rest/v1/medicines")
    suspend fun getMedicinesByPatient(
        @Query("patient_id") patientFilter: String,
        @Query("select") select: String = "*"
    ): List<MedicineDto>

    @POST("rest/v1/medicines")
    @Headers("Prefer: resolution=merge-duplicates")
    suspend fun upsertMedicines(@Body medicines: List<MedicineDto>): Response<ResponseBody>

    @POST("rest/v1/medicines")
    @Headers("Prefer: resolution=merge-duplicates")
    suspend fun upsertMedicine(@Body medicine: MedicineDto): Response<ResponseBody>

    @DELETE("rest/v1/medicines")
    suspend fun deleteMedicine(@Query("id") idFilter: String): Response<ResponseBody>

    // ----------------------------------------------------
    // 3. Medications (Legacy/Calendar)
    // ----------------------------------------------------
    @GET("rest/v1/medications")
    suspend fun getMedications(@Query("select") select: String = "*"): List<MedicationDto>

    @POST("rest/v1/medications")
    @Headers("Prefer: resolution=merge-duplicates")
    suspend fun upsertMedications(@Body medications: List<MedicationDto>): Response<ResponseBody>

    // ----------------------------------------------------
    // 4. Medicine Reminders
    // ----------------------------------------------------
    @GET("rest/v1/medicine_reminders")
    suspend fun getReminders(@Query("select") select: String = "*"): List<MedicineReminderDto>

    @POST("rest/v1/medicine_reminders")
    @Headers("Prefer: resolution=merge-duplicates")
    suspend fun upsertReminders(@Body reminders: List<MedicineReminderDto>): Response<ResponseBody>

    @POST("rest/v1/medicine_reminders")
    @Headers("Prefer: resolution=merge-duplicates")
    suspend fun upsertReminder(@Body reminder: MedicineReminderDto): Response<ResponseBody>

    @DELETE("rest/v1/medicine_reminders")
    suspend fun deleteReminder(@Query("id") idFilter: String): Response<ResponseBody>

    // ----------------------------------------------------
    // 5. Medicine History
    // ----------------------------------------------------
    @GET("rest/v1/medicine_history")
    suspend fun getHistory(@Query("select") select: String = "*"): List<MedicineHistoryDto>

    @POST("rest/v1/medicine_history")
    @Headers("Prefer: resolution=merge-duplicates")
    suspend fun upsertHistories(@Body historyList: List<MedicineHistoryDto>): Response<ResponseBody>

    @POST("rest/v1/medicine_history")
    @Headers("Prefer: resolution=merge-duplicates")
    suspend fun insertHistory(@Body history: MedicineHistoryDto): Response<ResponseBody>

    // ----------------------------------------------------
    // 6. Appointments
    // ----------------------------------------------------
    @GET("rest/v1/appointments")
    suspend fun getAppointments(@Query("select") select: String = "*"): List<AppointmentDto>

    @POST("rest/v1/appointments")
    @Headers("Prefer: resolution=merge-duplicates")
    suspend fun upsertAppointments(@Body appointments: List<AppointmentDto>): Response<ResponseBody>

    @POST("rest/v1/appointments")
    @Headers("Prefer: resolution=merge-duplicates")
    suspend fun upsertAppointment(@Body appointment: AppointmentDto): Response<ResponseBody>

    @DELETE("rest/v1/appointments")
    suspend fun deleteAppointment(@Query("id") idFilter: String): Response<ResponseBody>

    // ----------------------------------------------------
    // 7. Messages
    // ----------------------------------------------------
    @GET("rest/v1/messages")
    suspend fun getMessages(@Query("select") select: String = "*"): List<MessageDto>

    @POST("rest/v1/messages")
    @Headers("Prefer: resolution=merge-duplicates")
    suspend fun upsertMessages(@Body messages: List<MessageDto>): Response<ResponseBody>

    @POST("rest/v1/messages")
    @Headers("Prefer: resolution=merge-duplicates")
    suspend fun insertMessage(@Body message: MessageDto): Response<ResponseBody>

    // ----------------------------------------------------
    // 8. Caretaker Links
    // ----------------------------------------------------
    @GET("rest/v1/caretaker_links")
    suspend fun getCaretakerLinks(@Query("select") select: String = "*"): List<CaretakerLinkDto>

    @POST("rest/v1/caretaker_links")
    @Headers("Prefer: resolution=merge-duplicates")
    suspend fun upsertCaretakerLinks(@Body links: List<CaretakerLinkDto>): Response<ResponseBody>

    @POST("rest/v1/caretaker_links")
    @Headers("Prefer: resolution=merge-duplicates")
    suspend fun upsertCaretakerLink(@Body link: CaretakerLinkDto): Response<ResponseBody>

    // ----------------------------------------------------
    // 9. Medical Documents
    // ----------------------------------------------------
    @GET("rest/v1/medical_documents")
    suspend fun getDocuments(@Query("select") select: String = "*"): List<MedicalDocumentDto>

    @POST("rest/v1/medical_documents")
    @Headers("Prefer: resolution=merge-duplicates")
    suspend fun upsertDocuments(@Body docs: List<MedicalDocumentDto>): Response<ResponseBody>

    @POST("rest/v1/medical_documents")
    @Headers("Prefer: resolution=merge-duplicates")
    suspend fun upsertDocument(@Body doc: MedicalDocumentDto): Response<ResponseBody>

    @DELETE("rest/v1/medical_documents")
    suspend fun deleteDocument(@Query("id") idFilter: String): Response<ResponseBody>

    // ----------------------------------------------------
    // 10. Notifications
    // ----------------------------------------------------
    @GET("rest/v1/notifications")
    suspend fun getNotifications(@Query("select") select: String = "*"): List<NotificationDto>

    @POST("rest/v1/notifications")
    @Headers("Prefer: resolution=merge-duplicates")
    suspend fun upsertNotifications(@Body notifications: List<NotificationDto>): Response<ResponseBody>

    @POST("rest/v1/notifications")
    @Headers("Prefer: resolution=merge-duplicates")
    suspend fun insertNotification(@Body notification: NotificationDto): Response<ResponseBody>

    // ----------------------------------------------------
    // 11. Audit Logs
    // ----------------------------------------------------
    @GET("rest/v1/audit_logs")
    suspend fun getAuditLogs(@Query("select") select: String = "*"): List<AuditLogDto>

    @POST("rest/v1/audit_logs")
    @Headers("Prefer: resolution=merge-duplicates")
    suspend fun upsertAuditLogs(@Body logs: List<AuditLogDto>): Response<ResponseBody>

    @POST("rest/v1/audit_logs")
    @Headers("Prefer: resolution=merge-duplicates")
    suspend fun insertAuditLog(@Body log: AuditLogDto): Response<ResponseBody>

    // ----------------------------------------------------
    // 12. Announcements
    // ----------------------------------------------------
    @GET("rest/v1/announcements")
    suspend fun getAnnouncements(@Query("select") select: String = "*"): List<AnnouncementDto>

    @POST("rest/v1/announcements")
    @Headers("Prefer: resolution=merge-duplicates")
    suspend fun upsertAnnouncements(@Body announcements: List<AnnouncementDto>): Response<ResponseBody>

    // ----------------------------------------------------
    // 13. Patient Addresses
    // ----------------------------------------------------
    @GET("rest/v1/patient_saved_addresses")
    suspend fun getPatientAddresses(@Query("select") select: String = "*"): List<PatientAddressDto>

    @POST("rest/v1/patient_saved_addresses")
    @Headers("Prefer: resolution=merge-duplicates")
    suspend fun upsertPatientAddresses(@Body addresses: List<PatientAddressDto>): Response<ResponseBody>

    @POST("rest/v1/patient_saved_addresses")
    @Headers("Prefer: resolution=merge-duplicates")
    suspend fun upsertPatientAddress(@Body address: PatientAddressDto): Response<ResponseBody>

    @DELETE("rest/v1/patient_saved_addresses")
    suspend fun deletePatientAddress(@Query("id") idFilter: String): Response<ResponseBody>

    // ----------------------------------------------------
    // 14. Caretaker Assistance Requests
    // ----------------------------------------------------
    @GET("rest/v1/caretaker_assistance_requests")
    suspend fun getAssistanceRequests(@Query("select") select: String = "*"): List<CaretakerAssistanceRequestDto>

    @POST("rest/v1/caretaker_assistance_requests")
    @Headers("Prefer: resolution=merge-duplicates")
    suspend fun upsertAssistanceRequests(@Body requests: List<CaretakerAssistanceRequestDto>): Response<ResponseBody>

    @POST("rest/v1/caretaker_assistance_requests")
    @Headers("Prefer: resolution=merge-duplicates")
    suspend fun upsertAssistanceRequest(@Body request: CaretakerAssistanceRequestDto): Response<ResponseBody>

    // ----------------------------------------------------
    // 15. Wallets
    // ----------------------------------------------------
    @GET("rest/v1/wallets")
    suspend fun getWallets(@Query("select") select: String = "*"): List<WalletDto>

    @POST("rest/v1/wallets")
    @Headers("Prefer: resolution=merge-duplicates")
    suspend fun upsertWallets(@Body wallets: List<WalletDto>): Response<ResponseBody>

    @POST("rest/v1/wallets")
    @Headers("Prefer: resolution=merge-duplicates")
    suspend fun upsertWallet(@Body wallet: WalletDto): Response<ResponseBody>

    // ----------------------------------------------------
    // 16. Wallet Transactions
    // ----------------------------------------------------
    @GET("rest/v1/wallet_transactions")
    suspend fun getWalletTransactions(@Query("select") select: String = "*"): List<WalletTransactionDto>

    @POST("rest/v1/wallet_transactions")
    @Headers("Prefer: resolution=merge-duplicates")
    suspend fun upsertWalletTransactions(@Body txns: List<WalletTransactionDto>): Response<ResponseBody>

    @POST("rest/v1/wallet_transactions")
    @Headers("Prefer: resolution=merge-duplicates")
    suspend fun insertWalletTransaction(@Body txn: WalletTransactionDto): Response<ResponseBody>
}
