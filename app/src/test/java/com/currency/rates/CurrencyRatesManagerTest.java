package com.currency.rates;

import android.content.Context;

import com.currency.rates.api.CurrencyClient;
import com.currency.rates.db.CurrencyDAO;
import com.currency.rates.db.CurrencyDatabase;
import com.currency.rates.managers.CurrencyRatesManager;
import com.currency.rates.models.Currency;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.List;

import androidx.room.Room;
import androidx.test.platform.app.InstrumentationRegistry;

import static org.junit.Assert.assertFalse;


@Config(manifest = Config.NONE)
@RunWith(RobolectricTestRunner.class)
public class CurrencyRatesManagerTest {

    private CurrencyRatesManager currencyRatesManager;

    @Before
    public void setUp() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext().getApplicationContext();
        CurrencyDAO currencyDAO = Room.inMemoryDatabaseBuilder(context, CurrencyDatabase.class)
                .allowMainThreadQueries()
                .build().currencyDAO();
        CurrencyClient currencyClient = CurrencyClient.getInstance();

        currencyRatesManager = new CurrencyRatesManager(context, currencyClient, currencyDAO);
    }

    @Test
    public void getAllCurrencyRates_thenAllCurrencyRatesAreRetrieved() {
        List<Currency> currencyList = currencyRatesManager.getAllCurrencyRates().blockingGet();
        assertFalse(currencyList.isEmpty());
    }
}