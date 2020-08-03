package com.currency.rates.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.currency.rates.R;



import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

/**
 * A fragment representing a single Item detail screen.
 * This fragment is either contained in a {@link CurrencyListActivity}
 * in two-pane mode (on tablets) or a {@link CurrencyRatesDetailActivity}
 * on handsets.
 */
public class CurrencyRatesDetailFragment extends Fragment {
    public static final String ARG_CURRENCY_KEY = "currency_key";
    public static final String ARG_CURRENCY_VALUE = "currency_value";
    public static final String ARG_CURRENCY_RATE = "currency_rate";
    public static final String CURRENCY_RATES_FRAGMENT_TAG = "currency_rates_fragment_tag";

    private ArrayList<String> currencyKey;
    private ArrayList<String> currencyValue;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle != null && bundle.containsKey(ARG_CURRENCY_KEY)) {
            currencyKey = (bundle.getStringArrayList(ARG_CURRENCY_KEY));
            currencyValue = (bundle.getStringArrayList(ARG_CURRENCY_VALUE));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_currency_detail, container, false);
        RecyclerView currencyListRecyclerView = rootView.findViewById(R.id.currency_detail_recycler_view);
        setupRecyclerView(currencyListRecyclerView);
        return rootView;
    }

    private void setupRecyclerView(@NonNull RecyclerView recyclerView) {
        CurrencyDetailRecyclerAdapter currencyDetailRecyclerAdapter = new CurrencyDetailRecyclerAdapter(currencyKey, currencyValue);
        recyclerView.setAdapter(currencyDetailRecyclerAdapter);
        recyclerView.setHasFixedSize(true);
    }

    public static class CurrencyDetailRecyclerAdapter
            extends RecyclerView.Adapter<CurrencyDetailRecyclerAdapter.ViewHolder> {

        private final List<String> currencyList;
        private final List<String> rateList;

        CurrencyDetailRecyclerAdapter(List<String> currencyList, List<String> rateList) {
            this.currencyList = currencyList;
            this.rateList = rateList;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.currency_rates_detail_item_view, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            holder.currency.setText(currencyList.get(position));
            holder.rate.setText(rateList.get(position));
        }

        @Override
        public int getItemCount() {
            return currencyList.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            final TextView currency;
            final TextView rate;

            ViewHolder(View view) {
                super(view);
                currency = view.findViewById(R.id.currency);
                rate = view.findViewById(R.id.rate);
            }
        }
    }
}
