package com.example.data.remote.supabase

import android.content.Context
import android.util.Log
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

sealed class SupabaseConnectionResult {
    data class Success(val latencyMs: Long, val statusCode: Int, val message: String) : SupabaseConnectionResult()
    data class Error(val statusCode: Int?, val message: String) : SupabaseConnectionResult()
    data class NotConfigured(val message: String) : SupabaseConnectionResult()
}

/**
 * Singleton client that creates and caches the Retrofit PostgREST API service.
 * Dynamically re-initializes if the user updates Supabase credentials at runtime.
 */
class SupabaseClient private constructor(private val context: Context) {
    private val config = SupabaseConfig.getInstance(context)

    @Volatile
    private var cachedBaseUrl: String = ""
    @Volatile
    private var cachedApiKey: String = ""
    @Volatile
    private var cachedApiService: SupabaseApiService? = null

    companion object {
        private const val TAG = "SupabaseClient"

        @Volatile
        private var INSTANCE: SupabaseClient? = null

        fun getInstance(context: Context): SupabaseClient {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: SupabaseClient(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    /**
     * Retrieves the SupabaseApiService, recreating the Retrofit instance if the URL or API Key changed.
     */
    fun getApiService(): SupabaseApiService {
        val currentUrl = config.getBaseUrl()
        val currentKey = config.getAnonKey()

        if (cachedApiService != null && currentUrl == cachedBaseUrl && currentKey == cachedApiKey) {
            return cachedApiService!!
        }

        synchronized(this) {
            if (cachedApiService != null && currentUrl == cachedBaseUrl && currentKey == cachedApiKey) {
                return cachedApiService!!
            }

            val logging = HttpLoggingInterceptor { message -> Log.d(TAG, message) }.apply {
                level = HttpLoggingInterceptor.Level.BASIC
            }

            val authInterceptor = Interceptor { chain ->
                val original = chain.request()
                val requestBuilder = original.newBuilder()
                    .header("apikey", currentKey)
                    .header("Authorization", "Bearer $currentKey")
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")

                val request = requestBuilder.build()
                chain.proceed(request)
            }

            val okHttpClient = OkHttpClient.Builder()
                .addInterceptor(authInterceptor)
                .addInterceptor(logging)
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .writeTimeout(20, TimeUnit.SECONDS)
                .build()

            val moshi = Moshi.Builder()
                .addLast(KotlinJsonAdapterFactory())
                .build()

            val retrofit = Retrofit.Builder()
                .baseUrl(currentUrl)
                .client(okHttpClient)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()

            cachedBaseUrl = currentUrl
            cachedApiKey = currentKey
            val service = retrofit.create(SupabaseApiService::class.java)
            cachedApiService = service
            return service
        }
    }

    /**
     * Executes a fast HTTP test ping to the Supabase PostgREST endpoint.
     */
    suspend fun testConnection(): SupabaseConnectionResult = withContext(Dispatchers.IO) {
        if (!config.isConfigured()) {
            return@withContext SupabaseConnectionResult.NotConfigured(
                "Supabase URL or Anon Key is missing. Configure your credentials in Settings or .env"
            )
        }

        val start = System.currentTimeMillis()
        try {
            val response = getApiService().testConnection()
            val latency = System.currentTimeMillis() - start

            if (response.isSuccessful) {
                SupabaseConnectionResult.Success(
                    latencyMs = latency,
                    statusCode = response.code(),
                    message = "Connected to Supabase PostgreSQL (${latency}ms)"
                )
            } else {
                val errorBody = response.errorBody()?.string() ?: response.message()
                SupabaseConnectionResult.Error(
                    statusCode = response.code(),
                    message = "Supabase responded with code ${response.code()}: $errorBody"
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Connection test failed: ${e.message}", e)
            SupabaseConnectionResult.Error(
                statusCode = null,
                message = e.localizedMessage ?: "Failed to connect to Supabase: ${e.message}"
            )
        }
    }
}
