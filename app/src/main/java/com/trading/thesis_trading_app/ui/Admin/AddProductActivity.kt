package com.trading.thesis_trading_app.ui.Admin

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.trading.thesis_trading_app.R
import com.trading.thesis_trading_app.firestore.FirestoreClass
import com.trading.thesis_trading_app.ui.Authentication.BaseActivity
import com.trading.thesis_trading_app.utils.Constants
import com.trading.thesis_trading_app.utils.GlideLoader
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_add_product.*
import kotlinx.android.synthetic.main.activity_q_r_generator.*
import kotlinx.android.synthetic.main.activity_user_profile.*
import java.io.IOException
import java.util.*
import kotlin.collections.HashMap


class AddProductActivity : BaseActivity(), View.OnClickListener {

    private var mSeleactedImageFileUri: Uri? = null
    private var mProductImageImageURL: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_product)

        if (intent.hasExtra(Constants.PRODUCT_BARCODE)) {
            val barcode = intent.getStringExtra(Constants.PRODUCT_BARCODE)!!
            et_new_product_barCode.setText(barcode)
        }

        et_new_product_category.setText("Nước uống các loại")
        et_new_product_price.setText("10000")
        et_new_product_amount.setText("100")
        new_product_avatar.setOnClickListener(this)
        btn_add_product.setOnClickListener(this)
    }

    private fun validateNewProductDetails(): Boolean {
        return when {
            TextUtils.isEmpty(et_new_product_name.text.toString().trim { it <= ' ' }) -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_enter_product_name), true)
                false
            }

            TextUtils.isEmpty(et_new_product_category.text.toString().trim { it <= ' ' }) -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_enter_product_category), true)
                false
            }

            TextUtils.isEmpty(et_new_product_price.text.toString().trim { it <= ' ' }) -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_enter_product_price), true)
                false
            }

            TextUtils.isEmpty(et_new_product_amount.text.toString().trim { it <= ' ' }) -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_enter_product_amount), true)
                false
            }
            TextUtils.isEmpty(et_new_product_barCode.text.toString().trim { it <= ' ' }) -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_enter_product_barCode), true)
                false
            }
            TextUtils.isEmpty(et_new_product_brand.text.toString().trim { it <= ' ' }) -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_enter_product_brand), true)
                false
            }
            TextUtils.isEmpty(et_new_product_made_in.text.toString().trim { it <= ' ' }) -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_enter_product_made_in), true)
                false
            }
            else -> {
                true
            }
        }
    }

    private fun addNewProduct() {
        if (validateNewProductDetails()) {
            val newProductName: String = et_new_product_name.text.toString().trim { it <= ' '}
            val newProductPrice: String = et_new_product_price.text.toString().trim { it <= ' '}
            val newProductCategory: String = et_new_product_category.text.toString().trim { it <= ' '}
            val newProductAmount: String = et_new_product_amount.text.toString().trim { it <= ' '}
            val newProductBarcode: String = et_new_product_barCode.text.toString().trim { it <= ' '}
            val newProductBrand: String = et_new_product_brand.text.toString().trim { it <= ' '}
            val newProductMadeIn: String = et_new_product_made_in.text.toString().trim { it <= ' '}

            val db = FirebaseFirestore.getInstance()
            val userHashMap = HashMap<String, Any>()
            userHashMap[Constants.PRODUCT_NAME] = newProductName
            userHashMap[Constants.PRODUCT_CATEGORY] = newProductCategory
            userHashMap[Constants.PRODUCT_PRICE] = newProductPrice.toLong()
            userHashMap[Constants.PRODUCT_AMOUNT] = newProductAmount.toInt()
            userHashMap[Constants.PRODUCT_BRAND] = newProductBrand
            userHashMap[Constants.PRODUCT_MADE_IN] = newProductMadeIn
            userHashMap["product_id"] = 0
            userHashMap["product_created_at"] = "2021-01-01 00:00:00"
            userHashMap["comment"] = mutableListOf<String>()
            userHashMap["comment_user"] = mutableListOf<String>()
            userHashMap[Constants.PRODUCT_SEARCH_KEYWORD] = generateSearchKeywords(newProductName)
            if (mProductImageImageURL != "") {
                userHashMap[Constants.PRODUCT_IMAGE] = mProductImageImageURL
            }
            userHashMap[Constants.PRODUCT_BARCODE] = newProductBarcode.toLong()

            db.collection(Constants.PRODUCTS).add(userHashMap)
                .addOnSuccessListener {
                    Toast.makeText(
                        this@AddProductActivity,
                        "Product added successfully",
                        Toast.LENGTH_LONG
                    ).show()
                }.addOnFailureListener {
                    Toast.makeText(
                        this@AddProductActivity,
                        "Product added failed",
                        Toast.LENGTH_LONG
                    ).show()
                }

        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onClick(view: View?) {
        if (view != null) {
            when (view.id) {
                R.id.btn_add_product -> {
                    if (mSeleactedImageFileUri != null) {
                        FirestoreClass().uploadProductImageToCloudStorage(
                            this,
                            mSeleactedImageFileUri
                        )
                    } else {
                        addNewProduct()
                    }
                    val intent = Intent(this@AddProductActivity, AdminActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                R.id.new_product_avatar -> {
                    getImage()
                }
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
            Constants.showImageChooser(this@AddProductActivity)
        } else {
            ActivityCompat.requestPermissions(
                this@AddProductActivity,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                Constants.READ_STORAGE_PERMISSION_CODE
            )
        }
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

                Constants.showImageChooser(this@AddProductActivity)
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
                    GlideLoader(this).loadProductPicture(
                        mSeleactedImageFileUri!!,
                        new_product_avatar
                    )
                } catch (e: IOException) {
                    e.printStackTrace()
                    Toast.makeText(
                        this@AddProductActivity,
                        resources.getString(R.string.image_selection_failed),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        } else if (resultCode == Activity.RESULT_CANCELED) {
            Log.e("Request Cancelled", "Image selection cancelled")
        }
    }

    fun productImageUploadSuccess(imageURL: String) {
        mProductImageImageURL = imageURL

        addNewProduct()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.product_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.qr_scanner -> {
                startActivity(Intent(this, AddBarcodeActivity::class.java))
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun generateSearchKeywords(inputText: String): List<String> {
        val inputString = inputText.toLowerCase(Locale.ROOT)
        val keywords = mutableListOf<String>()

        val words = inputString.split(" ")
        Log.e("words", words.toString())

        var appendString = ""
        val length = words.size
        Log.e("words", length.toString())

        for (index in 0 until length) {
            for (i in index until length) {
                appendString += if (appendString == "") {
                    words[i]
                } else {
                    " " + words[i]
                }
                keywords.add(appendString)
            }
            appendString = ""
        }

        return keywords
    }

//    private fun encodeBitmapToString(bmp: Bitmap) : String{
//        val bao = ByteArrayOutputStream()
//        bmp.compress(Bitmap.CompressFormat.PNG, 100, bao) // bmp is bitmap from user image file
//        val byteArray: ByteArray = bao.toByteArray()
//        return Base64.encodeToString(byteArray, Base64.DEFAULT)
//    }
//
    //    private fun generateQRCode(): String {
//        val multiFormatWriter = MultiFormatWriter()
//
//        try {
//            val bitMatrix = multiFormatWriter.encode(
//                et_new_product_name.text.toString() + "|" + et_new_product_category.text.toString() + "|" + et_new_product_price.text.toString(),
//                BarcodeFormat.QR_CODE,
//                300,
//                300
//            )
//            val barcodeEncoder = BarcodeEncoder()
//            val bitmap = barcodeEncoder.createBitmap(bitMatrix)
//
//            return encodeBitmapToString(bitmap)
//
//        } catch (e: WriterException) {
//            e.printStackTrace()
//            return ""
//        }
//    }

}