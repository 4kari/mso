package id.sisi.si.mso.ui.inspection.add;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import id.sisi.si.mso.data.AppDataManager;
import id.sisi.si.mso.data.api.MsoService;
import id.sisi.si.mso.data.api.ServiceGenerator;
import id.sisi.si.mso.data.api.model.BaseResponse;
import id.sisi.si.mso.data.model.Abnormal;
import id.sisi.si.mso.data.model.Nomenclature;
import id.sisi.si.mso.ui.abnormality.addOrEdit.AbnormalityAddOrEditActivity;
import id.sisi.si.mso.ui.base.BasePresenter;
import id.sisi.si.mso.utils.NetworkUtil;
import id.sisi.si.mso.utils.TextUtils;
import io.realm.Realm;
import okhttp3.internal.huc.OkHttpsURLConnection;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Luhur on 5/3/2018.
 */

public class AddManualPresenter extends BasePresenter<Contract.AddManualView> implements Contract.AddManualPresenter {
    private AppDataManager mAppDataManager;
    private MsoService msoService = ServiceGenerator.getInstance().getRetrofit().create(MsoService.class);
    private static final String TAG = "" ;
    private Abnormal mTempAbnormal;

    public AddManualPresenter(Abnormal abnormal) {
        mAppDataManager = AppDataManager.getInstance();
        mTempAbnormal = abnormal;
    }

    @Override
    public void getNomenclature() {

    }

    @Override
    public void attachView(Contract.AddManualView view) {
        super.attachView(view);

        if (NetworkUtil.isConnected() & getView() != null)
            getView().initView(mTempAbnormal, AddManualActivity.ONLINE_MODE);
        else
            getView().initView(mTempAbnormal, AddManualActivity.OFFLINE_MODE);
    }

    @Override
    public void loadNomenclature(String keyword, String plant, int transactionMode) {
        if (transactionMode == AddManualActivity.ONLINE_MODE & NetworkUtil.isConnected())
            onlineLoadNomenclature(keyword, plant);
        else
            offlineLoadNomenclature(keyword);
    }

    private void onlineLoadNomenclature(final String keyword, final String plant) {
        Call<BaseResponse<List<Nomenclature>>>
                call = msoService.autoCompleteNomenclature(keyword, plant,10);
        if (TextUtils.isNullOrEmpty(keyword)) {
            return;
        }

        if (keyword.length() < 2)

            return;
        if (call.isExecuted()) {
            call.cancel();
        }

        if (getView() != null)
            getView().hideProgressBarFindNomenclature(false);

        call.enqueue(new Callback<BaseResponse<List<Nomenclature>>>() {
            @Override
            public void onResponse(Call<BaseResponse<List<Nomenclature>>> call, Response<BaseResponse<List<Nomenclature>>> response) {
                if (!handleErrorResponse(response)) {
                    return;
                }

                if (response.isSuccessful()) {
                    final List<Nomenclature> nomenclatures = response.body().getData();
                    final ArrayList<String> nomenList = new ArrayList<String>();
                    for(int i=0; i<nomenclatures.size(); i++)
                        nomenList.add(nomenclatures.get(i).getName());
                        //Log.e(TAG, "plant:" + nomenclatures.get(i).getMplant()); }
                    if (nomenclatures.size() > 0) {
                        if (getView() == null)
                            return;

                        mAppDataManager.addNomenclatures(nomenclatures, new Realm.Transaction.OnSuccess() {
                            @Override
                            public void onSuccess() {
                                if (getView() != null)
                                    getView().showNomenclature(nomenList);
                            }
                        }, new Realm.Transaction.OnError() {
                            @Override
                            public void onError(Throwable error) {
                                if (getView() == null)
                                    return;

                                getView().showToast("Delete" + BaseResponse.Type.FAILED
                                        + " (Realm Error : " + error.getMessage() + ")");
                            }
                        });
                    }
                    if (getView() != null)
                        getView().hideProgressBarFindNomenclature(true);
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<List<Nomenclature>>> call, Throwable t) {
                if (getView() == null)
                    return;

                if (!call.isCanceled())
                    offlineLoadNomenclature(keyword);

                //getView().showToast("Connection" + BaseResponse.Type.FAILED + ", looking for cache");
            }
        });
    }

    protected void offlineLoadNomenclature(String name) {
        if (TextUtils.isNullOrEmpty(name))
            return;

        final List<Nomenclature> nomenclatures = mAppDataManager.findNomenclaturesbyUser(name, "contain");
        final ArrayList<String> nomenList = new ArrayList<String>();
        for(int i=0; i<nomenclatures.size(); i++)
            nomenList.add(nomenclatures.get(i).getName());
        if (getView() != null) {
            getView().hideProgressBarFindNomenclature(true);
            getView().showNomenclature(nomenList);
        }

    }

    private boolean handleErrorResponse(Response response) {
        if (response.code() == OkHttpsURLConnection.HTTP_UNAUTHORIZED || response.code() == OkHttpsURLConnection.HTTP_INTERNAL_ERROR) {
            if (response.code() == OkHttpsURLConnection.HTTP_UNAUTHORIZED) {
                if (getView() != null)
                    getView().showDialog("Error", "Session expired");
                logOut();
            } else if (response.code() == OkHttpsURLConnection.HTTP_INTERNAL_ERROR) {
                if (getView() != null)
                    getView().showDialog("Error", "Terjadi kesalahan, data tidak ditemukan");
                else {
                    if (getView() != null)
                        getView().showDialog("Error", "Terjadi kesalahan, data tidak ditemukan");
                }
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
