package com.trading.thesis_trading_app.firestore

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.trading.thesis_trading_app.models.Admin
import com.trading.thesis_trading_app.models.User
import com.trading.thesis_trading_app.ui.Admin.AddProductActivity
import com.trading.thesis_trading_app.ui.Admin.ModifyProductActivity
import com.trading.thesis_trading_app.ui.Authentication.LoginActivity
import com.trading.thesis_trading_app.ui.Authentication.RegisterActivity
import com.trading.thesis_trading_app.ui.User.SettingActivity
import com.trading.thesis_trading_app.ui.User.UserProfileActivity
import com.trading.thesis_trading_app.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class FirestoreClass {

    private val mFireStore = FirebaseFirestore.getInstance()

    // Create collection using entity in ERD

    fun registerUser(activity: RegisterActivity, userInfo: User) {
        // The "users" is collection name. If the collection is already created then it will not create the same object
        mFireStore.collection(Constants.USERS)
            // Document ID for users fields. Here the document it is the User ID.
            .document(userInfo.user_id)
            // Here the userInfo are Field and the SetOption is set to merge. It is for if we wants to merge later
            .set(userInfo, SetOptions.merge())
            .addOnSuccessListener {
                // Here call a function of base activity for transferring the result to it
                activity.userRegistrationSuccess()
        }
            .addOnFailureListener { e ->
                activity.hideProgressDialog()
                Log.e(
                    activity.javaClass.simpleName,
                    "Error while registering the user.",
                    e
                )
            }

        val cartHashMap = HashMap<String, Any>()
        cartHashMap[Constants.USER_ID] = userInfo.user_id
        cartHashMap[Constants.CART_PRODUCT_NUM] = 0
        cartHashMap[Constants.CART_PRICE] = 0
        cartHashMap[Constants.CART_PRODUCTS] = mutableListOf<Long>()
        cartHashMap[Constants.CART_PRODUCT_AMOUNT] = mutableListOf<Int>()
        mFireStore.collection(Constants.CARTS).add(cartHashMap)

        val receiptHashMap = HashMap<String, Any>()
        receiptHashMap[Constants.USER_ID] = userInfo.user_id
        receiptHashMap[Constants.RECEIPT_DATES] = mutableListOf<String>()
        receiptHashMap[Constants.RECEIPT_PRICES] = mutableListOf<Long>()
        receiptHashMap[Constants.RECEIPT_PRODUCT_AMOUNT] = mutableListOf<String>()
        receiptHashMap[Constants.RECEIPT_PRODUCTS] = mutableListOf<String>()
        receiptHashMap[Constants.RECEIPT_TOTAL_PRODUCTS] = mutableListOf<Int>()
        mFireStore.collection(Constants.RECEIPTS).add(receiptHashMap)

        FirebaseFirestore.getInstance().collection(Constants.TOTAL_POINT).document("5Tw5en3twZNtk7tk5bKD").get().addOnSuccessListener {
            val point = it.data?.get(Constants.POINTS_LIST) as MutableList<Long>
            val users = it.data?.get(Constants.USERS_LIST) as MutableList<String>
            point.add(0)
            users.add(userInfo.user_id)

            val totalPointHashMap = HashMap<String, Any>()
            totalPointHashMap[Constants.POINTS_LIST] = point
            totalPointHashMap[Constants.USERS_LIST] = users
            FirebaseFirestore.getInstance().collection(Constants.TOTAL_POINT).document("5Tw5en3twZNtk7tk5bKD").update(totalPointHashMap).addOnSuccessListener {
                Log.e("Register", "Success")
            }.addOnFailureListener {
                Log.e("Register", "Failed")
            }
        }

    }

    fun registerAdmin(activity: RegisterActivity, adminInfo: Admin) {
        // The "users" is collection name. If the collection is already created then it will not create the same object
        mFireStore.collection(Constants.ADMIN)
            // Document ID for users fields. Here the document it is the User ID.
            .document(adminInfo.admin_id)
            // Here the userInfo are Field and the SetOption is set to merge. It is for if we wants to merge later
            .set(adminInfo, SetOptions.merge())
            .addOnSuccessListener {
                // Here call a function of base activity for transferring the result to it
                activity.userRegistrationSuccess()
            }
            .addOnFailureListener { e ->
                activity.hideProgressDialog()
                Log.e(
                    activity.javaClass.simpleName,
                    "Error while registering the admin.",
                    e
                )
            }
    }

    fun getCurrentUserId(): String {
        // An instance of currentUser using FirebaseAuth
        val currentUser = FirebaseAuth.getInstance().currentUser

        // A variable to assign the currentUserId if it is not null or else it will be blank
        var currentUserID = ""
        if (currentUser != null) {
            currentUserID = currentUser.uid
        }
        Log.e("ID", currentUserID)
        return currentUserID
    }

    fun getUserDetails(activity: Activity) {

        // Here we pass the collection name from which we wants the data
        mFireStore.collection(Constants.USERS)
            // The document id to get the Fields of user
            .document(getCurrentUserId()).get().addOnSuccessListener { document ->
                Log.i(activity.javaClass.simpleName, document.toString())

                // Here we have received the document snapshot which is converted into the User Data model object
                val user = document.toObject(User::class.java)!!

                val sharedPreferences = activity.getSharedPreferences(
                    Constants.MYAPP_PREFERENCES,
                    Context.MODE_PRIVATE
                )

                val editor: SharedPreferences.Editor = sharedPreferences.edit()
                // Key: Value         logged_in_username: Ho Vo
                editor.putString(
                    Constants.LOGGED_IN_USERNAME,
                    "${user.user_firstName} ${user.user_lastName}"
                )
                editor.apply()

                // START
                when (activity) {
                    is LoginActivity -> {
                        // Call a function of base activity for transferring the result to it
                        activity.userLoggedInSuccess(user)
                    }

                    is SettingActivity -> {
                        activity.userDetailsSuccess(user)
                    }
                }
                // END
            }.addOnFailureListener { e ->
                // Hide the progress dialog if there is any error. And print the error in log
                when (activity) {
                    is LoginActivity -> {
                        activity.hideProgressDialog()
                    }

                    is SettingActivity -> {
                        activity.hideProgressDialog()
                    }
                }

                Log.e(
                    activity.javaClass.simpleName,
                    "Error while getting user details.",
                    e
                )
            }
    }

    fun getAdminDetails(activity: Activity) {

        // Here we pass the collection name from which we wants the data
        mFireStore.collection(Constants.ADMIN)
            // The document id to get the Fields of user
            .document(getCurrentUserId()).get().addOnSuccessListener { document ->
                Log.i(activity.javaClass.simpleName, document.toString())

                // Here we have received the document snapshot which is converted into the Admin Data model object
                val admin = document.toObject(Admin::class.java)!!

                val sharedPreferences = activity.getSharedPreferences(
                    Constants.MYAPP_PREFERENCES,
                    Context.MODE_PRIVATE
                )

                val editor: SharedPreferences.Editor = sharedPreferences.edit()
                // Key: Value         logged_in_username: Ho Vo
                editor.putString(
                    Constants.LOGGED_IN_USERNAME,
                    "${admin.admin_firstName} ${admin.admin_lastName}"
                )
                editor.apply()
                Log.e("Admin acc: ", "${admin.admin_firstName} ${admin.admin_lastName}")

                // START
                when (activity) {
                    is LoginActivity -> {
                        // Call a function of base activity for transferring the result to it
                        activity.adminLoggedInSuccess(admin)
                    }

                    is SettingActivity -> {
                        activity.adminDetailsSuccess(admin)
                    }
                }
                // END
            }.addOnFailureListener { e ->
                // Hide the progress dialog if there is any error. And print the error in log
                when (activity) {
                    is LoginActivity -> {
                        activity.hideProgressDialog()
                    }

                    is SettingActivity -> {
                        activity.hideProgressDialog()
                    }
                }

                Log.e(
                    activity.javaClass.simpleName,
                    "Error while getting user details.",
                    e
                )
            }
    }

    fun updateUserProfileData(activity: Activity, userHashMap: HashMap<String, Any>) {

        mFireStore.collection(Constants.USERS)
            .document(getCurrentUserId())
            .update(userHashMap)
            .addOnSuccessListener {
                when (activity) {
                    is UserProfileActivity -> {
                        activity.userProfileUpdateSuccess()
                    }
                }
            }
            .addOnFailureListener { e ->
                when (activity) {
                    is UserProfileActivity -> {
                        activity.hideProgressDialog()
                    }
                }

                Log.e(activity.javaClass.simpleName, "Error while updating the user details.", e)
            }
    }

    fun uploadImageToCloudStorage(activity: Activity, imageFileURI: Uri?) {
        val sRef: StorageReference = FirebaseStorage.getInstance().reference.child(
            Constants.USER_PROFILE_IMAGE + System.currentTimeMillis() + "." + Constants.getFileExtension(
                activity,
                imageFileURI
            )
        )

        sRef.putFile(imageFileURI!!).addOnSuccessListener { taskSnapshot ->
            Log.e("Firebase Image URL", taskSnapshot.metadata!!.reference!!.downloadUrl.toString())

            taskSnapshot.metadata!!.reference!!.downloadUrl.addOnSuccessListener { uri ->
                Log.e("Downloadable Image URL", uri.toString())

                when (activity) {
                    is UserProfileActivity -> {
                        activity.imageUploadSuccess(uri.toString())
                    }

                }
            }
        }
            .addOnFailureListener { exception ->
                when (activity) {
                    is UserProfileActivity -> {
                        activity.hideProgressDialog()
                    }
                }

                Log.e(activity.javaClass.simpleName,
                exception.message,
                exception)
            }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun uploadProductImageToCloudStorage(activity: Activity, imageFileURI: Uri?) {
        val sRef: StorageReference = FirebaseStorage.getInstance().reference.child(
            Constants.PRODUCT_IMAGE + System.currentTimeMillis() + "." + Constants.getFileExtension(
                activity,
                imageFileURI
            )
        )

        sRef.putFile(imageFileURI!!).addOnSuccessListener { taskSnapshot ->
            Log.e("Firebase Image URL", taskSnapshot.metadata!!.reference!!.downloadUrl.toString())

            taskSnapshot.metadata!!.reference!!.downloadUrl.addOnSuccessListener { uri ->
                Log.e("Downloadable Image URL", uri.toString())

                when (activity) {
                    is AddProductActivity -> {
                        activity.productImageUploadSuccess(uri.toString())
                    }
                    is ModifyProductActivity -> {
                        activity.productImageUploadSuccess(uri.toString())
                    }
                }
            }
        }
            .addOnFailureListener { exception ->
                when (activity) {
                    is AddProductActivity -> {
                        activity.hideProgressDialog()
                    }
                    is ModifyProductActivity -> {
                        activity.hideProgressDialog()
                    }
                }

                Log.e(activity.javaClass.simpleName,
                    exception.message,
                    exception)
            }
    }

}