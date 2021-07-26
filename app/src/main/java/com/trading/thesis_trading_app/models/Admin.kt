package com.trading.thesis_trading_app.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize

class Admin (
    val admin_id: String = "",
    val admin_firstName: String = "",
    val admin_lastName: String = "",
    val admin_email: String = "",
    val admin_image: String = "",
    val admin_mobile: Long = 0,
    val admin_gender: String = "",
    val admin_address: String = "",
    val admin_store_id: Int = 0
): Parcelable