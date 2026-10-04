package com.example.messqlpu.data.remote

import com.google.gson.annotations.SerializedName
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.util.concurrent.TimeUnit

// DTOs
data class DiningHallDto(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("type") val type: String,
    @SerializedName("rating") val rating: Float,
    @SerializedName("reviewCount") val reviewCount: Int,
    @SerializedName("distanceMeters") val distanceMeters: Int,
    @SerializedName("crowdLevel") val crowdLevel: String,
    @SerializedName("waitTimeMinutes") val waitTimeMinutes: Int,
    @SerializedName("capacityTotal") val capacityTotal: Int,
    @SerializedName("capacityCurrent") val capacityCurrent: Int,
    @SerializedName("imageUrl") val imageUrl: String,
    @SerializedName("facilities") val facilities: List<String>,
    @SerializedName("openingHours") val openingHours: String,
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("longitude") val longitude: Double,
    @SerializedName("description") val description: String,
    @SerializedName("todaySpecial") val todaySpecial: String?,
    @SerializedName("isRecommendedNow") val isRecommendedNow: Boolean?
)

data class JoinQueueRequest(
    @SerializedName("diningHallId") val diningHallId: String,
    @SerializedName("studentId") val studentId: String,
    @SerializedName("studentName") val studentName: String
)

data class CreateOrderRequest(
    @SerializedName("studentId") val studentId: String,
    @SerializedName("diningHallId") val diningHallId: String,
    @SerializedName("diningHallName") val diningHallName: String,
    @SerializedName("itemIds") val itemIds: List<String>,
    @SerializedName("totalAmount") val totalAmount: Double,
    @SerializedName("pickupTime") val pickupTime: String,
    @SerializedName("note") val note: String
)

interface MessQApiService {
    @GET("api/v1/dining-halls")
    suspend fun getDiningHalls(): List<DiningHallDto>

    @GET("api/v1/dining-halls/{id}")
    suspend fun getDiningHall(@Path("id") id: String): DiningHallDto

    @POST("api/v1/queue/join")
    suspend fun joinQueue(@Body request: JoinQueueRequest): DiningHallDto
}

object RetrofitClient {
    private const val BASE_URL = "https://api.messq-lpu.internal/"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    val apiService: MessQApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(MessQApiService::class.java)
    }
}
