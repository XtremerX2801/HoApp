package com.trading.thesis_trading_app.ui.User

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.trading.thesis_trading_app.R
import com.trading.thesis_trading_app.firestore.FirestoreClass
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.journeyapps.barcodescanner.BarcodeEncoder
import kotlinx.android.synthetic.main.activity_user_idactivity.*

class UserIDActivity : AppCompatActivity(), View.OnClickListener {

    private val userId = FirestoreClass().getCurrentUserId()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_idactivity)

        generateQRCode()

        btn_return_home1.setOnClickListener (this)
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
            id_QR.setImageBitmap(bitmap)

        } catch (e: WriterException) {
            e.printStackTrace()
        }
    }

    override fun onClick(view: View?) {
        if (view != null) {
            when (view.id) {
                R.id.btn_return_home1 -> {
                    val intent = Intent(this, UserActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }
        }
    }

}