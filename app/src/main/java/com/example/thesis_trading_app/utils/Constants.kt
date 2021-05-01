package com.example.thesis_trading_app.utils

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import android.webkit.MimeTypeMap

object Constants {
    const val USERS: String = "users"
    const val ADMIN: String = "admin"
    const val PRODUCTS: String = "products"
    const val CARTS: String = "carts"

    const val MYAPP_PREFERENCES: String = "myAppPrefs"
    const val LOGGED_IN_USERNAME: String = "logged_in_username"
    const val EXTRA_USER_DETAILS: String = "extra_user_details"
    const val READ_STORAGE_PERMISSION_CODE = 2
    const val PICK_IMAGE_REQUEST_CODE = 1
    const val USER_PROFILE_IMAGE: String = "User_Profile_Image"
    const val COMLPLETE_PROFILE: String = "profileCompleted"
    const val UPDATE_PERSON: String = "update_person"
    const val UPDATE_DATE: String = "update_date"
    const val UPDATE_PERSON_MOBILE: String = "update_person_mobile"
    const val PRODUCT_IMAGE: String = "product_image"
    const val PRODUCT_BARCODE: String = "product_barcode"
    const val PRODUCT_SEARCH_KEYWORD: String = "product_search_keyword"
    const val MALE: String = "male"
    const val FEMALE: String = "female"
    const val PRODUCT_DATA: String = "product_data"
    const val SEARCH_PRODUCT_NAME = "search_product_name"
    const val SEARCH_PRODUCT_BARCODE = "search_product_barcode"
    const val PRODUCT_BARCODE_VALUE = "product_barcode_value"
    const val USER_ID = "user_id"
    const val CART_PRODUCT_NUM = "cart_product_num"
    const val CART_PRODUCTS = "cart_products"
    const val CART_PRICE = "cart_price"
    const val CART_PRODUCT_AMOUNT = "cart_product_amount"
    const val USER_MOBILE: String = "user_mobile"
    const val USER_GENDER: String = "user_gender"
    const val USER_IMAGE: String = "user_image"
    const val USER_FIRST_NAME: String = "user_firstName"
    const val USER_LAST_NAME: String = "user_lastName"
    const val PRODUCT_NAME: String = "product_name"
    const val PRODUCT_CATEGORY: String = "product_category"
    const val PRODUCT_PRICE: String = "product_price"
    const val PRODUCT_AMOUNT: String = "product_amount"
    const val SALES: String = "sales"
    const val SALEID: String = "saleID"
    const val SALES_PRICE: String = "sales_price"
    const val SALES_PRODUCT_AMOUNT: String= "sales_product_amount"
    const val SALES_PRODUCTS: String = "sales_products"
    const val SALES_TOTAL_PRODUCTS: String = "sales_total_products"
    const val SALES_PRODUCT_CATEGORY_AMOUNT: String = "sales_product_category_amount"
    const val RECEIPTS: String = "receipts"
    const val RECEIPT_PRODUCTS: String = "receipt_products"
    const val RECEIPT_PRODUCT_AMOUNT: String = "receipt_product_amount"
    const val RECEIPT_PRICES: String = "receipt_prices"
    const val RECEIPT_TOTAL_PRODUCTS: String = "receipt_total_products"
    const val MY_CAMERA_PERMISSION_REQUEST = 1111
    const val KEY_ENVIRONMENT = "key_environment"

    fun showImageChooser(activity: Activity) {
        // An intent for launching the image selection of phone storage
        val galleryIntent = Intent(
            Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        )

        Log.e("Next", "Time")

        // Launches the image selection of phone storage using the constant mode
        activity.startActivityForResult(galleryIntent, PICK_IMAGE_REQUEST_CODE)
    }

    fun getFileExtension(activity: Activity, uri: Uri?): String? {
        // MimeTypeMap: Two-way map that maps MIME-types to file extensions and vice versa.
        // getSingleton(): Get the singleton instance of MimeType
        // getExtensionFromMimeType: return the registered extension for the given MIME type
        // contentResolver.getType: Return the MIME type of the given content URL
        return MimeTypeMap.getSingleton().getExtensionFromMimeType(
            activity.contentResolver.getType(
                uri!!
            )
        )
    }

}