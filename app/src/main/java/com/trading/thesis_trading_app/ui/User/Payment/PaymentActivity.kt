package com.trading.thesis_trading_app.ui.User.Payment

import android.content.Intent
import android.content.pm.ApplicationInfo
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.TextView
import androidx.annotation.RequiresApi
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.trading.thesis_trading_app.R
import com.trading.thesis_trading_app.firestore.FirestoreClass
import com.trading.thesis_trading_app.ui.Authentication.BaseActivity
import com.trading.thesis_trading_app.utils.Constants
import com.google.firebase.firestore.FirebaseFirestore
import com.trading.thesis_trading_app.ui.User.UserActivity
import com.trading.thesis_trading_app.ui.User.fragments.NoticeCustomDialogFragment
import jp.wasabeef.recyclerview.animators.SlideInUpAnimator
import kotlinx.android.synthetic.main.activity_payment.*
import kotlinx.android.synthetic.main.fragment_orders.view.*
import vn.momo.momo_partner.AppMoMoLib
import vn.momo.momo_partner.MoMoParameterNamePayment
import java.util.*
import kotlin.math.roundToLong

class PaymentActivity : BaseActivity(), View.OnClickListener {

    private val fee = "0"

    private val merchantName = "HoApp"
    private val merchantCode = "MOMO3FHA20210521"
    private val merchantNameLabel = "Shopping"

    private var adapter: PaymentProductsAdapter? = null
    private val userId = FirestoreClass().getCurrentUserId()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment)

        val productRecyclerView = findViewById<View>(R.id.product_ordered_payment) as RecyclerView

        AppMoMoLib.getInstance().setEnvironment(AppMoMoLib.ENVIRONMENT.DEVELOPMENT)
