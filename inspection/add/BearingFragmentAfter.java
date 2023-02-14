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
import id.sisi.si.mso.data.model.GetPrevInspection;
import id.sisi.si.mso.data.model.GetPrevInspectionResponse;
import id.sisi.si.mso.data.model.InspectionDetail;
import id.sisi.si.mso.data.model.picItem;
import id.sisi.si.mso.utils.TextUtils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by durrrr on 11-Oct-17.
 * Email: cab.purnama@gmail.com
 */
public class BearingFragmentAfter extends TechnicalFragment{

    final List<String> conditionOptions = Arrays.asList("GOOD", "BAD");

    //added by rama 08 nov 2022 untuk prev data inspeksi
    private Call<GetPrevInspectionResponse> mPrevIns;
    @BindView(R.id.txtgEDSVert) TextView txtgEDSVert;
    @BindView(R.id.txtgEDSHor) TextView txtgEDSHor;
    @BindView(R.id.txtgEDSAxial) TextView txtgEDSAxial;
    @BindView(R.id.txtVibDSVert) TextView txtVibDSVert;
    @BindView(R.id.txtVibDSHor) TextView txtVibDSHor;
    @BindView(R.id.txtVibDSAxial) TextView txtVibDSAxial;
    @BindView(R.id.txtgENDSVert) TextView txtgENDSVert;
    @BindView(R.id.txtgENDSHor) TextView txtgENDSHor;
    @BindView(R.id.txtgENDSAxial) TextView txtgENDSAxial;
    @BindView(R.id.txtVibNDSVert) TextView txtVibNDSVert;
    @BindView(R.id.txtVibNDSHor) TextView txtVibNDSHor;
    @BindView(R.id.txtVibNDSAxial) TextView txtVibNDSAxial;
    // end of added by rama

    @BindView(R.id.vibrasiDsVert)
    EditText vibrasiDsVert;
    @BindView(R.id.vibrasiDsHor)
    EditText vibrasiDsHor;
    @BindView(R.id.vibrasiDsAxial)
    EditText vibrasiDsAxial;
    @BindView(R.id.geDsVert)
    EditText geDsVertAfter;
    @BindView(R.id.geDsHor)
    EditText geDsHorAfter;
    @BindView(R.id.geDsAxial)
    EditText geDsAxialAfter;

    @BindView(R.id.temperatureDS)
    EditText temperatureDS;
    @BindView(R.id.temperatureNds)
    EditText temperatureNDS;

    @BindView(R.id.vibrasiNdsVer)
    EditText vibrasiNdsVerAfter;
    @BindView(R.id.vibrasiNdsHor)
    EditText vibrasiNdsHorAfter;
    @BindView(R.id.vibrasiNdsAxial)
    EditText vibrasiNdsAxialAfter;
    @BindView(R.id.geNdsVert)
    EditText geNdsVertAfter;
    @BindView(R.id.geNdsHor)
    EditText geNdsHortAfter;
    @BindView(R.id.geNdsAxial)
    EditText geNdsAxialAfter;

    @BindView(R.id.txt_condition)
    TextView regreaseCondition;
    @BindView(R.id.txtTempDS)
    TextView txtTempDS;
    @BindView(R.id.txtTempNDS)
    TextView txtTempNDS;
    @BindView(R.id.spcondition)
    Spinner spCondition;

    @BindView(R.id.etRegrease)
    EditText etRegrease;
    @BindView(R.id.etKelengkapanMtr)
    EditText etKelengkapanMtr;
    @BindView(R.id.etKeterangan)
    EditText etKeterangan;

    @BindView(R.id.txtRegrease)
    TextView txtRegrease;
    @BindView(R.id.txtKelengkapanMtr)
    TextView txtKelengkapanMtr;
    @BindView(R.id.txtKeterangan)
    TextView txtKeterangan;
    @BindView(R.id.txtKondisi)
    TextView txtKondisi;

    private Contract.ActivityCallback mActivityCallback;

    public static BearingFragmentAfter newInstance() {
        BearingFragmentAfter instance = new BearingFragmentAfter();
        Bundle args = new Bundle();
        instance.setArguments(args);
        return instance;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_bearing_inspection_ed, container, false);
        setUnbinder(ButterKnife.bind(this, view));

