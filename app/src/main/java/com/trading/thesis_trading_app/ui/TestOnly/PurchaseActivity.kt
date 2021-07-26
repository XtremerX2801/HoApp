package com.trading.thesis_trading_app.ui.TestOnly

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.trading.thesis_trading_app.R
import kotlinx.android.synthetic.main.activity_purchase.*

class PurchaseActivity : AppCompatActivity(), View.OnClickListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_purchase)
        bought.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        if (v != null) {
            when (v.id) {
                R.id.bought -> {
                    val intent = Intent(this@PurchaseActivity, PurchaseCompleteActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }
        }
    }
}