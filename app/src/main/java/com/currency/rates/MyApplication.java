package com.currency.rates;

import android.annotation.TargetApi;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkRequest;
import android.os.Build;

import com.currency.rates.api.CurrencyClient;
import com.currency.rates.db.CurrencyDatabase;
import com.currency.rates.managers.CurrencyRatesManager;

import static android.net.ConnectivityManager.CONNECTIVITY_ACTION;

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        registerNetworkChange();
    }

    private void registerNetworkChange() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            NetworkRequest networkRequest = new NetworkRequest.Builder().build();
            ConnectivityManager connectivityManager = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            connectivityManager.registerNetworkCallback(networkRequest, new NetworkChangeCallBack());
        } else {
            IntentFilter filter = new IntentFilter(CONNECTIVITY_ACTION);
            NetworkReceiver networkReceiver = new NetworkReceiver();
            registerReceiver(networkReceiver, filter);
        }
    }

    class NetworkReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            getAndSaveCurrencyRatesInDb();
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    class NetworkChangeCallBack extends ConnectivityManager.NetworkCallback {
        @Override
        public void onAvailable(Network network) {
            super.onAvailable(network);
            getAndSaveCurrencyRatesInDb();
        }
    }

    private void getAndSaveCurrencyRatesInDb() {
        new CurrencyRatesManager(getBaseContext(),
                CurrencyClient.getInstance(),
                CurrencyDatabase.getInstance(getBaseContext()).currencyDAO()).getAndSaveCurrencyRatesInDB();
    }
}
