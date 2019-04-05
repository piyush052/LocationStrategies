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
    var count = 1;


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
        locationManager!!.requestLocationUpdates(PROVIDER, 15000, 0f, this)
        //locationManager!!.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 15000, 0f, this)
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


//        //textView.text = "Everything is cleared\n"
//        val hashMap: HashMap<String, Any> = hashMapOf()
//
//        hashMap.put("id", 123456789012)
//        hashMap.put("timestamp", 123456789012)
//        hashMap.put("lat", 34.566)
//        hashMap.put("lon", 132.44444)
//        hashMap.put("speed", 40)
//        hashMap.put("bearing", 6.0)
//        hashMap.put("altitude", 200)
//        hashMap.put("accuracy", 1)
//        hashMap.put("batt", 89)
//
//        val s =
//            "http://api.traxsmart.in:5055?id=8884144794&timestamp=1554363378&lat=13.1986348037&lon=77.7065928&bearing=272." +
//                    "679170984&speed=0&alarm=sos&accuracy=100&rpm=2472&fuel=76&driverUniqueId=123456"
//
//        val x: HttpUrl? = HttpUrl.parse(s)
//
//        x?.let {
//            NetworkService().getInstance().sendDataToServer(hashMap /*123456789012,123456789012,34.566,132.44444,40.0,8*/)
//                .enqueue(object : Callback, retrofit2.Callback<Any> {
//                    override fun onFailure(call: Call, e: IOException) {
//                    }
//
//                    override fun onResponse(call: Call, response: Response) {
//                    }
//
//                    override fun onFailure(call: retrofit2.Call<Any>, t: Throwable) {
//                        Log.e("onFailure", t.message)
//                    }
//
//                    override fun onResponse(call: retrofit2.Call<Any>, response: retrofit2.Response<Any>) {
//                       // Log.e("--", s)
//                    }
//                })
//        }


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
}