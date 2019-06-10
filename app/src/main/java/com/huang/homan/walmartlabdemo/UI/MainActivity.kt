package com.huang.homan.walmartlabdemo.UI

import android.Manifest.permission.*
import android.annotation.SuppressLint
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.huang.homan.walmartlabdemo.R
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.widget.TextView
import androidx.appcompat.app.AlertDialog

import kotlinx.coroutines.ExperimentalCoroutinesApi
import com.tbruyelle.rxpermissions2.RxPermissions
import android.os.SystemClock


class MainActivity : AppCompatActivity() {


    val fragmentManager : FragmentManager? = this.supportFragmentManager
    var pageNumber: Int = 1
    var pageSize: Int = 30
    lateinit var prevBT: Button
    lateinit var nextBT: Button
    lateinit var pageTV: TextView
    lateinit var listFragment: ProductListFragment
    // check double click to prevent crash
    var mLastClickTime: Long = 0

    @ExperimentalCoroutinesApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        pageTV = findViewById(R.id.pageTV)

        prevBT = findViewById(R.id.prevBT)
        prevBT.visibility = INVISIBLE
        prevBT.setOnClickListener {
            // double click crash prevention
            if (SystemClock.elapsedRealtime() - mLastClickTime < 1000){
                return@setOnClickListener
            }
            mLastClickTime = SystemClock.elapsedRealtime()

            prevBtJob()
        }

        nextBT = findViewById(R.id.nextBT)
        nextBT.setOnClickListener{
            // double click crash prevention
            if (SystemClock.elapsedRealtime() - mLastClickTime < 1000){
                return@setOnClickListener
            }
            mLastClickTime = SystemClock.elapsedRealtime()

            nextBtJob()
        }

        checkPermission()
    }

    // Get User Permission (RxPermissions)
    // Example link at https://github.com/tbruyelle/RxPermissions
    @SuppressLint("CheckResult")
    fun checkPermission() {
        val rxPermissions: RxPermissions = RxPermissions(this)
        rxPermissions.setLogging(true)
        rxPermissions
            .requestEachCombined(
                INTERNET
            )
            .subscribe { // will emit 2 Permission objects
                    permission ->
                when {
                    permission.granted -> {
                        // `permission.name` is granted !
                        lgi("${permission.name} granted.")
                        loadListFragment(pageNumber, pageSize)
                    }

                    permission.shouldShowRequestPermissionRationale -> {
                        // Denied permission without ask never again
                        lge("${permission.name} not granted.")
                        alertDeniedMsg("${permission.name}\nnot granted.")
                    }

                    else -> {
                        // Denied permission with ask never again
                        // Need to go to the settings
                        lge("${permission.name} not granted.")
                        alertDeniedMsg("${permission.name}\nnot granted.")
                    }
                }
            }
    }

    fun alertDeniedMsg(msg: String) {
        // build alert dialog
        val dialogBuilder = AlertDialog.Builder(this)

        // set message of alert dialog
        dialogBuilder.setMessage("$msg\nExit program!")
            // if the dialog is cancelable
            .setCancelable(false)
            // positive button text and action
            .setPositiveButton("Exit", DialogInterface.OnClickListener {
                    dialog, id -> finish()
            })
        // create dialog box
        val alert = dialogBuilder.create()
        // set title for alert dialog box
        alert.setTitle("Denied Alert")
        // show alert dialog
        alert.show()
    }

    // Job for Prev Button
    @SuppressLint("SetTextI18n")
    @ExperimentalCoroutinesApi
    fun prevBtJob() {
        backPressed = false

        pageNumber--
        nextBT.visibility = VISIBLE
        if (pageNumber == 1)  prevBT.visibility = INVISIBLE

        pageTV.text = "Page - $pageNumber"
        closeDetailFragment()
        listFragment.updateData(pageNumber, pageSize)
    }

    // Job for Next Button
    @SuppressLint("SetTextI18n")
    @ExperimentalCoroutinesApi
    fun nextBtJob() {
        backPressed = false

        if (pageNumber == 1) prevBT.visibility = VISIBLE

        pageNumber++
        if (pageNumber == maxPage) nextBT.visibility = INVISIBLE

        pageTV.text = "Page - $pageNumber"

        closeDetailFragment()
        listFragment.updateData(pageNumber, pageSize)
    }

    // Close Detail Fragment
    fun closeDetailFragment() {
        val item: String = "Detail"
        val fragment =  fragmentManager!!.findFragmentByTag(item)
        if (fragment != null) {
            this.getSupportFragmentManager().popBackStack()
        }
    }

    // Load List Fragment
    fun loadListFragment(page: Int, size: Int) {
        val item = "myListProducts"
        lgi("Page Info: $item")
        lgi("New fragment created: $item")

        val ft: FragmentTransaction = fragmentManager!!.beginTransaction()
        listFragment = ProductListFragment.newInstance(page, size)
        ft.replace(R.id.displayContainer, listFragment, item)
        ft.addToBackStack(null)
        ft.commit()
    }

    // Load Detail Fragment
    fun loadDetailFragment(itemNumber: Int) {
        val ft: FragmentTransaction = fragmentManager!!.beginTransaction()
        val fragment = DetailFragment.newInstance(itemNumber)
        val item: String = "Detail"
        ft.replace(R.id.displayContainer, fragment, item)
        ft.addToBackStack(null)
        ft.commit()
    }

    companion object {
        // Global vars
        var backPressed: Boolean = false
        var maxPage = 2
        var remainder = 0
        var totalProducts: Int = 0

        /* Log tag and shortcut */
        private val TAG = "MYLOG " + MainActivity::class.java.simpleName

        fun log(s: String) { System.out.println("$TAG $s") } // output windows
        fun lge(message: String) { Log.e(TAG, message) } // logcat error
        fun lgi(message: String) { Log.i(TAG, message) } // logcat info

    }
}
