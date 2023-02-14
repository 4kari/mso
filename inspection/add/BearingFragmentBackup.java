package id.sisi.si.mso.ui.inspection.add;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.stepstone.stepper.VerificationError;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import id.sisi.si.mso.R;
import id.sisi.si.mso.data.model.Bearing;
import id.sisi.si.mso.data.model.InspectionDetail;
import id.sisi.si.mso.data.model.picItem;
import id.sisi.si.mso.utils.TextUtils;
import timber.log.Timber;

/**
 * Created by durrrr on 11-Oct-17.
 * Email: cab.purnama@gmail.com
 */
public class BearingFragmentBackup extends TechnicalFragment {

    @BindView(R.id.vibarasiDs)
    EditText vibrasiDS;
    @BindView(R.id.geDs)
    EditText geDS;
    @BindView(R.id.temperatureDS)
    EditText temperatureDS;
    @BindView(R.id.vibrasiNds)
    EditText vibrasiNDS;
    @BindView(R.id.geNds)
    EditText geNDS;
    @BindView(R.id.temperatureNds)
    EditText temperatureNDS;

    public static BearingFragmentBackup newInstance() {
        BearingFragmentBackup instance = new BearingFragmentBackup();
        Bundle args = new Bundle();
        instance.setArguments(args);
        return instance;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bearing_inspection, container, false);
        setUnbinder(ButterKnife.bind(this, view));

        return view;
    }

    @Override
    Bearing setDataToModel() {
        Bearing bearing = new Bearing();

        bearing.setVibrasiDs(Double.parseDouble(vibrasiDS.getText().toString()));
        bearing.setGeDs(Double.parseDouble(geDS.getText().toString()));
        bearing.setTemperaturDs(Double.parseDouble(temperatureDS.getText().toString()));
        bearing.setVibrasiNds(Double.parseDouble(vibrasiNDS.getText().toString()));
        bearing.setGeNds(Double.parseDouble(geNDS.getText().toString()));
        bearing.setTemperaturNds(Double.parseDouble(temperatureNDS.getText().toString()));

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

        if (TextUtils.isNullOrEmpty(vibrasiDS.getText().toString())) {
            vibrasiDS.setError(getString(R.string.error_field_vibrasi));
            errorMessage = getString(R.string.alert_any_field_required);
        } else {
            vibrasiDS.setError(null);
        }

        if (TextUtils.isNullOrEmpty(geDS.getText().toString())) {
            geDS.setError(getString(R.string.error_field_geds));
            errorMessage = getString(R.string.alert_any_field_required);
        } else {
            geDS.setError(null);
        }

        if (TextUtils.isNullOrEmpty(temperatureDS.getText().toString())) {
            temperatureDS.setError(getString(R.string.error_field_temperatureds));
            errorMessage = getString(R.string.alert_any_field_required);
        } else {
            temperatureDS.setError(null);
        }

        if (TextUtils.isNullOrEmpty(vibrasiNDS.getText().toString())) {
            vibrasiNDS.setError(getString(R.string.error_field_vibrasids));
            errorMessage = getString(R.string.alert_any_field_required);
        } else {
            vibrasiNDS.setError(null);
        }

        if (TextUtils.isNullOrEmpty(geNDS.getText().toString())) {
            geNDS.setError(getString(R.string.error_field_gends));
            errorMessage = getString(R.string.alert_any_field_required);
        } else {
            geNDS.setError(null);
        }

        if (TextUtils.isNullOrEmpty(temperatureNDS.getText().toString())) {
            temperatureNDS.setError(getString(R.string.error_field_temperatureNDS));
            errorMessage = getString(R.string.alert_any_field_required);
        } else {
            temperatureNDS.setError(null);
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
}
