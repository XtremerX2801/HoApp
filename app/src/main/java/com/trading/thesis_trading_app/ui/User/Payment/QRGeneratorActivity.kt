package com.trading.thesis_trading_app.ui.User.Payment

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.installations.FirebaseInstallations
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.Gson
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.journeyapps.barcodescanner.BarcodeEncoder
import com.trading.thesis_trading_app.R
import com.trading.thesis_trading_app.firestore.FirestoreClass
import com.trading.thesis_trading_app.ui.Authentication.BaseActivity
import com.trading.thesis_trading_app.ui.User.Notification.FirebaseService
import com.trading.thesis_trading_app.ui.User.Notification.NotificationData
import com.trading.thesis_trading_app.ui.User.Notification.PushNotification
import com.trading.thesis_trading_app.ui.User.Notification.RetrofitInstance
import com.trading.thesis_trading_app.ui.User.UserActivity
import com.trading.thesis_trading_app.ui.User.fragments.PaymentCustomDialogFragment
import com.trading.thesis_trading_app.utils.Constants
import kotlinx.android.synthetic.main.activity_q_r_generator.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class QRGeneratorActivity : BaseActivity(), View.OnClickListener {

    val TAG = "QRGeneratorActivity"
    var theToken: String = ""

    private val userId = FirestoreClass().getCurrentUserId()
    private val CHANNEL_ID = "channel_id_example_01"
    private val notificationId = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_q_r_generator)
        FirebaseService.sharedPref = getSharedPreferences("sharedPref", Context.MODE_PRIVATE)
        FirebaseInstallations.getInstance().getToken(true).addOnSuccessListener {
            FirebaseService.token = it.token
            theToken = it.token
        }
        FirebaseMessaging.getInstance().subscribeToTopic(Constants.TOPIC)

        generateQRCode()
        createNotificationChannel()

        if (intent.hasExtra("Store")) {
            val id = intent.getIntExtra("Store", 0)
            get_address.text = when (id) {
                1 -> "Cửa hàng 1"
                2 -> "Cửa hàng 2"
                3 -> "Cửa hàng 3"
                4 -> "Cửa hàng 4"
                5 -> "Cửa hàng 5"
                else -> "Xin hãy chọn lại địa chỉ"
            }
        }

        btn_return_home.setOnClickListener (this)
        choose_address.setOnClickListener(this)
        btn_send_message_of_product.setOnClickListener(this)
        btn_back.setOnClickListener(this)
    }

    private fun generateQRCode() {
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
            image_QR.setImageBitmap(bitmap)

        } catch (e: WriterException) {
            e.printStackTrace()
        }
    }

    private fun sendNotification(notification: PushNotification) = CoroutineScope(Dispatchers.IO).launch {
        try {
            val response = RetrofitInstance.api.postNotification(notification)
            if (response.isSuccessful) {
                Log.e(TAG, "Response: ${Gson().toJson(response)}")
            } else {
                Log.e(TAG, response.errorBody().toString())
            }
        } catch (e: Exception) {
            Log.e(TAG, e.toString())
        }
    }

