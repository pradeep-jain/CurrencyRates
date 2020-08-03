package com.currency.rates.api;

import com.currency.rates.models.Currency;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import io.reactivex.Single;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class CurrencyClient {
    private static CurrencyClient INSTANCE;
    private ICurrencyRatesApiService currencyRatesApiService;

    public static CurrencyClient getInstance() {
        if (INSTANCE == null) {
            synchronized (CurrencyClient.class) {
                if (INSTANCE == null) {
                    INSTANCE = new CurrencyClient();
                }
            }
        }
        return INSTANCE;
    }

    private CurrencyClient() {
        Gson gson = new GsonBuilder()
                .setLenient()
                .create();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://api.ratesapi.io/api/")
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();

        currencyRatesApiService = retrofit.create(ICurrencyRatesApiService.class);
    }

    public Single<Currency> getAllCurrencyRates(String currency) {
        return currencyRatesApiService.getCurrencyRates(currency);
    }
}
