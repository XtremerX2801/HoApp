package com.trading.thesis_trading_app.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class User (
    val user_id: String = "",
    val user_firstName: String = "",
    val user_lastName: String = "",
    val user_email: String = "",
    val user_coupon: Long = 0,
    val total_user_coupon: Long = 0,
    val user_location: String = "",
    val temp_location: String = "",
    val user_image: String = "",
    val user_mobile: Long = 0,
    val user_gender: String = "",
    val user_address: String = "",
    val profileCompleted: Int = 0
): Parcelable