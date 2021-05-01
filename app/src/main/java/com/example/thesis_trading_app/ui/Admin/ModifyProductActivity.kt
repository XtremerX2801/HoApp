package com.example.thesis_trading_app.ui.Admin

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.thesis_trading_app.R
import com.example.thesis_trading_app.firestore.FirestoreClass
import com.example.thesis_trading_app.ui.Authentication.BaseActivity
import com.example.thesis_trading_app.utils.Constants
import com.example.thesis_trading_app.utils.GlideLoader
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_add_product.*
import kotlinx.android.synthetic.main.activity_modify_product.*
import java.io.IOException
import java.util.*
import kotlin.collections.HashMap

class ModifyProductActivity : BaseActivity(), View.OnClickListener {

    private var mSeleactedImageFileUri: Uri? = null
    private var mProductImageImageURL: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_modify_product)

        new_modified_product_avatar.setOnClickListener(this)
        btn_modify_product.setOnClickListener(this)

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == Constants.READ_STORAGE_PERMISSION_CODE) {
            // If permission is granted
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                showErrorSnackBar("The storage permission is granted.", false)

                Constants.showImageChooser(this@ModifyProductActivity)
            } else {
                // Display another toast if permission is not granted
                Toast.makeText(
                    this,
                    resources.getString(R.string.read_storage_permission_denied),
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.e("imageData", requestCode.toString())
//        if (requestCode == 1) {
        if (requestCode == Constants.PICK_IMAGE_REQUEST_CODE) {
            if (data != null) {
                try {
                    mSeleactedImageFileUri = data.data!!
//                        iv_user_photo.setImageURI(selectedImageFileUri)
                    GlideLoader(this).loadProductPicture(mSeleactedImageFileUri!!, new_modified_product_avatar)
                } catch (e: IOException) {
                    e.printStackTrace()
                    Toast.makeText(
                        this@ModifyProductActivity,
                        resources.getString(R.string.image_selection_failed),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        } else if (resultCode == Activity.RESULT_CANCELED) {
            Log.e("Request Cancelled", "Image selection cancelled")
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun productImageUploadSuccess(imageURL: String) {
        mProductImageImageURL = imageURL
        var barcode = ""
        if (intent.hasExtra(Constants.PRODUCT_BARCODE)) {
            barcode = intent.getStringExtra(Constants.PRODUCT_BARCODE)!!
        }
        updateProduct(barcode.toLong())
    }

    private fun updateProduct(barcode: Long) {
        val newProName = findViewById<com.example.thesis_trading_app.utils.MTAEditText>(R.id.et_new_product_name)
        val newProCat = findViewById<com.example.thesis_trading_app.utils.MTAEditText>(R.id.et_new_product_category)
        val newProPrice = findViewById<com.example.thesis_trading_app.utils.MTAEditText>(R.id.et_new_product_price)
        val newProAmount = findViewById<com.example.thesis_trading_app.utils.MTAEditText>(R.id.et_new_product_amount)

        val productHashMap = HashMap<String, Any>()
        if (newProName.text?.isNotEmpty() == true) {
            productHashMap[Constants.PRODUCT_NAME] = newProName.text.toString()
        }
        if (newProCat.text?.isNotEmpty() == true) {
            productHashMap[Constants.PRODUCT_CATEGORY] = newProCat.text.toString()
        }
        if (newProPrice.text?.isNotEmpty() == true) {
            productHashMap[Constants.PRODUCT_PRICE] = newProPrice.text.toString().toLong()
        }
        if (newProAmount.text?.isNotEmpty() == true) {
            productHashMap[Constants.PRODUCT_AMOUNT] = newProAmount.text.toString().toInt()
        }

        FirebaseFirestore.getInstance().collection(Constants.PRODUCTS).whereEqualTo(Constants.PRODUCT_BARCODE, barcode).get().addOnSuccessListener {
            for (document in it) {
                FirebaseFirestore.getInstance().collection(Constants.PRODUCTS).document(document.id).update(productHashMap)
            }
        }
    }

    private fun getImage() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
            == PackageManager.PERMISSION_GRANTED
        ) {
            Constants.showImageChooser(this@ModifyProductActivity)
        } else {
            ActivityCompat.requestPermissions(
                this@ModifyProductActivity,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                Constants.READ_STORAGE_PERMISSION_CODE
            )
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onClick(v: View?) {
        if (v != null){
            when (v.id) {
                R.id.btn_modify_product -> {
                    if (mSeleactedImageFileUri != null) {
                        FirestoreClass().uploadProductImageToCloudStorage(this, mSeleactedImageFileUri)
                    } else {
                        var barcode = ""
                        if (intent.hasExtra(Constants.PRODUCT_BARCODE)) {
                            barcode = intent.getStringExtra(Constants.PRODUCT_BARCODE)!!
                        }
                        updateProduct(barcode.toLong())
                    }
                    val intent = Intent(this@ModifyProductActivity, ListProductsActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                R.id.new_modified_product_avatar -> {
                    getImage()
                }
            }
        }
    }

}