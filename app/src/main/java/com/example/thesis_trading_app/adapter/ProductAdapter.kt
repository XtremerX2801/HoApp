package com.example.thesis_trading_app.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.thesis_trading_app.R
import com.example.thesis_trading_app.models.Product
import com.example.thesis_trading_app.utils.ContactDiffCallback
import com.firebase.ui.firestore.FirestoreRecyclerOptions

// Not need anymore
class ProductAdapter(private val mProducts: MutableList<Product>, val options: FirestoreRecyclerOptions<Product>) : RecyclerView.Adapter<ProductAdapter.ViewHolder>() {

    // Provide a direct reference to each of the views within a data item
    // Used to cache the views within the item layout for fast access
    inner class ViewHolder(listItemView: View) : RecyclerView.ViewHolder(listItemView) {
        // Your holder should contain and initialize a member variable
        // for any view that will be set as you render a row
        val productName: TextView = itemView.findViewById(R.id.product_name)
        val productPrice: TextView = itemView.findViewById(R.id.product_price)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductAdapter.ViewHolder {
        val context = parent.context
        val inflater = LayoutInflater.from(context)
        // Inflate the custom layout
        val contactView = inflater.inflate(R.layout.item_product, parent, false)
        // Return a new holder instance
        return ViewHolder(contactView)
    }

    // Involves populating data into the item through holder
    override fun onBindViewHolder(viewHolder: ProductAdapter.ViewHolder, position: Int) {
        // Get the data model based on position
        val product: Product = mProducts[position]
        // Set item views based on your views and data model
        val name = viewHolder.productName as TextView
        name.text = product.product_name
        val price = viewHolder.productPrice as TextView
        price.text = product.product_price.toString()
    }

    // Returns the total count of items in the list
    override fun getItemCount(): Int {
        return mProducts.size
    }

    fun swapItems(products: MutableList<Product>?) {
        // compute diffs
        val diffCallback = ContactDiffCallback(this.mProducts, products!!)
        val diffResult = DiffUtil.calculateDiff(diffCallback)

        // clear contacts and add
        this.mProducts.clearAndAddAll(products)

        diffResult.dispatchUpdatesTo(this) // calls adapter's notify methods after diff is computed
    }

    private fun <E> MutableCollection<E>.clearAndAddAll(replace: MutableList<E>) {
        clear()
        addAll(replace)
    }

}