package com.example.thesis_trading_app.ui.User

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.budiyev.android.codescanner.*
import com.example.thesis_trading_app.R
import com.example.thesis_trading_app.utils.Constants
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_user_q_r.*

class UserQRActivity : AppCompatActivity() {

    private lateinit var codeScanner: CodeScanner
    private lateinit var barcodeValue: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_q_r)

        codeScanner = CodeScanner(this, user_scannerView)
        codeScanner.camera = CodeScanner.CAMERA_BACK
        codeScanner.formats = CodeScanner.ALL_FORMATS
        codeScanner.autoFocusMode = AutoFocusMode.SAFE
        codeScanner.scanMode  = ScanMode.SINGLE
        codeScanner.isAutoFocusEnabled = true
        codeScanner.isFlashEnabled = false

        codeScanner.decodeCallback = DecodeCallback {
            runOnUiThread {
                barcodeValue = it.text
                Toast.makeText(this, "Quét sản phẩm thành công", Toast.LENGTH_LONG).show()
                var userId = ""
                if (intent?.hasExtra(Constants.EXTRA_USER_DETAILS)  == true) {
                    userId = intent?.getStringExtra(Constants.EXTRA_USER_DETAILS)!!
                }
                addToCart(userId, barcodeValue)

                @Suppress("DEPRECATION")
                Handler().postDelayed({ val intent = Intent(this@UserQRActivity, DashboardActivity::class.java)
                    intent.putExtra(Constants.EXTRA_USER_DETAILS, userId)
                    intent.putExtra(Constants.PRODUCT_DATA, 1)
                    startActivity(intent)
                    finish() }, 1000)
            }
        }

        codeScanner.errorCallback = ErrorCallback {
            runOnUiThread {
                Toast.makeText(this, "Camera Error: ${it.message}", Toast.LENGTH_SHORT).show()
            }
        }

        codeScanner.startPreview()
        checkPermission()
    }

    private fun checkPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                Constants.MY_CAMERA_PERMISSION_REQUEST
            )
        } else {
            codeScanner.startPreview()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode ==  Constants.MY_CAMERA_PERMISSION_REQUEST && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            codeScanner.startPreview()
        } else {
            Toast.makeText(this, "Please give the camera PERMISSION", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        codeScanner.startPreview()
    }

    override fun onPause() {
        codeScanner.releaseResources()
        super.onPause()
    }

    private fun addToCart(userId: String, barcode: String){
        val cartHashMap = HashMap<String, Any>()
        var proPrice: Long = 0
        FirebaseFirestore.getInstance().collection(Constants.PRODUCTS).whereEqualTo(Constants.PRODUCT_BARCODE, barcode.toLong()).get().addOnSuccessListener {
            for (document in it) {
                proPrice = document.data[Constants.PRODUCT_PRICE] as Long
            }
        }

        var cartNum: Long
        var cartPrice: Long
        var cartProducts: MutableList<Long>
        var cartProductAmount: MutableList<Int>
        val amount = 1
        FirebaseFirestore.getInstance().collection(Constants.CARTS).whereEqualTo(Constants.USER_ID, userId).get().addOnSuccessListener {
            for (document in it) {
                cartNum = document.data[Constants.CART_PRODUCT_NUM] as Long
                cartPrice = document.data[Constants.CART_PRICE] as Long
                cartProducts = document.data[Constants.CART_PRODUCTS] as MutableList<Long>
                cartProductAmount = document.data[Constants.CART_PRODUCT_AMOUNT] as MutableList<Int>

                var same = 0
                if (cartProducts.isEmpty()) {
                    cartProductAmount.add(amount)
                    cartProducts.add(barcode.toLong())
                } else {
                    for (i in 0 until cartProducts.size) {
                        if (cartProducts[i] == barcode.toLong()) {
                            cartProductAmount[i] = cartProductAmount[i] + 1
                            same = 1
                        }
                    }
                    if (same == 0) {
                        cartProductAmount.add(amount)
                        cartProducts.add(barcode.toLong())
                    }
                }

                cartHashMap[Constants.CART_PRODUCT_NUM] = cartNum + 1
                cartHashMap[Constants.CART_PRICE] = cartPrice + proPrice
                cartHashMap[Constants.CART_PRODUCTS] = cartProducts
                cartHashMap[Constants.CART_PRODUCT_AMOUNT] = cartProductAmount

                FirebaseFirestore.getInstance().collection(Constants.CARTS).document(document.id).update(cartHashMap)
            }
        }

    }

}