package com.example.thesis_trading_app.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class User (
    val user_id: String = "",
    val user_firstName: String = "",
    val user_lastName: String = "",
    val user_email: String = "",
    val user_image: String = "",
    val user_mobile: Long = 0,
    val user_gender: String = "",
    val profileCompleted: Int = 0
): Parcelable