package com.huang.homan.walmartlabdemo.Helper

import com.huang.homan.walmartlabdemo.Model.ProductPojo.ProductResult
import io.reactivex.Single
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path


/**
 * Provide Products list form Walmart Lab
 * by Retrofit and RxJava
 *
 * Example: val walmartService = WalmartApi.Factory.create(httpClient)
 */
interface WalmartApi {

    // Get Products List
    @GET("/walmartproducts/{pageNumber}/{pageSize}")
    fun productsList(
        @Path("pageNumber") pageNumber: Int,
        @Path("pageSize") pageSize: Int
    ): Single<ProductResult>

    /**
     * Factory class for convenient creation of the Api Service interface
     */
    companion object Factory {
        val BaseUrl = "https://mobile-tha-server.firebaseapp.com"

        // Inject Http client for authentication
        fun create(okClient: OkHttpClient): WalmartApi {
            val retrofit = Retrofit.Builder()
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(BaseUrl)
                .client(okClient)
                .build()
            return retrofit.create(WalmartApi::class.java)
        }
    }
}