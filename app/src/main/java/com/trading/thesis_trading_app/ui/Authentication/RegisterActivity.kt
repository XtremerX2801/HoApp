package com.trading.thesis_trading_app.ui.Authentication

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.Toast
import com.trading.thesis_trading_app.R
import com.trading.thesis_trading_app.firestore.FirestoreClass
import com.trading.thesis_trading_app.models.Admin
import com.trading.thesis_trading_app.models.User
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.activity_register.*

class RegisterActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

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

        setupActionBar()

        tv_login.setOnClickListener {
            // Launch login screen when user clicks Login
            val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
            startActivity(intent)
            // Can use OnBackPressed()
        }

        btn_register.setOnClickListener {
            registerUser()
        }
    }

    // Modify the actionBar
    private fun setupActionBar() {
        setSupportActionBar(toolbar_register_activity)

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_24dp)
        }

        toolbar_register_activity.setNavigationOnClickListener{ onBackPressed()}
    }

    // A function to validate the entries of new user
    private fun validateRegisterDetails(): Boolean {
        return when {
            TextUtils.isEmpty(et_first_name.text.toString().trim { it <= ' ' }) -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_enter_first_name), true)
                false
            }

            TextUtils.isEmpty(et_last_name.text.toString().trim { it <= ' ' }) -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_enter_last_name), true)
                false
            }

            TextUtils.isEmpty(et_email.text.toString().trim { it <= ' ' }) -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_enter_email), true)
                false
            }
            // Can add condition et_password.length() <= 8 before ->
            TextUtils.isEmpty(et_password.text.toString().trim { it <= ' ' }) -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_enter_password), true)
                false
            }

            TextUtils.isEmpty(et_confirm_password.text.toString().trim { it <= ' ' }) -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_enter_confirm_password), true)
                false
            }

            et_password.text.toString().trim { it <= ' '} != et_confirm_password.text.toString().trim{ it <= ' ' } -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_password_and_confirm_password_mismatch), true)
                false
            }
            !cb_terms_and_condition.isChecked -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_agree_terms_and_condition), true)
                false
            }
            else -> {
                true
            }
        }
    }

    private fun registerUser() {
        // Use validateRegisterDetails function to check if entries are valid or not
        if (validateRegisterDetails()) {

            showProgressDialog(resources.getString(R.string.please_wait))

            val email: String = et_email.text.toString().trim { it <= ' '}
            val password: String = et_password.text.toString().trim { it <= ' '}

            // Create an instance and create a register with email and password
            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password).addOnCompleteListener(
                OnCompleteListener<AuthResult> { task ->

                    if (task.isSuccessful) {
                        val firebaseUser: FirebaseUser = task.result!!.user!!

                        val userId = firebaseUser.uid
                        val user = User(
                            userId,
                            et_first_name.text.toString().trim { it <= ' '},
                            et_last_name.text.toString().trim { it <= ' '},
                            et_email.text.toString().trim { it <= ' '},
                            0,
                            0,
                            "",
                            ""
                        )

                        FirestoreClass().registerUser(this@RegisterActivity, user)

                        // Close RegisterActivity and go back to LoginActivity
//                        FirebaseAuth.getInstance().signOut()
//                        finish()

                    } else {
                        hideProgressDialog()
                        // If the registering not successful
                        showErrorSnackBar(task.exception!!.message.toString(), true)
                    }
                }
            )
        }
    }

    private fun registerAdmin() {
        // Use validateRegisterDetails function to check if entries are valid or not
        if (validateRegisterDetails()) {

            showProgressDialog(resources.getString(R.string.please_wait))

            val email: String = et_email.text.toString().trim { it <= ' '}
            val password: String = et_password.text.toString().trim { it <= ' '}

            // Create an instance and create a register with email and password
            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password).addOnCompleteListener(
                OnCompleteListener<AuthResult> { task ->

                    if (task.isSuccessful) {
                        val firebaseUser: FirebaseUser = task.result!!.user!!

                        val admin = Admin(
                            firebaseUser.uid,
                            et_first_name.text.toString().trim { it <= ' '},
                            et_last_name.text.toString().trim { it <= ' '},
                            et_email.text.toString().trim { it <= ' '}
                        )

                        FirestoreClass().registerAdmin(this@RegisterActivity, admin)

                        // Close RegisterActivity and go back to LoginActivity
//                        FirebaseAuth.getInstance().signOut()
//                        finish()

                    } else {
                        hideProgressDialog()
                        // If the registering not successful
                        showErrorSnackBar(task.exception!!.message.toString(), true)
                    }
                }
            )
        }
    }

    fun userRegistrationSuccess() {

        // Hide the progress dialog
        hideProgressDialog()

        Toast.makeText(
            this@RegisterActivity,
            resources.getString(R.string.register_successful),
            Toast.LENGTH_SHORT
        ).show()

    }

}