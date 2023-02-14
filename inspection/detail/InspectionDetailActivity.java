package id.sisi.si.mso.ui.inspection.detail;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.crashlytics.android.Crashlytics;
import com.jakewharton.picasso.OkHttp3Downloader;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import id.sisi.si.mso.R;
import id.sisi.si.mso.data.api.MsoService;
import id.sisi.si.mso.data.api.ServiceGenerator;
import id.sisi.si.mso.data.model.AbnormalityParams;
import id.sisi.si.mso.data.model.Bearing;
import id.sisi.si.mso.data.model.BeltWeighter;
import id.sisi.si.mso.data.model.DetailInspection;
import id.sisi.si.mso.data.model.EquipmentInfo;
import id.sisi.si.mso.data.model.TechnicalModel;
import id.sisi.si.mso.data.model.Transformer;
import id.sisi.si.mso.data.model.picItem;
import id.sisi.si.mso.ui.base.BaseActivity;
import id.sisi.si.mso.ui.inspection.detail.adapter.EquipmentViewPagerAdapter;
import id.sisi.si.mso.ui.photoview.PhotoViewActivity;
import id.sisi.si.mso.ui.widget.InspectionDetailViewPager;
import id.sisi.si.mso.utils.DialogUtil;
import id.sisi.si.mso.utils.TextUtils;
import timber.log.Timber;

/**
 * Created by durrrr on 04-Oct-17.
 * Email: cab.purnama@gmail.com
 */
