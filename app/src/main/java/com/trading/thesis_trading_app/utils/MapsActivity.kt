package com.trading.thesis_trading_app.utils

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.trading.thesis_trading_app.R
import com.trading.thesis_trading_app.databinding.ActivityMapsBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Add a marker in Sydney and move the camera
        val sydney = LatLng(-34.0, 151.0)
        val store1 = LatLng(10.7616512, 106.6660773)
        val store2 = LatLng(10.720324, 106.7090996)
        val store3 = LatLng(10.8039918, 106.7128706)
        val store4 = LatLng(10.730656, 106.6868926)
        val store5 = LatLng(10.7607969, 106.6477348)

        var location: LatLng = sydney
            if (intent.hasExtra("Store")) {
            location = when (intent.getStringExtra("Store")) {
                "Cửa hàng 1" -> store1
                "Cửa hàng 2" -> store2
                "Cửa hàng 3" -> store3
                "Cửa hàng 4" -> store4
                "Cửa hàng 5" -> store5
                else -> sydney
            }
        } else if (intent.hasExtra("Self")) {
            location = intent.getParcelableExtra("Self")!!
        }

        mMap.addMarker(MarkerOptions().position(location).title("Marker"))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 20f))
    }
}