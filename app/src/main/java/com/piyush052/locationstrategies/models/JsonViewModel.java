package com.piyush052.locationstrategies.models;

import android.annotation.SuppressLint;
import android.app.Application;
import android.util.Log;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.google.gson.Gson;
import com.piyush052.locationstrategies.network.NetworkService;
import com.piyush052.locationstrategies.network.Request;
import com.piyush052.locationstrategies.service.NetworkResponse;
import org.jetbrains.annotations.NotNull;

public class JsonViewModel extends AndroidViewModel {
    // You probably have something more complicated
    // than just a String. Roll with me
    private final MutableLiveData<String> data = new MutableLiveData<>();
    public JsonViewModel(Application application) {
        super(application);
        loadData();
    }
    public LiveData<String> getData() {
        return data;
    }
    private void loadData() {
        new NetworkService() .callLoginApi(new Request<String>(),new NetworkResponse<String>() {

                    @Override
                    public void onNetworkError(@NotNull Request<String> request) {

                    }

                    @Override
                    public void onNetworkResponse(@NotNull Request<String> request) {
                        data.setValue(request.getResponse());
                        Log.e("", "onNetworkError: "+ new Gson().toJson(data));
                    }
                }
        );

//        new AsyncTask<Void,Void,List<String>>() {
//            @Override
//            protected List<String> doInBackground(Void... voids) {
//                File jsonFile = new File(getApplication().getFilesDir(),
//                        "downloaded.json");
//                List<String> data = new ArrayList<>();
//                // Parse the JSON using the library of your choice
//                return data;
//            }
//            @Override
//            protected void onPostExecute(List<String> data1) {
//                data.setValue(data1);
//            }
//        }.execute();
    }
}