package com.currency.rates;

import android.content.Context;

import com.currency.rates.db.CurrencyDAO;
import com.currency.rates.db.CurrencyDatabase;
import com.currency.rates.models.Currency;
import com.currency.rates.util.TestUtil;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.List;

import androidx.room.Room;
import androidx.test.platform.app.InstrumentationRegistry;

import static org.junit.Assert.assertEquals;

@Config(manifest = Config.NONE)
@RunWith(RobolectricTestRunner.class)
public class CurrencyDBTest {
    private CurrencyDAO currencyDAO;

    @Before
    public void setUp() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext().getApplicationContext();
        currencyDAO = Room.inMemoryDatabaseBuilder(context, CurrencyDatabase.class)
                .allowMainThreadQueries()
                .build().currencyDAO();
    }

    @Test
    public void insertCurrencyList_thenVerifyByRetrievingData() {
        List<Currency> testData = TestUtil.getCurrencyData();
        currencyDAO.insertCurrencyRates(testData);
        List<Currency> dbData = currencyDAO.getAllCurrencyRates().blockingGet();
        assertEquals(testData.size(), dbData.size());
        assertEquals(testData.get(0).getBase(), dbData.get(0).getBase());
    }
}

