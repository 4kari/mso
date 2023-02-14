package id.sisi.si.mso.ui.inspection.list;

import android.util.Log;

import java.util.List;

import id.sisi.si.mso.data.AppDataManager;
import id.sisi.si.mso.data.api.MsoService;
import id.sisi.si.mso.data.api.ServiceGenerator;
import id.sisi.si.mso.data.api.model.ListResponse;
import id.sisi.si.mso.data.model.InspectionList;
import id.sisi.si.mso.ui.base.BasePresenter;
import id.sisi.si.mso.utils.NetworkUtil;
import id.sisi.si.mso.utils.SyncManager;
import id.sisi.si.mso.utils.TextUtils;
import io.realm.OrderedRealmCollection;
import io.realm.Realm;
import okhttp3.internal.huc.OkHttpsURLConnection;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

/**
 * Created by durrrr on 04-Oct-17.
 * Email: cab.purnama@gmail.com
 */
public class InspectionListPresenter extends BasePresenter<Contract.View> implements Contract.Presenter {

    private static final int ITEM_PER_PAGE = 40;

    private final AppDataManager mAppDataManager;

    private int mServerTotalData = -1;
    private int mCurrentPage = -1;
    private boolean mIsLoading = false;
    private boolean mIsThereLocalData = false;

    private String mQuery = "";
    private int mCondition = -1;

    public InspectionListPresenter() {
        mAppDataManager = AppDataManager.getInstance();
    }

    @Override
    public void attachView(Contract.View view) {
        super.attachView(view);

        if (getView() != null)
            getView().initView();
        getInspectionList();
    }

    @Override
    public void getInspectionList() {
        if (NetworkUtil.isConnected() & !SyncManager.getInstance().isNeedSyncForInspection()) {
            refreshData();
        } else {
            OrderedRealmCollection<InspectionList> inspectionList;
            if (mCondition == -1)
                inspectionList = mAppDataManager.getUnsyncInspectionList(mQuery, null);
            else
                inspectionList = mAppDataManager.getUnsyncInspectionList(mQuery, mCondition);

            if (inspectionList != null & getView() != null) {
                getView().populateFields(inspectionList);
                getView().hideProgress();
                mIsThereLocalData = true;
            }
        }
    }

    @Override
    public void loadMore() {
        if (SyncManager.getInstance().isNeedSyncForInspection()) {
            return;
        }

        if (mIsLoading | !NetworkUtil.isConnected()) {
            return;
        }
        mIsLoading = true;

        Timber.d("Masuk 2");
        mCurrentPage++;
        int start = ((mCurrentPage) * ITEM_PER_PAGE) + 1;
        int end = start + ITEM_PER_PAGE - 1;

        if (mServerTotalData != -1 & start > mServerTotalData) {
            mCurrentPage--;
            mIsLoading = false;
            Timber.d("Masuk 2");
            return;
        }
        Timber.d("Masuk 3");

        if (getView() != null) {
            getView().showLoadMore();
            loadInspectionList(start, end);
        }
    }

    @Override
    public void refreshData() {
        if (!NetworkUtil.isConnected()) {
            getView().onListFailedToUpdate();
            return;
        }

        if (SyncManager.getInstance().isNeedSyncForInspection()) {
            if (getView() != null)
                getView().onDataNeedSync();
            return;
        }

        if (mIsThereLocalData)
            mIsThereLocalData = false;

        Timber.d("Masuk 1");

        if (getView() != null)
            getView().showProgress();

        mCurrentPage = -1;
        mAppDataManager.clearInspectionList(new Realm.Transaction.OnSuccess() {
            @Override
            public void onSuccess() {
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
    public void setCondition(int condition) {
        mCondition = condition;
    }

    @Override
    public void filter(String query) {
        if (TextUtils.isNullOrEmpty(query))
            query = "";

        mQuery = query;
        if (mServerTotalData <= 0)
            mServerTotalData = 1;

        getInspectionList();
    }

    @Override
    public void loadInspectionList(int seqNumStart, int seqNumEnd) {
        if (getView() != null) {
            getView().showProgress();
        }
        Call<ListResponse<InspectionList>> call = ServiceGenerator.getInstance().getRetrofit().create(MsoService.class)
                .getInspectionList(seqNumStart, seqNumEnd, mCondition != -1 ? mCondition : null, mQuery);
        call.enqueue(new Callback<ListResponse<InspectionList>>() {
            @Override
            public void onResponse(Call<ListResponse<InspectionList>> call, Response<ListResponse<InspectionList>> response) {
                if (!handleErrorResponse(response)) {
                    if (getView() != null)
                        getView().hideLoadMore();
                    return;
                }

                final List<InspectionList> inspections = response.body().getData();
                mServerTotalData = response.body().getTotal();
                mAppDataManager.addInspectionList(inspections, new Realm.Transaction.OnSuccess() {
                            @Override
                            public void onSuccess() {
                                if (!mIsThereLocalData & NetworkUtil.isConnected()) {
                                    OrderedRealmCollection<InspectionList> inspectionList = mAppDataManager.getInspectionList();

                                    if (inspectionList != null) {
                                        if (getView() != null) {
                                            getView().populateFields(inspectionList);
                                            mIsThereLocalData = true;
                                        }
                                    }
                                }

                                mIsLoading = false;

                                if (getView() != null) {
                                    getView().hideLoadMore();
                                    getView().hideProgress();
                                }
                            }
                        },
                        new Realm.Transaction.OnError() {
                            @Override
                            public void onError(Throwable error) {
                                if (getView() != null) {
                                    getView().onListFailedToUpdate();
                                    getView().hideLoadMore();
                                    getView().hideProgress();
                                }
                            }
                        });
            }

            @Override
            public void onFailure(Call<ListResponse<InspectionList>> call, Throwable t) {
                mIsLoading = false;
                if (getView() != null) {
                    getView().onListFailedToUpdate();
                    getView().hideLoadMore();
                    getView().hideProgress();
                }
            }
        });
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
