package id.sisi.si.mso.ui.inspection.detail;

import android.widget.EditText;

import java.util.ArrayList;
import java.util.List;

import id.sisi.si.mso.data.model.AbnormalityParams;
import id.sisi.si.mso.data.model.DetailInspection;
import id.sisi.si.mso.data.model.TechnicalModel;
import id.sisi.si.mso.data.model.picItem;
import id.sisi.si.mso.ui.base.BaseView;

/**
 * Created by durrrr on 04-Oct-17.
 * Email: andaruwildan@gmail.com
 */
public interface Contract {

    interface Presenter {

        void attemptDeleteInspection();

        void attemptUpdateInspection();

        void getAutocompleteMember(String query, String tag);

    }

    interface getMemberCallback{

        void onSearchItem(String query, String tag);

        void onShowMember(List<picItem> picItems, String tag);

    }

    interface View extends BaseView {

        void initView();

        void populateFields(DetailInspection detailInspectionModel, AbnormalityParams abnormalityParams, TechnicalModel technicalModel, int transactionMode);

        void setupForEdit();

        void onFailedGetDataFromServer();

        void onInspectionDeleted();

        void onInspectionFailedToDeleted();

        void onInspectionUpdated();

        void onInspectionFailedToUpdate();

        void onSaveMember(List<picItem> items, String tag);
    }

}
