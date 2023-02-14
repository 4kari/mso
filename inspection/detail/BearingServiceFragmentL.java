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

public class BearingServiceFragmentL extends TechnicalFragment
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

    public static BearingServiceFragmentL newInstance(DetailInspection detailInspectionModel, Bearing bearingModel) {
        BearingServiceFragmentL instance = new BearingServiceFragmentL();
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
            etPoint.setText("L");
            checkValue(getData().getBearing().getA1L(), etA1);
            checkValue(getData().getBearing().getA2L(), etA2);
            checkValue(getData().getBearing().getB1L(), etB1);
            checkValue(getData().getBearing().getB2L(), etB2);
            checkValue(getData().getBearing().getB3L(), etB3);
            checkValue(getData().getBearing().getC1L(), etC1);
            checkValue(getData().getBearing().getC2L(), etC2);
            checkValue(getData().getBearing().getC3L(), etC3);
            checkValue(getData().getBearing().getD1L(), etD1);
            checkValue(getData().getBearing().getD2L(), etD2);
            checkValue(getData().getBearing().getD3L(), etD3);
            checkValue(getData().getBearing().getE1L(), etE1);
            checkValue(getData().getBearing().getE2L(), etE2);
            checkValue(getData().getBearing().getE3L(), etE3);
            checkValue(getData().getBearing().getF1L(), etF1);
            checkValue(getData().getBearing().getF2L(), etF2);
            checkValue(getData().getBearing().getF3L(), etF3);
            checkValue(getData().getBearing().getG1L(), etG1);
            checkValue(getData().getBearing().getG2L(), etG2);
            checkValue(getData().getBearing().getH1L(), etH1);
            checkValue(getData().getBearing().getH2L(), etH2);
            checkValue(getData().getBearing().getI1l(), etI1);
            checkValue(getData().getBearing().getI2l(), etI2);
            checkValue(getData().getBearing().getJ1l(), etJ1);
            checkValue(getData().getBearing().getJ2l(), etJ2);
            checkValue(getData().getBearing().getK1l(), etK1);
            checkValue(getData().getBearing().getK2l(), etK2);
            checkValue(getData().getBearing().getL1l(), etL1);
            checkValue(getData().getBearing().getL2l(), etL2);
            checkValue(getData().getBearing().getM1l(), etM1);
            checkValue(getData().getBearing().getM2l(), etM2);
            clearNullDataInt(getData().getBearing().getJmlPenggantianL(), etJmlPenggantian);
            clearNullData(getData().getBearing().getRunHoursL(), etRunHour);
            clearNullData(getData().getBearing().getRotorL(), etRotor);
            clearNullData(getData().getBearing().getStatorL(), etStator);
            cbFilter.setChecked(isChecked(getData().getBearing().getFilterL()));
            etKeteranganMMV.setText(getData().getBearing().getKeteranganMMVL());
            initViewMembers(getData().getBearing().getMembersL());
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
        etI1.setFocusable(true);
        etI2.setFocusableInTouchMode(true);
        etJ1.setFocusable(true);
        etJ2.setFocusableInTouchMode(true);
        etK1.setFocusable(true);
        etK2.setFocusableInTouchMode(true);
        etL1.setFocusable(true);
        etL2.setFocusableInTouchMode(true);
        etM1.setFocusable(true);
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
        bearing.setKeteranganMMVL(etKeteranganMMV.getText().toString());

        if(!etA1.getText().toString().isEmpty())
            bearing.setA1L(Double.parseDouble(etA1.getText().toString()));
        if(!etA2.getText().toString().isEmpty())
            bearing.setA2L(Double.parseDouble(etA2.getText().toString()));
        if(!etB1.getText().toString().isEmpty())
            bearing.setB1L(Double.parseDouble(etB1.getText().toString()));
        if(!etB2.getText().toString().isEmpty())
            bearing.setB2L(Double.parseDouble(etB2.getText().toString()));
        if(!etB3.getText().toString().isEmpty())
            bearing.setB3L(Double.parseDouble(etB3.getText().toString()));
        if(!etC1.getText().toString().isEmpty())
            bearing.setC1L(Double.parseDouble(etC1.getText().toString()));
        if(!etC2.getText().toString().isEmpty())
            bearing.setC2L(Double.parseDouble(etC2.getText().toString()));
        if(!etC3.getText().toString().isEmpty())
            bearing.setC3L(Double.parseDouble(etC3.getText().toString()));
        if(!etD1.getText().toString().isEmpty())
            bearing.setD1L(Double.parseDouble(etD1.getText().toString()));
        if(!etD2.getText().toString().isEmpty())
            bearing.setD2L(Double.parseDouble(etD2.getText().toString()));
        if(!etD3.getText().toString().isEmpty())
            bearing.setD3L(Double.parseDouble(etD3.getText().toString()));
        if(!etE1.getText().toString().isEmpty())
            bearing.setE1L(Double.parseDouble(etE1.getText().toString()));
        if(!etE2.getText().toString().isEmpty())
            bearing.setE2L(Double.parseDouble(etE2.getText().toString()));
        if(!etE3.getText().toString().isEmpty())
            bearing.setE3L(Double.parseDouble(etE3.getText().toString()));
        if(!etF1.getText().toString().isEmpty())
            bearing.setF1L(Double.parseDouble(etF1.getText().toString()));
        if(!etF2.getText().toString().isEmpty())
            bearing.setF2L(Double.parseDouble(etF2.getText().toString()));
        if(!etF3.getText().toString().isEmpty())
            bearing.setF3L(Double.parseDouble(etF3.getText().toString()));
        if(!etG1.getText().toString().isEmpty())
            bearing.setG1L(Double.parseDouble(etG1.getText().toString()));
        if(!etG2.getText().toString().isEmpty())
            bearing.setG2L(Double.parseDouble(etG2.getText().toString()));
        if(!etH1.getText().toString().isEmpty())
            bearing.setH1L(Double.parseDouble(etH1.getText().toString()));
        if(!etH2.getText().toString().isEmpty())
            bearing.setH2L(Double.parseDouble(etH2.getText().toString()));
        if(!etI1.getText().toString().isEmpty())
            bearing.setH1L(Double.parseDouble(etI1.getText().toString()));
        if(!etI2.getText().toString().isEmpty())
            bearing.setH2L(Double.parseDouble(etI2.getText().toString()));
        if(!etJ1.getText().toString().isEmpty())
            bearing.setH1L(Double.parseDouble(etJ1.getText().toString()));
        if(!etJ2.getText().toString().isEmpty())
            bearing.setH2L(Double.parseDouble(etJ2.getText().toString()));
        if(!etK1.getText().toString().isEmpty())
            bearing.setH1L(Double.parseDouble(etK1.getText().toString()));
        if(!etK2.getText().toString().isEmpty())
            bearing.setH2L(Double.parseDouble(etK2.getText().toString()));
        if(!etL1.getText().toString().isEmpty())
            bearing.setH1L(Double.parseDouble(etL1.getText().toString()));
        if(!etL2.getText().toString().isEmpty())
            bearing.setH2L(Double.parseDouble(etL2.getText().toString()));
        if(!etM1.getText().toString().isEmpty())
            bearing.setH1L(Double.parseDouble(etM1.getText().toString()));
        if(!etM2.getText().toString().isEmpty())
            bearing.setH2L(Double.parseDouble(etM2.getText().toString()));
        if(!TextUtils.isNullOrEmpty(etJmlPenggantian.getText().toString()))
            bearing.setJmlPenggantianL(Integer.parseInt(etJmlPenggantian.getText().toString()));
        bearing.setFilterL(cbFilter.isChecked()? 1 : 0);
        if(!etRunHour.getText().toString().isEmpty())
            bearing.setRunHoursL(Double.parseDouble(etRunHour.getText().toString()));
        if(!etRotor.getText().toString().isEmpty())
            bearing.setRotorL(Double.parseDouble(etRotor.getText().toString()));
        if(!etStator.getText().toString().isEmpty())
            bearing.setStatorL(Double.parseDouble(etStator.getText().toString()));

        StringBuilder memberConcated = new StringBuilder();
        for (String o : mChoosedMembers){
            if (memberConcated.length() > 0) memberConcated.append( "," );
            memberConcated.append(o);
        }
        bearing.setMembersL(memberConcated.toString());
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
            mActivityCallback.onSearchItem(query, "L");
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
