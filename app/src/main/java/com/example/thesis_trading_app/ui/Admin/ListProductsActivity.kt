package com.example.thesis_trading_app.ui.Admin

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.thesis_trading_app.R
import com.example.thesis_trading_app.models.Product
import com.example.thesis_trading_app.ui.Authentication.BaseActivity
import com.example.thesis_trading_app.utils.Constants
import com.example.thesis_trading_app.utils.EndlessRecyclerViewScrollListener
import com.example.thesis_trading_app.utils.GlideLoader
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.*
import jp.wasabeef.recyclerview.animators.SlideInUpAnimator
import kotlinx.android.synthetic.main.activity_list_products.*
import kotlinx.android.synthetic.main.activity_setting.*
import kotlinx.android.synthetic.main.item_admin_product.*

class ListProductsActivity : BaseActivity(){

    private var scrollListener: EndlessRecyclerViewScrollListener? = null
    private lateinit var firebaseFirestore: FirebaseFirestore
    private var adapter: ProductFirestoreRecyclerAdapter? = null
    private lateinit var proName: String
    private lateinit var proBarcode: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_products)

        if (intent.hasExtra(Constants.SEARCH_PRODUCT_NAME)) {
            proName = intent.getStringExtra(Constants.SEARCH_PRODUCT_NAME)!!
            setProductNameAdapter(proName)
        } else if (intent.hasExtra(Constants.SEARCH_PRODUCT_BARCODE)) {
            proBarcode = intent.getStringExtra(Constants.SEARCH_PRODUCT_BARCODE)!!
            setProductBarcodeAdapter(proBarcode)
        }else {
            setAdapter()
        }

        list_product_view.addOnItemTouchListener(object : RecyclerView.OnItemTouchListener {
            override fun onTouchEvent(recycler: RecyclerView, event: MotionEvent) {
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

        list_product_view.scrollToPosition(0)
        scrollListener = object : EndlessRecyclerViewScrollListener(LinearLayoutManager(this)) {
            override fun onLoadMore(page: Int, totalItemsCount: Int, view: RecyclerView?) {
            }
        }
    }

    private fun setAdapter() {
        firebaseFirestore = FirebaseFirestore.getInstance()
        val query = firebaseFirestore.collection("products")
//            .orderBy("product_name", Query.Direction.ASCENDING)
        val options = FirestoreRecyclerOptions.Builder<Product>().setQuery(query, Product::class.java).build()

        adapter = ProductFirestoreRecyclerAdapter(options)
        list_product_view.adapter = adapter

        list_product_view.adapter = adapter
        list_product_view.layoutManager = LinearLayoutManager(this)
        list_product_view.setHasFixedSize(true)
        list_product_view.itemAnimator = SlideInUpAnimator()
    }

    private fun setProductNameAdapter(name: String) {
        firebaseFirestore = FirebaseFirestore.getInstance()
        val query = firebaseFirestore.collection("products").whereArrayContains(Constants.PRODUCT_SEARCH_KEYWORD, name)
        val options = FirestoreRecyclerOptions.Builder<Product>().setQuery(query, Product::class.java).build()

        adapter = ProductFirestoreRecyclerAdapter(options)
        list_product_view.adapter = adapter

        list_product_view.adapter = adapter
        list_product_view.layoutManager = LinearLayoutManager(this)
        list_product_view.setHasFixedSize(true)
        list_product_view.itemAnimator = SlideInUpAnimator()
    }

    private fun setProductBarcodeAdapter(barcode: String) {
        firebaseFirestore = FirebaseFirestore.getInstance()
        val query = firebaseFirestore.collection("products").whereEqualTo(Constants.PRODUCT_BARCODE, barcode.toLong())
        val options = FirestoreRecyclerOptions.Builder<Product>().setQuery(query, Product::class.java).build()

        adapter = ProductFirestoreRecyclerAdapter(options)
        list_product_view.adapter = adapter

        list_product_view.adapter = adapter
        list_product_view.layoutManager = LinearLayoutManager(this)
        list_product_view.setHasFixedSize(true)
        list_product_view.itemAnimator = SlideInUpAnimator()
    }

    private inner class ProductViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {
        fun setProductProperties(
            productName: String,
            productCategory: String,
            productPrice: Long,
            productAmount: Int,
            productBarcode: Long,
            productImage: String
        ) {
            val modifyProduct = view.findViewById<Button>(R.id.modify_product)
            val proName = view.findViewById<TextView>(R.id.product_admin_name)
            val proCategory = view.findViewById<TextView>(R.id.product_admin_category)
            val proPrice = view.findViewById<TextView>(R.id.product_admin_price)
            val proAmount = view.findViewById<TextView>(R.id.product_admin_amount)
            val proBarcode = view.findViewById<TextView>(R.id.product_admin_barcode)
            val proImage = view.findViewById<ImageView>(R.id.product_admin_image)

            proName.text = "Name: " + productName
            proCategory.text = "Category: " + productCategory
            proPrice.text = "Price: " + productPrice.toString()
            proAmount.text = "Amount: " + productAmount.toString()
            proBarcode.text = "Barcode: " + productBarcode.toString()
            GlideLoader(this@ListProductsActivity).loadProductPicture(productImage, proImage)

            modifyProduct.setOnClickListener {
                val intent = Intent(this@ListProductsActivity, ModifyProductActivity::class.java)
                intent.putExtra(Constants.PRODUCT_BARCODE, productBarcode.toString())
                Log.e("BARCODE", productBarcode.toString())
                startActivity(intent)
                finish()
            }
        }
    }

    private inner class ProductFirestoreRecyclerAdapter(options: FirestoreRecyclerOptions<Product>) : FirestoreRecyclerAdapter<Product, ProductViewHolder>(options) {
        override fun onBindViewHolder(productViewHolder: ProductViewHolder, position: Int, productModel: Product) {
            productViewHolder.setProductProperties(productModel.product_name, productModel.product_category, productModel.product_price, productModel.product_amount, productModel.product_barcode, productModel.product_image)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_admin_product, parent, false)
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

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.searchmenu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.search -> {
                startActivity(Intent(this, SearchProductAdminActivity::class.java))
                finish()
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

//    private fun decodeStringToBitmap(str: String) : Bitmap {
//        val imageBytes = Base64.decode(str, Base64.DEFAULT)
//        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
//    }

}