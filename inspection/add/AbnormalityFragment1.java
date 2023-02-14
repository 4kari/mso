package id.sisi.si.mso.ui.inspection.add;

import static id.sisi.si.mso.R.layout.list_item_spinner;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.MultiAutoCompleteTextView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.jakewharton.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;
import com.stepstone.stepper.Step;
import com.stepstone.stepper.VerificationError;

import java.io.File;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import id.sisi.si.mso.R;
import id.sisi.si.mso.data.api.MsoService;
import id.sisi.si.mso.data.api.ServiceGenerator;
import id.sisi.si.mso.data.model.Abnormal;
import id.sisi.si.mso.data.model.AbnormalityParams;
import id.sisi.si.mso.data.model.ActionItem;
import id.sisi.si.mso.data.model.ConditionItem;
import id.sisi.si.mso.data.model.EquipmentInfo;
import id.sisi.si.mso.data.model.PriorityItem;
import id.sisi.si.mso.data.model.SourceItem;
import id.sisi.si.mso.ui.base.BaseFragment;
import id.sisi.si.mso.utils.DialogUtil;
import timber.log.Timber;

/**
 * Created by durrrr on 11-Oct-17.
 * Email: cab.purnama@gmail.com
 */
public class AbnormalityFragment1 extends BaseFragment implements Step {

    private final String ABNORMAL_MODEL_KEY = "abnormal_model_key";
    private final String ABNORMALITY_PARAMS_KEY = "abnormality_params_model_key";
    private final String EQUIPMENTINFO_MODEL_KEY = "equipmentinfo_model_key";
    private final String CONDITION_KEY = "condition_key";
    public int identitas = 0;
    @BindView(R.id.spSource)
    Spinner spSource;
    @BindView(R.id.spPriority)
    Spinner spPriority;
    @BindView(R.id.spCondition)
    Spinner spCondition;
    @BindView(R.id.spAction)
    Spinner spAction;
    @BindView(R.id.etPlant)
    EditText etPlant;
    @BindView(R.id.matAbnormal)
    MultiAutoCompleteTextView matAbnormal;
    @BindView(R.id.matActivity)
    MultiAutoCompleteTextView matActivity;
    @BindView(R.id.etRemark)
    EditText etRemark;
    @BindView(R.id.pict_before)
    ImageButton ibPictBefore;
    /*@BindView(R.id.pict_after)
    ImageButton ibPictAfter;*/
    @BindView(R.id.progressbar_pict_before)
    ProgressBar pbPictBefore;
    /*@BindView(R.id.progressbar_pict_after)
    ProgressBar pbPictAfter;*/
    @BindView(R.id.btn_reupload_pict_before)
    Button btnReuploadPictBefore;
    /*@BindView(R.id.btn_reupload_pict_after)
    Button btnReuploadPictAfter;*/
    @BindView(R.id.tvPictAfter)
    TextView tvPictAfter;
    @BindView(R.id.rlPictAfter)
    RelativeLayout rlPictAfter;

    @BindView(R.id.abinstxt)
    TextView abinstxt;
    @BindView(R.id.btnsaveai)
    Button btnsaveai;
    @BindView(R.id.saveabis)
    Button saveabis;

    private Abnormal mAbnormalModel;
    private AbnormalityParams mAbnormalityParams;
    private EquipmentInfo mEquipmentInfo;
    private int mChoosedCondition = 0;

    public static AbnormalityFragment1 newInstance(AbnormalityParams abnormalityParams, EquipmentInfo equipmentInfo) {
        AbnormalityFragment1 instance = new AbnormalityFragment1();
        Bundle args = new Bundle();
        args.putParcelable(AbnormalityParams.TYPE_EXTRA, abnormalityParams);
        args.putParcelable(EquipmentInfo.TYPE_EXTRA, equipmentInfo);
        instance.setArguments(args);
        return instance;
    }
    //added by rama 29 dec 2022
    public static AbnormalityFragment1 newInstance(AbnormalityParams abnormalityParams, EquipmentInfo equipmentInfo, int index) {
        AbnormalityFragment1 instance = new AbnormalityFragment1();
//        Log.d("ABSnewInstance ", String.valueOf(index));
        Bundle args = new Bundle();
        args.putInt("index",index);
        args.putParcelable(AbnormalityParams.TYPE_EXTRA, abnormalityParams);
        args.putParcelable(EquipmentInfo.TYPE_EXTRA, equipmentInfo);
        instance.setArguments(args);
        return instance;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_abnormality_inspection, container, false);
        setUnbinder(ButterKnife.bind(this, view));
        Log.d( "AbParams ",AbnormalityParams.TYPE_EXTRA);
        Log.d( "EquipmentInfoEX ",EquipmentInfo.TYPE_EXTRA);

