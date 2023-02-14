package id.sisi.si.mso.ui.inspection.add;

import android.support.annotation.Nullable;
import android.util.Log;

import com.crashlytics.android.Crashlytics;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import id.sisi.si.mso.data.AppDataManager;
import id.sisi.si.mso.data.api.MsoService;
import id.sisi.si.mso.data.api.ServiceGenerator;
import id.sisi.si.mso.data.api.model.BaseResponse;
import id.sisi.si.mso.data.api.model.ListResponse;
import id.sisi.si.mso.data.model.Abnormal;
import id.sisi.si.mso.data.model.AbnormalityParams;
import id.sisi.si.mso.data.model.Bearing;
import id.sisi.si.mso.data.model.BeltWeighter;
import id.sisi.si.mso.data.model.EquipmentInfo;
import id.sisi.si.mso.data.model.InspectionActivity;
import id.sisi.si.mso.data.model.InspectionDetail;
import id.sisi.si.mso.data.model.InspectionList;
import id.sisi.si.mso.data.model.Parent;
import id.sisi.si.mso.data.model.ReporterItem;
import id.sisi.si.mso.data.model.SimpleItem;
import id.sisi.si.mso.data.model.TechnicalModel;
import id.sisi.si.mso.data.model.Transformer;
import id.sisi.si.mso.data.model.UnsyncedInspection;
import id.sisi.si.mso.data.model.User;
import id.sisi.si.mso.data.model.picItem;
import id.sisi.si.mso.data.model.typedParams;
import id.sisi.si.mso.ui.base.BasePresenter;
import id.sisi.si.mso.utils.LoggingHelper;
import id.sisi.si.mso.utils.NetworkUtil;
import id.sisi.si.mso.utils.TextUtils;
import io.realm.Realm;
import io.realm.RealmList;
import io.realm.Sort;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.internal.huc.OkHttpsURLConnection;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

/**
 * Created by durrrr on 04-Oct-17.
 * Email: cab.purnama@gmail.com
 */
public class AddInspectionPresenter extends BasePresenter<Contract.AddInspectionView> implements Contract.AddInspectionPresenter {

    public static final int UPLOAD_IMAGE_SUCCESS = 0;
    public static final int UPLOAD_IMAGE_FAILED = 1;

    public static final int EQUIPMENT_CONDITION_GOOD = 0;
    public static final int EQUIPMENT_CONDITION_BAD = 1;

    public static final String TRANSACTION_MODE_KEY = "transaction_mode";
    public static final int ONLINE_MODE = 0;
    public static final int OFFLINE_MODE = 1;
    private int[] index;
    private final AppDataManager mAppDataManager;
    private final Realm mRealm;

    private EquipmentInfo mEquipmentInfo;
    //    private Abnormal mAbnormalModel;
    private AbnormalityParams mAbnormalityParamsModel;
    private int mEquipmentCondition = EQUIPMENT_CONDITION_GOOD;
    private int mTransactionMode = ONLINE_MODE;

    public AddInspectionPresenter(EquipmentInfo equipmentInfo, int transactionMode) {
        mRealm = Realm.getDefaultInstance();
        mAppDataManager = AppDataManager.getInstance();

        mEquipmentInfo = equipmentInfo;
        mTransactionMode = transactionMode;
    }

    @Override
    public void attachView(Contract.AddInspectionView addInspectionView) {
        super.attachView(addInspectionView);

        if (mTransactionMode == ONLINE_MODE) {
            getActivityOptions();
        } else {
            Timber.d("act " + LoggingHelper.getInstance().getJsonString(mAppDataManager.getInspectionActivities()));
            if (mAppDataManager.getInspectionActivities() != null) {
                if (getView() != null) {
                    getView().initView(new ArrayList<>(mAppDataManager.getInspectionActivities()));
                    getView().showDialog("Offline", "Anda berada di dalam mode offline, data selanjutnya disimpan secara lokal");
                }
            } else {
                if (getView() != null)
                    getView().showDialog("Error", "Data aktivitas tidak tersedia");
            }
        }
    }

    @Override
    public void attemptUploadImage(File imageFile) {
        if (NetworkUtil.isConnected()) {
            uploadImage(imageFile);
        } else {
            if (imageFile != null) {
                if (getView() != null)
                    getView().uploadImageResult(UPLOAD_IMAGE_SUCCESS, imageFile.getAbsolutePath(), null);
            } else {
                if (getView() != null)
                    getView().uploadImageResult(UPLOAD_IMAGE_FAILED, null, null);
            }
        }
    }

    private void getActivityOptions() {
        Call<ListResponse<InspectionActivity>> call = ServiceGenerator.getInstance().getRetrofit().create(MsoService.class)
                .getActOptions();
        call.enqueue(new Callback<ListResponse<InspectionActivity>>() {
            @Override
            public void onResponse(Call<ListResponse<InspectionActivity>> call, Response<ListResponse<InspectionActivity>> response) {
                if (!handleErrorResponse(response)) {
                    if (getView() != null)
                        getView().onInitFailed();
                }

                if (response.isSuccessful()) {
                    if (getView() != null)
                        getView().initView(new ArrayList<>(response.body().getData()));
                }
            }

            @Override
            public void onFailure(Call<ListResponse<InspectionActivity>> call, Throwable t) {
                t.printStackTrace();
                if (getView() != null)
                    getView().onInitFailed();
            }
        });
    }

    private void uploadImage(File imageFile) {
        RequestBody reqFile = RequestBody.create(MediaType.parse(imageFile.getAbsolutePath()), imageFile);
        MultipartBody.Part bodyFile = MultipartBody.Part.createFormData("file", imageFile.getName(), reqFile);

        Call<BaseResponse> call = ServiceGenerator.getInstance().getRetrofit().create(MsoService.class).uploadImage(bodyFile);
        call.enqueue(new Callback<BaseResponse>() {
            @Override
            public void onResponse(Call<BaseResponse> call, Response<BaseResponse> response) {
                if (!handleErrorResponse(response)) {
                    if (getView() != null)
                        getView().uploadImageResult(UPLOAD_IMAGE_FAILED, null, null);
                    return;
                }

                final BaseResponse defaultResponse = response.body();
                if (response.isSuccessful() && defaultResponse.isSuccess()) {
                    if (getView() != null) {
                        getView().uploadImageResult(UPLOAD_IMAGE_SUCCESS, null, defaultResponse.getMessage());
                    }

                } else {
                    if (getView() != null)
                        getView().uploadImageResult(UPLOAD_IMAGE_FAILED, null, null);
                }
            }

            @Override
            public void onFailure(Call<BaseResponse> call, Throwable t) {
                if (getView() != null)
                    getView().uploadImageResult(UPLOAD_IMAGE_FAILED, null, null);
            }
        });
    }

    private void getAbnormalityParams() {
        if (NetworkUtil.isConnected()) {
            updateAbnormalityParams();
        } else {
            mAbnormalityParamsModel = mAppDataManager.getCopyOfAbnormalityParams();
        }
    }

