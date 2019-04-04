package com.piyush052.locationstrategies

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
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
import android.content.IntentSender
import android.service.carrier.CarrierMessagingService.ResultCallback
import android.content.BroadcastReceiver.PendingResult
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.*
import com.google.android.gms.location.LocationSettingsStatusCodes
import com.google.android.gms.location.LocationSettingsStates
import com.google.android.gms.location.places.ui.PlaceAutocomplete.getStatus
import com.google.gson.Gson
import com.piyush052.locationstrategies.network.NetworkService
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException


class MainActivity : AppCompatActivity(), GoogleApiClient.ConnectionCallbacks,
    GoogleApiClient.OnConnectionFailedListener {
    override fun onConnectionFailed(p0: ConnectionResult) {
        toast("Failed")
    }

    override fun onConnected(p0: Bundle?) {
        startListeningLocation()
    }

    override fun onConnectionSuspended(p0: Int) {
        toast("Suspended")
    }

    private lateinit var context: Context
    var googleApiClient: Any? = null
    private var currentLocation: Location? = null
    private val _PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 101
    private val TWO_MINUTES: Long = 1000 * 60 * 2
    private val PROVIDER = LocationManager.GPS_PROVIDER
    var locationManager: LocationManager? = null

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        context = this
        // Acquire a reference to the system Location Manager
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        checkPermission()
        clear.setOnClickListener { sendData() }
        sendData()

    }

    @SuppressLint("MissingPermission")
    fun startListeningLocation() {
        locationManager!!.requestLocationUpdates(PROVIDER, 0, 0f, locationListener)
        val location = getLocation(PROVIDER)
        if (location is Location)
            makeUseOfNewLocation(location)
    }

    fun turnONGps() {
        if (googleApiClient == null) {
            googleApiClient = GoogleApiClient.Builder(this)
                .addApi(LocationServices.API).addConnectionCallbacks(this@MainActivity)
                .addOnConnectionFailedListener(this@MainActivity).build()
            (googleApiClient as GoogleApiClient?)!!.connect()
            val locationRequest = LocationRequest.create()
            locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY;
            locationRequest.interval = 30 * 1000
            locationRequest.fastestInterval = 5 * 1000
            val builder = LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest)

            // **************************
            builder.setAlwaysShow(true) // this is the key ingredient
            // **************************

            val result =
                LocationServices.SettingsApi.checkLocationSettings(googleApiClient as GoogleApiClient?, builder.build())
            result.setResultCallback {
                (ResultCallback<LocationSettingsResult> { result ->
                    val status = result.status
                    val state = result.locationSettingsStates
                    when (status.statusCode) {
                        LocationSettingsStatusCodes.SUCCESS -> toast("Success")
                        LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> {
                            toast("GPS is not on")
                            // Location settings are not satisfied. But could be
                            // fixed by showing the user
                            // a dialog.
                            try {
                                // Show the dialog by calling
                                // startResolutionForResult(),
                                // and check the result in onActivityResult().
                                status.startResolutionForResult(this@MainActivity, 1000)

                            } catch (e: IntentSender.SendIntentException) {
                                // Ignore the error.
                                Log.e("SendIntentException", e.message)
                            }

                        }
                        LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> toast("Setting change not allowed")
                    }
                })
            }
        }
    }

    fun toast(abs: String) {
        Toast.makeText(this@MainActivity, abs, Toast.LENGTH_LONG).show()
    }


    @SuppressLint("MissingPermission")
    fun getLocation(provider: String): Location? {
        if (locationManager!!.isProviderEnabled(provider)) {
            if (locationManager != null) {
                return locationManager!!.getLastKnownLocation(provider)
            }
        }
        return null
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
                        toast("Please provide the permission ")
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

        }

        override fun onProviderDisabled(provider: String) {
            textView.append("\nonProviderDisabled  -- ${provider}")
            //turnONGps()


        }

    }

    override fun onResume() {
        super.onResume()
        turnONGps()

    }

    override fun onDestroy() {
        super.onDestroy()
        locationManager!!.removeUpdates(locationListener)
    }

    @SuppressLint("SetTextI18n")
    private fun makeUseOfNewLocation(location: Location) {

        textView.append("\nLocation -- ${location.latitude}, ${location.longitude}  ${location.accuracy}")
        if (currentLocation == null) {
            currentLocation = location
            textView.append(isBetterLocation(location, null).toString())

        } else {
            textView.append(isBetterLocation(currentLocation as Location, location).toString())
        }

        currentLocation = location
    }


    /** Determines whether one Location reading is better than the current Location fix
     * @param location The new Location that you want to evaluate
     * @param currentBestLocation The current Location fix, to which you want to compare the new one
     */
    private fun isBetterLocation(location: Location, currentBestLocation: Location?): Boolean {
        if (currentBestLocation == null) {
            // A new location is always better than no location
            return true
        }

        // Check whether the new location fix is newer or older
        val timeDelta: Long = location.time - currentBestLocation.time
        val isSignificantlyNewer: Boolean = timeDelta > TWO_MINUTES
        val isSignificantlyOlder: Boolean = timeDelta < -TWO_MINUTES

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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1000) {
            if (resultCode == Activity.RESULT_OK) {
                var result = data!!.getStringExtra("result")
                startListeningLocation()

            }
            if (resultCode == Activity.RESULT_CANCELED) {
                //Write your code if there's no result
            }
        }
    }

    fun sendData() {
        textView.text = "Everything is cleared\n"
        val hashMap: HashMap<String, Any> = hashMapOf()

        hashMap.put("id", 123456789012)
        hashMap.put("timestamp", 123456789012)
        hashMap.put("lat", 34.566)
        hashMap.put("lon", 132.44444)
        hashMap.put("speed", 40)
        hashMap.put("bearing", 6.0)
        hashMap.put("altitude", 200)
        hashMap.put("accuracy", 1)
        hashMap.put("batt", 89)

        val s =
            "http://api.traxsmart.in:5055?id=8884144794&timestamp=1554363378&lat=13.1986348037&lon=77.7065928&bearing=272." +
                    "679170984&speed=0&alarm=sos&accuracy=100&rpm=2472&fuel=76&driverUniqueId=123456"

        NetworkService().getInstance().sendDataToServer(s/*123456789012,123456789012,34.566,132.44444,40.0,8*/)
            .enqueue(object : Callback, retrofit2.Callback<Any> {
                override fun onFailure(call: Call, e: IOException) {
                    //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                }

                override fun onResponse(call: Call, response: Response) {
                    //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                }

                override fun onFailure(call: retrofit2.Call<Any>, t: Throwable) {
                    Log.e("onFailure", t.message)
                }

                override fun onResponse(call: retrofit2.Call<Any>, response: retrofit2.Response<Any>) {
                    Log.e("onResponse", Gson().toJson(response))
                }

            })
    }
}
