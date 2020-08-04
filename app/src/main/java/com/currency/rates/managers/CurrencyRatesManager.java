package com.currency.rates.managers;

import android.content.Context;

import com.currency.rates.api.CurrencyClient;
import com.currency.rates.db.CurrencyDAO;
import com.currency.rates.models.Currency;
import com.currency.rates.util.NetworkUtil;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class CurrencyRatesManager {
    private Context context;
    private CurrencyDAO currencyDAO;
    private CurrencyClient currencyClient;
    private String defaultCurrency = "INR";

    public CurrencyRatesManager(Context context, CurrencyClient currencyClient, CurrencyDAO currencyDAO) {
        this.context = context;
        this.currencyClient = currencyClient;
        this.currencyDAO = currencyDAO;
    }

    public void getAndSaveCurrencyRatesInDB() {
        Single<Currency> observableCurrency;

        observableCurrency = currencyClient.getAllCurrencyRates(defaultCurrency);
        if (observableCurrency != null) {
            observableCurrency.map(currency -> {
                List<Single<Currency>> observableCurrencyList = new ArrayList<>();
                for (String countryCurrency : currency.getRates().keySet()) {
                    observableCurrencyList.add(currencyClient.getAllCurrencyRates(countryCurrency));
                }
                return observableCurrencyList;
            }).flatMap((Function<List<Single<Currency>>, Single<List<Currency>>>) singles ->
                    Single.zip(singles, objects -> {
                        List<Currency> currencyList = new ArrayList<>();
                        for (Object o : objects) {
                            Currency currency = (Currency) o;
                            currencyList.add(currency);
                        }
                        if (currencyList.size() > 0) {
                            addCurrencyToDB(currencyList);
                        }
                        return currencyList;
                    })).subscribeOn(Schedulers.io())
                    .subscribe();
        }
    }

    public Single<Currency> getCurrencyList(String baseCurrency) {
        Single<Currency> currencySingle;
        if (NetworkUtil.isNetworkAvailable(context)) {
            currencySingle = currencyClient.getAllCurrencyRates(baseCurrency);
            return currencySingle.map(currency -> {
                Completable.fromAction(() -> currencyDAO.insertCurrencyRates(currency)).subscribeOn(Schedulers.io())
                        .subscribe();
                return currency;
            });
        } else {
            currencySingle = currencyDAO.getCurrencies(baseCurrency);
        }
        return currencySingle;
    }

    private void addCurrencyToDB(List<Currency> currencies) {
        Completable.fromAction(() -> currencyDAO.insertCurrencyRates(currencies)).subscribeOn(Schedulers.io())
                .subscribe();
    }
}
