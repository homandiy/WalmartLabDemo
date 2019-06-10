package com.huang.homan.walmartlabdemo.UI

import android.content.Context
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.huang.homan.walmartlabdemo.Model.ProductPojo.Product
import com.huang.homan.walmartlabdemo.R
import com.huang.homan.walmartlabdemo.UI.MainActivity.Companion.maxPage
import com.huang.homan.walmartlabdemo.UI.MainActivity.Companion.totalProducts
import com.huang.homan.walmartlabdemo.ViewModel.ProductListViewModel
import kotlinx.coroutines.*
import org.jetbrains.anko.support.v4.toast


private const val PAGE = "PAGE"
private const val PAGE_SIZE = "PAGE_SIZE"

class ProductListFragment : Fragment() {
    lateinit var viewModel: ProductListViewModel
    lateinit var productListRV: RecyclerView
    lateinit var mContext: Context
    lateinit var mAdapter: ProductListRvAdapter
    var mList: MutableList<Product> = mutableListOf()
    var pageNumber = 1
    var pageSize = 30
    private var hitBottom = 0

    /**
     *  Lifecycles:
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lgi("onCreate: arguments and viewModel")

        // Inflate the layout for this fragment
        if (arguments != null) {
            pageNumber = arguments!!.getInt(PAGE)
            pageSize = arguments!!.getInt(PAGE_SIZE)
        }

        // Live Data
        viewModel = activity?.run {
            ViewModelProviders.of(this).get(ProductListViewModel::class.java)
        } ?: throw Exception("Invalid Activity")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.product_list_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        lgi("onViewCreated()")
        lge("Backpressed check: ${MainActivity.backPressed}")

        mContext = this.activity!!.applicationContext
        viewModel.setDefault(this.activity!!.packageName, pageNumber, pageSize)

        // Recyclerview
        productListRV = view.findViewById(R.id.productListRV)
        mAdapter = ProductListRvAdapter(
            activity as MainActivity,
            mContext,
            mList
        )

        val mRvManagaer = GridLayoutManager(mContext, 2)
        productListRV.setLayoutManager(mRvManagaer)

        productListRV.setHasFixedSize(true)
        productListRV.addItemDecoration(BottomPaddingDecoration(200))
        productListRV.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)

                val totalItemCount = mRvManagaer.getItemCount()
                val lastVisibleItem = mRvManagaer.findLastVisibleItemPosition()

                lgi ("total Item count $pageNumber-$maxPage:  $lastVisibleItem")

                if (newState == RecyclerView.SCROLL_STATE_IDLE &&
                    lastVisibleItem == totalItemCount - 1) {

                    hitBottom++
                    lge("Hit bottom : $hitBottom")

                    // Hold user's tap n times before loading next page.
                    // So the user can have time to read the end of list.
                    if (hitBottom > 1) {
                        hitBottom = 0

                        // order next page
                        if (pageNumber < maxPage) {
                            toast("      Loading Next Page...     ")

                            // Avoid Out of Index Exception as scrolling down
                            productListRV.scrollToPosition(1)

                            // Loading next page
                            (activity as MainActivity).nextBtJob()
                        } else if (pageNumber == maxPage) {
                            toast("Last Page")
                        }
                    } else {
                        if (pageNumber != maxPage)
                            toast("To next page? (Swipe DOWN again)")
                        else
                            toast("Last Page")
                    }
                }
            }
        })
        productListRV.setAdapter(mAdapter)

        updateData(pageNumber, pageSize)

    }

    override fun onResume() {
        super.onResume()
        lgi("onResume()")

        // Back pressed
        view!!.isFocusableInTouchMode = true
        view!!.requestFocus()
        view!!.setOnKeyListener(object : View.OnKeyListener {
            override fun onKey(v: View, keyCode: Int, event: KeyEvent): Boolean {

                return if (event.getAction() === KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
                    lge("Back Pressed!")
                    getActivity()!!.getSupportFragmentManager().popBackStack()

                    true
                } else false
            }
        })
    }

    // Prepare the data for RecyclerView
    @ExperimentalCoroutinesApi
    fun updateData(pageNumber: Int, pageSize: Int) {
        this.pageNumber = pageNumber
        this.pageSize = pageSize
        lgi("updateData: page# $pageNumber, size: $pageSize")

        viewModel.setDefault(this.activity!!.packageName, pageNumber, pageSize)

        var size = viewModel.productListLiveData.value!!.size

        // present total
        val planTotal = pageNumber * pageSize

        lge("Live Data size: $size vs planTotal: $planTotal")

        if (size < planTotal) {// Has not downloaded yet

            viewModel.loadData() // start download
            val remainder = MainActivity.remainder
            val maxPage = MainActivity.maxPage

            // Monitor download
            val handler = CoroutineExceptionHandler { _, exception -> log("Caught $exception") }
            val checkData = GlobalScope.async(handler) {
                withContext(NonCancellable) {
                    var i = 1
                    while (i < 21) { // cancellable computation loop: 20 times

                        size = viewModel.productListLiveData.value!!.size
                        val nextSize = pageSize * pageNumber

                        // print a message twice per second
                        lgi("Checking at page#$pageNumber of maxPage#$maxPage: #$i -- $size")

                        if (pageNumber < maxPage) {
                            if (size >= nextSize) {
                                lgi("(page#$pageNumber < maxPage($maxPage) && size($size) >= pageSize($pageSize)) is true.")
                                updateView()
                                break
                            }
                        }
                        if (pageNumber == maxPage && size == totalProducts) {
                            lgi("(pageNumber($pageNumber) == maxPage($maxPage) && size($size) >= remainder($remainder)) is true.")
                            updateView()
                            break
                        }
                        i++
                        Thread.sleep(500L) // delay 500ms
                    }
                }
            }
            runBlocking {
                withTimeout(10000) {
                    checkData
                }
            }
        } else { // downloaded
            updateView()
        }

    }

    // Update Recyclerview with portion of list in live data
    fun updateView() {
        lgi("updateView()")
        //setRecyclerViewAdapter() // Recyclerview adapter

        // List in Live data
        val liveList = viewModel.productListLiveData.value!!

        lgi("Live data size: ${liveList.size}")

        var end = pageNumber * pageSize
        if (pageNumber == maxPage) end = totalProducts
        val start = (pageNumber - 1) * pageSize

        lgi("clone from start( $start ) == end: ( $end )")


        activity!!.runOnUiThread {
            mAdapter.updateData(start, liveList.subList(start, end) )
            mAdapter.notifyDataSetChanged()
        }
    }

    // Increase bottom padding to have clear view of last item.
    // Or the scrollbar will block the view.
    inner class BottomPaddingDecoration(private val bottomPadding: Int) : RecyclerView.ItemDecoration() {
        override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
            val position = (view.layoutParams as RecyclerView.LayoutParams).viewLayoutPosition
            if (position == parent.adapter!!.itemCount - 1) {
                outRect.set(0, 0, 0, bottomPadding)
            }
        }
    }

    companion object {
        fun newInstance(pageNumber: Int, pageSize: Int): ProductListFragment {
            val fragment = ProductListFragment()
            val args = Bundle()
            args.putInt(PAGE, pageNumber)
            args.putInt(PAGE_SIZE, pageSize)
            fragment.arguments = args
            return fragment
        }

        /* Log tag and shortcut */
        private val TAG = "MYLOG " + ProductListFragment::class.java.simpleName

        fun log(s: String) { System.out.println("$TAG $s") } // output windows
        fun lge(message: String) { Log.e(TAG, message) } // logcat error
        fun lgi(message: String) { Log.i(TAG, message) } // logcat info

    }
}
