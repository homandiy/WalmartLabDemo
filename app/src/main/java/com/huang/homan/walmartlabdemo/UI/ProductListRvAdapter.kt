package com.huang.homan.walmartlabdemo.UI

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.UiThread
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.huang.homan.walmartlabdemo.Model.ProductPojo.Product
import com.huang.homan.walmartlabdemo.R
import org.jetbrains.anko.verticalMargin


class ProductListRvAdapter(
    val mainActivity: MainActivity,
    val mContext: Context,
    var productList: MutableList<Product>) :
        RecyclerView.Adapter<ProductListRvAdapter.productViewHolder>() {

    inner class productViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        //layout variables
        internal var productIV: ImageView = view.findViewById(R.id.productImageIV)
        internal var productNameTV: TextView = view.findViewById(R.id.productNameTV)
        internal var oneCardCL: ConstraintLayout = view.findViewById(R.id.oneCardCL)
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        lgi("Present product list size: ${productList.size}")
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): productViewHolder {
        //import the layout
        val itemCardView = LayoutInflater.from(parent.context)
            .inflate(R.layout.product_card, parent, false)
        return productViewHolder(itemCardView)
    }

    override fun onBindViewHolder(holder: productViewHolder, position: Int) {
        val mProduct = productList.get(position)

        // Image
        val baseUrl = mContext.getString(R.string.baseUrl)
        val mUrl = baseUrl+mProduct.productImage
        Glide.with(mContext)
            .load(mUrl)
            .error(android.R.drawable.ic_dialog_alert)
            .into(holder.productIV)

        holder.productNameTV.text = mProduct.productName
        holder.oneCardCL.setOnClickListener {
            mainActivity.loadDetailFragment(start+position)
        }

    }

    var start = 0
    fun updateData(start: Int, mList: MutableList<Product>) {
        lge("updateData with: ${mList.size}")

        this.start = start

        // clear data
        productList.clear()
        notifyDataSetChanged()

        // update data
        productList.addAll(mList)
        lgi("pass data to: ${productList.size}")

        //notifyDataSetChanged()

        return
    }

    override fun getItemCount(): Int {
        return productList.size
    }

    companion object {
        private val TAG = "MYLOG " + ProductListRvAdapter::class.java.simpleName

        fun log(s: String) { System.out.println("$TAG $s") } // output windows
        fun lge(message: String) { Log.e(TAG, message) } // logcat error
        fun lgi(message: String) { Log.i(TAG, message) } // logcat info
    }
}
