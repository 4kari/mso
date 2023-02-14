package id.sisi.si.mso.ui.inspection.add;

import android.app.Activity;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.stepstone.stepper.StepperLayout;
import com.stepstone.stepper.VerificationError;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import id.sisi.si.mso.R;
import id.sisi.si.mso.data.model.Abnormal;
import id.sisi.si.mso.data.model.AbnormalityParams;
import id.sisi.si.mso.data.model.EquipmentInfo;
import id.sisi.si.mso.data.model.InspectionActivity;
import id.sisi.si.mso.data.model.InspectionDetail;
import id.sisi.si.mso.data.model.TechnicalModel;
import id.sisi.si.mso.data.model.picItem;
import id.sisi.si.mso.ui.base.BaseActivity;
import id.sisi.si.mso.ui.inspection.add.adapter.AddInspectionAdapter;
import id.sisi.si.mso.utils.DialogUtil;
import id.sisi.si.mso.utils.LoggingHelper;
import timber.log.Timber;

/**
 * Created by durrrr on 04-Oct-17.
 * Email: cab.purnama@gmail.com
 */
public class AddInspectionActivity extends BaseActivity
        implements Contract.AddInspectionView, StepperLayout.StepperListener,
        InspectionFragment.ActivityCallback, Contract.ActivityCallback, Contract.getMemberCallback{

    private final String EQUIPMENT_CONDITION_KEY = "equipment_condition_key";
    public static final String BEARING_TYPE = "bearing_type";
    public static final int TYPE_MOTOR = 0;
    public static final int TYPE_MCC = 1;
    public static final int TYPE_MEKANIKAL = 4;
    public static final int TYPE_SERVICE_MOTOR = 2;
    public static final int NON_BEARING = 3;
    public static ArrayList<EditText> conditionList = new ArrayList<EditText>();

    private List<AbnormalityFragment> listfragABS = new ArrayList<AbnormalityFragment>();
    @BindView(R.id.stepperLayout)
    StepperLayout stepperLayout;
    @BindView(R.id.toolbar)
    Toolbar toolbar;

    private AddInspectionPresenter mPresenter;

    private AddInspectionAdapter mAdapter;

    private EquipmentInfo mEquipmentInfo;

    private InspectionFragment mInspectionFragment;
    private TechnicalFragment mTechnicalFragment;
    private TechnicalFragment mTechnicalFragmentAfter;
    private TechnicalFragment mTechnicalFragmentServiceL, mTechnicalFragmentServiceM;
    private AbnormalityFragment mAbnormalityFragment;
    private AbnormalityFragmentM mAbnormalityFragment2;

    TechnicalModel technicalModels = null;

    private int mEquipmentCondition = AddInspectionPresenter.EQUIPMENT_CONDITION_GOOD;
    private int mBearingType;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_inspection);
        setUnbinder(ButterKnife.bind(this));

        toolbar.setTitle("Add Inspection");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (getIntent().getParcelableExtra(EquipmentInfo.TYPE_EXTRA) != null) {
            mEquipmentInfo = getIntent().getParcelableExtra(EquipmentInfo.TYPE_EXTRA);

            List<Fragment> fragments = getSupportFragmentManager().getFragments();
            for (Fragment fragment : fragments) {
                if (fragment instanceof InspectionFragment) {
                    mInspectionFragment = (InspectionFragment) fragment;
                }

                if (fragment instanceof TechnicalFragment) {
                    mTechnicalFragment = (TechnicalFragment) fragment;
                }

                if (fragment instanceof AbnormalityFragment)
                    mAbnormalityFragment = (AbnormalityFragment) fragment;
            }

            if(savedInstanceState != null) {
                if(savedInstanceState.containsKey(EQUIPMENT_CONDITION_KEY))
                    mEquipmentCondition = savedInstanceState.getInt(EQUIPMENT_CONDITION_KEY);
            }

            mBearingType = getIntent().getIntExtra(BEARING_TYPE, NON_BEARING);
            mPresenter = new AddInspectionPresenter(mEquipmentInfo,
                    getIntent().getIntExtra(AddInspectionPresenter.TRANSACTION_MODE_KEY, AddInspectionPresenter.ONLINE_MODE));
            mPresenter.attachView(this);
        }
    }

        @Override
    public void initView(ArrayList<InspectionActivity> activities) {
        stepperLayout.setOffscreenPageLimit(10);

        mAdapter = new AddInspectionAdapter(getSupportFragmentManager(), this);

        Timber.d(LoggingHelper.getInstance().getJsonString(activities));
        if (mInspectionFragment == null) {
            mInspectionFragment = InspectionFragment.newInstance(mEquipmentInfo, activities);
        }
        mAdapter.addAdditionalStep(mInspectionFragment);

        if (mEquipmentInfo.getType() != null) {
            switch (mEquipmentInfo.getType().getId()) {
                case EquipmentInfo.TYPE_BEARING:
                    Log.d("mBearingType", String.valueOf(mBearingType));
                    if(mBearingType == TYPE_MOTOR) {
                        if (mTechnicalFragment == null) {
                            mTechnicalFragment = BearingFragment.newInstance();
                            // added by rama 08 nov 2022
                            Bundle inputan = new Bundle();
                            inputan.putString("nomenclature",mEquipmentInfo.getName());
                            mTechnicalFragment.setArguments(inputan);
                            // end of added by rama
                        }
                        if (mTechnicalFragmentAfter == null) {
                            mTechnicalFragmentAfter = BearingFragmentAfter.newInstance();
                            // added by rama 08 nov 2022
                            Bundle inputan = new Bundle();
                            inputan.putString("nomenclature",mEquipmentInfo.getName());
                            mTechnicalFragmentAfter.setArguments(inputan);
                            // end of added by rama
                        }
                    }
                    else if(mBearingType == TYPE_MCC) {
                        if (mTechnicalFragment == null)
                            mTechnicalFragment = BearingMccFragment.newInstance(mEquipmentInfo);
                    }else if(mBearingType == TYPE_MEKANIKAL) {
                        if (mTechnicalFragment == null)
                            mTechnicalFragment = BearingMekanikalFragment.newInstance(mEquipmentInfo);
                    }
                    else {
                        if (mTechnicalFragment == null) {
                            mTechnicalFragment = BearingServiceFragmentK.newInstance();
                            // added by rama 08 nov 2022
                            Bundle inputan = new Bundle();
                            inputan.putString("nomenclature", mEquipmentInfo.getName());
                            mTechnicalFragment.setArguments(inputan);
                            // end of added by rama
                        }
                        if (mTechnicalFragmentServiceL == null) {
                            mTechnicalFragmentServiceL = BearingServiceFragmentL.newInstance();
                            // added by rama 08 nov 2022
                            Bundle inputan = new Bundle();
                            inputan.putString("nomenclature", mEquipmentInfo.getName());
                            mTechnicalFragmentServiceL.setArguments(inputan);
                            // end of added by rama
                        }
                        if (mTechnicalFragmentServiceM == null) {
                            mTechnicalFragmentServiceM = BearingServiceFragmentM.newInstance();
                            // added by rama 08 nov 2022
                            Bundle inputan = new Bundle();
                            inputan.putString("nomenclature", mEquipmentInfo.getName());
                            mTechnicalFragmentServiceM.setArguments(inputan);
                            // end of added by rama
                        }
                    }
                    mAdapter.addAdditionalStep(mTechnicalFragment);

                    if(mBearingType == TYPE_MOTOR)
                        mAdapter.addAdditionalStep(mTechnicalFragmentAfter);
                    if(mBearingType == TYPE_SERVICE_MOTOR){
                        mAdapter.addAdditionalStep(mTechnicalFragmentServiceL);
                        mAdapter.addAdditionalStep(mTechnicalFragmentServiceM);
                    }
                    break;
                case EquipmentInfo.TYPE_BELT_WEIGHTER:
                    if (mTechnicalFragment == null) {
                        mTechnicalFragment = BeltWeighterFragment.newInstance();
                        // added by rama 08 nov 2022
                        Bundle inputan = new Bundle();
                        inputan.putString("nomenclature", mEquipmentInfo.getName());
                        mTechnicalFragment.setArguments(inputan);
                        // end of added by rama

                    }
                    mAdapter.addAdditionalStep(mTechnicalFragment);
                    break;
                case EquipmentInfo.TYPE_TRANSFORMER:
                    if (mTechnicalFragment == null) {
                        mTechnicalFragment = TransformerFragment.newInstance();
                        // added by rama 08 nov 2022
                        Bundle inputan = new Bundle();
                        inputan.putString("nomenclature", mEquipmentInfo.getName());
                        mTechnicalFragment.setArguments(inputan);
                        // end of added by rama
                    }
                    mAdapter.addAdditionalStep(mTechnicalFragment);
                    break;
                default:
                    mTechnicalFragment = null;
                    break;
            }
        } else {
            if (mTechnicalFragment == null)
                mTechnicalFragment = EmptyFragment.newInstance();
            mAdapter.addAdditionalStep(mTechnicalFragment);
        }

        if(mEquipmentCondition == AddInspectionPresenter.EQUIPMENT_CONDITION_BAD & mAbnormalityFragment != null)
            mAdapter.addAdditionalStep(mAbnormalityFragment);

        stepperLayout.setAdapter(mAdapter);
        stepperLayout.setListener(this);
    }

    @Override
    public void onConditionChanged(int condition) {
        mPresenter.setEquipmentCondition(condition);
    }
    @Override
    public void onConditionChanged2(int condition, int[] index) {
        mPresenter.setEquipmentCondition2(condition,index);
    }

    @Override
    public void onSetCondition(ArrayList<EditText> condPackage, int status) {
        Timber.d("isi : " + condPackage);
        conditionList = condPackage;
        switch (status) {
            case TYPE_MOTOR:
                if (mTechnicalFragmentAfter != null) {
                    if (!conditionList.isEmpty())
                        mTechnicalFragmentAfter.setConditions(1);
                    else
                        mTechnicalFragmentAfter.setConditions(0);
                }
                break;
            case TYPE_MCC:
                if (mTechnicalFragment != null) {
                    if (!conditionList.isEmpty())
                        mTechnicalFragment.setConditions(1);
                    else
                        mTechnicalFragment.setConditions(0);
                }
                break;
            case TYPE_MEKANIKAL:
                if (mTechnicalFragment != null) {
                    if (!conditionList.isEmpty())
                        mTechnicalFragment.setConditions(1);
                    else
                        mTechnicalFragment.setConditions(0);
                }
                break;
            case TYPE_SERVICE_MOTOR:
                if (mTechnicalFragmentServiceM != null) {
                    if (!conditionList.isEmpty())
                        mTechnicalFragmentServiceM.setConditions(1);
                    else
                        mTechnicalFragmentServiceM.setConditions(0);
                }
                break;
        }

    }
    @Override
    public void setupAbnormalityStep(AbnormalityParams abnormalityParams) {
        Log.d( "SAS normal ","jalan");
        Log.d("abnormalityp",abnormalityParams.toString());
        Log.d("dpsection",abnormalityParams.getDefaultPsection());
        if (mAbnormalityFragment == null)
            mAbnormalityFragment = AbnormalityFragment.newInstance(abnormalityParams, mEquipmentInfo);
        Log.d( "defaukt: ", "yang ini jalan");
        if (mAdapter.getCount() < 11 && mBearingType == TYPE_MEKANIKAL){
            mAbnormalityFragment = AbnormalityFragment.newInstance(abnormalityParams, mEquipmentInfo);
            addAbnormalityFragment();
        }
        else if (mAdapter.getCount() < 4 && mBearingType == TYPE_MOTOR)
            addAbnormalityFragment();
        else if (mAdapter.getCount() < 3 && mBearingType != TYPE_MOTOR)
            addAbnormalityFragment();
        else if (mAdapter.getCount() < 5 && mBearingType == TYPE_SERVICE_MOTOR)
            addAbnormalityFragment();

        mEquipmentCondition = AddInspectionPresenter.EQUIPMENT_CONDITION_BAD;
    }
    public void addAbnormality2(AbnormalityParams abnormalityParams,int i){
        mAbnormalityFragment2 = AbnormalityFragmentM.newInstance(abnormalityParams, mEquipmentInfo, i);
        mAdapter.addAdditionalStep(mAbnormalityFragment2);
        mAdapter.notifyDataSetChanged();
        stepperLayout.setCurrentStepPosition(stepperLayout.getCurrentStepPosition());

        if(mInspectionFragment.setDataToModel() != null) {
            if(mInspectionFragment.setDataToModel().getPhotoPath() != null)
                mAbnormalityFragment2.setupAbnormalityImage(null, mInspectionFragment.setDataToModel().getPhotoPath());

            if(mInspectionFragment.setDataToModel().getLocalPhotoPath() != null)
                mAbnormalityFragment2.setupAbnormalityImage(mInspectionFragment.setDataToModel().getLocalPhotoPath(), null);
        }
    }

    @Override
    public void addAbis(Abnormal data){
//        data = mAbnormalityFragment.setDataToModel();
        Log.d("addAbis: ","jalan");
        mPresenter.insertabis(data);
        checkAbis();//untuk cek data sekarang
    }
    public void checkAbis(){
        mPresenter.checkAbis();
    }
    @Override
    public void setupAbnormalityStep2(AbnormalityParams abnormalityParams, int[] index) {
        Log.d( "setupAbnormalityStep2: ",Arrays.toString(index));
        for (int i = 0; i < index.length; i++) {
            if(index[i]==1){
                addAbnormality2(abnormalityParams,i);
            }
        }
        mAdapter.checkstep();
    }
    private void addAbnormalityFragment() {
        mAdapter.addAdditionalStep(mAbnormalityFragment);
        mAdapter.notifyDataSetChanged();
        stepperLayout.setCurrentStepPosition(stepperLayout.getCurrentStepPosition());

        if(mInspectionFragment.setDataToModel() != null) {
            if(mInspectionFragment.setDataToModel().getPhotoPath() != null)
                mAbnormalityFragment.setupAbnormalityImage(null, mInspectionFragment.setDataToModel().getPhotoPath());

            if(mInspectionFragment.setDataToModel().getLocalPhotoPath() != null)
                mAbnormalityFragment.setupAbnormalityImage(mInspectionFragment.setDataToModel().getLocalPhotoPath(), null);
        }
    }

    @Override
    public void removeAbnormalityStep() {
        if (mAdapter.getCount() == 3 && mBearingType != TYPE_MOTOR)
            removeAbnormality();
        else if (mAdapter.getCount() == 4 && mBearingType == TYPE_MOTOR)
           removeAbnormality();
        else if (mAdapter.getCount() == 5 && mBearingType == TYPE_SERVICE_MOTOR)
            removeAbnormality();
        else if (mAdapter.getCount() > 2 && mBearingType == TYPE_MEKANIKAL)
            removeAbnormality();

        mEquipmentCondition = AddInspectionPresenter.EQUIPMENT_CONDITION_GOOD;
    }

    private void removeAbnormality() {
        mAdapter.removeStep(mAbnormalityFragment);
        mAdapter.notifyDataSetChanged();
        stepperLayout.setCurrentStepPosition(stepperLayout.getCurrentStepPosition());
    }

    @Override
    public void attemptUploadImage(File imageFile) {
        mPresenter.attemptUploadImage(imageFile);
    }

    @Override
    public void uploadImageResult(int result, @Nullable String imagePath, @Nullable String imageUrl) {
        mInspectionFragment.uploadImageResult(result, imagePath, imageUrl);
        Timber.d("imageUrl" + imageUrl);
        if (mAbnormalityFragment != null)
            mAbnormalityFragment.setupAbnormalityImage(imagePath, imageUrl);
        if (mAbnormalityFragment2 != null)
            mAbnormalityFragment2.setupAbnormalityImage(imagePath, imageUrl);
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        outState.putInt(EQUIPMENT_CONDITION_KEY, mEquipmentCondition);
    }

    @Override
    public void onCompleted(View completeButton) {
        conditionList.clear();

        InspectionDetail inspectionDetail = mInspectionFragment.setDataToModel();
        TechnicalModel technicalModel = null;
        TechnicalModel technicalModel2 = null;
        TechnicalModel technicalModel3 = null;
        InspectionDetail condition = null;
        Abnormal abnormal = null;

        if (mEquipmentInfo.getType() != null) {
            technicalModel = mTechnicalFragment.setDataToModel();
            if(mEquipmentInfo.getType().getName().equals("BEARING")) {
                if(mBearingType == TYPE_MOTOR) {
                    technicalModel2 = mTechnicalFragmentAfter.setDataToModel();
                    condition = mTechnicalFragmentAfter.setCondition();
                }
                else if(mBearingType == TYPE_SERVICE_MOTOR){
                    technicalModel2 = mTechnicalFragmentServiceL.setDataToModel();
                    technicalModel3 = mTechnicalFragmentServiceM.setDataToModel();
                    condition = mTechnicalFragmentServiceM.setCondition();
                }
                else
                    condition = mTechnicalFragment.setCondition();
            }
            else
                condition = mTechnicalFragment.setCondition();
        }
        if (mEquipmentCondition == AddInspectionPresenter.EQUIPMENT_CONDITION_BAD)
            abnormal = mAbnormalityFragment.setDataToModel();

        mPresenter.attemptSave(inspectionDetail, technicalModel, abnormal,
                    technicalModel2, technicalModel3, condition, mBearingType);
    }

    @Override
    public void onError(VerificationError verificationError) {

    }

    @Override
    public void onStepSelected(int newStepPosition) {

    }

    @Override
    public void onReturn() {

    }

    @Override
    public void showProgress(String title, String message) {
        DialogUtil.showProgressDialog(this, title, message, false);
    }

    @Override
    public void hideProgress() {
        DialogUtil.dismiss();
    }

    @Override
    public void onSaveSuccess() {
        setResult(Activity.RESULT_OK);
        DialogUtil.showEndDialog(this, "Sukses", "Data berhasil disimpan");
    }

    @Override
    public void onSaveFailed() {
        DialogUtil.showBasicDialog(this, "Gagal", "Terjadi kesalahan ketika menyimpan data");
    }

    @Override
    public void onOfflineSave() {
        MaterialDialog dialog = new MaterialDialog.Builder(AddInspectionActivity.this)
                .title("Simpan Data")
                .content("Koneksi jaringan tidak terdeteksi. Data Inspeksi akan disimpan secara offline terlebih dahulu.")
                .positiveText("Ya")
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        InspectionDetail inspectionDetail = mInspectionFragment.setDataToModel();
                        TechnicalModel technicalModel = null;
                        TechnicalModel technicalModel2 = null;
                        TechnicalModel technicalModel3 = null;
                        Abnormal abnormal = null;
                        InspectionDetail condition = null;

                        if (mEquipmentInfo.getType() != null) {
                            technicalModel = mTechnicalFragment.setDataToModel();
                            if(mEquipmentInfo.getType().getId().equalsIgnoreCase(EquipmentInfo.TYPE_BEARING)) {
                                if(mBearingType == TYPE_MOTOR) {
                                    technicalModel2 = mTechnicalFragmentAfter.setDataToModel();
                                    condition = mTechnicalFragmentAfter.setCondition();
                                }
                                else if(mBearingType == TYPE_SERVICE_MOTOR){
                                    technicalModel2 = mTechnicalFragmentServiceL.setDataToModel();
                                    technicalModel3 = mTechnicalFragmentServiceM.setDataToModel();
                                    condition = mTechnicalFragmentServiceM.setCondition();
                                }
                                else
                                    condition = mTechnicalFragment.setCondition();
                            }
                            else
                                condition = mTechnicalFragment.setCondition();
                        }
                        if (mEquipmentCondition == AddInspectionPresenter.EQUIPMENT_CONDITION_BAD)
                            abnormal = mAbnormalityFragment.setDataToModel();

                        Timber.d("condition : " + condition);
                        mPresenter.attemptLocalSave(inspectionDetail, technicalModel, abnormal, technicalModel2, technicalModel3, condition, mBearingType);
                    }
                })
                .negativeText("Batal")
                .build();
        dialog.show();
    }

    @Override
    public void onInitFailed() {
        DialogUtil.showBasicDialog(this, "Gagal", "Inisialisasi gagal");
    }

    @Override
    public void onSaveMember(List<picItem> items, String tag) {
        onShowMember(items, tag);
    }

    @Override
    public void onOfflineSaveSuccess() {
        setResult(Activity.RESULT_OK);
        DialogUtil.showEndDialog(this, "Sukses", "Data Inspeksi berhasil disimpan offline");
    }

    @Override
    public void onOfflineSaveFailed() {
        DialogUtil.showBasicDialog(this, "Gagal", "Terjadi kesalahan ketika menyimpan data Inspeksi offline");
    }

    @Override
    protected void onDestroy() {
        mPresenter.detachView();
        super.onDestroy();
    }

    @Override
    public void onSearchItem(String query, String tag) {
        mPresenter.getAutocompleteMember(query, tag);
    }

    @Override
    public void onShowMember(List<picItem> picItems, String tag) {
        //update into different fragment
        switch (tag){
            case "K":
                mTechnicalFragment.updateMember(picItems);
                break;
            case "L":
                mTechnicalFragmentServiceL.updateMember(picItems);
                break;
            case "M":
                mTechnicalFragmentServiceM.updateMember(picItems);
                break;
        }

    }


}
