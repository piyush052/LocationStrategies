package com.piyush052.locationstrategies

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private var context: Context? = null

    var locationManager: LocationManager? = null
    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        context = this

        // Acquire a reference to the system Location Manager
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        locationManager!!.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0f, locationListener)


    }

    val locationListener: LocationListener
        get() = object : LocationListener {

            override fun onLocationChanged(location: Location) {
                // Called when a new location is found by the network location provider.
                makeUseOfNewLocation(location)
            }

            override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {
                textView.append( "\nonStatusChanged  -- ${provider}, ${status}")

            }

            @SuppressLint("MissingPermission")
            override fun onProviderEnabled(provider: String) {
                textView.append("\nonProviderEnabled  -- ${provider}")
              //  locationManager!!.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0f, locationListener)

            }

            @SuppressLint("MissingPermission")
            override fun onProviderDisabled(provider: String) {
               textView.append( "\nonProviderDisabled  -- ${provider}")
              //  locationManager!!.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0f, locationListener)


            }
        }


    override fun onDestroy() {
        super.onDestroy()
        locationManager!!.removeUpdates(locationListener)
    }

    @SuppressLint("SetTextI18n")
    private fun makeUseOfNewLocation(location: Location) {
        textView.append("\nLocation  -- ${location.latitude}, ${location.longitude}")
    }
}
