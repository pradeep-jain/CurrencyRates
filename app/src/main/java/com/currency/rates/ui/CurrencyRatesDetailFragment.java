package com.currency.rates.ui;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.currency.rates.R;
import com.currency.rates.api.CurrencyClient;
import com.currency.rates.db.CurrencyDatabase;
import com.currency.rates.managers.CurrencyRatesManager;
import com.currency.rates.models.Currency;
import com.currency.rates.util.CurrencyUtil;
import com.currency.rates.util.ProgressDialogUtil;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * A fragment representing a single Item detail screen.
 * This fragment is either contained in a {@link CurrencyListActivity}
 * in two-pane mode (on tablets) or a {@link CurrencyRatesDetailActivity}
 * on handsets.
 */
public class CurrencyRatesDetailFragment extends Fragment {
    public static final String ARG_BASE_CURRENCY_KEY = "base_currency_key";
    public static final String ARG_CURRENCY_RATE = "currency_rate";
    public static final String CURRENCY_RATES_FRAGMENT_TAG = "currency_rates_fragment_tag";

    private ArrayList<String> currencyList = new ArrayList<>();
    private ArrayList<String> rateList = new ArrayList<>();
    private String baseCurrency;
    private CurrencyDetailRecyclerAdapter currencyDetailRecyclerAdapter;
    private AlertDialog progressDialog;
    private CompositeDisposable disposable = new CompositeDisposable();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_currency_detail, container, false);
        RecyclerViewWithEmptyText currencyListRecyclerView = rootView.findViewById(R.id.currency_detail_recycler_view);
        TextView emptyView = rootView.findViewById(R.id.empty_view);
        TextView baseCurrencyView = rootView.findViewById(R.id.base_currency_view);

        Bundle bundle = getArguments();
        if (bundle != null && bundle.containsKey(ARG_BASE_CURRENCY_KEY)) {
            baseCurrency = bundle.getString(ARG_BASE_CURRENCY_KEY);
            baseCurrencyView.setText(String.format(getString(R.string.base_currency), baseCurrency));
        }

        setupRecyclerView(currencyListRecyclerView, emptyView);
        progressDialog = ProgressDialogUtil.createProgressDialog(getActivity());
        ProgressDialogUtil.showProgressDialog(progressDialog);
        getCurrencyRates(baseCurrency);
        return rootView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(disposable != null){
            disposable.clear();
        }
    }

    private void getCurrencyRates(String baseCurrency) {
        Single<Currency> currency = new CurrencyRatesManager(getContext(),
                CurrencyClient.getInstance(),
                CurrencyDatabase.getInstance(getContext()).currencyDAO()).getCurrencyList(baseCurrency);

        currency.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<Currency>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        disposable.add(d);
                    }

                    @Override
                    public void onSuccess(Currency currency) {
                        if (currency != null) {
                            currencyList.clear();
                            rateList.clear();
                            currencyList.addAll(CurrencyUtil.getCurrencyList(currency));
                            rateList.addAll(CurrencyUtil.getRateList(currencyList, currency));
                            currencyDetailRecyclerAdapter.setAdapterData(currencyList, rateList);
                        }
                        ProgressDialogUtil.hideProgressDialog(progressDialog);
                    }

                    @Override
                    public void onError(Throwable e) {
                        currencyList.clear();
                        rateList.clear();
                        currencyDetailRecyclerAdapter.setAdapterData(currencyList, rateList);
                        ProgressDialogUtil.hideProgressDialog(progressDialog);
                    }
                });
    }

    private void setupRecyclerView(@NonNull RecyclerViewWithEmptyText recyclerView, @NonNull TextView emptyView) {
        currencyDetailRecyclerAdapter = new CurrencyDetailRecyclerAdapter(currencyList, rateList);
        recyclerView.setAdapter(currencyDetailRecyclerAdapter);
        recyclerView.setHasFixedSize(true);
        recyclerView.setEmptyView(emptyView);
    }

    public static class CurrencyDetailRecyclerAdapter
            extends RecyclerView.Adapter<CurrencyDetailRecyclerAdapter.ViewHolder> {

        private List<String> currencyList;
        private List<String> rateList;

        CurrencyDetailRecyclerAdapter(List<String> currencyList, List<String> rateList) {
            this.currencyList = currencyList;
            this.rateList = rateList;
        }

        private void setAdapterData(List<String> currencyList, List<String> rateList) {
            this.currencyList = currencyList;
            this.rateList = rateList;
            notifyDataSetChanged();
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
