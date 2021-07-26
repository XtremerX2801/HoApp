package com.trading.thesis_trading_app.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class Cart(
    val user_id: String = "",
    val cart_product_num: Int = 0,
    val cart_price: Long = 0,
    val cart_products:  MutableList<Long> = mutableListOf(),
    val cart_product_amount: MutableList<Int> = mutableListOf()
    ): Parcelable