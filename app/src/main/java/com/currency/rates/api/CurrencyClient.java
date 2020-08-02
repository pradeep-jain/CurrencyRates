package com.currency.rates.api;

import android.content.Context;

import com.currency.rates.R;
import com.currency.rates.models.Currency;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import io.reactivex.Single;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class CurrencyClient {
    private static CurrencyClient INSTANCE;
    private ICurrencyRatesApiService currencyRatesApiService;

    public static CurrencyClient getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (CurrencyClient.class) {
                if (INSTANCE == null) {
                    INSTANCE = new CurrencyClient(context);
                }
            }
        }
        return INSTANCE;
    }

    private CurrencyClient(Context context) {
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.level(HttpLoggingInterceptor.Level.BODY);

        Gson gson = new GsonBuilder()
                .setLenient()
                .create();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(context.getString(R.string.base_url))
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();

        currencyRatesApiService = retrofit.create(ICurrencyRatesApiService.class);
    }

    public Single<Currency> getAllCurrencyRates(String currency) {
        return currencyRatesApiService.getCurrencyRates(currency);
    }
}
