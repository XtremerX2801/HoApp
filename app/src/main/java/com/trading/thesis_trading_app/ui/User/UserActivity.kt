package com.trading.thesis_trading_app.ui.User

import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.trading.thesis_trading_app.R
import com.trading.thesis_trading_app.ui.Authentication.BaseActivity
import com.trading.thesis_trading_app.utils.Constants
import com.google.android.material.bottomnavigation.BottomNavigationView

class UserActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        supportActionBar!!.setBackgroundDrawable(
            ContextCompat.getDrawable(
                this@UserActivity,
                R.drawable.app_gradient_color_background
            )
        )

        val productData = intent.hasExtra(Constants.PRODUCT_DATA)
        val searchData = intent.hasExtra((Constants.SEARCH_PRODUCT_NAME))

        val navView: BottomNavigationView = findViewById(R.id.nav_view)

        val navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_receipt, R.id.navigation_products, R.id.navigation_orders
            )
        )
        if (productData) {
            navController.navigate(R.id.navigation_orders)
        } else if (searchData) {
            navController.navigate(R.id.navigation_products)
        }

        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }

    override fun onBackPressed() {
        doubleBackToExit()
    }

}
