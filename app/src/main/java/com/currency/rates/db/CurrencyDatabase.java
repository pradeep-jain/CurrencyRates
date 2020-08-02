package com.currency.rates.db;

import android.content.Context;

import com.currency.rates.models.Currency;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

@Database(entities = {Currency.class}, version = 1, exportSchema = false)
@TypeConverters({Converters.class})
public abstract class CurrencyDatabase extends RoomDatabase {
    private static CurrencyDatabase INSTANCE;

    public abstract CurrencyDAO currencyDAO();

    private static String DB_NAME = "Currency_db";

    public static CurrencyDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (CurrencyDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context, CurrencyDatabase.class, DB_NAME).fallbackToDestructiveMigration().build();
                }
            }
        }
        return INSTANCE;
    }
}
