package id.sisi.si.mso.ui.inspection.add;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import butterknife.BindView;
import butterknife.ButterKnife;
import id.sisi.si.mso.R;
import id.sisi.si.mso.data.model.EquipmentInfo;
import id.sisi.si.mso.data.model.Order;
import id.sisi.si.mso.ui.base.BaseFragment;
import id.sisi.si.mso.ui.confirmationOrder.action.ActionActivity;
import id.sisi.si.mso.ui.confirmationOrder.assign.AssignActivity;
import id.sisi.si.mso.ui.confirmationOrder.detail.OrderDetailActivity;
import id.sisi.si.mso.ui.confirmationOrder.list.InfiniteScrollListener;
import id.sisi.si.mso.ui.confirmationOrder.list.OrderItemAdapter;
import id.sisi.si.mso.ui.offline.OfflineOptionActivity;
import id.sisi.si.mso.utils.DialogUtil;
import id.sisi.si.mso.utils.TextUtils;
import io.realm.OrderedRealmCollection;

/**
 * Created by durrrr on 19-Nov-17.
 * Email: cab.purnama@gmail.com
 */
public class EquipmentOrderListFragment
        extends BaseFragment implements Contract.EquipmentOrderListView, SwipeRefreshLayout.OnRefreshListener, OrderItemAdapter.OnItemMenuClicked {

    private static final String ARG_SECTION_KEY = "section_number";

    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    @BindView(R.id.swipeRefreshLayout)
    SwipeRefreshLayout swipeRefreshLayout;

    private LinearLayoutManager mLayoutManager;
    private OrderItemAdapter mAdapter;
    private EquipmentOrderListPresenter mPresenter;
    private RecyclerView.OnScrollListener mEndlessRecyclerViewScrollListener;

    private int mTransactionMode;

    public EquipmentOrderListFragment() {
    }

    /**
     * Returns a new instance of this fragment for the given mSection
     * number.
     */
    public static EquipmentOrderListFragment newInstance(String section, EquipmentInfo equipmentInfo, int transactionMode) {
        EquipmentOrderListFragment fragment = new EquipmentOrderListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_SECTION_KEY, section);
        args.putParcelable(EquipmentInfo.TYPE_EXTRA, equipmentInfo);
        args.putInt(EquipmentOrderListPresenter.TRANSACTION_MODE_KEY, transactionMode);
        fragment.setArguments(args);
        return fragment;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_order_abnormal, container, false);
        setUnbinder(ButterKnife.bind(this, rootView));

        String section = getArguments().getString(ARG_SECTION_KEY);
        EquipmentInfo equipmentInfo = getArguments().getParcelable(EquipmentInfo.TYPE_EXTRA);
        int transactionMode = getArguments().getInt(EquipmentOrderListPresenter.TRANSACTION_MODE_KEY);

        mPresenter = new EquipmentOrderListPresenter(section, equipmentInfo, transactionMode);
        mPresenter.attachView(this);

        return rootView;
    }

    @Override
    public void initView(int transactionMode) {
        mTransactionMode = transactionMode;

        mLayoutManager = new LinearLayoutManager(getContext());
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));

        swipeRefreshLayout.setOnRefreshListener(this);
    }

    @Override
    public void populateFields(OrderedRealmCollection<Order> data) {
        mAdapter = new OrderItemAdapter(data);
        recyclerView.setAdapter(mAdapter);
        mAdapter.setOnItemMenuClicked(this);

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
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ActionActivity.ACTION_REQUEST_KEY) {
            if (resultCode == Activity.RESULT_OK)
                mPresenter.refreshData();
        }
    }

    @Override
    public void onDestroy() {
        mPresenter.detachView();
        super.onDestroy();
    }

    @Override
    public void filterResult(String query) {
        if (TextUtils.isNullOrEmpty(query))
            query = "";

        mPresenter.filterOrder(query);
    }

    @Override
    public void onFilterCompleted(OrderedRealmCollection<Order> data) {
        mAdapter.updateData(data);
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
    public void hideProgress() {
        swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onListFailedToUpdate() {
        showDialog("Gagal", "Terjadi kesalahan mengambil data dari server");
        hideProgress();
    }

    @Override
    public void onDataNeedSync() {
        MaterialDialog dialog = new MaterialDialog.Builder(getContext())
                .title("Gagal")
                .content("Anda masih memiliki data offline. Sinkronasikan data terlebih dahulu untuk melanjutkan transaksi data secara online")
                .positiveText("Sync")
                .negativeText("Cancel")
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        Intent intent = new Intent(getContext(), OfflineOptionActivity.class);
                        startActivity(intent);
                    }
                })
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                        hideProgress();
                    }
                }).build();
        dialog.show();
    }

    @Override
    public void showDialog(String title, String messsage) {
        DialogUtil.showBasicDialog(getContext(), title, messsage);
    }

    @Override
    public void onRefresh() {
        mPresenter.refreshData();
    }

    @Override
    public void onItemClicked(View view, Order order) {
        Intent intent;
        switch (view.getId()) {
            case R.id.btn_assign:
                intent = new Intent(view.getContext(), AssignActivity.class);
                intent.putExtra(EquipmentOrderListActivity.ORDER_INTENT_KEY, order);
                startActivityForResult(intent, ActionActivity.ACTION_REQUEST_KEY);
                return;
            case R.id.btn_action:
                intent = new Intent(view.getContext(), ActionActivity.class);
                intent.putExtra(ActionActivity.TRANSACTION_MODE_KEY, mTransactionMode);
                break;
            default:
                intent = new Intent(getContext(), OrderDetailActivity.class);
                break;
        }
        if (intent == null)
            return;

        intent.putExtra(EquipmentOrderListActivity.ORDER_INTENT_KEY, order);
        startActivity(intent);
    }

    @Override
    public void onThereIsUnsyncedData() {
        View rootView = getActivity().getWindow().getDecorView().findViewById(android.R.id.content);
        Snackbar.make(rootView, "Terdapat data yang belum tersinkronisasi, anda tidak bisa transaksi dengan server", Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void onOffline() {
        View rootView = getActivity().getWindow().getDecorView().findViewById(android.R.id.content);
        Snackbar.make(rootView, "Jaringan tidak ditemukan, transaksi dilakukan menggunakan data offline", Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void showProgress(String title, String message) {
        DialogUtil.showProgressDialog(getContext(), title, message, false);
    }

    @Override
    public void onNotAvailable() {
        DialogUtil.showEndDialog(getContext(), "Gagal", "Menu tidak tersedia dalam mode offline");
    }

}
