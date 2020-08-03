package com.currency.rates.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
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

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
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
    private ArrayList<Currency> currencyList = new ArrayList<>();
    private RecyclerViewWithEmptyText currencyListRecyclerView;
    private CurrencyListRecyclerViewAdapter currencyListRecyclerViewAdapter;
    private AlertDialog progressDialog;
    private final String CURRENCY_LIST_KEY = "currency_list_key";
    private final String CURRENT_POSITION = "current_position";
    private int currentPosition = 0;

    private CompositeDisposable disposable = new CompositeDisposable();
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
        if(savedInstanceState != null){
            currencyList = savedInstanceState.getParcelableArrayList(CURRENCY_LIST_KEY);
            if(currencyList == null || currencyList.size() == 0){
                showProgressDialog();
                getCurrencyRates();
            }else {
                currencyListRecyclerViewAdapter.setCurrencyData(currencyList);
                currentPosition = savedInstanceState.getInt(CURRENT_POSITION);
            }
        }else {
            showProgressDialog();
            getCurrencyRates();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(disposable != null){
            disposable.clear();
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(CURRENCY_LIST_KEY, currencyList);
        outState.putInt(CURRENT_POSITION, currentPosition);
    }


    private void getCurrencyRates() {
        Single<List<Currency>> currencyRates = new CurrencyRatesManager(getBaseContext(),
                CurrencyClient.getInstance(),
                CurrencyDatabase.getInstance(getBaseContext()).currencyDAO()).getAllCurrencyRates();

        currencyRates.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<List<Currency>>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        disposable.add(d);
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
        currencyListRecyclerViewAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
        @Override
        public void onChanged() {
            super.onChanged();
            recyclerView.smoothScrollToPosition(currentPosition);
        }
        });
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

    private void setCurrentPosition(int position){
        currentPosition = position;
    }

    public static class CurrencyListRecyclerViewAdapter
            extends RecyclerView.Adapter<CurrencyListRecyclerViewAdapter.ViewHolder> {

        private final CurrencyListActivity parentActivity;
        private List<Currency> currencyList;
        private final boolean twoPane;
        private View selectedView;

        CurrencyListRecyclerViewAdapter(CurrencyListActivity parent, List<Currency> items,
                                        boolean twoPane) {
            this.currencyList = items;
            this.parentActivity = parent;
            this.twoPane = twoPane;
        }

        void setCurrencyData(List<Currency> currencies) {
            this.currencyList = currencies;
            notifyDataSetChanged();
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.currency_item_view, parent, false);
            view.setBackground(parent.getContext().getResources().getDrawable(R.drawable.list_selector));
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            holder.baseCurrency.setText(currencyList.get(position).getBase());
            holder.itemView.setTag(position);
            holder.itemView.setOnClickListener(onClickListener);
            if(twoPane && parentActivity.currentPosition == position){
                setViewSelected(holder.itemView);
                addCurrencyDetailsFragment(createBundle(currencyList.get(position)));
                parentActivity.currencyListRecyclerView.smoothScrollToPosition(position);
            }
            else {
                holder.itemView.setSelected(false);
            }
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

        private final View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int selectedPosition = (int) view.getTag();
                parentActivity.setCurrentPosition(selectedPosition);
                Bundle bundle = createBundle(currencyList.get(selectedPosition));
                if (twoPane) {
                    setViewBackground(view, selectedView);
                    addCurrencyDetailsFragment(bundle);
                } else {
                    startCurrencyDetailsActivity(parentActivity, bundle);
                }
            }
        };

        private void setViewBackground(View currentView, View previousView){
                if(previousView != null){
                    previousView.setSelected(false);
                }
                setViewSelected(currentView);
        }

        private Bundle createBundle(Currency currency){
            ArrayList<String> keyList = new ArrayList<>(currency.getRates().keySet());
            ArrayList<String> valueList = CurrencyUtil.getValueListFromMap(keyList, currency.getRates());
            Bundle bundle = new Bundle();
            bundle.putStringArrayList(CurrencyRatesDetailFragment.ARG_CURRENCY_KEY, keyList);
            bundle.putStringArrayList(CurrencyRatesDetailFragment.ARG_CURRENCY_VALUE, valueList);
            return bundle;
        }

        private void addCurrencyDetailsFragment(Bundle arguments){
            CurrencyRatesDetailFragment fragment = new CurrencyRatesDetailFragment();
            fragment.setArguments(arguments);
            parentActivity.getSupportFragmentManager().beginTransaction()
                    .replace(R.id.item_detail_container, fragment, CurrencyRatesDetailFragment.CURRENCY_RATES_FRAGMENT_TAG)
                    .commit();
        }

        private void startCurrencyDetailsActivity(Context context, Bundle arguments){
            Intent intent = new Intent(context, CurrencyRatesDetailActivity.class);
            intent.putExtra(CurrencyRatesDetailFragment.ARG_CURRENCY_RATE, arguments);
            context.startActivity(intent);
        }

        private void setViewSelected(View view){
            view.setSelected(true);
            selectedView = view;
        }
    }
}
