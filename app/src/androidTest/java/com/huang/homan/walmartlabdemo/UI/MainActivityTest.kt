package com.huang.homan.walmartlabdemo.UI

import android.util.Log
import androidx.test.rule.ActivityTestRule
import androidx.test.runner.AndroidJUnit4
import com.huang.homan.walmartlabdemo.Model.ProductPojo.Product
import com.huang.homan.walmartlabdemo.Model.WalmartProduct
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.containsString
import org.hamcrest.Matchers.greaterThan
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class MainActivityTest {

    private lateinit var mainActivity: MainActivity
    private val httpResponseDelay : Long = 5000

    @Rule
    @JvmField
    var activityTestRule = ActivityTestRule(MainActivity::class.java)

    @Before
    fun setUp() {
        mainActivity = activityTestRule.activity
    }

    lateinit var mError: String
    var listSize = 0
    lateinit var productList: List<Product>

    @Test
    fun testWalmartApi() {
        lgi("Start Test")

        // check http error
        checkApiCallback(0, 0)
        Thread.sleep(2000)
        lge("Error: $mError")
        assertThat("Expect HTTP 400", mError, containsString("400"))

        // check http error
        checkApiCallback(1, 0)
        Thread.sleep(2000)
        lge("Error: $mError")
        assertThat("Expect HTTP 400", mError, containsString("400"))

        // check single result
        checkApiCallback(1, 1)
        Thread.sleep(2000)
        lgi("Size: $listSize")
        assertThat("Expect Size > 0", listSize, greaterThan(0))

        // check multiple results
        checkApiCallback(1, 30)
        Thread.sleep(2000)
        lgi("Size: $listSize")
        assertThat("Expect Size > 20", listSize, greaterThan(20))

    }

    fun checkApiCallback(pageNumber: Int, pageSize: Int) {
        mError = "ERROR"
        listSize = 0

        val mProducts =
            WalmartProduct("Test.Package").getResult(pageNumber, pageSize)
        mProducts
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { result ->
                    productList = result.products
                    listSize = productList.size
                },
                { error ->
                    mError = error.message.toString()
                })
    }

    companion object {
        /* Log tag and shortcut */
        private val TAG = "TestLOG " + MainActivityTest::class.java.simpleName

        fun log(s: String) { System.out.println("$TAG $s") } // output windows
        fun lge(message: String) { Log.e(TAG, message) } // logcat error
        fun lgi(message: String) { Log.i(TAG, message) } // logcat info
    }
}