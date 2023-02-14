package id.sisi.si.mso.ui.inspection.add;

import android.support.annotation.Nullable;
import android.widget.EditText;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import id.sisi.si.mso.data.model.Abnormal;
import id.sisi.si.mso.data.model.AbnormalityParams;
import id.sisi.si.mso.data.model.Bearing;
import id.sisi.si.mso.data.model.InspectionActivity;
import id.sisi.si.mso.data.model.InspectionDetail;
import id.sisi.si.mso.data.model.Order;
import id.sisi.si.mso.data.model.TechnicalModel;
import id.sisi.si.mso.data.model.picItem;
import id.sisi.si.mso.ui.base.BaseView;
import io.realm.OrderedRealmCollection;

/**
 * Created by durrrr on 04-Oct-17.
 * Email: andaruwildan@gmail.com
 */
public interface Contract {

    interface ActivityCallback {

        void onConditionChanged(int condition);
        void onConditionChanged2(int condition, int[] index);
        void onSetCondition(ArrayList<EditText> condPackage, int status);

        void removeAbnormalityStep();
    }

    interface getMemberCallback{

        void onSearchItem(String query, String tag);

        void onShowMember(List<picItem> picItems, String tag);

        //void getSelectedMember(List<String> mChoosedMembers);

        void onConditionChanged(int condition);

        void onSetCondition(ArrayList<EditText> condPackage, int status);

    }

    interface AddManualPresenter {

        void getNomenclature();

        void loadNomenclature(final String keyword, final String plant, int transactionMode);

    }

    interface AddInspectionPresenter {

        void setEquipmentCondition(int condition);

        void attemptUploadImage(File imageFile);

        void attemptSave(InspectionDetail inspectionDetail, @Nullable TechnicalModel technicalModel,
                         @Nullable Abnormal abnormal, @Nullable TechnicalModel technicalModel2,
                         @Nullable TechnicalModel technicalModel3, @Nullable InspectionDetail condition, int bearingType);

        void attemptLocalSave(InspectionDetail inspectionDetail, @Nullable TechnicalModel technicalModel,
                              @Nullable Abnormal abnormal, @Nullable TechnicalModel technicalModel2,
                              @Nullable TechnicalModel technicalModel3, @Nullable InspectionDetail condition, int bearingType);

        void getAutocompleteMember(String query, String tag);

    }

    interface AddInspectionView extends BaseView {

        void initView(ArrayList<InspectionActivity> activities);

        void uploadImageResult(int result, @Nullable String imagePath, @Nullable String imageUrl);

        void setupAbnormalityStep(AbnormalityParams abnormalityParams);
        void setupAbnormalityStep2(AbnormalityParams abnormalityParams, int[] index);
        void removeAbnormalityStep();

        void onSaveSuccess();

        void onSaveFailed();

        void onOfflineSave();

        void onOfflineSaveSuccess();

        void onOfflineSaveFailed();

        void onInitFailed();

        void onSaveMember(List<picItem> items, String tag);
    }

    interface AddManualView extends BaseView {

        void initView(Abnormal abnomornaml, int transactionMode);

        void showNomenclature(List nomenclatures);

        void hideProgressBarFindNomenclature(boolean hide);

    }

    interface EquipmentOrderListPresenter {

        void loadOrderList(int seqNumStart, int seqNumEnd);

        void filterOrder(String newText);

        void refreshData();

        void loadMore();
    }

    interface EquipmentOrderListView extends BaseView {

        void initView(int transactionMode);

        void filterResult(String query);

        void onFilterCompleted(OrderedRealmCollection<Order> data);

        void populateFields(OrderedRealmCollection<Order> data);

        void showLoadMore();

        void hideLoadMore();

        void onDataNeedSync();

        void onListFailedToUpdate();

        void onNotAvailable();

        void onThereIsUnsyncedData();

        void onOffline();
    }

    interface bearingFragment {

        void getDataBearing(Bearing bearing);

    }
}
