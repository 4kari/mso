package id.sisi.si.mso.ui.inspection.add;

import android.content.Intent;
import android.text.InputFilter;
import android.text.Spanned;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.stepstone.stepper.VerificationError;

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
import id.sisi.si.mso.data.model.GetDefaultValue;
import id.sisi.si.mso.data.model.GetDefaultValueResponse;
import id.sisi.si.mso.data.model.GetPrevInspection;
import id.sisi.si.mso.data.model.InspectionDetail;
import id.sisi.si.mso.data.model.picItem;
import id.sisi.si.mso.ui.abnormality.addOrEdit.AbnormalityAddOrEditActivity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Luhur on 1/10/2019.
 */

public class BearingMekanikalFragment extends TechnicalFragment {
    //added by rama 08 nov 2022 untuk prev data inspeksi
    private Call<GetDefaultValueResponse> mdefval;
//    @BindView(R.id.Kb_fan) TextView Kb_fan;
//    @BindView(R.id.txtfLa) TextView txtfLa;
//    @BindView(R.id.txtAmpSet) TextView txtAmpSet;
//    @BindView(R.id.txtAmpR) TextView txtAmpR;
//    @BindView(R.id.txtAmpS) TextView txtAmpS;
//    @BindView(R.id.txtAmpT) TextView txtAmpT;
//    @BindView(R.id.txtTempR) TextView txtTempR;
//    @BindView(R.id.txtTempS) TextView txtTempS;
//    @BindView(R.id.txtTempT) TextView txtTempT;
    // end of added by rama

    final List<String> conditionOptions = Arrays.asList("GOOD", "BAD");
    @BindView(R.id.Kb_fan)
    Spinner Kb_fan;
    @BindView(R.id.vib_fan)
    Spinner Vib_fan;
    @BindView(R.id.KV_fan)
    Spinner KV_fan;
    @BindView(R.id.Temp_ds_imp)
    EditText Temp_ds_imp;
    @BindView(R.id.Temp_nds_imp)
    EditText Temp_nds_imp;
    @BindView(R.id.Pulley)
    Spinner Pulley;
    @BindView(R.id.SafetyG)
    Spinner SafetyG;
    @BindView(R.id.Houskeeping)
    Spinner Houskeeping;
    @BindView(R.id.UnsafeC)
    Spinner UnsafeC;

    @BindView(R.id.txt_Kb_fan)
    TextView txt_Kb_fan;
    @BindView(R.id.txt_vib_fan)
    TextView txt_vib_fan;
    @BindView(R.id.txt_KV_fan)
    TextView txt_KV_fan;
    @BindView(R.id.txt_Temp_ds_imp)
    TextView txt_Temp_ds_imp;
    @BindView(R.id.txt_Temp_nds_imp)
    TextView txt_Temp_nds_imp;
    @BindView(R.id.txt_Pulley)
    TextView txt_Pulley;
    @BindView(R.id.txt_SafetyG)
    TextView txt_SafetyG;
    @BindView(R.id.txt_Houskeeping)
    TextView txt_Houskeeping;
    @BindView(R.id.txt_UnsafeC)
    TextView txt_UnsafeC;

    @BindView(R.id.btn_Mkbf)
    Button btn_Mkbf;
    @BindView(R.id.btn_Mvf)
    Button btn_Mvf;
    @BindView(R.id.btn_Mkvf)
    Button btn_Mkvf;
    @BindView(R.id.btn_Mtbdi)
    Button btn_Mtbdi;
    @BindView(R.id.btn_Mtbni)
    Button btn_Mtbni;
    @BindView(R.id.btn_Mpavb)
    Button btn_Mpavb;
    @BindView(R.id.btn_Msg)
    Button btn_Msg;
    @BindView(R.id.btn_Mhmaa)
    Button btn_Mhmaa;
    @BindView(R.id.btn_Muc)
    Button btn_Muc;

    @BindView(R.id.keterangan)
    EditText keterangan;
    @BindView(R.id.spCondition)
    Spinner spCondition;
    @BindView(R.id.txtCondition)
    TextView txtCondition;
    @BindView(R.id.add_abis)
    Button add_abis;