//        tvEnvironment.text = "Development Production"

        FirebaseFirestore.getInstance().collection(Constants.CARTS).whereEqualTo(Constants.USER_ID, userId).get().addOnSuccessListener {
            for (document in it) {
                val cartProNum = document.data[Constants.CART_PRODUCT_NUM] as Long
                val cartPrice = document.data[Constants.CART_PRICE] as Long
                val cartProducts = document.data[Constants.CART_PRODUCTS] as MutableList<Long>
                val cartProductAmount = document.data[Constants.CART_PRODUCT_AMOUNT] as MutableList<Int>

                tvTotalPrice.text = "Tổng số tiền: $cartPrice" + "đ"
                tvTotalProduct.text = "Tổng số sản phẩm: $cartProNum món"

                adapter = PaymentProductsAdapter(cartProducts, cartProductAmount)
                productRecyclerView.adapter = adapter
                productRecyclerView.layoutManager = LinearLayoutManager(this)
                productRecyclerView.setHasFixedSize(true)
                productRecyclerView.itemAnimator = SlideInUpAnimator()
            }
        }

        val applicationInfo: ApplicationInfo = applicationContext.applicationInfo
        val stringId = applicationInfo.labelRes
        val appName = if (stringId == 0) applicationInfo.nonLocalizedLabel.toString() else applicationContext.getString(stringId)


        btnPayMoMo.setOnClickListener(this)
        btnPayRaw.setOnClickListener(this)
        btnPayOnline.setOnClickListener(this)
    }

    private inner class PaymentProductsAdapter(private val product_list: List<Long>, private val product_amount: List<Int>) : RecyclerView.Adapter<PaymentProductsAdapter.ViewHolder>(){
        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val productName: TextView = view.findViewById(R.id.payment_product_name)
            val productAmount: TextView = view.findViewById(R.id.payment_product_amount)
            val productPrice: TextView = view.findViewById(R.id.payment_product_price)
            val productTotalPrice: TextView = view.findViewById(R.id.payment_total_product_price)
        }

        override fun onBindViewHolder(productViewHolder: ViewHolder, position: Int) {
            FirebaseFirestore.getInstance().collection(Constants.PRODUCTS).whereEqualTo(
                Constants.PRODUCT_BARCODE,
                product_list[position]
            ).get().addOnSuccessListener {
                for (document in it) {
                    val name = document.data[Constants.PRODUCT_NAME] as String
                    val price = document.data[Constants.PRODUCT_PRICE] as Long

                    productViewHolder.productName.text = "Sản phẩm: " + name
                    productViewHolder.productPrice.text = price.toString() + "đ"
                    productViewHolder.productTotalPrice.text = (price * product_amount[position]).toString() + "đ"
                    productViewHolder.productAmount.text = "Số lượng: " + product_amount[position].toString()
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(this@PaymentActivity).inflate(
                R.layout.item_in_payment,
                parent,
                false
            )
            return ViewHolder(view)
        }

        override fun getItemCount() = product_list.size
    }

    private fun requestPayment() {
        AppMoMoLib.getInstance().setAction(AppMoMoLib.ACTION.PAYMENT)
        AppMoMoLib.getInstance().setActionType(AppMoMoLib.ACTION_TYPE.GET_TOKEN)
        val eventValue: MutableMap<String, Any> = HashMap()
        //client Required
        eventValue[MoMoParameterNamePayment.MERCHANT_NAME] = merchantName
        eventValue[MoMoParameterNamePayment.MERCHANT_CODE] = merchantCode
//        eventValue[MoMoParameterNamePayment.AMOUNT] = intent.getStringExtra(Constants.CART_PRICE)!!
        eventValue[MoMoParameterNamePayment.AMOUNT] = "10000"
        eventValue[MoMoParameterNamePayment.DESCRIPTION] = "Tổng số lượng sản phẩm: " + intent.getStringExtra(Constants.CART_PRODUCT_NUM)!!
        //client Optional
        eventValue[MoMoParameterNamePayment.FEE] = fee
        eventValue[MoMoParameterNamePayment.MERCHANT_NAME_LABEL] = merchantNameLabel
        eventValue[MoMoParameterNamePayment.REQUEST_ID] = merchantCode + "-" + UUID.randomUUID().toString()
        eventValue[MoMoParameterNamePayment.PARTNER_CODE] = merchantCode
//        val objExtraData = JSONObject()
//        try {
//            objExtraData.put("product_code", "008")
//            objExtraData.put("product_name", "Nước ngọt Pepsi")
//            objExtraData.put("product_category", "ĐỒ UỐNG CÁC LOẠI")
//            objExtraData.put("product_price", "9000")
//        } catch (e: JSONException) {
//            e.printStackTrace()
//        }
//        eventValue[MoMoParameterNamePayment.EXTRA_DATA] = objExtraData.toString()
        eventValue[MoMoParameterNamePayment.REQUEST_TYPE] = "payment"
        eventValue[MoMoParameterNamePayment.LANGUAGE] = "vi"
        eventValue[MoMoParameterNamePayment.EXTRA] = ""
        //Request momo app
        Log.e("Momo Payment Info", eventValue.toString())
        AppMoMoLib.getInstance().requestMoMoCallBack(this, eventValue)
        Log.e("is it OK?", "Yes?")
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.e("Data", "?")
        Log.e("data", data.toString())
        if (requestCode == AppMoMoLib.getInstance().REQUEST_CODE_MOMO && resultCode == -1) {
            if (data != null) {
                if (data.getIntExtra("status", -1) == 0) {
                    tvMessage.text = "message: " + "Get token " + data.getStringExtra("message")

                    FirebaseFirestore.getInstance().collection(Constants.SALES).whereEqualTo(Constants.SALEID, "1").get().addOnSuccessListener { it1 ->
                        for (document1 in it1) {
                            var salesPrice = document1.data[Constants.SALES_PRICE] as Long
                            var salesTotalProducts = document1.data[Constants.SALES_TOTAL_PRODUCTS] as Long
                            val salesProducts = document1.data[Constants.SALES_PRODUCTS] as MutableList<Long>
                            val salesProductAmount = document1.data[Constants.SALES_PRODUCT_AMOUNT] as MutableList<Int>
                            val salesProductCategoryAmount = document1.data[Constants.SALES_PRODUCT_CATEGORY_AMOUNT] as MutableList<Int>
                            val salesTime = document1.data[Constants.SALES_TIME] as MutableList<String>
                            val salesTimeAmount = document1.data[Constants.SALES_TIME_AMOUNT] as MutableList<Long>

                            FirebaseFirestore.getInstance().collection(Constants.CARTS).whereEqualTo(Constants.USER_ID, userId).get().addOnSuccessListener { it2 ->
                                for (document2 in it2) {
                                    val cartProNum = document2.data[Constants.CART_PRODUCT_NUM] as Long
                                    val cartPrice = document2.data[Constants.CART_PRICE] as Long
                                    val cartProducts = document2.data[Constants.CART_PRODUCTS] as MutableList<Long>
                                    val cartProductAmount = document2.data[Constants.CART_PRODUCT_AMOUNT] as MutableList<Int>

                                    salesPrice += cartPrice
                                    salesTotalProducts += cartProNum

                                    // Update same product
                                    var same = 0
                                    for (productIndex in 0 until cartProducts.size) {
                                        for (saleIndex in 0 until salesProducts.size) {
                                            if (cartProducts[productIndex] == salesProducts[saleIndex]) {
                                                same = 1
                                                salesProductAmount[saleIndex] += cartProductAmount[productIndex]
                                            }
                                        }
                                        if (same == 0) {
                                            salesProducts.add(cartProducts[productIndex])
                                            salesProductAmount.add(cartProductAmount[productIndex])
                                        }

                                        FirebaseFirestore.getInstance().collection(Constants.PRODUCTS).whereEqualTo(Constants.PRODUCT_BARCODE, cartProducts[productIndex]).get().addOnSuccessListener { it3 ->
                                            for (document3 in it3) {
                                                val amount = document3.data[Constants.PRODUCT_AMOUNT] as Long
                                                val productCategory = document3.data[Constants.PRODUCT_CATEGORY] as String
                                                val productHashMap = HashMap<String, Any>()

                                                productHashMap[Constants.PRODUCT_AMOUNT] = amount.toInt() - cartProductAmount[productIndex]
                                                FirebaseFirestore.getInstance().collection(Constants.PRODUCTS).document(document3.id).update(productHashMap)
                                                    .addOnSuccessListener {
                                                        Log.e("Product update: ", "Successful")
                                                    }.addOnFailureListener {
                                                        Log.e("Product update: ", "Failed")
                                                    }

                                                val saleHashMap = HashMap<String, Any>()
                                                when (productCategory) {
                                                    "ĐỒ UỐNG CÁC LOẠI" -> salesProductCategoryAmount[0] += cartProductAmount[productIndex]
                                                    "SỮA UỐNG CÁC LOẠI" -> salesProductCategoryAmount[1] += cartProductAmount[productIndex]
                                                    "BÁNH KẸO CÁC LOẠI" -> salesProductCategoryAmount[2] += cartProductAmount[productIndex]
                                                    "MÌ, CHÁO, PHỞ, BÚN" -> salesProductCategoryAmount[3] += cartProductAmount[productIndex]
                                                    "DẦU ĂN, GIA VỊ" -> salesProductCategoryAmount[4] += cartProductAmount[productIndex]
                                                    "GẠO, BỘT, ĐỒ KHÔ" -> salesProductCategoryAmount[5] += cartProductAmount[productIndex]
                                                    "ĐỒ MÁT, ĐÔNG LẠNH" -> salesProductCategoryAmount[6] += cartProductAmount[productIndex]
                                                    "TẢ, ĐỒ CHO BÉ" -> salesProductCategoryAmount[7] += cartProductAmount[productIndex]
                                                    "CHĂM SÓC CÁ NHÂN" -> salesProductCategoryAmount[8] += cartProductAmount[productIndex]
                                                    "VỆ SINH NHÀ CỬA" -> salesProductCategoryAmount[9] += cartProductAmount[productIndex]
                                                    "ĐỒ DÙNG GIA ĐÌNH" -> salesProductCategoryAmount[10] += cartProductAmount[productIndex]
                                                    "VĂN PHÒNG PHẨM" -> salesProductCategoryAmount[11] += cartProductAmount[productIndex]
                                                }

                                                val current = LocalDateTime.now()
                                                val formatter1 = DateTimeFormatter.BASIC_ISO_DATE
                                                val formatted1 = current.format(formatter1)
                                                val month = formatted1.substring(4, 6)
                                                val year = formatted1.substring(0, 4)
                                                val updateTime = "$month/$year"
                                                var pos = -1

                                                if (salesTime.isEmpty()) {
                                                    salesTime.add(updateTime)
                                                    salesTimeAmount.add(cartProductAmount[productIndex].toLong())
                                                } else {
                                                    for (ind in 0 until salesTime.size) {
                                                        if (updateTime == salesTime[ind]) {
                                                            pos = 0
                                                            salesTimeAmount[ind] += cartProductAmount[productIndex].toLong()
                                                        }
                                                    }
                                                    if (pos == -1) {
                                                        salesTime.add(updateTime)
                                                        salesTimeAmount.add(cartProductAmount[productIndex].toLong())
                                                    }
                                                }

                                                saleHashMap[Constants.SALES_PRODUCT_CATEGORY_AMOUNT] = salesProductCategoryAmount
                                                saleHashMap[Constants.SALES_PRICE] = salesPrice
                                                saleHashMap[Constants.SALES_TOTAL_PRODUCTS] = salesTotalProducts
                                                saleHashMap[Constants.SALES_PRODUCTS] = salesProducts
                                                saleHashMap[Constants.SALES_PRODUCT_AMOUNT] = salesProductAmount
                                                saleHashMap[Constants.SALES_TIME] = salesTime
                                                saleHashMap[Constants.SALES_TIME_AMOUNT] = salesTimeAmount
                                                FirebaseFirestore.getInstance().collection(Constants.SALES).document(document1.id).update(saleHashMap)
                                                    .addOnSuccessListener {
                                                        Log.e("Sales update: ", "Successful")
                                                    }.addOnFailureListener {
                                                        Log.e("Sales update: ", "Failed")
                                                    }
                                            }
                                        }
                                    }

                                    // Delete products in cart

                                    val cartHashMap = HashMap<String, Any>()
                                    cartHashMap[Constants.CART_PRICE] = 0
                                    cartHashMap[Constants.CART_PRODUCT_NUM] = 0
                                    cartHashMap[Constants.CART_PRODUCTS] = mutableListOf<Long>()
                                    cartHashMap[Constants.CART_PRODUCT_AMOUNT] = mutableListOf<Int>()

                                    FirebaseFirestore.getInstance().collection(Constants.CARTS).document(document2.id).update(cartHashMap)
                                        .addOnSuccessListener {
                                            Log.e("Cart update: ", "Successful")
                                        }.addOnFailureListener {
                                            Log.e("Cart update: ", "Failed")
                                        }


                                    var newReceiptProducts = ""
                                    var newReceiptProductAmount = ""

                                    // cartProducts.size = cartProductAmount.size
                                    for (i in 0 until cartProducts.size - 1) {
                                        newReceiptProducts += cartProducts[i].toString() + ","
                                        newReceiptProductAmount += cartProductAmount[i].toString() + ","
                                    }
                                    newReceiptProducts += cartProducts[cartProducts.size - 1]
                                    newReceiptProductAmount += cartProductAmount[cartProductAmount.size - 1]

                                    FirebaseFirestore.getInstance().collection(Constants.RECEIPTS).whereEqualTo(Constants.USER_ID, userId).get().addOnSuccessListener { it4 ->
                                        for (document4 in it4) {
                                            val receiptProducts = document4.data[Constants.RECEIPT_PRODUCTS] as MutableList<String>
                                            val receiptProductAmount = document4.data[Constants.RECEIPT_PRODUCT_AMOUNT] as MutableList<String>
                                            val receiptPrices = document4.data[Constants.RECEIPT_PRICES] as MutableList<Long>
                                            val receiptTotalProducts = document4.data[Constants.RECEIPT_TOTAL_PRODUCTS] as MutableList<Long>
                                            val receiptDates = document4.data[Constants.RECEIPT_DATES] as MutableList<String>
                                            val receiptHashMap = HashMap<String, Any>()

                                            receiptProducts.add(newReceiptProducts)
                                            receiptProductAmount.add(newReceiptProductAmount)
                                            receiptPrices.add(cartPrice)
                                            receiptTotalProducts.add(cartProNum)
                                            receiptHashMap[Constants.RECEIPT_PRODUCTS] = receiptProducts
                                            receiptHashMap[Constants.RECEIPT_PRODUCT_AMOUNT] = receiptProductAmount
                                            receiptHashMap[Constants.RECEIPT_PRICES] = receiptPrices
                                            receiptHashMap[Constants.RECEIPT_TOTAL_PRODUCTS] = receiptTotalProducts

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

                                            FirebaseFirestore.getInstance().collection(Constants.RECEIPTS).document(document4.id).update(receiptHashMap)
                                                .addOnSuccessListener {
                                                    Log.e("Receipt update: ", "Successful")
                                                }
                                                .addOnFailureListener {
                                                    Log.e("Receipt update: ", "Failed")
                                                }
                                        }
                                    }

                                    FirebaseFirestore.getInstance().collection(Constants.USERS).whereEqualTo(Constants.USER_ID, userId).get().addOnSuccessListener { it5 ->
                                        for (document5 in it5) {
                                            val userHashMap = HashMap<String, Any>()
                                            var userCoupon = document5.data[Constants.USER_COUPON] as Long
                                            userCoupon += (cartPrice * 0.05).roundToLong()
                                            userHashMap[Constants.USER_COUPON] = userCoupon
                                            FirebaseFirestore.getInstance().collection(Constants.USERS).document(document5.id).update(userHashMap).addOnSuccessListener {
                                                Log.e("User coupon update: ", "Successful")
                                            }
                                                .addOnFailureListener {
                                                    Log.e("User coupon update: ", "Failed")
                                                }
                                        }
                                    }

                                    FirebaseFirestore.getInstance().collection(Constants.TOTAL_POINT).document("5Tw5en3twZNtk7tk5bKD").get().addOnSuccessListener { it6 ->
                                        val point = it6.data?.get(Constants.POINTS_LIST) as MutableList<Long>
                                        val users = it6.data?.get(Constants.USERS_LIST) as MutableList<String>

                                        for (index in 0 until users.size) {
                                            if (userId == users[index]) {
                                                point[index] += (cartPrice * 0.05).roundToLong()
                                            }
                                        }
                                        val pointHashMap = HashMap<String, Any>()
                                        pointHashMap[Constants.POINTS_LIST] = point
                                        FirebaseFirestore.getInstance().collection(Constants.TOTAL_POINT).document("5Tw5en3twZNtk7tk5bKD").update(pointHashMap).addOnSuccessListener {
                                            Log.e("Update point", "Success")
                                        }.addOnFailureListener {
                                            Log.e("Update point", "Failed")
                                        }
                                    }

                                }
                            }



                        }

                    }
                    // Do this in the official app
                    val intent = Intent(this@PaymentActivity, UserActivity::class.java)
                    startActivity(intent)
                    finish()

                    if (data.getStringExtra("data") != null && data.getStringExtra("data") != "") {
                        // TODO:
                    } else {
                        tvMessage.text = "message: " + this.getString(R.string.not_receive_info)
                    }
                } else if (data.getIntExtra("status", -1) == 1) {
                    val message =
                        if (data.getStringExtra("message") != null) data.getStringExtra("message") else "Thất bại"
                    tvMessage.text = "message: $message"
                } else if (data.getIntExtra("status", -1) == 2) {
                    tvMessage.text = "message: " + this.getString(R.string.not_receive_info)
                } else {
                    tvMessage.text = "message: " + this.getString(R.string.not_receive_info)
                }
            } else {
                tvMessage.text = "message: " + this.getString(R.string.not_receive_info)
            }
        } else {
            tvMessage.text = "message: " + this.getString(R.string.not_receive_info_err)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.payment_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_notice -> {
                val dialog = NoticeCustomDialogFragment()
                dialog.show(supportFragmentManager, "Custom Dialog Notice")
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onClick(view: View?) {
        if (view != null) {
            when (view.id) {
                R.id.btnPayMoMo -> requestPayment()
                R.id.btnPayRaw -> {
                    val intent = Intent(this, QRGeneratorActivity::class.java)
                    intent.putExtra(Constants.USER_ID, userId)
                    startActivity(intent)
                    finish()
                }
                R.id.btnPayOnline -> {
                    val intent = Intent(this, OnlineOrderActivity::class.java)
                    intent.putExtra(Constants.USER_ID, userId)
                    startActivity(intent)
                    finish()
                }
            }
        }
    }

}