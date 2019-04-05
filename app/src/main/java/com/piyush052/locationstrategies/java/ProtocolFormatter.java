package com.piyush052.locationstrategies.java;

import android.net.Uri;
import com.piyush052.locationstrategies.Position;

public class ProtocolFormatter {



    public static String formatRequest(String url, Position position, String alarm) {
        Uri serverUrl = Uri.parse(url);
        Uri.Builder builder = serverUrl.buildUpon()
                .appendQueryParameter("id", position.getDeviceId())
                .appendQueryParameter("timestamp", String.valueOf(position.getTime().getTime() / 1000))
                .appendQueryParameter("lat", String.valueOf(position.getLatitude()))
                .appendQueryParameter("lon", String.valueOf(position.getLongitude()))
                .appendQueryParameter("speed", String.valueOf(position.getSpeed()))
                .appendQueryParameter("bearing", String.valueOf(position.getCourse()))
                .appendQueryParameter("altitude", String.valueOf(position.getAltitude()))
                .appendQueryParameter("accuracy", String.valueOf(position.getAccuracy()))
                .appendQueryParameter("batt", String.valueOf(position.getBattery()));

//        if (position.getMock()) {
//            builder.appendQueryParameter("mock", String.valueOf(position.getMock()));
//        }
//
        if (alarm != null) {
            builder.appendQueryParameter("alarm", alarm);
        }

        return builder.build().toString();
    }
}
