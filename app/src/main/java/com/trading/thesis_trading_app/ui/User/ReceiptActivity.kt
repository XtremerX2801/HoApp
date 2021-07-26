package com.trading.thesis_trading_app.ui.User

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.trading.thesis_trading_app.R
import com.trading.thesis_trading_app.firestore.FirestoreClass
import com.trading.thesis_trading_app.utils.Constants
import com.google.firebase.firestore.FirebaseFirestore
import com.trading.thesis_trading_app.utils.GlideLoader
import jp.wasabeef.recyclerview.animators.SlideInUpAnimator
import kotlinx.android.synthetic.main.activity_receipt.*

class ReceiptActivity : AppCompatActivity() {

    private val userId = FirestoreClass().getCurrentUserId()
    private var adapter: ReceiptProductsAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_receipt)

        if (intent.hasExtra("Receipt Position")) {
            getReceiptInfo(intent.getIntExtra("Receipt Position", 0))
        }
    }

    private fun getReceiptInfo(pos: Int) {
        FirebaseFirestore.getInstance().collection(Constants.RECEIPTS).whereEqualTo(Constants.USER_ID, userId).get().addOnSuccessListener { it1 ->
            for (document1 in it1) {
                val receiptDates = document1.data[Constants.RECEIPT_DATES] as MutableList<String>
                val receiptPrice = document1.data[Constants.RECEIPT_PRICES] as MutableList<Long>
                val receiptTotalProducts = document1.data[Constants.RECEIPT_TOTAL_PRODUCTS] as MutableList<Int>
                val receiptProducts = document1.data[Constants.RECEIPT_PRODUCTS] as MutableList<String>
                val receiptProductAmount = document1.data[Constants.RECEIPT_PRODUCT_AMOUNT] as MutableList<String>

                val date = receiptDates[pos].split("-")
                tv_receipt_title.text = "HÓA ĐƠN NGÀY " + date[1] + " LÚC " + date[0]
                receipt_total_products.text = "Tổng số sản phẩm: " + receiptTotalProducts[pos].toString()
                receipt_price.text = "Tổng số tiền: " + receiptPrice[pos].toString()

                val products = receiptProducts[pos].split(",")
                val productAmount = receiptProductAmount[pos].split(",")
                val receiptRecyclerView = findViewById<View>(R.id.receipt_recyclerView) as RecyclerView

                adapter = ReceiptProductsAdapter(products, productAmount)
                receiptRecyclerView.adapter = adapter
                receiptRecyclerView.layoutManager = LinearLayoutManager(this)
                receiptRecyclerView.setHasFixedSize(true)
                receiptRecyclerView.itemAnimator = SlideInUpAnimator()
            }
        }
    }

    private inner class ReceiptProductsAdapter(private val product_list: List<String>, private val product_amount: List<String>) : RecyclerView.Adapter<ReceiptProductsAdapter.ViewHolder>(){
        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val productName: TextView = view.findViewById(R.id.payment_product_name)
            val productAmount: TextView = view.findViewById(R.id.payment_product_amount)
            val productPrice: TextView = view.findViewById(R.id.payment_product_price)
            val productTotalPrice: TextView = view.findViewById(R.id.payment_total_product_price)
            val productImage: ImageView = view.findViewById(R.id.payment_product_image)
        }

        override fun onBindViewHolder(productViewHolder: ViewHolder, position: Int) {
            FirebaseFirestore.getInstance().collection(Constants.PRODUCTS).whereEqualTo(
                Constants.PRODUCT_BARCODE,
                product_list[position].toLong()
            ).get().addOnSuccessListener {
                for (document in it) {
                    val name = document.data[Constants.PRODUCT_NAME] as String
                    val price = document.data[Constants.PRODUCT_PRICE] as Long
                    val image = document.data[Constants.PRODUCT_IMAGE] as String

                    productViewHolder.productName.text = "Sản phẩm: " + name
                    productViewHolder.productPrice.text = "Giá: " + price.toString() + "đ"
                    productViewHolder.productTotalPrice.text = "Tổng: " + (price * product_amount[position].toInt()).toString() + "đ"
                    productViewHolder.productAmount.text = "Số lượng: " + product_amount[position]
                    GlideLoader(this@ReceiptActivity).loadProductPicture(image, productViewHolder.productImage)

                    productViewHolder.productImage.setOnClickListener{
                        val intent = Intent(this@ReceiptActivity, ProductInfoActivity::class.java)
                        intent.putExtra(Constants.PRODUCT_BARCODE, product_list[position].toLong())
                        startActivity(intent)
                    }
                    productViewHolder.productName.setOnClickListener{
                        val intent = Intent(this@ReceiptActivity, ProductInfoActivity::class.java)
                        intent.putExtra(Constants.PRODUCT_BARCODE, product_list[position].toLong())
                        startActivity(intent)
                    }
                    productViewHolder.productPrice.setOnClickListener{
                        val intent = Intent(this@ReceiptActivity, ProductInfoActivity::class.java)
                        intent.putExtra(Constants.PRODUCT_BARCODE, product_list[position].toLong())
                        startActivity(intent)
                    }
                    productViewHolder.productTotalPrice.setOnClickListener{
                        val intent = Intent(this@ReceiptActivity, ProductInfoActivity::class.java)
                        intent.putExtra(Constants.PRODUCT_BARCODE, product_list[position].toLong())
                        startActivity(intent)
                    }
                    productViewHolder.productAmount.setOnClickListener{
                        val intent = Intent(this@ReceiptActivity, ProductInfoActivity::class.java)
                        intent.putExtra(Constants.PRODUCT_BARCODE, product_list[position].toLong())
                        startActivity(intent)
                    }
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(this@ReceiptActivity).inflate(
                R.layout.item_in_payment,
                parent,
                false
            )
            return ViewHolder(view)
        }

        override fun getItemCount() = product_list.size
    }

}