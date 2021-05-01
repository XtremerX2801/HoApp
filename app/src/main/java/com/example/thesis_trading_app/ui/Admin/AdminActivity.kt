package com.example.thesis_trading_app.ui.Admin

import android.content.Intent
import android.os.Bundle
import android.view.View
import com.example.thesis_trading_app.R
import com.example.thesis_trading_app.ui.Authentication.BaseActivity
import com.example.thesis_trading_app.ui.Authentication.LoginActivity
import com.example.thesis_trading_app.ui.TestOnly.MainActivity
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_admin.*

class AdminActivity : BaseActivity(), View.OnClickListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin)

        add_a_product.setOnClickListener(this)
        add_products.setOnClickListener(this)
        list_all_products.setOnClickListener(this)
        sales_dashboard.setOnClickListener(this)
        get_user_cart.setOnClickListener(this)
        log_out.setOnClickListener(this)
    }

    override fun onClick(view: View?) {
        if (view != null) {
            when (view.id) {
                R.id.add_a_product -> {
                    val intent = Intent(this@AdminActivity, AddProductActivity::class.java)
                    startActivity(intent)
                }
                R.id.add_products -> {
                    val intent = Intent(this@AdminActivity, MainActivity::class.java)
                    startActivity(intent)
                }
                R.id.list_all_products -> {
                    val intent = Intent(this@AdminActivity, ListProductsActivity::class.java)
                    startActivity(intent)
                }
                R.id.sales_dashboard -> {
                    val intent = Intent(this@AdminActivity, SalesDashboardActivity::class.java)
                    startActivity(intent)
                }
                R.id.get_user_cart -> {
                    val intent = Intent(this@AdminActivity, GetUserCartActivity::class.java)
                    startActivity(intent)
                }
                R.id.log_out -> {
                    FirebaseAuth.getInstance().signOut()
                    val intent = Intent(this@AdminActivity, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }
            }
        }
    }

}