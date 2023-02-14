package id.sisi.si.mso.ui.inspection.add;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.stepstone.stepper.VerificationError;

import java.util.List;

import id.sisi.si.mso.R;
import id.sisi.si.mso.data.model.InspectionDetail;
import id.sisi.si.mso.data.model.TechnicalModel;
import id.sisi.si.mso.data.model.picItem;

/**
 * Created by durrrr on 19-Oct-17.
 * Email: andaruwildan@gmail.com
 */
public class EmptyFragment extends TechnicalFragment {

    public static EmptyFragment newInstance() {
        EmptyFragment instance = new EmptyFragment();
        return instance;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_empty_technical, container, false);
        return view;
    }

    @Override
    TechnicalModel setDataToModel() {
        return null;
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
        return null;
    }

    @Override
    public void onSelected() {

    }

    @Override
    public void onError(@NonNull VerificationError error) {

    }
}
