package com.mmh.taxiappkotlin

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.mmh.taxiappkotlin.customer.CustomerMapsActivity
import com.mmh.taxiappkotlin.customer.CustomerRegisterActivity
import com.mmh.taxiappkotlin.databinding.ActivitySplashBinding
import com.mmh.taxiappkotlin.driver.DriverMapsActivity
import com.mmh.taxiappkotlin.driver.DriverRegisterActivity
import com.mmh.taxiappkotlin.driver.OrderList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashActivity : AppCompatActivity() {
    private val binding: ActivitySplashBinding by lazy {
        ActivitySplashBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        supportActionBar?.hide()


        CoroutineScope(Dispatchers.Main).launch {
            delay(5500L)
            binding.splashLottie.playAnimation()
            binding.splashLottie.speed = 1F

            App.pref = getSharedPreferences("pref", MODE_PRIVATE)
            when (App.pref?.getString("userType", "none") as String) {
                "customer" -> startActivity(Intent(this@SplashActivity, CustomerMapsActivity::class.java))
                "driver" -> startActivity(Intent(this@SplashActivity, OrderList::class.java))
                "none" -> startActivity(Intent(this@SplashActivity,CustomerRegisterActivity::class.java))
            }

        }
    }
}