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

    private lateinit var context: Context
    private  var currentLocation: Location? = null

    private val _PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 101
    private  val TWO_MINUTES: Long = 1000 * 60 * 2


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
    fun startListeningLocation() {
        locationManager!!.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3000, 0f, locationListener)
    }


    private fun showRationaleDialog() {
        AlertDialog.Builder(this)
            .setPositiveButton("OK", DialogInterface.OnClickListener { dialog, which ->
                ActivityCompat.requestPermissions(
                    this@MainActivity,
                    arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), _PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION
                )
            })
            .setNegativeButton("Cancel", DialogInterface.OnClickListener { dialog, which ->
                Toast.makeText(this@MainActivity, "Provide the permission ", Toast.LENGTH_SHORT).show()
            })
            .setCancelable(false)
            .setMessage("App need Location permission ")
            .show()
    }


    private fun checkPermission() {
        if (ContextCompat.checkSelfPermission(context as MainActivity, Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(context as MainActivity, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {

            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    context as MainActivity,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            ) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                showRationaleDialog()
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(
                    context as MainActivity,
                    arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION),
                    _PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION
                )

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }


        } else {
            startListeningLocation()
        }

    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            _PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startListeningLocation()
                } else {
                    if (!ActivityCompat.shouldShowRequestPermissionRationale(
                            this,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        )
                    ) {
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


    private val locationListener: LocationListener = object : LocationListener {

        override fun onLocationChanged(location: Location) {
            // Called when a new location is found by the network location provider.
            makeUseOfNewLocation(location)
        }

        override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {
            textView.append("\nonStatusChanged  -- ${provider}, ${status}")

        }

        override fun onProviderEnabled(provider: String) {
            textView.append("\nonProviderEnabled  -- ${provider}")
            //  locationManager!!.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0f, locationListener)

        }

        override fun onProviderDisabled(provider: String) {
            textView.append("\nonProviderDisabled  -- ${provider}")
            //  locationManager!!.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0f, locationListener)


        }

    }


    override fun onDestroy() {
        super.onDestroy()
        locationManager!!.removeUpdates(locationListener)
    }

    @SuppressLint("SetTextI18n")
    private fun makeUseOfNewLocation(location: Location) {

        textView.append("\nLocation  -- ${location.latitude}, ${location.longitude}  ")
        if(currentLocation==null){
            currentLocation = location
            textView.append(isBetterLocation(location,null).toString())

        }else{
            textView.append(isBetterLocation(currentLocation as Location, location).toString())
        }

        currentLocation = location
    }



    /** Determines whether one Location reading is better than the current Location fix
     * @param location The new Location that you want to evaluate
     * @param currentBestLocation The current Location fix, to which you want to compare the new one
     */
    fun isBetterLocation(location: Location, currentBestLocation: Location?): Boolean {
        if (currentBestLocation == null) {
            // A new location is always better than no location
            return true
        }

        // Check whether the new location fix is newer or older
        val timeDelta: Long = location.time - currentBestLocation.time
        val isSignificantlyNewer: Boolean = timeDelta > TWO_MINUTES
        val isSignificantlyOlder:Boolean = timeDelta < -TWO_MINUTES

        when {
            // If it's been more than two minutes since the current location, use the new location
            // because the user has likely moved
            isSignificantlyNewer -> return true
            // If the new location is more than two minutes older, it must be worse
            isSignificantlyOlder -> return false
        }

        // Check whether the new location fix is more or less accurate
        val isNewer: Boolean = timeDelta > 0L
        val accuracyDelta: Float = location.accuracy - currentBestLocation.accuracy
        val isLessAccurate: Boolean = accuracyDelta > 0f
        val isMoreAccurate: Boolean = accuracyDelta < 0f
        val isSignificantlyLessAccurate: Boolean = accuracyDelta > 200f

        // Check if the old and new location are from the same provider
        val isFromSameProvider: Boolean = location.provider == currentBestLocation.provider

        // Determine location quality using a combination of timeliness and accuracy
        return when {
            isMoreAccurate -> true
            isNewer && !isLessAccurate -> true
            isNewer && !isSignificantlyLessAccurate && isFromSameProvider -> true
            else -> false
        }
    }
}
