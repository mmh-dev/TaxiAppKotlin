package com.mmh.taxiappkotlin.driver

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.gson.Gson
import com.mmh.taxiappkotlin.App
import com.mmh.taxiappkotlin.R
import com.mmh.taxiappkotlin.databinding.ActivityDriverMapsBinding
import com.mmh.taxiappkotlin.entities.Order


class DriverMapsActivity : AppCompatActivity(), OnMapReadyCallback, LocationListener {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityDriverMapsBinding
    var selectedOrder = Order()
    var fusedLocationProviderClient: FusedLocationProviderClient? = null
    var locationRequest: LocationRequest? = null
    var locationCallback: LocationCallback? = null
    var driverLocation: Location? = null

    override fun onBackPressed() {
        super.onBackPressed()
        finishAffinity()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDriverMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        val json = intent.getStringExtra("selectedOrder")  // as String
        selectedOrder = Gson().fromJson(json, Order::class.java)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        buildLocationRequest()

        binding.acceptRequest.setOnClickListener {
            selectedOrder.isTaken = true
            selectedOrder.driver = App.pref?.getString("userName", "")
            selectedOrder.phone = App.pref?.getString("phone", "")

        }

    }
    private fun buildLocationRequest() {
        locationRequest = LocationRequest()
        locationRequest?.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest?.interval = 100
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.mapType = GoogleMap.MAP_TYPE_NORMAL
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
            return
        }
        mMap.isMyLocationEnabled = true

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                super.onLocationResult(result)
                for (location in result.locations) {
                    driverLocation = location
                    mMap.clear()
                    val driverPosition = LatLng(location.latitude, location.longitude)
                    val customerPosition = LatLng(selectedOrder.location?.latitude!!, selectedOrder.location?.longitude!!)
                    mMap.addMarker(MarkerOptions().position(driverPosition).title("Your are here!").icon(
                        BitmapDescriptorFactory.fromResource(R.drawable.destination_marker)))
                    mMap.addMarker(MarkerOptions().position(customerPosition).title("Customer is here!").icon(BitmapDescriptorFactory.fromResource(R.drawable.marker)))
                    val builder = LatLngBounds.Builder()
                    builder.include(driverPosition)
                    builder.include(customerPosition)
                    val width = resources.displayMetrics.widthPixels
                    val height = resources.displayMetrics.heightPixels
                    mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), width, height, 300))
                }

            }
        }
        fusedLocationProviderClient?.requestLocationUpdates(locationRequest!!, locationCallback!!, Looper.getMainLooper())
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 5f, this)
            }
        }
    }

    override fun onLocationChanged(location: Location) {
        driverLocation = location
        mMap.clear()
        val driverPosition = LatLng(location.latitude, location.longitude)
        val customerPosition = LatLng(selectedOrder.location?.latitude!!, selectedOrder.location?.longitude!!)
        mMap.addMarker(MarkerOptions().position(driverPosition).title("Your are here!").icon(BitmapDescriptorFactory.fromResource(R.drawable.destination_marker)))
        mMap.addMarker(MarkerOptions().position(customerPosition).title("Customer is here!").icon(BitmapDescriptorFactory.fromResource(R.drawable.marker)))
    }
}