    private int[] listbad = {0,0,0,0,0,0,0,0,0};
    private final String EQUIPMENTINFO_MODEL_KEY = "equipmentinfo_model_key";
    private Contract.ActivityCallback mActivityCallback;
    private double setting;
    private EquipmentInfo mEquipmentInfo;
    private int created_abnormalities=0;

    public static BearingMekanikalFragment newInstance(EquipmentInfo equipmentInfo) {
        BearingMekanikalFragment instance = new BearingMekanikalFragment();
        Bundle args = new Bundle();
        args.putParcelable(EquipmentInfo.TYPE_EXTRA, equipmentInfo);
        instance.setArguments(args);
        return instance;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bearing_mekanikal, container, false);
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
        String subtipe = "5";
        String tipe = "1";
//        Log.d("nomenclature",nomenclature);Log.d("subtipe",subtipe);Log.d("tipe",tipe);
        setdefval(tipe, subtipe);
//        end of added by rama
        return view;
    }
    private void setVIS(Button btn, int vis){
        btn.setVisibility(vis);
    }
    private void setspinnerlistener(final Spinner v, final int index){
        v.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                if(v.getSelectedItem().equals("BAD")){
                    listbad[index]=1;
                }else{
                    listbad[index]=0;
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }
        });
    }
    private void setTemplistener(final EditText v, final int index){
        v.setFilters(new InputFilter[]{ new InputFilterMinMax("0", "100")});
        v.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {}

            @Override
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                if(s.length()>0 && Double.parseDouble(s.toString())>70){
                    listbad[index]=1;
                }else{
                    listbad[index]=0;
                }
            }
        });
    }
    private void setSEnabled(boolean En){
        Kb_fan.setEnabled(En);
        Vib_fan.setEnabled(En);
        KV_fan.setEnabled(En);
        Pulley.setEnabled(En);
        SafetyG.setEnabled(En);
        Houskeeping.setEnabled(En);
        UnsafeC.setEnabled(En);
        Temp_ds_imp.setEnabled(En);
        Temp_nds_imp.setEnabled(En);
        keterangan.setEnabled(En);
    }
    private void initView() {
        setSEnabled(true);
        created_abnormalities=0;
        spCondition.setAdapter(DataHelper.createAdapter(getActivity(), conditionOptions));
        spCondition.setVisibility(View.GONE);
        txtCondition.setVisibility(View.GONE);
        add_abis.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setSEnabled(false);
                int bad = 0;
                for (int i = 0; i < listbad.length; i++) {
                    if(listbad[i]>=1){bad = 1;}
                }
                if(created_abnormalities==0) {
                    mActivityCallback.onConditionChanged2(bad, listbad);
                    created_abnormalities+=1;
                    add_abis.setText("added");
                    add_abis.setEnabled(false);
                }
            }
        });

        setVIS(btn_Mkbf,View.GONE);
        setVIS(btn_Mvf,View.GONE);
        setVIS(btn_Mkvf,View.GONE);
        setVIS(btn_Mtbdi,View.GONE);
        setVIS(btn_Mtbni,View.GONE);
        setVIS(btn_Mpavb,View.GONE);
        setVIS(btn_Msg,View.GONE);
        setVIS(btn_Mhmaa,View.GONE);
        setVIS(btn_Muc,View.GONE);

        setspinnerlistener(Kb_fan,0);
        setspinnerlistener(Vib_fan,1);
        setspinnerlistener(KV_fan,2);
        setspinnerlistener(Pulley,5);
        setspinnerlistener(SafetyG,6);
        setspinnerlistener(Houskeeping,7);
        setspinnerlistener(UnsafeC,8);

        setTemplistener(Temp_ds_imp,3);
        setTemplistener(Temp_nds_imp,4);

    }

    @Override
    Bearing setDataToModel() {
        Bearing bearing = new Bearing();

        bearing.setKeterangan(keterangan.getText().toString());

        if(!Kb_fan.getSelectedItem().toString().isEmpty())
            bearing.setKb_fan(Kb_fan.getSelectedItem().toString());
        if(!Vib_fan.getSelectedItem().toString().isEmpty())
            bearing.setVib_fan(Vib_fan.getSelectedItem().toString());
        if(!KV_fan.getSelectedItem().toString().isEmpty())
            bearing.setKV_fan(KV_fan.getSelectedItem().toString());
        if(!Temp_ds_imp.getText().toString().isEmpty())
            bearing.setTemp_ds_imp(Double.parseDouble(Temp_ds_imp.getText().toString()));
        if(!Temp_nds_imp.getText().toString().isEmpty())
            bearing.setTemp_nds_imp(Double.parseDouble(Temp_nds_imp.getText().toString()));
        if(!Pulley.getSelectedItem().toString().isEmpty())
            bearing.setPulley(Pulley.getSelectedItem().toString());
        if(!SafetyG.getSelectedItem().toString().isEmpty())
            bearing.setSafetyG(SafetyG.getSelectedItem().toString());
        if(!Houskeeping.getSelectedItem().toString().isEmpty())
            bearing.setHouskeeping(Houskeeping.getSelectedItem().toString());
        if(!UnsafeC.getSelectedItem().toString().isEmpty())
            bearing.setUnsafeC(UnsafeC.getSelectedItem().toString());
        if(!keterangan.getText().toString().isEmpty())
            bearing.setKeteranganM(keterangan.getText().toString());
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
    public class InputFilterMinMax implements InputFilter {

        private int min, max;

        public InputFilterMinMax(int min, int max) {
            this.min = min;
            this.max = max;
        }

        public InputFilterMinMax(String min, String max) {
            this.min = Integer.parseInt(min);
            this.max = Integer.parseInt(max);
        }

        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            try {
                int input = Integer.parseInt(dest.toString() + source.toString());
                if (isInRange(min, max, input))
                    return null;
            } catch (NumberFormatException nfe) { }
            return "";
        }

        private boolean isInRange(int a, int b, int c) {
            return b > a ? c >= a && c <= b : c >= b && c <= a;
        }
    }
    public void setdefval(String tipe, String subtipe){
        mdefval = ServiceGenerator.getInstance().getRetrofit().create(MsoService.class).GetDefaultValue(tipe, subtipe);
        mdefval.enqueue(new Callback<GetDefaultValueResponse>() {
            @Override
            public void onResponse(Call<GetDefaultValueResponse> call, Response<GetDefaultValueResponse> response) {
                if (response.isSuccessful()) {
                    if (response.body() != null && response.body().isSuccess()) {
//                        AutonomosSupervisor.setValue(response.body().getData());
//                        Log.d("GetPrevInspection2", response.body().getData().toString());
                        List<GetDefaultValue> data = response.body().getData();
                        String id, value;
                        for (int i=0;i<data.size();i++) {
                            id = data.get(i).getId();
                            if(data.get(i).getValue()==null){
                                value = " (N/A)";
                            }else{
                                value = " ("+data.get(i).getValue()+")";
                            }
                            if(id.equals("232")) { txt_Kb_fan.setText(txt_Kb_fan.getText().toString() + value);
                            }else if(id.equals("233")){txt_vib_fan.setText(txt_vib_fan.getText().toString() + value);
                            }else if(id.equals("234")) {txt_KV_fan.setText(txt_KV_fan.getText().toString() + value);
                            }else if(id.equals("235")) {txt_Temp_ds_imp.setText(txt_Temp_ds_imp.getText().toString() + value);
                            }else if(id.equals("236")) {txt_Temp_nds_imp.setText(txt_Temp_nds_imp.getText().toString() + value);
                            }else if(id.equals("237")) {txt_Pulley.setText(txt_Pulley.getText().toString() + value);
                            }else if(id.equals("238")) {txt_SafetyG.setText(txt_SafetyG.getText().toString() + value);
                            }else if(id.equals("239")) {txt_Houskeeping.setText(txt_Houskeeping.getText().toString() + value);
                            }else if(id.equals("240")) {txt_UnsafeC.setText(txt_UnsafeC.getText().toString() + value);
                            }
                        }
                    }else if(!response.body().isSuccess()){
                        showToast("data sebelumnya tidak dapat diambil");
                    }
                } else {
                    if (response.body() != null) {
                        Log.e("GetPrevInspection", "NULL" );
                    }
                }
            }

            @Override
            public void onFailure(Call<GetDefaultValueResponse> call, Throwable t) {
                Log.d("GetPrevInspection ",t.toString());
            }
        });
    }
//        end of added by rama
}
