package id.sisi.si.mso.ui.inspection.add;

import com.stepstone.stepper.Step;

import java.util.ArrayList;
import java.util.List;

import id.sisi.si.mso.data.model.InspectionDetail;
import id.sisi.si.mso.data.model.TechnicalModel;
import id.sisi.si.mso.data.model.picItem;
import id.sisi.si.mso.ui.base.BaseFragment;

/**
 * Created by durrrr on 11-Oct-17.
 * Email: cab.purnama@gmail.com
 */
public abstract class TechnicalFragment extends BaseFragment implements Step {

    public String type;

    abstract TechnicalModel setDataToModel();

    abstract InspectionDetail setCondition();

    abstract void setConditions(int i);

    abstract void updateMember(List<picItem> picItems);

}
