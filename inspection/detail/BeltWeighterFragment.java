package id.sisi.si.mso.ui.inspection.detail;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Spinner;

import com.crashlytics.android.Crashlytics;

import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import id.sisi.si.mso.R;
import id.sisi.si.mso.data.DataHelper;
import id.sisi.si.mso.data.model.BeltWeighter;
import id.sisi.si.mso.data.model.DetailInspection;
import id.sisi.si.mso.data.model.picItem;
import id.sisi.si.mso.ui.widget.InspectionTextView;

/**
 * Created by durrrr on 06-Oct-17.
 * Email: andaruwildan@gmail.com
 */
public class BeltWeighterFragment extends TechnicalFragment {

    final List<String> activityOptions = Arrays.asList("Order", "Routine Check");
    private final String BELT_WEIGHTER_MODEL_KEY = "belt_weighter_model_key";
    @BindView(R.id.spActivity)
    Spinner spActivity;
    @BindView(R.id.qR)
    EditText qR;
    @BindView(R.id.counter)
    EditText counterCount;
    @BindView(R.id.spanCorrection)
    EditText spanCorrection;
    private BeltWeighter mBeltWeighterModel;


    public static BeltWeighterFragment newInstance() {
        BeltWeighterFragment instance = new BeltWeighterFragment();
        return instance;
    }

    public static BeltWeighterFragment newInstance(DetailInspection detailInspectionModel, BeltWeighter beltWeighterModel) {
        BeltWeighterFragment instance = new BeltWeighterFragment();
        Bundle args = new Bundle();
        args.putParcelable(DetailInspection.TYPE_EXTRA, detailInspectionModel);
        args.putParcelable(BeltWeighter.TYPE_EXTRA, beltWeighterModel);
        instance.setArguments(args);
        return instance;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_belt_weighter_detail_text, container, false);
        setUnbinder(ButterKnife.bind(this, view));
        if (getArguments().getParcelable(DetailInspection.TYPE_EXTRA) != null &
                getArguments().getParcelable(BeltWeighter.TYPE_EXTRA) != null) {
            mBeltWeighterModel = getArguments().getParcelable(BeltWeighter.TYPE_EXTRA);
            DetailInspection detailInspectionModel = getArguments().getParcelable(DetailInspection.TYPE_EXTRA);
            setData(detailInspectionModel);
            populateFields();
            setupForDetail();
        }

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
            qR.setText("" + getData().getBeltWeighter().getQr());
            counterCount.setText("" + getData().getBeltWeighter().getJumlahCounter());
            spanCorrection.setText("" + getData().getBeltWeighter().getSpanCorrection());
            spActivity.setAdapter(DataHelper.createAdapter(getActivity(), activityOptions));
            spActivity.setSelection(getData().getBeltWeighter().getAktivitas().equalsIgnoreCase("Order") ?
                    0 : 1);
        } catch (Exception e) {
            Crashlytics.logException(e.fillInStackTrace());
        }
    }

    @Override
    void setupForEdit() {
        spActivity.setEnabled(true);
        qR.setFocusable(true);
        qR.setFocusableInTouchMode(true);
        counterCount.setFocusable(true);
        counterCount.setFocusableInTouchMode(true);
        spanCorrection.setFocusable(true);
        spanCorrection.setFocusableInTouchMode(true);

        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(qR, InputMethodManager.SHOW_IMPLICIT);
    }

    @Override
    void setupForDetail() {
        spActivity.setEnabled(false);
        qR.setFocusable(false);
        counterCount.setFocusable(false);
        spanCorrection.setFocusable(false);
    }

    @Override
    void setDataToModel() {
        mBeltWeighterModel.setAktivitas(spActivity.getSelectedItem().toString());
        mBeltWeighterModel.setQr(Double.parseDouble(qR.getText().toString()));
        mBeltWeighterModel.setJumlahCounter(Double.parseDouble(counterCount.getText().toString()));
        mBeltWeighterModel.setSpanCorrection(Double.parseDouble(spanCorrection.getText().toString()));
    }

    @Override
    void updateMember(List<picItem> picItems) {

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (mBeltWeighterModel != null)
            outState.putParcelable(BELT_WEIGHTER_MODEL_KEY, mBeltWeighterModel);
    }
}
