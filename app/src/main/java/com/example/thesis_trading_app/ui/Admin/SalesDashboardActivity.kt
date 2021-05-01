package com.example.thesis_trading_app.ui.Admin

import android.content.Intent
import android.os.Bundle
import android.view.View
import com.example.thesis_trading_app.R
import com.example.thesis_trading_app.ui.Authentication.BaseActivity
import kotlinx.android.synthetic.main.activity_sales_dashboard.*

class SalesDashboardActivity : BaseActivity(), View.OnClickListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sales_dashboard)

        product_type1.setOnClickListener(this)
        product_type2.setOnClickListener(this)
    }

    override fun onClick(view: View?) {
        if (view != null){
            when (view.id) {
                R.id.product_type1 -> {
                    val intent = Intent(this, ProductType1Activity::class.java)
                    startActivity(intent)
                }
                R.id.product_type2 -> {
                    val intent = Intent(this, ProductType2Activity::class.java)
                    startActivity(intent)
                }
            }
        }
    }
}