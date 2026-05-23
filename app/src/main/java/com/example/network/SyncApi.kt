package com.example.network

import com.example.data.Account
import com.example.data.Budget
import com.example.data.Transaction
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import java.util.concurrent.TimeUnit

// Network Data Models
data class LoginRequest(
    val email: String,
    val password: String
)

data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String
)

data class AuthResponse(
    val token: String,
    val userId: String,
    val email: String,
    val name: String? = null
)

data class AccountNetwork(
    val localId: Int,
    val name: String,
    val kind: String,
    val balanceNature: String,
    val currency: String,
    val currentBalance: Double,
    val initialBalance: Double,
    val isArchived: Boolean
)

data class TransactionNetwork(
    val localId: Int,
    val type: String,
    val status: String,
    val kind: String,
    val accountId: Int,
    val amount: Double,
    val currency: String,
    val counterpartyAccountId: Int? = null,
    val counterpartyAmount: Double? = null,
    val counterpartyCurrency: String? = null,
    val category: String,
    val description: String,
    val date: Long
)

data class BudgetNetwork(
    val localId: Int,
    val category: String,
    val limitAmount: Double,
    val currency: String
)

data class SyncPayload(
    val accounts: List<AccountNetwork>,
    val transactions: List<TransactionNetwork>,
    val budgets: List<BudgetNetwork>
)

data class SyncResponse(
    val success: Boolean,
    val message: String? = null,
    val accounts: List<AccountNetwork>? = null,
    val transactions: List<TransactionNetwork>? = null,
    val budgets: List<BudgetNetwork>? = null
)

// Retrofit API Definition
interface SyncApi {
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): AuthResponse

    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): AuthResponse

    @POST("api/sync/push")
    suspend fun syncPush(
        @Header("Authorization") token: String,
        @Body payload: SyncPayload
    ): SyncResponse

    @GET("api/sync/pull")
    suspend fun syncPull(
        @Header("Authorization") token: String
    ): SyncResponse
}

// Retrofit Client Builder
object SyncApiClient {
    private var customBaseUrl: String = "https://fintrack-api.example.com/" // Default production URL

    fun setBaseUrl(url: String) {
        var cleanUrl = url.trim()
        if (!cleanUrl.endsWith("/")) {
            cleanUrl += "/"
        }
        if (!cleanUrl.startsWith("http://") && !cleanUrl.startsWith("https://")) {
            cleanUrl = "https://$cleanUrl"
        }
        customBaseUrl = cleanUrl
    }

    fun getBaseUrl(): String = customBaseUrl

    private fun createOkHttpClient(): OkHttpClient {
        val logger = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        return OkHttpClient.Builder()
            .addInterceptor(logger)
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .build()
    }

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    val service: SyncApi by lazy {
        Retrofit.Builder()
            .baseUrl(customBaseUrl)
            .client(createOkHttpClient())
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(SyncApi::class.java)
    }
}

// Helpers to map Room models to network models and vice versa
fun Account.toNetwork() = AccountNetwork(
    localId = id,
    name = name,
    kind = kind,
    balanceNature = balanceNature,
    currency = currency,
    currentBalance = currentBalance,
    initialBalance = initialBalance,
    isArchived = isArchived
)

fun Transaction.toNetwork() = TransactionNetwork(
    localId = id,
    type = type,
    status = status,
    kind = kind,
    accountId = accountId,
    amount = amount,
    currency = currency,
    counterpartyAccountId = counterpartyAccountId,
    counterpartyAmount = counterpartyAmount,
    counterpartyCurrency = counterpartyCurrency,
    category = category,
    description = description,
    date = date
)

fun Budget.toNetwork() = BudgetNetwork(
    localId = id,
    category = category,
    limitAmount = limitAmount,
    currency = currency
)
