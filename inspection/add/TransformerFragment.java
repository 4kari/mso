package id.sisi.si.mso.ui.inspection.add;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
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
import id.sisi.si.mso.data.model.GetPrevInspection;
import id.sisi.si.mso.data.model.GetPrevInspectionResponse;
import id.sisi.si.mso.data.model.InspectionDetail;
import id.sisi.si.mso.data.model.Transformer;
import id.sisi.si.mso.data.model.picItem;
import id.sisi.si.mso.utils.TextUtils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

import static id.sisi.si.mso.R.layout.list_item_spinner;

/**
 * Created by durrrr on 11-Oct-17.
 * Email: cab.purnama@gmail.com
 */

public class TransformerFragment extends TechnicalFragment {
    //added by rama 08 nov 2022 untuk prev data inspeksi
    private Call<GetPrevInspectionResponse> mPrevIns;
    @BindView(R.id.txtTempTraf) TextView txtTempTraf;
    @BindView(R.id.txtTempKon) TextView txtTempKon;
    @BindView(R.id.txtAmperePrem_R) TextView txtAmperePrem_R;
    @BindView(R.id.txtAmperePrem_S) TextView txtAmperePrem_S;
    @BindView(R.id.txtAmperePremT) TextView txtAmperePremT;
    @BindView(R.id.txttempwinding) TextView txttempwinding;
    @BindView(R.id.txtOilTemp) TextView txtOilTemp;
    @BindView(R.id.txtOil_lvl) TextView txtOil_lvl;
    // end of added by rama
    final List<String> conditionOptions = Arrays.asList("GOOD", "BAD");

    @BindView(R.id.temperatureTrafo)
    EditText temperatureTrafo;
    @BindView(R.id.temperatureKoneksi)
    EditText temperatureKoneksi;
    @BindView(R.id.ampPremierR)
    EditText ampPremierR;
    @BindView(R.id.ampPremierS)
    EditText ampPremierS;
    @BindView(R.id.ampPremierT)
    EditText ampPremierT;
    @BindView(R.id.tempWinding)
    EditText tempWinding;
    @BindView(R.id.oilTemp)
    EditText oilTemp;
    @BindView(R.id.oilLevel)
    EditText oilLevel;
    @BindView(R.id.etNegativeList)
    EditText etNegativeList;
    @BindView(R.id.spcondition)
    Spinner spCondition;
    @BindView(R.id.txtKondisi)
    TextView txtKondisi;

    @BindView(R.id.spSilicaGell)
    Spinner spSilica;
    private Contract.ActivityCallback mActivityCallback;

    public static TransformerFragment newInstance() {
        TransformerFragment instance = new TransformerFragment();
        Bundle args = new Bundle();
        instance.setArguments(args);
        return instance;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_transformer, container, false);
        setUnbinder(ButterKnife.bind(this, view));

        mActivityCallback = (Contract.ActivityCallback) getActivity();

        spCondition.setVisibility(View.VISIBLE);
        txtKondisi.setVisibility(View.VISIBLE);

