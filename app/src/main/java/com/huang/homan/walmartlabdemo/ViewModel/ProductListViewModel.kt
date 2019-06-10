package com.huang.homan.walmartlabdemo.ViewModel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ReportFragment
import androidx.lifecycle.ViewModel
import com.huang.homan.walmartlabdemo.Model.ProductPojo.Product
import com.huang.homan.walmartlabdemo.Model.WalmartProduct
import com.huang.homan.walmartlabdemo.UI.MainActivity
import com.huang.homan.walmartlabdemo.UI.ProductListFragment
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.*
import kotlin.system.measureTimeMillis


class ProductListViewModel : ViewModel() {

    // live data
    var productListLiveData: MutableLiveData<MutableList<Product>> = MutableLiveData()

    init {
        lgi("Product List ViewModel Created!")
        productListLiveData.value = mutableListOf()
    }

    // RxJava: CompositeDisposable
    private val disposables = CompositeDisposable()

    // Default and setters
    var packageName: String = "My.Package"
    var pageNumber = 1
    var pageSize = 30
    var remainder = 0
    lateinit var listFragment: ProductListFragment

    fun setDefault(packageName: String,
                   pageNumber: Int,
                   pageSize: Int) {
        this.packageName = packageName
        this.pageNumber = pageNumber
        this.pageSize = pageSize
    }

    // Load live data from RxJava
    fun loadData() {
        lge("Load Data: page#$pageNumber    size:$pageSize")
        val mProducts =
            WalmartProduct(packageName).getResult(pageNumber, pageSize)

        disposables.add(
            mProducts
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { result ->
                        val size = productListLiveData.value!!.size

                        if (size < 1) { // empty live data
                            MainActivity.totalProducts = result.totalProducts
                            val quotient = result.totalProducts / pageSize
                            remainder = result.totalProducts % pageSize
                            MainActivity.remainder = remainder

                            when {
                                remainder > 0 -> MainActivity.maxPage = quotient + 1
                                else -> MainActivity.maxPage = quotient
                            }
                        }

                        if (size < result.totalProducts) {
                            productListLiveData.value!!.addAll(result.products)
                            lge("Live data downloaded: ${productListLiveData.value!!.size}")
                        }
                    },
                    { error ->
                        lge("Error: ${error.message.toString()}")
                    }) )
    }

    override fun onCleared() {
        super.onCleared()
        lgi("ViewModel destroyed!")
        disposables.clear()
    }

    companion object {
        /* Log tag and shortcut */
        private val TAG = "MYLOG " + ProductListViewModel::class.java.simpleName

        fun log(s: String) { System.out.println("$TAG $s") } // output windows
        fun lge(message: String) { Log.e(TAG, message) } // logcat error
        fun lgi(message: String) { Log.i(TAG, message) } // logcat info
    }
}
