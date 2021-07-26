package com.trading.thesis_trading_app.ui.User

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.trading.thesis_trading_app.R
import com.trading.thesis_trading_app.firestore.FirestoreClass
import com.trading.thesis_trading_app.models.User
import com.trading.thesis_trading_app.ui.Authentication.BaseActivity
import com.trading.thesis_trading_app.utils.Constants
import com.trading.thesis_trading_app.utils.GlideLoader
import kotlinx.android.synthetic.main.activity_user_profile.*
import java.io.IOException

class UserProfileActivity : BaseActivity(), View.OnClickListener {

    private lateinit var mUserDetails: User
    private var mSeleactedImageFileUri: Uri? = null
    private var mUserProfileImageURL: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_profile)

        if (intent.hasExtra(Constants.EXTRA_USER_DETAILS)) {
            // Get the user details from intent as a ParcelableExtra
            mUserDetails = intent.getParcelableExtra(Constants.EXTRA_USER_DETAILS)!!
        }

        et_first_name.setText(mUserDetails.user_firstName)
        et_last_name.setText(mUserDetails.user_lastName)
        et_email.isEnabled = false
        et_email.setText(mUserDetails.user_email)

        if (mUserDetails.profileCompleted == 0) {
            tv_title.text = resources.getString(R.string.title_complete_profile)
            et_first_name.isEnabled = false

            et_last_name.isEnabled = false

        } else {
            setupActionBar()
            tv_title.text = resources.getString(R.string.title_complete_profile)
            GlideLoader(this@UserProfileActivity).loadUserPicture(mUserDetails.user_image, iv_user_photo)

            if (mUserDetails.user_mobile != 0L) {
                et_mobile_number.setText(mUserDetails.user_mobile.toString())
            }
            if (mUserDetails.user_address != "") {
                et_address.setText(mUserDetails.user_address)
            }
            if (mUserDetails.user_gender == Constants.MALE) {
                rb_male.isChecked = true
            } else {
                rb_female.isChecked = true
            }

        }

        iv_user_photo.setOnClickListener(this@UserProfileActivity)
        btn_summit.setOnClickListener(this@UserProfileActivity)
    }

    private fun setupActionBar() {

        setSupportActionBar(toolbar_user_profile_activity)

        val actionBar = supportActionBar
        if (actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
        }

        toolbar_user_profile_activity.setNavigationOnClickListener { onBackPressed() }
    }

    override fun onClick(v: View?) {
        if (v != null) {
            when (v.id) {
                R.id.iv_user_photo -> {
                    // Here we will check if the permission is already allowed or we need to request for it.
                    // First of all we will check the READ_EXTERNAL_STORAGE permission and if it is not allowed we show Error
                    if (ContextCompat.checkSelfPermission(
                            this,
                            Manifest.permission.READ_EXTERNAL_STORAGE
                        )
                    == PackageManager.PERMISSION_GRANTED
                    ) {
//                        showErrorSnackBar("You already have the storage permission.", false)
                        Constants.showImageChooser(this@UserProfileActivity)
                    } else {

                        /*Requests permissions to be granted to this application. These permissions
                        * must be requested in your manifest, they should not be granted to your app,
                        * and they should have protection level*/
                        ActivityCompat.requestPermissions(
                            this@UserProfileActivity,
                            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                            Constants.READ_STORAGE_PERMISSION_CODE
                        )
                    }
                }

                R.id.btn_summit -> {
                    if (validateUserProfileDetails()) {

                        showProgressDialog(resources.getString(R.string.please_wait))

                        if (mSeleactedImageFileUri != null) {
                            FirestoreClass().uploadImageToCloudStorage(this, mSeleactedImageFileUri)
                        } else {
                            updateUserProfileDetail()
                        }
                    }
                }
            }
        }
    }
// Important Functions
    private fun updateUserProfileDetail() {
        val userHashMap = HashMap<String, Any>()

        val firstName = et_first_name.text.toString().trim { it <= ' ' }
        if (firstName != mUserDetails.user_firstName) {
            userHashMap[Constants.USER_FIRST_NAME] = firstName
        }

        val lastName = et_last_name.text.toString().trim { it <= ' ' }
        if (lastName != mUserDetails.user_lastName) {
            userHashMap[Constants.USER_LAST_NAME] = lastName
        }

        val mobileNumber = et_mobile_number.text.toString().trim{ it <= ' '}
        val address = et_address.text.toString().trim { it <= ' '}
        val gender = if (rb_male.isChecked) {
            "Nam"
        } else {
            "Nữ"
        }

        if (mUserProfileImageURL.isNotEmpty()) {
            userHashMap[Constants.USER_IMAGE] = mUserProfileImageURL
        }
//      Can not use 0... here , need to fix
        if (mobileNumber.isNotEmpty() && mobileNumber != mUserDetails.user_mobile.toString()) {
            userHashMap[Constants.USER_MOBILE] = mobileNumber.toLong()
        }
        if (address.isNotEmpty() && address != mUserDetails.user_address) {
            userHashMap[Constants.USER_ADDRESS] = address
        }
        // key: gender   value: male/female
        if (gender.isNotEmpty() && gender != mUserDetails.user_gender) {
            userHashMap[Constants.USER_GENDER] = gender
        }

        userHashMap[Constants.COMLPLETE_PROFILE] = 1

        FirestoreClass().updateUserProfileData(this, userHashMap)
    }

    fun userProfileUpdateSuccess() {
        hideProgressDialog()

        Toast.makeText(this@UserProfileActivity,
        resources.getString(R.string.msg_profile_update_success),
        Toast.LENGTH_SHORT).show()

        startActivity(Intent(this@UserProfileActivity, UserActivity::class.java))
        finish()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == Constants.READ_STORAGE_PERMISSION_CODE) {
            // If permission is granted
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                showErrorSnackBar("The storage permission is granted.", false)

                Constants.showImageChooser(this@UserProfileActivity)
            } else {
                // Display another toast if permission is not granted
                Toast.makeText(this,
                resources.getString(R.string.read_storage_permission_denied),
                Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.e("imageData", requestCode.toString())
//        if (requestCode == 1) {
        if (requestCode == Constants.PICK_IMAGE_REQUEST_CODE) {
            if (data != null) {
                try {
                    mSeleactedImageFileUri = data.data!!
//                        iv_user_photo.setImageURI(selectedImageFileUri)
                    GlideLoader(this).loadUserPicture(mSeleactedImageFileUri!!, iv_user_photo)
                } catch (e: IOException) {
                    e.printStackTrace()
                    Toast.makeText(this@UserProfileActivity, resources.getString(R.string.image_selection_failed), Toast.LENGTH_SHORT).show()
                }
            }
        } else if (resultCode == Activity.RESULT_CANCELED) {
            Log.e("Request Cancelled", "Image selection cancelled")
        }
//        }
    }

    private fun validateUserProfileDetails(): Boolean {
        return when {
            TextUtils.isEmpty(et_mobile_number.text.toString().trim { it <= ' '}) -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_enter_mobile_number), true)
                false
            }
            else -> {
                true
            }
        }
    }

    fun imageUploadSuccess(imageURL: String) {
        mUserProfileImageURL = imageURL

        updateUserProfileDetail()
    }

}