        ArrayAdapter<String> spAdapter = new ArrayAdapter<>(getActivity(),
                list_item_spinner, conditionOptions);
        spSilica.setAdapter(spAdapter);
        spSilica.setSelection(0);

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
//        added by rama 08 Nov 2022
        String nomenclature = this.getArguments().getString("nomenclature");
        String subtipe = "1";
        String tipe = "3";
//        Log.d("nomenclature",nomenclature);Log.d("subtipe",subtipe);Log.d("tipe",tipe);
        setprevinspection(nomenclature,tipe, subtipe);
//        end of added by rama
        return view;
    }


    @Override
    Transformer setDataToModel() {
        Transformer transformer = new Transformer();

        if(!TextUtils.isNullOrEmpty(temperatureKoneksi.getText().toString()))
            transformer.setTemperaturKoneksi(Double.parseDouble(temperatureKoneksi.getText().toString()));
        if(!TextUtils.isNullOrEmpty(temperatureTrafo.getText().toString()))
            transformer.setTemperaturTrafo(Double.parseDouble(temperatureTrafo.getText().toString()));
        if(!TextUtils.isNullOrEmpty(ampPremierR.getText().toString()))
            transformer.setAmperePremier_r(Double.parseDouble(ampPremierR.getText().toString()));
        if(!TextUtils.isNullOrEmpty(ampPremierS.getText().toString()))
            transformer.setAmperePremier_s(Double.parseDouble(ampPremierS.getText().toString()));
        if(!TextUtils.isNullOrEmpty(ampPremierT.getText().toString()))
            transformer.setAmperePremier_t(Double.parseDouble(ampPremierT.getText().toString()));
        if(!TextUtils.isNullOrEmpty(tempWinding.getText().toString()))
            transformer.setTempWinding(Double.parseDouble(tempWinding.getText().toString()));
        if(!TextUtils.isNullOrEmpty(oilTemp.getText().toString()))
            transformer.setOilTemp(Double.parseDouble(oilTemp.getText().toString()));
        if(!TextUtils.isNullOrEmpty(oilLevel.getText().toString()))
            transformer.setOilLevel(Double.parseDouble(oilLevel.getText().toString()));
        if(!TextUtils.isNullOrEmpty(String.valueOf(spSilica.getSelectedItemPosition())))
            transformer.setSilicaGell(spSilica.getSelectedItemPosition());
        if(!TextUtils.isNullOrEmpty(etNegativeList.getText().toString()))
            transformer.setNegativeList(etNegativeList.getText().toString());

        return transformer;
    }

    @Override
    InspectionDetail setCondition() {
        InspectionDetail inspectionCondition = new InspectionDetail();

        inspectionCondition.setCondition(isConditionBad() ? 0 : 1);
        return inspectionCondition;
    }

    @Override
    void setConditions(int i) {

    }

    @Override
    void updateMember(List<picItem> picItems) {

    }

    public boolean isConditionBad() {
        return !spCondition.getSelectedItem().toString().equals(conditionOptions.get(0));
    }


    @Nullable
    @Override
    public VerificationError verifyStep() {
        String errorMessage = null;

        if (TextUtils.isNullOrEmpty(temperatureKoneksi.getText().toString())) {
            temperatureKoneksi.setError(getString(R.string.error_field_temperatureKoneksi));
            errorMessage = getString(R.string.alert_any_field_required);
        } else {
            temperatureKoneksi.setError(null);
        }

        if (TextUtils.isNullOrEmpty(temperatureTrafo.getText().toString())) {
            temperatureTrafo.setError(getString(R.string.error_field_temperatureTrafo));
            errorMessage = getString(R.string.alert_any_field_required);
        } else {
            temperatureTrafo.setError(null);
        }


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
    //    added by rama 09 Nov 2022
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
                            if(id.equals("11")) {
                                txtTempTraf.setText(txtTempTraf.getText().toString() + value);
                            }else if(id.equals("12")){
                                txtTempKon.setText(txtTempKon.getText().toString() + value);
                            }else if(id.equals("41")) {
                                txtAmperePrem_R.setText(txtAmperePrem_R.getText().toString() + value);
                            }else if(id.equals("42")){
                                txtAmperePrem_S.setText(txtAmperePrem_S.getText().toString() + value);
                            }else if(id.equals("43")){
                                txtAmperePremT.setText(txtAmperePremT.getText().toString() + value);
                            }else if(id.equals("44")) {
                                txttempwinding.setText(txttempwinding.getText().toString() + value);
                            }else if(id.equals("45")) {
                                txtOilTemp.setText(txtOilTemp.getText().toString() + value);
                            }else if(id.equals("46")){
                                txtOil_lvl.setText(txtOil_lvl.getText().toString() + value);
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
