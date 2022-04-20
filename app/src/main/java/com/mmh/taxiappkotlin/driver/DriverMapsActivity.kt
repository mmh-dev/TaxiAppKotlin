package com.mmh.taxiappkotlin.driver

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.gson.Gson
import com.mmh.taxiappkotlin.App
import com.mmh.taxiappkotlin.R
import com.mmh.taxiappkotlin.api.RetrofitBuilder
import com.mmh.taxiappkotlin.databinding.ActivityDriverMapsBinding
import com.mmh.taxiappkotlin.entities.Order
import com.mmh.taxiappkotlin.entities.ServerResponse
import com.mmh.taxiappkotlin.utils.toast
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


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

        val mapFragment = supportFragmentManager.findFragmentById(R.id.driverMap) as SupportMapFragment
        mapFragment.getMapAsync(this)

        val json = intent.getStringExtra("selectedOrder")  // as String
        selectedOrder = Gson().fromJson(json, Order::class.java)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        buildLocationRequest()

        binding.exitBtnDriver.setOnClickListener {
            val editor = App.pref?.edit()
            editor?.clear()
            editor?.apply()
            toast("Good bye!")
            startActivity(Intent(this@DriverMapsActivity, DriverRegisterActivity::class.java))
        }


        binding.acceptRequest.setOnClickListener {
            selectedOrder.isTaken = true
            selectedOrder.driver = App.pref?.getString("userName", "")
//            selectedOrder.phone = App.pref?.getString("phone", "")
            val api = RetrofitBuilder.api.updateOrder(selectedOrder.objectId!!, selectedOrder)
            api.enqueue(object: Callback<ServerResponse>{
                override fun onResponse(
                    call: Call<ServerResponse>,
                    response: Response<ServerResponse>
                ) {
                    toast("Order is accepted by driver ${selectedOrder.driver}")
                    val uri = "google.navigation:q=" + selectedOrder.location?.latitude + "," + selectedOrder.location?.longitude + "&mode=d"
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
                    intent.setPackage("com.google.android.apps.maps")
                    startActivity(intent)
                }

                override fun onFailure(call: Call<ServerResponse>, t: Throwable) {
                }

            })

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
                    val bounds = builder.build()
                    val padding = 300
                    val cu = CameraUpdateFactory.newLatLngBounds(bounds, padding)
                    mMap.animateCamera(cu)
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