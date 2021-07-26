package com.trading.thesis_trading_app.ui.User

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.trading.thesis_trading_app.R
import com.trading.thesis_trading_app.utils.Constants
import kotlinx.android.synthetic.main.activity_search_product_user.*
import java.util.*

class SearchProductUserActivity : AppCompatActivity(), View.OnClickListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_product_user)

        btn_search_product_user.setOnClickListener(this)
    }

    override fun onClick(view: View?) {
        if (view != null) {
            when (view.id) {
                R.id.btn_search_product_user -> {
                    val productName = et_search_product_name_user.text.toString()
                    val intent = Intent(this@SearchProductUserActivity, UserActivity::class.java)
                    if (productName.isEmpty()) {
                            Toast.makeText(this, "Xin điền tên sản phẩm để tìm kiếm", Toast.LENGTH_LONG).show()
                    } else {
                        intent.putExtra(Constants.SEARCH_PRODUCT_NAME, productName.toLowerCase(Locale.ROOT))
                    }
                    startActivity(intent)
                    finish()
                }
            }
        }
    }
}