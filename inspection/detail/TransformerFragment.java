package id.sisi.si.mso.ui.inspection.detail;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import id.sisi.si.mso.R;
import id.sisi.si.mso.data.model.DetailInspection;
import id.sisi.si.mso.data.model.Transformer;
import id.sisi.si.mso.data.model.picItem;
import id.sisi.si.mso.ui.widget.InspectionTextView;
import id.sisi.si.mso.utils.TextUtils;

import static id.sisi.si.mso.R.layout.list_item_spinner;

/**
 * Created by durrrr on 06-Oct-17.
 * Email: andaruwildan@gmail.com
 */
public class TransformerFragment extends TechnicalFragment {

    private final String TRANSFORMER_MODEL_KEY = "transformer_model_key";

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

    @BindView(R.id.spSilicaGell)
    Spinner spSilica;

    private Transformer mTransformer;


    public static TransformerFragment newInstance() {
        TransformerFragment instance = new TransformerFragment();
        return instance;
    }

    public static TransformerFragment newInstance(DetailInspection detailInspectionModel, Transformer transformerModel) {
        TransformerFragment instance = new TransformerFragment();
        Bundle args = new Bundle();
        args.putParcelable(DetailInspection.TYPE_EXTRA, detailInspectionModel);
        args.putParcelable(Transformer.TYPE_EXTRA, transformerModel);
        instance.setArguments(args);
        return instance;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_transformer_detail_text, container, false);
        setUnbinder(ButterKnife.bind(this, view));

        if (getArguments().getParcelable(DetailInspection.TYPE_EXTRA) != null
                & getArguments().getParcelable(Transformer.TYPE_EXTRA) != null) {
            mTransformer = getArguments().getParcelable(Transformer.TYPE_EXTRA);
            DetailInspection detailInspectionModel = getArguments().getParcelable(DetailInspection.TYPE_EXTRA);
            setData(detailInspectionModel);

            populateFields();
            setupForDetail();
        }

        ArrayAdapter<String> spAdapter = new ArrayAdapter<>(getActivity(),
                list_item_spinner, conditionOptions);
        spSilica.setAdapter(spAdapter);

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        (( InspectionDetailActivity ) getActivity()).measureViewPager(this);
    }

    @Override
    void populateFields() {
        try {
            checkEmpty(getData().getTransformer().getTemperaturTrafo(), temperatureTrafo);
            checkEmpty(getData().getTransformer().getTemperaturKoneksi(), temperatureKoneksi);
            checkEmpty(getData().getTransformer().getAmperePremier_r(), ampPremierR);
            checkEmpty(getData().getTransformer().getAmperePremier_s(), ampPremierS);
            checkEmpty(getData().getTransformer().getAmperePremier_t(), ampPremierT);
            checkEmpty(getData().getTransformer().getTempWinding(), tempWinding);
            checkEmpty(getData().getTransformer().getOilTemp(), oilTemp);
            checkEmpty(getData().getTransformer().getOilLevel(), oilLevel);
            spSilica.setSelection(getData().getTransformer().getSilicaGell());
            if(!etNegativeList.getText().toString().isEmpty())
                etNegativeList.setText(getData().getTransformer().getNegativeList());
        } catch (Exception e) {

        }
    }

     void checkEmpty(Double value, EditText mEditText) {
         if (value.equals(0.0))
             mEditText.getText().clear();
         else
             mEditText.setText("" + value);
     }

    @Override
    void setupForEdit() {
        EditText[] trafoEdittext = { temperatureTrafo, temperatureKoneksi, ampPremierR, ampPremierS, ampPremierT, tempWinding,
                oilTemp, oilLevel, etNegativeList};
        for(EditText et : trafoEdittext)
            setEditable(et, true);

        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(temperatureTrafo, InputMethodManager.SHOW_IMPLICIT);
    }

    private void setEditable(EditText mEdittext, boolean value) {
        mEdittext.setFocusable(value);
        mEdittext.setFocusableInTouchMode(value);
    }

    @Override
    void setupForDetail() {
        EditText[] trafoEdittext = { temperatureTrafo, temperatureKoneksi, ampPremierR, ampPremierS, ampPremierT, tempWinding,
                oilTemp, oilLevel, etNegativeList};
        for(EditText et : trafoEdittext)
            setEditable(et, false);
    }

    @Override
    void setDataToModel() {
        if(!TextUtils.isNullOrEmpty(temperatureKoneksi.getText().toString()))
            mTransformer.setTemperaturKoneksi(Double.parseDouble(temperatureKoneksi.getText().toString()));
        if(!TextUtils.isNullOrEmpty(temperatureTrafo.getText().toString()))
            mTransformer.setTemperaturTrafo(Double.parseDouble(temperatureTrafo.getText().toString()));
        if(!TextUtils.isNullOrEmpty(ampPremierR.getText().toString()))
            mTransformer.setAmperePremier_r(Double.parseDouble(ampPremierR.getText().toString()));
        if(!TextUtils.isNullOrEmpty(ampPremierS.getText().toString()))
            mTransformer.setAmperePremier_s(Double.parseDouble(ampPremierS.getText().toString()));
        if(!TextUtils.isNullOrEmpty(ampPremierT.getText().toString()))
            mTransformer.setAmperePremier_t(Double.parseDouble(ampPremierT.getText().toString()));
        if(!TextUtils.isNullOrEmpty(tempWinding.getText().toString()))
            mTransformer.setTempWinding(Double.parseDouble(tempWinding.getText().toString()));
        if(!TextUtils.isNullOrEmpty(oilTemp.getText().toString()))
            mTransformer.setOilTemp(Double.parseDouble(oilTemp.getText().toString()));
        if(!TextUtils.isNullOrEmpty(oilLevel.getText().toString()))
            mTransformer.setOilLevel(Double.parseDouble(oilLevel.getText().toString()));
        if(!TextUtils.isNullOrEmpty(String.valueOf(spSilica.getSelectedItemPosition())))
            mTransformer.setSilicaGell(spSilica.getSelectedItemPosition());
        if(!TextUtils.isNullOrEmpty(etNegativeList.getText().toString()))
            mTransformer.setNegativeList(etNegativeList.getText().toString());
    }

    @Override
    void updateMember(List<picItem> picItems) {

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (mTransformer != null)
            outState.putParcelable(TRANSFORMER_MODEL_KEY, mTransformer);
    }
}