    private void updateAbnormalityParams() {
        ServiceGenerator.getInstance().getRetrofit().create(MsoService.class).
                getParamsToAddAbnormality(null).enqueue(new Callback<BaseResponse<AbnormalityParams>>() {
            @Override
            public void onResponse(Call<BaseResponse<AbnormalityParams>> call, final Response<BaseResponse<AbnormalityParams>> response) {
                if (!handleErrorResponse(response)) {
                    if (getView() != null)
                        getView().onSaveFailed();
                    return;
                }

                if (response.isSuccessful()) {
                    if (response.body().getData() != null) {
                        mAppDataManager.addAbnormalityParams(response.body().getData(), new Realm.Transaction.OnSuccess() {
                            @Override
                            public void onSuccess() {
                                mAbnormalityParamsModel = mAppDataManager.getCopyOfAbnormalityParams();
                                if(index.length>0){
                                    if (getView() != null)
                                        getView().setupAbnormalityStep2(mAbnormalityParamsModel,index);
                                }else{
                                    if (getView() != null)
                                        getView().setupAbnormalityStep(mAbnormalityParamsModel);
                                }
                            }
                        }, new Realm.Transaction.OnError() {
                            @Override
                            public void onError(Throwable error) {
                                if (getView() != null)
                                    getView().onSaveFailed();
                            }
                        });
                    }
                } else {
                    if (getView() != null)
                        getView().onSaveFailed();
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<AbnormalityParams>> call, Throwable t) {
                Crashlytics.logException(t);
                if (getView() != null)
                    getView().onSaveFailed();
            }
        });
    }

    @Override
    public void attemptSave(InspectionDetail inspectionDetail, @Nullable TechnicalModel technicalModel,
                            @Nullable Abnormal abnormal, @Nullable TechnicalModel technicalModel2,
                            @Nullable TechnicalModel technicalModel3,@Nullable InspectionDetail condition, int bearingType) {
        if (!NetworkUtil.isConnected()) {
            if (getView() != null)
                getView().onOfflineSave();
            return;
        }

        if (getView() != null)
            getView().showProgress("Menyimpan data", "Harap Tunggu");

        Map<String, String> params = new HashMap<>();
        if (mEquipmentInfo.getType() != null)
            params.put("equipment_type_id", mEquipmentInfo.getType().getId());

        params.put("equipment_name", String.valueOf(mEquipmentInfo.getName()));
        params.put("date", inspectionDetail.getDate());

        if(mEquipmentInfo.getType() == null)
            params.put("condition", String.valueOf(inspectionDetail.getCondition()));
        else
            params.put("condition", String.valueOf(condition.getCondition()));

        params.put("description", inspectionDetail.getDescription());
        params.put("photo_path", inspectionDetail.getPhotoPath());
        params.put("plant", mEquipmentInfo.getPlant().getPlant());
        /*for (int i = 0; i < inspectionDetail.getActivities().size(); i++)
            params.put("activity[" + (i) + "]", inspectionDetail.getActivities().get(i));*/
        params.put("activity", inspectionDetail.getActivity());

        typedParams mEquipmentInfoSub;
        if(bearingType == AddInspectionActivity.TYPE_MCC || bearingType == AddInspectionActivity.TYPE_SERVICE_MOTOR || bearingType == AddInspectionActivity.TYPE_MEKANIKAL)
            mEquipmentInfoSub = mEquipmentInfo.getParams().get(bearingType);
        else
            mEquipmentInfoSub = mEquipmentInfo.getParams().get(0);

        if (mEquipmentInfo.getType() != null) {
            params.put("equipment_subtype_id", mEquipmentInfoSub.getSubTypeId());

            switch (mEquipmentInfo.getType().getId()) {
                case EquipmentInfo.TYPE_BEARING:
                    Bearing bearingModel = (Bearing) technicalModel;
                    if(bearingType == AddInspectionActivity.TYPE_MOTOR) {
                        Bearing bearingModelAfter = ( Bearing ) technicalModel2;
                        params.put(mEquipmentInfoSub.getParams().get(0).getId(), String.valueOf(bearingModel.getRegrease()));
                        params.put(mEquipmentInfoSub.getParams().get(1).getId(), bearingModelAfter.getKelengkapanMotor());
                        params.put(mEquipmentInfoSub.getParams().get(2).getId(), bearingModelAfter.getKeterangan());
                        params.put(mEquipmentInfoSub.getParams().get(3).getId(), String.valueOf(bearingModel.getVibrasiDs()));
                        params.put(mEquipmentInfoSub.getParams().get(4).getId(), String.valueOf(bearingModel.getGeDs()));
                        params.put(mEquipmentInfoSub.getParams().get(5).getId(), String.valueOf(bearingModel.getTemperaturDs()));
                        params.put(mEquipmentInfoSub.getParams().get(6).getId(), String.valueOf(bearingModel.getVibrasiNds()));
                        params.put(mEquipmentInfoSub.getParams().get(7).getId(), String.valueOf(bearingModel.getGeNds()));
                        params.put(mEquipmentInfoSub.getParams().get(8).getId(), String.valueOf(bearingModel.getTemperaturNds()));
                        params.put(mEquipmentInfoSub.getParams().get(9).getId(), String.valueOf(bearingModel.getGedsHor_before()));
                        params.put(mEquipmentInfoSub.getParams().get(10).getId(), String.valueOf(bearingModel.getGeNdsHor_before()));
                        params.put(mEquipmentInfoSub.getParams().get(11).getId(), String.valueOf(bearingModel.getGedsAxial_before()));
                        params.put(mEquipmentInfoSub.getParams().get(12).getId(), String.valueOf(bearingModel.getVibrasiDsHor_before()));
                        params.put(mEquipmentInfoSub.getParams().get(13).getId(), String.valueOf(bearingModel.getVibrasiNdsHor_before()));
                        params.put(mEquipmentInfoSub.getParams().get(14).getId(), String.valueOf(bearingModel.getVibrasiDsAxial_before()));
                        params.put(mEquipmentInfoSub.getParams().get(15).getId(), String.valueOf(bearingModel.getVibrasiNdsAxial_before()));
                        params.put(mEquipmentInfoSub.getParams().get(16).getId(), String.valueOf(bearingModel.getGeNdsAxial_before()));
                        params.put(mEquipmentInfoSub.getParams().get(17).getId(), String.valueOf(bearingModelAfter.getVibrasiDsVert_after()));
                        params.put(mEquipmentInfoSub.getParams().get(18).getId(), String.valueOf(bearingModelAfter.getGeDsVert_after()));
                        params.put(mEquipmentInfoSub.getParams().get(19).getId(), String.valueOf(bearingModelAfter.getVibrasiNdsVert_after()));
                        params.put(mEquipmentInfoSub.getParams().get(20).getId(), String.valueOf(bearingModelAfter.getGeNdsVert_after()));
                        params.put(mEquipmentInfoSub.getParams().get(21).getId(), String.valueOf(bearingModelAfter.getGeDsHor_after()));
                        params.put(mEquipmentInfoSub.getParams().get(22).getId(), String.valueOf(bearingModelAfter.getGeNdsHor_after()));
                        params.put(mEquipmentInfoSub.getParams().get(23).getId(), String.valueOf(bearingModelAfter.getGeDsAxial_after()));
                        params.put(mEquipmentInfoSub.getParams().get(24).getId(), String.valueOf(bearingModelAfter.getVibrasiDsHor_after()));
                        params.put(mEquipmentInfoSub.getParams().get(25).getId(), String.valueOf(bearingModelAfter.getVibrasiNdsHor_after()));
                        params.put(mEquipmentInfoSub.getParams().get(26).getId(), String.valueOf(bearingModelAfter.getVibrasiDsAxial_after()));
                        params.put(mEquipmentInfoSub.getParams().get(27).getId(), String.valueOf(bearingModelAfter.getVibrasiNdsAxial_after()));
                        params.put(mEquipmentInfoSub.getParams().get(28).getId(), String.valueOf(bearingModelAfter.getGeNdsAxial_after()));
                        params.put(mEquipmentInfoSub.getParams().get(29).getId(), String.valueOf(bearingModel.getRegrease_nde()));


                    }
                    else if(bearingType == AddInspectionActivity.TYPE_MCC){
                        params.put(mEquipmentInfoSub.getParams().get(0).getId(), String.valueOf(bearingModel.getKw()));
                        params.put(mEquipmentInfoSub.getParams().get(1).getId(), String.valueOf(bearingModel.getFla()));
                        params.put(mEquipmentInfoSub.getParams().get(2).getId(), String.valueOf(bearingModel.getAmpereSetting()));
                        params.put(mEquipmentInfoSub.getParams().get(3).getId(), String.valueOf(bearingModel.getAmpereRact()));
                        params.put(mEquipmentInfoSub.getParams().get(4).getId(), String.valueOf(bearingModel.getAmpereSact()));
                        params.put(mEquipmentInfoSub.getParams().get(5).getId(), String.valueOf(bearingModel.getAmpereTact()));
                        params.put(mEquipmentInfoSub.getParams().get(6).getId(), String.valueOf(bearingModel.getTemperatureR()));
                        params.put(mEquipmentInfoSub.getParams().get(7).getId(), String.valueOf(bearingModel.getTemperatureS()));
                        params.put(mEquipmentInfoSub.getParams().get(8).getId(), String.valueOf(bearingModel.getTemperatureT()));
                        params.put(mEquipmentInfoSub.getParams().get(9).getId(), bearingModel.getKeterangan());
                    }
                    else if(bearingType == AddInspectionActivity.TYPE_MEKANIKAL){
                        params.put(mEquipmentInfoSub.getParams().get(0).getId(), String.valueOf(bearingModel.getKb_fan()));
                        params.put(mEquipmentInfoSub.getParams().get(1).getId(), String.valueOf(bearingModel.getVib_fan()));
                        params.put(mEquipmentInfoSub.getParams().get(2).getId(), String.valueOf(bearingModel.getKV_fan()));
                        params.put(mEquipmentInfoSub.getParams().get(3).getId(), String.valueOf(bearingModel.getTemp_ds_imp()));
                        params.put(mEquipmentInfoSub.getParams().get(4).getId(), String.valueOf(bearingModel.getTemp_nds_imp()));
                        params.put(mEquipmentInfoSub.getParams().get(5).getId(), String.valueOf(bearingModel.getPulley()));
                        params.put(mEquipmentInfoSub.getParams().get(6).getId(), String.valueOf(bearingModel.getSafetyG()));
                        params.put(mEquipmentInfoSub.getParams().get(7).getId(), String.valueOf(bearingModel.getHouskeeping()));
                        params.put(mEquipmentInfoSub.getParams().get(8).getId(), String.valueOf(bearingModel.getUnsafeC()));
                        params.put(mEquipmentInfoSub.getParams().get(9).getId(), String.valueOf(bearingModel.getKeteranganM()));
                    }
                    else
                    {
                        //params.put(mEquipmentInfoSub.getParams().get(0).getId(), String.valueOf(bearingModel.getPoint()));
                        params.put(mEquipmentInfoSub.getParams().get(0).getId(), String.valueOf(bearingModel.getA1()));
                        params.put(mEquipmentInfoSub.getParams().get(1).getId(), String.valueOf(bearingModel.getA2()));
                        params.put(mEquipmentInfoSub.getParams().get(2).getId(), String.valueOf(bearingModel.getB1()));
                        params.put(mEquipmentInfoSub.getParams().get(3).getId(), String.valueOf(bearingModel.getB2()));
                        params.put(mEquipmentInfoSub.getParams().get(4).getId(), String.valueOf(bearingModel.getB3()));
                        params.put(mEquipmentInfoSub.getParams().get(5).getId(), String.valueOf(bearingModel.getC1()));
                        params.put(mEquipmentInfoSub.getParams().get(6).getId(), String.valueOf(bearingModel.getC2()));
                        params.put(mEquipmentInfoSub.getParams().get(7).getId(), String.valueOf(bearingModel.getC3()));
                        params.put(mEquipmentInfoSub.getParams().get(8).getId(), String.valueOf(bearingModel.getD1()));
                        params.put(mEquipmentInfoSub.getParams().get(9).getId(), String.valueOf(bearingModel.getD2()));
                        params.put(mEquipmentInfoSub.getParams().get(10).getId(), String.valueOf(bearingModel.getD3()));
                        params.put(mEquipmentInfoSub.getParams().get(11).getId(), String.valueOf(bearingModel.getE1()));
                        params.put(mEquipmentInfoSub.getParams().get(12).getId(), String.valueOf(bearingModel.getE2()));
                        params.put(mEquipmentInfoSub.getParams().get(13).getId(), String.valueOf(bearingModel.getE3()));
                        params.put(mEquipmentInfoSub.getParams().get(14).getId(), String.valueOf(bearingModel.getF1()));
                        params.put(mEquipmentInfoSub.getParams().get(15).getId(), String.valueOf(bearingModel.getF2()));
                        params.put(mEquipmentInfoSub.getParams().get(16).getId(), String.valueOf(bearingModel.getF3()));
                        params.put(mEquipmentInfoSub.getParams().get(17).getId(), String.valueOf(bearingModel.getG1()));
                        params.put(mEquipmentInfoSub.getParams().get(18).getId(), String.valueOf(bearingModel.getG2()));
                        params.put(mEquipmentInfoSub.getParams().get(19).getId(), String.valueOf(bearingModel.getH1()));
                        params.put(mEquipmentInfoSub.getParams().get(20).getId(), String.valueOf(bearingModel.getH2()));
                        params.put(mEquipmentInfoSub.getParams().get(21).getId(), String.valueOf(bearingModel.getI1()));
                        params.put(mEquipmentInfoSub.getParams().get(22).getId(), String.valueOf(bearingModel.getI2()));
                        params.put(mEquipmentInfoSub.getParams().get(23).getId(), String.valueOf(bearingModel.getJ1()));
                        params.put(mEquipmentInfoSub.getParams().get(24).getId(), String.valueOf(bearingModel.getJ2()));
                        params.put(mEquipmentInfoSub.getParams().get(25).getId(), String.valueOf(bearingModel.getK1()));
                        params.put(mEquipmentInfoSub.getParams().get(26).getId(), String.valueOf(bearingModel.getK2()));
                        params.put(mEquipmentInfoSub.getParams().get(27).getId(), String.valueOf(bearingModel.getL1()));
                        params.put(mEquipmentInfoSub.getParams().get(28).getId(), String.valueOf(bearingModel.getL2()));
                        params.put(mEquipmentInfoSub.getParams().get(29).getId(), String.valueOf(bearingModel.getM1()));
                        params.put(mEquipmentInfoSub.getParams().get(30).getId(), String.valueOf(bearingModel.getM2()));
                        params.put(mEquipmentInfoSub.getParams().get(31).getId(), String.valueOf(bearingModel.getJmlPenggantian()));
                        params.put(mEquipmentInfoSub.getParams().get(32).getId(), String.valueOf(bearingModel.getFilter()));
                        params.put(mEquipmentInfoSub.getParams().get(33).getId(), String.valueOf(bearingModel.getRunHours()));
                        params.put(mEquipmentInfoSub.getParams().get(34).getId(), String.valueOf(bearingModel.getRotor()));
                        params.put(mEquipmentInfoSub.getParams().get(35).getId(), String.valueOf(bearingModel.getStator()));
                        params.put(mEquipmentInfoSub.getParams().get(36).getId(), bearingModel.getMembers());
                        params.put(mEquipmentInfoSub.getParams().get(37).getId(), bearingModel.getKeteranganMMV());

                        Bearing bearingModelL = ( Bearing ) technicalModel2;
                        params.put(mEquipmentInfoSub.getParams().get(38).getId(), String.valueOf(bearingModelL.getA1L()));
                        params.put(mEquipmentInfoSub.getParams().get(39).getId(), String.valueOf(bearingModelL.getA2L()));
                        params.put(mEquipmentInfoSub.getParams().get(40).getId(), String.valueOf(bearingModelL.getB1L()));
                        params.put(mEquipmentInfoSub.getParams().get(41).getId(), String.valueOf(bearingModelL.getB2L()));
                        params.put(mEquipmentInfoSub.getParams().get(42).getId(), String.valueOf(bearingModelL.getB3L()));
                        params.put(mEquipmentInfoSub.getParams().get(43).getId(), String.valueOf(bearingModelL.getC1L()));
                        params.put(mEquipmentInfoSub.getParams().get(44).getId(), String.valueOf(bearingModelL.getC2L()));
                        params.put(mEquipmentInfoSub.getParams().get(45).getId(), String.valueOf(bearingModelL.getC3L()));
                        params.put(mEquipmentInfoSub.getParams().get(46).getId(), String.valueOf(bearingModelL.getD1L()));
                        params.put(mEquipmentInfoSub.getParams().get(47).getId(), String.valueOf(bearingModelL.getD2L()));
                        params.put(mEquipmentInfoSub.getParams().get(48).getId(), String.valueOf(bearingModelL.getD3L()));
                        params.put(mEquipmentInfoSub.getParams().get(49).getId(), String.valueOf(bearingModelL.getE1L()));
                        params.put(mEquipmentInfoSub.getParams().get(50).getId(), String.valueOf(bearingModelL.getE2L()));
                        params.put(mEquipmentInfoSub.getParams().get(51).getId(), String.valueOf(bearingModelL.getE3L()));
                        params.put(mEquipmentInfoSub.getParams().get(52).getId(), String.valueOf(bearingModelL.getF1L()));
                        params.put(mEquipmentInfoSub.getParams().get(53).getId(), String.valueOf(bearingModelL.getF2L()));
                        params.put(mEquipmentInfoSub.getParams().get(54).getId(), String.valueOf(bearingModelL.getF3L()));
                        params.put(mEquipmentInfoSub.getParams().get(55).getId(), String.valueOf(bearingModelL.getG1L()));
                        params.put(mEquipmentInfoSub.getParams().get(56).getId(), String.valueOf(bearingModelL.getG2L()));
                        params.put(mEquipmentInfoSub.getParams().get(57).getId(), String.valueOf(bearingModelL.getH1L()));
                        params.put(mEquipmentInfoSub.getParams().get(58).getId(), String.valueOf(bearingModelL.getH2L()));
                        params.put(mEquipmentInfoSub.getParams().get(59).getId(), String.valueOf(bearingModelL.getI1l()));
                        params.put(mEquipmentInfoSub.getParams().get(60).getId(), String.valueOf(bearingModelL.getI2l()));
                        params.put(mEquipmentInfoSub.getParams().get(61).getId(), String.valueOf(bearingModelL.getJ1l()));
                        params.put(mEquipmentInfoSub.getParams().get(62).getId(), String.valueOf(bearingModelL.getJ2l()));
                        params.put(mEquipmentInfoSub.getParams().get(63).getId(), String.valueOf(bearingModelL.getK1l()));
                        params.put(mEquipmentInfoSub.getParams().get(64).getId(), String.valueOf(bearingModelL.getK2l()));
                        params.put(mEquipmentInfoSub.getParams().get(65).getId(), String.valueOf(bearingModelL.getL1l()));
                        params.put(mEquipmentInfoSub.getParams().get(66).getId(), String.valueOf(bearingModelL.getL2l()));
                        params.put(mEquipmentInfoSub.getParams().get(67).getId(), String.valueOf(bearingModelL.getM1l()));
                        params.put(mEquipmentInfoSub.getParams().get(68).getId(), String.valueOf(bearingModelL.getM2l()));
                        params.put(mEquipmentInfoSub.getParams().get(69).getId(), String.valueOf(bearingModelL.getJmlPenggantianL()));
                        params.put(mEquipmentInfoSub.getParams().get(70).getId(), String.valueOf(bearingModelL.getFilterL()));
                        params.put(mEquipmentInfoSub.getParams().get(71).getId(), String.valueOf(bearingModelL.getRunHoursL()));
                        params.put(mEquipmentInfoSub.getParams().get(72).getId(), String.valueOf(bearingModelL.getRotorL()));
                        params.put(mEquipmentInfoSub.getParams().get(73).getId(), String.valueOf(bearingModelL.getStatorL()));
                        params.put(mEquipmentInfoSub.getParams().get(74).getId(), bearingModelL.getMembersL());
                        params.put(mEquipmentInfoSub.getParams().get(75).getId(), bearingModelL.getKeteranganMMVL());

                        Bearing bearingModelM = ( Bearing ) technicalModel3;
                        params.put(mEquipmentInfoSub.getParams().get(76).getId(), String.valueOf(bearingModelM.getA1M()));
                        params.put(mEquipmentInfoSub.getParams().get(77).getId(), String.valueOf(bearingModelM.getA2M()));
                        params.put(mEquipmentInfoSub.getParams().get(78).getId(), String.valueOf(bearingModelM.getB1M()));
                        params.put(mEquipmentInfoSub.getParams().get(79).getId(), String.valueOf(bearingModelM.getB2M()));
                        params.put(mEquipmentInfoSub.getParams().get(80).getId(), String.valueOf(bearingModelM.getB3M()));
                        params.put(mEquipmentInfoSub.getParams().get(81).getId(), String.valueOf(bearingModelM.getC1M()));
                        params.put(mEquipmentInfoSub.getParams().get(82).getId(), String.valueOf(bearingModelM.getC2M()));
                        params.put(mEquipmentInfoSub.getParams().get(83).getId(), String.valueOf(bearingModelM.getC3M()));
                        params.put(mEquipmentInfoSub.getParams().get(84).getId(), String.valueOf(bearingModelM.getD1M()));
                        params.put(mEquipmentInfoSub.getParams().get(85).getId(), String.valueOf(bearingModelM.getD2M()));
                        params.put(mEquipmentInfoSub.getParams().get(86).getId(), String.valueOf(bearingModelM.getD3M()));
                        params.put(mEquipmentInfoSub.getParams().get(87).getId(), String.valueOf(bearingModelM.getE1M()));
                        params.put(mEquipmentInfoSub.getParams().get(88).getId(), String.valueOf(bearingModelM.getE2M()));
                        params.put(mEquipmentInfoSub.getParams().get(89).getId(), String.valueOf(bearingModelM.getE3M()));
                        params.put(mEquipmentInfoSub.getParams().get(90).getId(), String.valueOf(bearingModelM.getF1M()));
                        params.put(mEquipmentInfoSub.getParams().get(91).getId(), String.valueOf(bearingModelM.getF2M()));
                        params.put(mEquipmentInfoSub.getParams().get(92).getId(), String.valueOf(bearingModelM.getF3M()));
                        params.put(mEquipmentInfoSub.getParams().get(93).getId(), String.valueOf(bearingModelM.getG1M()));
                        params.put(mEquipmentInfoSub.getParams().get(94).getId(), String.valueOf(bearingModelM.getG2M()));
                        params.put(mEquipmentInfoSub.getParams().get(95).getId(), String.valueOf(bearingModelM.getH1M()));
                        params.put(mEquipmentInfoSub.getParams().get(96).getId(), String.valueOf(bearingModelM.getH2M()));
                        params.put(mEquipmentInfoSub.getParams().get(97).getId(), String.valueOf(bearingModelM.getI1m()));
                        params.put(mEquipmentInfoSub.getParams().get(98).getId(), String.valueOf(bearingModelM.getI2m()));
                        params.put(mEquipmentInfoSub.getParams().get(99).getId(), String.valueOf(bearingModelM.getJ2m()));
                        params.put(mEquipmentInfoSub.getParams().get(100).getId(), String.valueOf(bearingModelM.getJ2m()));
                        params.put(mEquipmentInfoSub.getParams().get(101).getId(), String.valueOf(bearingModelM.getK1m()));
                        params.put(mEquipmentInfoSub.getParams().get(102).getId(), String.valueOf(bearingModelM.getK2m()));
                        params.put(mEquipmentInfoSub.getParams().get(103).getId(), String.valueOf(bearingModelM.getL1m()));
                        params.put(mEquipmentInfoSub.getParams().get(104).getId(), String.valueOf(bearingModelM.getL2m()));
                        params.put(mEquipmentInfoSub.getParams().get(105).getId(), String.valueOf(bearingModelM.getM1m()));
                        params.put(mEquipmentInfoSub.getParams().get(106).getId(), String.valueOf(bearingModelM.getM2m()));
                        params.put(mEquipmentInfoSub.getParams().get(107).getId(), String.valueOf(bearingModelM.getJmlPenggantianM()));
                        params.put(mEquipmentInfoSub.getParams().get(108).getId(), String.valueOf(bearingModelM.getFilterM()));
                        params.put(mEquipmentInfoSub.getParams().get(109).getId(), String.valueOf(bearingModelM.getRunHoursM()));
                        params.put(mEquipmentInfoSub.getParams().get(110).getId(), String.valueOf(bearingModelM.getRotorM()));
                        params.put(mEquipmentInfoSub.getParams().get(111).getId(), String.valueOf(bearingModelM.getStatorM()));
                        params.put(mEquipmentInfoSub.getParams().get(112).getId(), bearingModelM.getMembersM());
                        params.put(mEquipmentInfoSub.getParams().get(113).getId(), bearingModelM.getKeteranganMMVM());
                    }
                    break;

                case EquipmentInfo.TYPE_BELT_WEIGHTER:
                    BeltWeighter beltWeighterModel = (BeltWeighter) technicalModel;
                    params.put(mEquipmentInfoSub.getParams().get(0).getId(), String.valueOf(beltWeighterModel.getAktivitas()));
                    params.put(mEquipmentInfoSub.getParams().get(1).getId(), String.valueOf(beltWeighterModel.getQr()));
                    params.put(mEquipmentInfoSub.getParams().get(2).getId(), String.valueOf(beltWeighterModel.getJumlahCounter()));
                    params.put(mEquipmentInfoSub.getParams().get(3).getId(), String.valueOf(beltWeighterModel.getSpanCorrection()));
                    break;
                case EquipmentInfo.TYPE_TRANSFORMER:
                    Transformer transformerModel = (Transformer) technicalModel;
                    params.put(mEquipmentInfoSub.getParams().get(0).getId(), String.valueOf(transformerModel.getTemperaturTrafo()));
                    params.put(mEquipmentInfoSub.getParams().get(1).getId(), String.valueOf(transformerModel.getTemperaturKoneksi()));
                    params.put(mEquipmentInfoSub.getParams().get(2).getId(), String.valueOf(transformerModel.getAmperePremier_r()));
                    params.put(mEquipmentInfoSub.getParams().get(3).getId(), String.valueOf(transformerModel.getAmperePremier_s()));
                    params.put(mEquipmentInfoSub.getParams().get(4).getId(), String.valueOf(transformerModel.getAmperePremier_t()));
                    params.put(mEquipmentInfoSub.getParams().get(5).getId(), String.valueOf(transformerModel.getTempWinding()));
                    params.put(mEquipmentInfoSub.getParams().get(6).getId(), String.valueOf(transformerModel.getOilTemp()));
                    params.put(mEquipmentInfoSub.getParams().get(7).getId(), String.valueOf(transformerModel.getOilLevel()));
                    params.put(mEquipmentInfoSub.getParams().get(8).getId(), String.valueOf(transformerModel.getSilicaGell()));
                    params.put(mEquipmentInfoSub.getParams().get(9).getId(), transformerModel.getNegativeList());
                    break;
            }
        }

        if (mEquipmentCondition == EQUIPMENT_CONDITION_BAD ) {
            abnormal.setAbnormalDate(inspectionDetail.getDate());
            abnormal.setReportedBy(new ReporterItem("", ""));
            abnormal.setEquipmentId(mEquipmentInfo.getId());
            abnormal.setNomenclature(mEquipmentInfo.getName());
            abnormal.setMpi(mEquipmentInfo.getMpi());
            abnormal.setPictBefore(inspectionDetail.getPhotoPath());
            abnormal.setSynced(true);

            params.put("abnormal_date", abnormal.getAbnormalDate());
            params.put("abnormal_plant", abnormal.getPlant().getPlant());
            params.put("abnormal_reported_by", abnormal.getReportedBy().getValue());
            params.put("abnormal_equipment_id", String.valueOf(abnormal.getEquipmentId()));
            params.put("abnormal_source", String.valueOf(abnormal.getSource().getValue()));
            params.put("abnormal_priority", String.valueOf(abnormal.getPriority().getValue()));
            params.put("abnormal_condition", String.valueOf(abnormal.getCondition().getValue()));
            params.put("abnormal_action", String.valueOf(abnormal.getAction().getValue()));
            params.put("abnormal_abnormal", abnormal.getAbnormal());
            params.put("abnormal_activity", abnormal.getActivity());
            params.put("abnormal_remark", abnormal.getRemark());
            params.put("abnormal_pict_before", abnormal.getPictBefore());
            params.put("abnormal_pict_after", abnormal.getPictAfter());
        }

        Timber.d("params" + LoggingHelper.getInstance().getJsonString(params));
        Call<BaseResponse> call = ServiceGenerator.getInstance().getRetrofit().create(MsoService.class).saveInspection(params);
        call.enqueue(new Callback<BaseResponse>() {
            @Override
            public void onResponse(Call<BaseResponse> call, Response<BaseResponse> response) {
                if (!handleErrorResponse(response)) {
                    Timber.d(response.message());
                    if (getView() != null)
                        getView().onSaveFailed();
                    return;
                }

                if (response.isSuccessful()) {
                    if (getView() != null)
                        getView().onSaveSuccess();
                } else {
                    Timber.d(response.message());
                    if (getView() != null)
                        getView().onSaveFailed();
                }
            }

            @Override
            public void onFailure(Call<BaseResponse> call, Throwable t) {
                Crashlytics.logException(t);
                Timber.d(t.getMessage());
                t.printStackTrace();
                if (getView() != null)
                    getView().onSaveFailed();
            }
        });
    }

    @Override
    public void attemptLocalSave(final InspectionDetail inspectionDetail, @Nullable TechnicalModel technicalModel,
                                 @Nullable Abnormal abnormal, @Nullable TechnicalModel technicalModel2,
                                 @Nullable TechnicalModel technicalModel3, @Nullable final InspectionDetail condition, int bearingType) {
        if (getView() != null)
            getView().showProgress("Menyimpan data lokal", "Harap Tunggu");

        final UnsyncedInspection unsyncedInspectionModel = new UnsyncedInspection();

        if (mEquipmentInfo.getType() != null) {
            if(mEquipmentInfo.getType().getId().equalsIgnoreCase(EquipmentInfo.TYPE_BEARING)) {
                unsyncedInspectionModel.setEquipmentSubTypeId(String.valueOf(bearingType + 1));
            }else
                unsyncedInspectionModel.setEquipmentSubTypeId("1");
            unsyncedInspectionModel.setEquipmentTypeId(mEquipmentInfo.getType().getId());
            unsyncedInspectionModel.setEquipmentTypeName(mEquipmentInfo.getType().getName());
        }

        final User user = mRealm.where(Parent.class).findFirst().getLoggedInUser();

        final int kondisi;
        Long inspectionNo = 0L;
        try {
            InspectionList lastRegisteredInspection = user.getInspectionList().sort("inspectionNo", Sort.DESCENDING).first();
            inspectionNo = Long.valueOf(lastRegisteredInspection.getInspectionNo()) + 1;
        } catch (NullPointerException | IndexOutOfBoundsException e) {

        }

        if(mEquipmentInfo.getType() == null) {
            unsyncedInspectionModel.setCondition(String.valueOf(inspectionDetail.getCondition()));
            kondisi = inspectionDetail.getCondition();
        }
        else {
            unsyncedInspectionModel.setCondition(String.valueOf(condition.getCondition()));
            kondisi = condition.getCondition();
        }
        unsyncedInspectionModel.setUnsyncedInspectionNo(inspectionNo);
        unsyncedInspectionModel.setEquipmentName(mEquipmentInfo.getName());
        unsyncedInspectionModel.setDate(inspectionDetail.getDate());

        unsyncedInspectionModel.setDescription(inspectionDetail.getDescription());
        unsyncedInspectionModel.setPlant(mEquipmentInfo.getPlant().getPlant());
        unsyncedInspectionModel.setActivity(inspectionDetail.getActivity());

        if (!TextUtils.isNullOrEmpty(inspectionDetail.getPhotoPath()))
            unsyncedInspectionModel.setPhotoPath(inspectionDetail.getPhotoPath());
        if (!TextUtils.isNullOrEmpty(inspectionDetail.getLocalPhotoPath()))
            unsyncedInspectionModel.setLocalPhotoPath(inspectionDetail.getLocalPhotoPath());

        RealmList<SimpleItem> activities = new RealmList<>();
        /*for (int i = 0; i < inspectionDetail.getActivities().size(); i++) {
            activities.add(new SimpleItem("activity[" + (i) + "]", inspectionDetail.getActivities().get(i)));
        }
        unsyncedInspectionModel.setActivities(activities);*/


        if (mEquipmentInfo.getType() != null) {
            switch (mEquipmentInfo.getType().getId()) {
                case EquipmentInfo.TYPE_BEARING:
                    Bearing bearingTamp = new Bearing();
                    Bearing bearingModel = (Bearing) technicalModel;
                    Bearing bearingModel2 = ( Bearing ) technicalModel2;
                    Bearing bearingModel3 = ( Bearing ) technicalModel3;

                    if(bearingType == AddInspectionActivity.TYPE_MOTOR) {
                        bearingTamp.setRegrease(bearingModel.getRegrease());
                        bearingTamp.setRegrease_nde(bearingModel.getRegrease_nde());
                        bearingTamp.setGeDs(bearingModel.getGeDs());
                        bearingTamp.setGedsHor_before(bearingModel.getGedsHor_before());
                        bearingTamp.setGedsAxial_before(bearingModel.getGedsAxial_before());
                        bearingTamp.setTemperaturDs(bearingModel.getTemperaturDs());
                        bearingTamp.setTemperaturNds(bearingModel.getTemperaturNds());
                        bearingTamp.setVibrasiDs(bearingModel.getVibrasiDs());
                        bearingTamp.setVibrasiDsAxial_before(bearingModel.getVibrasiDsAxial_before());
                        bearingTamp.setVibrasiDsHor_before(bearingModel.getVibrasiDsHor_before());
                        bearingTamp.setGeNds(bearingModel.getGeNds());
                        bearingTamp.setGeNdsHor_before(bearingModel.getGeNdsHor_before());
                        bearingTamp.setGeNdsAxial_before(bearingModel.getGeNdsAxial_before());
                        bearingTamp.setVibrasiNds(bearingModel.getVibrasiNds());
                        bearingTamp.setVibrasiNdsHor_before(bearingModel.getVibrasiNdsHor_before());
                        bearingTamp.setVibrasiNdsAxial_before(bearingModel.getVibrasiNdsAxial_before());
                        //after
                        bearingTamp.setKeterangan(bearingModel2.getKeterangan());
                        bearingTamp.setKelengkapanMotor(bearingModel2.getKelengkapanMotor());
                        bearingTamp.setGeDsVert_after(bearingModel2.getGeDsVert_after());
                        bearingTamp.setGeDsHor_after(bearingModel2.getGeDsHor_after());
                        bearingTamp.setGeDsAxial_after(bearingModel2.getGeDsAxial_after());
                        bearingTamp.setVibrasiDsVert_after(bearingModel2.getVibrasiDsVert_after());
                        bearingTamp.setVibrasiDsAxial_after(bearingModel2.getVibrasiDsAxial_after());
                        bearingTamp.setVibrasiDsHor_after(bearingModel2.getVibrasiDsHor_after());
                        bearingTamp.setGeNdsVert_after(bearingModel2.getGeNdsVert_after());
                        bearingTamp.setGeNdsHor_after(bearingModel2.getGeNdsHor_after());
                        bearingTamp.setGeNdsAxial_after(bearingModel2.getGeNdsAxial_after());
                        bearingTamp.setVibrasiNdsVert_after(bearingModel2.getVibrasiNdsVert_after());
                        bearingTamp.setVibrasiNdsHor_after(bearingModel2.getVibrasiNdsHor_after());
                        bearingTamp.setVibrasiNdsAxial_after(bearingModel2.getVibrasiNdsAxial_after());
                    }
                    else if(bearingType == AddInspectionActivity.TYPE_MCC)
                    {
                        bearingTamp.setKw(bearingModel.getKw());
                        bearingTamp.setFla(bearingModel.getFla());
                        bearingTamp.setAmpereSetting(bearingModel.getAmpereSetting());
                        bearingTamp.setAmpereRact(bearingModel.getAmpereRact());
                        bearingTamp.setAmpereTact(bearingModel.getAmpereTact());
                        bearingTamp.setAmpereSact(bearingModel.getAmpereSact());
                        bearingTamp.setTemperatureR(bearingModel.getTemperatureR());
                        bearingTamp.setTemperatureT(bearingModel.getTemperatureT());
                        bearingTamp.setTemperatureS(bearingModel.getTemperatureS());
                        bearingTamp.setKeterangan(bearingModel.getKeterangan());

                    }
                    else if(bearingType == AddInspectionActivity.TYPE_MEKANIKAL){
                        bearingTamp.setTemp_ds_imp(bearingModel.getTemp_ds_imp());
                        bearingTamp.setTemp_nds_imp(bearingModel.getTemp_nds_imp());
                        bearingTamp.setKb_fan(bearingModel.getKb_fan());
                        bearingTamp.setVib_fan(bearingModel.getVib_fan());
                        bearingTamp.setKV_fan(bearingModel.getKV_fan());
                        bearingTamp.setPulley(bearingModel.getPulley());
                        bearingTamp.setSafetyG(bearingModel.getSafetyG());
                        bearingTamp.setHouskeeping(bearingModel.getHouskeeping());
                        bearingTamp.setUnsafeC(bearingModel.getUnsafeC());
                        bearingTamp.setKeteranganM(bearingModel.getKeteranganM());
                    }
                    else
                    {
                        //bearingTamp.setPoint(bearingModel.getPoint());
                        bearingTamp.setA1(bearingModel.getA1());
                        bearingTamp.setA2(bearingModel.getA2());
                        bearingTamp.setB1(bearingModel.getB1());
                        bearingTamp.setB2(bearingModel.getB2());
                        bearingTamp.setB3(bearingModel.getB3());
                        bearingTamp.setC1(bearingModel.getC1());
                        bearingTamp.setC2(bearingModel.getC2());
                        bearingTamp.setC3(bearingModel.getC3());
                        bearingTamp.setD1(bearingModel.getD1());
                        bearingTamp.setD2(bearingModel.getD2());
                        bearingTamp.setD3(bearingModel.getD3());
                        bearingTamp.setE1(bearingModel.getE1());
                        bearingTamp.setE2(bearingModel.getE2());
                        bearingTamp.setE3(bearingModel.getE3());
                        bearingTamp.setF1(bearingModel.getF1());
                        bearingTamp.setF2(bearingModel.getF2());
                        bearingTamp.setF3(bearingModel.getF3());
                        bearingTamp.setG1(bearingModel.getG1());
                        bearingTamp.setG2(bearingModel.getG2());
                        bearingTamp.setH1(bearingModel.getH1());
                        bearingTamp.setH2(bearingModel.getH2());
                        bearingTamp.setI1(bearingModel.getI1());
                        bearingTamp.setI2(bearingModel.getI2());
                        bearingTamp.setJ1(bearingModel.getJ1());
                        bearingTamp.setJ2(bearingModel.getJ2());
                        bearingTamp.setK1(bearingModel.getK1());
                        bearingTamp.setK2(bearingModel.getK2());
                        bearingTamp.setL1(bearingModel.getL1());
                        bearingTamp.setL2(bearingModel.getL2());
                        bearingTamp.setM1(bearingModel.getM1());
                        bearingTamp.setM2(bearingModel.getM2());
                        bearingTamp.setJmlPenggantian(bearingModel.getJmlPenggantian());
                        bearingTamp.setFilter(bearingModel.getFilter());
                        bearingTamp.setRunHours(bearingModel.getRunHours());
                        bearingTamp.setRotor(bearingModel.getRotor());
                        bearingTamp.setStator(bearingModel.getStator());
                        bearingTamp.setMembers(bearingModel.getMembers());
                        bearingTamp.setKeteranganMMV(bearingModel.getKeteranganMMV());

                        bearingTamp.setA1L(bearingModel2.getA1L());
                        bearingTamp.setA2L(bearingModel2.getA2L());
                        bearingTamp.setB1L(bearingModel2.getB1L());
                        bearingTamp.setB2L(bearingModel2.getB2L());
                        bearingTamp.setB3L(bearingModel2.getB3L());
                        bearingTamp.setC1L(bearingModel2.getC1L());
                        bearingTamp.setC2L(bearingModel2.getC2L());
                        bearingTamp.setC3L(bearingModel2.getC3L());
                        bearingTamp.setD1L(bearingModel2.getD1L());
                        bearingTamp.setD2L(bearingModel2.getD2L());
                        bearingTamp.setD3L(bearingModel2.getD3L());
                        bearingTamp.setE1L(bearingModel2.getE1L());
                        bearingTamp.setE2L(bearingModel2.getE2L());
                        bearingTamp.setE3L(bearingModel2.getE3L());
                        bearingTamp.setF1L(bearingModel2.getF1L());
                        bearingTamp.setF2L(bearingModel2.getF2L());
                        bearingTamp.setF3L(bearingModel2.getF3L());
                        bearingTamp.setG1L(bearingModel2.getG1L());
                        bearingTamp.setG2L(bearingModel2.getG2L());
                        bearingTamp.setH1L(bearingModel2.getH1L());
                        bearingTamp.setH2L(bearingModel2.getH2L());
                        bearingTamp.setI1l(bearingModel2.getI1l());
                        bearingTamp.setI2l(bearingModel2.getI2l());
                        bearingTamp.setJ1l(bearingModel2.getJ1l());
                        bearingTamp.setJ2l(bearingModel2.getJ2l());
                        bearingTamp.setK1l(bearingModel2.getK1l());
                        bearingTamp.setK2l(bearingModel2.getK2l());
                        bearingTamp.setL1l(bearingModel2.getL1l());
                        bearingTamp.setL2l(bearingModel2.getL2l());
                        bearingTamp.setM1l(bearingModel2.getM1l());
                        bearingTamp.setM2l(bearingModel2.getM2l());
                        bearingTamp.setJmlPenggantianL(bearingModel2.getJmlPenggantianL());
                        bearingTamp.setFilterL(bearingModel2.getFilterL());
                        bearingTamp.setRunHoursL(bearingModel2.getRunHoursL());
                        bearingTamp.setRotorL(bearingModel2.getRotorL());
                        bearingTamp.setStatorL(bearingModel2.getStatorL());
                        bearingTamp.setMembersL(bearingModel2.getMembersL());
                        bearingTamp.setKeteranganMMVL(bearingModel2.getKeteranganMMVL());

                        bearingTamp.setA1M(bearingModel3.getA1M());
                        bearingTamp.setA2M(bearingModel3.getA2M());
                        bearingTamp.setB1M(bearingModel3.getB1M());
                        bearingTamp.setB2M(bearingModel3.getB2M());
                        bearingTamp.setB3M(bearingModel3.getB3M());
                        bearingTamp.setC1M(bearingModel3.getC1M());
                        bearingTamp.setC2M(bearingModel3.getC2M());
                        bearingTamp.setC3M(bearingModel3.getC3M());
                        bearingTamp.setD1M(bearingModel3.getD1M());
                        bearingTamp.setD2M(bearingModel3.getD2M());
                        bearingTamp.setD3M(bearingModel3.getD3M());
                        bearingTamp.setE1M(bearingModel3.getE1M());
                        bearingTamp.setE2M(bearingModel3.getE2M());
                        bearingTamp.setE3M(bearingModel3.getE3M());
                        bearingTamp.setF1M(bearingModel3.getF1M());
                        bearingTamp.setF2M(bearingModel3.getF2M());
                        bearingTamp.setF3M(bearingModel3.getF3M());
                        bearingTamp.setG1M(bearingModel3.getG1M());
                        bearingTamp.setG2M(bearingModel3.getG2M());
                        bearingTamp.setH1M(bearingModel3.getH1M());
                        bearingTamp.setH2M(bearingModel3.getH2M());
                        bearingTamp.setI1m(bearingModel3.getI1m());
                        bearingTamp.setI2m(bearingModel3.getI2m());
                        bearingTamp.setJ1m(bearingModel3.getJ1m());
                        bearingTamp.setJ2m(bearingModel3.getJ2m());
                        bearingTamp.setK1m(bearingModel3.getK1m());
                        bearingTamp.setK2m(bearingModel3.getK2m());
                        bearingTamp.setL1m(bearingModel3.getL1m());
                        bearingTamp.setL2m(bearingModel3.getL2m());
                        bearingTamp.setM1m(bearingModel3.getM1m());
                        bearingTamp.setM2m(bearingModel3.getM2m());
                        bearingTamp.setJmlPenggantianM(bearingModel3.getJmlPenggantianM());
                        bearingTamp.setFilterM(bearingModel3.getFilterM());
                        bearingTamp.setRunHoursM(bearingModel3.getRunHoursM());
                        bearingTamp.setRotorM(bearingModel3.getRotorM());
                        bearingTamp.setStatorM(bearingModel3.getStatorM());
                        bearingTamp.setMembersM(bearingModel3.getMembersM());
                        bearingTamp.setKeteranganMMVM(bearingModel3.getKeteranganMMVM());
                    }
                    unsyncedInspectionModel.setBearing(bearingTamp);
                    break;

                case EquipmentInfo.TYPE_BELT_WEIGHTER:
                    unsyncedInspectionModel.setBeltWeighter((BeltWeighter) technicalModel);
                    break;

                case EquipmentInfo.TYPE_TRANSFORMER:
                    unsyncedInspectionModel.setTransformer((Transformer) technicalModel);
                    break;
            }
        }

        if (mEquipmentCondition == EQUIPMENT_CONDITION_BAD) {
            Long abnormalNo = 0L;
            try {
                Abnormal lastRegisteredAbnormal = user.getAbnormalityByType(Abnormal.Type.PENDING)
                        .sort("abnormalNumber", Sort.DESCENDING).first();
                abnormalNo = lastRegisteredAbnormal.getAbnormalNo() + 1;
            } catch (NullPointerException | IndexOutOfBoundsException e) {

            }

            abnormal.setAbnormalNo(abnormalNo);
            abnormal.setAbnormalDate(inspectionDetail.getDate());
            abnormal.setReportedBy(new ReporterItem("", ""));
            abnormal.setEquipmentId(mEquipmentInfo.getId());
            abnormal.setNomenclature(mEquipmentInfo.getName());
            abnormal.setMpi(mEquipmentInfo.getMpi());
            abnormal.setEquipmentCode(mEquipmentInfo.getCode());
            abnormal.setActionToConfirm("not_permitted");
            abnormal.setActionToNotif("not_permitted");
            abnormal.setActionToCancel("not_permitted");
            abnormal.setActionToEdit("permitted");
            abnormal.setActionToDelete("not_permitted");
            abnormal.setActionToAddtask("not_permitted");
            abnormal.setSynced(false);
            abnormal.setFromInspection(true);

            unsyncedInspectionModel.setAbnormal(abnormal);
        }

        mAppDataManager.addUnsyncedInspection(unsyncedInspectionModel, new Realm.Transaction.OnSuccess() {
            @Override
            public void onSuccess() {
                saveToLocalListInspection(unsyncedInspectionModel.getUnsyncedInspectionNo(), inspectionDetail, kondisi);
            }
        }, new Realm.Transaction.OnError() {
            @Override
            public void onError(Throwable error) {
                if (getView() != null)
                    getView().onOfflineSaveFailed();
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

    private void saveToLocalListInspection(Long inspectionNo, InspectionDetail inspectionDetail, int condition) {
        InspectionList inspectionList = new InspectionList();
        inspectionList.setInspectionNo(inspectionNo);
        inspectionList.setEquipmentName(mEquipmentInfo.getName());
        inspectionList.setDate(inspectionDetail.getDate());
        inspectionList.setCondition(String.valueOf(condition));
        inspectionList.setSynced(false);

        mAppDataManager.addUnsyncedInspectionList(inspectionList, new Realm.Transaction.OnSuccess() {
            @Override
            public void onSuccess() {
                if (getView() != null)
                    getView().onOfflineSaveSuccess();
            }
        }, new Realm.Transaction.OnError() {
            @Override
            public void onError(Throwable error) {
                if (getView() != null)
                    getView().onOfflineSaveFailed();
            }
        });
    }
    public void setEquipmentCondition2(int condition, int[] index) {
        Log.d("setEquipmentCondition: ","jalan");
        mEquipmentCondition = condition;
        this.index=index;
        if (mEquipmentCondition == EQUIPMENT_CONDITION_BAD) {
            if (mAbnormalityParamsModel == null)
                getAbnormalityParams();
//            Log.d("mAbnormalParams ",mAbnormalityParamsModel.getDefaultPsection());

            if (mAbnormalityParamsModel != null) {
                Log.d("mAbnormalityParams ", String.valueOf(getView()));
                if (getView() != null)
                    getView().setupAbnormalityStep2(mAbnormalityParamsModel,index);
            }

        } else if (mEquipmentCondition == EQUIPMENT_CONDITION_GOOD) {
            if (getView() != null)
                getView().removeAbnormalityStep();
        }
    }
    @Override
    public void setEquipmentCondition(int condition) {
        mEquipmentCondition = condition;

        if (mEquipmentCondition == EQUIPMENT_CONDITION_BAD) {
            if (mAbnormalityParamsModel == null)
                getAbnormalityParams();

            if (mAbnormalityParamsModel != null) {
                if (getView() != null)
                    getView().setupAbnormalityStep(mAbnormalityParamsModel);
            }

        } else if (mEquipmentCondition == EQUIPMENT_CONDITION_GOOD) {
            if (getView() != null)
                getView().removeAbnormalityStep();
        }
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

    public void logOut() {
        mAppDataManager.logOut();
        if (getView() != null)
            getView().onLogoutSuccess();
    }
}