        if (getArguments().getParcelable(AbnormalityParams.TYPE_EXTRA) != null
                & getArguments().getParcelable(EquipmentInfo.TYPE_EXTRA) != null) {
            mAbnormalityParams = getArguments().getParcelable(AbnormalityParams.TYPE_EXTRA);
            mEquipmentInfo = getArguments().getParcelable(EquipmentInfo.TYPE_EXTRA);

            if (savedInstanceState != null) {
                if (savedInstanceState.containsKey(ABNORMAL_MODEL_KEY)) {
                    mAbnormalModel = savedInstanceState.getParcelable(ABNORMAL_MODEL_KEY);
                }

                if (savedInstanceState.containsKey(ABNORMALITY_PARAMS_KEY)) {
                    mAbnormalityParams = savedInstanceState.getParcelable(ABNORMALITY_PARAMS_KEY);
                }

                if (savedInstanceState.containsKey(EQUIPMENTINFO_MODEL_KEY)) {
                    mEquipmentInfo = savedInstanceState.getParcelable(EQUIPMENTINFO_MODEL_KEY);
                }

                if (savedInstanceState.containsKey(CONDITION_KEY))
                    mChoosedCondition = savedInstanceState.getInt(CONDITION_KEY);
            }
            if(getArguments().getInt("index") >= 0){
                int index = getArguments().getInt("index");
                String[] abs = {"Kb_fan","Vib_fan","KV_fan","Temp_ds_imp","Temp_nds_imp","Pulley","\n" +
                        "SafetyG","Houskeeping","UnsafeC"};
                identitas = index;
                abinstxt.setText(abs[index]);
                abinstxt.setVisibility(View.VISIBLE);
                Log.d("abinstxt", abinstxt.getText().toString());
            }else{
                abinstxt.setVisibility(View.INVISIBLE);
            }
            initView();
        }

