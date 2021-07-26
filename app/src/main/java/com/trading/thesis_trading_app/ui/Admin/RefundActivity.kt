package com.trading.thesis_trading_app.ui.Admin

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.trading.thesis_trading_app.R
import com.trading.thesis_trading_app.utils.Constants
import com.google.firebase.firestore.FirebaseFirestore
import jp.wasabeef.recyclerview.animators.SlideInUpAnimator
import kotlinx.android.synthetic.main.activity_refund.*

class RefundActivity : AppCompatActivity() {

    private var adapter: CartProductsAdapter? = null
    private var userId = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_refund)

        if (intent.hasExtra(Constants.EXTRA_USER_DETAILS)) {
            userId = intent.getStringExtra(Constants.EXTRA_USER_DETAILS).toString()
        }

        val receiptRecyclerView = findViewById<View>(R.id.receipt_admin_fragment) as RecyclerView
        FirebaseFirestore.getInstance().collection(Constants.RECEIPTS).whereEqualTo(Constants.USER_ID, userId).get().addOnSuccessListener {
            for (document in it) {
                val receiptDates = document.data[Constants.RECEIPT_DATES] as MutableList<String>
                adapter = CartProductsAdapter(userId, receiptDates)
                receiptRecyclerView.adapter = adapter
                receiptRecyclerView.layoutManager = LinearLayoutManager(this@RefundActivity)
                receiptRecyclerView.setHasFixedSize(true)
                receiptRecyclerView.itemAnimator = SlideInUpAnimator()
            }
        }
    }

    @Suppress("DEPRECATION")
    private inner class CartProductsAdapter(private val userId: String, private val receipt_dates: List<String>) : RecyclerView.Adapter<CartProductsAdapter.ViewHolder>(){
        inner class ViewHolder(view: View) :RecyclerView.ViewHolder(view) {
            val receiptDate: TextView = view.findViewById(R.id.receipt_date)
        }

        override fun onBindViewHolder(receiptViewHolder: CartProductsAdapter.ViewHolder, position: Int) {
            val date = receipt_dates[position].split("-")

            receiptViewHolder.receiptDate.text = "Hóa đơn ngày " + date[1] + " lúc " + date[0]
            receiptViewHolder.receiptDate.setOnClickListener {
                val intent = Intent(this@RefundActivity, RefundReceiptActivity::class.java)
                intent.putExtra("Receipt Position", position)
                intent.putExtra(Constants.EXTRA_USER_DETAILS, userId)
                startActivity(intent)
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartProductsAdapter.ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(
                R.layout.item_receipt,
                parent,
                false
            )
            return ViewHolder(view)
        }

        override fun getItemCount() = receipt_dates.size
    }

}