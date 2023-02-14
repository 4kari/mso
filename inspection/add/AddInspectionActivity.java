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
    private AbnormalityFragment mAbnormalityFragment2;
    private AbnormalityFragment mAbnormalityFragment3;
    private AbnormalityFragment mAbnormalityFragment4;
    private AbnormalityFragment mAbnormalityFragment5;
    private AbnormalityFragment mAbnormalityFragment6;
    private AbnormalityFragment mAbnormalityFragment7;
    private AbnormalityFragment mAbnormalityFragment8;
    private AbnormalityFragment mAbnormalityFragment9;

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
            listfragABS.addAll(Arrays.asList(mAbnormalityFragment,mAbnormalityFragment2,mAbnormalityFragment3,
                    mAbnormalityFragment4,mAbnormalityFragment5,mAbnormalityFragment6,mAbnormalityFragment7,
                    mAbnormalityFragment8,mAbnormalityFragment9));
//        stepperLayout.setOffscreenPageLimit(4);
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
        Log.d("onCondi","jalan");
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
    @Override
    public void setupAbnormalityStep2(AbnormalityParams abnormalityParams, int[] index) {
////        if (mAbnormalityFragment == null)
////            mAbnormalityFragment = AbnormalityFragment.newInstance(abnormalityParams, mEquipmentInfo);
//
////        menghapus tampilan abnormalitas yang sudah ada 29 dec 2022
//
//        int fid=0;
//        while (mAdapter.getCount()>2){
////            mAdapter.removeStep(listfragABS.get(fid));
//            try{
////                stepperLayout.removeViewAt(2);
////                Log.d("delete: ",mAdapter.findStep(2).toString());
//                mAdapter.removeStep(mAdapter.findStep(2));
//                mAdapter.notifyDataSetChanged();
//                stepperLayout.setCurrentStepPosition(stepperLayout.getCurrentStepPosition());
//            }catch (Exception e){
//                Log.d("Exception",e.getMessage());
//            }
//
//        }
//            Log.d("adaptercount", String.valueOf(mAdapter.getCount()));
////            for (int i=index.length-1;i>=0;i--){
////    //            addlistfrag(abnormalityParams,listfragABS.get(i),i);
////                if(index[i]==0){
////                    Log.d("removeABS",String.valueOf(2+i));
////                    Log.d("removed ",mAdapter.findStep(2+i).toString());
////                    mAdapter.removeStep(mAdapter.findStep(2+i));
////                    afterRstep();
////                }
////            }
//
////            try {
////                Log.d("list_terhapus",listfragABS.get(i).toString());
////                hapusAbsF(listfragABS.get(i));
////            } catch (Exception e){
////                Log.d("error hapus abs", e.toString());
////            }
//            try {
////                Log.d("step",mAdapter.findStep(2).getClass().getName());
////                mAdapter.removeStep(mAdapter.findStep(2));
////                afterRstep();
//
////                if (i == 0) {
////                    mAdapter.removeStep(mAbnormalityFragment);
////                    afterRstep();
////                } else if (i == 1) {
////                    mAdapter.removeStep(mAbnormalityFragment2);
////                    afterRstep();
////                } else if (i == 2) {
////                    mAdapter.removeStep(mAbnormalityFragment3);
////                    afterRstep();
////                } else if (i == 3) {
////                    mAdapter.removeStep(mAbnormalityFragment4);
////                    afterRstep();
////                } else if (i == 4) {
////                    mAdapter.removeStep(mAbnormalityFragment5);
////                    afterRstep();
////                } else if (i == 5) {
////                    mAdapter.removeStep(mAbnormalityFragment6);
////                    afterRstep();
////                } else if (i == 6) {
////                    mAdapter.removeStep(mAbnormalityFragment7);
////                    afterRstep();
////                } else if (i == 7) {
////                    mAdapter.removeStep(mAbnormalityFragment8);
////                    afterRstep();
////                } else if (i == 8) {
////                    mAdapter.removeStep(mAbnormalityFragment9);
////                    afterRstep();
////                }
//            }catch (Exception e){
//                Log.d("error hapus abs", e.toString());
//            }
////        for (int i = 0; i < index.length ; i++) {
////            if(index[i]==1) {
////                if (i == 0) {
////
////                    mAbnormalityFragment = AbnormalityFragment.newInstance(abnormalityParams, mEquipmentInfo, i);
////                    mAdapter.addAdditionalStep(mAbnormalityFragment);
////                    mAdapter.notifyDataSetChanged();
////                    stepperLayout.setCurrentStepPosition(stepperLayout.getCurrentStepPosition());
////
////                    if(mInspectionFragment.setDataToModel() != null) {
////                        if(mInspectionFragment.setDataToModel().getPhotoPath() != null)
////                            mAbnormalityFragment.setupAbnormalityImage(null, mInspectionFragment.setDataToModel().getPhotoPath());
////
////                        if(mInspectionFragment.setDataToModel().getLocalPhotoPath() != null)
////                            mAbnormalityFragment.setupAbnormalityImage(mInspectionFragment.setDataToModel().getLocalPhotoPath(), null);
////                    }
////                }
////                else if (i == 1) {
////                    mAbnormalityFragment2 = AbnormalityFragment.newInstance(abnormalityParams, mEquipmentInfo, i);
////                    mAdapter.addAdditionalStep(mAbnormalityFragment2);
////                    mAdapter.notifyDataSetChanged();
////                    stepperLayout.setCurrentStepPosition(stepperLayout.getCurrentStepPosition());
////
////                    if(mInspectionFragment.setDataToModel() != null) {
////                        if(mInspectionFragment.setDataToModel().getPhotoPath() != null)
////                            mAbnormalityFragment2.setupAbnormalityImage(null, mInspectionFragment.setDataToModel().getPhotoPath());
////
////                        if(mInspectionFragment.setDataToModel().getLocalPhotoPath() != null)
////                            mAbnormalityFragment2.setupAbnormalityImage(mInspectionFragment.setDataToModel().getLocalPhotoPath(), null);
////                    }
////                }
////                else if (i == 2) {
////                    mAbnormalityFragment3 = AbnormalityFragment.newInstance(abnormalityParams, mEquipmentInfo, i);
////                    mAdapter.addAdditionalStep(mAbnormalityFragment3);
////                    mAdapter.notifyDataSetChanged();
////                    stepperLayout.setCurrentStepPosition(stepperLayout.getCurrentStepPosition());
////
////                    if(mInspectionFragment.setDataToModel() != null) {
////                        if(mInspectionFragment.setDataToModel().getPhotoPath() != null)
////                            mAbnormalityFragment3.setupAbnormalityImage(null, mInspectionFragment.setDataToModel().getPhotoPath());
////
////                        if(mInspectionFragment.setDataToModel().getLocalPhotoPath() != null)
////                            mAbnormalityFragment3.setupAbnormalityImage(mInspectionFragment.setDataToModel().getLocalPhotoPath(), null);
////                    }
////                }
////                else if (i == 3) {
////                    mAbnormalityFragment4 = AbnormalityFragment.newInstance(abnormalityParams, mEquipmentInfo, i);
////                    mAdapter.addAdditionalStep(mAbnormalityFragment4);
////                    mAdapter.notifyDataSetChanged();
////                    stepperLayout.setCurrentStepPosition(stepperLayout.getCurrentStepPosition());
////
////                    if(mInspectionFragment.setDataToModel() != null) {
////                        if(mInspectionFragment.setDataToModel().getPhotoPath() != null)
////                            mAbnormalityFragment4.setupAbnormalityImage(null, mInspectionFragment.setDataToModel().getPhotoPath());
////
////                        if(mInspectionFragment.setDataToModel().getLocalPhotoPath() != null)
////                            mAbnormalityFragment4.setupAbnormalityImage(mInspectionFragment.setDataToModel().getLocalPhotoPath(), null);
////                    }
////                }
////                else if (i == 4) {
////                    mAbnormalityFragment5 = AbnormalityFragment.newInstance(abnormalityParams, mEquipmentInfo, i);
////                    mAdapter.addAdditionalStep(mAbnormalityFragment5);
////                    mAdapter.notifyDataSetChanged();
////                    stepperLayout.setCurrentStepPosition(stepperLayout.getCurrentStepPosition());
////
////                    if(mInspectionFragment.setDataToModel() != null) {
////                        if(mInspectionFragment.setDataToModel().getPhotoPath() != null)
////                            mAbnormalityFragment5.setupAbnormalityImage(null, mInspectionFragment.setDataToModel().getPhotoPath());
////
////                        if(mInspectionFragment.setDataToModel().getLocalPhotoPath() != null)
////                            mAbnormalityFragment5.setupAbnormalityImage(mInspectionFragment.setDataToModel().getLocalPhotoPath(), null);
////                    }
////                }
////                else if (i == 5) {
////                    mAbnormalityFragment6 = AbnormalityFragment.newInstance(abnormalityParams, mEquipmentInfo, i);
////                    mAdapter.addAdditionalStep(mAbnormalityFragment6);
////                    mAdapter.notifyDataSetChanged();
////                    stepperLayout.setCurrentStepPosition(stepperLayout.getCurrentStepPosition());
////
////                    if(mInspectionFragment.setDataToModel() != null) {
////                        if(mInspectionFragment.setDataToModel().getPhotoPath() != null)
////                            mAbnormalityFragment6.setupAbnormalityImage(null, mInspectionFragment.setDataToModel().getPhotoPath());
////
////                        if(mInspectionFragment.setDataToModel().getLocalPhotoPath() != null)
////                            mAbnormalityFragment6.setupAbnormalityImage(mInspectionFragment.setDataToModel().getLocalPhotoPath(), null);
////                    }
////                }
////                else if (i == 6) {
////                    mAbnormalityFragment7 = AbnormalityFragment.newInstance(abnormalityParams, mEquipmentInfo, i);
////                    mAdapter.addAdditionalStep(mAbnormalityFragment7);
////                    mAdapter.notifyDataSetChanged();
////                    stepperLayout.setCurrentStepPosition(stepperLayout.getCurrentStepPosition());
////
////                    if(mInspectionFragment.setDataToModel() != null) {
////                        if(mInspectionFragment.setDataToModel().getPhotoPath() != null)
////                            mAbnormalityFragment7.setupAbnormalityImage(null, mInspectionFragment.setDataToModel().getPhotoPath());
////
////                        if(mInspectionFragment.setDataToModel().getLocalPhotoPath() != null)
////                            mAbnormalityFragment7.setupAbnormalityImage(mInspectionFragment.setDataToModel().getLocalPhotoPath(), null);
////                    }
////                }
////                else if (i == 7) {
////                    mAbnormalityFragment8 = AbnormalityFragment.newInstance(abnormalityParams, mEquipmentInfo, i);
////                    mAdapter.addAdditionalStep(mAbnormalityFragment8);
////                    mAdapter.notifyDataSetChanged();
////                    stepperLayout.setCurrentStepPosition(stepperLayout.getCurrentStepPosition());
////
////                    if(mInspectionFragment.setDataToModel() != null) {
////                        if(mInspectionFragment.setDataToModel().getPhotoPath() != null)
////                            mAbnormalityFragment8.setupAbnormalityImage(null, mInspectionFragment.setDataToModel().getPhotoPath());
////
////                        if(mInspectionFragment.setDataToModel().getLocalPhotoPath() != null)
////                            mAbnormalityFragment8.setupAbnormalityImage(mInspectionFragment.setDataToModel().getLocalPhotoPath(), null);
////                    }
////                }
////                else if (i == 8) {
////                    mAbnormalityFragment9 = AbnormalityFragment.newInstance(abnormalityParams, mEquipmentInfo, i);
////                    mAdapter.addAdditionalStep(mAbnormalityFragment9);
////                    mAdapter.notifyDataSetChanged();
////                    stepperLayout.setCurrentStepPosition(stepperLayout.getCurrentStepPosition());
////
////                    if(mInspectionFragment.setDataToModel() != null) {
////                        if(mInspectionFragment.setDataToModel().getPhotoPath() != null)
////                            mAbnormalityFragment9.setupAbnormalityImage(null, mInspectionFragment.setDataToModel().getPhotoPath());
////
////                        if(mInspectionFragment.setDataToModel().getLocalPhotoPath() != null)
////                            mAbnormalityFragment9.setupAbnormalityImage(mInspectionFragment.setDataToModel().getLocalPhotoPath(), null);
////                    }
////                }
////            }
////                if (mAdapter.getCount() < 11 && mBearingType == TYPE_MEKANIKAL){
////                    listfragABS.set(i,AbnormalityFragment.newInstance(abnormalityParams, mEquipmentInfo, i));
////                    addAbnormalityFragment2(listfragABS.get(i));// perlu ganti
//////                    listfragABS.add(mAbnormalityFragment);
////                    Log.d("listfragABS", String.valueOf(listfragABS.size()));
////                }
////        }
////        mEquipmentCondition = AddInspectionPresenter.EQUIPMENT_CONDITION_BAD;
    }
    private void afterRstep(){
        mAdapter.notifyDataSetChanged();
        stepperLayout.setCurrentStepPosition(stepperLayout.getCurrentStepPosition());
    }
    private void addlistfrag(AbnormalityParams abnormalityParams, AbnormalityFragment mABS, int i){
        Log.d("created index", String.valueOf(i));
        mABS = AbnormalityFragment.newInstance(abnormalityParams, mEquipmentInfo, i);
        addAbnormalityFragment2(mABS);
        listfragABS.set(i, mABS);
    }
    private void addAbnormalityFragment2(AbnormalityFragment mAbs) {
        mAdapter.addAdditionalStep(mAbs);
        mAdapter.notifyDataSetChanged();
        stepperLayout.setCurrentStepPosition(stepperLayout.getCurrentStepPosition());

        if(mInspectionFragment.setDataToModel() != null) {
            if(mInspectionFragment.setDataToModel().getPhotoPath() != null)
                mAbs.setupAbnormalityImage(null, mInspectionFragment.setDataToModel().getPhotoPath());

            if(mInspectionFragment.setDataToModel().getLocalPhotoPath() != null)
                mAbs.setupAbnormalityImage(mInspectionFragment.setDataToModel().getLocalPhotoPath(), null);
        }
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
