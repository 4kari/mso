package id.sisi.si.mso.ui.inspection.add;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
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
import id.sisi.si.mso.data.model.BeltWeighter;
import id.sisi.si.mso.data.model.GetPrevInspection;
import id.sisi.si.mso.data.model.GetPrevInspectionResponse;
import id.sisi.si.mso.data.model.InspectionDetail;
import id.sisi.si.mso.data.model.picItem;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

/**
 * Created by durrrr on 11-Oct-17.
 * Email: cab.purnama@gmail.com
 */
public class BeltWeighterFragment extends TechnicalFragment {
    //added by rama 08 nov 2022 untuk prev data inspeksi
    private Call<GetPrevInspectionResponse> mPrevIns;
    @BindView(R.id.txtqR) TextView txtqR;
    @BindView(R.id.txtjmlCounter) TextView txtjmlCounter;
    @BindView(R.id.txtspancor) TextView txtspancor;
    // end of added by rama
    final List<String> conditionOptions = Arrays.asList("GOOD", "BAD");
    final List<String> activityOptions = Arrays.asList("Order", "Routine Check");
    @BindView(R.id.spActivity)
    Spinner spActivity;
    @BindView(R.id.qR)
    EditText qR;
    @BindView(R.id.counter)
    EditText counterCount;
    @BindView(R.id.spanCorrection)
    EditText spanCorrection;
    @BindView(R.id.spcondition)
    Spinner spCondition;
    @BindView(R.id.txtKondisi)
    TextView txtKondisi;

    private Contract.ActivityCallback mActivityCallback;

    public static BeltWeighterFragment newInstance() {
        BeltWeighterFragment instance = new BeltWeighterFragment();
        Bundle args = new Bundle();
        instance.setArguments(args);
        return instance;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_belt_weighter, container, false);
        setUnbinder(ButterKnife.bind(this, view));

        spActivity.setAdapter(DataHelper.createAdapter(getActivity(), activityOptions));
        mActivityCallback = (Contract.ActivityCallback) getActivity();

        spCondition.setVisibility(View.VISIBLE);
        txtKondisi.setVisibility(View.VISIBLE);
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
        String tipe = "2";
        Log.d("nomenclature",nomenclature);Log.d("subtipe",subtipe);Log.d("tipe",tipe);
        setprevinspection(nomenclature,tipe, subtipe);
//        end of added by rama
        return view;
    }

    public boolean isConditionBad() {
        return !spCondition.getSelectedItem().toString().equals(conditionOptions.get(0));
    }

    @Override
    BeltWeighter setDataToModel() {
        BeltWeighter beltWeighter = new BeltWeighter();

        beltWeighter.setAktivitas(spActivity.getSelectedItem().toString());
        if(!qR.getText().toString().isEmpty())
            beltWeighter.setQr(Double.parseDouble(qR.getText().toString()));
        if(!counterCount.getText().toString().isEmpty())
            beltWeighter.setJumlahCounter(Double.parseDouble(counterCount.getText().toString()));
        if(!spanCorrection.getText().toString().isEmpty())
            beltWeighter.setSpanCorrection(Double.parseDouble(spanCorrection.getText().toString()));

        return beltWeighter;
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
//    added by rama 09 Nov 2022
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
                        if(id.equals("8")) {
                            txtqR.setText(txtqR.getText().toString() + value);
                        }else if(id.equals("9")){
                            txtjmlCounter.setText(txtjmlCounter.getText().toString() + value);
                        }else if(id.equals("10")) {
                            txtspancor.setText(txtspancor.getText().toString() + value);
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
