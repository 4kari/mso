package id.sisi.si.mso.ui.inspection.detail;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.flexbox.FlexboxLayout;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import fisk.chipcloud.ChipCloud;
import fisk.chipcloud.ChipCloudConfig;
import fisk.chipcloud.ChipDeletedListener;
import id.sisi.si.mso.R;
import id.sisi.si.mso.data.model.Bearing;
import id.sisi.si.mso.data.model.DetailInspection;
import id.sisi.si.mso.data.model.picItem;
import id.sisi.si.mso.ui.widget.SearchableDialog;
import id.sisi.si.mso.utils.NetworkUtil;
import id.sisi.si.mso.utils.TextUtils;
import io.realm.RealmList;
import timber.log.Timber;

/**
 * Created by Luhur on 2/18/2019.
 */

public class BearingServiceFragmentM extends TechnicalFragment
        implements SearchableDialog.SearchableItemCallback, View.OnTouchListener{

    //final List<String> pointCondition = Arrays.asList("K", "L", "M");
    private final String BEARING_MODEL_KEY = "bearing_model_key";
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
    @BindView(R.id.txtKondisi)
    TextView txtCondition;

    private Bearing bearing;
    private ChipCloud memberChipCloud;
    private SearchableDialog mMembersDialog;
    private RealmList<String> mChoosedMembers;
    private Contract.getMemberCallback mActivityCallback;

    public static BearingServiceFragmentM newInstance(DetailInspection detailInspectionModel, Bearing bearingModel) {
        BearingServiceFragmentM instance = new BearingServiceFragmentM();
        Bundle args = new Bundle();
        args.putParcelable(DetailInspection.TYPE_EXTRA, detailInspectionModel);
        args.putParcelable(Bearing.TYPE_EXTRA, bearingModel);
        instance.setArguments(args);
        return instance;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_form_motor, container, false);
        setUnbinder(ButterKnife.bind(this, view));

        txtCondition.setVisibility(View.GONE);
        spCondition.setVisibility(View.GONE);

        mActivityCallback = (Contract.getMemberCallback) getActivity();

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
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        (( InspectionDetailActivity ) getActivity()).measureViewPager(this);
    }


    @Override
    void populateFields() {
        if (!NetworkUtil.isConnected())
            flMembers.setClickable(false);

        try {
            etPoint.setText("M");
            checkValue(getData().getBearing().getA1M(), etA1);
            checkValue(getData().getBearing().getA2M(), etA2);
            checkValue(getData().getBearing().getB1M(), etB1);
            checkValue(getData().getBearing().getB2M(), etB2);
            checkValue(getData().getBearing().getB3M(), etB3);
            checkValue(getData().getBearing().getC1M(), etC1);
            checkValue(getData().getBearing().getC2M(), etC2);
            checkValue(getData().getBearing().getC3M(), etC3);
            checkValue(getData().getBearing().getD1M(), etD1);
            checkValue(getData().getBearing().getD2M(), etD2);
            checkValue(getData().getBearing().getD3M(), etD3);
            checkValue(getData().getBearing().getE1M(), etE1);
            checkValue(getData().getBearing().getE2M(), etE2);
            checkValue(getData().getBearing().getE3M(), etE3);
            checkValue(getData().getBearing().getF1M(), etF1);
            checkValue(getData().getBearing().getF2M(), etF2);
            checkValue(getData().getBearing().getF3M(), etF3);
            checkValue(getData().getBearing().getG1M(), etG1);
            checkValue(getData().getBearing().getG2M(), etG2);
            checkValue(getData().getBearing().getH1M(), etH1);
            checkValue(getData().getBearing().getH2M(), etH2);
            checkValue(getData().getBearing().getI1m(), etI1);
            checkValue(getData().getBearing().getI2m(), etI2);
            checkValue(getData().getBearing().getJ1m(), etJ1);
            checkValue(getData().getBearing().getJ2m(), etJ2);
            checkValue(getData().getBearing().getK1m(), etK1);
            checkValue(getData().getBearing().getK2m(), etK2);
            checkValue(getData().getBearing().getL1m(), etL1);
            checkValue(getData().getBearing().getL2m(), etL2);
            checkValue(getData().getBearing().getM1m(), etM1);
            checkValue(getData().getBearing().getM2m(), etM2);
            clearNullDataInt(getData().getBearing().getJmlPenggantianM(), etJmlPenggantian);
            clearNullData(getData().getBearing().getRunHoursM(), etRunHour);
            clearNullData(getData().getBearing().getRotorM(), etRotor);
            clearNullData(getData().getBearing().getStatorM(), etStator);
            cbFilter.setChecked(isChecked(getData().getBearing().getFilterM()));
            etKeteranganMMV.setText(getData().getBearing().getKeteranganMMVM());
            initViewMembers(getData().getBearing().getMembersM());
        }
        catch (NullPointerException e) {
        }
    }

    private void initViewMembers(String members) {
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
            mChoosedMembers = new RealmList<>();

        if(members!=null) {
            String[] tampMember = members.split(",");
            memberChipCloud.addChips(tampMember);
            mChoosedMembers.addAll(Arrays.asList(tampMember));
            Timber.d("tampMember : " + mChoosedMembers.get(0));
        }
        else
        {
            TextView memberHint = new TextView(getActivity());
            memberHint.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            memberHint.setText("Masukkan nama member");
            memberHint.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
            memberHint.setTextColor(ContextCompat.getColor(getActivity(), R.color.material_grey_400));
            flMembers.addView(memberHint);
        }

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

    }

    void clearNullDataInt(int value, EditText mEditText){
        if(value == 0)
            mEditText.getText().clear();
        else
            mEditText.setText("" + value);
    }

    void clearNullData(Double value, EditText mEditText){
        if(value.equals(0.0))
            mEditText.getText().clear();
        else
            mEditText.setText("" + value);
    }

    private void checkValue(Double value, EditText mEditText) {
        if(value.equals(0.0))
            mEditText.getText().clear();
        else
            mEditText.setText("" + value);

        if(!value.toString().trim().isEmpty()) {
            if (value >= 30)
                mEditText.getBackground().setColorFilter(getResources().getColor(R.color.material_green_400), PorterDuff.Mode.SRC_ATOP);
            else if (value < 25)
                mEditText.getBackground().setColorFilter(getResources().getColor(R.color.material_red_400), PorterDuff.Mode.SRC_ATOP);
            else if(value >= 25 && value <= 30)
                mEditText.getBackground().setColorFilter(getResources().getColor(R.color.material_yellow_400), PorterDuff.Mode.SRC_ATOP);
            else
                mEditText.getBackground().setColorFilter(getResources().getColor(R.color.ms_white_54_opacity), PorterDuff.Mode.SRC_ATOP);
        }
        else
        {
            mEditText.getBackground().setColorFilter(getResources().getColor(R.color.ms_white_54_opacity), PorterDuff.Mode.SRC_ATOP);
        }
    }

    public boolean isChecked(int data){
        if(data==0)
            return false;
        else
            return true;
    }

    @Override
    void setupForEdit() {
        etA1.setFocusable(true);
        etA1.setFocusableInTouchMode(true);
        etA2.setFocusable(true);
        etA2.setFocusableInTouchMode(true);
        etB1.setFocusable(true);
        etB1.setFocusableInTouchMode(true);
        etB2.setFocusable(true);
        etB2.setFocusableInTouchMode(true);
        etB3.setFocusable(true);
        etB3.setFocusableInTouchMode(true);
        etC1.setFocusable(true);
        etC1.setFocusableInTouchMode(true);
        etC2.setFocusable(true);
        etC2.setFocusableInTouchMode(true);
        etC3.setFocusable(true);
        etC3.setFocusableInTouchMode(true);
        etD1.setFocusable(true);
        etD1.setFocusableInTouchMode(true);
        etD2.setFocusable(true);
        etD2.setFocusableInTouchMode(true);
        etD3.setFocusable(true);
        etD3.setFocusableInTouchMode(true);
        etE1.setFocusable(true);
        etE1.setFocusableInTouchMode(true);
        etE2.setFocusable(true);
        etE2.setFocusableInTouchMode(true);
        etE3.setFocusable(true);
        etE3.setFocusableInTouchMode(true);
        etF1.setFocusable(true);
        etF1.setFocusableInTouchMode(true);
        etF2.setFocusable(true);
        etF2.setFocusableInTouchMode(true);
        etF3.setFocusable(true);
        etF3.setFocusableInTouchMode(true);
        etG1.setFocusable(true);
        etG1.setFocusableInTouchMode(true);
        etG2.setFocusable(true);
        etG2.setFocusableInTouchMode(true);
        etH1.setFocusable(true);
        etH1.setFocusableInTouchMode(true);
        etH2.setFocusable(true);
        etH2.setFocusableInTouchMode(true);
        etI1.setFocusable(true);
        etI1.setFocusableInTouchMode(true);
        etI2.setFocusable(true);
        etI2.setFocusableInTouchMode(true);
        etJ1.setFocusable(true);
        etJ1.setFocusableInTouchMode(true);
        etJ2.setFocusable(true);
        etJ2.setFocusableInTouchMode(true);
        etK1.setFocusable(true);
        etK1.setFocusableInTouchMode(true);
        etK2.setFocusable(true);
        etK2.setFocusableInTouchMode(true);
        etL1.setFocusable(true);
        etL1.setFocusableInTouchMode(true);
        etL2.setFocusable(true);
        etL2.setFocusableInTouchMode(true);
        etM1.setFocusable(true);
        etM1.setFocusableInTouchMode(true);
        etM2.setFocusable(true);
        etM2.setFocusableInTouchMode(true);
        etJmlPenggantian.setFocusable(true);
        etJmlPenggantian.setFocusableInTouchMode(true);
        etRunHour.setFocusable(true);
        etRunHour.setFocusableInTouchMode(true);
        etRotor.setFocusable(true);
        etRotor.setFocusableInTouchMode(true);
        etStator.setFocusable(true);
        etStator.setFocusableInTouchMode(true);
        etKeteranganMMV.setFocusable(true);
        etKeteranganMMV.setFocusableInTouchMode(true);
        cbFilter.setClickable(true);
        flMembers.setClickable(true);
        flMembers.setOnTouchListener(this);
    }

    @Override
    void setupForDetail() {
        etPoint.setFocusable(false);
        etA1.setFocusable(false);
        etA2.setFocusable(false);
        etB1.setFocusable(false);
        etB2.setFocusable(false);
        etB3.setFocusable(false);
        etC1.setFocusable(false);
        etC2.setFocusable(false);
        etC3.setFocusable(false);
        etD1.setFocusable(false);
        etD2.setFocusable(false);
        etD3.setFocusable(false);
        etE1.setFocusable(false);
        etE2.setFocusable(false);
        etE3.setFocusable(false);
        etF1.setFocusable(false);
        etF2.setFocusable(false);
        etF3.setFocusable(false);
        etG1.setFocusable(false);
        etG2.setFocusable(false);
        etH1.setFocusable(false);
        etH2.setFocusable(false);
        etI1.setFocusable(false);
        etI2.setFocusable(false);
        etJ1.setFocusable(false);
        etJ2.setFocusable(false);
        etK1.setFocusable(false);
        etK2.setFocusable(false);
        etL1.setFocusable(false);
        etL2.setFocusable(false);
        etM1.setFocusable(false);
        etM2.setFocusable(false);
        cbFilter.setClickable(false);
        etJmlPenggantian.setFocusable(false);
        etRunHour.setFocusable(false);
        etRotor.setFocusable(false);
        etStator.setFocusable(false);
        etKeteranganMMV.setFocusable(false);
        flMembers.setClickable(false);
    }

    @Override
    void setDataToModel() {
        bearing.setKeteranganMMVM(etKeteranganMMV.getText().toString());

        if(!etA1.getText().toString().isEmpty())
            bearing.setA1M(Double.parseDouble(etA1.getText().toString()));
        if(!etA2.getText().toString().isEmpty())
            bearing.setA2M(Double.parseDouble(etA2.getText().toString()));
        if(!etB1.getText().toString().isEmpty())
            bearing.setB1M(Double.parseDouble(etB1.getText().toString()));
        if(!etB2.getText().toString().isEmpty())
            bearing.setB2M(Double.parseDouble(etB2.getText().toString()));
        if(!etB3.getText().toString().isEmpty())
            bearing.setB3M(Double.parseDouble(etB3.getText().toString()));
        if(!etC1.getText().toString().isEmpty())
            bearing.setC1M(Double.parseDouble(etC1.getText().toString()));
        if(!etC2.getText().toString().isEmpty())
            bearing.setC2M(Double.parseDouble(etC2.getText().toString()));
        if(!etC3.getText().toString().isEmpty())
            bearing.setC3M(Double.parseDouble(etC3.getText().toString()));
        if(!etD1.getText().toString().isEmpty())
            bearing.setD1M(Double.parseDouble(etD1.getText().toString()));
        if(!etD2.getText().toString().isEmpty())
            bearing.setD2M(Double.parseDouble(etD2.getText().toString()));
        if(!etD3.getText().toString().isEmpty())
            bearing.setD3M(Double.parseDouble(etD3.getText().toString()));
        if(!etE1.getText().toString().isEmpty())
            bearing.setE1M(Double.parseDouble(etE1.getText().toString()));
        if(!etE2.getText().toString().isEmpty())
            bearing.setE2M(Double.parseDouble(etE2.getText().toString()));
        if(!etE3.getText().toString().isEmpty())
            bearing.setE3M(Double.parseDouble(etE3.getText().toString()));
        if(!etF1.getText().toString().isEmpty())
            bearing.setF1M(Double.parseDouble(etF1.getText().toString()));
        if(!etF2.getText().toString().isEmpty())
            bearing.setF2M(Double.parseDouble(etF2.getText().toString()));
        if(!etF3.getText().toString().isEmpty())
            bearing.setF3M(Double.parseDouble(etF3.getText().toString()));
        if(!etG1.getText().toString().isEmpty())
            bearing.setG1M(Double.parseDouble(etG1.getText().toString()));
        if(!etG2.getText().toString().isEmpty())
            bearing.setG2M(Double.parseDouble(etG2.getText().toString()));
        if(!etH1.getText().toString().isEmpty())
            bearing.setH1M(Double.parseDouble(etH1.getText().toString()));
        if(!etH2.getText().toString().isEmpty())
            bearing.setH2M(Double.parseDouble(etH2.getText().toString()));
        if(!etI1.getText().toString().isEmpty())
            bearing.setI1m(Double.parseDouble(etI1.getText().toString()));
        if(!etI2.getText().toString().isEmpty())
            bearing.setI2m(Double.parseDouble(etI2.getText().toString()));
        if(!etJ1.getText().toString().isEmpty())
            bearing.setJ1m(Double.parseDouble(etJ1.getText().toString()));
        if(!etJ2.getText().toString().isEmpty())
            bearing.setJ2m(Double.parseDouble(etJ2.getText().toString()));
        if(!etK1.getText().toString().isEmpty())
            bearing.setK1m(Double.parseDouble(etK1.getText().toString()));
        if(!etK2.getText().toString().isEmpty())
            bearing.setK2m(Double.parseDouble(etK2.getText().toString()));
        if(!etL1.getText().toString().isEmpty())
            bearing.setL1m(Double.parseDouble(etL1.getText().toString()));
        if(!etL2.getText().toString().isEmpty())
            bearing.setL2m(Double.parseDouble(etL2.getText().toString()));
        if(!etM1.getText().toString().isEmpty())
            bearing.setM1m(Double.parseDouble(etM1.getText().toString()));
        if(!etM2.getText().toString().isEmpty())
            bearing.setM2m(Double.parseDouble(etM2.getText().toString()));
        if(!TextUtils.isNullOrEmpty(etJmlPenggantian.getText().toString()))
            bearing.setJmlPenggantianM(Integer.parseInt(etJmlPenggantian.getText().toString()));
        bearing.setFilterM(cbFilter.isChecked()? 1 : 0);
        if(!etRunHour.getText().toString().isEmpty())
            bearing.setRunHoursM(Double.parseDouble(etRunHour.getText().toString()));
        if(!etRotor.getText().toString().isEmpty())
            bearing.setRotorM(Double.parseDouble(etRotor.getText().toString()));
        if(!etStator.getText().toString().isEmpty())
            bearing.setStatorM(Double.parseDouble(etStator.getText().toString()));

        StringBuilder memberConcated = new StringBuilder();
        for (String o : mChoosedMembers){
            if (memberConcated.length() > 0) memberConcated.append( "," );
            memberConcated.append(o);
        }
        bearing.setMembersM(memberConcated.toString());
    }

    @Override
    void updateMember(List<picItem> picItems) {
        mMembersDialog.updateDataset(picItems);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelable(BEARING_MODEL_KEY, bearing);
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
            mActivityCallback.onSearchItem(query, "M");
        else
            mMembersDialog.updateDataset(Collections.EMPTY_LIST);
    }

    @Override
    public void onSearchableItemSelected(Object object, String tag) {
        mMembersDialog.dismiss();

        if(mChoosedMembers == null || !mChoosedMembers.contains(object)) {
            if (mChoosedMembers == null || mChoosedMembers.size() == 0)
                flMembers.removeAllViews();
            memberChipCloud.addChip(((picItem) object).getValue());
            mChoosedMembers.add(((picItem) object).getValue());
        }
    }
}
