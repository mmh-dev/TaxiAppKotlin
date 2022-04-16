package com.mmh.taxiappkotlin.driver

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import com.mmh.taxiappkotlin.App
import com.mmh.taxiappkotlin.R
import com.mmh.taxiappkotlin.api.RetrofitBuilder
import com.mmh.taxiappkotlin.customer.CustomerRegisterActivity
import com.mmh.taxiappkotlin.databinding.ActivityDriverRegisterBinding
import com.mmh.taxiappkotlin.entities.CreateUserResponse
import com.mmh.taxiappkotlin.entities.SignInResponse
import com.mmh.taxiappkotlin.entities.User
import com.mmh.taxiappkotlin.utils.toast
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DriverRegisterActivity : AppCompatActivity() {

    private val binding: ActivityDriverRegisterBinding by lazy {
        ActivityDriverRegisterBinding.inflate(layoutInflater)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finishAffinity()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.apply {
            customerAppLink.setOnClickListener {
                startActivity(Intent(this@DriverRegisterActivity, CustomerRegisterActivity::class.java))
            }
            signUp.setOnClickListener {
                val builder = AlertDialog.Builder(this@DriverRegisterActivity)
                val inflater = layoutInflater
                val dialogLayout = inflater.inflate(R.layout.dialog_layout, null)
                val email: EditText = dialogLayout.findViewById(R.id.email)
                val username: EditText = dialogLayout.findViewById(R.id.username)
                val phone: EditText = dialogLayout.findViewById(R.id.phone)
                val password: EditText = dialogLayout.findViewById(R.id.password)
                val car: EditText = dialogLayout.findViewById(R.id.car)
                with(builder) {
                    setTitle(R.string.sign_up)
                    setMessage(R.string.please_use_email_to_register)
                    setPositiveButton("Register") { dialog, which ->
                        val user = User()
                        user.email = email.text.toString().trim()
                        user.phone = phone.text.toString().trim()
                        user.username = username.text.toString().trim()
                        user.password = password.text.toString().trim()
                        user.userType = "driver"
                        user.car = car.text.toString().trim()
                        val api = RetrofitBuilder.api.createUser(user)
                        api.enqueue(object: Callback<CreateUserResponse> {
                            override fun onResponse(
                                call: Call<CreateUserResponse>,
                                response: Response<CreateUserResponse>
                            ) {
                                if (response.isSuccessful) {
                                    toast("New user ${user.username} is created!")
                                    App.pref = getSharedPreferences("pref", MODE_PRIVATE)
                                    val editor = App.pref?.edit()
                                    editor?.putString("userType", "driver")
                                    editor?.putString("userName", user.username)
                                    editor?.putString("phone", user.phone)
                                    editor?.apply()
                                    startActivity(Intent(this@DriverRegisterActivity, OrderList::class.java))
                                }
                            }

                            override fun onFailure(call: Call<CreateUserResponse>, t: Throwable) {
                                Log.d("fail", t.message.toString())
                            }

                        })
                    }
                    setNegativeButton("Cancel") {dialog, which ->
                        dialog.dismiss()
                    }
                    setView(dialogLayout).create().show()
                }
            }
            signIn.setOnClickListener {
                val builder = AlertDialog.Builder(this@DriverRegisterActivity)
                val inflater = layoutInflater
                val dialogLayout = inflater.inflate(R.layout.dialog_layout, null)
                val userName: EditText = dialogLayout.findViewById(R.id.username)
                val password: EditText = dialogLayout.findViewById(R.id.password)
                dialogLayout.findViewById<EditText>(R.id.email).visibility = View.GONE
                dialogLayout.findViewById<EditText>(R.id.phone).visibility = View.GONE
                dialogLayout.findViewById<EditText>(R.id.car).visibility = View.GONE
                with(builder) {
                    setTitle(R.string.sign_in)
                    setMessage(getString(R.string.sign_in_message))
                    setPositiveButton("Sign In") { dialog, which ->
                        val username = userName.text.toString().trim()
                        val password = password.text.toString().trim()
                        val api = RetrofitBuilder.api.signIn(username, password)
                        api.enqueue(object: Callback<SignInResponse> {
                            override fun onResponse(
                                call: Call<SignInResponse>,
                                response: Response<SignInResponse>
                            ) {
                                if (response.isSuccessful) {
                                    toast("Welcome, ${username}!")
                                    App.pref = getSharedPreferences("pref", MODE_PRIVATE)
                                    val editor = App.pref?.edit()
                                    editor?.putString("userType", "driver")
                                    editor?.putString("userName", response.body()?.username)
                                    editor?.putString("phone", response.body()?.phone)
                                    editor?.apply()
                                    startActivity(Intent(this@DriverRegisterActivity, OrderList::class.java))
                                }
                            }

                            override fun onFailure(call: Call<SignInResponse>, t: Throwable) {
                                Log.d("fail", t.message.toString())
                            }

                        })
                    }
                    setNegativeButton("Cancel") {dialog, which ->
                        dialog.dismiss()
                    }
                    setView(dialogLayout).create().show()
                }
            }
        }

    }
}