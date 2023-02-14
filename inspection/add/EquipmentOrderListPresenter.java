package id.sisi.si.mso.ui.inspection.add;

import com.crashlytics.android.Crashlytics;

import java.util.List;

import id.sisi.si.mso.data.AppDataManager;
import id.sisi.si.mso.data.api.MsoService;
import id.sisi.si.mso.data.api.ServiceGenerator;
import id.sisi.si.mso.data.api.model.ListResponse;
import id.sisi.si.mso.data.model.EquipmentInfo;
import id.sisi.si.mso.data.model.Order;
import id.sisi.si.mso.ui.base.BasePresenter;
import id.sisi.si.mso.utils.NetworkUtil;
import id.sisi.si.mso.utils.SyncManager;
import io.realm.OrderedRealmCollection;
import io.realm.Realm;
import okhttp3.internal.huc.OkHttpsURLConnection;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

/**
 * Created by durrrr on 19-Nov-17.
 * Email: cab.purnama@gmail.com
 */
public class EquipmentOrderListPresenter
        extends BasePresenter<Contract.EquipmentOrderListView> implements Contract.EquipmentOrderListPresenter {

    public static final String TRANSACTION_MODE_KEY = "transaction_mode";
    public static final int ONLINE_MODE = 0;
    public static final int OFFLINE_MODE = 1;
    private static final int ITEM_PER_PAGE = 40;
    private final AppDataManager mAppDataManager;
    private String mOrderType;
    private int serverTotalData = -1;
    private int currentPage = -1;
    private boolean isLoading = false;
    private boolean isThereLocalData = false;
    private String mQuery = "";
    private EquipmentInfo mEquipmentInfo;
    private int mTransactionMode = ONLINE_MODE;
    private String pgQuery = "";


    public EquipmentOrderListPresenter(String orderType, EquipmentInfo equipmentInfo, int transactionMode) {
        mOrderType = orderType;
        mEquipmentInfo = equipmentInfo;
        mTransactionMode = transactionMode;
        mAppDataManager = AppDataManager.getInstance();
    }

    @Override
    public void attachView(Contract.EquipmentOrderListView view) {
        super.attachView(view);

        if (!NetworkUtil.isConnected() | SyncManager.getInstance().isNeedSyncForOrder()) {
            mTransactionMode = OFFLINE_MODE;

            switch (mOrderType) {
                case Order.Type.UNCONFIRMED:
                    mOrderType = Order.Type.OFFLINE_UNCONFIRMED;
                    break;
                case Order.Type.PARTIAL_CONFIRMED:
                    mOrderType = Order.Type.OFFLINE_PARTIAL_CONFIRMED;
                    break;
                default:
                    break;
            }

            if (!NetworkUtil.isConnected()) {
                if (!mOrderType.equalsIgnoreCase(Order.Type.OFFLINE_UNCONFIRMED)
                        & !mOrderType.equalsIgnoreCase(Order.Type.OFFLINE_PARTIAL_CONFIRMED)) {
                    if (getView() != null)
                        getView().onNotAvailable();
                    return;
                }
            }

            if (SyncManager.getInstance().isNeedSyncForOrder()) {
                if (getView() != null)
                    getView().onThereIsUnsyncedData();
            }
        }

        if (getView() != null) {
            getView().initView(mTransactionMode);
            getOrderList(true);
        }
    }

    private void getOrderList(boolean isInit) {
        if (NetworkUtil.isConnected() & !SyncManager.getInstance().isNeedSyncForOrder()) {
            refreshData();
        } else {
            if (mOrderType == Order.Type.OFFLINE_PARTIAL_CONFIRMED | mOrderType == Order.Type.OFFLINE_UNCONFIRMED) {
                if (!NetworkUtil.isConnected()) {
                    if (isInit)
                        getView().onOffline();
                }

                if (SyncManager.getInstance().isNeedSyncForOrder()) {
                    if (isInit) {
                        if (getView() != null)
                            getView().onThereIsUnsyncedData();
                    }
                }

                OrderedRealmCollection<Order> orders = mAppDataManager.getOrders(mOrderType, mEquipmentInfo.getName(), mQuery);
                if (orders != null) {
                    getView().populateFields(orders);
                    getView().hideProgress();
                    isThereLocalData = true;
                }
            }
        }
    }

    @Override
    public void filterOrder(String query) {
        mQuery = query;
        getOrderList(false);
    }

    @Override
    public void refreshData() {
        if (!NetworkUtil.isConnected()) {
            if (getView() != null)
                getView().onListFailedToUpdate();
            return;
        }

        if (SyncManager.getInstance().isNeedSyncForOrder()) {
            if (getView() != null)
                getView().onDataNeedSync();
            return;
        }

        if (getView() != null)
            getView().showProgress();

        currentPage = -1;
        mAppDataManager.clearOrder(mOrderType, new Realm.Transaction.OnSuccess() {
            @Override
            public void onSuccess() {
                if (getView() != null)
                    getView().hideProgress();

                loadMore();
            }
        }, new Realm.Transaction.OnError() {
            @Override
            public void onError(Throwable error) {
                if (getView() != null)
                    getView().hideProgress();
            }
        });
    }

    @Override
    public void loadOrderList(int seqNumStart, int seqNumEnd) {
        if (getView() != null)
            getView().showLoadMore();

        Call<ListResponse<Order>> call = ServiceGenerator.getInstance()
                .getRetrofit().create(MsoService.class).getListOrder(mOrderType, pgQuery, seqNumStart, seqNumEnd, mEquipmentInfo.getName(), mQuery, null);
        call.enqueue(new Callback<ListResponse<Order>>() {
            @Override
            public void onResponse(Call<ListResponse<Order>> call, Response<ListResponse<Order>> response) {
                if (!handleErrorResponse(response)) {
                    if (getView() != null) {
                        getView().hideLoadMore();
                        getView().showDialog("Error", "Data tidak ditemukan");
                    }
                    return;
                }

                final List<Order> orders = response.body().getData();
                serverTotalData = response.body().getTotal();
                if (serverTotalData <= 0)
                    serverTotalData = 1;

                mAppDataManager.addOrders(mOrderType, orders, new Realm.Transaction.OnSuccess() {
                    @Override
                    public void onSuccess() {
                        if (!isThereLocalData & NetworkUtil.isConnected()) {
                            OrderedRealmCollection<Order> orderList = mAppDataManager.getOrders(mOrderType, mQuery);
                            if (orders != null) {
                                if (getView() != null)
                                    getView().populateFields(orderList);
                                isThereLocalData = true;
                            }
                        }

                        isLoading = false;

                        if (getView() != null) {
                            getView().hideLoadMore();
                            getView().hideProgress();
                        }
                    }
                }, new Realm.Transaction.OnError() {
                    @Override
                    public void onError(Throwable error) {
                        Timber.d(error.getMessage());
                        if (getView() == null)
                            return;
                        getView().hideLoadMore();
                        getView().showError("Error Occurred", "Can not add to database");
                    }
                });
            }


            @Override
            public void onFailure(Call<ListResponse<Order>> call, Throwable t) {
                Timber.d(t.getMessage());
                Crashlytics.logException(t);
                isLoading = false;
                if (getView() != null) {
                    getView().onListFailedToUpdate();
                    getView().hideLoadMore();
                    getView().hideProgress();
                }
            }
        });
    }

    @Override
    public void loadMore() {
        if (SyncManager.getInstance().isNeedSyncForInspection()) {
            return;
        }

        if (isLoading | !NetworkUtil.isConnected()) {
            return;
        }
        isLoading = true;

        currentPage++;
        int start = ((currentPage) * ITEM_PER_PAGE) + 1;
        int end = start + ITEM_PER_PAGE - 1;

        if (serverTotalData != -1 & start > serverTotalData) {
            currentPage--;
            isLoading = false;
            return;
        }

        if (getView() != null)
            getView().showLoadMore();
        loadOrderList(start, end);
    }

    private boolean handleErrorResponse(Response response) {
        if (response.code() == OkHttpsURLConnection.HTTP_UNAUTHORIZED || response.code() == OkHttpsURLConnection.HTTP_INTERNAL_ERROR) {
            if (response.code() == OkHttpsURLConnection.HTTP_UNAUTHORIZED) {
                if (getView() != null)
                    getView().showDialog("Error", "Session expired");
                logOut();
            }
            return false;
        }
        return true;
    }

    @Override
    public void logOut() {
        mAppDataManager.logOut();
        if (getView() != null)
            getView().onLogoutSuccess();
    }
}
