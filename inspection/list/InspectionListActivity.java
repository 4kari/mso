package id.sisi.si.mso.ui.inspection.list;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BaseTransientBottomBar;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.crashlytics.android.Crashlytics;

import butterknife.BindView;
import butterknife.ButterKnife;
import id.sisi.si.mso.R;
import id.sisi.si.mso.data.model.InspectionList;
import id.sisi.si.mso.ui.base.BaseActivity;
import id.sisi.si.mso.ui.inspection.add.AddManualActivity;
import id.sisi.si.mso.ui.inspection.scan.ScanQRActivity;
import id.sisi.si.mso.ui.offline.OfflineOptionActivity;
import id.sisi.si.mso.utils.DialogUtil;
import id.sisi.si.mso.utils.NetworkUtil;
import id.sisi.si.mso.utils.SpacesItemDecoration;
import io.realm.OrderedRealmCollection;
import timber.log.Timber;

/**
 * Created by durrrr on 04-Oct-17.
 * Email: cab.purnama@gmail.com
 */
public class InspectionListActivity extends BaseActivity
        implements Contract.View, View.OnClickListener, SwipeRefreshLayout.OnRefreshListener,
        SearchView.OnQueryTextListener {

    public static final int ADD_INSPECTION_REQUEST_CODE = 101;
    public static final int NO_RESULT = 40;
    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    //@BindView(R.id.progress_bar)
    //ProgressBar progressBar;
    @BindView(R.id.fab)
    FloatingActionButton fab;
    @BindView(R.id.swipe_refresh_layout)
    SwipeRefreshLayout swipeRefreshLayout;
    private SearchView mSearchView;

    InspectionListPresenter mPresenter;
    InspectionListAdapter mAdapter;

    private LinearLayoutManager mLayoutManager;
    private RecyclerView.OnScrollListener mEndlessRecyclerViewScrollListener;
    private CharSequence[] filterOptions = {"Negative List", "Positive List"};
    private int selectedFilter = -1;
    private String SELECTED_FILTER_KEY = "selected_filter_key";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_inspection);

        setUnbinder(ButterKnife.bind(this));

        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(SELECTED_FILTER_KEY))
                selectedFilter = savedInstanceState.getInt(SELECTED_FILTER_KEY);
        }

        mPresenter = new InspectionListPresenter();
        mPresenter.attachView(this);
    }

    @Override
    public void initView() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        mLayoutManager = new LinearLayoutManager(this) {
            // override this as a workaround for inconsistent layout exception
            @Override
            public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
                try {
                    super.onLayoutChildren(recycler, state);
                } catch (IndexOutOfBoundsException e) {
                    Crashlytics.logException(e.fillInStackTrace());
                }
            }
        };
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.addItemDecoration(new SpacesItemDecoration(2));

        fab.setOnClickListener(this);
        swipeRefreshLayout.setOnRefreshListener(this);
    }

    @Override
    public void onClick(View view) {
        String[] menu_source = {
                "Scan QR Code",
                "Manual",
        };
        switch (view.getId()) {
            case R.id.fab:
                new MaterialDialog.Builder(this)
                        .title(R.string.source)
                        .titleColorRes(R.color.material_blue_grey_900)
                        .items(menu_source)
                        .itemsCallback(new MaterialDialog.ListCallback() {
                            @Override
                            public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                                //ifcompany code = 4000
                                switch (which) {
                                    case 0:
                                        startActivityForResult(new Intent(InspectionListActivity.this, ScanQRActivity.class), ADD_INSPECTION_REQUEST_CODE);
                                        break;
                                    case 1:
                                        startActivityForResult(new Intent(InspectionListActivity.this, AddManualActivity.class), ADD_INSPECTION_REQUEST_CODE);
                                        break;
                                    default:
                                        Snackbar.make(view, "Sumber belum tersedia", BaseTransientBottomBar.LENGTH_SHORT)
                                                .show();
                                        return;
                                }
                            }
                        })
                        .show();
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_inspection_list, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        mSearchView = (SearchView) searchItem.getActionView();
        mSearchView.setOnQueryTextListener(this);
        return true;
    }

    @Override
    public void populateFields(OrderedRealmCollection<InspectionList> inspectionList) {
        mAdapter = new InspectionListAdapter(inspectionList, true);
        recyclerView.setAdapter(mAdapter);

        if (mEndlessRecyclerViewScrollListener == null) {
            mEndlessRecyclerViewScrollListener = new InfiniteScrollListener(mLayoutManager) {
                @Override
                public void onLoadMore() {
                    mPresenter.loadMore();
                }
            };
            recyclerView.addOnScrollListener(mEndlessRecyclerViewScrollListener);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_filter:
                Integer[] savedFilter = new Integer[]{0, 1};
                if (selectedFilter != -1)
                    savedFilter = new Integer[]{selectedFilter};

                MaterialDialog dialog = new MaterialDialog.Builder(InspectionListActivity.this)
                        .title("Filter")
                        .items(filterOptions)
                        .itemsCallbackMultiChoice(savedFilter, new MaterialDialog.ListCallbackMultiChoice() {
                            @Override
                            public boolean onSelection(MaterialDialog dialog, Integer[] which, CharSequence[] text) {
                                return false;
                            }
                        })
                        .positiveText("Ok")
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                if (dialog.getSelectedIndices().length == 1) {
                                    selectedFilter = dialog.getSelectedIndices()[0];
                                }

                                if (dialog.getSelectedIndices().length == 2 | dialog.getSelectedIndices().length == 0) {
                                    selectedFilter = -1;
                                }

                                mPresenter.setCondition(selectedFilter);
                                mPresenter.getInspectionList();
                            }
                        })
                        .negativeText("Batal")
                        .build();
                dialog.show();
                break;
            case R.id.action_search:

                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        mPresenter.filter(newText);
        return false;
    }

    @Override
    public void onRefresh() {
        mPresenter.refreshData();
    }

    @Override
    public void showLoadMore() {
        if (mAdapter != null)
            mAdapter.setLoadingMore(true);
    }

    @Override
    public void hideLoadMore() {
        if (mAdapter != null)
            mAdapter.setLoadingMore(false);

        if (swipeRefreshLayout.isRefreshing())
            swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void showProgress() {
        //progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideProgress() {
        swipeRefreshLayout.setRefreshing(false);
        //progressBar.setVisibility(View.GONE);
    }

    @Override
    public void onListFailedToUpdate() {
        showDialog("Gagal", "Terjadi kesalahan mengambil data dari server");
        hideProgress();
    }

    @Override
    public void onDataNeedSync() {
        MaterialDialog dialog = new MaterialDialog.Builder(this)
                .title("Peringatan")
                .content("Anda masih memiliki data offline. Sinkronasikan data terlebih dahulu untuk melanjutkan transaksi data secara online")
                .positiveText("Sync")
                .negativeText("Cancel")
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        Intent intent = new Intent(InspectionListActivity.this, OfflineOptionActivity.class);
                        startActivity(intent);
                        finish();
                    }
                })
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                        hideProgress();
                    }
                }).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Timber.d("rikues " + requestCode + " " + resultCode);
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ADD_INSPECTION_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                if(NetworkUtil.isConnected())
                    mPresenter.refreshData();
            }
            if(resultCode == RESULT_FIRST_USER) {
                Toast.makeText(InspectionListActivity.this, "Equipment tidak ditemukan di plant anda",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void showDialog(String title, String messsage) {
        DialogUtil.showBasicDialog(this, title, messsage);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(SELECTED_FILTER_KEY, selectedFilter);
    }

    @Override
    public void onBackPressed() {
        if (!mSearchView.isIconified()) {
            mSearchView.setIconified(true);
            return;
        }
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        mPresenter.detachView();
        super.onDestroy();
    }
}
