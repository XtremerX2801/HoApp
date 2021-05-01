package com.example.thesis_trading_app.ui.Admin

import android.content.Intent
import android.os.Bundle
import android.view.View
import com.example.thesis_trading_app.R
import com.example.thesis_trading_app.firestore.FirestoreClass
import com.example.thesis_trading_app.ui.Authentication.BaseActivity
import com.example.thesis_trading_app.ui.User.DashboardActivity
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.journeyapps.barcodescanner.BarcodeEncoder
import kotlinx.android.synthetic.main.activity_q_r_generator.*

class QRGeneratorActivity : BaseActivity(), View.OnClickListener {

    private val userId = FirestoreClass().getCurrentUserId()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_q_r_generator)

        generateQRCode()

        btn_return_home.setOnClickListener (this)
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

    override fun onClick(view: View?) {
        if (view != null) {
            when (view.id) {
                R.id.btn_return_home -> {
                    val intent = Intent(this, DashboardActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }
        }
    }

}