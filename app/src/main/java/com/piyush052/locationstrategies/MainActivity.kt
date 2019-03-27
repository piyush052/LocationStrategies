package com.piyush052.locationstrategies

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private var context: Context? = null

    var locationManager: LocationManager?= null
    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        context = this

        // Acquire a reference to the system Location Manager
         locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        // Define a listener that responds to location updates


        // Register the listener with the Location Manager to receive location updates
        //locationManager!!.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0f, locationListener)
        locationManager!!.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0f, locationListener)
       // locationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, 0, 0f, locationListener)


        textView.setOnClickListener(object :View.OnClickListener{
            override fun onClick(v: View?) {
                Toast.makeText(context, "---------",Toast.LENGTH_LONG).show()
            }

        })

    }

    val locationListener: LocationListener
        get() = object : LocationListener {

            override fun onLocationChanged(location: Location) {
                // Called when a new location is found by the network location provider.
                makeUseOfNewLocation(location)
            }

            override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {
                textView.text = "onStatusChanged  -- ${provider}, ${status}"

            }

            override fun onProviderEnabled(provider: String) {
                textView.text = "onProviderEnabled  -- ${provider}"
            }

            @SuppressLint("MissingPermission")
            override fun onProviderDisabled(provider: String) {
                textView.text = "onProviderDisabled  -- ${provider}"
                locationManager!!.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0f, locationListener)


            }
        }


    override fun onDestroy() {
        super.onDestroy()
        locationManager!!.removeUpdates(locationListener)
    }

    @SuppressLint("SetTextI18n")
    private fun makeUseOfNewLocation(location: Location) {
        textView.text = "Location  -- ${location.latitude}, ${location.longitude}"
    }
}
