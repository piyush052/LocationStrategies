package com.piyush052.locationstrategies.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.app.NotificationManager
import android.app.NotificationChannel
import android.content.IntentFilter
import android.os.BatteryManager
import android.util.Log
import com.piyush052.locationstrategies.java.Position
import com.piyush052.locationstrategies.java.CallAsync


class ForegroundService : Service(), LocationListener {

    var locationManager: LocationManager? = null
    private val PROVIDER = LocationManager.GPS_PROVIDER
    val CHANNEL_DEFAULT_IMPORTANCE = "1111"
    private val ONGOING_NOTIFICATION_ID = 1111
    var count = 1
    val minTime:Long = 150000
    val minDistace : Float= 100f


    override fun onLocationChanged(location: Location?) {
        Log.e("onLocationChanged","")
        if (location != null) {
            makeUseOfNewLocation(location)
        }
    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
    }

    override fun onProviderEnabled(provider: String?) {

    }

    override fun onProviderDisabled(provider: String?) {
    }


    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        // Register as Foreground Service
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            registerForgroundService()

        // start Listening
        startListeningLocation()
        return super.onStartCommand(intent, flags, startId)

    }


    @SuppressLint("NewApi")
    fun registerForgroundService() {

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = getString(com.piyush052.locationstrategies.R.string.app_name)
        val notificationChannel = NotificationChannel(channelId, channelId, NotificationManager.IMPORTANCE_DEFAULT)
        notificationChannel.description = channelId
        notificationChannel.setSound(null, null)

        notificationManager.createNotificationChannel(notificationChannel)
        val notification = Notification.Builder(this, channelId)
            .setContentTitle(getString(com.piyush052.locationstrategies.R.string.app_name))
            .setContentText("Syncing Data")
            .setSmallIcon(com.piyush052.locationstrategies.R.mipmap.ic_launcher)
            .setPriority(Notification.PRIORITY_DEFAULT)
            .build()
        startForeground(111, notification)

    }

    @SuppressLint("MissingPermission")
    fun startListeningLocation() {

        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        locationManager!!.requestLocationUpdates(PROVIDER, minTime, minDistace, this)
        locationManager!!.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, minTime, minDistace, this)
        val location = getLocation(PROVIDER)
        if (location is Location) {
            Log.e("Last Location","")
            makeUseOfNewLocation(location)
        }
    }

    private fun makeUseOfNewLocation(location: Location) {
        // check location accuracy

        sendData(location)
    }

     private fun getBatteryLevel(context: Context): Double {
        val batteryIntent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        if (batteryIntent != null) {
            val level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0)
            val scale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, 1)
            return level * 100.0 / scale
        }
        return 0.0
    }

    private fun sendData(location: Location) {

        count++

        val position = Position(
            "123456789012",
            location,
            getBatteryLevel(applicationContext)
        )


        if(count%10==0){
            CallAsync().callAsyncAPI(this, position,"sos")
        }else
        CallAsync().callAsyncAPI(this, position,null)


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


    private val TWO_MINUTES: Long = 1000 * 60 * 2
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


    override fun onDestroy() {
        super.onDestroy()
        locationManager!!.removeUpdates(this)
    }
}