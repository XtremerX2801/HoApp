package com.trading.thesis_trading_app.ui.Admin

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.trading.thesis_trading_app.R
import com.trading.thesis_trading_app.utils.Constants
import com.trading.thesis_trading_app.utils.GlideLoader
import jp.wasabeef.recyclerview.animators.SlideInUpAnimator
import kotlinx.android.synthetic.main.activity_online_products.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.HashMap
import kotlin.math.roundToLong

class OnlineProductsActivity : AppCompatActivity(), View.OnClickListener {

    private var pos = -1
    private var storeId: Long = -1
    private var adapter: StoreOrderAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_online_products)

        if (intent.hasExtra("Receipt Position")) {
            pos = intent.getIntExtra("Receipt Position", -1)
            storeId = intent.getLongExtra("Store ID", 0)
        }

        val productRecyclerView = findViewById<View>(R.id.online_products_recyclerView) as RecyclerView

        if (pos >= 0) {
            FirebaseFirestore.getInstance().collection(Constants.ONLINE_ORDER).whereEqualTo(Constants.STORE_ID, storeId).get().addOnSuccessListener {
                for (document in it) {
                    val amounts = document.data[Constants.AMOUNTS] as MutableList<String>
                    val productAmounts = amounts[pos].split(",")
                    val products = document.data[Constants.STORE_PRODUCTS] as MutableList<String>
                    val storeProducts = products[pos].split(",")
                    val date = document.data[Constants.STORE_DATES] as MutableList<String>
                    val users = document.data[Constants.USER_IDS] as MutableList<String>
                    val prices = document.data[Constants.PRICES] as MutableList<Long>

                    online_products_date.text = "Đơn hàng ngày: " + date[pos]
                    online_products_total_price.text = "Tổng tiền: " + prices[pos].toString()

                    adapter = StoreOrderAdapter(productAmounts, storeProducts)
                    productRecyclerView.adapter = adapter
                    productRecyclerView.layoutManager = LinearLayoutManager(this)
                    productRecyclerView.setHasFixedSize(true)
                    productRecyclerView.itemAnimator = SlideInUpAnimator()

                    FirebaseFirestore.getInstance().collection(Constants.USERS).whereEqualTo(
                        Constants.USER_ID, users[pos]).get().addOnSuccessListener { it1 ->
                        for (document1 in it1) {
                            val firstName = document1.data[Constants.USER_FIRST_NAME] as String
                            val lastName = document1.data[Constants.USER_LAST_NAME] as String

                            online_products_customer_name.text = "Khách hàng: $firstName $lastName"
                        }
                    }
                }
            }
        }

        online_product_purchase.setOnClickListener(this)
    }

    private inner class StoreOrderAdapter(private val amounts: List<String>, private val barcodes: List<String>) : RecyclerView.Adapter<StoreOrderAdapter.ViewHolder>(){
        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val productName: TextView = view.findViewById(R.id.store_order_product_name)
            val productAmount: TextView = view.findViewById(R.id.store_order_product_amount)
            val productPrice: TextView = view.findViewById(R.id.store_order_total_product_price)
            val productImage: ImageView = view.findViewById(R.id.store_order_product_image)
        }

        override fun onBindViewHolder(storeViewHolder: StoreOrderAdapter.ViewHolder, position: Int) {

            FirebaseFirestore.getInstance().collection(Constants.PRODUCTS).whereEqualTo(Constants.PRODUCT_BARCODE, barcodes[position].toLong()).get().addOnSuccessListener {
                for (document in it) {
                    val price = document.data[Constants.PRODUCT_PRICE] as Long
                    val name = document.data[Constants.PRODUCT_NAME] as String
                    val image = document.data[Constants.PRODUCT_IMAGE] as String

                    storeViewHolder.productName.text = name
                    storeViewHolder.productPrice.text = "Giá: " + price.toString()
                    storeViewHolder.productAmount.text = "Số lượng: " + amounts[position]

                    GlideLoader(this@OnlineProductsActivity).loadProductPicture(image, storeViewHolder.productImage)
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoreOrderAdapter.ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(
                R.layout.item_in_store_order,
                parent,
                false
            )
            return ViewHolder(view)
        }

        override fun getItemCount() = barcodes.size
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun payProducts(position: Int) {
        Log.e("Position", position.toString())
        FirebaseFirestore.getInstance().collection(Constants.SALES).whereEqualTo(Constants.SALEID, "1").get().addOnSuccessListener {
            for (document in it) {
                var salesPrice = document.data[Constants.SALES_PRICE] as Long
                var salesTotalProducts = document.data[Constants.SALES_TOTAL_PRODUCTS] as Long
                val salesProducts = document.data[Constants.SALES_PRODUCTS] as MutableList<Long>
                val salesProductAmount = document.data[Constants.SALES_PRODUCT_AMOUNT] as MutableList<Int>
                val salesProductCategoryAmount = document.data[Constants.SALES_PRODUCT_CATEGORY_AMOUNT] as MutableList<Int>
                val salesTime = document.data[Constants.SALES_TIME] as MutableList<String>
                val salesTimeAmount = document.data[Constants.SALES_TIME_AMOUNT] as MutableList<Long>

                FirebaseFirestore.getInstance().collection(Constants.ONLINE_ORDER).whereEqualTo(Constants.STORE_ID, storeId).get().addOnSuccessListener { it2 ->
                    for (document2 in it2) {
                        val amounts = document2.data[Constants.AMOUNTS] as MutableList<String>
                        val prices = document2.data[Constants.PRICES] as MutableList<Long>
                        val storeDates =
                            document2.data[Constants.STORE_DATES] as MutableList<String>
                        val storeProducts =
                            document2.data[Constants.STORE_PRODUCTS] as MutableList<String>
                        val userIds = document2.data[Constants.USER_IDS] as MutableList<String>

                        val thedates = storeDates[position]
                        val theid = userIds[position]
                        val theamount = amounts[position]

                        salesPrice += prices[position]
                        val num = theamount.split(",")
                        for (element in num) {
                            salesTotalProducts += element.toLong()
                        }

                        // Update same product
                        var same = 0
                        val products = storeProducts[position].split(",")

                        val saleHashMap = HashMap<String, Any>()
                        val adjustProductList: MutableList<Long> = salesProducts
                        val adjustProductAmountList: MutableList<Int> = salesProductAmount
                        val newProductList: MutableList<Long> = mutableListOf()
                        val newProductAmountList: MutableList<Int> = mutableListOf()

                        for (productIndex in products.indices) {
                            for (saleIndex in 0 until salesProducts.size) {
                                if (products[productIndex].toLong() == salesProducts[saleIndex]) {
                                    same = 1
                                    adjustProductAmountList[saleIndex] += num[productIndex].toInt()
                                }
                            }
                            if (same == 0) {
                                newProductList.add(products[productIndex].toLong())
                                newProductAmountList.add(num[productIndex].toInt())
                            }

                            FirebaseFirestore.getInstance().collection(Constants.PRODUCTS)
                                .whereEqualTo(
                                    Constants.PRODUCT_BARCODE,
                                    products[productIndex].toLong()
                                ).get().addOnSuccessListener { it3 ->
                                    for (document3 in it3) {
                                        val amount =
                                            document3.data[Constants.PRODUCT_AMOUNT] as Long
                                        val productCategory =
                                            document3.data[Constants.PRODUCT_CATEGORY] as String
                                        val productHashMap = HashMap<String, Any>()

                                        productHashMap[Constants.PRODUCT_AMOUNT] =
                                            amount.toInt() - num[productIndex].toInt()
                                        FirebaseFirestore.getInstance()
                                            .collection(Constants.PRODUCTS)
                                            .document(document3.id).update(productHashMap)
                                            .addOnSuccessListener {
                                                Log.e("Product update: ", "Successful")
                                            }.addOnFailureListener {
                                                Log.e("Product update: ", "Failed")
                                            }

                                        when (productCategory) {
                                            "ĐỒ UỐNG CÁC LOẠI" -> salesProductCategoryAmount[0] += num[productIndex].toInt()
                                            "Nước uống các loại" -> salesProductCategoryAmount[0] += num[productIndex].toInt()
                                            "SỮA UỐNG CÁC LOẠI" -> salesProductCategoryAmount[1] += num[productIndex].toInt()
                                            "BÁNH KẸO CÁC LOẠI" -> salesProductCategoryAmount[2] += num[productIndex].toInt()
                                            "MÌ, CHÁO, PHỞ, BÚN" -> salesProductCategoryAmount[3] += num[productIndex].toInt()
                                            "DẦU ĂN, GIA VỊ" -> salesProductCategoryAmount[4] += num[productIndex].toInt()
                                            "GẠO, BỘT, ĐỒ KHÔ" -> salesProductCategoryAmount[5] += num[productIndex].toInt()
                                            "ĐỒ MÁT, ĐÔNG LẠNH" -> salesProductCategoryAmount[6] += num[productIndex].toInt()
                                            "TẢ, ĐỒ CHO BÉ" -> salesProductCategoryAmount[7] += num[productIndex].toInt()
                                            "CHĂM SÓC CÁ NHÂN" -> salesProductCategoryAmount[8] += num[productIndex].toInt()
                                            "VỆ SINH NHÀ CỬA" -> salesProductCategoryAmount[9] += num[productIndex].toInt()
                                            "ĐỒ DÙNG GIA ĐÌNH" -> salesProductCategoryAmount[10] += num[productIndex].toInt()
                                            "VĂN PHÒNG PHẨM" -> salesProductCategoryAmount[11] += num[productIndex].toInt()
                                        }

                                        saleHashMap[Constants.SALES_PRODUCT_CATEGORY_AMOUNT] =
                                            salesProductCategoryAmount
                                        FirebaseFirestore.getInstance().collection(Constants.SALES)
                                            .document(document.id).update(saleHashMap)
                                            .addOnSuccessListener {
                                                Log.e("Sales update: ", "Successful")
                                            }.addOnFailureListener {
                                                Log.e("Sales update: ", "Failed")
                                            }
                                    }
                                }
                        }

                        if (newProductList.isNotEmpty()) {
                            for (i in newProductList.indices) {
                                adjustProductList.add(newProductList[i])
                                adjustProductAmountList.add(newProductAmountList[i])
                            }
                            saleHashMap[Constants.SALES_PRODUCTS] = adjustProductList
                            saleHashMap[Constants.SALES_PRODUCT_AMOUNT] =
                                adjustProductAmountList
                            FirebaseFirestore.getInstance().collection(Constants.SALES)
                                .document(document.id).update(saleHashMap)
                                .addOnSuccessListener {
                                    Log.e("Sales update: ", "Successful")
                                }.addOnFailureListener {
                                    Log.e("Sales update: ", "Failed")
                                }
                        }

                        var count = -1
                        val dates = thedates.split("/")
                        val updateTime = dates[1] + "/" + dates[2]

                        if (salesTime.isEmpty()) {
                            var theNewAmount: Long = 0
                            for (element in num) {
                                theNewAmount += element.toLong()
                            }
                            salesTime.add(updateTime)
                            salesTimeAmount.add(theNewAmount)
                        } else {
                            for (ind in 0 until salesTime.size) {
                                if (updateTime == salesTime[ind]) {
                                    count = 0
                                    var theNewAmount: Long = 0
                                    for (element in num) {
                                        theNewAmount += element.toLong()
                                    }
                                    salesTimeAmount[ind] += theNewAmount
                                }
                            }
                            if (count == -1) {
                                salesTime.add(updateTime)
                                var theNewAmount: Long = 0
                                for (element in num) {
                                    theNewAmount += element.toLong()
                                }
                                salesTimeAmount.add(theNewAmount)
                            }
                        }

                        saleHashMap[Constants.SALES_PRICE] = salesPrice
                        saleHashMap[Constants.SALES_TOTAL_PRODUCTS] =
                            salesTotalProducts
                        saleHashMap[Constants.SALES_TIME] = salesTime
                        saleHashMap[Constants.SALES_TIME_AMOUNT] = salesTimeAmount
                        FirebaseFirestore.getInstance().collection(Constants.SALES)
                            .document(document.id).update(saleHashMap)
                            .addOnSuccessListener {
                                Log.e("Sales update: ", "Successful")
                            }.addOnFailureListener {
                                Log.e("Sales update: ", "Failed")
                            }

                        val newReceiptProducts = storeProducts[position]
                        val newReceiptProductAmount = amounts[position]
                        val newPrice = prices[position]

                        FirebaseFirestore.getInstance().collection(Constants.RECEIPTS)
                            .whereEqualTo(Constants.USER_ID, userIds[position]).get()
                            .addOnSuccessListener { it4 ->
                                for (document4 in it4) {
                                    val receiptProducts =
                                        document4.data[Constants.RECEIPT_PRODUCTS] as MutableList<String>
                                    val receiptProductAmount =
                                        document4.data[Constants.RECEIPT_PRODUCT_AMOUNT] as MutableList<String>
                                    val receiptPrices =
                                        document4.data[Constants.RECEIPT_PRICES] as MutableList<Long>
                                    val receiptTotalProducts =
                                        document4.data[Constants.RECEIPT_TOTAL_PRODUCTS] as MutableList<Long>
                                    val receiptDates =
                                        document4.data[Constants.RECEIPT_DATES] as MutableList<String>
                                    val receiptHashMap = HashMap<String, Any>()

                                    receiptProducts.add(newReceiptProducts)
                                    receiptProductAmount.add(newReceiptProductAmount)
                                    receiptPrices.add(newPrice)
                                    var total: Long = 0
                                    for (element in num) {
                                        total += element.toLong()
                                    }
                                    receiptTotalProducts.add(total)
                                    receiptHashMap[Constants.RECEIPT_PRODUCTS] = receiptProducts
                                    receiptHashMap[Constants.RECEIPT_PRODUCT_AMOUNT] =
                                        receiptProductAmount
                                    receiptHashMap[Constants.RECEIPT_PRICES] = receiptPrices
                                    receiptHashMap[Constants.RECEIPT_TOTAL_PRODUCTS] =
                                        receiptTotalProducts

                                    val current = LocalDateTime.now()
                                    val formatter = DateTimeFormatter.BASIC_ISO_DATE
                                    val formatter2 = DateTimeFormatter.ISO_TIME
                                    val formatted = current.format(formatter)
                                    val formatted2 = current.format(formatter2)
                                    val day = formatted.substring(6, 8)
                                    val month = formatted.substring(4, 6)
                                    val year = formatted.substring(0, 4)
                                    val time = "$formatted2-$day/$month/$year"
                                    receiptDates.add(time)
                                    receiptHashMap[Constants.RECEIPT_DATES] = receiptDates

                                    FirebaseFirestore.getInstance().collection(Constants.RECEIPTS)
                                        .document(document4.id).update(receiptHashMap)
                                        .addOnSuccessListener {
                                            Log.e("Receipt update: ", "Successful")
                                        }
                                        .addOnFailureListener {
                                            Log.e("Receipt update: ", "Failed")
                                        }
                                }
                            }

                        FirebaseFirestore.getInstance().collection(Constants.USERS)
                            .whereEqualTo(Constants.USER_ID, theid).get()
                            .addOnSuccessListener { it5 ->
                                for (document5 in it5) {
                                    val userHashMap = HashMap<String, Any>()
                                    var userCoupon = document5.data[Constants.USER_COUPON] as Long
                                    userCoupon += (newPrice * 0.05).roundToLong()
                                    userHashMap[Constants.USER_COUPON] = userCoupon
                                    FirebaseFirestore.getInstance().collection(Constants.USERS)
                                        .document(document5.id).update(userHashMap)
                                        .addOnSuccessListener {
                                            Log.e("User coupon update: ", "Successful")
                                        }
                                        .addOnFailureListener {
                                            Log.e("User coupon update: ", "Failed")
                                        }
                                }
                            }

                        FirebaseFirestore.getInstance().collection(Constants.TOTAL_POINT)
                            .document("5Tw5en3twZNtk7tk5bKD").get().addOnSuccessListener { it6 ->
                                val point =
                                    it6.data?.get(Constants.POINTS_LIST) as MutableList<Long>
                                val users =
                                    it6.data?.get(Constants.USERS_LIST) as MutableList<String>

                                for (index in 0 until users.size) {
                                    if (theid == users[index]) {
                                        point[index] += (newPrice * 0.05).roundToLong()
                                    }
                                }
                                val pointHashMap = HashMap<String, Any>()
                                pointHashMap[Constants.POINTS_LIST] = point
                                FirebaseFirestore.getInstance().collection(Constants.TOTAL_POINT)
                                    .document("5Tw5en3twZNtk7tk5bKD").update(pointHashMap)
                                    .addOnSuccessListener {
                                        Log.e("Update point", "Success")
                                    }.addOnFailureListener {
                                        Log.e("Update point", "Failed")
                                    }
                            }

                        amounts.removeAt(position)
                        prices.removeAt(position)
                        storeDates.removeAt(position)
                        storeProducts.removeAt(position)
                        userIds.removeAt(position)

                        val onlineHashMap = HashMap<String, Any>()
                        onlineHashMap[Constants.AMOUNTS] = amounts
                        onlineHashMap[Constants.PRICES] = prices
                        onlineHashMap[Constants.STORE_DATES] = storeDates
                        onlineHashMap[Constants.STORE_PRODUCTS] = storeProducts
                        onlineHashMap[Constants.USER_IDS] = userIds

                        FirebaseFirestore.getInstance().collection(Constants.ONLINE_ORDER)
                            .document(document2.id).update(onlineHashMap).addOnSuccessListener {
                                Log.e("Update Online Order", "Success")
                            }.addOnFailureListener {
                                Log.e("Update Online Order", "Failed")
                            }
                    }
                }
            }
        }

        Toast.makeText(this@OnlineProductsActivity, "Sản phẩm đã được thanh toán", Toast.LENGTH_SHORT).show()
        val intent = Intent(this, AdminActivity::class.java)
        startActivity(intent)
        finish()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onClick(view: View?) {
        if (view != null) {
            when (view.id) {
                R.id.online_product_purchase -> {
                    payProducts(pos)
                }
            }
        }
    }
}