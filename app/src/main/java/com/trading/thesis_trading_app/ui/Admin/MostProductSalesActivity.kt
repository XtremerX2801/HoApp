package com.trading.thesis_trading_app.ui.Admin

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.trading.thesis_trading_app.R
import com.trading.thesis_trading_app.utils.Constants
import com.google.firebase.firestore.FirebaseFirestore
import jp.wasabeef.recyclerview.animators.SlideInUpAnimator
import kotlinx.android.synthetic.main.activity_most_product_sales.*

class MostProductSalesActivity : AppCompatActivity(){

    private var adapter: PaymentProductsAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_most_product_sales)

        getProductsList()
    }

    private fun getProductsList(){
        FirebaseFirestore.getInstance().collection(Constants.SALES).whereEqualTo(Constants.SALEID, "1").get().addOnSuccessListener {
            for (document in it) {
                val salesProducts = document.data[Constants.SALES_PRODUCTS] as MutableList<Long>
                val salesProductAmount = document.data[Constants.SALES_PRODUCT_AMOUNT] as MutableList<Int>

                val indexList: MutableList<Int> = mutableListOf()
                for (i in 0 until salesProductAmount.size) {
                    indexList.add(i)
                }
                var best = 0
                var sub: Int

                for (a in 0 until indexList.size - 1) {
                    for (b in 0 until indexList.size - 1) {
                        best = if (salesProductAmount[b] < salesProductAmount[b + 1]) {
                            b + 1
                        } else {
                            b
                        }
                    }
                    if (best != a) {
                        sub = indexList[a]
                        indexList[a] = indexList[best]
                        indexList[best] = sub
                    }
                }

                val arrangedProduct: MutableList<Long> = mutableListOf()
                val arrangedAmount: MutableList<Int> = mutableListOf()
                for (proIndex in 0 until indexList.size) {
                    arrangedProduct.add(salesProducts[indexList[proIndex]])
                    arrangedAmount.add(salesProductAmount[indexList[proIndex]])
                }

                val productRecyclerView = findViewById<View>(R.id.most_product_sales_recyclerView) as RecyclerView
                adapter = PaymentProductsAdapter(arrangedProduct, arrangedAmount)
                productRecyclerView.adapter = adapter
                productRecyclerView.layoutManager = LinearLayoutManager(this)
                productRecyclerView.setHasFixedSize(true)
                productRecyclerView.itemAnimator = SlideInUpAnimator()
            }
        }
    }

    private inner class PaymentProductsAdapter(private val product_list: List<Long>, private val product_amount: List<Int>) : RecyclerView.Adapter<PaymentProductsAdapter.ViewHolder>(){
        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val productName: TextView = view.findViewById(R.id.most_item_name)
            val productAmount: TextView = view.findViewById(R.id.most_item_amount)
            val productTotalPrice: TextView = view.findViewById(R.id.most_item_total_price)
        }

        override fun onBindViewHolder(productViewHolder: PaymentProductsAdapter.ViewHolder, position: Int) {
            FirebaseFirestore.getInstance().collection(Constants.PRODUCTS).whereEqualTo(
                Constants.PRODUCT_BARCODE,
                product_list[position]
            ).get().addOnSuccessListener {
                for (document in it) {
                    val name = document.data[Constants.PRODUCT_NAME] as String
                    val price = document.data[Constants.PRODUCT_PRICE] as Long

                    productViewHolder.productName.text = "Sản phẩm: " + name
                    productViewHolder.productTotalPrice.text = "Tổng tiền: " + (price * product_amount[position]).toString() + "đ"
                    productViewHolder.productAmount.text = "Tổng số lượng: " + product_amount[position]
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PaymentProductsAdapter.ViewHolder {
            val view = LayoutInflater.from(this@MostProductSalesActivity).inflate(
                R.layout.item_most_product_dashboard,
                parent,
                false
            )
            return ViewHolder(view)
        }

        override fun getItemCount() = product_list.size
    }
}