package id.sisi.si.mso.ui.inspection.detail;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;

import com.crashlytics.android.Crashlytics;
import com.squareup.picasso.Picasso;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;
import id.sisi.si.mso.R;
import id.sisi.si.mso.data.api.MsoService;
import id.sisi.si.mso.data.model.DetailInspection;
import id.sisi.si.mso.ui.base.BaseFragment;
import id.sisi.si.mso.utils.TextUtils;
import timber.log.Timber;

/**
 * Created by durrrr on 10-Oct-17.
 * Email: cab.purnama@gmail.com
 */
public class AbnormalityFragment extends BaseFragment {

    @BindView(R.id.etPlant)
    EditText etPlant;
    @BindView(R.id.etInspector)
    EditText etInspector;
    @BindView(R.id.etSource)
    EditText etSource;
    @BindView(R.id.etPriority)
    EditText etPriority;
    @BindView(R.id.etCondition)
    EditText etCondition;
    @BindView(R.id.etAction)
    EditText etAction;
    @BindView(R.id.etAbnormal)
    EditText etAbnormal;
    @BindView(R.id.matActivity)
    EditText etActivity;
    @BindView(R.id.etRemark)
    EditText etKeterangan;
    @BindView(R.id.pict_before)
    ImageButton imPictBefore;
    @BindView(R.id.progressbar_pict_before)
    ProgressBar progressBarPictBefore;
    @BindView(R.id.pict_after)
    ImageButton imPictAfter;
    @BindView(R.id.progressbar_pict_after)
    ProgressBar progressBarPictAfter;

    private DetailInspection mDetailInspectionModel;

    public static AbnormalityFragment newInstance(DetailInspection detailInspectionModel) {
        AbnormalityFragment instance = new AbnormalityFragment();
        Bundle args = new Bundle();
        args.putParcelable(DetailInspection.TYPE_EXTRA, detailInspectionModel);
        instance.setArguments(args);
        return instance;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_abnormal_detail_1, container, false);
        ButterKnife.bind(this, view);

        if (getArguments().getParcelable(DetailInspection.TYPE_EXTRA) != null) {
            mDetailInspectionModel = getArguments().getParcelable(DetailInspection.TYPE_EXTRA);
        }

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView();
    }

    private void initView() {
        try {
            etAbnormal.setText(mDetailInspectionModel.getAbnormalValues().getAbnormal());
            etAbnormal.setFocusable(false);

            etActivity.setText(mDetailInspectionModel.getAbnormalValues().getActivity());
            etActivity.setFocusable(false);

            etKeterangan.setText(mDetailInspectionModel.getAbnormalValues().getRemark());
            etKeterangan.setFocusable(false);

            etInspector.setText(mDetailInspectionModel.getAbnormalValues().getReportedBy().getLabel());
            etInspector.setFocusable(false);

            etPlant.setText(mDetailInspectionModel.getAbnormalValues().getPlant().getPlant()
                    + " - " + mDetailInspectionModel.getAbnormalValues().getPlant().getName());
            etPlant.setFocusable(false);

            etSource.setText(mDetailInspectionModel.getAbnormalValues().getSource().getLabel());
            etSource.setFocusable(false);

            etCondition.setText(mDetailInspectionModel.getAbnormalValues().getCondition().getLabel());
            etCondition.setFocusable(false);

            etPriority.setText(mDetailInspectionModel.getAbnormalValues().getPriority().getLabel());
            etPriority.setFocusable(false);

            etAction.setText(mDetailInspectionModel.getAbnormalValues().getAction().getLabel());
            etAction.setFocusable(false);

            if (mDetailInspectionModel.getAbnormalValues().getPictBeforeLocalPath() != null) {
                Timber.d(mDetailInspectionModel.getAbnormalValues().getPictBeforeLocalPath());
                try {
                    Picasso.with(getContext())
                            .load(new File(mDetailInspectionModel.getAbnormalValues()
                                    .getPictBeforeLocalPath()))
                            .into(imPictBefore);
                } catch (Exception e) {

                }
            }

            if (mDetailInspectionModel.getAbnormalValues().getPictBefore() != null) {
                Picasso.with(getContext()).load(MsoService.URL_PICT_PATH + mDetailInspectionModel.getAbnormalValues().getPictBefore())
                        .into(imPictBefore);
            }

            if (TextUtils.isNullOrEmpty(mDetailInspectionModel.getEquipmentTypeId()))
                ((InspectionDetailActivity) getActivity()).measureViewPager(this);

        } catch (NullPointerException e) {
            Crashlytics.logException(e.fillInStackTrace());
        }
    }
}
