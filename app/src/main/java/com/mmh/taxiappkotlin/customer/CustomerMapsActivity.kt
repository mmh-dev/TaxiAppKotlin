package com.mmh.taxiappkotlin.customer

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
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
import com.google.android.gms.maps.model.MarkerOptions

import com.mmh.taxiappkotlin.App
import com.mmh.taxiappkotlin.R
import com.mmh.taxiappkotlin.api.RetrofitBuilder
import com.mmh.taxiappkotlin.databinding.ActivityCustomerMapsBinding
import com.mmh.taxiappkotlin.entities.CreateUserResponse
import com.mmh.taxiappkotlin.entities.GeoPoint
import com.mmh.taxiappkotlin.entities.Order
import com.mmh.taxiappkotlin.utils.toast
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class CustomerMapsActivity : AppCompatActivity(), OnMapReadyCallback, LocationListener {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityCustomerMapsBinding
    private var currentAddress: String? = null
    var fusedLocationProviderClient: FusedLocationProviderClient? = null
    var locationRequest: LocationRequest? = null
    var locationCallback: LocationCallback? = null
    var userLocation: Location? = null

    override fun onBackPressed() {
        super.onBackPressed()
        finishAffinity()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCustomerMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        buildLocationRequest()


        binding.exitBtn.setOnClickListener {
            val editor = App.pref?.edit()
            editor?.clear()
            editor?.apply()
            toast("Good bye!")
            startActivity(Intent(this@CustomerMapsActivity, CustomerRegisterActivity::class.java))
        }

        binding.getTaxi.setOnClickListener {
            val geoPoint = GeoPoint()
            geoPoint.latitude = userLocation?.latitude
            geoPoint.longitude = userLocation?.longitude
            val order = Order()
            order.location = geoPoint
            order.address = currentAddress
            order.username = App.pref?.getString("userName", "")
            order.phone = App.pref?.getString("phone", "")

            val api = RetrofitBuilder.api.createOrder(order)
            api.enqueue(object: Callback<CreateUserResponse>{
                override fun onResponse(call: Call<CreateUserResponse>, response: Response<CreateUserResponse>) {
                    if (response.isSuccessful) {
                        toast(getString(R.string.order_created))
                    }
                }

                override fun onFailure(call: Call<CreateUserResponse>, t: Throwable) {
                    TODO("Not yet implemented")
                }

            })
        }

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
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
                    userLocation = location
                    mMap.clear()
                    val position = LatLng(location.latitude, location.longitude)
                    mMap.addMarker(MarkerOptions().position(position).title("Your are here!").icon(BitmapDescriptorFactory.fromResource(R.drawable.marker)))
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(position, 13f))
                    val geocoder = Geocoder(this@CustomerMapsActivity, Locale.getDefault())
                    val addressList = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                    currentAddress = addressList[0].getAddressLine(0)
                    binding.address.text = currentAddress
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
        userLocation = location
        mMap.clear()
        val position = LatLng(location.latitude, location.longitude)
        mMap.addMarker(MarkerOptions().position(position).title("Your are here!").icon(BitmapDescriptorFactory.fromResource(R.drawable.marker)))
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(position, 13f))
        val geocoder = Geocoder(this, Locale.getDefault())
        val addressList = geocoder.getFromLocation(location.latitude, location.longitude, 1)
        currentAddress = addressList[0].getAddressLine(0) as String
        binding.address.text = currentAddress
    }
}