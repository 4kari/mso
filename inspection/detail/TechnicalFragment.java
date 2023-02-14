package id.sisi.si.mso.ui.inspection.detail;

import java.util.List;

import id.sisi.si.mso.data.model.DetailInspection;
import id.sisi.si.mso.data.model.picItem;
import id.sisi.si.mso.ui.base.BaseFragment;

/**
 * Created by durrrr on 09-Oct-17.
 * Email: andaruwildan@gmail.com
 */
public abstract class TechnicalFragment extends BaseFragment {

    private DetailInspection mData;

    protected DetailInspection getData() {
        return mData;
    }

    protected void setData(DetailInspection data) {
        mData = data;
    }

    abstract void populateFields();

    abstract void setupForEdit();

    abstract void setupForDetail();

    abstract void setDataToModel();

    abstract void updateMember(List<picItem> picItems);
}
