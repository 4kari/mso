package id.sisi.si.mso.ui.inspection.add;

import android.content.SharedPreferences;
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
import android.widget.EditText;
import android.widget.TextView;

import com.stepstone.stepper.VerificationError;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import id.sisi.si.mso.R;
import id.sisi.si.mso.data.api.MsoService;
import id.sisi.si.mso.data.api.ServiceGenerator;
import id.sisi.si.mso.data.model.Bearing;
import id.sisi.si.mso.data.model.GetPrevInspectionResponse;
import id.sisi.si.mso.data.model.InspectionDetail;
import id.sisi.si.mso.data.model.picItem;
import id.sisi.si.mso.data.model.GetPrevInspection;
import id.sisi.si.mso.ui.inspection.add.Contract.ActivityCallback;
import id.sisi.si.mso.utils.TextUtils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

/**
 * Created by durrrr on 11-Oct-17.
 * Email: cab.purnama@gmail.com
 */
public class BearingFragment extends TechnicalFragment{
    public static final String CONDITION_KEY = "condition";

    //added by rama 08 nov 2022 untuk prev data inspeksi
    private Call<GetPrevInspectionResponse> mPrevIns;
    @BindView(R.id.txtgEDSVert) TextView txtgEDSVert;
    @BindView(R.id.txtTempDS) TextView txtTempDS;
    @BindView(R.id.txtgEDSHor) TextView txtgEDSHor;
    @BindView(R.id.txtgEDSAxial) TextView txtgEDSAxial;
    @BindView(R.id.txtVibDSVert) TextView txtVibDSVert;
    @BindView(R.id.txtVibDSHor) TextView txtVibDSHor;
    @BindView(R.id.txtVibDSAxial) TextView txtVibDSAxial;
    @BindView(R.id.txtTempNDS) TextView txtTempNDS;
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
    EditText geDsVert;
    @BindView(R.id.geDsHor)
    EditText geDsHor;
    @BindView(R.id.geDsAxial)
    EditText geDsAxial;

    @BindView(R.id.temperatureDS)
    EditText temperatureDS;
    @BindView(R.id.temperatureNds)
    EditText temperatureNDS;

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

    @BindView(R.id.etRegrease)
    EditText etRegrease;
    @BindView(R.id.etRegreaseNDE)
    EditText etRegreaseNDE;
    @BindView(R.id.etKelengkapanMtr)
    EditText etKelengkapanMtr;
    @BindView(R.id.etKeterangan)
    EditText etKeterangan;

    @BindView(R.id.txtRegrease)
    TextView txtRegrease;
    @BindView(R.id.txtRegreaseNDE)
    TextView txtRegreaseNDE;
    @BindView(R.id.txtKelengkapanMtr)
    TextView txtKelengkapanMtr;
    @BindView(R.id.txtKeterangan)
    TextView txtKeterangan;

    SharedPreferences sp;
    SharedPreferences.Editor editor;
    private ActivityCallback mActivityCallback;

    public static BearingFragment newInstance() {
        BearingFragment instance = new BearingFragment();
        Bundle args = new Bundle();
        instance.setArguments(args);
        return instance;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_bearing_inspection_ed, container, false);
        setUnbinder(ButterKnife.bind(this, view));

        mActivityCallback = (Contract.ActivityCallback) getActivity();

        txtRegrease.setVisibility(View.VISIBLE);
        etRegrease.setVisibility(View.VISIBLE);

        txtRegreaseNDE.setVisibility(View.VISIBLE);
        etRegreaseNDE.setVisibility(View.VISIBLE);

        temperatureDS.addTextChangedListener(new temperatureWatcher(temperatureDS));
        temperatureNDS.addTextChangedListener(new temperatureWatcher(temperatureNDS));

//        added by rama 08 Nov 2022
        String nomenclature = this.getArguments().getString("nomenclature");
        String tipe = "1";
        String subtipe = "1";
        Log.d("nomenclature",nomenclature);Log.d("subtipe",subtipe);Log.d("tipe",tipe);
        setprevinspection(nomenclature,tipe, subtipe);
//        end of added by rama
        EditText[] bearingEt = { geDsVert, geDsHor, geDsAxial, geNdsVert, geNdsAxial, geNdsHor};
        for(EditText et : bearingEt)
            et.addTextChangedListener(new bearingWatcher(et));