        regreaseCondition.setText("Setelah regrease");
        txtTempDS.setVisibility(View.GONE);
        txtTempNDS.setVisibility(View.GONE);
        temperatureDS.setVisibility(View.GONE);
        temperatureNDS.setVisibility(View.GONE);
        etKelengkapanMtr.setVisibility(View.VISIBLE);
        txtKelengkapanMtr.setVisibility(View.VISIBLE);
        etKeterangan.setVisibility(View.VISIBLE);
        txtKeterangan.setVisibility(View.VISIBLE);
        spCondition.setVisibility(View.VISIBLE);
        txtKondisi.setVisibility(View.VISIBLE);
//        added by rama 08 Nov 2022
        String nomenclature = this.getArguments().getString("nomenclature");
        String subtipe = "1";
        String tipe = "1";
        Log.d("nomenclature",nomenclature);Log.d("subtipe",subtipe);Log.d("tipe",tipe);
        setprevinspection(nomenclature,tipe, subtipe);
//        end of added by rama
        EditText[] bearingEt = { geDsVertAfter, geDsHorAfter, geDsAxialAfter, geNdsVertAfter, geNdsAxialAfter, geNdsHortAfter};
        for(EditText et : bearingEt)
            et.addTextChangedListener(new bearingWatcher(et));

        EditText[] vibrasiEt = { vibrasiDsVert, vibrasiDsHor, vibrasiDsAxial, vibrasiNdsVerAfter, vibrasiNdsAxialAfter, vibrasiNdsHorAfter};
        for(EditText et : vibrasiEt)
            et.addTextChangedListener(new vibrasiWatcher(et));

        mActivityCallback = (Contract.ActivityCallback) getActivity();

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

