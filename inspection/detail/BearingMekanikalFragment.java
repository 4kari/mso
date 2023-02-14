package id.sisi.si.mso.ui.inspection.detail;

import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import org.apache.commons.lang3.ObjectUtils;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import id.sisi.si.mso.R;
import id.sisi.si.mso.data.model.Bearing;
import id.sisi.si.mso.data.model.DetailInspection;
import id.sisi.si.mso.data.model.picItem;
import id.sisi.si.mso.utils.TextUtils;

public class BearingMekanikalFragment extends TechnicalFragment {

    private final String BEARING_MODEL_KEY = "bearing_model_key";
    private Bearing bearing;
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
    @BindView(R.id.txtCondition)
    TextView txtCondition;
    @BindView(R.id.spCondition)
    Spinner spCondition;
    @BindView(R.id.add_abis)
    Button add_abis;

    public static BearingMekanikalFragment newInstance() {
        BearingMekanikalFragment instance = new BearingMekanikalFragment();
        return instance;
    }

    public static BearingMekanikalFragment newInstance(DetailInspection detailInspectionModel, Bearing bearingModel) {
        BearingMekanikalFragment instance = new BearingMekanikalFragment();
        Bundle args = new Bundle();
        args.putParcelable(DetailInspection.TYPE_EXTRA, detailInspectionModel);
        args.putParcelable(Bearing.TYPE_EXTRA, bearingModel);
        instance.setArguments(args);
        return instance;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bearing_mekanikal, container, false);
        setUnbinder(ButterKnife.bind(this, view));

        txtCondition.setVisibility(View.GONE);
        spCondition.setVisibility(View.GONE);

        if (getArguments().getParcelable(DetailInspection.TYPE_EXTRA) != null
                & getArguments().getParcelable(Bearing.TYPE_EXTRA) != null) {
            DetailInspection detailInspectionModel = getArguments().getParcelable(DetailInspection.TYPE_EXTRA);
            bearing = getArguments().getParcelable(Bearing.TYPE_EXTRA);
            setData(detailInspectionModel);
            Log.d("detailInspectionModel",detailInspectionModel.getBearing().toString());
            populateFields();
            setupForDetail();
        }

        return view;
    }

    @Override
    void populateFields() {
        try {
            clearNullData(getData().getBearing().getTemp_ds_imp(), Temp_ds_imp);
            clearNullData(getData().getBearing().getTemp_nds_imp(), Temp_nds_imp);

            setSpinnerVal(getData().getBearing().getKb_fan(),Kb_fan);
            setSpinnerVal(getData().getBearing().getVib_fan(),Vib_fan);
            setSpinnerVal(getData().getBearing().getKV_fan(),KV_fan);
            setSpinnerVal(getData().getBearing().getPulley(),Pulley);
            setSpinnerVal(getData().getBearing().getSafetyG(),SafetyG);
            setSpinnerVal(getData().getBearing().getHouskeeping(),Houskeeping);
            setSpinnerVal(getData().getBearing().getUnsafeC(),UnsafeC);
            keterangan.setText(getData().getBearing().getKeteranganM());
        }
        catch (NullPointerException e) {
            Log.d("nullpointer",e.getMessage());
        }
    }
    void setSpinnerVal(String data, Spinner sp){
        Log.d("setSpinnerVal: ",data);
        if(data.equals("GOOD")){
            sp.setSelection(0);
        }else if(data.equals("WARNING")){
            sp.setSelection(1);
        }else{
            sp.setSelection(2);
        }
    }
    void clearNullData(Double value, EditText mEditText){
        if(value==null || value.equals(0.0))
            mEditText.setText("0.0");
        else
            mEditText.setText("" + value);
    }

//    void checkBearing(Double value, EditText mEditText){
//        if(value.equals(0.0))
//            mEditText.getText().clear();
//        else
//            mEditText.setText("" + value);
//
//        if(!value.toString().isEmpty()) {
//            if(!TextUtils.isNullOrEmpty(ampereSetting.getText().toString())) {
//                Double setting = Double.parseDouble(ampereSetting.getText().toString());
//                if (value >= setting)
//                    mEditText.getBackground().setColorFilter(getResources().getColor(R.color.material_red_400), PorterDuff.Mode.SRC_ATOP);
//                else if (value < setting)
//                    mEditText.getBackground().setColorFilter(getResources().getColor(R.color.material_green_400), PorterDuff.Mode.SRC_ATOP);
//            }
//            else
//                mEditText.getBackground().setColorFilter(getResources().getColor(R.color.ms_white_54_opacity), PorterDuff.Mode.SRC_ATOP);
//        }
//        else
//        {
//            mEditText.getBackground().setColorFilter(getResources().getColor(R.color.ms_white_54_opacity), PorterDuff.Mode.SRC_ATOP);
//        }
//    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((InspectionDetailActivity) getActivity()).measureViewPager(this);
    }

    @Override
    void setupForEdit() {
        Temp_ds_imp.setFocusable(true);
        Temp_ds_imp.setFocusableInTouchMode(true);
        Temp_nds_imp.setFocusable(true);
        Temp_nds_imp.setFocusableInTouchMode(true);
        Kb_fan.setEnabled(true);
        Vib_fan.setEnabled(true);
        KV_fan.setEnabled(true);
        Pulley.setEnabled(true);
        SafetyG.setEnabled(true);
        Houskeeping.setEnabled(true);
        UnsafeC.setEnabled(true);
        keterangan.setFocusable(true);
        keterangan.setFocusableInTouchMode(true);
    }

    @Override
    void setupForDetail() {
        Temp_ds_imp.setFocusable(false);
        Temp_nds_imp.setFocusable(false);
        Kb_fan.setEnabled(false);
        Vib_fan.setEnabled(false);
        KV_fan.setEnabled(false);
        Pulley.setEnabled(false);
        SafetyG.setEnabled(false);
        Houskeeping.setEnabled(false);
        UnsafeC.setEnabled(false);
        keterangan.setFocusable(false);
        add_abis.setVisibility(View.GONE);
    }

    @Override
    void setDataToModel() {
        bearing.setKeteranganM(keterangan.getText().toString());

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
    }

    @Override
    void updateMember(List<picItem> picItems) {

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelable(BEARING_MODEL_KEY, bearing);
    }
}
