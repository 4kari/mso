package id.sisi.si.mso.ui.inspection.detail;

import android.util.Log;

import com.crashlytics.android.Crashlytics;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import id.sisi.si.mso.data.AppDataManager;
import id.sisi.si.mso.data.api.MsoService;
import id.sisi.si.mso.data.api.ServiceGenerator;
import id.sisi.si.mso.data.api.model.BaseResponse;
import id.sisi.si.mso.data.model.AbnormalityParams;
import id.sisi.si.mso.data.model.Bearing;
import id.sisi.si.mso.data.model.BeltWeighter;
import id.sisi.si.mso.data.model.DetailInspection;
import id.sisi.si.mso.data.model.EquipmentInfo;
import id.sisi.si.mso.data.model.GetPrevInspection;
import id.sisi.si.mso.data.model.InspectionList;
import id.sisi.si.mso.data.model.Param;
import id.sisi.si.mso.data.model.Parent;
import id.sisi.si.mso.data.model.TechnicalModel;
import id.sisi.si.mso.data.model.Transformer;
import id.sisi.si.mso.data.model.UnsyncedInspection;
import id.sisi.si.mso.data.model.User;
import id.sisi.si.mso.data.model.picItem;
import id.sisi.si.mso.data.persistence.BasePersistence;
import id.sisi.si.mso.ui.base.BasePresenter;
import io.realm.Realm;
import io.realm.RealmList;
import io.realm.exceptions.RealmMigrationNeededException;
import okhttp3.internal.huc.OkHttpsURLConnection;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

/**
 * Created by durrrr on 04-Oct-17.
 * Email: cab.purnama@gmail.com
 */
public class InspectionDetailPresenter extends BasePresenter<Contract.View> implements Contract.Presenter {

    public static final String TRANSACTION_MODE_KEY = "transaction_mode";
    public static final int ONLINE_MODE = 0;
    public static final int OFFLINE_MODE = 1;
    private final AppDataManager mAppDataManager;
    private final Realm mRealm;
    private DetailInspection mDetailInspectionModel;
    private AbnormalityParams mAbnormalityParamsModel;
    private EquipmentInfo mEquipmentInfoModel;
    private TechnicalModel mTechnicalModel;
    private UnsyncedInspection mUnsyncedInspectionModel;
    private Long mInspectionNo;
    private String mEquipmentName;
    private int mTransactionMode = ONLINE_MODE;
    private GetPrevInspection mGetPrevInspection; //added by rama 07 Nov 2022

    public InspectionDetailPresenter(Long inspectionNo, String equipmentName, int transactionMode) {
        mAppDataManager = AppDataManager.getInstance();
        mRealm = Realm.getDefaultInstance();

        mInspectionNo = inspectionNo;
        mEquipmentName = equipmentName;
        mTransactionMode = transactionMode;
    }

    @Override
    public void attachView(Contract.View view) {
        super.attachView(view);
        if (getView() != null)
            getView().showProgress("Load data", "Harap tunggu");

        if (mTransactionMode == ONLINE_MODE) {
            updateDetailInspection();
            //updateAbnormalityParams();
            //updateEquipmentInfo();
        } else {
            getUnsyncedInspection();
        }

        /*getLocalDetailInspection();
        getLocalAbnormalityParams();*/
    }

    private void initTechnicalModel() {
        switch (mEquipmentInfoModel.getType().getId()) {
            case EquipmentInfo.TYPE_BEARING:
                mTechnicalModel = new Bearing();
                break;

            case EquipmentInfo.TYPE_BELT_WEIGHTER:
                mTechnicalModel = new BeltWeighter();
                break;

            case EquipmentInfo.TYPE_TRANSFORMER:
                mTechnicalModel = new Transformer();
                break;

            default:
                break;
        }
    }

