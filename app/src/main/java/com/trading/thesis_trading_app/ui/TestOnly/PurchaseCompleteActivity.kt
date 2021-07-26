package com.trading.thesis_trading_app.ui.TestOnly

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.trading.thesis_trading_app.R
import com.trading.thesis_trading_app.ui.User.UserActivity
import kotlinx.android.synthetic.main.activity_purchase_complete.*

class PurchaseCompleteActivity : AppCompatActivity(), View.OnClickListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_purchase_complete)

        bought_complete.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        if (v != null) {
            when (v.id) {
                R.id.bought_complete -> {
                    val intent = Intent(this@PurchaseCompleteActivity, UserActivity::class.java)
                    startActivity(intent)
                }
            }
        }
    }
}