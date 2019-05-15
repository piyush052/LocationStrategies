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
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.*
import com.google.android.gms.location.LocationSettingsStatusCodes
import com.google.android.gms.location.LocationSettingsStates
import com.google.android.gms.location.places.ui.PlaceAutocomplete.getStatus
import com.google.gson.Gson
import com.piyush052.locationstrategies.models.JsonViewModel
import com.piyush052.locationstrategies.network.NetworkService
import com.piyush052.locationstrategies.network.Request
import com.piyush052.locationstrategies.service.ForegroundService
import com.piyush052.locationstrategies.service.NetworkResponse
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
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
    private val _PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 101

    var locationManager: LocationManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        context = this
        // Acquire a reference to the system Location Manager
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        checkPermission()

//        clear.setOnClickListener {
//            val url = "http://www.example.com/gizmos"
//            val i = Intent(Intent.ACTION_VIEW)
//            i.data = Uri.parse(url)
//            startActivity(i)
//        }

        val model = ViewModelProviders.of(this).get(JsonViewModel::class.java)

        model.getData().observe(this, object : Observer< String> {
            override fun onChanged(t: String?) {
               Log.e("viewModel changed", Gson().toJson(t))
            }

        })



        GlobalScope.launch {
            NetworkService().callLoginApi(request= Request(), networkResponse = object : NetworkResponse<String> {
                override fun onNetworkError(request: Request<String>) {

                }

                override fun onNetworkResponse(request: Request<String>) {

                }

            })
        }


    }

    @SuppressLint("MissingPermission")
    fun startListeningLocation() {


        // run the service
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(Intent(this@MainActivity, ForegroundService::class.java))
        } else {
            startService(Intent(this@MainActivity, ForegroundService::class.java))

        }

    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP_MR1)
    fun turnONGps() {
        googleApiClient = null
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
            result.setResultCallback(
                object : ResultCallback<LocationSettingsResult>,
                    com.google.android.gms.common.api.ResultCallback<LocationSettingsResult> {
                    override fun onReceiveResult(result: LocationSettingsResult) {
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


                    }

                    override fun onResult(result: LocationSettingsResult) {
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

                    }

                })
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
            turnONGps()
            startListeningLocation()
        }

    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            _PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    turnONGps()
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

}
