package com.yr.restfulapidemo.network

/*
 * Copyright (C) 2021 The Android Open Source Project.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

private const val BASE_URL = "http://172.20.10.2:8080/"

val logging: HttpLoggingInterceptor =
    HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BASIC)

// 創建一個信任所有憑證的 TrustManager
val trustAllCerts = arrayOf<TrustManager>(
    object : X509TrustManager {
        override fun checkClientTrusted(
            chain: Array<java.security.cert.X509Certificate>,
            authType: String
        ) {
        }

        override fun checkServerTrusted(
            chain: Array<java.security.cert.X509Certificate>,
            authType: String
        ) {
        }

        override fun getAcceptedIssuers(): Array<java.security.cert.X509Certificate> {
            return arrayOf()
        }
    }
)

// 安裝信任所有憑證的 SSL 上下文
val sslSocketFactory: SSLSocketFactory = SSLContext.getInstance("SSL").apply {
    init(null, trustAllCerts, java.security.SecureRandom())
}.socketFactory

private val okHttpClient = OkHttpClient.Builder()
    .sslSocketFactory(sslSocketFactory, trustAllCerts[0] as X509TrustManager)
    .hostnameVerifier(HostnameVerifier { _, _ -> true })
    .addInterceptor(logging)
    .addInterceptor { chain ->
        val original: Request = chain.request()
        val request: Request = original.newBuilder()
            .addHeader("Content-Type", "application/json")
            .addHeader("mtd", original.method)
            .method(original.method, original.body)
            .build()

        chain.proceed(request)
    }
    .build()

/**
 * Use the Retrofit builder to build a retrofit object using a Moshi converter with our Moshi
 * object.
 */
private val retrofit = Retrofit.Builder()
    .addConverterFactory(GsonConverterFactory.create())
    .baseUrl(BASE_URL)
    .client(okHttpClient)
    .build()


interface TodoItemApiService {
    @GET("todo/{id}")
    suspend fun getTodoItemById(@Path("id") id: Int): TodoItem?

    @POST("todo")
    suspend fun addTodoItem(@Body todoItem: TodoPostItem): TodoItem?

    @PUT("todo/todos/{id}")
    suspend fun updateTodoItem(@Path("id") id: Int, @Body todoItem: TodoPostItem): ResponseBody?

    @DELETE("todo/{id}")
    suspend fun deleteTodoItem(@Path("id") id: Int): ResponseBody?
}

/**
 * A public Api object that exposes the lazy-initialized Retrofit service
 */
object TodoItemApi {
    private lateinit var retrofitService: TodoItemApiService
    private lateinit var retrofit: Retrofit

    fun getRetrofitService(baseUrl: String): TodoItemApiService {
        if (!::retrofitService.isInitialized ||retrofit.baseUrl().toString() != baseUrl) {
            // 設定Gson為寬鬆模式
            val gson = GsonBuilder().setLenient().create()

            // 建立或更新 Retrofit 執行個體，使用新的基礎 URL
            retrofit = Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create(gson))
                .baseUrl(baseUrl)
                .client(okHttpClient)
                .build()

            retrofitService = retrofit.create(TodoItemApiService::class.java)
        }
        return retrofitService
    }
}
