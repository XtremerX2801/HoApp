package com.example.thesis_trading_app.ui.Authentication

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import com.example.thesis_trading_app.R
import com.example.thesis_trading_app.firestore.FirestoreClass
import com.example.thesis_trading_app.models.Admin
import com.example.thesis_trading_app.models.User
import com.example.thesis_trading_app.ui.Admin.AdminActivity
import com.example.thesis_trading_app.ui.User.DashboardActivity
import com.example.thesis_trading_app.ui.User.UserProfileActivity
import com.example.thesis_trading_app.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : BaseActivity(), View.OnClickListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Hide statusBar
        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }

        tv_forgot_password.setOnClickListener(this)
        btb_login.setOnClickListener(this)
        tv_register.setOnClickListener(this)

    }

    fun userLoggedInSuccess(user: User) {
        // Hide the progress dialog
        hideProgressDialog()

        if (user.profileCompleted == 0) {
            // If the user profile is incomplete then launch the UserProfileActivity.
            val intent = Intent(this@LoginActivity, UserProfileActivity::class.java)
            intent.putExtra(Constants.EXTRA_USER_DETAILS, user)
            startActivity(intent)
            finish()
        } else {
            val intent = Intent(this@LoginActivity, DashboardActivity::class.java)
            intent.putExtra(Constants.EXTRA_USER_DETAILS, user.user_id)
            startActivity(intent)
            finish()
        }
    }

    fun adminLoggedInSuccess(admin: Admin) {
        hideProgressDialog()

        val intent = Intent(this@LoginActivity, AdminActivity::class.java)
        intent.putExtra(Constants.EXTRA_USER_DETAILS, admin)
        startActivity(intent)
        finish()
    }

    override fun onClick(view: View?) {
        if (view != null) {
            when (view.id) {

                R.id.tv_forgot_password -> {
                    val intent = Intent(this@LoginActivity, ForgotPasswordActivity::class.java)
                    startActivity(intent)
                }

                R.id.btb_login -> {
                    login()
                }

                R.id.tv_register -> {
                    val intent = Intent(this@LoginActivity, RegisterActivity::class.java)
                    startActivity(intent)
                }

            }
        }
    }

    private fun validateLoginDetails(): Boolean {
        return when {
            TextUtils.isEmpty(et_email.text.toString().trim { it <= ' ' }) -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_enter_email), true)
                false
            }
            TextUtils.isEmpty(et_password.text.toString().trim { it <= ' ' }) -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_enter_password), true)
                false
            }
            else -> {
                true
            }
        }
    }

    private fun login() {
        if (validateLoginDetails()) {
            showProgressDialog(resources.getString(R.string.please_wait))

            val email = et_email.text.toString().trim { it <= ' ' }
            val password = et_password.text.toString().trim { it <= ' '}

            // Login with email and password
            FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password).addOnCompleteListener{  task ->

                if (task.isSuccessful) {
                    Log.e("Email", email)
                    ///////////////////////////////// HERE THE DIFFERENT
                    if (email == "vdho123@gmail.com") {
                        FirestoreClass().getAdminDetails(this@LoginActivity)
                    } else {
                        FirestoreClass().getUserDetails(this@LoginActivity)
                    }
                } else {
                    hideProgressDialog()
                    showErrorSnackBar(task.exception!!.message.toString(), true)
                }
            }
        }
    }

}