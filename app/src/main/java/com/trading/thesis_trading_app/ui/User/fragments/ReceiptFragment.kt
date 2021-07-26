package com.trading.thesis_trading_app.ui.User.fragments

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.trading.thesis_trading_app.R
import com.trading.thesis_trading_app.firestore.FirestoreClass
import com.trading.thesis_trading_app.ui.User.*
import com.trading.thesis_trading_app.utils.Constants
import com.google.firebase.firestore.FirebaseFirestore
import jp.wasabeef.recyclerview.animators.SlideInUpAnimator

class ReceiptFragment : Fragment() {

    private var adapter: CartProductsAdapter? = null
    private val userId = FirestoreClass().getCurrentUserId()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val root = inflater.inflate(R.layout.fragment_receipt, container, false)
        val receiptRecyclerView = root.findViewById<View>(R.id.receipt_fragment) as RecyclerView
        FirebaseFirestore.getInstance().collection(Constants.RECEIPTS).whereEqualTo(Constants.USER_ID, userId).get().addOnSuccessListener {
            for (document in it) {
                val receiptDates = document.data[Constants.RECEIPT_DATES] as MutableList<String>
                val list = receiptDates as List<String>
                adapter = CartProductsAdapter(list.asReversed())
                receiptRecyclerView.adapter = adapter
                receiptRecyclerView.layoutManager = LinearLayoutManager(activity)
                receiptRecyclerView.setHasFixedSize(true)
                receiptRecyclerView.itemAnimator = SlideInUpAnimator()
            }
        }

        return root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.receipt_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.action_setting -> {
                startActivity(Intent(activity, SettingActivity::class.java))
                return true
            }
            R.id.action_ranking -> {
                startActivity(Intent(activity, RankingActivity::class.java))
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    private inner class CartProductsAdapter(private val receipt_dates: List<String>) : RecyclerView.Adapter<CartProductsAdapter.ViewHolder>(){
        inner class ViewHolder(view: View) :RecyclerView.ViewHolder(view) {
            val receiptDate: TextView = view.findViewById(R.id.receipt_date)
        }

        override fun onBindViewHolder(receiptViewHolder: CartProductsAdapter.ViewHolder, position: Int) {
            val date = receipt_dates[position].split("-")

            receiptViewHolder.receiptDate.text = "Hóa đơn ngày " + date[1] + " lúc " + date[0]
            receiptViewHolder.receiptDate.setOnClickListener {
                val intent = Intent(activity, ReceiptActivity::class.java)
                intent.putExtra("Receipt Position", receipt_dates.size -1 - position)
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