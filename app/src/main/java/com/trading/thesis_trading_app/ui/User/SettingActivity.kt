package com.trading.thesis_trading_app.ui.User

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.trading.thesis_trading_app.R
import com.trading.thesis_trading_app.firestore.FirestoreClass
import com.trading.thesis_trading_app.models.Admin
import com.trading.thesis_trading_app.models.User
import com.trading.thesis_trading_app.ui.Authentication.BaseActivity
import com.trading.thesis_trading_app.ui.Authentication.LoginActivity
import com.trading.thesis_trading_app.utils.Constants
import com.trading.thesis_trading_app.utils.GlideLoader
import kotlinx.android.synthetic.main.activity_setting.*
import java.util.*
import kotlin.collections.HashMap

class SettingActivity : BaseActivity(), View.OnClickListener {

    private lateinit var mUserDetails: User
    private lateinit var mAdminDetails: Admin
    private val PERMISSION_ID = 52
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    lateinit var locationRequest: LocationRequest
    private val userId = FirestoreClass().getCurrentUserId()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)
        setupActionBar()

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        btn_logout.setOnClickListener(this)
        tv_edit.setOnClickListener(this)
        btn_gps.setOnClickListener(this)
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
        tv_name.text = "Họ tên: ${user.user_firstName} ${user.user_lastName}"
        tv_gender.text = "Giới tính: " + user.user_gender
        tv_email.text = "Email: " + user.user_email
        tv_mobile_number.text = "Số điện thoại: ${user.user_mobile}"
        tv_coupon.text = "Coupon: " + user.user_coupon
        tv_address.text = "Địa chỉ: " + user.user_address
        if (user.user_location == "") {
            tv_your_location.text = "Nhấn nút để nhập địa chỉ hiện tại"
        } else {
            tv_your_location.text = user.user_location
        }

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

    private fun getName(lat: Double, long: Double): String {
        val geoCoder = Geocoder(this, Locale.getDefault())
        val address = geoCoder.getFromLocation(lat, long, 1)

        return address[0].getAddressLine(0)
    }

    private fun getLastLocation() {
        if (checkPermission()) {
            if (isLocationEnabled()) {
                fusedLocationProviderClient.lastLocation.addOnCompleteListener { task ->
                    val location: Location? = task.result
                    if (location == null) {
                        getNewLocation()
                    } else {
                        tv_your_location.text = getName(location.latitude, location.longitude)

                        FirebaseFirestore.getInstance().collection(Constants.USERS).whereEqualTo(Constants.USER_ID, userId).get().addOnSuccessListener {
                            for (document in it) {
                                val yourLocation = location.latitude.toString() + "/" + location.longitude.toString()
                                val userHashMap = HashMap<String, Any>()
                                userHashMap[Constants.USER_LOCATION] = yourLocation
                                FirebaseFirestore.getInstance().collection(Constants.USERS).document(document.id).update(userHashMap).addOnSuccessListener {
                                    Log.e("Update location", "Success")
                                }.addOnFailureListener {
                                    Log.e("Update Location", "Failed")
                                }
                            }
                        }
                    }
                }
            } else {
                Toast.makeText(this, "Hãy bật GPS", Toast.LENGTH_LONG).show()
            }
        } else {
            requestPermission()
        }
    }

    private fun getNewLocation() {
        locationRequest = LocationRequest()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 0
        locationRequest.fastestInterval = 0
        locationRequest.numUpdates = 2
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest, locationCallback, Looper.myLooper()
        )
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(p0: LocationResult) {
            val lastLocation: Location = p0.lastLocation

            tv_your_location.text = getName(lastLocation.latitude, lastLocation.longitude)
        }
    }

    private fun checkPermission(): Boolean {
        if ((ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) ||
                (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)) {
            return true
        }
        return false
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
            PERMISSION_ID
        )
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == PERMISSION_ID) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.e("Debug", "Permission Success")
            }
        }
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
                R.id.btn_gps -> {
                    getLastLocation()
                }
            }
        }
    }

}