        return view;
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
            if(!TextUtils.isNullOrEmpty(s.toString().trim())){
                Double value = Double.parseDouble(s.toString());
                if (value > 4)
                    mEditText.getBackground().setColorFilter(getResources().getColor(R.color.material_red_400), PorterDuff.Mode.SRC_ATOP);
                else if (value >= 2.1 && value <= 4)
                    mEditText.getBackground().setColorFilter(getResources().getColor(R.color.material_yellow_400), PorterDuff.Mode.SRC_ATOP);
                else if (value >= 0.1 && value <= 2)
                    mEditText.getBackground().setColorFilter(getResources().getColor(R.color.material_green_400), PorterDuff.Mode.SRC_ATOP);
                else
                    mEditText.getBackground().setColorFilter(getResources().getColor(R.color.ms_white_54_opacity), PorterDuff.Mode.SRC_ATOP);

                checkCondition(value, mEditText, 1);
            }
            else
            {
                checkCondition(0.0, mEditText, 1);
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
            if(!TextUtils.isNullOrEmpty(s.toString().trim())) {
                Double value = Double.parseDouble(s.toString());
                if (value > 4.5)
                    mEditText.getBackground().setColorFilter(getResources().getColor(R.color.material_red_400), PorterDuff.Mode.SRC_ATOP);
                else if (value >= 2.81 && value <= 4.5)
                    mEditText.getBackground().setColorFilter(getResources().getColor(R.color.material_orange_400), PorterDuff.Mode.SRC_ATOP);
                else if (value >= 1.41 && value <= 2.8)
                    mEditText.getBackground().setColorFilter(getResources().getColor(R.color.material_yellow_400), PorterDuff.Mode.SRC_ATOP);
                else
                    mEditText.getBackground().setColorFilter(getResources().getColor(R.color.ms_white_54_opacity), PorterDuff.Mode.SRC_ATOP);

                checkCondition(value, mEditText, 2);
            }
            else
            {
                checkCondition(0.0, mEditText, 2);
                mEditText.getBackground().setColorFilter(getResources().getColor(R.color.ms_white_54_opacity), PorterDuff.Mode.SRC_ATOP);
            }
        }
    }

    public void checkCondition(Double value, EditText key, int watch)
    {
        ArrayList<EditText> condPackage = new ArrayList<>();
        ArrayList<EditText> tampPackage = new ArrayList<>();

        tampPackage = AddInspectionActivity.conditionList;

        if(tampPackage != null)
            condPackage = tampPackage;

        if(condPackage.contains(key)){
            switch (watch) {
                case 1:
                    if (value > 4)
                        condPackage.add(key);
                    else
                        condPackage.remove(key);
                    break;
                case 2:
                    if (value > 4.5)
                        condPackage.add(key);
                    else
                        condPackage.remove(key);
                    break;
            }
        }
        else {
            switch (watch) {
                case 1:
                    if (value > 4)
                        condPackage.add(key);
                    break;
                case 2:
                    if (value > 4.5)
                        condPackage.add(key);
                    break;
            }
        }

        mActivityCallback.onSetCondition(condPackage, AddInspectionActivity.TYPE_MOTOR);
    }

    public boolean isConditionBad() {
        return !spCondition.getSelectedItem().toString().equals(conditionOptions.get(0));
    }

    @Override
    Bearing setDataToModel() {
        Bearing bearing = new Bearing();

        bearing.setKeterangan(etKeterangan.getText().toString());
        bearing.setKelengkapanMotor(etKelengkapanMtr.getText().toString());

        if(!vibrasiDsVert.getText().toString().isEmpty())
            bearing.setVibrasiDsVert_after(Double.parseDouble(vibrasiDsVert.getText().toString()));
        if(!vibrasiDsHor.getText().toString().isEmpty())
            bearing.setVibrasiDsHor_after(Double.parseDouble(vibrasiDsHor.getText().toString()));
        if(!vibrasiDsAxial.getText().toString().isEmpty())
            bearing.setVibrasiDsAxial_after(Double.parseDouble(vibrasiDsAxial.getText().toString()));

        if(!geDsVertAfter.getText().toString().isEmpty())
            bearing.setGeDsVert_after(Double.parseDouble(geDsVertAfter.getText().toString()));
        if(!geDsHorAfter.getText().toString().isEmpty())
            bearing.setGeDsHor_after(Double.parseDouble(geDsHorAfter.getText().toString()));
        if(!geDsAxialAfter.getText().toString().isEmpty())
            bearing.setGeDsAxial_after(Double.parseDouble(geDsAxialAfter.getText().toString()));

        if(!geNdsVertAfter.getText().toString().isEmpty())
            bearing.setGeNdsVert_after(Double.parseDouble(geNdsVertAfter.getText().toString()));
        if(!geNdsHortAfter.getText().toString().isEmpty())
            bearing.setGeNdsHor_after(Double.parseDouble(geNdsHortAfter.getText().toString()));
        if(!geNdsAxialAfter.getText().toString().isEmpty())
            bearing.setGeNdsAxial_after(Double.parseDouble(geNdsAxialAfter.getText().toString()));

        if(!vibrasiNdsVerAfter.getText().toString().isEmpty())
            bearing.setVibrasiNdsVert_after(Double.parseDouble(vibrasiNdsVerAfter.getText().toString()));
        if(!vibrasiNdsHorAfter.getText().toString().isEmpty())
            bearing.setVibrasiNdsHor_after(Double.parseDouble(vibrasiNdsHorAfter.getText().toString()));
        if(!vibrasiNdsAxialAfter.getText().toString().isEmpty())
            bearing.setVibrasiNdsAxial_after(Double.parseDouble(vibrasiNdsAxialAfter.getText().toString()));

        return bearing;
    }

    @Override
    InspectionDetail setCondition() {
        InspectionDetail inspectionCondition = new InspectionDetail();

        inspectionCondition.setCondition(isConditionBad() ? 0 : 1);
        return inspectionCondition;
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
        String errorMessage = null;
//
//        if (TextUtils.isNullOrEmpty(vibrasiDS.getText().toString())) {
//            vibrasiDS.setError(getString(R.string.error_field_vibrasi));
//            errorMessage = getString(R.string.alert_any_field_required);
//        } else {
//            vibrasiDS.setError(null);
//        }
//
//        if (TextUtils.isNullOrEmpty(geDS.getText().toString())) {
//            geDS.setError(getString(R.string.error_field_geds));
//            errorMessage = getString(R.string.alert_any_field_required);
//        } else {
//            geDS.setError(null);
//        }
//
//        if (TextUtils.isNullOrEmpty(temperatureDS.getText().toString())) {
//            temperatureDS.setError(getString(R.string.error_field_temperatureds));
//            errorMessage = getString(R.string.alert_any_field_required);
//        } else {
//            temperatureDS.setError(null);
//        }
//
//        if (TextUtils.isNullOrEmpty(vibrasiNDS.getText().toString())) {
//            vibrasiNDS.setError(getString(R.string.error_field_vibrasids));
//            errorMessage = getString(R.string.alert_any_field_required);
//        } else {
//            vibrasiNDS.setError(null);
//        }
//
//        if (TextUtils.isNullOrEmpty(geNDS.getText().toString())) {
//            geNDS.setError(getString(R.string.error_field_gends));
//            errorMessage = getString(R.string.alert_any_field_required);
//        } else {
//            geNDS.setError(null);
//        }
//
//        if (TextUtils.isNullOrEmpty(temperatureNDS.getText().toString())) {
//            temperatureNDS.setError(getString(R.string.error_field_temperatureNDS));
//            errorMessage = getString(R.string.alert_any_field_required);
//        } else {
//            temperatureNDS.setError(null);
//        }
//
//
//        Timber.d(errorMessage);

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

    }
    //    added by rama 07 nov 2022
    public void setprevinspection(String nomenclature,String tipe, String subtipe){
        mPrevIns = ServiceGenerator.getInstance().getRetrofit().create(MsoService.class).getPrevInspection(nomenclature,tipe, subtipe);
        mPrevIns.enqueue(new Callback<GetPrevInspectionResponse>() {
            @Override
            public void onResponse(Call<GetPrevInspectionResponse> call, Response<GetPrevInspectionResponse> response) {
                if (response.isSuccessful()) {
                    if (response.body() != null && response.body().isSuccess()) {
//                        AutonomosSupervisor.setValue(response.body().getData());
                        Log.d("GetPrevInspection2", response.body().getData().toString());
                        List<GetPrevInspection> data = response.body().getData();
                        String id, value;
                        for (int i=0;i<data.size();i++) {
                            id = data.get(i).getId();
                            value = " (prev : "+data.get(i).getValue()+")";
                            if(id.equals("27")) {
                                txtgEDSVert.setText(txtgEDSVert.getText().toString() + value);
                            }else if(id.equals("3")){
                                txtTempDS.setText(txtTempDS.getText().toString() + value);
                            }else if(id.equals("30")) {
                                txtgEDSHor.setText(txtgEDSHor.getText().toString() + value);
                            }else if(id.equals("32")) {
                                txtgEDSAxial.setText(txtgEDSAxial.getText().toString() + value);
                            }else if(id.equals("26")) {
                                txtVibDSVert.setText(txtVibDSVert.getText().toString() + value);
                            }else if(id.equals("33")) {
                                txtVibDSHor.setText(txtVibDSHor.getText().toString() + value);
                            }else if(id.equals("35")) {
                                txtVibDSAxial.setText(txtVibDSAxial.getText().toString() + value);
                            }else if(id.equals("6")) {
                                txtTempNDS.setText(txtTempNDS.getText().toString() + value);
                            }else if(id.equals("29")) {
                                txtgENDSVert.setText(txtgENDSVert.getText().toString() + value);
                            }else if(id.equals("31")) {
                                txtgENDSHor.setText(txtgENDSHor.getText().toString() + value);
                            }else if(id.equals("37")) {
                                txtgENDSAxial.setText(txtgENDSAxial.getText().toString() + value);
                            }else if(id.equals("28")) {
                                txtVibNDSVert.setText(txtVibNDSVert.getText().toString() + value);
                            }else if(id.equals("34")) {
                                txtVibNDSHor.setText(txtVibNDSHor.getText().toString() + value);
                            }else if(id.equals("36")) {
                                txtVibNDSAxial.setText(txtVibNDSAxial.getText().toString() + value);
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