        return view;
    }

    private void initView() {
        ArrayAdapter<String> abnormalityAdapter = new ArrayAdapter<>(getContext(),
                R.layout.list_item_spinner, mAbnormalityParams.getAutocompleteAbnormal());
        matAbnormal.setAdapter(abnormalityAdapter);
        matAbnormal.setThreshold(1);
        matAbnormal.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());

        ArrayAdapter<String> activityAdapter = new ArrayAdapter<>(getContext(),
                R.layout.list_item_spinner, mAbnormalityParams.getAutocompleteActivity());
        matActivity.setAdapter(activityAdapter);
        matActivity.setThreshold(1);
        matActivity.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());

        etPlant.setFocusable(false);
        etPlant.setText(mEquipmentInfo.getPlant().getPlant() + " - " +mEquipmentInfo.getPlant().getName());

        ArrayAdapter<SourceItem> sourceAdapter = new ArrayAdapter<>(getContext(), R.layout.list_item_spinner,
                android.R.id.text1, mAbnormalityParams.getSources());
        spSource.setAdapter(sourceAdapter);

        final ArrayAdapter<ConditionItem> conditionAdapter = new ArrayAdapter<>(getContext(), R.layout.list_item_spinner,
                android.R.id.text1, mAbnormalityParams.getConditions());
        spCondition.setAdapter(conditionAdapter);
        spCondition.setSelection(mChoosedCondition);

        ArrayAdapter<PriorityItem> priorityAdapter = new ArrayAdapter<>(getContext(), R.layout.list_item_spinner,
                android.R.id.text1, mAbnormalityParams.getPriorities());
        spPriority.setAdapter(priorityAdapter);

        spPriority.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                List<ConditionItem> conditionItems;
                if (spPriority.getSelectedItem().toString().equalsIgnoreCase("Emergency")) {
                    conditionItems = mAbnormalityParams.getEmergencyConditions();
                } else {
                    conditionItems = mAbnormalityParams.getConditions();
                }

                ArrayAdapter<ConditionItem> conditionAdapter = new ArrayAdapter<>(getContext(),
                        list_item_spinner, android.R.id.text1, conditionItems);
                spCondition.setAdapter(conditionAdapter);
                mChoosedCondition = i;
                spCondition.setSelection(mChoosedCondition);

                /*if (mActionType == ACTION_EDIT && ON_FIRST_BIND_CONDITION) {
                    spCondition.setSelection(conditionItems.indexOf(mAbnormal.getCondition()));
                    ON_FIRST_BIND_CONDITION = false;
                }*/
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        saveabis.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        ArrayAdapter<ActionItem> actionAdapter = new ArrayAdapter<>(getContext(), R.layout.list_item_spinner,
                android.R.id.text1, mAbnormalityParams.getActions());
        spAction.setAdapter(actionAdapter);
    }

    public void setupAbnormalityImage(@Nullable String imagePath, @Nullable String imageUrl) {
        if(mAbnormalModel == null) {
            mAbnormalModel = new Abnormal();
            mAbnormalModel.setSynced(false);
        }

        if (imageUrl != null) {
            mAbnormalModel.setPictBefore(imageUrl);
            String imageLink = new StringBuilder().append(MsoService.URL_PATH).append(mAbnormalModel.getPictBefore().replace("media/dtupload/", "getImage/")).toString();
            try {
                new Picasso.Builder(getContext()).downloader(new OkHttp3Downloader(ServiceGenerator.getInstance().getClient())).build().load(imageLink).into(ibPictBefore);
            }catch (Exception e){}
            Timber.d("imageLink" + imageLink);
            //Picasso.with(getContext()).load(MsoService.URL_PICT_PATH + mAbnormalModel.getPictBefore()).into(ibPictBefore);
        }

        if (imagePath != null) {
            mAbnormalModel.setPictBeforeLocalPath(imagePath);
            Picasso.with(getContext()).load(new File(mAbnormalModel.getPictBeforeLocalPath())).into(ibPictBefore);
        }
    }

    Abnormal setDataToModel() {
        /*mAbnormalModel.setAbnormalDate(textDate);
        mAbnormalModel.setReportedBy("");
        mAbnormalModel.setEquipmentId(mEquipmentInfo.getType().getId());*/

        if(mAbnormalModel == null) {
            mAbnormalModel = new Abnormal();
            mAbnormalModel.setSynced(false);
        }

        mAbnormalModel.setPlant(mEquipmentInfo.getPlant());
        mAbnormalModel.setSource((SourceItem) spSource.getSelectedItem());
        mAbnormalModel.setPriority((PriorityItem) spPriority.getSelectedItem());
        mAbnormalModel.setCondition((ConditionItem) spCondition.getSelectedItem());
        mAbnormalModel.setAction((ActionItem) spAction.getSelectedItem());
        mAbnormalModel.setAbnormal(matAbnormal.getText().toString());
        mAbnormalModel.setActivity(matActivity.getText().toString());
        mAbnormalModel.setRemark(etRemark.getText().toString());
//        mAbnormalModel.setPictBefore(pictBeforeValue);
        mAbnormalModel.setPictAfter("");

        return mAbnormalModel;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mAbnormalModel != null)
            outState.putParcelable(ABNORMAL_MODEL_KEY, mAbnormalModel);
        if (mAbnormalityParams != null)
            outState.putParcelable(ABNORMALITY_PARAMS_KEY, mAbnormalityParams);
        if (mEquipmentInfo != null)
            outState.putParcelable(EQUIPMENTINFO_MODEL_KEY, mEquipmentInfo);
    }

    @Nullable
    @Override
    public VerificationError verifyStep() {
        String errorMessage = null;

        /*if (TextUtils.isNullOrEmpty(matAbnormal.getText().toString())) {
            errorMessage = getString(R.string.alert_any_field_required);
            matAbnormal.setError(getString(R.string.error_field_abnormal));
        } else {
            matAbnormal.setError(null);
        }

        if (TextUtils.isNullOrEmpty(etRemark.getText().toString())) {
            errorMessage = getString(R.string.alert_any_field_required);
            etRemark.setError(getString(R.string.error_field_keterangan));
        } else {
            etRemark.setError(null);
        }

        if (TextUtils.isNullOrEmpty(matActivity.getText().toString())) {
            errorMessage = getString(R.string.alert_any_field_required);
            matActivity.setError(getString(R.string.error_field_activity));
        } else {
            matActivity.setError(null);
        }*/

        if (errorMessage == null)
            return null;
        else
            return new VerificationError(errorMessage);
    }

    @Override
    public void onSelected() {

    }

    @Override
    public void onError(@NonNull VerificationError error) {
        DialogUtil.showBasicDialog(getContext(), "Perhatian", error.getErrorMessage());
    }
}