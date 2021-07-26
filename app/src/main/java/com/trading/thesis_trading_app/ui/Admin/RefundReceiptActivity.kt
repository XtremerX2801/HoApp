package com.trading.thesis_trading_app.ui.Admin

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.trading.thesis_trading_app.R
import com.trading.thesis_trading_app.utils.Constants
import com.google.firebase.firestore.FirebaseFirestore
import jp.wasabeef.recyclerview.animators.SlideInUpAnimator
import kotlinx.android.synthetic.main.activity_refund_receipt.*

class RefundReceiptActivity : AppCompatActivity(), View.OnClickListener {

    private var userId = ""
    private var adapter: ReceiptProductsAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_refund_receipt)

        if (intent.hasExtra("Receipt Position")) {
            userId = intent.getStringExtra(Constants.EXTRA_USER_DETAILS).toString()
            getReceiptInfo(userId, intent.getIntExtra("Receipt Position", 0))
        }

        receipt_refund.setOnClickListener(this@RefundReceiptActivity)
    }

    private fun getReceiptInfo(userID: String, pos: Int) {
        FirebaseFirestore.getInstance().collection(Constants.RECEIPTS).whereEqualTo(Constants.USER_ID, userID).get().addOnSuccessListener { it1 ->
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
        }

        override fun onBindViewHolder(productViewHolder: ViewHolder, position: Int) {
            FirebaseFirestore.getInstance().collection(Constants.PRODUCTS).whereEqualTo(
                Constants.PRODUCT_BARCODE,
                product_list[position].toLong()
            ).get().addOnSuccessListener {
                for (document in it) {
                    val name = document.data[Constants.PRODUCT_NAME] as String
                    val price = document.data[Constants.PRODUCT_PRICE] as Long

                    productViewHolder.productName.text = "Sản phẩm: " + name
                    productViewHolder.productPrice.text = price.toString() + "đ"
                    productViewHolder.productTotalPrice.text = (price * product_amount[position].toInt()).toString() + "đ"
                    productViewHolder.productAmount.text = "Số lượng: " + product_amount[position]
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(this@RefundReceiptActivity).inflate(
                R.layout.item_in_payment,
                parent,
                false
            )
            return ViewHolder(view)
        }

        override fun getItemCount() = product_list.size
    }

    private fun refundReceipt(userID: String, pos: Int) {
        FirebaseFirestore.getInstance().collection(Constants.RECEIPTS).whereEqualTo(Constants.USER_ID, userID).get().addOnSuccessListener {
            for (document in it) {
                val receiptDates = document.data[Constants.RECEIPT_DATES] as MutableList<String>
                val receiptPrice = document.data[Constants.RECEIPT_PRICES] as MutableList<Long>
                val receiptTotalProducts = document.data[Constants.RECEIPT_TOTAL_PRODUCTS] as MutableList<Int>
                val receiptProducts = document.data[Constants.RECEIPT_PRODUCTS] as MutableList<String>
                val receiptProductAmount = document.data[Constants.RECEIPT_PRODUCT_AMOUNT] as MutableList<String>

                receiptDates.removeAt(pos)
                receiptPrice.removeAt(pos)
                receiptTotalProducts.removeAt(pos)
                receiptProducts.removeAt(pos)
                receiptProductAmount.removeAt(pos)

                val receiptHashMap = HashMap<String, Any>()
                receiptHashMap[Constants.RECEIPT_DATES] = receiptDates
                receiptHashMap[Constants.RECEIPT_PRICES] = receiptPrice
                receiptHashMap[Constants.RECEIPT_TOTAL_PRODUCTS] = receiptTotalProducts
                receiptHashMap[Constants.RECEIPT_PRODUCTS] = receiptProducts
                receiptHashMap[Constants.RECEIPT_PRODUCT_AMOUNT] = receiptProductAmount

                FirebaseFirestore.getInstance().collection(Constants.RECEIPTS).document(document.id).update(receiptHashMap).addOnSuccessListener {
                    Toast.makeText(this, "Refund Success", Toast.LENGTH_LONG).show()
                    val intent = Intent(this, RefundActivity::class.java)
                    intent.putExtra(Constants.EXTRA_USER_DETAILS, userId)
                    startActivity(intent)
                    finish()

                }.addOnFailureListener {
                    Toast.makeText(this, "Refund Failed", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onClick(view: View?) {
        if (view != null) {
            when (view.id) {
                R.id.receipt_refund -> {
                    if (intent.hasExtra("Receipt Position")) {
                        userId = intent.getStringExtra(Constants.EXTRA_USER_DETAILS).toString()
                        refundReceipt(userId, intent.getIntExtra("Receipt Position", 0))
                    }
                }
            }
        }
    }

}