public class InspectionDetailActivity extends BaseActivity
        implements Contract.View, View.OnClickListener, Contract.getMemberCallback{

    @BindView(R.id.tab_layout)
    TabLayout tabLayout;
    @BindView(R.id.pager)
    InspectionDetailViewPager viewPager;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.toolbar_layout)
    CollapsingToolbarLayout collapsingToolbarLayout;
    @BindView(R.id.ibMenu)
    ImageButton ibMenu;
    @BindView(R.id.nested)
    NestedScrollView nestedScrollView;
    @BindView(R.id.detail_container)
    LinearLayout detailContainer;

    @BindView(R.id.imdetailinspection)
    ImageView imDetailInspection;
    @BindView(R.id.progressbar_pict_detail)
    ProgressBar progressBarDetail;
    @BindView(R.id.tvequipname)
    TextView tvEquipName;
    @BindView(R.id.tv_tanggal)
    TextView tvTanggal;
    @BindView(R.id.sp_condition)
    Spinner spCondition;
    /*@BindView(R.id.ll_activity)
    LinearLayout llActivity;*/
    @BindView(R.id.et_activity)
    EditText etActivity;
    @BindView(R.id.et_description)
    TextView etDescription;

    @BindView(R.id.btn_submit)
    Button btnSubmit;

    @BindView(R.id.tvequipdesc)
    TextView tvEquipDesc;

    private InspectionDetailPresenter mPresenter;
    private String title = "", title2 = "", title3 = "";
    private TechnicalFragment mTechnicalFragment;
    private TechnicalFragment mTechnicalFragment2;
    private TechnicalFragment mTechnicalFragment3;
    private AbnormalityFragment mAbnormalityFragment;

    private EquipmentViewPagerAdapter mViewPagerAdapter;

    private PopupMenu popupMenu;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_inspection_detail);
        setUnbinder(ButterKnife.bind(this));

        if (getIntent() != null) {
            mPresenter = new InspectionDetailPresenter(getIntent().getLongExtra("inspection_no", -1),
                    getIntent().getStringExtra("equipment_name"),
                    getIntent().getIntExtra("synced", InspectionDetailPresenter.ONLINE_MODE));

            mPresenter.attachView(this);
        }
    }

    @Override
    public void initView() {
        toolbar.setTitle(getIntent().getStringExtra("equipment_name"));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        collapsingToolbarLayout.setExpandedTitleTextAppearance(R.style.exp_toolbar_title);

        spCondition.setEnabled(false);
        etDescription.setFocusable(false);

        ibMenu.setOnClickListener(this);
        tabLayout.setupWithViewPager(viewPager);
        btnSubmit.setOnClickListener(this);
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    private void initPopupMenu(boolean isEditEnabled) {
        popupMenu = new PopupMenu(this, ibMenu);

        try {
            Field[] fields = popupMenu.getClass().getDeclaredFields();
            for (Field field : fields) {
                if ("mPopup".equals(field.getName())) {
                    field.setAccessible(true);
                    Object menuPopupHelper = field.get(popupMenu);
                    Class<?> classPopupHelper = Class.forName(menuPopupHelper.getClass().getName());
                    Method setForceIcons = classPopupHelper.getMethod("setForceShowIcon", boolean.class);
                    setForceIcons.invoke(menuPopupHelper, true);
                    break;
                }
            }
        } catch (Exception e) {
            Crashlytics.logException(e.fillInStackTrace());
        }


        popupMenu.getMenuInflater().inflate(R.menu.menu_detail_inspection, popupMenu.getMenu());
        if (!isEditEnabled)
            popupMenu.getMenu().findItem(R.id.action_edit).setEnabled(false);

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.action_edit:
                        setupForEdit();
                        break;
                    case R.id.action_delete:
                        MaterialDialog dialog = new MaterialDialog.Builder(InspectionDetailActivity.this)
                                .content("Apakah Anda yakin ingin menghapus item ini?")
                                .positiveText("Ya")
                                .onPositive(new MaterialDialog.SingleButtonCallback() {
                                    @Override
                                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                        mPresenter.attemptDeleteInspection();
                                    }
                                })
                                .negativeText("Batal")
                                .build();
                        dialog.show();
                        break;
                }

                return true;
            }
        });

    }

    @Override
    public void populateFields(final DetailInspection detailInspection, AbnormalityParams abnormalityParamsModel, TechnicalModel technicalModel, final int transactionMode) {
        if (detailInspection != null) {
            try {
                setupViewPager(detailInspection, abnormalityParamsModel, technicalModel);

                tvEquipName.setText(getIntent().getStringExtra("equipment_name"));
                tvTanggal.setText(detailInspection.getDate());
                spCondition.setSelection(Integer.parseInt(detailInspection.getCondition()));
                etDescription.setText(detailInspection.getDescription());
                etActivity.setText(detailInspection.getActivity());
                tvEquipDesc.setText(detailInspection.getEquipmentDesc());

                /*String[] activities = detailInspection.getActivity().split("\n");
                for (int i = 0; i < activities.length; i++) {
                    LayoutInflater layoutInflater = (LayoutInflater) getBaseContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    View activityView = layoutInflater.inflate(R.layout.view_inspection_activity, llActivity, false);
                    llActivity.addView(activityView);

                    TextView tvActivity = activityView.findViewById(R.id.et_activity);
                    tvActivity.setFocusable(false);
                    tvActivity.setText(activities[i]);
                }*/

                progressBarDetail.setVisibility(View.VISIBLE);
                com.squareup.picasso.Callback picassoCallback = new Callback() {
                    @Override
                    public void onSuccess() {
                        try {
                            progressBarDetail.setVisibility(View.INVISIBLE);
                            imDetailInspection.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent intent = new Intent(InspectionDetailActivity.this, PhotoViewActivity.class);
                                    if (transactionMode == InspectionDetailPresenter.ONLINE_MODE)
                                        intent.putExtra(PhotoViewActivity.IMAGE_LINK_INTENT,
                                                MsoService.URL_PICT_PATH + detailInspection.getPhotopath());
                                    else
                                        intent.putExtra(PhotoViewActivity.IMAGE_PATH_INTENT, detailInspection.getPhotopath());
                                    startActivity(intent);
                                }
                            });
                        } catch (NullPointerException e) {
                            Crashlytics.logException(e.fillInStackTrace());
                        }
                    }

                    @Override
                    public void onError() {
                        try {
                            progressBarDetail.setVisibility(View.INVISIBLE);
                        } catch (NullPointerException e) {

                        }
                    }
                };
                if (transactionMode == InspectionDetailPresenter.ONLINE_MODE) {
                    String imageLink = new StringBuilder().append(MsoService.URL_PATH).append(detailInspection.getPhotopath().replace("media/dtupload/", "getImage/")).toString();
                    new Picasso.Builder(this).downloader(new OkHttp3Downloader(ServiceGenerator.getInstance().getClient())).build().load(imageLink).into(imDetailInspection, picassoCallback);
                    /*Picasso.with(this).load(MsoService.URL_PICT_PATH + detailInspection.getPhotopath())
                            .into(imDetailInspection, picassoCallback);*/
                }
                else
                    Picasso.with(this).load(new File(detailInspection.getPhotopath()))
                            .into(imDetailInspection, picassoCallback);
            } catch (NullPointerException e) {
                Crashlytics.logException(e.fillInStackTrace());
            }
        }
    }

    private void setupViewPager(DetailInspection detailInspectionModel, AbnormalityParams abnormalityParamsModel, TechnicalModel technicalModel) {
        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        for (Fragment fragment : fragments) {
            if (fragment instanceof TechnicalFragment) {
                mTechnicalFragment = (TechnicalFragment) fragment;
            }

            if (fragment instanceof AbnormalityFragment)
                mAbnormalityFragment = (AbnormalityFragment) fragment;
        }

        mViewPagerAdapter = new EquipmentViewPagerAdapter(getSupportFragmentManager());
        viewPager.setOffscreenPageLimit(2);

        if (!TextUtils.isNullOrEmpty(detailInspectionModel.getEquipmentTypeId())) {
            if (mTechnicalFragment == null) {
                switch (StringUtils.lowerCase(detailInspectionModel.getEquipmentTypeId())) {
                    case DetailInspection.TYPE_BEARING:
                        Timber.d("equipment subtype id : " + detailInspectionModel.getEquipmentSubTypeId());
                        if(detailInspectionModel.getEquipmentSubTypeId().equalsIgnoreCase(EquipmentInfo.TYPE_MOTOR_EXIST))
                        {   mTechnicalFragment = BearingFragment.newInstance(detailInspectionModel, (Bearing) technicalModel);
                            mTechnicalFragment2 = BearingFragmentAfter.newInstance(detailInspectionModel, (Bearing) technicalModel);
                            title = detailInspectionModel.getEquipmentTypeName() + " before";
                            title2 = detailInspectionModel.getEquipmentTypeName() + " after";
                        }
                        else if(detailInspectionModel.getEquipmentSubTypeId().equalsIgnoreCase(EquipmentInfo.TYPE_MCC_EXIST)) {
                            mTechnicalFragment = BearingMccFragment.newInstance(detailInspectionModel, (Bearing) technicalModel);
                            title = detailInspectionModel.getEquipmentSubTypeName();
                        }else if(detailInspectionModel.getEquipmentSubTypeId().equalsIgnoreCase(EquipmentInfo.TYPE_MEKANIKAL)){
                            mTechnicalFragment = BearingMekanikalFragment.newInstance(detailInspectionModel, (Bearing) technicalModel);
                            title = detailInspectionModel.getEquipmentSubTypeName();
                        }
                        else
                        {
                            mTechnicalFragment = BearingServiceFragmentK.newInstance(detailInspectionModel, (Bearing) technicalModel);
                            mTechnicalFragment2 = BearingServiceFragmentL.newInstance(detailInspectionModel, (Bearing) technicalModel);
                            mTechnicalFragment3 = BearingServiceFragmentM.newInstance(detailInspectionModel, (Bearing) technicalModel);
                            title = detailInspectionModel.getEquipmentSubTypeName() + " K";
                            title2 = detailInspectionModel.getEquipmentSubTypeName() + " L";
                            title3 = detailInspectionModel.getEquipmentSubTypeName() + " M";

                        }
                            break;
                    case DetailInspection.TYPE_BELT_WEIGHTER:
                        mTechnicalFragment = BeltWeighterFragment.newInstance(detailInspectionModel, (BeltWeighter) technicalModel);
                        title = detailInspectionModel.getEquipmentTypeName();
                        break;
                    case DetailInspection.TYPE_TRANSFORMER:
                        mTechnicalFragment = TransformerFragment.newInstance(detailInspectionModel, (Transformer) technicalModel);
                        title = detailInspectionModel.getEquipmentTypeName();
                        break;
                    default:
                        mTechnicalFragment = null;
                        break;
                }

                if (mTechnicalFragment != null) {
                    mViewPagerAdapter.addFragment(mTechnicalFragment, StringUtils.capitalize(title));
                     if(mTechnicalFragment2!=null)
                         mViewPagerAdapter.addFragment(mTechnicalFragment2, StringUtils.capitalize(title2));
                    if(mTechnicalFragment3!=null)
                        mViewPagerAdapter.addFragment(mTechnicalFragment3, StringUtils.capitalize(title3));
                     initPopupMenu(true);
                }
            }
        } else {
            initPopupMenu(false);
        }

        if (detailInspectionModel.getCondition().equals("0")) {
            if (mAbnormalityFragment == null) {
                mAbnormalityFragment = AbnormalityFragment.newInstance(detailInspectionModel);
                mViewPagerAdapter.addFragment(mAbnormalityFragment, StringUtils.capitalize("Abnormalitas"));
            }
        }

        if (mViewPagerAdapter.getCount() > 0) {
            viewPager.setAdapter(mViewPagerAdapter);
            mViewPagerAdapter.notifyDataSetChanged();
        }
    }

    public void measureViewPager(Fragment fragment) {
        viewPager.measureCurrentView(fragment.getView());
    }

    @Override
    public void setupForEdit() {
        getSupportActionBar().setTitle("Update Inspection");
        btnSubmit.setEnabled(true);
        btnSubmit.setVisibility(View.VISIBLE);
        mTechnicalFragment.setupForEdit();
        if(mTechnicalFragment2!=null)
            mTechnicalFragment2.setupForEdit();
        if(mTechnicalFragment3!=null)
            mTechnicalFragment3.setupForEdit();
        nestedScrollView.scrollTo(0, viewPager.getBottom());
    }

    private void attemptUpdateInspection() {
        mTechnicalFragment.setDataToModel();
        if(mTechnicalFragment2!=null)
            mTechnicalFragment2.setDataToModel();
        if(mTechnicalFragment3!=null)
            mTechnicalFragment3.setDataToModel();
        mPresenter.attemptUpdateInspection();
    }

    @Override
    public void onInspectionUpdated() {
        getSupportActionBar().setTitle("Detail Inspection");
        mTechnicalFragment.setupForDetail();
        if(mTechnicalFragment2!=null)
            mTechnicalFragment2.setupForEdit();
        if(mTechnicalFragment3!=null)
            mTechnicalFragment3.setupForEdit();
        DialogUtil.showBasicDialog(this, "Sukses", "Data berhasil diupdate");
        btnSubmit.setEnabled(false);
        btnSubmit.setVisibility(View.GONE);
    }

    @Override
    public void onInspectionDeleted() {
        DialogUtil.showEndDialog(this, "Sukses", "Data berhasil dihapus");
    }

    @Override
    public void onInspectionFailedToDeleted() {
        DialogUtil.showBasicDialog(this, "Gagal", "Data gagal dihapus");
    }

    @Override
    public void onInspectionFailedToUpdate() {
        getSupportActionBar().setTitle("Detail Inspection");
        mTechnicalFragment.setupForDetail();
        if(mTechnicalFragment2!=null)
            mTechnicalFragment2.setupForDetail();
        if(mTechnicalFragment3!=null)
            mTechnicalFragment3.setupForDetail();
        DialogUtil.showBasicDialog(this, "Gagal", "Terjadi kesalahan");
        btnSubmit.setVisibility(View.GONE);
    }

    @Override
    public void onSaveMember(List<picItem> items, String tag) {
        onShowMember(items, tag);
    }

    @Override
    public void showProgress(String title, String message) {
        DialogUtil.showProgressDialog(this, title, message, false);
    }

    @Override
    public void hideProgress() {
        DialogUtil.dismiss();
    }

    @Override
    public void showDialog(String title, String messsage) {
        DialogUtil.showBasicDialog(this, title, messsage);
    }

    @Override
    public void onFailedGetDataFromServer() {
        showDialog("Error", "Gagal mendapatkan data dari server");
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ibMenu:
                if (popupMenu != null)
                    popupMenu.show();
                break;
            case R.id.btn_submit:
                MaterialDialog dialog = new MaterialDialog.Builder(InspectionDetailActivity.this)
                        .content("Apakah Anda yakin memperbarui item ini?")
                        .positiveText("Ya")
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                attemptUpdateInspection();
                            }
                        })
                        .negativeText("Batal")
                        .build();
                dialog.show();
                break;
        }
    }

    @Override
    protected void onDestroy() {
        mPresenter.detachView();
        mTechnicalFragment = null;
        mTechnicalFragment2 = null;
        mTechnicalFragment3 = null;
        mAbnormalityFragment = null;
        super.onDestroy();
    }

    @Override
    public void onSearchItem(String query, String tag) {
        mPresenter.getAutocompleteMember(query, tag);
    }

    @Override
    public void onShowMember(List<picItem> picItems, String tag) {
        switch (tag){
            case "K":
                mTechnicalFragment.updateMember(picItems);
                break;
            case "L":
                mTechnicalFragment2.updateMember(picItems);
                break;
            case "M":
                mTechnicalFragment3.updateMember(picItems);
                break;
        }

    }
}