//    private fun encodeBitmapToString(bmp: Bitmap) : String{
//        val bao = ByteArrayOutputStream()
//        bmp.compress(Bitmap.CompressFormat.PNG, 100, bao) // bmp is bitmap from user image file
//        val byteArray: ByteArray = bao.toByteArray()
//        return Base64.encodeToString(byteArray, Base64.DEFAULT)
//    }
//
//    private fun decodeStringToBitmap(str: String) : Bitmap {
//        val imageBytes = Base64.decode(str, Base64.DEFAULT)
//        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
//    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun sendMessageToStore() {
        val address = get_address.text.toString().trim {it <= ' '}

        if (address.isEmpty()) {
            Toast.makeText(this@QRGeneratorActivity, "Xin hãy chọn địa chỉ nhận hàng", Toast.LENGTH_LONG).show()
        } else {
            var storeId = 0
            if (intent.hasExtra("Store")) {
                storeId = intent.getIntExtra("Store", 0)
                // Admin function
                getNotification(storeId.toString())
            }

            FirebaseFirestore.getInstance().collection(Constants.STORE_ORDER).whereEqualTo(Constants.STORE_ID, storeId).get().addOnSuccessListener {
                for (document in it) {
                    val userIds = document.data[Constants.USER_IDS] as MutableList<String>
                    val storeProducts = document.data[Constants.STORE_PRODUCTS] as MutableList<String>
                    val amounts = document.data[Constants.AMOUNTS] as MutableList<String>
                    val prices = document.data[Constants.PRICES] as MutableList<Long>
                    val storeDates = document.data[Constants.STORE_DATES] as MutableList<String>

                    FirebaseFirestore.getInstance().collection(Constants.CARTS).whereEqualTo(Constants.USER_ID, userId).get().addOnSuccessListener { it1 ->
                        for (document1 in it1) {
                            val cartProducts = document1.data[Constants.CART_PRODUCTS] as MutableList<Long>
                            val cartProductAmount = document1.data[Constants.CART_PRODUCT_AMOUNT] as MutableList<Int>
                            val cartPrice = document1.data[Constants.CART_PRICE] as Long

                            var products = cartProducts[0].toString()
                            var proAmounts = cartProductAmount[0].toString()
                            for (a in 1 until cartProducts.size) {
                                products += "," + cartProducts[a].toString()
                                proAmounts += "," + cartProductAmount[a].toString()
                            }

                            val current = LocalDateTime.now()
                            val formatter1 = DateTimeFormatter.BASIC_ISO_DATE
                            val formatted1 = current.format(formatter1)
                            val day = formatted1.substring(6, 8)
                            val month = formatted1.substring(4, 6)
                            val year = formatted1.substring(0, 4)
                            val updateTime = "$day/$month/$year"

                            storeProducts.add(products)
                            amounts.add(proAmounts)
                            prices.add(cartPrice)
                            userIds.add(userId)
                            storeDates.add(updateTime)

                            val orderHashMap = HashMap<String, Any>()
                            orderHashMap[Constants.STORE_PRODUCTS] = storeProducts
                            orderHashMap[Constants.AMOUNTS] = amounts
                            orderHashMap[Constants.PRICES] = prices
                            orderHashMap[Constants.USER_IDS] = userIds
                            orderHashMap[Constants.STORE_DATES] = storeDates

                            FirebaseFirestore.getInstance().collection(Constants.STORE_ORDER).document(document.id).update(orderHashMap).addOnSuccessListener {
                                Log.e("Update store order", "Success")
                            }.addOnFailureListener {
                                Log.e("Update store order", "Failed")
                            }

                            val cartHashMap = HashMap<String, Any>()
                            cartHashMap[Constants.CART_PRICE] = 0
                            cartHashMap[Constants.CART_PRODUCT_NUM] = 0
                            cartHashMap[Constants.CART_PRODUCTS] = mutableListOf<Long>()
                            cartHashMap[Constants.CART_PRODUCT_AMOUNT] = mutableListOf<Int>()

                            FirebaseFirestore.getInstance().collection(Constants.CARTS).document(document1.id).update(cartHashMap).addOnSuccessListener {
                                Log.e("Cart update: ", "Successful")
                            }.addOnFailureListener {
                                Log.e("Cart update: ", "Failed")
                            }
                        }
                    }

                    Toast.makeText(this, "Cảm ơn quý khách đã mua hàng", Toast.LENGTH_LONG).show()
                    val intent = Intent(this, UserActivity::class.java)
                    startActivity(intent)
                    finish()

                }
            }
        }

    }

    // make notification show in the admin app
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "New Order Notification"
            val descriptionText = "Notification Description"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun getNotification(store: String?) {

        val intent = Intent(this, UserActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(this, 0, intent, 0)
        val bitmapLargeIcon = BitmapFactory.decodeResource(applicationContext.resources, R.drawable.ic_dashboard_black_24dp)

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Đơn đặt hàng mới")
            .setContentText(store)
            .setLargeIcon(bitmapLargeIcon)
            .setStyle(NotificationCompat.BigTextStyle().bigText("Bạn đã chọn $store làm nơi nhận hàng, xin hãy đến nhận trong ngày. Đơn hàng sẽ bị hủy vào ngày hôm sau"))
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        with(NotificationManagerCompat.from(this)) {
            notify(notificationId, builder.build())
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onClick(view: View?) {
        if (view != null) {
            when (view.id) {
                R.id.btn_return_home -> {
                    val intent = Intent(this, UserActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                R.id.choose_address -> {
                    val dialog = PaymentCustomDialogFragment()
                    dialog.show(supportFragmentManager, "Custom Dialog Location")
                }
                R.id.btn_send_message_of_product -> {
                    sendMessageToStore()
                    val title = "New Order"
                    val message = get_address.text.toString()
                    var recipientToken = ""
                    if (theToken != "") {
                        recipientToken = theToken
                    }

                    if (message.isNotEmpty()) {
                        PushNotification(
                            NotificationData(title, message),
                            recipientToken
                        ).also {
//                            sendNotification(it)
                        }
                    }

                    val intent = Intent(this@QRGeneratorActivity, UserActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                R.id.btn_back -> {
                    val intent = Intent(this, PaymentActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }
        }
    }

}