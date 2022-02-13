package com.example.liveattendanceapp.networking

object ApiServices {
    fun getLiveAttendaceServices(): LiveAttendanceApiServices{
        return RetrofitClient
            .getClient()
            .create(LiveAttendanceApiServices::class.java)
    }
}