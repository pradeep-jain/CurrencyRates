package com.currency.rates.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.currency.rates.R;
import com.currency.rates.managers.CurrencyRatesManager;
import com.currency.rates.models.Currency;
import com.currency.rates.util.CurrencyUtil;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * An activity representing a list of Items. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link CurrencyRatesDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class CurrencyListActivity extends AppCompatActivity {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;
    private List<Currency> currencyList = new ArrayList<>();
    private RecyclerViewWithEmptyText currencyListRecyclerView;
    private CurrencyListRecyclerViewAdapter currencyListRecyclerViewAdapter;
    private AlertDialog progressDialog;
    private final String CURRENCY_LIST_KEY = "currency_list_key";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_currency_list);

        if (findViewById(R.id.item_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;
        }

        currencyListRecyclerView = findViewById(R.id.item_list);
        TextView emptyView = findViewById(R.id.empty_view);
        setupRecyclerView(currencyListRecyclerView, emptyView);
        createProgressDialog();
        showProgressDialog();
        getCurrencyRates();
    }

    private void getCurrencyRates() {
        Single<List<Currency>> currencyRates = new CurrencyRatesManager(getBaseContext()).getAllCurrencyRates();

        currencyRates.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<List<Currency>>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onSuccess(List<Currency> currencies) {
                        if (currencies != null) {
                            currencyList.addAll(currencies);
                            currencyListRecyclerViewAdapter.setCurrencyData(currencies);
                        }
                        hideProgressDialog();
                    }

                    @Override
                    public void onError(Throwable e) {
                        hideProgressDialog();
                    }
                });

    }

    private void setupRecyclerView(@NonNull RecyclerViewWithEmptyText recyclerView, @NonNull TextView emptyView) {
        currencyListRecyclerViewAdapter = new CurrencyListRecyclerViewAdapter(this, currencyList, mTwoPane);
        recyclerView.setEmptyView(emptyView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(currencyListRecyclerViewAdapter);
    }

    private void createProgressDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View progressView = inflater.inflate(R.layout.progress_view, null);
        builder.setView(progressView);
        progressDialog = builder.create();
    }

    private void showProgressDialog() {
        if (progressDialog != null && !progressDialog.isShowing()) {
            progressDialog.show();
        }
    }

    private void hideProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    public static class CurrencyListRecyclerViewAdapter
            extends RecyclerView.Adapter<CurrencyListRecyclerViewAdapter.ViewHolder> {

        private final CurrencyListActivity parentActivity;
        private List<Currency> currencyList;
        private final boolean twoPane;
        private final View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Currency currency = (Currency) view.getTag();

                ArrayList<String> keyList = new ArrayList<>(currency.getRates().keySet());
                ArrayList<String> valueList = CurrencyUtil.getValueListFromMap(keyList, currency.getRates());

                Bundle arguments = new Bundle();
                arguments.putStringArrayList(CurrencyRatesDetailFragment.ARG_CURRENCY_KEY, keyList);
                arguments.putStringArrayList(CurrencyRatesDetailFragment.ARG_CURRENCY_VALUE, valueList);

                if (twoPane) {
                    CurrencyRatesDetailFragment fragment = new CurrencyRatesDetailFragment();
                    fragment.setArguments(arguments);
                    parentActivity.getSupportFragmentManager().beginTransaction()
                            .replace(R.id.item_detail_container, fragment)
                            .commit();
                } else {
                    Context context = view.getContext();
                    Intent intent = new Intent(context, CurrencyRatesDetailActivity.class);
                    intent.putExtra(CurrencyRatesDetailFragment.ARG_CURRENCY_RATE, arguments);
                    context.startActivity(intent);
                }
            }
        };

        CurrencyListRecyclerViewAdapter(CurrencyListActivity parent, List<Currency> items,
                                        boolean twoPane) {
            this.currencyList = items;
            this.parentActivity = parent;
            this.twoPane = twoPane;
        }

        public void setCurrencyData(List<Currency> currencies) {
            this.currencyList = currencies;
            notifyDataSetChanged();
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.currency_item_view, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            holder.baseCurrency.setText(currencyList.get(position).getBase());
            holder.itemView.setTag(currencyList.get(position));
            holder.itemView.setOnClickListener(onClickListener);
        }

        @Override
        public int getItemCount() {
            return currencyList.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            final TextView baseCurrency;

            ViewHolder(View view) {
                super(view);
                baseCurrency = view.findViewById(R.id.base_currency);
            }
        }
    }
}
