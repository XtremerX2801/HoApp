package com.trading.thesis_trading_app.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class Product(
    val product_id: Long = 0,
    val product_name: String = "",
    val product_category: String = "",
    val product_price: Long = 0,
    val product_amount: Int = 0,
    val update_person: String = "Ho Vo",
    val update_date: String = "01-01-2021",
    val product_brand: String = "",
    val product_made_in: String = "",
    val product_image: String ="",
    val product_barcode: Long = 0
): Parcelable