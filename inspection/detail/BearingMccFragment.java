package id.sisi.si.mso.ui.inspection.detail;

import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import id.sisi.si.mso.R;
import id.sisi.si.mso.data.model.Bearing;
import id.sisi.si.mso.data.model.DetailInspection;
import id.sisi.si.mso.data.model.picItem;
import id.sisi.si.mso.utils.TextUtils;

public class BearingMccFragment extends TechnicalFragment {

    private final String BEARING_MODEL_KEY = "bearing_model_key";
    private Bearing bearing;
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
    @BindView(R.id.txtCondition)
    TextView txtCondition;
    @BindView(R.id.spCondition)
    Spinner spCondition;

    public static BearingMccFragment newInstance() {
        BearingMccFragment instance = new BearingMccFragment();
        return instance;
    }

    public static BearingMccFragment newInstance(DetailInspection detailInspectionModel, Bearing bearingModel) {
        BearingMccFragment instance = new BearingMccFragment();
        Bundle args = new Bundle();
        args.putParcelable(DetailInspection.TYPE_EXTRA, detailInspectionModel);
        args.putParcelable(Bearing.TYPE_EXTRA, bearingModel);
        instance.setArguments(args);
        return instance;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bearing_mccform, container, false);
        setUnbinder(ButterKnife.bind(this, view));

        txtCondition.setVisibility(View.GONE);
        spCondition.setVisibility(View.GONE);

        if (getArguments().getParcelable(DetailInspection.TYPE_EXTRA) != null
                & getArguments().getParcelable(Bearing.TYPE_EXTRA) != null) {
            DetailInspection detailInspectionModel = getArguments().getParcelable(DetailInspection.TYPE_EXTRA);
            bearing = getArguments().getParcelable(Bearing.TYPE_EXTRA);
            setData(detailInspectionModel);
            populateFields();
            setupForDetail();
        }

        return view;
    }

    @Override
    void populateFields() {
        try {
            clearNullData(getData().getBearing().getKw(), kW);
            clearNullData(getData().getBearing().getFla(), fla);
            clearNullData(getData().getBearing().getAmpereSetting(), ampereSetting);
            checkBearing(getData().getBearing().getAmpereRact(), ampereR);
            checkBearing(getData().getBearing().getAmpereSact(), ampereS);
            checkBearing(getData().getBearing().getAmpereTact(), ampereT);
            clearNullData(getData().getBearing().getTemperatureT(), tempT);
            clearNullData(getData().getBearing().getTemperatureR(), tempR);
            clearNullData(getData().getBearing().getTemperatureS(), tempS);
            keterangan.setText(getData().getBearing().getKeterangan());
        }
        catch (NullPointerException e) {

        }
    }

    void clearNullData(Double value, EditText mEditText){
        if(value.equals(0.0))
            mEditText.getText().clear();
        else
            mEditText.setText("" + value);
    }

    void checkBearing(Double value, EditText mEditText){
        if(value.equals(0.0))
            mEditText.getText().clear();
        else
            mEditText.setText("" + value);

        if(!value.toString().isEmpty()) {
            if(!TextUtils.isNullOrEmpty(ampereSetting.getText().toString())) {
                Double setting = Double.parseDouble(ampereSetting.getText().toString());
                if (value >= setting)
                    mEditText.getBackground().setColorFilter(getResources().getColor(R.color.material_red_400), PorterDuff.Mode.SRC_ATOP);
                else if (value < setting)
                    mEditText.getBackground().setColorFilter(getResources().getColor(R.color.material_green_400), PorterDuff.Mode.SRC_ATOP);
            }
            else
                mEditText.getBackground().setColorFilter(getResources().getColor(R.color.ms_white_54_opacity), PorterDuff.Mode.SRC_ATOP);
        }
        else
        {
            mEditText.getBackground().setColorFilter(getResources().getColor(R.color.ms_white_54_opacity), PorterDuff.Mode.SRC_ATOP);
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((InspectionDetailActivity) getActivity()).measureViewPager(this);
    }

    @Override
    void setupForEdit() {
        /*kW.setFocusable(true);
        kW.setFocusableInTouchMode(true);
        fla.setFocusable(true);
        fla.setFocusableInTouchMode(true);
        ampereSetting.setFocusable(true);
        ampereSetting.setFocusableInTouchMode(true);*/
        ampereS.setFocusable(true);
        ampereS.setFocusableInTouchMode(true);
        ampereR.setFocusable(true);
        ampereR.setFocusableInTouchMode(true);
        ampereT.setFocusable(true);
        ampereT.setFocusableInTouchMode(true);
        tempR.setFocusable(true);
        tempR.setFocusableInTouchMode(true);
        tempS.setFocusable(true);
        tempS.setFocusableInTouchMode(true);
        tempT.setFocusable(true);
        tempT.setFocusableInTouchMode(true);
        keterangan.setFocusable(true);
        keterangan.setFocusableInTouchMode(true);

    }

    @Override
    void setupForDetail() {
        kW.setFocusable(false);
        fla.setFocusable(false);
        ampereSetting.setFocusable(false);
        ampereT.setFocusable(false);
        ampereS.setFocusable(false);
        ampereR.setFocusable(false);
        tempR.setFocusable(false);
        tempS.setFocusable(false);
        tempT.setFocusable(false);
        keterangan.setFocusable(false);

    }

    @Override
    void setDataToModel() {

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
