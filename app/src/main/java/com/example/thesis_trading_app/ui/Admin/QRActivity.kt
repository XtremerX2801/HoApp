package com.example.thesis_trading_app.ui.Admin

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.budiyev.android.codescanner.*
import com.example.thesis_trading_app.R
import com.example.thesis_trading_app.utils.Constants
import kotlinx.android.synthetic.main.activity_q_r.*

class QRActivity : AppCompatActivity() {

    private lateinit var codeScanner: CodeScanner
    private lateinit var barcodeValue: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_q_r)

        codeScanner = CodeScanner(this, scannerView)
        codeScanner.camera = CodeScanner.CAMERA_BACK
        codeScanner.formats = CodeScanner.ALL_FORMATS
        codeScanner.autoFocusMode = AutoFocusMode.SAFE
        codeScanner.scanMode  = ScanMode.SINGLE
        codeScanner.isAutoFocusEnabled = true
        codeScanner.isFlashEnabled = false

        codeScanner.decodeCallback = DecodeCallback {
            runOnUiThread {
                barcodeValue = it.text
                Log.e("Barcode", barcodeValue)
                Toast.makeText(this, "Scan Result: $barcodeValue", Toast.LENGTH_SHORT).show()
                val intent = Intent(this@QRActivity, SearchProductAdminActivity::class.java)
                intent.putExtra(Constants.PRODUCT_BARCODE_VALUE, barcodeValue)
                startActivity(intent)
                finish()
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
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), Constants.MY_CAMERA_PERMISSION_REQUEST)
        } else {
            codeScanner.startPreview()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
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

}