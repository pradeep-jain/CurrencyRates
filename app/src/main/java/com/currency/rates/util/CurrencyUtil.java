package com.currency.rates.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CurrencyUtil {

    public static ArrayList<String> getValueListFromMap(List<String> keyList, Map<String, String> currencies){
        ArrayList<String> valueList = new ArrayList<>();
        for (String key : keyList){
            valueList.add(currencies.get(key));
        }
        return valueList;
    }
}
