package id.sisi.si.mso.ui.inspection.add;

import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.stepstone.stepper.VerificationError;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import id.sisi.si.mso.R;
import id.sisi.si.mso.data.DataHelper;
import id.sisi.si.mso.data.api.MsoService;
import id.sisi.si.mso.data.api.ServiceGenerator;
import id.sisi.si.mso.data.model.Bearing;
import id.sisi.si.mso.data.model.EquipmentInfo;
import id.sisi.si.mso.data.model.GetPrevInspection;
import id.sisi.si.mso.data.model.GetPrevInspectionResponse;
import id.sisi.si.mso.data.model.InspectionDetail;
import id.sisi.si.mso.data.model.picItem;
import id.sisi.si.mso.utils.TextUtils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Luhur on 1/10/2019.
 */

public class BearingMccFragment extends TechnicalFragment {
    //added by rama 08 nov 2022 untuk prev data inspeksi
    private Call<GetPrevInspectionResponse> mPrevIns;
    @BindView(R.id.txtKW) TextView txtKW;
    @BindView(R.id.txtfLa) TextView txtfLa;
    @BindView(R.id.txtAmpSet) TextView txtAmpSet;
    @BindView(R.id.txtAmpR) TextView txtAmpR;
    @BindView(R.id.txtAmpS) TextView txtAmpS;
    @BindView(R.id.txtAmpT) TextView txtAmpT;
    @BindView(R.id.txtTempR) TextView txtTempR;
    @BindView(R.id.txtTempS) TextView txtTempS;
    @BindView(R.id.txtTempT) TextView txtTempT;
    // end of added by rama

    final List<String> conditionOptions = Arrays.asList("GOOD", "BAD");
    @BindView(R.id.kW)
    EditText kW;
    @BindView(R.id.fLA)
    EditText fla;
    @BindView(R.id.ampSetting)
    EditText ampereSetting;
    @BindView(R.id.ampRact)
    EditText ampereR;
    @BindView(R.id.ampSact)
    EditText ampereS;
    @BindView(R.id.ampTact)
    EditText ampereT;
    @BindView(R.id.tempR)
    EditText tempR;
    @BindView(R.id.tempS)
    EditText tempS;
    @BindView(R.id.tempT)
    EditText tempT;
    @BindView(R.id.keterangan)
    EditText keterangan;
    @BindView(R.id.spCondition)
    Spinner spCondition;

    private final String EQUIPMENTINFO_MODEL_KEY = "equipmentinfo_model_key";
    private Contract.ActivityCallback mActivityCallback;
    private double setting;
    private EquipmentInfo mEquipmentInfo;

    public static BearingMccFragment newInstance(EquipmentInfo equipmentInfo) {
        BearingMccFragment instance = new BearingMccFragment();
        Bundle args = new Bundle();
        args.putParcelable(EquipmentInfo.TYPE_EXTRA, equipmentInfo);
        instance.setArguments(args);
        return instance;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bearing_mcc, container, false);
        setUnbinder(ButterKnife.bind(this, view));

        if (getArguments().getParcelable(EquipmentInfo.TYPE_EXTRA) != null) {
            mEquipmentInfo = getArguments().getParcelable(EquipmentInfo.TYPE_EXTRA);
            if (savedInstanceState != null) {
                if (savedInstanceState.containsKey(EQUIPMENTINFO_MODEL_KEY))
                    mEquipmentInfo = savedInstanceState.getParcelable(EQUIPMENTINFO_MODEL_KEY);
            }
        }

        mActivityCallback = (Contract.ActivityCallback) getActivity();

