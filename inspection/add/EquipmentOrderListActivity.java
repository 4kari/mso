package id.sisi.si.mso.ui.inspection.add;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.ThemedSpinnerAdapter;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import id.sisi.si.mso.R;
import id.sisi.si.mso.data.model.EquipmentInfo;
import id.sisi.si.mso.data.model.Order;
import id.sisi.si.mso.ui.base.BaseActivity;
import id.sisi.si.mso.ui.dashboard.DashboardActivity;

/**
 * Created by durrrr on 19-Nov-17.
 * Email: cab.purnama@gmail.com
 */
public class EquipmentOrderListActivity
        extends BaseActivity implements SearchView.OnQueryTextListener {

    public static final String ORDER_INTENT_KEY = "order_intent_key";
    private static final String[] HEADER = new String[]{
            "UNCONFIRMED",
            "PARTIAL CONFIRMED",
    };
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.spinner)
    Spinner mSpinner;
    @BindView(R.id.btn_add_inspection)
    Button btnAddInspection;
    private EquipmentInfo mEquipmentInfo;

    //    @BindView(R.id.container)
//    NestedScrollView container;
//
//    @BindView(R.id.progress_bar)
//    ProgressBar progressBar;
    private MenuItem mSearchItem;
    private SearchView mSearchView;
    private int selectedTab = 0;
    private EquipmentOrderListFragment currentFragment;
    private SpinnerAdapter mAdapter;
    private int mTransactionMode;
    private int mBearingType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inspection_order);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        mEquipmentInfo = getIntent().getParcelableExtra(EquipmentInfo.TYPE_EXTRA);
        mTransactionMode = getIntent().getIntExtra(AddInspectionPresenter.TRANSACTION_MODE_KEY, AddInspectionPresenter.ONLINE_MODE);
        mBearingType = getIntent().getIntExtra(AddInspectionActivity.BEARING_TYPE, AddInspectionActivity.NON_BEARING);

        mAdapter = new SpinnerAdapter(getApplicationContext(), HEADER) {
            @Override
            public boolean isEnabled(int position) {
                switch (mEquipmentInfo.getInOrder()) {
                    case EquipmentInfo.IN_ORDER_UNCONFIRMED:
                        if(position == 1)
                            return false;
                        break;
                    case EquipmentInfo.IN_ORDER_PARTIAL:
                        if(position == 0)
                            return false;
                        break;
                    case EquipmentInfo.IN_ORDER_BOTH:
                        break;
                }
                return super.isEnabled(position);
            }
        };

        mSpinner.setAdapter(mAdapter);
        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String section = position == 0 ? Order.Type.UNCONFIRMED : Order.Type.PARTIAL_CONFIRMED;
                currentFragment = EquipmentOrderListFragment.newInstance(section, mEquipmentInfo, mTransactionMode);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, currentFragment)
                        .commit();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        selectedTab = getIntent().getIntExtra(DashboardActivity.SELECTED_EXTRA, 0);
        mSpinner.setSelection(selectedTab, true);

        btnAddInspection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(EquipmentOrderListActivity.this, AddInspectionActivity.class);
                intent.putExtra(EquipmentInfo.TYPE_EXTRA, mEquipmentInfo);
                intent.putExtra(AddInspectionPresenter.TRANSACTION_MODE_KEY, mTransactionMode);
                intent.putExtra(AddInspectionActivity.BEARING_TYPE, mBearingType);
                startActivity(intent);
                finish();
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search_logout, menu);
        mSearchItem = menu.findItem(R.id.action_search);
        mSearchView = (SearchView) MenuItemCompat.getActionView(mSearchItem);
        mSearchView.setOnQueryTextListener(this);
        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        if (currentFragment != null)
            currentFragment.filterResult(newText);
        return false;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {

            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

//            container.setVisibility(show ? OrderListView.GONE : OrderListView.VISIBLE);
//            container.animate().setDuration(shortAnimTime).alpha(
//                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
//                @Override
//                public void onAnimationEnd(Animator animation) {
//                    container.setVisibility(show ? OrderListView.GONE : OrderListView.VISIBLE);
//                }
//            });

//            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
//            progressBar.animate().setDuration(shortAnimTime).alpha(
//                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
//                @Override
//                public void onAnimationEnd(Animator animation) {
//                    progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
//                }
//            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
//            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
            //container.setVisibility(show ? OrderListView.GONE : OrderListView.VISIBLE);
        }
    }

    @Override
    public void showProgress() {
        showProgress(true);
    }

    @Override
    public void showProgress(String title, String message) {

    }

    @Override
    public void hideProgress() {
        showProgress(false);
    }

    @Override
    public void showUnauthorizedError() {

    }

    @Override
    public void showError(String title, String errorMessage) {

    }

    private class SpinnerAdapter extends ArrayAdapter<String> implements ThemedSpinnerAdapter {

        private final Helper mDropDownHelper;

        public SpinnerAdapter(@NonNull Context context, @NonNull String[] objects) {
            super(context, android.R.layout.simple_list_item_1, objects);
            mDropDownHelper = new Helper(context);
        }

        @Override
        public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            View view;
            if (convertView == null) {
                LayoutInflater inflater = mDropDownHelper.getDropDownViewInflater();
                view = inflater.inflate(android.R.layout.simple_list_item_1, parent, false);
            } else {
                view = convertView;
            }
            TextView textView = view.findViewById(android.R.id.text1);
            textView.setText(getItem(position));

            switch (mEquipmentInfo.getInOrder()) {
                case EquipmentInfo.IN_ORDER_UNCONFIRMED:
                    if(position == 1)
                        textView.setTextColor(Color.GRAY);
                    break;
                case EquipmentInfo.IN_ORDER_PARTIAL:
                    if(position == 0)
                        textView.setTextColor(Color.GRAY);
                    break;
                case EquipmentInfo.IN_ORDER_BOTH:
                    break;
            }

            return view;
        }

        @Nullable
        @Override
        public Resources.Theme getDropDownViewTheme() {
            return mDropDownHelper.getDropDownViewTheme();
        }

        @Override
        public void setDropDownViewTheme(@Nullable Resources.Theme theme) {
            mDropDownHelper.setDropDownViewTheme(theme);
        }
    }
}
