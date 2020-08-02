package com.currency.rates.db;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Map;

import androidx.room.TypeConverter;

public class Converters {
    @TypeConverter
    public static Map<String, String> fromString(String value) {
        Type listType = new TypeToken<Map<String, String>>() {
        }.getType();
        return new Gson().fromJson(value, listType);
    }

    @TypeConverter
    public static String fromArrayList(Map<String, String> list) {
        Gson gson = new Gson();
        return gson.toJson(list);
    }
}