        initView();
//        added by rama 08 Nov 2022
        String nomenclature = mEquipmentInfo.getName();
        String subtipe = "2";
        String tipe = "1";
//        Log.d("nomenclature",nomenclature);Log.d("subtipe",subtipe);Log.d("tipe",tipe);
        setprevinspection(nomenclature,tipe, subtipe);
//        end of added by rama
        return view;
    }

    private void initView() {
        fla.setText(mEquipmentInfo.getDefault().getFla());
        kW.setText(mEquipmentInfo.getDefault().getKw());
        ampereSetting.setText(mEquipmentInfo.getDefault().getSetting());

        spCondition.setAdapter(DataHelper.createAdapter(getActivity(), conditionOptions));
        spCondition.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                mActivityCallback.onConditionChanged(i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        EditText[] bearingEt = { ampereR, ampereS, ampereT };
        for(EditText et : bearingEt)
            et.addTextChangedListener(new bearingWatcher(et));

    }

    private class bearingWatcher implements TextWatcher {
        private EditText mEditText;

        public bearingWatcher(EditText e) {
            mEditText = e;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            if(!TextUtils.isNullOrEmpty(s.toString().trim())){
                Double value = Double.parseDouble(s.toString());
                if(!TextUtils.isNullOrEmpty(ampereSetting.getText().toString())) {
                    setting = Double.parseDouble(ampereSetting.getText().toString());
                    if (value >= setting)
                        mEditText.getBackground().setColorFilter(getResources().getColor(R.color.material_red_400), PorterDuff.Mode.SRC_ATOP);
                    else if (value < setting)
                        mEditText.getBackground().setColorFilter(getResources().getColor(R.color.material_green_400), PorterDuff.Mode.SRC_ATOP);
                }
                else
                    mEditText.getBackground().setColorFilter(getResources().getColor(R.color.ms_white_54_opacity), PorterDuff.Mode.SRC_ATOP);
                checkCondition(value, mEditText);
            }
            else
            {
                checkCondition(0.0, mEditText);
                mEditText.getBackground().setColorFilter(getResources().getColor(R.color.ms_white_54_opacity), PorterDuff.Mode.SRC_ATOP);
            }
        }
    }

    private void checkCondition(Double value, EditText key) {
        ArrayList<EditText> condPackage = new ArrayList<>();
        ArrayList<EditText> tampPackage = new ArrayList<>();

        tampPackage = AddInspectionActivity.conditionList;

        if(tampPackage != null)
            condPackage = tampPackage;

        if(condPackage.contains(key)){
            if (value >= setting)
                condPackage.add(key);
            else
                condPackage.remove(key);
        }
        else {
            if (value >= setting)
                condPackage.add(key);
        }

        mActivityCallback.onSetCondition(condPackage, AddInspectionActivity.TYPE_MCC);
    }

    @Override
    Bearing setDataToModel() {
        Bearing bearing = new Bearing();

        bearing.setKeterangan(keterangan.getText().toString());
        if(!kW.getText().toString().isEmpty())
            bearing.setKw(Double.parseDouble(kW.getText().toString()));
        if(!fla.getText().toString().isEmpty())
            bearing.setFla(Double.parseDouble(fla.getText().toString()));
        if(!ampereSetting.getText().toString().isEmpty())
            bearing.setAmpereSetting(Double.parseDouble(ampereSetting.getText().toString()));
        if(!ampereR.getText().toString().isEmpty())
            bearing.setAmpereRact(Double.parseDouble(ampereR.getText().toString()));
        if(!ampereS.getText().toString().isEmpty())
            bearing.setAmpereSact(Double.parseDouble(ampereS.getText().toString()));
        if(!ampereT.getText().toString().isEmpty())
            bearing.setAmpereTact(Double.parseDouble(ampereT.getText().toString()));

        if(!tempR.getText().toString().isEmpty())
            bearing.setTemperatureR(Double.parseDouble(tempR.getText().toString()));
        if(!tempS.getText().toString().isEmpty())
            bearing.setTemperatureS(Double.parseDouble(tempS.getText().toString()));
        if(!tempT.getText().toString().isEmpty())
            bearing.setTemperatureT(Double.parseDouble(tempT.getText().toString()));

        return bearing;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mEquipmentInfo != null)
            outState.putParcelable(EQUIPMENTINFO_MODEL_KEY, mEquipmentInfo);
    }

    @Override
    InspectionDetail setCondition() {
        InspectionDetail inspectionCondition = new InspectionDetail();

        inspectionCondition.setCondition(isConditionBad() ? 0 : 1);
        return inspectionCondition;
    }

    public boolean isConditionBad() {
        return !spCondition.getSelectedItem().toString().equals(conditionOptions.get(0));
    }

    @Override
    void setConditions(int i) {
        int selectedIndex = spCondition.getSelectedItemPosition();

        if(selectedIndex==0 && i==1)
            spCondition.setSelection(i);
        else if(selectedIndex==1 && i==0)
            spCondition.setSelection(i);
    }

    @Override
    void updateMember(List<picItem> picItems) {

    }

    @Nullable
    @Override
    public VerificationError verifyStep() {
        return null;
    }

    @Override
    public void onSelected() {

    }

    @Override
    public void onError(@NonNull VerificationError error) {

    }
    public void setprevinspection(String nomenclature,String tipe, String subtipe){
        mPrevIns = ServiceGenerator.getInstance().getRetrofit().create(MsoService.class).getPrevInspection(nomenclature,tipe, subtipe);
        mPrevIns.enqueue(new Callback<GetPrevInspectionResponse>() {
            @Override
            public void onResponse(Call<GetPrevInspectionResponse> call, Response<GetPrevInspectionResponse> response) {
                if (response.isSuccessful()) {
                    if (response.body() != null && response.body().isSuccess()) {
//                        AutonomosSupervisor.setValue(response.body().getData());
//                        Log.d("GetPrevInspection2", response.body().getData().toString());
                        List<GetPrevInspection> data = response.body().getData();
                        String id, value;
                        for (int i=0;i<data.size();i++) {
                            id = data.get(i).getId();
                            value = " (prev : "+data.get(i).getValue()+")";
                            if(id.equals("49")) { txtKW.setText(txtKW.getText().toString() + value);
                            }else if(id.equals("50")){txtfLa.setText(txtfLa.getText().toString() + value);
                            }else if(id.equals("51")) {txtAmpSet.setText(txtAmpSet.getText().toString() + value);
                            }else if(id.equals("52")) {txtAmpR.setText(txtAmpR.getText().toString() + value);
                            }else if(id.equals("53")) {txtAmpS.setText(txtAmpS.getText().toString() + value);
                            }else if(id.equals("54")) {txtAmpT.setText(txtAmpT.getText().toString() + value);
                            }else if(id.equals("55")) {txtTempR.setText(txtTempR.getText().toString() + value);
                            }else if(id.equals("56")) {txtTempS.setText(txtTempS.getText().toString() + value);
                            }else if(id.equals("57")) {txtTempT.setText(txtTempT.getText().toString() + value);
                            }
                        }
                    }else if(!response.body().isSuccess()){
                        showToast("data sebelumnya tidak dapat diambil");
                        call.cancel();
                    }
                } else {
                    if (response.body() != null) {
                        Log.e("GetPrevInspection", "NULL" );
                    }
                }
            }

            @Override
            public void onFailure(Call<GetPrevInspectionResponse> call, Throwable t) {
                Log.d("GetPrevInspection ",t.toString());
            }
        });
    }
//        end of added by rama
}
