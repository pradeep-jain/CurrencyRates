package com.currency.rates.util;

import com.currency.rates.models.Currency;

import java.util.ArrayList;
import java.util.Map;

public class CurrencyUtil {

    public static ArrayList<String> getCurrencyList(Currency currency) {
        return new ArrayList<>(currency.getRates().keySet());
    }

    public static ArrayList<String> getRateList(ArrayList<String> currencyList, Currency currency) {
        ArrayList<String> rateList = new ArrayList<>();
        Map<String, String> currencies = currency.getRates();
        for (String key : currencyList) {
            rateList.add(currencies.get(key));
        }
        return rateList;
    }
}
