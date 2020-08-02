package com.currency.rates.managers;

import android.content.Context;

import com.currency.rates.R;
import com.currency.rates.api.CurrencyClient;
import com.currency.rates.db.CurrencyDatabase;
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

    public CurrencyRatesManager(Context context) {
        this.context = context;
    }

    public Single<List<Currency>> getAllCurrencyRates() {
        Single<Currency> observableCurrency;

        if (NetworkUtil.isNetworkAvailable(context)) {
            CurrencyClient currencyClient = CurrencyClient.getInstance(context);
            observableCurrency = currencyClient.getAllCurrencyRates(context.getResources().getString(R.string.default_currency));
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
                                addCurrencyToDB(context, currencyList);
                            }
                            return currencyList;
                        }));
            } else {
                return null;
            }
        } else {
            return CurrencyDatabase.getInstance(context).currencyDAO().getAllCurrencyRates();
        }
    }

    private void addCurrencyToDB(Context context, List<Currency> currencies) {
        Completable.fromAction(() -> CurrencyDatabase.getInstance(context).currencyDAO().insertCurrencyRates(currencies)).subscribeOn(Schedulers.io())
                .subscribe();
    }
}
