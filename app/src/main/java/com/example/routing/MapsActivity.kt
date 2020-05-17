package com.example.routing

import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import com.example.routing.databinding.ActivityMapsBinding

import com.example.routing.utils.MarkerPosition
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions


class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private val TAG = MapsActivity::class.java.simpleName
    private var currentLocation: Location? = null
    internal var mCurrLocationMarker: Marker? = null
    private var listMarkerPosition = ArrayList<MarkerPosition>()

    private lateinit var mapsViewModel: MapsViewModel
    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    private lateinit var mGoogleMap: GoogleMap
    private lateinit var mLocationRequest: LocationRequest
    private lateinit var binding: ActivityMapsBinding

    internal var mLocationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val locationList = locationResult.locations
            if (locationList.isNotEmpty()) {
                //The last location in the list is the newest
                val location = locationList.last()
                Log.e(
                    TAG,
                    "Location: " + location.getLatitude() + " " + location.getLongitude()
                )
                currentLocation = location
                if (mCurrLocationMarker != null) {
                    mCurrLocationMarker?.remove()
                }

                //Place current location marker
                val latLng = LatLng(location.latitude, location.longitude)
                val markerOptions = MarkerOptions()
                markerOptions.position(latLng)
                markerOptions.title("Current Position")
                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA))
                mCurrLocationMarker = mGoogleMap.addMarker(markerOptions)

                //move map camera
                mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15.0F))
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_maps)

        mapsViewModel = MapsViewModel()
        binding.mapsModel = mapsViewModel!!.mapsModel

        createFused()
        observer()
    }

    override fun onResume() {
        super.onResume()
        initial()
    }

    private fun createFused() {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        mLocationRequest = LocationRequest()
        mLocationRequest.interval = 120000 // two minute interval
        mLocationRequest.fastestInterval = 120000
        mLocationRequest.priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
    }

    private fun initial() {
        if (checkPermissions()) {
            if (!isLocationEnabled()) {
                Toast.makeText(this, "Turn on location", Toast.LENGTH_LONG).show()
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
            val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
            mapFragment.getMapAsync(this)

        } else {
            requestPermissions()
        }
    }


    override fun onMapReady(googleMap: GoogleMap) {
        mGoogleMap = googleMap

        mFusedLocationClient?.requestLocationUpdates(
            mLocationRequest,
            mLocationCallback,
            Looper.myLooper()
        )
        mGoogleMap.isMyLocationEnabled = true
        marker()
//        mapsViewModel!!.routng()
    }

    private fun marker() {
        mGoogleMap.setOnMapClickListener {
            if (listMarkerPosition.size < 5) {
                listMarkerPosition.add(MarkerPosition(it))

//                mapsViewModel!!.getListLocation(listMarkerPosition, mLastLocation!!)
                createMarker(
                    it,
                    BitmapDescriptorFactory.HUE_RED
                )
            }

            if (listMarkerPosition.size == 5) {
                mapsViewModel!!.getListLocation(listMarkerPosition, currentLocation!!)
            }
        }
    }

    private fun createMarker(
        latLong: LatLng,
        iconResID: Float
    ): MarkerOptions {

        var marker = MarkerOptions()
        marker.position(
            latLong
        )
        marker.icon(BitmapDescriptorFactory.defaultMarker(iconResID))
        mGoogleMap.addMarker(marker)
        return marker
    }

    fun observer() {
        mapsViewModel!!.shortestDistanceIndex.observe(
            this,
            androidx.lifecycle.Observer { closestMarker ->
                if (closestMarker != null) {

                    //Remove
                    var markerName: Marker? =
                        mGoogleMap.addMarker(MarkerOptions().position(closestMarker.latLng))

                    markerName!!.remove()
                    //Add
                    var markerOptions = MarkerOptions()
                    markerOptions.position(
                        closestMarker.latLng
                    )
                    markerOptions.title("Closest")
                    markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW))

                    mGoogleMap.addMarker(markerOptions)
                }
            })

    }


    ////////////check permission/////////////////////////

    fun checkPermissions(): Boolean {
        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            return true
        }
        return false

    }

    fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ),
            42

        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == 42) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                initial()

                mFusedLocationClient?.requestLocationUpdates(
                    mLocationRequest,
                    mLocationCallback,
                    Looper.myLooper()
                )

            }
        }
    }

    private fun isLocationEnabled(): Boolean {
        var locationManager: LocationManager =
            getSystemService(AppCompatActivity.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )

    }


}
