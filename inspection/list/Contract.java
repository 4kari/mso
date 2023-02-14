package id.sisi.si.mso.ui.inspection.list;

import id.sisi.si.mso.data.model.InspectionList;
import id.sisi.si.mso.ui.base.BaseView;
import io.realm.OrderedRealmCollection;

/**
 * Created by durrrr on 04-Oct-17.
 * Email: andaruwildan@gmail.com
 */
public interface Contract {

    interface Presenter {

        void getInspectionList();

        void loadInspectionList(int seqNumStart, int seqNumEnd);

        void loadMore();

        void filter(String query);

        void refreshData();

        void setCondition(int condition);

    }

    interface View extends BaseView {

        void initView();

        void populateFields(OrderedRealmCollection<InspectionList> inspectionList);

        void onDataNeedSync();

        void showLoadMore();

        void hideLoadMore();

        void onListFailedToUpdate();
    }

}