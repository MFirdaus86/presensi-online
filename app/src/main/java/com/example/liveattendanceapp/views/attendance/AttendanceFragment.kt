package com.example.liveattendanceapp.views.attendance

import android.Manifest
import android.content.Context.LOCATION_SERVICE
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.example.liveattendanceapp.R
import com.example.liveattendanceapp.databinding.BottomSheetAttendanceBinding
import com.example.liveattendanceapp.databinding.FragmentAttendanceBinding
import com.example.liveattendanceapp.dialog.MyDialog
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.bottomsheet.BottomSheetBehavior

class AttendanceFragment : Fragment(), OnMapReadyCallback {

    companion object{
        private const val REQUEST_CODE_LOCATION = 2000
        private val TAG = AttendanceFragment::class.java.simpleName
    }

    private val mapPermissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    private var mapAttendance: SupportMapFragment ? = null
    private var map: GoogleMap ? = null
    private var binding: FragmentAttendanceBinding? = null
    private var bindingBottomSheet: BottomSheetAttendanceBinding? = null
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<ConstraintLayout>
    private var locationManager: LocationManager? = null
    private var locationRequest: LocationRequest? = null
    private var locationSettingRequest: LocationSettingsRequest? = null
    private var settingClient: SettingsClient?= null


    private var requestMultiplePermission = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ){
        permissions -> permissions.entries.forEach{
            val permissionName = it.key
            val isGranted = it.value
            if(isGranted){
                setupMaps()
            }else{
                val message = permissionName.toString() + "\n" + getString(R.string.not_granted)
                MyDialog.dynamicDialog(context, getString(R.string.require_permissions), message)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAttendanceBinding.inflate(inflater,container,false)
        bindingBottomSheet = binding?.layoutBottomSheet
        return binding?.root
    }


//    override fun onRequestPermissionsResult(
//        requestCode: Int,
//        permissions: Array<out String>,
//        grantResults: IntArray
//    ) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//        when(requestCode){
//            REQUEST_CODE_MAP_PERMISSIONS -> {
//                var isHasPermission = false
//                val permissionNotGranted = StringBuilder()
//
//                for(i in permissions.indices){
//                    isHasPermission = grantResults[i] == PackageManager.PERMISSION_GRANTED
//
//                    if(!isHasPermission){
//                        permissionNotGranted.append("${permissions[i]}\n")
//                    }
//                }
//
//                if(isHasPermission){
//                    setupMaps()
//                }else{
//                    val message = permissionNotGranted.toString() + "\n" + getString(R.string.not_granted)
//                    MyDialog.dynamicDialog(context, getString(R.string.require_permissions), message)
//                }
//            }
//        }
//    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
        setupMaps()
    }

    private fun init() {
        //setup location
        locationManager = context?.getSystemService(LOCATION_SERVICE) as LocationManager
        settingClient = LocationServices.getSettingsClient(requireContext())

        locationRequest = LocationRequest.create().apply {
            interval = 1000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest!!)
        locationSettingRequest = builder.build()

        //setup bottomsheet
        bottomSheetBehavior = BottomSheetBehavior.from(bindingBottomSheet!!.bottomSheetAttendance)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

    private fun setupMaps() {
        mapAttendance = childFragmentManager.findFragmentById(R.id.map_attendance) as SupportMapFragment
        mapAttendance?.getMapAsync(this)
    }

    override fun onMapReady(p0: GoogleMap) {
        map = p0
        if(checkPermission()){
            val sydney = LatLng(-6.974777105484707, 108.50041773499163)
            map?.moveCamera(CameraUpdateFactory.newLatLng(sydney))
            map?.animateCamera(CameraUpdateFactory.zoomTo(20f))

            goToCurrentLocation()
        }else{
            setRequestPermission()
        }

    }

    private fun setRequestPermission() {
//        requestPermissions(mapPermissions, REQUEST_CODE_MAP_PERMISSIONS)
        requestMultiplePermission.launch(mapPermissions)
    }

    private fun goToCurrentLocation() {
        if(checkPermission()){
            if(isLocationEnabled()){

            }else{
                goToTurnOnGps()
            }
        }else{
            setRequestPermission()
        }
    }

    private fun goToTurnOnGps() {
        settingClient?.checkLocationSettings(locationSettingRequest!!)
            ?.addOnSuccessListener {
                goToCurrentLocation()
            }?.addOnFailureListener {
                when((it as ApiException).statusCode){
                    LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> {
                        try{
                            val resolvableApiException = it as ResolvableApiException
                            resolvableApiException.startResolutionForResult(
                                requireActivity(),
                                REQUEST_CODE_LOCATION
                            )
                        }catch (ex: IntentSender.SendIntentException){
                            ex.printStackTrace()
                            Log.e(TAG, "Error: ${ex.message}")
                        }
                    }
                }
            }
    }

    private fun isLocationEnabled(): Boolean {
        if(locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER)!! ||
                locationManager?.isProviderEnabled(LocationManager.NETWORK_PROVIDER)!!){
            return true
        }

        return false
    }

    private fun checkPermission(): Boolean {
        var isHasPermission = false
        context?.let{
            for(permission in mapPermissions){
                isHasPermission = ActivityCompat.checkSelfPermission(it,permission) == PackageManager.PERMISSION_GRANTED
            }
        }
        return isHasPermission
    }


}
