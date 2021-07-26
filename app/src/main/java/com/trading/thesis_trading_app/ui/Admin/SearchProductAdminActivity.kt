package com.trading.thesis_trading_app.ui.Admin

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.trading.thesis_trading_app.R
import com.trading.thesis_trading_app.ui.Authentication.BaseActivity
import com.trading.thesis_trading_app.utils.Constants
import kotlinx.android.synthetic.main.activity_search_product_admin.*
import java.util.*

class SearchProductAdminActivity : BaseActivity(), View.OnClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_product_admin)

        if (intent.hasExtra(Constants.PRODUCT_BARCODE_VALUE)) {
            et_search_product_barCode.setText(intent.getStringExtra(Constants.PRODUCT_BARCODE_VALUE)!!)
        }

        btn_search_product.setOnClickListener(this)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.product_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.qr_scanner -> {
                val intent = Intent(this, QRActivity::class.java)
                startActivity(intent)
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onClick(view: View?) {
        if (view != null) {
            when (view.id) {
                R.id.btn_search_product -> {
                    val productName = et_search_product_name.text.toString()
                    val productBarcode = et_search_product_barCode.text.toString()
                    val intent = Intent(this@SearchProductAdminActivity, ListProductsActivity::class.java)
                    if (productName.isEmpty()) {
                        if (productBarcode.isEmpty()) {
                            Toast.makeText(this, "No product name or barcode", Toast.LENGTH_LONG).show()
                        } else {
                            Log.e("Product Barcode", productBarcode)
                            intent.putExtra(Constants.SEARCH_PRODUCT_BARCODE, productBarcode)
                        }
                    } else {
                        intent.putExtra(Constants.SEARCH_PRODUCT_NAME,
                            productName.toLowerCase(Locale.ROOT))
                    }
                    startActivity(intent)
                    finish()
                }
            }
        }
    }

}