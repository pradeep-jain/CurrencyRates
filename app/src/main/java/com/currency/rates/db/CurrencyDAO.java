package com.currency.rates.db;

import com.currency.rates.models.Currency;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import io.reactivex.Single;

@Dao
public interface CurrencyDAO {

    @Query("SELECT * from Currency where base = :base")
    Single<Currency> getCurrencies(String base);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertCurrencyRates(List<Currency> currency);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertCurrencyRates(Currency currency);
}
