package com.mmh.taxiappkotlin.driver

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import com.mmh.taxiappkotlin.App
import com.mmh.taxiappkotlin.api.MapsRetrofitBuilder
import com.mmh.taxiappkotlin.api.RetrofitBuilder
import com.mmh.taxiappkotlin.customer.CustomerRegisterActivity
import com.mmh.taxiappkotlin.databinding.ActivityOrderListBinding
import com.mmh.taxiappkotlin.entities.GetOrderResponse
import com.mmh.taxiappkotlin.entities.MapsResponse
import com.mmh.taxiappkotlin.entities.Order
import com.mmh.taxiappkotlin.utils.API_KEY
import com.mmh.taxiappkotlin.utils.MAPS_URL
import com.mmh.taxiappkotlin.utils.toast
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.StringBuilder

class OrderList : AppCompatActivity(), LocationListener {

    var adapter: ArrayAdapter<String>? = null
    private var requestList: ArrayList<String> = ArrayList()
    private var orderList: ArrayList<Order> = ArrayList()
    var driverLocation = Location("")
    var userLocation = Location("")
    var fusedLocationProviderClient: FusedLocationProviderClient? = null
    var locationRequest: LocationRequest? = null
    var locationCallback: LocationCallback? = null
    var distance: String = ""

    private val binding: ActivityOrderListBinding by lazy {
        ActivityOrderListBinding.inflate(layoutInflater)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finishAffinity()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
            return
        }

        binding.apply {
            exitBtn.setOnClickListener {
                val editor = App.pref?.edit()
                editor?.clear()
                editor?.apply()
                toast("Good bye!")
                startActivity(Intent(this@OrderList, DriverRegisterActivity::class.java))
            }
        }

        val api = RetrofitBuilder.api.getOrders()
        api.enqueue(object : Callback<GetOrderResponse> {
            override fun onResponse(call: Call<GetOrderResponse>, response: Response<GetOrderResponse>) {
                if (response.isSuccessful) {
                    orderList.clear()
                    orderList.addAll(response.body()?.orderList!!)
                }
            }

            override fun onFailure(call: Call<GetOrderResponse>, t: Throwable) {
                toast("order request is failed")
            }
        })

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this@OrderList)
        buildLocationRequest()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                super.onLocationResult(result)
                for (location in result.locations) {
                    driverLocation = location
                    requestList.clear()
                    for (order in orderList) {
                        userLocation.latitude = order.location?.latitude!!
                        userLocation.longitude = order.location?.longitude!!
//                        val distance = (driverLocation.distanceTo(userLocation) / 1000).toFloat()
//                        val distanceF = String.format("%.02f", distance) as String
//                        requestList.add("$distanceF км")
                        getNavigationDistance()
                        val str = "$distance км"
                        requestList.add(str)
                    }
                    adapter = ArrayAdapter(this@OrderList, android.R.layout.simple_list_item_activated_1, requestList)
                    binding.orderListView.adapter = adapter
                }

            }
        }
        fusedLocationProviderClient?.requestLocationUpdates(locationRequest!!, locationCallback!!, Looper.getMainLooper())

        binding.orderListView.setOnItemClickListener { adapterView, itemView, position, id ->
            val selectedOrder = orderList[position]
            val intent = Intent(this@OrderList, DriverMapsActivity::class.java)
            val json = Gson().toJson(selectedOrder)
            intent.putExtra("selectedOrder", json)
            startActivity(intent)
        }
    }
    private fun buildLocationRequest() {
        locationRequest = LocationRequest()
        locationRequest?.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest?.interval = 100
    }
    
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if ((requestCode == 1 && grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) &&
            (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)) {
            val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 5f, this)
        }
    }

    override fun onLocationChanged(location: Location) {
        driverLocation = location
    }

    fun getNavigationDistance() {
        val api = MapsRetrofitBuilder.mapsApi.getRoute(
            "46.482525, 30.723309", "42.027973, 35.151726", API_KEY, true, "ru", "ferries")
        api.enqueue(object : Callback<MapsResponse> {
            override fun onResponse(call: Call<MapsResponse>, response: Response<MapsResponse>) {
                if (response.isSuccessful) {
                    distance = response.body()!!.routes[0].legs[0].distance.text as String
                }
            }

            override fun onFailure(call: Call<MapsResponse>, t: Throwable) {
                toast("order request is failed")
            }
        })
    }
}