package id.sisi.si.mso.ui.inspection.detail;

import android.content.Context;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import id.sisi.si.mso.R;
import id.sisi.si.mso.data.model.Bearing;
import id.sisi.si.mso.data.model.DetailInspection;
import id.sisi.si.mso.data.model.picItem;
import id.sisi.si.mso.utils.TextUtils;

public class BearingFragmentAfter extends TechnicalFragment {

    private final String BEARING_MODEL_KEY = "bearing_model_key";

    @BindView(R.id.vibrasiDsVert)
    EditText vibrasiDsVert;
    @BindView(R.id.vibrasiDsHor)
    EditText vibrasiDsHor;
    @BindView(R.id.vibrasiDsAxial)
    EditText vibrasiDsAxial;
    @BindView(R.id.geDsVert)
    EditText geDsVert;
    @BindView(R.id.geDsHor)
    EditText geDsHor;
    @BindView(R.id.geDsAxial)
    EditText geDsAxial;

    @BindView(R.id.vibrasiNdsVer)
    EditText vibrasiNdsVer;
    @BindView(R.id.vibrasiNdsHor)
    EditText vibrasiNdsHor;
    @BindView(R.id.vibrasiNdsAxial)
    EditText vibrasiNdsAxial;
    @BindView(R.id.geNdsVert)
    EditText geNdsVert;
    @BindView(R.id.geNdsHor)
    EditText geNdsHor;
    @BindView(R.id.geNdsAxial)
    EditText geNdsAxial;
    @BindView(R.id.temperatureDS)
    EditText temperatureDS;
    @BindView(R.id.temperatureNds)
    EditText temperatureNDS;

    @BindView(R.id.txtTempDS)
    TextView txtTempDS;
    @BindView(R.id.txtTempNDS)
    TextView txtTempNDS;

    @BindView(R.id.etKelengkapanMtr)
    EditText etKelengkapanMtr;
    @BindView(R.id.etKeterangan)
    EditText etKeterangan;

    @BindView(R.id.txtKelengkapanMtr)
    TextView txtKelengkapanMtr;
    @BindView(R.id.txtKeterangan)
    TextView txtKeterangan;

    private Bearing bearing;
    private String errorMessage;

    public static BearingFragmentAfter newInstance() {
        BearingFragmentAfter instance = new BearingFragmentAfter();
        return instance;
    }

    public static BearingFragmentAfter newInstance(DetailInspection detailInspectionModel, Bearing bearingModel) {
        BearingFragmentAfter instance = new BearingFragmentAfter();
        Bundle args = new Bundle();
        args.putParcelable(DetailInspection.TYPE_EXTRA, detailInspectionModel);
        args.putParcelable(Bearing.TYPE_EXTRA, bearingModel);
        instance.setArguments(args);
        return instance;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bearing_before_regrease, container, false);
        setUnbinder(ButterKnife.bind(this, view));

        txtTempDS.setVisibility(View.GONE);
        txtTempNDS.setVisibility(View.GONE);
        temperatureDS.setVisibility(View.GONE);
        temperatureNDS.setVisibility(View.GONE);
        etKelengkapanMtr.setVisibility(View.VISIBLE);
        txtKelengkapanMtr.setVisibility(View.VISIBLE);
        etKeterangan.setVisibility(View.VISIBLE);
        txtKeterangan.setVisibility(View.VISIBLE);

        if (getArguments().getParcelable(DetailInspection.TYPE_EXTRA) != null
                & getArguments().getParcelable(Bearing.TYPE_EXTRA) != null) {
            DetailInspection detailInspectionModel = getArguments().getParcelable(DetailInspection.TYPE_EXTRA);
            bearing = getArguments().getParcelable(Bearing.TYPE_EXTRA);
            setData(detailInspectionModel);
            populateFields();
            setupForDetail();
        }

        EditText[] bearingEt = { geDsVert, geDsHor, geDsAxial, geNdsVert, geNdsAxial, geNdsHor};
        for(EditText et : bearingEt)
            et.addTextChangedListener(new bearingWatcher(et));

