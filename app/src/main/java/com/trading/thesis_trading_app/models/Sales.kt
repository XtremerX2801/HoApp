package com.trading.thesis_trading_app.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class Sales(
    val saleID: String = "",
    val sale_total_products: Int = 0,
    val sales_price: Long = 0,
    val sales_products:  MutableList<Long> = mutableListOf(),
    val sales_product_amount: MutableList<Int> = mutableListOf(),
    val sales_product_category_amount: MutableList<Int> = mutableListOf(),
    val sales_time: MutableList<String> = mutableListOf(),
    val sales_time_amount: MutableList<Long> = mutableListOf()
    ): Parcelable