        EditText[] vibrasiEt = { vibrasiDsVert, vibrasiDsHor, vibrasiDsAxial, vibrasiNdsVer, vibrasiNdsAxial, vibrasiNdsHor};
        for(EditText et : vibrasiEt)
            et.addTextChangedListener(new vibrasiWatcher(et));

        return view;
    }


    private class temperatureWatcher implements TextWatcher {
        private EditText mEditText;

        public temperatureWatcher(EditText e) {
            mEditText = e;
        }

        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        public void afterTextChanged(Editable s) {
            if(!TextUtils.isNullOrEmpty(s.toString().trim())) {
                Double value = Double.parseDouble(s.toString());
                if (value > 70) {
                    mEditText.getBackground().setColorFilter(getResources().getColor(R.color.material_red_400), PorterDuff.Mode.SRC_ATOP);
                }
                else if (value >= 60.1 && value <= 70) {
                    mEditText.getBackground().setColorFilter(getResources().getColor(R.color.material_orange_400), PorterDuff.Mode.SRC_ATOP);
                }
                else if (value >= 45.1 && value <= 60) {
                    mEditText.getBackground().setColorFilter(getResources().getColor(R.color.material_yellow_400), PorterDuff.Mode.SRC_ATOP);
                }
                else {
                    mEditText.getBackground().setColorFilter(getResources().getColor(R.color.ms_white_54_opacity), PorterDuff.Mode.SRC_ATOP);
                }

                //condition bad/good checked
                checkCondition(value,mEditText, 0);
            }
            else
            {
                checkCondition(0.0, mEditText, 0);
                mEditText.getBackground().setColorFilter(getResources().getColor(R.color.ms_white_54_opacity), PorterDuff.Mode.SRC_ATOP);
            }

        }
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
            if(!TextUtils.isNullOrEmpty(s.toString().trim())) {
                Double value = Double.parseDouble(s.toString());
                if (value > 4) {
                    mEditText.getBackground().setColorFilter(getResources().getColor(R.color.material_red_400), PorterDuff.Mode.SRC_ATOP);
                }
                else if (value >= 2.1 && value <= 4){
                    mEditText.getBackground().setColorFilter(getResources().getColor(R.color.material_yellow_400), PorterDuff.Mode.SRC_ATOP);
                }
                else if (value >= 0.1 && value <= 2){
                    mEditText.getBackground().setColorFilter(getResources().getColor(R.color.material_green_400), PorterDuff.Mode.SRC_ATOP);
                }
                else{
                    mEditText.getBackground().setColorFilter(getResources().getColor(R.color.ms_white_54_opacity), PorterDuff.Mode.SRC_ATOP);
                }

                //condition bad/good checked
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
                if (value > 4.5) {
                    mEditText.getBackground().setColorFilter(getResources().getColor(R.color.material_red_400), PorterDuff.Mode.SRC_ATOP);
                }
                else if (value >= 2.81 && value <= 4.5){
                    mEditText.getBackground().setColorFilter(getResources().getColor(R.color.material_orange_400), PorterDuff.Mode.SRC_ATOP);
                }
                else if (value >= 1.41 && value <= 2.8){
                    mEditText.getBackground().setColorFilter(getResources().getColor(R.color.material_yellow_400), PorterDuff.Mode.SRC_ATOP);
                }
                else{
                    mEditText.getBackground().setColorFilter(getResources().getColor(R.color.ms_white_54_opacity), PorterDuff.Mode.SRC_ATOP);
                }

                //condition bad/good checked
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
                case 0:
                    if (value > 70)
                        condPackage.add(key);
                    else
                        condPackage.remove(key);
                    break;
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
                case 0:
                    if (value > 70)
                        condPackage.add(key);
                    break;
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

    @Override
    Bearing setDataToModel() {
        Bearing bearing = new Bearing();

        if(!TextUtils.isNullOrEmpty(etRegrease.getText().toString()))
            bearing.setRegrease(Integer.parseInt(etRegrease.getText().toString()));
        if(!TextUtils.isNullOrEmpty(etRegreaseNDE.getText().toString()))
            bearing.setRegrease_nde(Integer.parseInt(etRegreaseNDE.getText().toString()));



        if(!vibrasiDsVert.getText().toString().isEmpty())
            bearing.setVibrasiDs(Double.parseDouble(vibrasiDsVert.getText().toString()));
        if(!vibrasiDsHor.getText().toString().isEmpty())
            bearing.setVibrasiDsHor_before(Double.parseDouble(vibrasiDsHor.getText().toString()));
        if(!vibrasiDsAxial.getText().toString().isEmpty())
            bearing.setVibrasiDsAxial_before(Double.parseDouble(vibrasiDsAxial.getText().toString()));

        if(!geDsVert.getText().toString().isEmpty())
            bearing.setGeDs(Double.parseDouble(geDsVert.getText().toString()));
        if(!geDsHor.getText().toString().isEmpty())
            bearing.setGedsHor_before(Double.parseDouble(geDsHor.getText().toString()));
        if(!geDsAxial.getText().toString().isEmpty())
            bearing.setGedsAxial_before(Double.parseDouble(geDsAxial.getText().toString()));

        if(!temperatureDS.getText().toString().isEmpty())
            bearing.setTemperaturDs(Double.parseDouble(temperatureDS.getText().toString()));
        if(!temperatureNDS.getText().toString().isEmpty())
            bearing.setTemperaturNds(Double.parseDouble(temperatureNDS.getText().toString()));

        if(!geNdsVert.getText().toString().isEmpty())
            bearing.setGeNds(Double.parseDouble(geNdsVert.getText().toString()));
        if(!geNdsHor.getText().toString().isEmpty())
            bearing.setGeNdsHor_before(Double.parseDouble(geNdsHor.getText().toString()));
        if(!geNdsAxial.getText().toString().isEmpty())
            bearing.setGeNdsAxial_before(Double.parseDouble(geNdsAxial.getText().toString()));

        if(!vibrasiNdsVer.getText().toString().isEmpty())
            bearing.setVibrasiNds(Double.parseDouble(vibrasiNdsVer.getText().toString()));
        if(!vibrasiNdsHor.getText().toString().isEmpty())
            bearing.setVibrasiNdsHor_before(Double.parseDouble(vibrasiNdsHor.getText().toString()));
        if(!vibrasiNdsAxial.getText().toString().isEmpty())
            bearing.setVibrasiNdsAxial_before(Double.parseDouble(vibrasiNdsAxial.getText().toString()));

        return bearing;
    }

    @Override
    InspectionDetail setCondition() {
        return null;
    }

    @Override
    void setConditions(int i) {

    }

    @Override
    void updateMember(List<picItem> picItems) {

    }

    @Nullable
    @Override
    public VerificationError verifyStep() {
        String errorMessage = null;

        Timber.d(errorMessage);

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
                            if(id.equals("2")) {
                                txtgEDSVert.setText(txtgEDSVert.getText().toString() + value);
                            }else if(id.equals("3")){
                                txtTempDS.setText(txtTempDS.getText().toString() + value);
                            }else if(id.equals("18")) {
                                txtgEDSHor.setText(txtgEDSHor.getText().toString() + value);
                            }else if(id.equals("20")) {
                                txtgEDSAxial.setText(txtgEDSAxial.getText().toString() + value);
                            }else if(id.equals("1")) {
                                txtVibDSVert.setText(txtVibDSVert.getText().toString() + value);
                            }else if(id.equals("21")) {
                                txtVibDSHor.setText(txtVibDSHor.getText().toString() + value);
                            }else if(id.equals("23")) {
                                txtVibDSAxial.setText(txtVibDSAxial.getText().toString() + value);
                            }else if(id.equals("6")) {
                                txtTempNDS.setText(txtTempNDS.getText().toString() + value);
                            }else if(id.equals("5")) {
                                txtgENDSVert.setText(txtgENDSVert.getText().toString() + value);
                            }else if(id.equals("19")) {
                                txtgENDSHor.setText(txtgENDSHor.getText().toString() + value);
                            }else if(id.equals("25")) {
                                txtgENDSAxial.setText(txtgENDSAxial.getText().toString() + value);
                            }else if(id.equals("4")) {
                                txtVibNDSVert.setText(txtVibNDSVert.getText().toString() + value);
                            }else if(id.equals("22")) {
                                txtVibNDSHor.setText(txtVibNDSHor.getText().toString() + value);
                            }else if(id.equals("24")) {
                                txtVibNDSAxial.setText(txtVibNDSAxial.getText().toString() + value);
                            }else if(id.equals("38")) {
                                txtRegrease.setText(txtRegrease.getText().toString() + value);
                            }else if(id.equals("195")) {
                                txtRegreaseNDE.setText(txtRegreaseNDE.getText().toString() + value);
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
