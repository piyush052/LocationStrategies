package com.piyush052.locationstrategies.java;

import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class RequestManager {

    private static final int TIMEOUT = 15 * 1000;

    public interface RequestHandler {
        void onComplete(boolean success);
    }

    private static class RequestAsyncTask extends AsyncTask<String, Void, Boolean> {

        private RequestHandler handler;

        public RequestAsyncTask(RequestHandler handler) {
            this.handler = handler;
        }

        @Override
        protected Boolean doInBackground(String... request) {
//            Log.e(TAG, "doInBackground: " );
            return sendRequest(request[0]);
        }

        @Override
        protected void onPostExecute(Boolean result) {
//            Log.e(TAG, "onPostExecute: " );
            handler.onComplete(result);
        }
    }

    public static boolean sendRequest(String request) {
        Log.e("---", "sendRequest: "+ request );
        InputStream inputStream = null;
        try {
            URL url = new URL(request);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setReadTimeout(TIMEOUT);
            connection.setConnectTimeout(TIMEOUT);
            connection.setRequestMethod("GET");
            connection.setDoOutput(false);
            connection.setDoInput(true);

            connection.connect();
//            inputStream = connection.getInputStream();

            int status = connection.getResponseCode();

            if (status != HttpURLConnection.HTTP_OK)
                inputStream = connection.getErrorStream();
            else
                inputStream = connection.getInputStream();


            while (inputStream.read() != -1);
            return true;


        } catch (IOException error) {
//            Log.e(TAG, "sendRequest: ",error );
            return false;
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException secondError) {
                Log.e(RequestManager.class.getSimpleName(),"", secondError);
            }
        }
    }

    public static void sendRequestAsync(String request, RequestHandler handler) {
//        Log.e(TAG, "sendRequestAsync: " );
        RequestAsyncTask task = new RequestAsyncTask(handler);
        task.execute(request);
    }

}

