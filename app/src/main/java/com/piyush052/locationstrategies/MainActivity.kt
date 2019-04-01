package com.piyush052.locationstrategies

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*
import android.widget.Toast
import android.content.DialogInterface
import androidx.appcompat.app.AlertDialog


class MainActivity : AppCompatActivity() {

    private var context: Context? = null
    private val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION =  101


    var locationManager: LocationManager? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        context = this
        // Acquire a reference to the system Location Manager
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        checkPermission()



    }

    @SuppressLint("MissingPermission")
    fun startListeningLocation (){
        locationManager!!.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0f, locationListener)
    }


    private fun showRationaleDialog() {
        AlertDialog.Builder(this)
            .setPositiveButton("OK", DialogInterface.OnClickListener { dialog, which ->
                ActivityCompat.requestPermissions(
                    this@MainActivity,
                    arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION
                )
            })
            .setNegativeButton("Cancel", DialogInterface.OnClickListener { dialog, which ->
                Toast.makeText(this@MainActivity, "Provide the permission ", Toast.LENGTH_SHORT).show()
            })
            .setCancelable(false)
            .setMessage("App need Location permission ")
            .show()
    }


    fun checkPermission (){
        if (ContextCompat.checkSelfPermission(context as MainActivity, Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(context as MainActivity, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(context as MainActivity, Manifest.permission.ACCESS_COARSE_LOCATION)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                showRationaleDialog()
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(context as MainActivity,
                    arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION),
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION)

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }


        }else{
            startListeningLocation()
        }

    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startListeningLocation()
                } else {
                    if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                        Toast.makeText(
                            this@MainActivity,
                            "Please provide the permission ",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        showRationaleDialog()
                    }
                }
            }
        }
    }



    private val locationListener: LocationListener
        get() = object : LocationListener {

            override fun onLocationChanged(location: Location) {
                // Called when a new location is found by the network location provider.
                makeUseOfNewLocation(location)
            }

            override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {
                textView.append( "\nonStatusChanged  -- ${provider}, ${status}")

            }

            override fun onProviderEnabled(provider: String) {
                textView.append("\nonProviderEnabled  -- ${provider}")
              //  locationManager!!.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0f, locationListener)

            }

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
