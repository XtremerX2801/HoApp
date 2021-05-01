package com.example.thesis_trading_app.ui.User.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnItemTouchListener
import com.example.thesis_trading_app.R
import com.example.thesis_trading_app.models.Product
import com.example.thesis_trading_app.ui.User.UserQRActivity
import com.example.thesis_trading_app.utils.Constants
import com.example.thesis_trading_app.utils.EndlessRecyclerViewScrollListener
import com.example.thesis_trading_app.utils.GlideLoader
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import jp.wasabeef.recyclerview.animators.SlideInUpAnimator

class ProductFragment : Fragment(){

    private var scrollListener: EndlessRecyclerViewScrollListener? = null
    lateinit var firebaseFirestore: FirebaseFirestore
    private var adapter: ProductFirestoreRecyclerAdapter? = null

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

        firebaseFirestore = FirebaseFirestore.getInstance()
        val query = firebaseFirestore.collection("products").orderBy("product_name", Query.Direction.ASCENDING)
        val options = FirestoreRecyclerOptions.Builder<Product>().setQuery(query, Product::class.java).build()

        val root = inflater.inflate(R.layout.fragment_product, container, false)
        val productRecyclerView = root.findViewById<View>(R.id.product_fragment) as RecyclerView

        adapter = ProductFirestoreRecyclerAdapter(options)
        productRecyclerView.adapter = adapter

        productRecyclerView.adapter = adapter
        productRecyclerView.layoutManager = LinearLayoutManager(activity)
        productRecyclerView.setHasFixedSize(true)
        productRecyclerView.itemAnimator = SlideInUpAnimator()
        productRecyclerView.addOnItemTouchListener(object : OnItemTouchListener {
            override fun onTouchEvent(recycler: RecyclerView, event: MotionEvent) {
                // Handle on touch events here
            }

            override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {
            }

            override fun onInterceptTouchEvent(
                recycler: RecyclerView,
                event: MotionEvent
            ): Boolean {
                return false
            }
        })

        productRecyclerView.scrollToPosition(0)
        scrollListener = object : EndlessRecyclerViewScrollListener(LinearLayoutManager(activity)) {
            override fun onLoadMore(page: Int, totalItemsCount: Int, view: RecyclerView?) {
//                loadNextDataFromApi(page)
            }
        }

        return root
    }

    private inner class ProductViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {
        fun setProductProperties(product_name: String, product_category: String, product_price: Long, product_image: String) {
            val productName = view.findViewById<TextView>(R.id.product_name)
            val productCategory = view.findViewById<TextView>(R.id.product_category)
            val productPrice = view.findViewById<TextView>(R.id.product_price)
            val productImage = view.findViewById<ImageView>(R.id.product_image)

            productName.text = "Name: " + product_name
            productCategory.text = "Category: " + product_category
            productPrice.text = "Price: " + product_price.toString()
            GlideLoader(view.context).loadProductPicture(product_image, productImage)
        }
    }

    private inner class ProductFirestoreRecyclerAdapter(options: FirestoreRecyclerOptions<Product>) : FirestoreRecyclerAdapter<Product, ProductViewHolder>(options) {
        override fun onBindViewHolder(productViewHolder: ProductViewHolder, position: Int, productModel: Product) {
            productViewHolder.setProductProperties(productModel.product_name, productModel.product_category, productModel.product_price, productModel.product_image)
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
        inflater.inflate(R.menu.product_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.qr_scanner -> {
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
        }

        return super.onOptionsItemSelected(item)
    }

}