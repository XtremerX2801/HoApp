package com.trading.thesis_trading_app.ui.User.Notification

import com.trading.thesis_trading_app.utils.Constants.CONTENT_TYPE
import com.trading.thesis_trading_app.utils.Constants.SERVER_KEY
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface NotificationAPI {

    @Headers("Authorization: key=$SERVER_KEY", "Content-Type:${CONTENT_TYPE}T_TYPE")
    @POST("fcm/send")
    suspend fun postNotification(
        @Body notification: PushNotification
    ): Response<ResponseBody>
}