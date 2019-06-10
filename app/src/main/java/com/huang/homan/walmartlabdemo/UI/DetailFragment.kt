package com.huang.homan.walmartlabdemo.UI


import android.graphics.Color
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.bumptech.glide.Glide
import com.huang.homan.walmartlabdemo.Model.ProductPojo.Product
import com.huang.homan.walmartlabdemo.R
import com.huang.homan.walmartlabdemo.ViewModel.ProductListViewModel

private const val ARG_PARAM1 = "param1"

/**
 * Show detail from selected item of product list fragment
 */
class DetailFragment : Fragment() {

    lateinit var viewModel: ProductListViewModel
    private var itemNumber: Int = 0
    private lateinit var mProduct: Product

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lgi("onCreate()")

        // Inflate the layout for this fragment
        if (arguments != null) {
            itemNumber = arguments!!.getInt(ARG_PARAM1)
        }

        // Get Live Data
        viewModel = activity?.run {
            ViewModelProviders.of(this).get(ProductListViewModel::class.java)
        } ?: throw Exception("Invalid Activity")

        lge("LiveDataSize: ${viewModel.productListLiveData.value!!.size}")
        mProduct = viewModel.productListLiveData.value!!.get(itemNumber)
        lgi("Product: ${mProduct.productName}")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_detail, container, false)
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        lgi("onViewCreated()")

        // Image
        val productImageIV = view.findViewById<ImageView>(R.id.productImageIV)
        val mUrl = getString(R.string.baseUrl)+mProduct.productImage
        Glide.with(context!!)
            .load(mUrl)
            .error(android.R.drawable.ic_dialog_alert)
            .override(300, 200)
            .into(productImageIV)

        // Name
        val productNameTV = view.findViewById<TextView>(R.id.productNameTV)
        productNameTV.text = mProduct.productName

        // Price
        val priceTV = view.findViewById<TextView>(R.id.priceTV)
        priceTV.text = mProduct.price

        // Short Description HTML
        val shortDescTV = view.findViewById<TextView>(R.id.shortDescTV)
        val shortHtml = mProduct.shortDescription
        lgi("Short: $shortHtml")
        if (shortHtml != null) {
            val spannedShort = Html.fromHtml(shortHtml, Html.FROM_HTML_MODE_COMPACT)
            shortDescTV.text = spannedShort
        }

        // Long Description
        val longDescTV = view.findViewById<TextView>(R.id.longDescTV)
        val longHtml = mProduct.longDescription
        if (longHtml != null) {
            val spannedLong = Html.fromHtml(longHtml, Html.FROM_HTML_MODE_COMPACT)
            longDescTV.text = spannedLong
        }

        // In Stock
        val inStockTV = view.findViewById<TextView>(R.id.inStockTV)
        if (mProduct.inStock) {
            inStockTV.setTextColor(Color.GREEN)
        } else {
            inStockTV.setTextColor(Color.RED)
            inStockTV.text = "Out of Order"
        }

        // User Rating
        val ratingTV = view.findViewById<TextView>(R.id.ratingTV)
        val ratingInfo = "Rating:  ${mProduct.reviewRating}"
        ratingTV.text = ratingInfo

        // User Reviewed
        val reviewTV = view.findViewById<TextView>(R.id.reviewTV)
        val reviewInfo = "Reviewed:  ${mProduct.reviewCount}"
        reviewTV.text = reviewInfo

    }

    override fun onResume() {
        super.onResume()
        view!!.isFocusableInTouchMode = true
        view!!.requestFocus()
        view!!.setOnKeyListener(object : View.OnKeyListener {
            override fun onKey(v: View, keyCode: Int, event: KeyEvent): Boolean {

                return if (event.getAction() === KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
                    lge("Back Pressed!")
                    MainActivity.backPressed = true
                    getActivity()!!.getSupportFragmentManager().popBackStack()

                    true
                } else false
            }
        })
    }

    companion object {
        @JvmStatic
        fun newInstance(itemNumber: Int) : DetailFragment {
            val fragment = DetailFragment()
            val args = Bundle()
            args.putInt(ARG_PARAM1, itemNumber)
            fragment.arguments = args
            return fragment
        }

        /* Log tag and shortcut */
        private val TAG = "MYLOG " + DetailFragment::class.java.simpleName

        fun log(s: String) { System.out.println("$TAG $s") } // output windows
        fun lge(message: String) { Log.e(TAG, message) } // logcat error
        fun lgi(message: String) { Log.i(TAG, message) } // logcat info
    }
}
