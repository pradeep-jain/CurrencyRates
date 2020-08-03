package com.currency.rates.util;

import com.currency.rates.models.Currency;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TestUtil {
    private TestUtil(){}
    public static List<Currency> getCurrencyData(){
        List<Currency> currencyList = new ArrayList<>();

        Currency firstCurrency = new Currency();
        firstCurrency.setBase("INR");
        HashMap<String, String> rateMapInr = new HashMap<>();
        rateMapInr.put("AUD", "0.0186020274");
        firstCurrency.setRates(rateMapInr);

        Currency secondCurrency = new Currency();
        secondCurrency.setBase("AUD");
        HashMap<String, String> rateMap = new HashMap<>();
        rateMap.put("INR", "53.7575812712");
        secondCurrency.setRates(rateMap);

        currencyList.add(firstCurrency);
        currencyList.add(secondCurrency);
        return currencyList;
    }
}
