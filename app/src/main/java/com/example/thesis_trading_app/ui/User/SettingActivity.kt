package com.example.thesis_trading_app.ui.User

import android.content.Intent
import android.os.Bundle
import android.view.View
import com.example.thesis_trading_app.R
import com.example.thesis_trading_app.firestore.FirestoreClass
import com.example.thesis_trading_app.models.Admin
import com.example.thesis_trading_app.models.User
import com.example.thesis_trading_app.ui.Authentication.BaseActivity
import com.example.thesis_trading_app.ui.Authentication.LoginActivity
import com.example.thesis_trading_app.utils.Constants
import com.example.thesis_trading_app.utils.GlideLoader
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_setting.*

class SettingActivity : BaseActivity(), View.OnClickListener {

    private lateinit var mUserDetails: User
    private lateinit var mAdminDetails: Admin

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)
        setupActionBar()

        btn_logout.setOnClickListener(this)
        tv_edit.setOnClickListener(this)
    }

    private fun setupActionBar() {

        setSupportActionBar(toolbar_settings_activity)

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
        }

        toolbar_settings_activity.setNavigationOnClickListener {onBackPressed()}
    }

    private fun getUserDetails() {
        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().getUserDetails(this)
    }

    fun userDetailsSuccess(user: User) {
        mUserDetails = user

        hideProgressDialog()

        GlideLoader(this@SettingActivity).loadUserPicture(user.user_image, iv_user_photo)
        tv_name.text = "${user.user_firstName} ${user.user_lastName}"
        tv_gender.text = user.user_gender
        tv_email.text = user.user_email
        tv_mobile_number.text = "${user.user_mobile}"

    }

    fun adminDetailsSuccess(admin: Admin) {
        mAdminDetails = admin

        hideProgressDialog()

        GlideLoader(this@SettingActivity).loadUserPicture(admin.admin_image, iv_user_photo)
        tv_name.text = "${admin.admin_firstName} ${admin.admin_lastName}"
        tv_gender.text = admin.admin_gender
        tv_email.text = admin.admin_email
        tv_mobile_number.text = "${admin.admin_mobile}"

    }

    override fun onResume() {
        super.onResume()
        getUserDetails()
    }

    override fun onClick(v: View?) {
        if (v != null) {
            when (v.id) {

                R.id.tv_edit -> {
                    val intent = Intent(this@SettingActivity, UserProfileActivity::class.java)
                    intent.putExtra(Constants.EXTRA_USER_DETAILS, mUserDetails)
                    startActivity(intent)
                }

                R.id.btn_logout -> {
                    FirebaseAuth.getInstance().signOut()
                    val intent = Intent(this@SettingActivity, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }
            }
        }
    }

}