package com.trading.thesis_trading_app.ui.User.Payment

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.firebase.firestore.FirebaseFirestore
import com.google.maps.android.SphericalUtil
import com.trading.thesis_trading_app.R
import com.trading.thesis_trading_app.firestore.FirestoreClass
import com.trading.thesis_trading_app.ui.User.UserActivity
import com.trading.thesis_trading_app.utils.Constants
import kotlinx.android.synthetic.main.activity_online_order.*
import kotlinx.android.synthetic.main.activity_setting.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.HashMap

class OnlineOrderActivity : AppCompatActivity(), View.OnClickListener {

    private val userId = FirestoreClass().getCurrentUserId()
    private val PERMISSION_ID = 52
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    lateinit var locationRequest: LocationRequest

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_online_order)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        FirebaseFirestore.getInstance().collection(Constants.USERS).whereEqualTo(Constants.USER_ID, userId).get().addOnSuccessListener {
            for (document in it) {
                val userLocation = document.data[Constants.USER_LOCATION] as String
                val userTempLocation = document.data[Constants.TEMP_LOCATION] as String
                if (userTempLocation != "") {
                    val lat = userTempLocation.split("/")[0]
                    val long = userTempLocation.split("/")[1]
                    aoo_address.text = getName(lat.toDouble(), long.toDouble())
                }
                else if (userLocation != "") {
                    val lat = userLocation.split("/")[0]
                    val long = userLocation.split("/")[1]
                    aoo_address.text = getName(lat.toDouble(), long.toDouble())
                } else {
                    aoo_address.text = document.data[Constants.USER_ADDRESS] as String
                }
            }
        }

        btn_change_address.setOnClickListener(this)
        btn_pay_MoMo.setOnClickListener(this)
        btn_pay_home.setOnClickListener(this)
        btn_back.setOnClickListener(this)

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
                        aoo_address.text = getName(location.latitude, location.longitude)
                        val locationHashMap = HashMap<String, Any>()
                        locationHashMap[Constants.TEMP_LOCATION] = location.latitude.toString() + "/" + location.longitude.toString()
                        FirebaseFirestore.getInstance().collection(Constants.USERS).document(userId).update(locationHashMap).addOnSuccessListener {
                            Log.e("Temp location", "Success")
                        }.addOnFailureListener {
                            Log.e("Temp location", "Failed")
                        }
                        val intent = Intent(this, OnlineOrderActivity::class.java)
                        startActivity(intent)
                        finish()
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

            aoo_address.text = getName(lastLocation.latitude, lastLocation.longitude)

            val locationHashMap = HashMap<String, Any>()
            locationHashMap[Constants.TEMP_LOCATION] = lastLocation.latitude.toString() + "/" + lastLocation.longitude.toString()
            FirebaseFirestore.getInstance().collection(Constants.USERS).document(userId).update(locationHashMap).addOnSuccessListener {
                Log.e("Temp location", "Success")
            }.addOnFailureListener {
                Log.e("Temp location", "Failed")
            }
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
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER)
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

    @RequiresApi(Build.VERSION_CODES.O)
    private fun payHome() {
        FirebaseFirestore.getInstance().collection(Constants.USERS).whereEqualTo(Constants.USER_ID, userId).get().addOnSuccessListener { it0 ->
            for (document in it0) {
                var latLng = LatLng(0.0, 0.0)
                val userLocation = document.data[Constants.USER_LOCATION] as String
                val userTempLocation = document.data[Constants.TEMP_LOCATION] as String
                if (userTempLocation != "") {
                    val lat = userTempLocation.split("/")[0]
                    val long = userTempLocation.split("/")[1]
                    latLng = LatLng(lat.toDouble(), long.toDouble())
                } else if (userLocation != "") {
                    val lat = userLocation.split("/")[0]
                    val long = userLocation.split("/")[1]
                    latLng = LatLng(lat.toDouble(), long.toDouble())
                }

                FirebaseFirestore.getInstance().collection(Constants.STORE_LOCATION).document("ILCDI3fQZeZo88rTOEq2").get().addOnSuccessListener { it ->
                    val storeLats = it.data?.get(Constants.STORE_LATS) as MutableList<Double>
                    val storeLongs = it.data?.get(Constants.STORE_LONGS) as MutableList<Double>

                    var distance: Double
                    var nearest: Double = -1.0
                    var nearestStore = 0

                    for (index in 0 until storeLats.size) {
                        distance = SphericalUtil.computeDistanceBetween(latLng, LatLng(storeLats[index], storeLongs[index]))
                        if (nearest < 0.0) {
                            nearest = distance
                            nearestStore = index
                        } else if ((nearest > 0) && (nearest > distance)) {
                            nearest = distance
                            nearestStore = index
                        }
                    }

                    Log.e("Nearest Store", nearestStore.toString())

                    FirebaseFirestore.getInstance().collection(Constants.ONLINE_ORDER).whereEqualTo(Constants.STORE_ID, nearestStore.toLong()).get().addOnSuccessListener { it1 ->
                        for (document1 in it1) {
                            val userIds = document1.data[Constants.USER_IDS] as MutableList<String>
                            val storeProducts = document1.data[Constants.STORE_PRODUCTS] as MutableList<String>
                            val amounts = document1.data[Constants.AMOUNTS] as MutableList<String>
                            val prices = document1.data[Constants.PRICES] as MutableList<Long>
                            val storeDates = document1.data[Constants.STORE_DATES] as MutableList<String>

                            FirebaseFirestore.getInstance().collection(Constants.CARTS).whereEqualTo(Constants.USER_ID, userId).get().addOnSuccessListener { it2 ->
                                for (document2 in it2) {
                                    val cartProducts = document2.data[Constants.CART_PRODUCTS] as MutableList<Long>
                                    val cartProductAmount = document2.data[Constants.CART_PRODUCT_AMOUNT] as MutableList<Int>
                                    val cartPrice = document2.data[Constants.CART_PRICE] as Long

                                    var products = cartProducts[0].toString()
                                    var proAmounts = cartProductAmount[0].toString()
                                    for (a in 1 until cartProducts.size) {
                                        products += "," + cartProducts[a].toString()
                                        proAmounts += "," + cartProductAmount[a].toString()
                                    }

                                    val current = LocalDateTime.now()
                                    val formatter1 = DateTimeFormatter.BASIC_ISO_DATE
                                    val formatted1 = current.format(formatter1)
                                    val day = formatted1.substring(6, 8)
                                    val month = formatted1.substring(4, 6)
                                    val year = formatted1.substring(0, 4)
                                    val updateTime = "$day/$month/$year"

                                    storeProducts.add(products)
                                    amounts.add(proAmounts)
                                    prices.add(cartPrice)
                                    userIds.add(userId)
                                    storeDates.add(updateTime)

                                    val orderHashMap = HashMap<String, Any>()
                                    orderHashMap[Constants.STORE_PRODUCTS] = storeProducts
                                    orderHashMap[Constants.AMOUNTS] = amounts
                                    orderHashMap[Constants.PRICES] = prices
                                    orderHashMap[Constants.USER_IDS] = userIds
                                    orderHashMap[Constants.STORE_DATES] = storeDates

                                    FirebaseFirestore.getInstance().collection(Constants.ONLINE_ORDER).document(document1.id).update(orderHashMap).addOnSuccessListener {
                                        Log.e("Update store order", "Success")
                                    }.addOnFailureListener {
                                        Log.e("Update store order", "Failed")
                                    }

                                    val cartHashMap = HashMap<String, Any>()
                                    cartHashMap[Constants.CART_PRICE] = 0
                                    cartHashMap[Constants.CART_PRODUCT_NUM] = 0
                                    cartHashMap[Constants.CART_PRODUCTS] = mutableListOf<Long>()
                                    cartHashMap[Constants.CART_PRODUCT_AMOUNT] = mutableListOf<Int>()

                                    FirebaseFirestore.getInstance().collection(Constants.CARTS).document(document2.id).update(cartHashMap).addOnSuccessListener {
                                        Log.e("Cart update: ", "Successful")
                                    }.addOnFailureListener {
                                        Log.e("Cart update: ", "Failed")
                                    }
                                }
                            }

                            Toast.makeText(this, "Cảm ơn quý khách đã mua hàng", Toast.LENGTH_LONG).show()
                            val intent = Intent(this, UserActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                    }
                }

            }
        }
    }

    private fun payMoMo() {

    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onClick(view: View?) {
        if (view != null) {
            when (view.id) {
                R.id.btn_change_address -> {
                    getLastLocation()
                }
                R.id.btn_pay_MoMo -> {
                    payMoMo()
                }
                R.id.btn_pay_home -> {
                    payHome()
                }
                R.id.btn_back -> {
                    val intent = Intent(this, PaymentActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }
        }
    }
}