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
import com.currency.rates.util.ProgressDialogUtil;

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
    ArrayList<String> baseCurrencyList = new ArrayList<>();
    private RecyclerViewWithEmptyText baseCurrencyListRecyclerView;
    private CurrencyListRecyclerViewAdapter baseCurrencyListRecyclerViewAdapter;
    private AlertDialog progressDialog;
    private final String KEY_BASE_CURRENCY_LIST = "key_base_currency_list";
    private final String KEY_CURRENT_POSITION = "key_current_position";
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

        baseCurrencyListRecyclerView = findViewById(R.id.item_list);
        TextView emptyView = findViewById(R.id.empty_view);
        setupRecyclerView(baseCurrencyListRecyclerView, emptyView);

        progressDialog = ProgressDialogUtil.createProgressDialog(CurrencyListActivity.this);
        if(savedInstanceState != null){
            baseCurrencyList = savedInstanceState.getStringArrayList(KEY_BASE_CURRENCY_LIST);
            if(baseCurrencyList == null || baseCurrencyList.size() == 0){
                ProgressDialogUtil.showProgressDialog(progressDialog);
                getBaseCurrencyList();
            }else {
                baseCurrencyListRecyclerViewAdapter.setCurrencyData(baseCurrencyList);
                currentPosition = savedInstanceState.getInt(KEY_CURRENT_POSITION);
            }
        }else {
            ProgressDialogUtil.showProgressDialog(progressDialog);
            getBaseCurrencyList();
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
        outState.putStringArrayList(KEY_BASE_CURRENCY_LIST, baseCurrencyList);
        outState.putInt(KEY_CURRENT_POSITION, currentPosition);
    }

    private void getBaseCurrencyList() {
        String defaultCurrency = getString(R.string.default_currency);
        Single<Currency> currency = new CurrencyRatesManager(getBaseContext(),
                CurrencyClient.getInstance(),
                CurrencyDatabase.getInstance(getBaseContext()).currencyDAO()).getCurrencyList(defaultCurrency);

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
                            baseCurrencyList.addAll(CurrencyUtil.getCurrencyList(currency));
                            baseCurrencyListRecyclerViewAdapter.setCurrencyData(baseCurrencyList);
                        }
                        ProgressDialogUtil.hideProgressDialog(progressDialog);
                    }

                    @Override
                    public void onError(Throwable e) {
                        ProgressDialogUtil.hideProgressDialog(progressDialog);
                    }
                });
    }

    private void setupRecyclerView(@NonNull RecyclerViewWithEmptyText recyclerView, @NonNull TextView emptyView) {
        baseCurrencyListRecyclerViewAdapter = new CurrencyListRecyclerViewAdapter(this, baseCurrencyList, mTwoPane);
        recyclerView.setEmptyView(emptyView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(baseCurrencyListRecyclerViewAdapter);
        baseCurrencyListRecyclerViewAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                recyclerView.smoothScrollToPosition(currentPosition);
            }
        });
    }

    private void setCurrentPosition(int position){
        currentPosition = position;
    }

    public static class CurrencyListRecyclerViewAdapter
            extends RecyclerView.Adapter<CurrencyListRecyclerViewAdapter.ViewHolder> {

        private final CurrencyListActivity parentActivity;
        private List<String> baseCurrencyList;
        private final boolean twoPane;
        private View selectedView;

        CurrencyListRecyclerViewAdapter(CurrencyListActivity parent, List<String> items,
                                        boolean twoPane) {
            this.baseCurrencyList = items;
            this.parentActivity = parent;
            this.twoPane = twoPane;
        }

        void setCurrencyData(List<String> baseCurrencyList) {
            this.baseCurrencyList = baseCurrencyList;
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
            holder.baseCurrency.setText(baseCurrencyList.get(position));
            holder.itemView.setTag(position);
            holder.itemView.setOnClickListener(onClickListener);
            if(twoPane && parentActivity.currentPosition == position){
                setViewSelected(holder.itemView);
                addCurrencyDetailsFragment(createBundle(baseCurrencyList.get(position)));
                parentActivity.baseCurrencyListRecyclerView.smoothScrollToPosition(position);
            }
            else {
                holder.itemView.setSelected(false);
            }
        }

        @Override
        public int getItemCount() {
            return baseCurrencyList.size();
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
                Bundle bundle = createBundle(baseCurrencyList.get(selectedPosition));
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

        private Bundle createBundle(String selectedCurrency){
            Bundle bundle = new Bundle();
            bundle.putString(CurrencyRatesDetailFragment.ARG_BASE_CURRENCY_KEY, selectedCurrency);
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
