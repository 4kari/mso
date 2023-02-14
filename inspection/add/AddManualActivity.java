package id.sisi.si.mso.ui.inspection.add;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import static id.sisi.si.mso.R.layout.list_item_spinner;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import id.sisi.si.mso.R;
import id.sisi.si.mso.data.AppDataManager;
import id.sisi.si.mso.data.DataHelper;
import id.sisi.si.mso.data.api.MsoService;
import id.sisi.si.mso.data.api.ServiceGenerator;
import id.sisi.si.mso.data.api.model.BaseResponse;
import id.sisi.si.mso.data.model.Abnormal;
import id.sisi.si.mso.data.model.EquipmentInfo;
import id.sisi.si.mso.data.model.Nomenclature;
import id.sisi.si.mso.ui.base.BaseActivity;
import id.sisi.si.mso.ui.dashboard.DashboardActivity;
import id.sisi.si.mso.utils.DialogUtil;
import id.sisi.si.mso.utils.NetworkUtil;
import id.sisi.si.mso.utils.TextUtils;
import okhttp3.internal.huc.OkHttpsURLConnection;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddManualActivity extends BaseActivity implements Contract.AddManualView {

    public static final int ONLINE_MODE = 0;
    public static final int OFFLINE_MODE = 1;
    private final int SUCCESS = 2;
    private final int FAILED = 1;
    private final int NONE = 0;
    private int getEquiptmentStatus = NONE;
    private boolean isBearingType = false;
    private static final String TAG = "" ;
    private int mTransactionMode = ONLINE_MODE;
    private boolean isOfflineMode = false;
    private final String TRANSACTION_MODE_KEY = "transaction_mode_key";
    private Abnormal mAbnormal;

    MaterialDialog mDialog;
    private AppDataManager mAppDataManager;

    @BindView(R.id.txtNomenclature)
    AutoCompleteTextView tetNomenclature;
    @BindView(R.id.progressbar_nomenclature)
    ProgressBar pbNomenclature;
    @BindView(R.id.txt_tipeEquipment)
    TextView txtTipeEqupt;
    @BindView(R.id.spTipeEqupt)
    Spinner spTipeEqupt;
    @BindView(R.id.btn_submit)
    Button btnSubmit;

    private boolean mIsNomenclatureAssigned = false;
    List<Nomenclature> nomenclatureList;
    private AddManualPresenter mPresenter;
    String selectedNomenclature;
    private EquipmentInfo eqInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_nomen);
        setUnbinder(ButterKnife.bind(this));
        getSupportActionBar().setTitle("Add Inspection ");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //Log.e(TAG, "username : " + DashboardActivity.USERNAME);

        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(TRANSACTION_MODE_KEY))
                mTransactionMode = savedInstanceState.getInt(TRANSACTION_MODE_KEY);
        }

        if (mAbnormal == null) {
            mAbnormal = new Abnormal();
            if (NetworkUtil.isConnected())
                mAbnormal.setSynced(true);
            else
                mAbnormal.setSynced(false);

            mIsNomenclatureAssigned = false;
        }

        mAppDataManager = AppDataManager.getInstance();
        mPresenter = new AddManualPresenter(mAbnormal);
        mPresenter.attachView(this);
    }

    @Override
    public void initView(Abnormal abnormal, int transactionMode) {
        mAbnormal = abnormal;
        mTransactionMode = transactionMode;

        tetNomenclature.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                hideKeyboard();
            }
        });
        tetNomenclature.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //check count or count in edit text and do something
                if (tetNomenclature.isPerformingCompletion()) {
                    // An item has been selected from the list. Ignore.
                    mIsNomenclatureAssigned = true;
                    return;
                }
                if (TextUtils.isNullOrEmpty(tetNomenclature.getText().toString())) {
                    mIsNomenclatureAssigned = false;
                }
            }

            @Override
            public void afterTextChanged(Editable input) {
                if (mIsNomenclatureAssigned)
                    return;

                //String plant = ((PlantItem) spPlant.getSelectedItem()).getPlant();
                String plant = "UN-" + DashboardActivity.USERNAME;
                mPresenter.loadNomenclature(input.toString(), plant, mTransactionMode);

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }
        });

        tetNomenclature.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedNomenclature = tetNomenclature.getText().toString();
                findInformations(selectedNomenclature);
            }
        });

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!TextUtils.isNullOrEmpty(tetNomenclature.getText().toString()) && getEquiptmentStatus != SUCCESS){
                    showDialog("Perhatian", "Nomenclature tidak ditemukan. Silahkan coba kembali");
                    return;
                }
                if (TextUtils.isNullOrEmpty(tetNomenclature.getText().toString())) {
                    DataHelper.formSetError(tetNomenclature, getString(R.string.error_field_nomenclature));
                    showDialog("Perhatian", "Nomenclature tidak boleh kosong");
                    return;
                }
                else {
                  if (Integer.parseInt(eqInfo.getId()) > 0) {
                      if (eqInfo.getInOrder().equalsIgnoreCase("NONE")) {
                          Intent intent = new Intent(AddManualActivity.this, AddInspectionActivity.class);
                          if(isBearingType)
                              intent.putExtra(AddInspectionActivity.BEARING_TYPE, spTipeEqupt.getSelectedItemPosition());
                          intent.putExtra(EquipmentInfo.TYPE_EXTRA, eqInfo);
                          intent.putExtra(AddInspectionPresenter.TRANSACTION_MODE_KEY, isOfflineMode ?
                                      AddInspectionPresenter.OFFLINE_MODE : AddInspectionPresenter.ONLINE_MODE);
                          intent.addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);
                          intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                          startActivity(intent);
                          finish();
                      } else {
                          if(mDialog != null) {
                              mDialog.dismiss();
                              mDialog = null;
                          }

                          mDialog = new MaterialDialog.Builder(AddManualActivity.this)
                                  .title("Perhatian")
                                  .content("Equipment memiliki order yang belum diclose, halaman akan diarahkan ke list order")
                                  .positiveText("Ok")
                                  .onPositive(new MaterialDialog.SingleButtonCallback() {
                                      @Override
                                      public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                          Intent intent = new Intent(AddManualActivity.this, EquipmentOrderListActivity.class);
                                          intent.putExtra(EquipmentInfo.TYPE_EXTRA, eqInfo);
                                          intent.putExtra(AddInspectionPresenter.TRANSACTION_MODE_KEY, isOfflineMode ?
                                                  AddInspectionPresenter.OFFLINE_MODE : AddInspectionPresenter.ONLINE_MODE);
                                          if(isBearingType)
                                              intent.putExtra(AddInspectionActivity.BEARING_TYPE, spTipeEqupt.getSelectedItemPosition());
                                          intent.addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);
                                          intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                          startActivity(intent);
                                          finish();
                                      }
                                  })
                                  .dismissListener(new DialogInterface.OnDismissListener() {
                                      @Override
                                      public void onDismiss(DialogInterface dialog) {
                                          Intent intent = new Intent(AddManualActivity.this, EquipmentOrderListActivity.class);
                                          intent.putExtra(EquipmentInfo.TYPE_EXTRA, eqInfo);
                                          intent.putExtra(AddInspectionPresenter.TRANSACTION_MODE_KEY, isOfflineMode ?
                                                  AddInspectionPresenter.OFFLINE_MODE : AddInspectionPresenter.ONLINE_MODE);
                                          if(isBearingType)
                                              intent.putExtra(AddInspectionActivity.BEARING_TYPE, spTipeEqupt.getSelectedItemPosition());
                                          intent.addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);
                                          intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                          startActivity(intent);
                                          finish();
                                      }
                                  }).build();
                          mDialog.show();
                      }
                  }
                }
            }
        });
    }

    @Override
    public void showNomenclature(List nomenclatures) {

        if (nomenclatures == null)
            return;

        if (nomenclatureList == null)
            nomenclatureList = new ArrayList<>();

        nomenclatureList.clear();
        nomenclatureList.addAll(nomenclatures);

        ArrayAdapter<Nomenclature> nomenclatureAdapter = new ArrayAdapter<>(this,
                list_item_spinner, nomenclatureList);
        tetNomenclature.setThreshold(2);
        tetNomenclature.setAdapter(nomenclatureAdapter);

        //Log.e(TAG, "activity : " + nomenclatures);

        try {
            tetNomenclature.showDropDown();
        }
        catch (WindowManager.BadTokenException e) {
        }

    }

    @Override
    public void showDialog(String title, String message) {
        DialogUtil.showBasicDialog(this, title, message);
    }

    @Override
    public void hideProgressBarFindNomenclature(boolean hide) {
        pbNomenclature.setVisibility(hide ? View.GONE : View.VISIBLE);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPresenter.detachView();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(TRANSACTION_MODE_KEY, mTransactionMode);
    }

    private void findInformations(String equipmentNumber) {
        if (!NetworkUtil.isConnected()) {
            localInformations(equipmentNumber);
            isOfflineMode = true;
        }
        else {
            MsoService msoService = ServiceGenerator.getInstance().getRetrofit().create(MsoService.class);
            Call<BaseResponse<EquipmentInfo>> call = msoService.getEquipmentInfo(equipmentNumber);
            call.enqueue(new Callback<BaseResponse<EquipmentInfo>>() {
                @Override
                public void onResponse(Call<BaseResponse<EquipmentInfo>> call, Response<BaseResponse<EquipmentInfo>> response) {
                    // showProgress(false);

                    if (response.isSuccessful() & response.body().isSuccess()) {
                        getEquiptmentStatus = SUCCESS;
                        onlineInformation(response.body().getData());
                    } else {
                        getEquiptmentStatus = FAILED;
                        handleErrorResponse(response);
                    }
                }

                @Override
                public void onFailure(Call<BaseResponse<EquipmentInfo>> call, Throwable t) {
                    getEquiptmentStatus = FAILED;
                    Toast.makeText(AddManualActivity.this, "Failed Load Data, MSG: " + t.toString(),
                            Toast.LENGTH_SHORT).show();
                    // showProgress(false);
                }
            });
        }
    }

    private void localInformations(String equipmentNumber){
        eqInfo = mAppDataManager.getCopyOfEquipmentInfo(equipmentNumber);
        if(eqInfo!=null){
            getEquiptmentStatus = SUCCESS;
            if (eqInfo.getType() != null) {
                if (eqInfo.getType().getId().equalsIgnoreCase(EquipmentInfo.TYPE_BEARING)){
                    showTipeEqupt(true);
                    isBearingType = true;
                }
                else
                    showTipeEqupt(false);
            }
            else
                showTipeEqupt(false);
        }
        else {
            getEquiptmentStatus = FAILED;
            showDialog("Perhation", "Equipment tidak ditemukan di plant anda");
        }
    }

    private void onlineInformation(EquipmentInfo equpt){
        eqInfo = equpt;
            if (eqInfo.getType() != null) {
                if (eqInfo.getType().getId().equalsIgnoreCase(EquipmentInfo.TYPE_BEARING)) {
                    isBearingType = true;
                    showTipeEqupt(true);
                }
                else
                    showTipeEqupt(false);
            }
            else{ showTipeEqupt(false); }
    }

    private void showTipeEqupt(Boolean view){
        spTipeEqupt.setVisibility(view? View.VISIBLE : View.GONE);
        txtTipeEqupt.setVisibility(view? View.VISIBLE : View.GONE);
    }

    private boolean handleErrorResponse(Response response) {
        if (response.code() == OkHttpsURLConnection.HTTP_UNAUTHORIZED || response.code() == OkHttpsURLConnection.HTTP_INTERNAL_ERROR) {
            logout();
            return false;
        } else {
//            Snackbar.make(graphicOverlay, "Equipment tidak ditemukan di plant anda",
//                    Snackbar.LENGTH_SHORT)
//                    .show();
            return false;
        }
    }

    private void logout() {
        mAppDataManager.logOut();
        onLogoutSuccess();
    }

}
