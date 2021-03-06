package com.trading.thesis_trading_app.ui.User.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.journeyapps.barcodescanner.BarcodeEncoder
import com.trading.thesis_trading_app.R
import com.trading.thesis_trading_app.firestore.FirestoreClass
import com.trading.thesis_trading_app.ui.User.Payment.PaymentActivity
import com.trading.thesis_trading_app.utils.Constants
import com.trading.thesis_trading_app.utils.EndlessRecyclerViewScrollListener
import com.trading.thesis_trading_app.utils.GlideLoader
import jp.wasabeef.recyclerview.animators.SlideInUpAnimator
import kotlinx.android.synthetic.main.fragment_orders.view.*


class OrdersFragment : Fragment() , View.OnClickListener{

    private var scrollListener: EndlessRecyclerViewScrollListener? = null
    private val firebaseFirestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private var adapter: CartProductsAdapter? = null
    private val userId = FirestoreClass().getCurrentUserId()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val root = inflater.inflate(R.layout.fragment_orders, container, false)
        val productRecyclerView = root.findViewById<View>(R.id.product_ordered_fragment) as RecyclerView
        val qrImage = root.findViewById(R.id.the_qr) as ImageView

        generateQRCode(qrImage)

        firebaseFirestore.collection(Constants.CARTS).whereEqualTo(Constants.USER_ID, userId).get().addOnSuccessListener {
            for (document in it) {
                val cartProNum = document.data[Constants.CART_PRODUCT_NUM] as Long
                val cartPrice = document.data[Constants.CART_PRICE] as Long
                val cartProducts = document.data[Constants.CART_PRODUCTS] as MutableList<Long>

                root.pro_num.text = "T???ng s??? l?????ng s???n ph???m: " + cartProNum.toString()
                root.pro_price.text = "T???ng gi?? ti???n: " + cartPrice.toString() + "??"

                adapter = CartProductsAdapter(cartProducts)
                productRecyclerView.adapter = adapter
                productRecyclerView.layoutManager = LinearLayoutManager(activity)
                productRecyclerView.setHasFixedSize(true)
                productRecyclerView.itemAnimator = SlideInUpAnimator()
            }
        }

        productRecyclerView.addOnItemTouchListener(object : RecyclerView.OnItemTouchListener {
            override fun onTouchEvent(recycler: RecyclerView, event: MotionEvent) {
                refresh()
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
            }
        }

        root.btn_cashout.setOnClickListener(this)

        return root
    }

