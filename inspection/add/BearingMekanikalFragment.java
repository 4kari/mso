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

import com.stepstone.stepper.VerificationError;

import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import id.sisi.si.mso.R;
import id.sisi.si.mso.data.DataHelper;
import id.sisi.si.mso.data.model.Bearing;
import id.sisi.si.mso.data.model.EquipmentInfo;
import id.sisi.si.mso.data.model.InspectionDetail;
import id.sisi.si.mso.data.model.picItem;
import id.sisi.si.mso.ui.abnormality.addOrEdit.AbnormalityAddOrEditActivity;

/**
 * Created by Luhur on 1/10/2019.
 */

public class BearingMekanikalFragment extends TechnicalFragment {
    //added by rama 08 nov 2022 untuk prev data inspeksi
//    private Call<GetPrevInspectionResponse> mPrevIns;
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

    private int[] listbad = {0,0,0,0,0,0,0,0,0};
    private final String EQUIPMENTINFO_MODEL_KEY = "equipmentinfo_model_key";
    private Contract.ActivityCallback mActivityCallback;
    private double setting;
    private EquipmentInfo mEquipmentInfo;

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
//        setprevinspection(nomenclature,tipe, subtipe);
//        end of added by rama
        return view;
    }
    private void setVIS(Button btn, int vis){
        btn.setVisibility(vis);
    }
    private void setspinnerlistener(final Spinner v, final Button btn, final int index){
        v.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                if(v.getSelectedItem().equals("BAD")){
//                    Log.d("spinner","jalan");
                    listbad[index]=1;
//                    mActivityCallback.onConditionChanged2(0, listbad);
                    setVIS(btn,View.VISIBLE);
                }else{
                    listbad[index]=0;
                    int bad = 1;
                    for (int i = 0; i < listbad.length; i++) {
                        if(listbad[i]==1){bad = 0;}
                    }
                    setVIS(btn,View.INVISIBLE);
//                    mActivityCallback.onConditionChanged2(bad, listbad);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }
        });
    }
    private void setTemplistener(final EditText v, final Button btn, final int index){
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
//                    mActivityCallback.onConditionChanged2(1, listbad);
                    setVIS(btn,View.VISIBLE);
                }else{
                    listbad[index]=0;
                    setVIS(btn,View.INVISIBLE);
//                    mActivityCallback.onConditionChanged2(1, listbad);
                }
            }
        });
    }
    private void initView() {
        spCondition.setAdapter(DataHelper.createAdapter(getActivity(), conditionOptions));
//        spCondition.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
//                mActivityCallback.onConditionChanged(i);
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> adapterView) {
//
//            }
//        });

//        btn_Mkbf; btn_Mvf; btn_Mkvf;btn_Mtbdi;btn_Mtbni;btn_Mpavb;btn_Msg;btn_Mhmaa;btn_Muc;
//        Spinner Kb_fan;Spinner Vib_fan;Spinner KV_fan;EditText Temp_ds_imp;EditText Temp_nds_imp;Spinner Pulley;
//        Spinner SafetyG;Spinner Houskeeping;Spinner UnsafeC;

        setVIS(btn_Mkbf,View.INVISIBLE);
        setVIS(btn_Mvf,View.INVISIBLE);
        setVIS(btn_Mkvf,View.INVISIBLE);
        setVIS(btn_Mtbdi,View.INVISIBLE);
        setVIS(btn_Mtbni,View.INVISIBLE);
        setVIS(btn_Mpavb,View.INVISIBLE);
        setVIS(btn_Msg,View.INVISIBLE);
        setVIS(btn_Mhmaa,View.INVISIBLE);
        setVIS(btn_Muc,View.INVISIBLE);

        setspinnerlistener(Kb_fan,btn_Mkbf,0);
        setspinnerlistener(Vib_fan,btn_Mvf,1);
        setspinnerlistener(KV_fan,btn_Mkvf,2);
        setspinnerlistener(Pulley,btn_Mpavb,5);
        setspinnerlistener(SafetyG,btn_Msg,6);
        setspinnerlistener(Houskeeping,btn_Mhmaa,7);
        setspinnerlistener(UnsafeC,btn_Muc,8);

        setTemplistener(Temp_ds_imp,btn_Mtbdi,3);
        setTemplistener(Temp_nds_imp,btn_Mtbni,4);

        btn_Mkbf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent intent = new Intent(getActivity(), AddAbnormalMekanikal.class);
                Intent intent = new Intent(getActivity(), AbnormalityAddOrEditActivity.class);
                startActivity(intent);

            }
        });

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
//            if(!TextUtils.isNullOrEmpty(s.toString().trim())){
//                Double value = Double.parseDouble(s.toString());
//                if(!TextUtils.isNullOrEmpty(ampereSetting.getText().toString())) {
//                    setting = Double.parseDouble(ampereSetting.getText().toString());
//                    if (value >= setting)
//                        mEditText.getBackground().setColorFilter(getResources().getColor(R.color.material_red_400), PorterDuff.Mode.SRC_ATOP);
//                    else if (value < setting)
//                        mEditText.getBackground().setColorFilter(getResources().getColor(R.color.material_green_400), PorterDuff.Mode.SRC_ATOP);
//                }
//                else
//                    mEditText.getBackground().setColorFilter(getResources().getColor(R.color.ms_white_54_opacity), PorterDuff.Mode.SRC_ATOP);
//                checkCondition(value, mEditText);
//            }
//            else
//            {
//                checkCondition(0.0, mEditText);
//                mEditText.getBackground().setColorFilter(getResources().getColor(R.color.ms_white_54_opacity), PorterDuff.Mode.SRC_ATOP);
//            }
        }
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
    public void setprevinspection(String nomenclature,String tipe, String subtipe){
//        mPrevIns = ServiceGenerator.getInstance().getRetrofit().create(MsoService.class).getPrevInspection(nomenclature,tipe, subtipe);
//        mPrevIns.enqueue(new Callback<GetPrevInspectionResponse>() {
//            @Override
//            public void onResponse(Call<GetPrevInspectionResponse> call, Response<GetPrevInspectionResponse> response) {
//                if (response.isSuccessful()) {
//                    if (response.body() != null && response.body().isSuccess()) {
////                        AutonomosSupervisor.setValue(response.body().getData());
////                        Log.d("GetPrevInspection2", response.body().getData().toString());
//                        List<GetPrevInspection> data = response.body().getData();
//                        String id, value;
//                        for (int i=0;i<data.size();i++) {
//                            id = data.get(i).getId();
//                            value = " (prev : "+data.get(i).getValue()+")";
//                            if(id.equals("49")) { txtKW.setText(txtKW.getText().toString() + value);
//                            }else if(id.equals("50")){txtfLa.setText(txtfLa.getText().toString() + value);
//                            }else if(id.equals("51")) {txtAmpSet.setText(txtAmpSet.getText().toString() + value);
//                            }else if(id.equals("52")) {txtAmpR.setText(txtAmpR.getText().toString() + value);
//                            }else if(id.equals("53")) {txtAmpS.setText(txtAmpS.getText().toString() + value);
//                            }else if(id.equals("54")) {txtAmpT.setText(txtAmpT.getText().toString() + value);
//                            }else if(id.equals("55")) {txtTempR.setText(txtTempR.getText().toString() + value);
//                            }else if(id.equals("56")) {txtTempS.setText(txtTempS.getText().toString() + value);
//                            }else if(id.equals("57")) {txtTempT.setText(txtTempT.getText().toString() + value);
//                            }
//                        }
//                    }else if(!response.body().isSuccess()){
//                        showToast("data sebelumnya tidak dapat diambil");
//                        call.cancel();
//                    }
//                } else {
//                    if (response.body() != null) {
//                        Log.e("GetPrevInspection", "NULL" );
//                    }
//                }
//            }
//
//            @Override
//            public void onFailure(Call<GetPrevInspectionResponse> call, Throwable t) {
//                Log.d("GetPrevInspection ",t.toString());
//            }
//        });
    }
//        end of added by rama
}