    private void updateEquipmentInfo() {
        Call<BaseResponse<EquipmentInfo>> call = ServiceGenerator.getInstance()
                .getRetrofit().create(MsoService.class).getEquipmentInfo(mEquipmentName);
        call.enqueue(new Callback<BaseResponse<EquipmentInfo>>() {
            @Override
            public void onResponse(Call<BaseResponse<EquipmentInfo>> call, Response<BaseResponse<EquipmentInfo>> response) {
                if (!handleErrorResponse(response)) {
                    if (getView() != null)
                        getView().onFailedGetDataFromServer();
                    return;
                }

                if (response.isSuccessful()) {
                    mEquipmentInfoModel = response.body().getData();
                    if (mEquipmentInfoModel != null) {
                        if (mEquipmentInfoModel.getType() != null)
                            initTechnicalModel();
//                        Log.d( "onResponse: ",response.body().getData().toString());
                        if (mDetailInspectionModel != null & mAbnormalityParamsModel != null
                                & mEquipmentInfoModel != null) {
                            if (getView() != null) {
                                Log.d("mEquipmentName",mEquipmentName.toString());
                                getView().initView();
                                getView().populateFields(mDetailInspectionModel, mAbnormalityParamsModel, mTechnicalModel, mTransactionMode);
                            }
                        }
                    } else {
                        if (getView() != null)
                            getView().showDialog("Gagal", "Data equipment tidak terdaftar pada user ini");
                    }
                } else {
                    if (getView() != null)
                        getView().onFailedGetDataFromServer();
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<EquipmentInfo>> call, Throwable t) {
                if (getView() != null)
                    getView().onFailedGetDataFromServer();
            }
        });
    }

    private void getUnsyncedInspection() {
        try {
            mUnsyncedInspectionModel = mAppDataManager.getCopyOfUnsyncedInspection(mInspectionNo);
            mEquipmentInfoModel = mAppDataManager.getEquipmentInfo(mUnsyncedInspectionModel.getEquipmentName());
            mAbnormalityParamsModel = mAppDataManager.getCopyOfAbnormalityParams();

            mDetailInspectionModel = new DetailInspection();
            mDetailInspectionModel.setInspectionNo(mUnsyncedInspectionModel.getUnsyncedInspectionNo());
            mDetailInspectionModel.setDate(mUnsyncedInspectionModel.getDate());
            mDetailInspectionModel.setCondition(mUnsyncedInspectionModel.getCondition());
            mDetailInspectionModel.setDescription(mUnsyncedInspectionModel.getDescription());
            mDetailInspectionModel.setPhotopath(mUnsyncedInspectionModel.getLocalPhotoPath());
            mDetailInspectionModel.setActivity(mUnsyncedInspectionModel.getActivity());

            /*StringBuilder activities = new StringBuilder();
            for (int i = 0; i < mUnsyncedInspectionModel.getActivities().size(); i++) {
                if (i != mUnsyncedInspectionModel.getActivities().size() - 1) {
                    activities.append(mUnsyncedInspectionModel.getActivities().get(i).getName()).append("\n");
                } else {
                    activities.append(mUnsyncedInspectionModel.getActivities().get(i).getName());
                }
            }
            mDetailInspectionModel.setActivity(activities.toString());*/

            if (mUnsyncedInspectionModel.getEquipmentTypeId() != null) {
                initTechnicalModel();
                mDetailInspectionModel.setEquipmentTypeId(mUnsyncedInspectionModel.getEquipmentTypeId());
                mDetailInspectionModel.setEquipmentTypeName(mUnsyncedInspectionModel.getEquipmentTypeName());
                mDetailInspectionModel.setEquipmentSubTypeId(mUnsyncedInspectionModel.getEquipmentSubTypeId());

                switch (mDetailInspectionModel.getEquipmentTypeId()) {
                    case DetailInspection.TYPE_BEARING:
                        mDetailInspectionModel.setBearing(mUnsyncedInspectionModel.getBearing());
                        break;

                    case DetailInspection.TYPE_BELT_WEIGHTER:
                        mDetailInspectionModel.setBeltWeighter(mUnsyncedInspectionModel.getBeltWeighter());
                        break;

                    case DetailInspection.TYPE_TRANSFORMER:
                        mDetailInspectionModel.setTransformer(mUnsyncedInspectionModel.getTransformer());
                        break;
                }
            }

            if (mUnsyncedInspectionModel.getCondition().equals("0")) {
                mDetailInspectionModel.setAbnormalValues(mUnsyncedInspectionModel.getAbnormal());
            }

            if (mDetailInspectionModel != null & mAbnormalityParamsModel != null
                    & mEquipmentInfoModel != null) {
                if (getView() != null) {
                    getView().hideProgress();
                    getView().initView();
                    getView().populateFields(mDetailInspectionModel, mAbnormalityParamsModel, mTechnicalModel, mTransactionMode);
                }
            } else {
                if (getView() != null)
                    getView().showDialog("Error", "Terdapat kesalahan dalam pengambilan data inspeksi");
            }
        } catch (Exception e) {
            Crashlytics.logException(e.fillInStackTrace());
            Crashlytics.logException(e.fillInStackTrace());
        }
    }

    public void updateDetailInspection() {
        Call<BaseResponse<DetailInspection>> call = ServiceGenerator.getInstance()
                .getRetrofit().create(MsoService.class).getDetailInspection(mInspectionNo);
        call.enqueue(new Callback<BaseResponse<DetailInspection>>() {
            @Override
            public void onResponse(Call<BaseResponse<DetailInspection>> call, Response<BaseResponse<DetailInspection>> response) {
                if (!handleErrorResponse(response)) {
                    if (getView() != null)
                        getView().onFailedGetDataFromServer();
                    return;
                }

                if (response.isSuccessful()) {
                    final DetailInspection detailInspectionResponse = response.body().getData();
                    if (detailInspectionResponse != null) {
                        mRealm.executeTransactionAsync(new Realm.Transaction() {
                            @Override
                            public void execute(Realm realm) {
                                detailInspectionResponse.setInspectionNo(mInspectionNo);
                                if (detailInspectionResponse.getAbnormalValues() != null)
                                    detailInspectionResponse.getAbnormalValues().setSynced(true);

                                BasePersistence.addDetailInspection(realm, detailInspectionResponse);
                            }
                        }, new Realm.Transaction.OnSuccess() {
                            @Override
                            public void onSuccess() {
                                final User user = mRealm.where(Parent.class).findFirst().getLoggedInUser();
                                mDetailInspectionModel = user.getDetailInspections().where().equalTo("inspectionNo", detailInspectionResponse.getInspectionNo()).findFirst();
                                Log.d("getDetailInspection",mDetailInspectionModel.getEquipmentSubTypeName());
                                Log.d("getDetailInspection2",mDetailInspectionModel.toString());
                                Log.d("getDetailInspection3",mDetailInspectionModel.getBearing().toString());
                                updateAbnormalityParams();
                                /*if (mDetailInspectionModel != null & mAbnormalityParamsModel != null
                                        & mEquipmentInfoModel != null) {
                                    getView().initView();
                                    getView().populateFields(mDetailInspectionModel, mAbnormalityParamsModel, mTechnicalModel, mTransactionMode);*/
                            }
                        });
                    }
                } else {
                    if (getView() != null) {
                        getView().hideProgress();
                        getView().showDialog("Error", "Terdapat kesalahan dalam pengambilan data inspeksi");
                    }
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<DetailInspection>> call, Throwable t) {
                if (getView() != null) {
                    Timber.d("error :" + t.toString());
                    getView().hideProgress();
                    getView().showDialog("Error", "Terdapat kesalahan dalam pengambilan data inspeksi dari server");
                }
            }
        });
    }

    private void updateAbnormalityParams() {
        ServiceGenerator.getInstance().getRetrofit().create(MsoService.class).
                getParamsToAddAbnormality(null).enqueue(new Callback<BaseResponse<AbnormalityParams>>() {
            @Override
            public void onResponse(Call<BaseResponse<AbnormalityParams>> call, final Response<BaseResponse<AbnormalityParams>> response) {
                if (!handleErrorResponse(response)) {
                    if (getView() != null)
                        getView().onFailedGetDataFromServer();
                    return;
                }

                if (response.isSuccessful() & response.body().getData() != null) {
                    mAbnormalityParamsModel = response.body().getData();

                    mAppDataManager.addAbnormalityParams(mAbnormalityParamsModel, new Realm.Transaction.OnSuccess() {
                        @Override
                        public void onSuccess() {
                            /*if (mDetailInspectionModel != null & mAbnormalityParamsModel != null
                                    & mEquipmentInfoModel != null) {
                                if (getView() != null) {
                                    getView().initView();
                                    getView().populateFields(mDetailInspectionModel, mAbnormalityParamsModel, mTechnicalModel, mTransactionMode);
                                }
                            }*/
                            updateEquipmentInfo();
                        }
                    }, new Realm.Transaction.OnError() {
                        @Override
                        public void onError(Throwable error) {

                        }
                    });
                    if (getView() != null) {
                        getView().hideProgress();
                    }
                } else {
                    if (getView() != null) {
                        getView().hideProgress();
                        getView().showDialog("Error", "Terdapat kesalahan dalam pengambilan data abnormal");
                    }
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<AbnormalityParams>> call, Throwable t) {
                if (getView() != null) {
                    getView().hideProgress();
                    getView().showDialog("Error", "Terdapat kesalahan dalam pengambilan data abnormal dari server");
                }
            }
        });
    }

    @Override
    public void attemptDeleteInspection() {
        if (mTransactionMode == OFFLINE_MODE) {
            attemptDeleteLocalInspection();
            return;
        }

        if (getView() != null)
            getView().showProgress("Menghapus inspeksi", "Harap tunggu");

        Call<BaseResponse> call = ServiceGenerator.getInstance()
                .getRetrofit().create(MsoService.class).deleteInspection(mDetailInspectionModel.getInspectionNo());
        call.enqueue(new Callback<BaseResponse>() {
            @Override
            public void onResponse(Call<BaseResponse> call, Response<BaseResponse> response) {
                if (!handleErrorResponse(response)) {
                    if (getView() != null)
                        getView().onInspectionFailedToDeleted();
                    return;
                }

                if (response.isSuccessful()) {
                    final BaseResponse defaultResponse = response.body();
                    if (defaultResponse.isSuccess()) {
                        // Delete row from realm will update into server too
                        Realm realm = Realm.getDefaultInstance();
                        try {
                            realm.executeTransaction(new Realm.Transaction() {
                                @Override
                                public void execute(Realm realm) {
                                    DetailInspection inspectionDetail = realm.where(DetailInspection.class).equalTo("inspectionNo", mDetailInspectionModel.getInspectionNo())
                                            .findFirst();
                                    InspectionList inspectionList = realm.where(InspectionList.class).equalTo("inspectionNo", mDetailInspectionModel.getInspectionNo()).findFirst();
                                    User user = realm.where(Parent.class).findFirst().getLoggedInUser();
                                    user.getDetailInspections().remove(inspectionDetail);
                                    user.getInspectionList().remove(inspectionList);
                                }
                            });
                        } catch (IllegalArgumentException | RealmMigrationNeededException e) {

                        } finally {
                            realm.close();
                        }

                        if (getView() != null)
                            getView().onInspectionDeleted();

                        return;
                    } else {
                        if (getView() != null)
                            getView().onInspectionFailedToDeleted();
                    }
                }
            }

            @Override
            public void onFailure(Call<BaseResponse> call, Throwable t) {
                if (getView() != null)
                    getView().onInspectionFailedToDeleted();
            }
        });
    }

    @Override
    public void attemptUpdateInspection() {
        if (mTransactionMode == OFFLINE_MODE) {
            attemptUpdateLocalInspection();
            return;
        }

        Map<String, String> params = new HashMap<>();
        RealmList<Param> mEquipmentInfoSub = mEquipmentInfoModel.getParams().get(0).getParams();

        switch (mDetailInspectionModel.getEquipmentTypeId()) {
            case DetailInspection.TYPE_BEARING:
                Bearing bearingModel = (Bearing) mTechnicalModel;
                if(mDetailInspectionModel.getEquipmentSubTypeId().equalsIgnoreCase(EquipmentInfo.TYPE_MOTOR_EXIST)) {
                    params.put(mEquipmentInfoSub.get(0).getId(), String.valueOf(bearingModel.getRegrease()));
                    params.put(mEquipmentInfoSub.get(1).getId(), String.valueOf(bearingModel.getKelengkapanMotor()));
                    params.put(mEquipmentInfoSub.get(2).getId(), String.valueOf(bearingModel.getKeterangan()));
                    params.put(mEquipmentInfoSub.get(3).getId(), String.valueOf(bearingModel.getVibrasiDs()));
                    params.put(mEquipmentInfoSub.get(4).getId(), String.valueOf(bearingModel.getGeDs()));
                    params.put(mEquipmentInfoSub.get(5).getId(), String.valueOf(bearingModel.getTemperaturDs()));
                    params.put(mEquipmentInfoSub.get(6).getId(), String.valueOf(bearingModel.getVibrasiNds()));
                    params.put(mEquipmentInfoSub.get(7).getId(), String.valueOf(bearingModel.getGeNds()));
                    params.put(mEquipmentInfoSub.get(8).getId(), String.valueOf(bearingModel.getTemperaturNds()));
                    params.put(mEquipmentInfoSub.get(9).getId(), String.valueOf(bearingModel.getGedsHor_before()));
                    params.put(mEquipmentInfoSub.get(10).getId(), String.valueOf(bearingModel.getGeNdsHor_before()));
                    params.put(mEquipmentInfoSub.get(11).getId(), String.valueOf(bearingModel.getGedsAxial_before()));
                    params.put(mEquipmentInfoSub.get(12).getId(), String.valueOf(bearingModel.getVibrasiDsHor_before()));
                    params.put(mEquipmentInfoSub.get(13).getId(), String.valueOf(bearingModel.getVibrasiNdsHor_before()));
                    params.put(mEquipmentInfoSub.get(14).getId(), String.valueOf(bearingModel.getVibrasiDsAxial_before()));
                    params.put(mEquipmentInfoSub.get(15).getId(), String.valueOf(bearingModel.getVibrasiNdsAxial_before()));
                    params.put(mEquipmentInfoSub.get(16).getId(), String.valueOf(bearingModel.getGeNdsAxial_before()));
                    params.put(mEquipmentInfoSub.get(17).getId(), String.valueOf(bearingModel.getVibrasiDsVert_after()));
                    params.put(mEquipmentInfoSub.get(18).getId(), String.valueOf(bearingModel.getGeDsVert_after()));
                    params.put(mEquipmentInfoSub.get(19).getId(), String.valueOf(bearingModel.getVibrasiNdsVert_after()));
                    params.put(mEquipmentInfoSub.get(20).getId(), String.valueOf(bearingModel.getGeNdsVert_after()));
                    params.put(mEquipmentInfoSub.get(21).getId(), String.valueOf(bearingModel.getGeDsHor_after()));
                    params.put(mEquipmentInfoSub.get(22).getId(), String.valueOf(bearingModel.getGeNdsHor_after()));
                    params.put(mEquipmentInfoSub.get(23).getId(), String.valueOf(bearingModel.getGeDsAxial_after()));
                    params.put(mEquipmentInfoSub.get(24).getId(), String.valueOf(bearingModel.getVibrasiDsHor_after()));
                    params.put(mEquipmentInfoSub.get(25).getId(), String.valueOf(bearingModel.getVibrasiNdsHor_after()));
                    params.put(mEquipmentInfoSub.get(26).getId(), String.valueOf(bearingModel.getVibrasiDsAxial_after()));
                    params.put(mEquipmentInfoSub.get(27).getId(), String.valueOf(bearingModel.getVibrasiNdsAxial_after()));
                    params.put(mEquipmentInfoSub.get(28).getId(), String.valueOf(bearingModel.getGeNdsAxial_after()));
                    params.put(mEquipmentInfoSub.get(29).getId(), String.valueOf(bearingModel.getRegrease_nde()));
                }
                else if(mDetailInspectionModel.getEquipmentSubTypeId().equalsIgnoreCase(EquipmentInfo.TYPE_MCC_EXIST)){
                    RealmList<Param> mEquipmentInfoSubMcc = mEquipmentInfoModel.getParams().get(1).getParams();
                    params.put(mEquipmentInfoSubMcc.get(0).getId(), String.valueOf(bearingModel.getKw()));
                    params.put(mEquipmentInfoSubMcc.get(1).getId(), String.valueOf(bearingModel.getFla()));
                    params.put(mEquipmentInfoSubMcc.get(2).getId(), String.valueOf(bearingModel.getAmpereSetting()));
                    params.put(mEquipmentInfoSubMcc.get(3).getId(), String.valueOf(bearingModel.getAmpereRact()));
                    params.put(mEquipmentInfoSubMcc.get(4).getId(), String.valueOf(bearingModel.getAmpereSact()));
                    params.put(mEquipmentInfoSubMcc.get(5).getId(), String.valueOf(bearingModel.getAmpereTact()));
                    params.put(mEquipmentInfoSubMcc.get(6).getId(), String.valueOf(bearingModel.getTemperatureR()));
                    params.put(mEquipmentInfoSubMcc.get(7).getId(), String.valueOf(bearingModel.getTemperatureS()));
                    params.put(mEquipmentInfoSubMcc.get(8).getId(), String.valueOf(bearingModel.getTemperatureT()));
                    params.put(mEquipmentInfoSubMcc.get(9).getId(), bearingModel.getKeterangan());
                }
                else if(mDetailInspectionModel.getEquipmentSubTypeId().equalsIgnoreCase(EquipmentInfo.TYPE_MEKANIKAL)){
                    RealmList<Param> mEquipmentInfoSubMekanik = mEquipmentInfoModel.getParams().get(4).getParams();
                    params.put(mEquipmentInfoSubMekanik.get(0).getId(), String.valueOf(bearingModel.getTemp_ds_imp()));
                    params.put(mEquipmentInfoSubMekanik.get(1).getId(), String.valueOf(bearingModel.getTemp_nds_imp()));
                    params.put(mEquipmentInfoSubMekanik.get(2).getId(), String.valueOf(bearingModel.getKb_fan()));
                    params.put(mEquipmentInfoSubMekanik.get(3).getId(), String.valueOf(bearingModel.getVib_fan()));
                    params.put(mEquipmentInfoSubMekanik.get(4).getId(), String.valueOf(bearingModel.getKV_fan()));
                    params.put(mEquipmentInfoSubMekanik.get(5).getId(), String.valueOf(bearingModel.getPulley()));
                    params.put(mEquipmentInfoSubMekanik.get(6).getId(), String.valueOf(bearingModel.getSafetyG()));
                    params.put(mEquipmentInfoSubMekanik.get(7).getId(), String.valueOf(bearingModel.getHouskeeping()));
                    params.put(mEquipmentInfoSubMekanik.get(8).getId(), String.valueOf(bearingModel.getUnsafeC()));
                    params.put(mEquipmentInfoSubMekanik.get(9).getId(), bearingModel.getKeterangan());
                }
                else {
                    RealmList<Param> mEquipmentInfoSubMMV = mEquipmentInfoModel.getParams().get(2).getParams();
                    params.put(mEquipmentInfoSubMMV.get(0).getId(), String.valueOf(bearingModel.getA1()));
                    params.put(mEquipmentInfoSubMMV.get(1).getId(), String.valueOf(bearingModel.getA2()));
                    params.put(mEquipmentInfoSubMMV.get(2).getId(), String.valueOf(bearingModel.getB1()));
                    params.put(mEquipmentInfoSubMMV.get(3).getId(), String.valueOf(bearingModel.getB2()));
                    params.put(mEquipmentInfoSubMMV.get(4).getId(), String.valueOf(bearingModel.getB3()));
                    params.put(mEquipmentInfoSubMMV.get(5).getId(), String.valueOf(bearingModel.getC1()));
                    params.put(mEquipmentInfoSubMMV.get(6).getId(), String.valueOf(bearingModel.getC2()));
                    params.put(mEquipmentInfoSubMMV.get(7).getId(), String.valueOf(bearingModel.getC3()));
                    params.put(mEquipmentInfoSubMMV.get(8).getId(), String.valueOf(bearingModel.getD1()));
                    params.put(mEquipmentInfoSubMMV.get(9).getId(), String.valueOf(bearingModel.getD2()));
                    params.put(mEquipmentInfoSubMMV.get(10).getId(), String.valueOf(bearingModel.getD3()));
                    params.put(mEquipmentInfoSubMMV.get(11).getId(), String.valueOf(bearingModel.getE1()));
                    params.put(mEquipmentInfoSubMMV.get(12).getId(), String.valueOf(bearingModel.getE2()));
                    params.put(mEquipmentInfoSubMMV.get(13).getId(), String.valueOf(bearingModel.getE3()));
                    params.put(mEquipmentInfoSubMMV.get(14).getId(), String.valueOf(bearingModel.getF1()));
                    params.put(mEquipmentInfoSubMMV.get(15).getId(), String.valueOf(bearingModel.getF2()));
                    params.put(mEquipmentInfoSubMMV.get(16).getId(), String.valueOf(bearingModel.getF3()));
                    params.put(mEquipmentInfoSubMMV.get(17).getId(), String.valueOf(bearingModel.getG1()));
                    params.put(mEquipmentInfoSubMMV.get(18).getId(), String.valueOf(bearingModel.getG2()));
                    params.put(mEquipmentInfoSubMMV.get(19).getId(), String.valueOf(bearingModel.getH1()));
                    params.put(mEquipmentInfoSubMMV.get(20).getId(), String.valueOf(bearingModel.getH2()));
                    params.put(mEquipmentInfoSubMMV.get(21).getId(), String.valueOf(bearingModel.getI1()));
                    params.put(mEquipmentInfoSubMMV.get(22).getId(), String.valueOf(bearingModel.getI2()));
                    params.put(mEquipmentInfoSubMMV.get(23).getId(), String.valueOf(bearingModel.getJ1()));
                    params.put(mEquipmentInfoSubMMV.get(24).getId(), String.valueOf(bearingModel.getJ2()));
                    params.put(mEquipmentInfoSubMMV.get(25).getId(), String.valueOf(bearingModel.getK1()));
                    params.put(mEquipmentInfoSubMMV.get(26).getId(), String.valueOf(bearingModel.getK2()));
                    params.put(mEquipmentInfoSubMMV.get(27).getId(), String.valueOf(bearingModel.getL1()));
                    params.put(mEquipmentInfoSubMMV.get(28).getId(), String.valueOf(bearingModel.getL2()));
                    params.put(mEquipmentInfoSubMMV.get(29).getId(), String.valueOf(bearingModel.getM1()));
                    params.put(mEquipmentInfoSubMMV.get(30).getId(), String.valueOf(bearingModel.getM2()));
                    params.put(mEquipmentInfoSubMMV.get(31).getId(), String.valueOf(bearingModel.getJmlPenggantian()));
                    params.put(mEquipmentInfoSubMMV.get(32).getId(), String.valueOf(bearingModel.getFilter()));
                    params.put(mEquipmentInfoSubMMV.get(33).getId(), String.valueOf(bearingModel.getRunHours()));
                    params.put(mEquipmentInfoSubMMV.get(34).getId(), String.valueOf(bearingModel.getRotor()));
                    params.put(mEquipmentInfoSubMMV.get(35).getId(), String.valueOf(bearingModel.getStator()));
                    params.put(mEquipmentInfoSubMMV.get(36).getId(), bearingModel.getMembers());
                    params.put(mEquipmentInfoSubMMV.get(37).getId(), bearingModel.getKeteranganMMV());

                    params.put(mEquipmentInfoSubMMV.get(38).getId(), String.valueOf(bearingModel.getA1L()));
                    params.put(mEquipmentInfoSubMMV.get(39).getId(), String.valueOf(bearingModel.getA2L()));
                    params.put(mEquipmentInfoSubMMV.get(40).getId(), String.valueOf(bearingModel.getB1L()));
                    params.put(mEquipmentInfoSubMMV.get(41).getId(), String.valueOf(bearingModel.getB2L()));
                    params.put(mEquipmentInfoSubMMV.get(42).getId(), String.valueOf(bearingModel.getB3L()));
                    params.put(mEquipmentInfoSubMMV.get(43).getId(), String.valueOf(bearingModel.getC1L()));
                    params.put(mEquipmentInfoSubMMV.get(44).getId(), String.valueOf(bearingModel.getC2L()));
                    params.put(mEquipmentInfoSubMMV.get(45).getId(), String.valueOf(bearingModel.getC3L()));
                    params.put(mEquipmentInfoSubMMV.get(46).getId(), String.valueOf(bearingModel.getD1L()));
                    params.put(mEquipmentInfoSubMMV.get(47).getId(), String.valueOf(bearingModel.getD2L()));
                    params.put(mEquipmentInfoSubMMV.get(48).getId(), String.valueOf(bearingModel.getD3L()));
                    params.put(mEquipmentInfoSubMMV.get(49).getId(), String.valueOf(bearingModel.getE1L()));
                    params.put(mEquipmentInfoSubMMV.get(50).getId(), String.valueOf(bearingModel.getE2L()));
                    params.put(mEquipmentInfoSubMMV.get(51).getId(), String.valueOf(bearingModel.getE3L()));
                    params.put(mEquipmentInfoSubMMV.get(52).getId(), String.valueOf(bearingModel.getF1L()));
                    params.put(mEquipmentInfoSubMMV.get(53).getId(), String.valueOf(bearingModel.getF2L()));
                    params.put(mEquipmentInfoSubMMV.get(54).getId(), String.valueOf(bearingModel.getF3L()));
                    params.put(mEquipmentInfoSubMMV.get(55).getId(), String.valueOf(bearingModel.getG1L()));
                    params.put(mEquipmentInfoSubMMV.get(56).getId(), String.valueOf(bearingModel.getG2L()));
                    params.put(mEquipmentInfoSubMMV.get(57).getId(), String.valueOf(bearingModel.getH1L()));
                    params.put(mEquipmentInfoSubMMV.get(58).getId(), String.valueOf(bearingModel.getH2L()));
                    params.put(mEquipmentInfoSubMMV.get(59).getId(), String.valueOf(bearingModel.getI1l()));
                    params.put(mEquipmentInfoSubMMV.get(60).getId(), String.valueOf(bearingModel.getI2l()));
                    params.put(mEquipmentInfoSubMMV.get(61).getId(), String.valueOf(bearingModel.getJ1l()));
                    params.put(mEquipmentInfoSubMMV.get(62).getId(), String.valueOf(bearingModel.getJ2l()));
                    params.put(mEquipmentInfoSubMMV.get(63).getId(), String.valueOf(bearingModel.getK1l()));
                    params.put(mEquipmentInfoSubMMV.get(64).getId(), String.valueOf(bearingModel.getK2l()));
                    params.put(mEquipmentInfoSubMMV.get(65).getId(), String.valueOf(bearingModel.getL1l()));
                    params.put(mEquipmentInfoSubMMV.get(66).getId(), String.valueOf(bearingModel.getL2l()));
                    params.put(mEquipmentInfoSubMMV.get(67).getId(), String.valueOf(bearingModel.getM1l()));
                    params.put(mEquipmentInfoSubMMV.get(68).getId(), String.valueOf(bearingModel.getM2l()));
                    params.put(mEquipmentInfoSubMMV.get(69).getId(), String.valueOf(bearingModel.getJmlPenggantianL()));
                    params.put(mEquipmentInfoSubMMV.get(70).getId(), String.valueOf(bearingModel.getFilterL()));
                    params.put(mEquipmentInfoSubMMV.get(71).getId(), String.valueOf(bearingModel.getRunHoursL()));
                    params.put(mEquipmentInfoSubMMV.get(72).getId(), String.valueOf(bearingModel.getRotorL()));
                    params.put(mEquipmentInfoSubMMV.get(73).getId(), String.valueOf(bearingModel.getStatorL()));
                    params.put(mEquipmentInfoSubMMV.get(74).getId(), bearingModel.getMembersL());
                    params.put(mEquipmentInfoSubMMV.get(75).getId(), bearingModel.getKeteranganMMVL());

                    params.put(mEquipmentInfoSubMMV.get(76).getId(), String.valueOf(bearingModel.getA1M()));
                    params.put(mEquipmentInfoSubMMV.get(77).getId(), String.valueOf(bearingModel.getA2M()));
                    params.put(mEquipmentInfoSubMMV.get(78).getId(), String.valueOf(bearingModel.getB1M()));
                    params.put(mEquipmentInfoSubMMV.get(79).getId(), String.valueOf(bearingModel.getB2M()));
                    params.put(mEquipmentInfoSubMMV.get(80).getId(), String.valueOf(bearingModel.getB3M()));
                    params.put(mEquipmentInfoSubMMV.get(81).getId(), String.valueOf(bearingModel.getC1M()));
                    params.put(mEquipmentInfoSubMMV.get(82).getId(), String.valueOf(bearingModel.getC2M()));
                    params.put(mEquipmentInfoSubMMV.get(83).getId(), String.valueOf(bearingModel.getC3M()));
                    params.put(mEquipmentInfoSubMMV.get(84).getId(), String.valueOf(bearingModel.getD1M()));
                    params.put(mEquipmentInfoSubMMV.get(85).getId(), String.valueOf(bearingModel.getD2M()));
                    params.put(mEquipmentInfoSubMMV.get(86).getId(), String.valueOf(bearingModel.getD3M()));
                    params.put(mEquipmentInfoSubMMV.get(87).getId(), String.valueOf(bearingModel.getE1M()));
                    params.put(mEquipmentInfoSubMMV.get(88).getId(), String.valueOf(bearingModel.getE2M()));
                    params.put(mEquipmentInfoSubMMV.get(89).getId(), String.valueOf(bearingModel.getE3M()));
                    params.put(mEquipmentInfoSubMMV.get(90).getId(), String.valueOf(bearingModel.getF1M()));
                    params.put(mEquipmentInfoSubMMV.get(91).getId(), String.valueOf(bearingModel.getF2M()));
                    params.put(mEquipmentInfoSubMMV.get(92).getId(), String.valueOf(bearingModel.getF3M()));
                    params.put(mEquipmentInfoSubMMV.get(93).getId(), String.valueOf(bearingModel.getG1M()));
                    params.put(mEquipmentInfoSubMMV.get(94).getId(), String.valueOf(bearingModel.getG2M()));
                    params.put(mEquipmentInfoSubMMV.get(95).getId(), String.valueOf(bearingModel.getH1M()));
                    params.put(mEquipmentInfoSubMMV.get(96).getId(), String.valueOf(bearingModel.getH2M()));
                    params.put(mEquipmentInfoSubMMV.get(97).getId(), String.valueOf(bearingModel.getI1m()));
                    params.put(mEquipmentInfoSubMMV.get(98).getId(), String.valueOf(bearingModel.getI2m()));
                    params.put(mEquipmentInfoSubMMV.get(99).getId(), String.valueOf(bearingModel.getJ2m()));
                    params.put(mEquipmentInfoSubMMV.get(100).getId(), String.valueOf(bearingModel.getJ2m()));
                    params.put(mEquipmentInfoSubMMV.get(101).getId(), String.valueOf(bearingModel.getK1m()));
                    params.put(mEquipmentInfoSubMMV.get(102).getId(), String.valueOf(bearingModel.getK2m()));
                    params.put(mEquipmentInfoSubMMV.get(103).getId(), String.valueOf(bearingModel.getL1m()));
                    params.put(mEquipmentInfoSubMMV.get(104).getId(), String.valueOf(bearingModel.getL2m()));
                    params.put(mEquipmentInfoSubMMV.get(105).getId(), String.valueOf(bearingModel.getM1m()));
                    params.put(mEquipmentInfoSubMMV.get(106).getId(), String.valueOf(bearingModel.getM2m()));
                    params.put(mEquipmentInfoSubMMV.get(107).getId(), String.valueOf(bearingModel.getJmlPenggantianM()));
                    params.put(mEquipmentInfoSubMMV.get(108).getId(), String.valueOf(bearingModel.getFilterM()));
                    params.put(mEquipmentInfoSubMMV.get(109).getId(), String.valueOf(bearingModel.getRunHoursM()));
                    params.put(mEquipmentInfoSubMMV.get(110).getId(), String.valueOf(bearingModel.getRotorM()));
                    params.put(mEquipmentInfoSubMMV.get(111).getId(), String.valueOf(bearingModel.getStatorM()));
                    params.put(mEquipmentInfoSubMMV.get(112).getId(), bearingModel.getMembersM());
                    params.put(mEquipmentInfoSubMMV.get(113).getId(), bearingModel.getKeteranganMMVM());
                }
                break;
            case DetailInspection.TYPE_BELT_WEIGHTER:
                BeltWeighter beltWeighterModel = (BeltWeighter) mTechnicalModel;
                params.put(mEquipmentInfoSub.get(0).getId(), String.valueOf(beltWeighterModel.getAktivitas()));
                params.put(mEquipmentInfoSub.get(1).getId(), String.valueOf(beltWeighterModel.getQr()));
                params.put(mEquipmentInfoSub.get(2).getId(), String.valueOf(beltWeighterModel.getJumlahCounter()));
                params.put(mEquipmentInfoSub.get(3).getId(), String.valueOf(beltWeighterModel.getSpanCorrection()));
                break;
            case DetailInspection.TYPE_TRANSFORMER:
                Transformer transformerModel = (Transformer) mTechnicalModel;
                params.put(mEquipmentInfoSub.get(0).getId(), String.valueOf(transformerModel.getTemperaturTrafo()));
                params.put(mEquipmentInfoSub.get(1).getId(), String.valueOf(transformerModel.getTemperaturKoneksi()));
                params.put(mEquipmentInfoSub.get(2).getId(), String.valueOf(transformerModel.getAmperePremier_r()));
                params.put(mEquipmentInfoSub.get(3).getId(), String.valueOf(transformerModel.getAmperePremier_s()));
                params.put(mEquipmentInfoSub.get(4).getId(), String.valueOf(transformerModel.getAmperePremier_t()));
                params.put(mEquipmentInfoSub.get(5).getId(), String.valueOf(transformerModel.getTempWinding()));
                params.put(mEquipmentInfoSub.get(6).getId(), String.valueOf(transformerModel.getOilTemp()));
                params.put(mEquipmentInfoSub.get(7).getId(), String.valueOf(transformerModel.getOilLevel()));
                params.put(mEquipmentInfoSub.get(8).getId(), String.valueOf(transformerModel.getSilicaGell()));
                params.put(mEquipmentInfoSub.get(9).getId(), transformerModel.getNegativeList());
                break;
        }

        Call<BaseResponse> call = ServiceGenerator.getInstance()
                .getRetrofit().create(MsoService.class).updateInspection(mInspectionNo,
                        mDetailInspectionModel.getDescription(),
                        params);

        call.enqueue(new Callback<BaseResponse>() {
            @Override
            public void onResponse(Call<BaseResponse> call, Response<BaseResponse> response) {
                if (!handleErrorResponse(response)) {
                    if (getView() != null)
                        getView().onInspectionFailedToUpdate();
                    return;
                }

                if (response.isSuccessful()) {
                    if (getView() != null)
                        getView().onInspectionUpdated();
                } else {
                    if (getView() != null)
                        getView().onInspectionFailedToUpdate();
                }
            }

            @Override
            public void onFailure(Call<BaseResponse> call, Throwable t) {
                Timber.d("error message : " + t.toString());
                if (getView() != null)
                    getView().onInspectionFailedToUpdate();
            }
        });
    }

    @Override
    public void getAutocompleteMember(String query, final String tag) {
        Call<BaseResponse<List<picItem>>> call = ServiceGenerator.getInstance().getRetrofit().create(MsoService.class)
                .getAutoCompleteDataPIC("PIC", query);
        call.enqueue(new Callback<BaseResponse<List<picItem>>>() {
            @Override
            public void onResponse(Call<BaseResponse<List<picItem>>> call, final Response<BaseResponse<List<picItem>>> response) {
                if (response.isSuccessful()) {
                    List<picItem> picItem = response.body().getData();

                    if (getView() != null) {
                        getView().onSaveMember(picItem, tag);
                    }
                }
                if (response.code() == OkHttpsURLConnection.HTTP_UNAUTHORIZED || response.code() == OkHttpsURLConnection.HTTP_INTERNAL_ERROR) {
                    if (response.code() == OkHttpsURLConnection.HTTP_UNAUTHORIZED) {
                        if (getView() != null)
                            getView().showDialog("Error", "Server error");
                    }
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<List<picItem>>> call, Throwable t) {
                if (getView() != null) {
                    getView().hideProgress();
                }
            }
        });
    }

    private void attemptUpdateLocalInspection() {
        mRealm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                UnsyncedInspection unsyncedInspection = mAppDataManager.getUnsyncedInspection(mInspectionNo);
                switch (mDetailInspectionModel.getEquipmentTypeId()) {
                    case DetailInspection.TYPE_BEARING:
                        Bearing bearingModel = (Bearing) mTechnicalModel;
                        if(mDetailInspectionModel.getEquipmentSubTypeId().equalsIgnoreCase(EquipmentInfo.TYPE_MOTOR_EXIST)) {
                            unsyncedInspection.getBearing().setRegrease(bearingModel.getRegrease());
                            unsyncedInspection.getBearing().setKelengkapanMotor(bearingModel.getKelengkapanMotor());
                            unsyncedInspection.getBearing().setKeterangan(bearingModel.getKeterangan());
                            unsyncedInspection.getBearing().setGeDs(bearingModel.getGeDs());
                            unsyncedInspection.getBearing().setGedsHor_before(bearingModel.getGedsHor_before());
                            unsyncedInspection.getBearing().setGedsAxial_before(bearingModel.getGedsAxial_before());
                            unsyncedInspection.getBearing().setTemperaturDs(bearingModel.getTemperaturDs());
                            unsyncedInspection.getBearing().setTemperaturNds(bearingModel.getTemperaturNds());
                            unsyncedInspection.getBearing().setVibrasiDs(bearingModel.getVibrasiDs());
                            unsyncedInspection.getBearing().setVibrasiDsAxial_before(bearingModel.getVibrasiDsAxial_before());
                            unsyncedInspection.getBearing().setVibrasiDsHor_before(bearingModel.getVibrasiDsHor_before());
                            unsyncedInspection.getBearing().setGeNds(bearingModel.getGeNds());
                            unsyncedInspection.getBearing().setGeNdsHor_before(bearingModel.getGeNdsHor_before());
                            unsyncedInspection.getBearing().setGeNdsAxial_before(bearingModel.getGeNdsAxial_before());
                            unsyncedInspection.getBearing().setVibrasiNds(bearingModel.getVibrasiNds());
                            unsyncedInspection.getBearing().setVibrasiNdsHor_before(bearingModel.getVibrasiNdsHor_before());
                            unsyncedInspection.getBearing().setVibrasiNdsAxial_before(bearingModel.getVibrasiNdsAxial_before());
                            //after
                            unsyncedInspection.getBearing().setGeDsVert_after(bearingModel.getGeDsVert_after());
                            unsyncedInspection.getBearing().setGeDsHor_after(bearingModel.getGeDsHor_after());
                            unsyncedInspection.getBearing().setGeDsAxial_after(bearingModel.getGeDsAxial_after());
                            unsyncedInspection.getBearing().setVibrasiDsVert_after(bearingModel.getVibrasiDsVert_after());
                            unsyncedInspection.getBearing().setVibrasiDsAxial_after(bearingModel.getVibrasiDsAxial_after());
                            unsyncedInspection.getBearing().setVibrasiDsHor_after(bearingModel.getVibrasiDsHor_after());
                            unsyncedInspection.getBearing().setGeNdsVert_after(bearingModel.getGeNdsVert_after());
                            unsyncedInspection.getBearing().setGeNdsHor_after(bearingModel.getGeNdsHor_after());
                            unsyncedInspection.getBearing().setGeNdsAxial_after(bearingModel.getGeNdsAxial_after());
                            unsyncedInspection.getBearing().setVibrasiNdsVert_after(bearingModel.getVibrasiNdsVert_after());
                            unsyncedInspection.getBearing().setVibrasiNdsHor_after(bearingModel.getVibrasiNdsHor_after());
                            unsyncedInspection.getBearing().setVibrasiNdsAxial_after(bearingModel.getVibrasiNdsAxial_after());
                            unsyncedInspection.getBearing().setRegrease_nde(bearingModel.getRegrease_nde());
                        }
                        else if(mDetailInspectionModel.getEquipmentSubTypeId().equalsIgnoreCase(EquipmentInfo.TYPE_MCC_EXIST))
                        {
                            unsyncedInspection.getBearing().setFla(bearingModel.getFla());
                            unsyncedInspection.getBearing().setKw(bearingModel.getKw());
                            unsyncedInspection.getBearing().setAmpereSetting(bearingModel.getAmpereSetting());
                            unsyncedInspection.getBearing().setAmpereRact(bearingModel.getAmpereRact());
                            unsyncedInspection.getBearing().setAmpereSact(bearingModel.getAmpereSact());
                            unsyncedInspection.getBearing().setAmpereTact(bearingModel.getAmpereTact());
                            unsyncedInspection.getBearing().setTemperatureR(bearingModel.getTemperatureR());
                            unsyncedInspection.getBearing().setTemperatureS(bearingModel.getTemperatureS());
                            unsyncedInspection.getBearing().setTemperatureT(bearingModel.getTemperatureT());
                            unsyncedInspection.getBearing().setKeterangan(bearingModel.getKeterangan());
                        }
                        else if(mDetailInspectionModel.getEquipmentSubTypeId().equalsIgnoreCase(EquipmentInfo.TYPE_MEKANIKAL)){
                            unsyncedInspection.getBearing().setTemp_ds_imp(bearingModel.getTemp_ds_imp());
                            unsyncedInspection.getBearing().setTemp_nds_imp(bearingModel.getTemp_nds_imp());
                            unsyncedInspection.getBearing().setKb_fan(bearingModel.getKb_fan());
                            unsyncedInspection.getBearing().setVib_fan(bearingModel.getVib_fan());
                            unsyncedInspection.getBearing().setKV_fan(bearingModel.getKV_fan());
                            unsyncedInspection.getBearing().setPulley(bearingModel.getPulley());
                            unsyncedInspection.getBearing().setSafetyG(bearingModel.getSafetyG());
                            unsyncedInspection.getBearing().setHouskeeping(bearingModel.getHouskeeping());
                            unsyncedInspection.getBearing().setUnsafeC(bearingModel.getUnsafeC());
                            unsyncedInspection.getBearing().setKeterangan(bearingModel.getKeterangan());
                        }
                        else{
                            //unsyncedInspection.getBearing().setPoint(bearingModel.getPoint());
                            unsyncedInspection.getBearing().setA1(bearingModel.getA1());
                            unsyncedInspection.getBearing().setA2(bearingModel.getA2());
                            unsyncedInspection.getBearing().setB1(bearingModel.getB1());
                            unsyncedInspection.getBearing().setB2(bearingModel.getB2());
                            unsyncedInspection.getBearing().setB3(bearingModel.getB3());
                            unsyncedInspection.getBearing().setC1(bearingModel.getC1());
                            unsyncedInspection.getBearing().setC2(bearingModel.getC2());
                            unsyncedInspection.getBearing().setC3(bearingModel.getC3());
                            unsyncedInspection.getBearing().setD1(bearingModel.getD1());
                            unsyncedInspection.getBearing().setD2(bearingModel.getD2());
                            unsyncedInspection.getBearing().setD3(bearingModel.getD3());
                            unsyncedInspection.getBearing().setE1(bearingModel.getE1());
                            unsyncedInspection.getBearing().setE2(bearingModel.getE2());
                            unsyncedInspection.getBearing().setE3(bearingModel.getE3());
                            unsyncedInspection.getBearing().setF1(bearingModel.getF1());
                            unsyncedInspection.getBearing().setF2(bearingModel.getF2());
                            unsyncedInspection.getBearing().setF3(bearingModel.getF3());
                            unsyncedInspection.getBearing().setG1(bearingModel.getG1());
                            unsyncedInspection.getBearing().setG2(bearingModel.getG2());
                            unsyncedInspection.getBearing().setH1(bearingModel.getH1());
                            unsyncedInspection.getBearing().setH2(bearingModel.getH2());
                            unsyncedInspection.getBearing().setI1(bearingModel.getI1());
                            unsyncedInspection.getBearing().setI2(bearingModel.getI2());
                            unsyncedInspection.getBearing().setJ1(bearingModel.getJ1());
                            unsyncedInspection.getBearing().setJ2(bearingModel.getJ2());
                            unsyncedInspection.getBearing().setK1(bearingModel.getK1());
                            unsyncedInspection.getBearing().setK2(bearingModel.getK2());
                            unsyncedInspection.getBearing().setL1(bearingModel.getL1());
                            unsyncedInspection.getBearing().setL2(bearingModel.getL2());
                            unsyncedInspection.getBearing().setM1(bearingModel.getM1());
                            unsyncedInspection.getBearing().setM2(bearingModel.getM2());
                            unsyncedInspection.getBearing().setJmlPenggantian(bearingModel.getJmlPenggantian());
                            unsyncedInspection.getBearing().setFilter(bearingModel.getFilter());
                            unsyncedInspection.getBearing().setRunHours(bearingModel.getRunHours());
                            unsyncedInspection.getBearing().setRotor(bearingModel.getRotor());
                            unsyncedInspection.getBearing().setStator(bearingModel.getStator());
                            unsyncedInspection.getBearing().setMembers(bearingModel.getMembers());
                            unsyncedInspection.getBearing().setKeteranganMMV(bearingModel.getKeteranganMMV());

                            unsyncedInspection.getBearing().setA1L(bearingModel.getA1L());
                            unsyncedInspection.getBearing().setA2L(bearingModel.getA2L());
                            unsyncedInspection.getBearing().setB1L(bearingModel.getB1L());
                            unsyncedInspection.getBearing().setB2L(bearingModel.getB2L());
                            unsyncedInspection.getBearing().setB3L(bearingModel.getB3L());
                            unsyncedInspection.getBearing().setC1L(bearingModel.getC1L());
                            unsyncedInspection.getBearing().setC2L(bearingModel.getC2L());
                            unsyncedInspection.getBearing().setC3L(bearingModel.getC3L());
                            unsyncedInspection.getBearing().setD1L(bearingModel.getD1L());
                            unsyncedInspection.getBearing().setD2L(bearingModel.getD2L());
                            unsyncedInspection.getBearing().setD3L(bearingModel.getD3L());
                            unsyncedInspection.getBearing().setE1L(bearingModel.getE1L());
                            unsyncedInspection.getBearing().setE2L(bearingModel.getE2L());
                            unsyncedInspection.getBearing().setE3L(bearingModel.getE3L());
                            unsyncedInspection.getBearing().setF1L(bearingModel.getF1L());
                            unsyncedInspection.getBearing().setF2L(bearingModel.getF2L());
                            unsyncedInspection.getBearing().setF3L(bearingModel.getF3L());
                            unsyncedInspection.getBearing().setG1L(bearingModel.getG1L());
                            unsyncedInspection.getBearing().setG2L(bearingModel.getG2L());
                            unsyncedInspection.getBearing().setH1L(bearingModel.getH1L());
                            unsyncedInspection.getBearing().setH2L(bearingModel.getH2L());
                            unsyncedInspection.getBearing().setI1l(bearingModel.getI1l());
                            unsyncedInspection.getBearing().setI2l(bearingModel.getI2l());
                            unsyncedInspection.getBearing().setJ1l(bearingModel.getJ1l());
                            unsyncedInspection.getBearing().setJ2l(bearingModel.getJ2l());
                            unsyncedInspection.getBearing().setK1l(bearingModel.getK1l());
                            unsyncedInspection.getBearing().setK2l(bearingModel.getK2l());
                            unsyncedInspection.getBearing().setL1l(bearingModel.getL1l());
                            unsyncedInspection.getBearing().setL2l(bearingModel.getL2l());
                            unsyncedInspection.getBearing().setM1l(bearingModel.getM1l());
                            unsyncedInspection.getBearing().setM2l(bearingModel.getM2l());
                            unsyncedInspection.getBearing().setJmlPenggantianL(bearingModel.getJmlPenggantianL());
                            unsyncedInspection.getBearing().setFilterL(bearingModel.getFilterL());
                            unsyncedInspection.getBearing().setRunHoursL(bearingModel.getRunHoursL());
                            unsyncedInspection.getBearing().setRotorL(bearingModel.getRotorL());
                            unsyncedInspection.getBearing().setStatorL(bearingModel.getStatorL());
                            unsyncedInspection.getBearing().setMembersL(bearingModel.getMembersL());
                            unsyncedInspection.getBearing().setKeteranganMMVL(bearingModel.getKeteranganMMVL());

                            unsyncedInspection.getBearing().setA1M(bearingModel.getA1M());
                            unsyncedInspection.getBearing().setA2M(bearingModel.getA2M());
                            unsyncedInspection.getBearing().setB1M(bearingModel.getB1M());
                            unsyncedInspection.getBearing().setB2M(bearingModel.getB2M());
                            unsyncedInspection.getBearing().setB3M(bearingModel.getB3M());
                            unsyncedInspection.getBearing().setC1M(bearingModel.getC1M());
                            unsyncedInspection.getBearing().setC2M(bearingModel.getC2M());
                            unsyncedInspection.getBearing().setC3M(bearingModel.getC3M());
                            unsyncedInspection.getBearing().setD1M(bearingModel.getD1M());
                            unsyncedInspection.getBearing().setD2M(bearingModel.getD2M());
                            unsyncedInspection.getBearing().setD3M(bearingModel.getD3M());
                            unsyncedInspection.getBearing().setE1M(bearingModel.getE1M());
                            unsyncedInspection.getBearing().setE2M(bearingModel.getE2M());
                            unsyncedInspection.getBearing().setE3M(bearingModel.getE3M());
                            unsyncedInspection.getBearing().setF1M(bearingModel.getF1M());
                            unsyncedInspection.getBearing().setF2M(bearingModel.getF2M());
                            unsyncedInspection.getBearing().setF3M(bearingModel.getF3M());
                            unsyncedInspection.getBearing().setG1M(bearingModel.getG1M());
                            unsyncedInspection.getBearing().setG2M(bearingModel.getG2M());
                            unsyncedInspection.getBearing().setH1M(bearingModel.getH1M());
                            unsyncedInspection.getBearing().setH2M(bearingModel.getH2M());
                            unsyncedInspection.getBearing().setI1m(bearingModel.getI1m());
                            unsyncedInspection.getBearing().setI2m(bearingModel.getI2m());
                            unsyncedInspection.getBearing().setJ1m(bearingModel.getJ1m());
                            unsyncedInspection.getBearing().setJ2m(bearingModel.getJ2m());
                            unsyncedInspection.getBearing().setK1m(bearingModel.getK1m());
                            unsyncedInspection.getBearing().setK2m(bearingModel.getK2m());
                            unsyncedInspection.getBearing().setL1m(bearingModel.getL1m());
                            unsyncedInspection.getBearing().setL2m(bearingModel.getL2m());
                            unsyncedInspection.getBearing().setM1m(bearingModel.getM1m());
                            unsyncedInspection.getBearing().setM2m(bearingModel.getM2m());
                            unsyncedInspection.getBearing().setJmlPenggantianM(bearingModel.getJmlPenggantianM());
                            unsyncedInspection.getBearing().setFilterM(bearingModel.getFilterM());
                            unsyncedInspection.getBearing().setRunHoursM(bearingModel.getRunHoursM());
                            unsyncedInspection.getBearing().setRotorM(bearingModel.getRotorM());
                            unsyncedInspection.getBearing().setStatorM(bearingModel.getStatorM());
                            unsyncedInspection.getBearing().setMembersM(bearingModel.getMembersM());
                            unsyncedInspection.getBearing().setKeteranganMMVM(bearingModel.getKeteranganMMVM());
                        }

                        break;

                    case DetailInspection.TYPE_BELT_WEIGHTER:
                        BeltWeighter beltWeighterModel = (BeltWeighter) mTechnicalModel;
                        unsyncedInspection.getBeltWeighter().setAktivitas(beltWeighterModel.getAktivitas());
                        unsyncedInspection.getBeltWeighter().setJumlahCounter(beltWeighterModel.getJumlahCounter());
                        unsyncedInspection.getBeltWeighter().setSpanCorrection(beltWeighterModel.getSpanCorrection());
                        unsyncedInspection.getBeltWeighter().setQr(beltWeighterModel.getQr());
                        break;

                    case DetailInspection.TYPE_TRANSFORMER:
                        Transformer transformerModel = (Transformer) mTechnicalModel;
                        unsyncedInspection.getTransformer().setTemperaturKoneksi(transformerModel.getTemperaturKoneksi());
                        unsyncedInspection.getTransformer().setTemperaturTrafo(transformerModel.getTemperaturTrafo());
                        unsyncedInspection.getTransformer().setAmperePremier_r(transformerModel.getAmperePremier_r());
                        unsyncedInspection.getTransformer().setAmperePremier_s(transformerModel.getAmperePremier_s());
                        unsyncedInspection.getTransformer().setAmperePremier_t(transformerModel.getAmperePremier_t());
                        unsyncedInspection.getTransformer().setTempWinding(transformerModel.getTempWinding());
                        unsyncedInspection.getTransformer().setOilTemp(transformerModel.getOilTemp());
                        unsyncedInspection.getTransformer().setOilLevel(transformerModel.getOilLevel());
                        unsyncedInspection.getTransformer().setSilicaGell(transformerModel.getSilicaGell());
                        unsyncedInspection.getTransformer().setNegativeList(transformerModel.getNegativeList());
                        break;
                }
            }
        }, new Realm.Transaction.OnSuccess() {
            @Override
            public void onSuccess() {
                if (getView() != null)
                    getView().onInspectionUpdated();
            }
        }, new Realm.Transaction.OnError() {
            @Override
            public void onError(Throwable error) {
                Timber.e(error.getMessage());
                Crashlytics.logException(error.fillInStackTrace());
                if (getView() != null)
                    getView().onInspectionFailedToUpdate();
            }
        });
    }

    private void attemptDeleteLocalInspection() {
        if (getView() != null)
            getView().showProgress("Menghapus inspeksi", "Harap tunggu");

        mAppDataManager.deleteUnsyncedInspection(mInspectionNo, new Realm.Transaction.OnSuccess() {
            @Override
            public void onSuccess() {
                if (getView() != null)
                    getView().onInspectionDeleted();
            }
        }, new Realm.Transaction.OnError() {
            @Override
            public void onError(Throwable error) {
                if (getView() != null)
                    getView().onInspectionFailedToDeleted();
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
