package com.huang.homan.walmartlabdemo.Model

import android.util.Log
import com.huang.homan.walmartlabdemo.BuildConfig
import com.huang.homan.walmartlabdemo.Helper.WalmartApi
import com.huang.homan.walmartlabdemo.Model.ProductPojo.ProductResult
import io.reactivex.Single
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

class WalmartProduct(private val pkgName: String) {

    /**
     * Create a Http client
     *
     */
    fun getHttpClient(pkgName: String) : OkHttpClient {
        val httpClientBuilder = OkHttpClient.Builder()

        // Http Log
        httpClientBuilder.addInterceptor(HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
        })

        // Http Authentication
        httpClientBuilder.addInterceptor { chain ->
            val original = chain.request()

            // Add authentication
            val requestBuilder = original.newBuilder()
                .header("X-Android-Package", pkgName)

            val request = requestBuilder.build()
            chain.proceed(request)
        }
        return httpClientBuilder.build()
    }

    // get Walmart Product result by RxJava
    fun getResult(pageNumber: Int, pageSize: Int): Single<ProductResult> {
        lgi("Product List: page# $pageNumber and size# $pageSize")
        val apiService = WalmartApi.create( getHttpClient(pkgName) )
        return apiService.productsList(pageNumber, pageSize)
    }

    companion object {
        /* Log tag and shortcut */
        private val TAG = "MYLOG " + WalmartProduct::class.java.simpleName

        fun log(s: String) { System.out.println("$TAG $s") } // output windows
        fun lge(message: String) { Log.e(TAG, message) } // logcat error
        fun lgi(message: String) { Log.i(TAG, message) } // logcat info
    }
}
