package com.currency.rates.api;

import com.currency.rates.models.Currency;

import io.reactivex.Single;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ICurrencyRatesApiService {
    @GET("latest")
    Single<Currency> getCurrencyRates(@Query("base") String baseCurrency);


}
