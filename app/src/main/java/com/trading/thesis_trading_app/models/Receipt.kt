package com.trading.thesis_trading_app.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class Receipt(
    val user_id: String = "",
    val receipt_dates: MutableList<String> = mutableListOf(),
    val receipt_prices: MutableList<Long> = mutableListOf(),
    val receipt_product_amount:  MutableList<Long> = mutableListOf(),
    val receipt_products: MutableList<String> = mutableListOf(),
    val receipt_total_products: MutableList<Int> = mutableListOf()
): Parcelable