    @Suppress("DEPRECATION")
    private inner class CartProductsAdapter(private val product_list: List<Long>) : RecyclerView.Adapter<CartProductsAdapter.ViewHolder>(){
        inner class ViewHolder(view: View) :RecyclerView.ViewHolder(view) {
            val productDeleteButton: Button = view.findViewById(R.id.cart_product_delete)
            val productName: TextView = view.findViewById(R.id.cart_product_name)
            val productCategory: TextView = view.findViewById(R.id.cart_product_category)
            val productPrice: TextView = view.findViewById(R.id.cart_product_price)
            val productAmount: TextView = view.findViewById(R.id.cart_product_amount)
            val productImage: ImageView = view.findViewById(R.id.cart_product_image)
            val productAdd: Button = view.findViewById(R.id.cart_product_add)
            val productRemove: Button = view.findViewById(R.id.cart_product_remove)
        }

        override fun onBindViewHolder(productViewHolder: ViewHolder, position: Int) {
            firebaseFirestore.collection(Constants.PRODUCTS).whereEqualTo(
                Constants.PRODUCT_BARCODE,
                product_list[position]
            ).get().addOnSuccessListener {
                for (document in it) {
                    val productPrice = document.data[Constants.PRODUCT_PRICE] as Long
                    productViewHolder.productName.text = "T??n s???n ph???m: " + document.data[Constants.PRODUCT_NAME].toString()
                    productViewHolder.productCategory.text = "Lo???i h??ng: " + document.data[Constants.PRODUCT_CATEGORY].toString()
                    productViewHolder.productPrice.text = "Gi??: " + productPrice.toString() + "??"
                    val image = document.data[Constants.PRODUCT_IMAGE] as String

                    context?.let { it1 -> GlideLoader(it1).loadProductPicture(
                        image,
                        productViewHolder.productImage
                    ) }
                    firebaseFirestore.collection(Constants.CARTS).whereEqualTo(
                        Constants.USER_ID,
                        userId
                    ).get().addOnSuccessListener { it1 ->
                        for (document1 in it1) {
                            val cartHashMap = HashMap<String, Any>()
                            val cartPrice1 = document1.data[Constants.CART_PRICE] as Long
                            val cartProductAmount1 = document1.data[Constants.CART_PRODUCT_AMOUNT] as MutableList<Int>
                            val cartProducts = document1.data[Constants.CART_PRODUCTS] as MutableList<Long>
                            val cartProductNum = document1.data[Constants.CART_PRODUCT_NUM] as Long

                            productViewHolder.productAmount.text = cartProductAmount1[position].toString()
                            productViewHolder.productAdd.setOnClickListener {
                                cartProductAmount1[position] += 1
                                productViewHolder.productAmount.text = cartProductAmount1[position].toString()
                                cartHashMap[Constants.CART_PRICE] = cartPrice1 + productPrice
                                cartHashMap[Constants.CART_PRODUCT_AMOUNT] = cartProductAmount1
                                cartHashMap[Constants.CART_PRODUCT_NUM] = cartProductNum + 1
                                FirebaseFirestore.getInstance().collection(Constants.CARTS).document(
                                    document1.id
                                ).update(cartHashMap)
                                refresh()
                            }
                            productViewHolder.productRemove.setOnClickListener {
                                cartProductAmount1[position] -= 1
                                if (cartProductAmount1[position] == 0) {
                                    cartProductAmount1.removeAt(position)
                                    cartProducts.removeAt(position)
                                    cartHashMap[Constants.CART_PRODUCTS] = cartProducts
                                } else {
                                    productViewHolder.productAmount.text = cartProductAmount1[position].toString()
                                }
                                cartHashMap[Constants.CART_PRICE] = cartPrice1 - productPrice
                                cartHashMap[Constants.CART_PRODUCT_AMOUNT] = cartProductAmount1
                                cartHashMap[Constants.CART_PRODUCT_NUM] = cartProductNum - 1
                                FirebaseFirestore.getInstance().collection(Constants.CARTS).document(
                                    document1.id
                                ).update(cartHashMap)
                                refresh()
                            }
                        }
                    }

                    productViewHolder.productDeleteButton.setOnClickListener {
                        firebaseFirestore.collection(Constants.CARTS).whereEqualTo(
                            Constants.USER_ID,
                            userId
                        ).get().addOnSuccessListener { it2 ->
                            for (document2 in it2) {
                                var cartPrice2 = document2.data[Constants.CART_PRICE] as Long
                                var cartProductNum = document2.data[Constants.CART_PRODUCT_NUM] as Long
                                val cartProducts = document2.data[Constants.CART_PRODUCTS] as MutableList<Long>
                                val cartProductAmount2 = document2.data[Constants.CART_PRODUCT_AMOUNT] as MutableList<Int>
                                cartProductNum -= cartProductAmount2[position].toLong()
                                cartPrice2 -= cartProductAmount2[position] * productPrice
                                cartProducts.removeAt(position)
                                cartProductAmount2.removeAt(position)

                                val cartHashMap = HashMap<String, Any>()
                                cartHashMap[Constants.CART_PRICE] = cartPrice2
                                cartHashMap[Constants.CART_PRODUCT_NUM] = cartProductNum.toInt()
                                cartHashMap[Constants.CART_PRODUCTS] = cartProducts
                                cartHashMap[Constants.CART_PRODUCT_AMOUNT] = cartProductAmount2
                                FirebaseFirestore.getInstance().collection(Constants.CARTS).document(
                                    document2.id
                                ).update(cartHashMap)
                                refresh()
                            }
                        }
                    }
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(
                R.layout.item_in_cart_product,
                parent,
                false
            )
            return ViewHolder(view)
        }

        override fun getItemCount() = product_list.size
    }

    @Suppress("DEPRECATION")
    fun refresh() {

        // Refresh tab data:
        fragmentManager?.beginTransaction()?.detach(this)?.attach(this)?.commit()
    }

    @Suppress("DEPRECATION")
    private fun payCart() {
        firebaseFirestore.collection(Constants.CARTS).whereEqualTo(Constants.USER_ID, userId).get().addOnSuccessListener {
            for (document in it) {
                val cartProNum = document.data[Constants.CART_PRODUCT_NUM] as Long
                val cartPrice = document.data[Constants.CART_PRICE] as Long
                val cartProducts = document.data[Constants.CART_PRODUCTS] as MutableList<Long>
                val cartProductAmount = document.data[Constants.CART_PRODUCT_AMOUNT] as MutableList<Int>

                var productBarcode = ""
                if (cartProducts.isEmpty()) {
                    Toast.makeText(activity, "Kh??ng c?? s???n ph???m ????? thanh to??n", Toast.LENGTH_LONG).show()
                    setUserVisibleHint(true)
                } else {
                    productBarcode += cartProducts[0].toString()
                    for (index in 1 until cartProducts.size) {
                        productBarcode += "," + cartProducts[index].toString()
                    }
                }

                var productNumPosition = ""
                if (cartProductAmount.isEmpty()) {
                    Toast.makeText(activity, "Kh??ng c?? s???n ph???m ????? thanh to??n", Toast.LENGTH_LONG).show()
                    setUserVisibleHint(true)
                } else {
                    productNumPosition += cartProductAmount[0].toString()
                    for (index in 1 until cartProductAmount.size) {
                        productNumPosition += "," + cartProductAmount[index].toString()
                    }
                }

                val intent = Intent(activity, PaymentActivity::class.java)
                intent.putExtra(Constants.CART_PRICE, cartPrice.toString())
                intent.putExtra(Constants.CART_PRODUCT_NUM, cartProNum.toString())
                startActivity(intent)
            }
        }
    }

    private fun generateQRCode(qr: ImageView) {
        val multiFormatWriter = MultiFormatWriter()

        try {
            val bitMatrix = multiFormatWriter.encode(
                userId,
                BarcodeFormat.QR_CODE,
                300,
                300
            )
            val barcodeEncoder = BarcodeEncoder()
            val bitmap = barcodeEncoder.createBitmap(bitMatrix)
            qr.setImageBitmap(bitmap)

        } catch (e: WriterException) {
            e.printStackTrace()
        }
    }

    override fun onClick(view: View?) {
        if (view != null) {
            when (view.id) {
                R.id.btn_cashout -> payCart()
            }
        }
    }

}
