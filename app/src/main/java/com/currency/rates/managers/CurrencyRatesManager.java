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

    public Single<List<Currency>> getAllCurrencyRates() {
        Single<Currency> observableCurrency;

        if (NetworkUtil.isNetworkAvailable(context)) {
            observableCurrency = currencyClient.getAllCurrencyRates(defaultCurrency);
            if (observableCurrency != null) {
                return observableCurrency.map(currency -> {
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
                        }));
            } else {
                return null;
            }
        } else {
            return currencyDAO.getAllCurrencyRates();
        }
    }

    private void addCurrencyToDB(List<Currency> currencies) {
        Completable.fromAction(() -> currencyDAO.insertCurrencyRates(currencies)).subscribeOn(Schedulers.io())
                .subscribe();
    }
}
