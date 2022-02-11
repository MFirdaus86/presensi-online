package com.example.liveattendanceapp.views.changepass

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.liveattendanceapp.R
import com.example.liveattendanceapp.databinding.ActivityChangePasswordBinding

class ChangePasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChangePasswordBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChangePasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()
        onClick()
    }

    private fun init(){
        setSupportActionBar(binding.tbChangePassword)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun onClick(){
        binding.tbChangePassword.setNavigationOnClickListener{
            finish()
        }
    }
}