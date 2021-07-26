package com.trading.thesis_trading_app.ui.User.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.trading.thesis_trading_app.R
import com.trading.thesis_trading_app.models.Product
import com.trading.thesis_trading_app.ui.User.ProductInfoActivity
import com.trading.thesis_trading_app.ui.User.SearchProductUserActivity
import com.trading.thesis_trading_app.ui.User.UserQRActivity
import com.trading.thesis_trading_app.utils.Constants
import com.trading.thesis_trading_app.utils.GlideLoader
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import jp.wasabeef.recyclerview.animators.SlideInUpAnimator

class ProductFragment : Fragment(){

    private val firebaseFirestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private var adapter: ProductFirestoreRecyclerAdapter? = null
    lateinit var proName: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // If we want to use the option menu in fragment we need to add it.
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val query: Query

        if (activity?.intent?.hasExtra(Constants.SEARCH_PRODUCT_NAME) == true) {
            proName = activity?.intent?.getStringExtra(Constants.SEARCH_PRODUCT_NAME)!!
            query = firebaseFirestore.collection("products").whereArrayContains(Constants.PRODUCT_SEARCH_KEYWORD, proName)
        } else {
            query = firebaseFirestore.collection("products")
        }

        val options = FirestoreRecyclerOptions.Builder<Product>().setQuery(query, Product::class.java).build()

        val root = inflater.inflate(R.layout.fragment_product, container, false)
        val productRecyclerView = root.findViewById<View>(R.id.product_fragment) as RecyclerView

        adapter = ProductFirestoreRecyclerAdapter(options)
        productRecyclerView.adapter = adapter

        productRecyclerView.adapter = adapter
        productRecyclerView.layoutManager = LinearLayoutManager(activity)
        productRecyclerView.setHasFixedSize(true)
        productRecyclerView.itemAnimator = SlideInUpAnimator()

        productRecyclerView.scrollToPosition(0)

        return root
    }

    private inner class ProductViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {
        fun setProductProperties(
            productName: String,
            productCategory: String,
            productPrice: Long,
            productBarcode: Long,
            productImage: String
        ) {
            val viewProduct = view.findViewById<Button>(R.id.view_user_product)
            val proName = view.findViewById<TextView>(R.id.product_user_name)
            val proCategory = view.findViewById<TextView>(R.id.product_user_category)
            val proPrice = view.findViewById<TextView>(R.id.product_user_price)
            val proImage = view.findViewById<ImageView>(R.id.product_user_image)

            proName.text = "Tên sản phẩm: " + productName
            proCategory.text = "Loại sản phẩm: " + productCategory
            proPrice.text = "Giá: " + productPrice.toString()
            context?.let { GlideLoader(it).loadProductPicture(productImage, proImage) }

            viewProduct.setOnClickListener {
                val intent = Intent(activity, ProductInfoActivity::class.java)
                intent.putExtra(Constants.PRODUCT_BARCODE, productBarcode)
                startActivity(intent)
            }
        }
    }

    private inner class ProductFirestoreRecyclerAdapter(options: FirestoreRecyclerOptions<Product>) : FirestoreRecyclerAdapter<Product, ProductViewHolder>(options) {
        override fun onBindViewHolder(productViewHolder: ProductViewHolder, position: Int, productModel: Product) {
            productViewHolder.setProductProperties(productModel.product_name, productModel.product_category, productModel.product_price, productModel.product_barcode, productModel.product_image)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_product, parent, false)
            return ProductViewHolder(view)
        }
    }

    override fun onStart() {
        super.onStart()
        adapter?.startListening()
    }

    override fun onStop() {
        super.onStop()
        adapter?.stopListening()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.user_product_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.user_qr_scanner -> {
                var userId = ""
                if (activity?.intent?.hasExtra(Constants.EXTRA_USER_DETAILS)  == true) {
                    userId = activity?.intent?.getStringExtra(Constants.EXTRA_USER_DETAILS)!!
                    Log.e("USER", userId)
                }
                val intent = Intent(activity, UserQRActivity::class.java)
                intent.putExtra(Constants.EXTRA_USER_DETAILS, userId)
                startActivity(intent)
                return true
            }

            R.id.user_product_searcher -> {
                val intent = Intent(activity, SearchProductUserActivity::class.java)
                startActivity(intent)
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

}