package com.trading.thesis_trading_app.ui.Admin

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.trading.thesis_trading_app.R
import com.trading.thesis_trading_app.firestore.FirestoreClass
import com.trading.thesis_trading_app.ui.Authentication.LoginActivity
import com.trading.thesis_trading_app.ui.User.ReceiptActivity
import com.trading.thesis_trading_app.utils.Constants
import jp.wasabeef.recyclerview.animators.SlideInUpAnimator

class StoreOrderActivity : AppCompatActivity() {

    private val adminId = FirestoreClass().getCurrentUserId()
    private var adapter: StoreOrderAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_store_order)

        val productRecyclerView = findViewById<View>(R.id.store_order_recyclerView) as RecyclerView

        FirebaseFirestore.getInstance().collection(Constants.ADMIN).whereEqualTo(Constants.ADMIN_ID, adminId).get().addOnSuccessListener {
            for (document in it) {
                val storeId = document.data[Constants.ADMIN_STORE_ID] as Long

                FirebaseFirestore.getInstance().collection(Constants.STORE_ORDER).whereEqualTo(Constants.STORE_ID, storeId).get().addOnSuccessListener {  it1 ->
                    for (document1 in it1) {
                        val userIds = document1.data[Constants.USER_IDS] as MutableList<String>
                        val storeDates = document1.data[Constants.STORE_DATES] as MutableList<String>

                        adapter = StoreOrderAdapter(storeId, userIds, storeDates)
                        productRecyclerView.adapter = adapter
                        productRecyclerView.layoutManager = LinearLayoutManager(this)
                        productRecyclerView.setHasFixedSize(true)
                        productRecyclerView.itemAnimator = SlideInUpAnimator()
                    }
                }
            }
        }

//        @Suppress("DEPRECATION")
//        Handler().postDelayed(
//            {
//                val intent = Intent(this@StoreOrderActivity, StoreOrderActivity::class.java)
//                startActivity(intent)
//                finish()
//            },
//            60000
//        )
    }

    private inner class StoreOrderAdapter(private val storeId: Long, private val users: List<String>, private val receipt_dates: List<String>) : RecyclerView.Adapter<StoreOrderAdapter.ViewHolder>(){
        inner class ViewHolder(view: View) :RecyclerView.ViewHolder(view) {
            val receiptDate: TextView = view.findViewById(R.id.receipt_date)
        }

        override fun onBindViewHolder(storeViewHolder: StoreOrderAdapter.ViewHolder, position: Int) {

            FirebaseFirestore.getInstance().collection(Constants.USERS).whereEqualTo(Constants.USER_ID, users[position]).get().addOnSuccessListener {
                for (document in it) {
                    val firstName = document.data[Constants.USER_FIRST_NAME] as String
                    val lastName = document.data[Constants.USER_LAST_NAME] as String

                    storeViewHolder.receiptDate.text = "Đơn của khách hàng $firstName $lastName" + " lúc " + receipt_dates[position]
                    storeViewHolder.receiptDate.textSize = 10F
                    storeViewHolder.receiptDate.setOnClickListener {
                        val intent = Intent(this@StoreOrderActivity, StoreOrderProductsActivity::class.java)
                        intent.putExtra("Receipt Position", position)
                        intent.putExtra("Store ID", storeId)
                        startActivity(intent)
                    }
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoreOrderAdapter.ViewHolder {
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