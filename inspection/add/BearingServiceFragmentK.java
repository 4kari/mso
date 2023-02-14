package id.sisi.si.mso.ui.inspection.add;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.flexbox.FlexboxLayout;
import com.stepstone.stepper.VerificationError;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import fisk.chipcloud.ChipCloud;
import fisk.chipcloud.ChipCloudConfig;
import fisk.chipcloud.ChipDeletedListener;
import id.sisi.si.mso.R;
import id.sisi.si.mso.data.DataHelper;
import id.sisi.si.mso.data.api.MsoService;
import id.sisi.si.mso.data.api.ServiceGenerator;
import id.sisi.si.mso.data.model.Bearing;
import id.sisi.si.mso.data.model.GetPrevInspection;
import id.sisi.si.mso.data.model.GetPrevInspectionResponse;
import id.sisi.si.mso.data.model.InspectionDetail;
import id.sisi.si.mso.data.model.TechnicalModel;
import id.sisi.si.mso.data.model.picItem;
import id.sisi.si.mso.ui.widget.SearchableDialog;
import id.sisi.si.mso.utils.NetworkUtil;
import id.sisi.si.mso.utils.TextUtils;
import io.realm.RealmList;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Luhur on 2/15/2019.
 */

public class BearingServiceFragmentK extends TechnicalFragment
        implements SearchableDialog.SearchableItemCallback, View.OnTouchListener{
    //added by rama 08 nov 2022 untuk prev data inspeksi
    private Call<GetPrevInspectionResponse> mPrevIns;
    @BindView(R.id.txtA1) TextView txtA1;
    @BindView(R.id.txtA2) TextView txtA2;
    @BindView(R.id.txtB1) TextView txtB1;
    @BindView(R.id.txtB2) TextView txtB2;
    @BindView(R.id.txtB3) TextView txtB3;
    @BindView(R.id.txtC1) TextView txtC1;
    @BindView(R.id.txtC2) TextView txtC2;
    @BindView(R.id.txtC3) TextView txtC3;
    @BindView(R.id.txtD1) TextView txtD1;
    @BindView(R.id.txtD2) TextView txtD2;
    @BindView(R.id.txtD3) TextView txtD3;
    @BindView(R.id.txtE1) TextView txtE1;
    @BindView(R.id.txtE2) TextView txtE2;
    @BindView(R.id.txtE3) TextView txtE3;
    @BindView(R.id.txtF1) TextView txtF1;
    @BindView(R.id.txtF2) TextView txtF2;
    @BindView(R.id.txtF3) TextView txtF3;

    @BindView(R.id.txtG1) TextView txtG1;
    @BindView(R.id.txtG2) TextView txtG2;
    @BindView(R.id.txtH1) TextView txtH1;
    @BindView(R.id.txtH2) TextView txtH2;
    @BindView(R.id.txtI1) TextView txtI1;
    @BindView(R.id.txtI2) TextView txtI2;
    @BindView(R.id.txtJ1) TextView txtJ1;
    @BindView(R.id.txtJ2) TextView txtJ2;
    @BindView(R.id.txtK1) TextView txtK1;
    @BindView(R.id.txtK2) TextView txtK2;
    @BindView(R.id.txtL1) TextView txtL1;
    @BindView(R.id.txtL2) TextView txtL2;
    @BindView(R.id.txtM1) TextView txtM1;
    @BindView(R.id.txtM2) TextView txtM2;

    @BindView(R.id.txtJmlChg) TextView txtJmlChg;
    @BindView(R.id.txtRunH) TextView txtRunH;
    @BindView(R.id.txtRotor) TextView txtRotor;
    @BindView(R.id.txtStator) TextView txtStator;
    // end of added by rama
    final List<String> conditionOptions = Arrays.asList("GOOD", "BAD");
    @BindView(R.id.spPoint)
    Spinner spPoint;
    @BindView(R.id.etPoint)
    EditText etPoint;
    @BindView(R.id.etA1)
    EditText etA1;
    @BindView(R.id.etA2)
    EditText etA2;
    @BindView(R.id.etB1)
    EditText etB1;
    @BindView(R.id.etB2)
    EditText etB2;
    @BindView(R.id.etB3)
    EditText etB3;
    @BindView(R.id.etC1)
    EditText etC1;
    @BindView(R.id.etC2)
    EditText etC2;
    @BindView(R.id.etC3)
    EditText etC3;
    @BindView(R.id.etD1)
    EditText etD1;
    @BindView(R.id.etD2)
    EditText etD2;
    @BindView(R.id.etD3)
    EditText etD3;
    @BindView(R.id.etE1)
    EditText etE1;
    @BindView(R.id.etE2)
    EditText etE2;
    @BindView(R.id.etE3)
    EditText etE3;
    @BindView(R.id.etF1)
    EditText etF1;
    @BindView(R.id.etF2)
    EditText etF2;
    @BindView(R.id.etF3)
    EditText etF3;
    @BindView(R.id.etG1)
    EditText etG1;
    @BindView(R.id.etG2)
    EditText etG2;
    @BindView(R.id.etH1)
    EditText etH1;
    @BindView(R.id.etH2)
    EditText etH2;
    @BindView(R.id.etI1)
    EditText etI1;
    @BindView(R.id.etI2)
    EditText etI2;
    @BindView(R.id.etJ1)
    EditText etJ1;
    @BindView(R.id.etJ2)
    EditText etJ2;
    @BindView(R.id.etK1)
    EditText etK1;
    @BindView(R.id.etK2)
    EditText etK2;
    @BindView(R.id.etL1)
    EditText etL1;
    @BindView(R.id.etL2)
    EditText etL2;
    @BindView(R.id.etM1)
    EditText etM1;
    @BindView(R.id.etM2)
    EditText etM2;
    @BindView(R.id.etJmlChg)
    EditText etJmlPenggantian;
    @BindView(R.id.cbFilter)
    CheckBox cbFilter;
    @BindView(R.id.etRunHour)
    EditText etRunHour;
    @BindView(R.id.etRotor)
    EditText etRotor;
    @BindView(R.id.etStator)
    EditText etStator;
    @BindView(R.id.etKeteranganMMV)
    EditText etKeteranganMMV;
    @BindView(R.id.fl_members)
    FlexboxLayout flMembers;
    @BindView(R.id.spCondition)
    Spinner spCondition;

    private ChipCloud memberChipCloud;
    private SearchableDialog mMembersDialog;
    private RealmList<String> mChoosedMembers;

    private Contract.getMemberCallback mActivityCallback;

    public static BearingServiceFragmentK newInstance() {
        BearingServiceFragmentK instance = new BearingServiceFragmentK();
        Bundle args = new Bundle();
        instance.setArguments(args);
        return instance;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_motor_service, container, false);
        setUnbinder(ButterKnife.bind(this, view));

        mActivityCallback = (Contract.getMemberCallback) getActivity();
        initView();
//        added by rama 08 Nov 2022
        String nomenclature = this.getArguments().getString("nomenclature");
        String subtipe = "3";
        String tipe = "1";
//        Log.d("nomenclature",nomenclature);Log.d("subtipe",subtipe);Log.d("tipe",tipe);
        setprevinspection(nomenclature,tipe, subtipe);
//        end of added by rama
        return view;
    }

    private void initView() {
        if (!NetworkUtil.isConnected())
            flMembers.setClickable(false);

        etPoint.setText("K");
        mMembersDialog = new SearchableDialog(getActivity(), this, null, true);
        mMembersDialog.setHint("Masukkan nama member");

        ChipCloudConfig config = new ChipCloudConfig()
                .selectMode(ChipCloud.SelectMode.none)
                .uncheckedChipColor(ContextCompat.getColor(getActivity(), R.color.material_light_yellow_700))
                .uncheckedTextColor(ContextCompat.getColor(getActivity(), R.color.material_grey_700))
                .showClose(Color.parseColor("#a6a6a6"))
                .useInsetPadding(true);

        memberChipCloud = new ChipCloud(getActivity(), flMembers, config);

        if (mChoosedMembers == null)
            mChoosedMembers = new RealmList<String>();

        TextView memberHint = new TextView(getActivity());
        memberHint.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        memberHint.setText("Masukkan nama member");
        memberHint.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        memberHint.setTextColor(ContextCompat.getColor(getActivity(), R.color.material_grey_400));
        flMembers.addView(memberHint);

        memberChipCloud.setDeleteListener(new ChipDeletedListener() {
            @Override
            public void chipDeleted(int i, String s) {
                mChoosedMembers.remove(mChoosedMembers.get(i));

                if (mChoosedMembers.size() == 0) {
                    TextView memberHint = new TextView(getActivity());
                    memberHint.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    memberHint.setText("Masukkan nama member");
                    memberHint.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
                    memberHint.setTextColor(ContextCompat.getColor(getActivity(), R.color.material_grey_400));
                    flMembers.addView(memberHint);
                }
            }
        });
        flMembers.setOnTouchListener(this);

        EditText[] bearingEt = { etA1, etA2, etB1, etB2, etB3, etC1, etC2, etC3, etD1, etD2, etD3, etE1, etE2, etE3, etF1, etF2, etF3, etG1, etG2, etH1, etH2, etI1, etI2, etJ1, etJ2, etK1, etK2, etL1, etL2, etM1, etM2};
        for(EditText et : bearingEt)
            et.addTextChangedListener(new valueWatcher(et));

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
    }

    @Override
    TechnicalModel setDataToModel() {
        Bearing bearing = new Bearing();

        bearing.setKeteranganMMV(etKeteranganMMV.getText().toString());
        //bearing.setPoint(spPoint.getSelectedItem().toString());

        if(!etA1.getText().toString().isEmpty())
            bearing.setA1(Double.parseDouble(etA1.getText().toString()));
        if(!etA2.getText().toString().isEmpty())
            bearing.setA2(Double.parseDouble(etA2.getText().toString()));
        if(!etB1.getText().toString().isEmpty())
            bearing.setB1(Double.parseDouble(etB1.getText().toString()));
        if(!etB2.getText().toString().isEmpty())
            bearing.setB2(Double.parseDouble(etB2.getText().toString()));
        if(!etB3.getText().toString().isEmpty())
            bearing.setB3(Double.parseDouble(etB3.getText().toString()));
        if(!etC1.getText().toString().isEmpty())
            bearing.setC1(Double.parseDouble(etC1.getText().toString()));
        if(!etC2.getText().toString().isEmpty())
            bearing.setC2(Double.parseDouble(etC2.getText().toString()));
        if(!etC3.getText().toString().isEmpty())
            bearing.setC3(Double.parseDouble(etC3.getText().toString()));
        if(!etD1.getText().toString().isEmpty())
            bearing.setD1(Double.parseDouble(etD1.getText().toString()));
        if(!etD2.getText().toString().isEmpty())
            bearing.setD2(Double.parseDouble(etD2.getText().toString()));
        if(!etD3.getText().toString().isEmpty())
            bearing.setD3(Double.parseDouble(etD3.getText().toString()));
        if(!etE1.getText().toString().isEmpty())
            bearing.setE1(Double.parseDouble(etE1.getText().toString()));
        if(!etE2.getText().toString().isEmpty())
            bearing.setE2(Double.parseDouble(etE2.getText().toString()));
        if(!etE3.getText().toString().isEmpty())
            bearing.setE3(Double.parseDouble(etE3.getText().toString()));
        if(!etF1.getText().toString().isEmpty())
            bearing.setF1(Double.parseDouble(etF1.getText().toString()));
        if(!etF2.getText().toString().isEmpty())
            bearing.setF2(Double.parseDouble(etF2.getText().toString()));
        if(!etF3.getText().toString().isEmpty())
            bearing.setF3(Double.parseDouble(etF3.getText().toString()));
        if(!etG1.getText().toString().isEmpty())
            bearing.setG1(Double.parseDouble(etG1.getText().toString()));
        if(!etG2.getText().toString().isEmpty())
            bearing.setG2(Double.parseDouble(etG2.getText().toString()));
        if(!etH1.getText().toString().isEmpty())
            bearing.setH1(Double.parseDouble(etH1.getText().toString()));
        if(!etH2.getText().toString().isEmpty())
            bearing.setH2(Double.parseDouble(etH2.getText().toString()));
        if(!etI1.getText().toString().isEmpty())
            bearing.setI1(Double.parseDouble(etI2.getText().toString()));
        if(!etI2.getText().toString().isEmpty())
            bearing.setI2(Double.parseDouble(etI2.getText().toString()));
        if(!etJ1.getText().toString().isEmpty())
            bearing.setJ1(Double.parseDouble(etJ1.getText().toString()));
        if(!etJ2.getText().toString().isEmpty())
            bearing.setJ2(Double.parseDouble(etJ2.getText().toString()));
        if(!etK1.getText().toString().isEmpty())
            bearing.setK1(Double.parseDouble(etK1.getText().toString()));
        if(!etK2.getText().toString().isEmpty())
            bearing.setK2(Double.parseDouble(etK2.getText().toString()));
        if(!etL1.getText().toString().isEmpty())
            bearing.setL1(Double.parseDouble(etL1.getText().toString()));
        if(!etL2.getText().toString().isEmpty())
            bearing.setL2(Double.parseDouble(etL2.getText().toString()));
        if(!etM1.getText().toString().isEmpty())
            bearing.setM1(Double.parseDouble(etM1.getText().toString()));
        if(!etM2.getText().toString().isEmpty())
            bearing.setM2(Double.parseDouble(etM2.getText().toString()));
        if(!TextUtils.isNullOrEmpty(etJmlPenggantian.getText().toString()))
            bearing.setJmlPenggantian(Integer.parseInt(etJmlPenggantian.getText().toString()));
        bearing.setFilter(cbFilter.isChecked()? 1 : 0);
        if(!etRunHour.getText().toString().isEmpty())
            bearing.setRunHours(Double.parseDouble(etRunHour.getText().toString()));
        if(!etRotor.getText().toString().isEmpty())
            bearing.setRotor(Double.parseDouble(etRotor.getText().toString()));
        if(!etStator.getText().toString().isEmpty())
            bearing.setStator(Double.parseDouble(etStator.getText().toString()));

        StringBuilder memberConcated = new StringBuilder();
        for (String o : mChoosedMembers){
            if (memberConcated.length() > 0) memberConcated.append( "," );
            memberConcated.append(o);
        }

        bearing.setMembers(memberConcated.toString());
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
        mMembersDialog.updateDataset(picItems);
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

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if(event.getAction() == MotionEvent.ACTION_UP){
            switch (v.getId()) {
                case R.id.fl_members:
                    mMembersDialog.show();
                    return true;
                default:
                    return false;
            }
        }
        return false;
    }

    @Override
    public void onSearchItem(String query, String tag) {
        if (!TextUtils.isNullOrEmpty(query))
            mActivityCallback.onSearchItem(query, "K");
        else {
            mMembersDialog.updateDataset(Collections.EMPTY_LIST);
        }
    }

    @Override
    public void onSearchableItemSelected(Object object, String tag) {
        mMembersDialog.dismiss();

        if(mChoosedMembers == null || !mChoosedMembers.contains(object)) {
            if (mChoosedMembers == null || mChoosedMembers.size() == 0)
                flMembers.removeAllViews();
            memberChipCloud.addChip(object);
            mChoosedMembers.add((( picItem ) object).getValue());
        }
    }

    private class valueWatcher implements TextWatcher{

        private EditText mEditText;

        public valueWatcher(EditText e) {
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
                if (value >= 30)
                    mEditText.getBackground().setColorFilter(getResources().getColor(R.color.material_green_400), PorterDuff.Mode.SRC_ATOP);
                else if (value < 25)
                    mEditText.getBackground().setColorFilter(getResources().getColor(R.color.material_red_400), PorterDuff.Mode.SRC_ATOP);
                else if(value >= 25 && value <= 30)
                    mEditText.getBackground().setColorFilter(getResources().getColor(R.color.material_yellow_400), PorterDuff.Mode.SRC_ATOP);
                else
                    mEditText.getBackground().setColorFilter(getResources().getColor(R.color.ms_white_54_opacity), PorterDuff.Mode.SRC_ATOP);

                checkCondition(value,mEditText);
            }
            else{
                checkCondition(0.0, mEditText);
                mEditText.getBackground().setColorFilter(getResources().getColor(R.color.ms_white_54_opacity), PorterDuff.Mode.SRC_ATOP);
            }
        }

        private void checkCondition(Double value, EditText key) {
            ArrayList<EditText> condPackage = new ArrayList<>();
            ArrayList<EditText> tampPackage = new ArrayList<>();

            tampPackage = AddInspectionActivity.conditionList;

            if(tampPackage != null)
                condPackage = tampPackage;

            if(condPackage.contains(key)){
                if (value < 25)
                    condPackage.add(key);
                else
                    condPackage.remove(key);
            }
            else {
                if (value < 25)
                    condPackage.add(key);
            }

            mActivityCallback.onSetCondition(condPackage, AddInspectionActivity.TYPE_SERVICE_MOTOR);
        }
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
                            if(id.equals("70")) {
                                txtA1.setText(txtA1.getText().toString() + value);
                            }else if(id.equals("71")){
                                txtA2.setText(txtA2.getText().toString() + value);
                            }else if(id.equals("72")) {
                                txtB1.setText(txtB1.getText().toString() + value);
                            }else if(id.equals("73")) {
                                txtB2.setText(txtB2.getText().toString() + value);
                            }else if(id.equals("74")) {
                                txtB3.setText(txtB3.getText().toString() + value);
                            }else if(id.equals("75")) {
                                txtC1.setText(txtC1.getText().toString() + value);
                            }else if(id.equals("89")) {
                                txtC2.setText(txtC2.getText().toString() + value);
                            }else if(id.equals("196")) {
                                txtC3.setText(txtC3.getText().toString() + value);
                            }else if(id.equals("90")) {
                                txtD1.setText(txtD1.getText().toString() + value);
                            }else if(id.equals("76")) {
                                txtD2.setText(txtD2.getText().toString() + value);
                            }else if(id.equals("77")) {
                                txtD3.setText(txtD3.getText().toString() + value);
                            }else if(id.equals("78")) {
                                txtE1.setText(txtE1.getText().toString() + value);
                            }else if(id.equals("79")) {
                                txtE2.setText(txtE2.getText().toString() + value);
                            }else if(id.equals("80")) {
                                txtE3.setText(txtE3.getText().toString() + value);
                            }else if(id.equals("91")) {
                                txtF1.setText(txtF1.getText().toString() + value);
                            }else if(id.equals("81")) {
                                txtF2.setText(txtF2.getText().toString() + value);
                            }else if(id.equals("197")) {
                                txtF3.setText(txtF3.getText().toString() + value);
                            }

                            else if(id.equals("136")) {
                                txtG1.setText(txtG1.getText().toString() + value);
                            }else if(id.equals("137")) {
                                txtG2.setText(txtG2.getText().toString() + value);
                            }else if(id.equals("138")) {
                                txtH1.setText(txtH1.getText().toString() + value);
                            }else if(id.equals("139")) {
                                txtH2.setText(txtH2.getText().toString() + value);
                            }else if(id.equals("148")) {
                                txtI1.setText(txtI1.getText().toString() + value);
                            }else if(id.equals("149")) {
                                txtI2.setText(txtI2.getText().toString() + value);
                            }else if(id.equals("150")) {
                                txtJ1.setText(txtJ1.getText().toString() + value);
                            }else if(id.equals("151")) {
                                txtJ2.setText(txtJ2.getText().toString() + value);
                            }else if(id.equals("152")) {
                                txtK1.setText(txtK1.getText().toString() + value);
                            }else if(id.equals("153")) {
                                txtK2.setText(txtK2.getText().toString() + value);
                            }else if(id.equals("154")) {
                                txtL1.setText(txtL1.getText().toString() + value);
                            }else if(id.equals("155")) {
                                txtL2.setText(txtL2.getText().toString() + value);
                            }else if(id.equals("156")) {
                                txtM1.setText(txtM1.getText().toString() + value);
                            }else if(id.equals("157")) {
                                txtM2.setText(txtM2.getText().toString() + value);
                            }else if(id.equals("82")) {
                                txtJmlChg.setText(txtJmlChg.getText().toString() + value);
                            }else if(id.equals("84")) {
                                txtRunH.setText(txtRunH.getText().toString() + value);
                            }else if(id.equals("85")) {
                                txtRotor.setText(txtRotor.getText().toString() + value);
                            }else if(id.equals("86")) {
                                txtStator.setText(txtStator.getText().toString() + value);
                            }
                        }
                    }else {
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