        EditText[] vibrasiEt = { vibrasiDsVert, vibrasiDsHor, vibrasiDsAxial, vibrasiNdsVer, vibrasiNdsAxial, vibrasiNdsHor};
        for(EditText et : vibrasiEt)
            et.addTextChangedListener(new vibrasiWatcher(et));

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((InspectionDetailActivity) getActivity()).measureViewPager(this);
    }

    @Override
    void populateFields() {
        try {
            etKelengkapanMtr.setText(getData().getBearing().getKelengkapanMotor());
            etKeterangan.setText(getData().getBearing().getKeterangan());

            System.out.println("get data" + getData().getBearing().getVibrasiDsVert_after());
            checkVibrasi(getData().getBearing().getVibrasiDsVert_after(), vibrasiDsVert);
            checkVibrasi(getData().getBearing().getVibrasiDsHor_after(), vibrasiDsHor);
            checkVibrasi(getData().getBearing().getVibrasiDsAxial_after(), vibrasiDsAxial);
            checkBearing(getData().getBearing().getGeDsVert_after(), geDsVert);
            checkBearing(getData().getBearing().getGeDsAxial_after(), geDsAxial);
            checkBearing(getData().getBearing().getGeDsHor_after(), geDsHor);

            checkVibrasi(getData().getBearing().getVibrasiNdsVert_after(), vibrasiNdsVer);
            checkVibrasi(getData().getBearing().getVibrasiNdsHor_after(), vibrasiNdsHor);
            checkVibrasi(getData().getBearing().getVibrasiNdsAxial_after(), vibrasiNdsAxial);
            checkBearing(getData().getBearing().getGeNdsVert_after(), geNdsVert);
            checkBearing(getData().getBearing().getGeNdsAxial_after(), geNdsAxial);
            checkBearing(getData().getBearing().getGeNdsHor_after(), geNdsHor);

        } catch (NullPointerException e) {

        }
    }

    void checkVibrasi(Double value, EditText mEditText){
        if(value != null) {
            if(value.equals(0.0))
                mEditText.getText().clear();
            else
                mEditText.setText("" + value);
            if (!value.toString().isEmpty()) {
                if (value > 4.5)
                    mEditText.getBackground().setColorFilter(getResources().getColor(R.color.material_red_400), PorterDuff.Mode.SRC_ATOP);
                else if (value >= 2.81 && value <= 4.5)
                    mEditText.getBackground().setColorFilter(getResources().getColor(R.color.material_orange_400), PorterDuff.Mode.SRC_ATOP);
                else if (value >= 1.41 && value <= 2.8)
                    mEditText.getBackground().setColorFilter(getResources().getColor(R.color.material_yellow_400), PorterDuff.Mode.SRC_ATOP);
                else
                    mEditText.getBackground().setColorFilter(getResources().getColor(R.color.ms_white_54_opacity), PorterDuff.Mode.SRC_ATOP);
            } else {
                mEditText.getBackground().setColorFilter(getResources().getColor(R.color.ms_white_54_opacity), PorterDuff.Mode.SRC_ATOP);
            }
        }
    }

    void checkBearing(Double value, EditText mEditText){
        if(value.equals(0.0))
            mEditText.getText().clear();
        else
            mEditText.setText("" + value);
        if(!value.toString().isEmpty()) {
            if (value > 4)
                mEditText.getBackground().setColorFilter(getResources().getColor(R.color.material_red_400), PorterDuff.Mode.SRC_ATOP);
            else if (value >= 2.1 && value <= 4)
                mEditText.getBackground().setColorFilter(getResources().getColor(R.color.material_yellow_400), PorterDuff.Mode.SRC_ATOP);
            else if (value >= 0.1 && value <= 2)
                mEditText.getBackground().setColorFilter(getResources().getColor(R.color.material_green_400), PorterDuff.Mode.SRC_ATOP);
            else
                mEditText.getBackground().setColorFilter(getResources().getColor(R.color.ms_white_54_opacity), PorterDuff.Mode.SRC_ATOP);
        }
        else
        {
            mEditText.getBackground().setColorFilter(getResources().getColor(R.color.ms_white_54_opacity), PorterDuff.Mode.SRC_ATOP);
        }
    }

    @Override
    void setupForEdit() {
        etKelengkapanMtr.setFocusable(true);
        etKelengkapanMtr.setFocusableInTouchMode(true);
        etKeterangan.setFocusable(true);
        etKeterangan.setFocusableInTouchMode(true);

        vibrasiDsVert.setFocusable(true);
        vibrasiDsVert.setFocusableInTouchMode(true);
        vibrasiDsAxial.setFocusable(true);
        vibrasiDsAxial.setFocusableInTouchMode(true);
        vibrasiDsHor.setFocusable(true);
        vibrasiDsHor.setFocusableInTouchMode(true);

        geDsVert.setFocusable(true);
        geDsVert.setFocusableInTouchMode(true);
        geDsAxial.setFocusable(true);
        geDsAxial.setFocusableInTouchMode(true);
        geDsHor.setFocusable(true);
        geDsHor.setFocusableInTouchMode(true);

        temperatureDS.setFocusable(true);
        temperatureDS.setFocusableInTouchMode(true);

        vibrasiNdsVer.setFocusable(true);
        vibrasiNdsVer.setFocusableInTouchMode(true);
        vibrasiNdsAxial.setFocusable(true);
        vibrasiNdsAxial.setFocusableInTouchMode(true);
        vibrasiNdsHor.setFocusable(true);
        vibrasiNdsHor.setFocusableInTouchMode(true);

        geNdsVert.setFocusable(true);
        geNdsVert.setFocusableInTouchMode(true);
        geNdsAxial.setFocusable(true);
        geNdsAxial.setFocusableInTouchMode(true);
        geNdsHor.setFocusable(true);
        geNdsHor.setFocusableInTouchMode(true);

        temperatureNDS.setFocusable(true);
        temperatureNDS.setFocusableInTouchMode(true);

        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(vibrasiDsVert, InputMethodManager.SHOW_IMPLICIT);
    }

    @Override
    void setupForDetail() {
        etKeterangan.setFocusable(false);
        etKelengkapanMtr.setFocusable(false);
        vibrasiDsVert.setFocusable(false);
        vibrasiDsAxial.setFocusable(false);
        vibrasiDsHor.setFocusable(false);
        geDsVert.setFocusable(false);
        geDsAxial.setFocusable(false);
        geDsHor.setFocusable(false);
        temperatureDS.setFocusable(false);

        vibrasiNdsVer.setFocusable(false);
        vibrasiNdsAxial.setFocusable(false);
        vibrasiNdsHor.setFocusable(false);
        geNdsVert.setFocusable(false);
        geNdsAxial.setFocusable(false);
        geNdsHor.setFocusable(false);
        temperatureNDS.setFocusable(false);


    }

    @Override
    void setDataToModel() {

        bearing.setKelengkapanMotor(etKelengkapanMtr.getText().toString());
        bearing.setKeterangan(etKeterangan.getText().toString());
        if(!vibrasiDsVert.getText().toString().isEmpty())
            bearing.setVibrasiDsVert_after(Double.parseDouble(vibrasiDsVert.getText().toString()));
        if(!vibrasiDsHor.getText().toString().isEmpty())
            bearing.setVibrasiDsHor_after(Double.parseDouble(vibrasiDsHor.getText().toString()));
        if(!vibrasiDsAxial.getText().toString().isEmpty())
            bearing.setVibrasiDsAxial_after(Double.parseDouble(vibrasiDsAxial.getText().toString()));

        if(!geDsVert.getText().toString().isEmpty())
            bearing.setGeDsVert_after(Double.parseDouble(geDsVert.getText().toString()));
        if(!geDsHor.getText().toString().isEmpty())
            bearing.setGeDsHor_after(Double.parseDouble(geDsHor.getText().toString()));
        if(!geDsAxial.getText().toString().isEmpty())
            bearing.setGeDsAxial_after(Double.parseDouble(geDsAxial.getText().toString()));

        if(!geNdsVert.getText().toString().isEmpty())
            bearing.setGeNdsVert_after(Double.parseDouble(geNdsVert.getText().toString()));
        if(!geNdsHor.getText().toString().isEmpty())
            bearing.setGeNdsHor_after(Double.parseDouble(geNdsHor.getText().toString()));
        if(!geNdsAxial.getText().toString().isEmpty())
            bearing.setGeNdsAxial_after(Double.parseDouble(geNdsAxial.getText().toString()));

        if(!vibrasiNdsVer.getText().toString().isEmpty())
            bearing.setVibrasiNdsVert_after(Double.parseDouble(vibrasiNdsVer.getText().toString()));
        if(!vibrasiNdsHor.getText().toString().isEmpty())
            bearing.setVibrasiNdsHor_after(Double.parseDouble(vibrasiNdsHor.getText().toString()));
        if(!vibrasiNdsAxial.getText().toString().isEmpty())
            bearing.setVibrasiNdsAxial_after(Double.parseDouble(vibrasiNdsAxial.getText().toString()));

    }

    @Override
    void updateMember(List<picItem> picItems) {
    }

    private class bearingWatcher implements TextWatcher {
        private EditText mEditText;

        public bearingWatcher(EditText e) {
            mEditText = e;
        }

        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        public void afterTextChanged(Editable s) {
            if(!s.toString().isEmpty()) {
                Double value = Double.parseDouble(s.toString());
                if (value > 4)
                    mEditText.getBackground().setColorFilter(getResources().getColor(R.color.material_red_400), PorterDuff.Mode.SRC_ATOP);
                else if (value >= 2.1 && value <= 4)
                    mEditText.getBackground().setColorFilter(getResources().getColor(R.color.material_yellow_400), PorterDuff.Mode.SRC_ATOP);
                else if (value >= 0.1 && value <= 2)
                    mEditText.getBackground().setColorFilter(getResources().getColor(R.color.material_green_400), PorterDuff.Mode.SRC_ATOP);
                else
                    mEditText.getBackground().setColorFilter(getResources().getColor(R.color.ms_white_54_opacity), PorterDuff.Mode.SRC_ATOP);
            }
            else
            {
                mEditText.getBackground().setColorFilter(getResources().getColor(R.color.ms_white_54_opacity), PorterDuff.Mode.SRC_ATOP);
            }
        }
    }

    private class vibrasiWatcher implements TextWatcher {
        private EditText mEditText;

        public vibrasiWatcher(EditText e) {
            mEditText = e;
        }

        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        public void afterTextChanged(Editable s) {
            if(!s.toString().isEmpty()) {
                Double value = Double.parseDouble(s.toString());
                if (value > 4.5)
                    mEditText.getBackground().setColorFilter(getResources().getColor(R.color.material_red_400), PorterDuff.Mode.SRC_ATOP);
                else if (value >= 2.81 && value <= 4.5)
                    mEditText.getBackground().setColorFilter(getResources().getColor(R.color.material_orange_400), PorterDuff.Mode.SRC_ATOP);
                else if (value >= 1.41 && value <= 2.8)
                    mEditText.getBackground().setColorFilter(getResources().getColor(R.color.material_yellow_400), PorterDuff.Mode.SRC_ATOP);
                else
                    mEditText.getBackground().setColorFilter(getResources().getColor(R.color.ms_white_54_opacity), PorterDuff.Mode.SRC_ATOP);
            }
            else
            {
                mEditText.getBackground().setColorFilter(getResources().getColor(R.color.ms_white_54_opacity), PorterDuff.Mode.SRC_ATOP);
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelable(BEARING_MODEL_KEY, bearing);
    }
}
