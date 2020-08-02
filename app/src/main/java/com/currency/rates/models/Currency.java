package com.currency.rates.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.util.Map;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "currency")
public class Currency implements Parcelable{

    @PrimaryKey
    @ColumnInfo(name = "base")
    @SerializedName("base")
    @androidx.annotation.NonNull
    private String base;
    //private Rates rates;

    @ColumnInfo(name = "rates")
    @SerializedName("rates")
    private Map<String, String> rates;

    @Ignore
    private String date;
    public String getBase() {
        return base;
    }

    public void setBase(String base) {
        this.base = base;
    }

    public Map<String, String> getRates() {
        return rates;
    }

    public void setRates(Map<String, String> rates) {
        this.rates = rates;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
    public Currency(){}



    protected Currency(Parcel in) {
        base = in.readString();
        date = in.readString();
    }

    public static final Creator<Currency> CREATOR = new Creator<Currency>() {
        @Override
        public Currency createFromParcel(Parcel in) {
            return new Currency(in);
        }

        @Override
        public Currency[] newArray(int size) {
            return new Currency[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(base);
        parcel.writeString(date);
    }
}
