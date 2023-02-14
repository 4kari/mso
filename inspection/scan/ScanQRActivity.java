package id.sisi.si.mso.ui.inspection.scan;

import static id.sisi.si.mso.ui.autonomous.list.AddQRAutonomousActivity.DATATYPE;
import static id.sisi.si.mso.ui.autonomous.list.AddQRAutonomousActivity.DATA_EQUIPNAME;
import static id.sisi.si.mso.ui.autonomous.list.AddQRAutonomousActivity.DATA_ID_ROUTE;
import static id.sisi.si.mso.ui.autonomous.list.AddQRAutonomousActivity.DATA_TYPE;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import java.io.IOException;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import id.sisi.si.mso.R;
import id.sisi.si.mso.data.AppDataManager;
import id.sisi.si.mso.data.api.MsoService;
import id.sisi.si.mso.data.api.ServiceGenerator;
import id.sisi.si.mso.data.api.model.BaseResponse;
import id.sisi.si.mso.data.api.model.ListResponse;
import id.sisi.si.mso.data.model.EquipmentInfo;
import id.sisi.si.mso.data.model.EquipmentList;
import id.sisi.si.mso.data.model.Nomenclature;
import id.sisi.si.mso.ui.abnormality.addOrEdit.AbnormalityAddOrEditActivity;
import id.sisi.si.mso.ui.autonomous.list.AddQRAutonomousActivity;
import id.sisi.si.mso.ui.base.BaseActivity;
import id.sisi.si.mso.ui.base.BaseView;
import id.sisi.si.mso.ui.camera.BarcodeGraphic;
import id.sisi.si.mso.ui.camera.BarcodeGraphicTracker;
import id.sisi.si.mso.ui.camera.BarcodeTrackerFactory;
import id.sisi.si.mso.ui.camera.CameraSource;
import id.sisi.si.mso.ui.camera.CameraSourcePreview;
import id.sisi.si.mso.ui.camera.GraphicOverlay;
import id.sisi.si.mso.ui.dashboard.DashboardActivity;
import id.sisi.si.mso.ui.equipment.detail.EquipmentDetailActivity;
import id.sisi.si.mso.ui.inspection.add.AddInspectionActivity;
import id.sisi.si.mso.ui.inspection.add.AddInspectionPresenter;
import id.sisi.si.mso.ui.inspection.add.EquipmentOrderListActivity;
import id.sisi.si.mso.utils.NetworkUtil;
import id.sisi.si.mso.utils.TextUtils;
import io.realm.Realm;
import okhttp3.internal.huc.OkHttpsURLConnection;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

/**
 * Created by durrrr on 11-Oct-17.
 * Email: andaruwildan@gmail.com
 */
public class ScanQRActivity extends BaseActivity implements BaseView {

    // constants used to pass extra data in the intent
    public static final String AutoFocus = "AutoFocus";
    public static final String UseFlash = "UseFlash";
    public static final int ACTION_INSPECTION = 20;
    public static final int ACTION_ADD = 21;

    public static final int ACTION_DETAIL = 22;
    public static final int ACTION_ADD_ROUTED = 23;
    public static final int ACTION_ADD_EQUIPMENT = 24;
    public static final int NO_RESULT = 40;
    public static final String ACTION_INTENT_KEY = "action_intent_key";

    private int mActionType = ACTION_INSPECTION;

    // intent request code to handle updating play services if needed.
    private static final int RC_HANDLE_GMS = 9001;
    // permission request codes need to be < 256
    private static final int RC_HANDLE_CAMERA_PERM = 2;
    static ProgressBar progressBar;
    private static Resources mResources;
    @BindView(R.id.preview)
    CameraSourcePreview cspPreview;
    @BindView(R.id.cbflash)
    CheckBox cbFlash;
    @BindView(R.id.graphicOverlay)
    GraphicOverlay<BarcodeGraphic> graphicOverlay;
    MaterialDialog mDialog;
    private CameraSource mCameraSource;
    private boolean useautoFocus = false;
    private boolean useFlash = false;
    private AppDataManager mAppDataManager;
    String title, plant;


    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public static void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {

            int shortAnimTime = mResources.getInteger(android.R.integer.config_shortAnimTime);

//            container.setVisibility(show ? View.GONE : View.VISIBLE);
//            container.animate().setDuration(shortAnimTime).alpha(
//                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
//                @Override
//                public void onAnimationEnd(Animator animation) {
//                    container.setVisibility(show ? View.GONE : View.VISIBLE);
//                }
//            });

            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
            progressBar.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
            //container.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_qr);
        ButterKnife.bind(this);

        if (getIntent().hasExtra(ACTION_INTENT_KEY))
            mActionType = getIntent().getIntExtra(ACTION_INTENT_KEY, 0);

        if (mActionType == ACTION_ADD)
            title = "Add Abnormalitas";
        else if(mActionType == ACTION_INSPECTION)
            title = "New Inspection";
        else if(mActionType == ACTION_ADD_ROUTED)
            title = "New Autonomous Route";
        else if(mActionType == ACTION_ADD_EQUIPMENT)
            title = "New Autonomous Equipment";
        else
            title = "Search Equipment";

        getSupportActionBar().setTitle(title);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        progressBar = findViewById(R.id.progress_bar);

        plant = "UN1-" + DashboardActivity.USERNAME;

        PackageManager pm = getPackageManager();
        if (pm.hasSystemFeature(PackageManager.FEATURE_CAMERA) && pm.hasSystemFeature(PackageManager.FEATURE_CAMERA_AUTOFOCUS)) {
            useautoFocus = true;
        }

        mAppDataManager = AppDataManager.getInstance();
        mResources = getResources();

        cbFlash.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (cspPreview != null) {
                    cspPreview.stop();
                }
                if (isChecked) {
                    createCameraSource(useautoFocus, isChecked);
                } else {
                    createCameraSource(useautoFocus, useFlash);
                }
                startCameraSource();
            }
        });

        // Check for the camera permission before accessing the camera.  If the
        // permission is not granted yet, request permission.
        int rc = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        if (rc == PackageManager.PERMISSION_GRANTED) {
            createCameraSource(useautoFocus, useFlash);
        } else {
            requestCameraPermission();
        }

        Snackbar.make(graphicOverlay, "Pinch or Stretch to zoom",
                Snackbar.LENGTH_LONG)
                .show();

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

    private void requestCameraPermission() {
        final String[] permissions = new String[]{Manifest.permission.CAMERA};

        if (!ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.CAMERA)) {
            ActivityCompat.requestPermissions(this, permissions, RC_HANDLE_CAMERA_PERM);
            return;
        }

        final Activity thisActivity = this;

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ActivityCompat.requestPermissions(thisActivity, permissions,
                        RC_HANDLE_CAMERA_PERM);
            }
        };

        findViewById(R.id.topLayout).setOnClickListener(listener);
        Snackbar.make(graphicOverlay, "Access to the camera is needed for detection",
                Snackbar.LENGTH_INDEFINITE)
                .setAction("OK", listener)
                .show();
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
//        boolean b = scaleGestureDetector.onTouchEvent(e);
//        boolean c = gestureDetector.onTouchEvent(e);
//        return b || c || super.onTouchEvent(e);
        return super.onTouchEvent(e);
    }

    @SuppressLint("InlinedApi")
    private void createCameraSource(boolean autoFocus, boolean useFlash) {
        Context context = getApplicationContext();

        // A barcode detector is created to track barcodes.  An associated multi-processor instance
        // is set to receive the barcode detection results, track the barcodes, and maintain
        // graphics for each barcode on screen.  The factory is used by the multi-processor to
        // create a separate tracker instance for each barcode.
        BarcodeDetector barcodeDetector = new BarcodeDetector.Builder(context).build();
        BarcodeTrackerFactory barcodeFactory = new BarcodeTrackerFactory(graphicOverlay, new BarcodeGraphicTracker.NewDetectionListener() {
            @Override
            public void onNewDetection(Barcode barcode) {
                if(mActionType == ACTION_ADD) {
                    getNomenclature(barcode.displayValue);
                }
                else if(mActionType == ACTION_INSPECTION)
                    findInformation(barcode.displayValue);

                else if(mActionType == ACTION_ADD_ROUTED){
                    findByRoute(barcode.displayValue);
//                    Log.d("Barcode",barcode.displayValue);
                }
                else if(mActionType == ACTION_ADD_EQUIPMENT){
                    findRouteByEquipment(barcode.displayValue);
//                    Log.d("Barcode",barcode.displayValue);
                }
                else
                    findDetailEquipment(barcode.displayValue);
            }
        });
        barcodeDetector.setProcessor(
                new MultiProcessor.Builder<>(barcodeFactory).build());

        if (!barcodeDetector.isOperational()) {
            // Note: The first time that an app using the barcode or face API is installed on a
            // device, GMS will download a native libraries to the device in order to do detection.
            // Usually this completes before the app is run for the first time.  But if that
            // download has not yet completed, then the above call will not detect any barcodes
            // and/or faces.
            //
            // isOperational() can be used to check if the required native libraries are currently
            // available.  The detectors will automatically become operational once the library
            // downloads complete on device.

            // Check for low storage.  If there is low storage, the native library will not be
            // downloaded, so detection will not become operational.
            IntentFilter lowstorageFilter = new IntentFilter(Intent.ACTION_DEVICE_STORAGE_LOW);
            boolean hasLowStorage = registerReceiver(null, lowstorageFilter) != null;

            if (hasLowStorage) {
                Toast.makeText(this, R.string.low_storage_error, Toast.LENGTH_LONG).show();
                Timber.d(getString(R.string.low_storage_error));
            }
        }

        // Creates and starts the camera.  Note that this uses a higher resolution in comparison
        // to other detection examples to enable the barcode detector to detect small barcodes
        // at long distances.
        CameraSource.Builder builder = new CameraSource.Builder(getApplicationContext(), barcodeDetector)
                .setFacing(CameraSource.CAMERA_FACING_BACK)
                //.setRequestedPreviewSize(1600, 1024)
                .setRequestedFps(15.0f);

        // make sure that auto focus is an available option
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            builder = builder.setFocusMode(
                    autoFocus ? Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE : null);
        }

        mCameraSource = builder
                .setFlashMode(useFlash ? Camera.Parameters.FLASH_MODE_TORCH : null)
                .build();
    }

    private void findByRoute(String displayValue) {
        Intent moveWithObjectIntent = new Intent(ScanQRActivity.this, AddQRAutonomousActivity.class);
        moveWithObjectIntent.putExtra(DATA_ID_ROUTE, displayValue);
        moveWithObjectIntent.putExtra(DATA_TYPE, "id");
        moveWithObjectIntent.putExtra(DATATYPE, "1");
        startActivity(moveWithObjectIntent);
        finish();
    }
    private void findRouteByEquipment(String displayValue) {
        Intent moveWithObjectIntent = new Intent(ScanQRActivity.this, AddQRAutonomousActivity.class);
        moveWithObjectIntent.putExtra(DATA_EQUIPNAME, displayValue);
        moveWithObjectIntent.putExtra(DATA_TYPE, "eq");
        moveWithObjectIntent.putExtra(DATATYPE, "2");
        startActivity(moveWithObjectIntent);
        finish();
    }
    @Override
    protected void onResume() {
        super.onResume();
        startCameraSource();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (cspPreview != null) {
            cspPreview.stop();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cspPreview != null) {
            cspPreview.release();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode != RC_HANDLE_CAMERA_PERM) {
            Timber.d("Got unexpected permission result: " + requestCode);
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            return;
        }

        if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Timber.d("Camera permission granted - initialize the camera source");
            // we have permission, so create the camerasource
            boolean autoFocus = getIntent().getBooleanExtra(AutoFocus, useautoFocus);
            boolean useFlash = getIntent().getBooleanExtra(UseFlash, false);
            createCameraSource(autoFocus, useFlash);
            return;
        }

        Timber.d("Permission not granted: results len = " + grantResults.length +
                " Result code = " + (grantResults.length > 0 ? grantResults[0] : "(empty)"));

        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                finish();
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Multitracker sample")
                .setMessage("This application cannot run because it does not have the camera permission. The current work will now exit.")
                .setPositiveButton("OK", listener)
                .show();
    }

    private void startCameraSource() throws SecurityException {
        // check that the device has play services available.
        int code = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(
                getApplicationContext());
        if (code != ConnectionResult.SUCCESS) {
            Dialog dlg =
                    GoogleApiAvailability.getInstance().getErrorDialog(this, code, RC_HANDLE_GMS);
            dlg.show();
        }

        if (mCameraSource != null) {
            try {
                cspPreview.start(mCameraSource, graphicOverlay);
            } catch (IOException e) {
                Timber.d("Unable to start camera source.", e);
                mCameraSource.release();
                mCameraSource = null;
            }
        }
    }

    private boolean onTap(float rawX, float rawY) {
        // Find tap point in preview frame coordinates.
        int[] location = new int[2];
        graphicOverlay.getLocationOnScreen(location);
        float x = (rawX - location[0]) / graphicOverlay.getWidthScaleFactor();
        float y = (rawY - location[1]) / graphicOverlay.getHeightScaleFactor();

        // Find the barcode whose center is closest to the tapped point.
        Barcode barcode = null;
        float bestDistance = Float.MAX_VALUE;
        for (BarcodeGraphic graphic : graphicOverlay.getGraphics()) {
            Barcode barcode2 = graphic.getBarcode();
            if (barcode2.getBoundingBox().contains((int) x, (int) y)) {
                // Exact hit, no need to keep looking.
                barcode = barcode2;
                break;
            }
            float dx = x - barcode2.getBoundingBox().centerX();
            float dy = y - barcode2.getBoundingBox().centerY();
            float distance = (dx * dx) + (dy * dy);  // actually squared distance
            if (distance < bestDistance) {
                barcode = barcode2;
                bestDistance = distance;
            }
        }

        if (barcode != null) {
            findInformation(barcode.displayValue);
            return true;
        }
        return false;
    }

    private void getNomenclature(final String keyword) {
        if (!NetworkUtil.isConnected()) {
            getLocalNomenclature(keyword);
        }
        else
        {
            MsoService msoService = ServiceGenerator.getInstance().getRetrofit().create(MsoService.class);
            Call<BaseResponse<List<Nomenclature>>>
                    call = msoService.autoCompleteNomenclature(keyword, plant, 2);
            if (TextUtils.isNullOrEmpty(keyword)) {
                return;
            }

            if (keyword.length() < 2)
                return;

            if (call.isExecuted()) {
                call.cancel();
            }

            call.enqueue(new Callback<BaseResponse<List<Nomenclature>>>() {
                @Override
                public void onResponse(Call<BaseResponse<List<Nomenclature>>> call, Response<BaseResponse<List<Nomenclature>>> response) {
                    showProgress(false);

                    if (response.isSuccessful() & response.body().isSuccess()) {
                        final List<Nomenclature> nomenclatures = response.body().getData();

                        if(nomenclatures != null && !nomenclatures.isEmpty() ) {
                            assignNomenclature(nomenclatures, "online");
                            if (nomenclatures.size() > 0) {
//                            if (getView() == null)
//                                return;
                                mAppDataManager.addNomenclatures(nomenclatures, new Realm.Transaction.OnSuccess() {
                                    @Override
                                    public void onSuccess() {
//                                    if (getView() != null)
//                                        getView().showNomenclature(nomenList);
                                    }
                                }, new Realm.Transaction.OnError() {
                                    @Override
                                    public void onError(Throwable error) {
//                                    if (getView() == null)
//                                        return;
//
//                                    getView().showToast("Delete" + BaseResponse.Type.FAILED
//                                            + " (Realm Error : " + error.getMessage() + ")");
                                    }
                                });
                            }
                        }
                        else {
                            Snackbar.make(graphicOverlay, "Equipment tidak ditemukan di plant anda",
                                    Snackbar.LENGTH_LONG).show();
                            showProgress(false);
                            Intent intent = new Intent();
                            setResult(RESULT_FIRST_USER, intent);
                            finish();
                        }
                    }
                    else{
                        handleErrorResponse(response);
                    }

                }

                @Override
                public void onFailure(Call<BaseResponse<List<Nomenclature>>> call, Throwable t) {
                    Toast.makeText(ScanQRActivity.this, "Failed Load Data, MSG: " + t.toString(),
                            Toast.LENGTH_SHORT).show();
                    showProgress(false);
                }
            });
        }
    }

    private void getLocalNomenclature(String keyword) {
        if (TextUtils.isNullOrEmpty(keyword) | TextUtils.isNullOrEmpty(plant))
            return;

        final List<Nomenclature> nomenclatures = mAppDataManager.findNomenclaturesbyUser(keyword, "equal");
        if(nomenclatures != null && !nomenclatures.isEmpty()) {
            assignNomenclature(nomenclatures, "offline");
        }else {
            Snackbar.make(graphicOverlay, "Equipment tidak ditemukan di plant anda",
                    Snackbar.LENGTH_LONG).show();
            showProgress(false);
            Intent intent = new Intent();
            setResult(RESULT_FIRST_USER, intent);
            finish();
        }
    }

    private void assignNomenclature(List<Nomenclature> nomenclatures, String command){
           for (int i = 0; i < nomenclatures.size(); i++) {
                Nomenclature data = nomenclatures.get(i);
                String Plant = data.getMplant() + data.getDescMplant();
                Timber.d("description plant : " + data.getDescMplant());
                String Nomenclature = data.getName();
                String Equipment = data.getId() + "-" + data.getCode();
                String Funloc = data.getFunloc();
                String MPI = data.getMpi();
                String tamp[] = { Nomenclature, Equipment, Funloc, MPI, Plant};
                if (Integer.parseInt(data.getId()) > 0) {
                    //send to addactivity
                    Intent intent = new Intent(ScanQRActivity.this, AbnormalityAddOrEditActivity.class);
                    intent.putExtra("Nomenclature", tamp);
                    intent.putExtra(AbnormalityAddOrEditActivity.ACTION_INTENT_KEY, AbnormalityAddOrEditActivity.ACTION_SCAN);
                    intent.addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);
                    intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                    finish();
                }
            }

    }

    private void findInformation(String equipmentNumber) {
        if (!NetworkUtil.isConnected()) {
            findLocalInformation(equipmentNumber);
        }
        else {
            MsoService msoService = ServiceGenerator.getInstance().getRetrofit().create(MsoService.class);
            Call<BaseResponse<EquipmentInfo>> call = msoService.getEquipmentInfo(equipmentNumber);
            call.enqueue(new Callback<BaseResponse<EquipmentInfo>>() {
                @Override
                public void onResponse(Call<BaseResponse<EquipmentInfo>> call, Response<BaseResponse<EquipmentInfo>> response) {
                    showProgress(false);

                    if (response.isSuccessful() & response.body().isSuccess()) {
                        final EquipmentInfo equipmentInfo = response.body().getData();

                        if (Integer.parseInt(equipmentInfo.getId()) > 0) {
                            if (equipmentInfo.getInOrder().equalsIgnoreCase("NONE")) {
                                if(equipmentInfo.getType()!=null) {
                                    if (equipmentInfo.getType().getId().equalsIgnoreCase(EquipmentInfo.TYPE_BEARING)) {
                                        if (mDialog != null) {
                                            mDialog.dismiss();
                                            mDialog = null;
                                        }
                                        mDialog = new MaterialDialog.Builder(ScanQRActivity.this)
                                                .title("Pilih Tipe")
                                                .items(R.array.sub_tipe_equipment)
                                                .itemsCallbackSingleChoice(-1, new MaterialDialog.ListCallbackSingleChoice() {
                                                    @Override
                                                    public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                                                        Timber.d("onSelection :" + which);

                                                        Intent intent = new Intent(ScanQRActivity.this, AddInspectionActivity.class);
                                                        intent.putExtra(AddInspectionActivity.BEARING_TYPE, which);
                                                        intent.putExtra(EquipmentInfo.TYPE_EXTRA, equipmentInfo);
                                                        intent.putExtra(AddInspectionPresenter.TRANSACTION_MODE_KEY, AddInspectionPresenter.ONLINE_MODE);
                                                        intent.addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);
                                                        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                                        startActivity(intent);

                                                        return true;
                                                    }
                                                })
                                                .positiveText(R.string.choose)
                                                .show();
                                        cspPreview.stop();
                                    }
                                }
                                else {
                                    Intent intent = new Intent(ScanQRActivity.this, AddInspectionActivity.class);
                                    intent.putExtra(EquipmentInfo.TYPE_EXTRA, equipmentInfo);
                                    intent.putExtra(AddInspectionPresenter.TRANSACTION_MODE_KEY, AddInspectionPresenter.ONLINE_MODE);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                    startActivity(intent);
                                    finish();
                                }
                            } else {
                                if(mDialog != null) {
                                    mDialog.dismiss();
                                    mDialog = null;
                                }

                                if(equipmentInfo.getType()!=null) {
                                    if (equipmentInfo.getType().getId().equalsIgnoreCase(EquipmentInfo.TYPE_BEARING)) {
                                        mDialog = new MaterialDialog.Builder(ScanQRActivity.this)
                                                .title("Pilih Tipe")
                                                .items(R.array.sub_tipe_equipment)
                                                .itemsCallbackSingleChoice(-1, new MaterialDialog.ListCallbackSingleChoice() {
                                                    @Override
                                                    public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                                                        Timber.d("onSelection :" + which);

                                                        final Intent intent = new Intent(ScanQRActivity.this, EquipmentOrderListActivity.class);
                                                        intent.putExtra(EquipmentInfo.TYPE_EXTRA, equipmentInfo);
                                                        intent.putExtra(AddInspectionPresenter.TRANSACTION_MODE_KEY, AddInspectionPresenter.ONLINE_MODE);
                                                        intent.addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);
                                                        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                                        intent.putExtra(AddInspectionActivity.BEARING_TYPE, which);

                                                        mDialog = new MaterialDialog.Builder(ScanQRActivity.this)
                                                                .title("Perhatian")
                                                                .content("Equipment memiliki order yang belum diclose, halaman akan diarahkan ke list order")
                                                                .positiveText("Ok")
                                                                .onPositive(new MaterialDialog.SingleButtonCallback() {
                                                                    @Override
                                                                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                                                        startActivity(intent);
                                                                        finish();
                                                                    }
                                                                })
                                                                .dismissListener(new DialogInterface.OnDismissListener() {
                                                                    @Override
                                                                    public void onDismiss(DialogInterface dialog) {
                                                                        startActivity(intent);
                                                                        finish();
                                                                    }
                                                                }).build();
                                                        mDialog.show();

                                                        return true;
                                                    }
                                                })
                                                .positiveText(R.string.choose)
                                                .show();
                                        cspPreview.stop();
                                    }
                                }
                                else {
                                    mDialog = new MaterialDialog.Builder(ScanQRActivity.this)
                                            .title("Perhatian")
                                            .content("Equipment memiliki order yang belum diclose, halaman akan diarahkan ke list order")
                                            .positiveText("Ok")
                                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                                @Override
                                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                                    Intent intent = new Intent(ScanQRActivity.this, EquipmentOrderListActivity.class);
                                                    intent.putExtra(EquipmentInfo.TYPE_EXTRA, equipmentInfo);
                                                    intent.putExtra(AddInspectionPresenter.TRANSACTION_MODE_KEY, AddInspectionPresenter.ONLINE_MODE);
                                                    intent.addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);
                                                    intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                                    startActivity(intent);
                                                    finish();
                                                }
                                            })
                                            .dismissListener(new DialogInterface.OnDismissListener() {
                                                @Override
                                                public void onDismiss(DialogInterface dialog) {
                                                    Intent intent = new Intent(ScanQRActivity.this, EquipmentOrderListActivity.class);
                                                    intent.putExtra(EquipmentInfo.TYPE_EXTRA, equipmentInfo);
                                                    intent.putExtra(AddInspectionPresenter.TRANSACTION_MODE_KEY, AddInspectionPresenter.ONLINE_MODE);
                                                    intent.addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);
                                                    intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                                    startActivity(intent);
                                                    finish();
                                                }
                                            }).build();
                                    mDialog.show();
                                    cspPreview.stop();
                                }
                            }
                        }
                    } else {
                        handleErrorResponse(response);
                    }
                }

                @Override
                public void onFailure(Call<BaseResponse<EquipmentInfo>> call, Throwable t) {
                    Toast.makeText(ScanQRActivity.this, "Failed Load Data, MSG: " + t.toString(),
                            Toast.LENGTH_SHORT).show();
                    showProgress(false);
                }
            });
        }
    }

    private void findLocalInformation(final String equipmentNumber) {
        final EquipmentInfo equipmentInfo = mAppDataManager.getCopyOfEquipmentInfo(equipmentNumber);
        if(equipmentInfo != null) {
            if(!TextUtils.isNullOrEmpty(equipmentInfo.getId()) & Integer.parseInt(equipmentInfo.getId()) > 0) {
                if (equipmentInfo.getInOrder().equalsIgnoreCase("NONE")) {
                    if (equipmentInfo.getType().getId().equalsIgnoreCase(EquipmentInfo.TYPE_BEARING)) {
                        if (mDialog != null) {
                            mDialog.dismiss();
                            mDialog = null;
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mDialog = new MaterialDialog.Builder(ScanQRActivity.this)
                                        .title("Pilih Tipe")
                                        .items(R.array.sub_tipe_equipment)
                                        .itemsCallbackSingleChoice(-1, new MaterialDialog.ListCallbackSingleChoice() {
                                            @Override
                                            public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                                                Timber.d("onSelection :" + which);

                                                Intent intent = new Intent(ScanQRActivity.this, AddInspectionActivity.class);
                                                intent.putExtra(AddInspectionActivity.BEARING_TYPE, which);
                                                intent.putExtra(EquipmentInfo.TYPE_EXTRA, equipmentInfo);
                                                intent.putExtra(AddInspectionPresenter.TRANSACTION_MODE_KEY, AddInspectionPresenter.OFFLINE_MODE);
                                                intent.addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);
                                                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                                startActivity(intent);
                                                return true;
                                            }
                                        })
                                        .positiveText(R.string.choose)
                                        .show();
                                cspPreview.stop();
                            }
                        });

                    }
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(mDialog != null) {
                                mDialog.dismiss();
                                mDialog = null;
                            }

                            if(equipmentInfo.getType()!=null) {
                                if (equipmentInfo.getType().getId().equalsIgnoreCase(EquipmentInfo.TYPE_BEARING)) {
                                    mDialog = new MaterialDialog.Builder(ScanQRActivity.this)
                                            .title("Pilih Tipe")
                                            .items(R.array.sub_tipe_equipment)
                                            .itemsCallbackSingleChoice(-1, new MaterialDialog.ListCallbackSingleChoice() {
                                                @Override
                                                public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                                                    Timber.d("onSelection :" + which);

                                                    final Intent intent = new Intent(ScanQRActivity.this, EquipmentOrderListActivity.class);
                                                    intent.putExtra(EquipmentInfo.TYPE_EXTRA, equipmentInfo);
                                                    intent.putExtra(AddInspectionPresenter.TRANSACTION_MODE_KEY, AddInspectionPresenter.OFFLINE_MODE);
                                                    intent.addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);
                                                    intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                                    intent.putExtra(AddInspectionActivity.BEARING_TYPE, which);

                                                    mDialog = new MaterialDialog.Builder(ScanQRActivity.this)
                                                            .title("Perhatian")
                                                            .content("Equipment memiliki order yang belum diclose, halaman akan diarahkan ke list order")
                                                            .positiveText("Ok")
                                                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                                                @Override
                                                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                                                    startActivity(intent);
                                                                    finish();
                                                                }
                                                            })
                                                            .dismissListener(new DialogInterface.OnDismissListener() {
                                                                @Override
                                                                public void onDismiss(DialogInterface dialog) {
                                                                    startActivity(intent);
                                                                    finish();
                                                                }
                                                            }).build();
                                                    mDialog.show();

                                                    return true;
                                                }
                                            })
                                            .positiveText(R.string.choose)
                                            .show();
                                    cspPreview.stop();
                                }
                            }
                            else {
                                mDialog = new MaterialDialog.Builder(ScanQRActivity.this)
                                        .title("Perhatian")
                                        .content("Equipment memiliki order yang belum diclose, halaman akan diarahkan ke list order")
                                        .positiveText("Ok")
                                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                                            @Override
                                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                                Intent intent = new Intent(ScanQRActivity.this, EquipmentOrderListActivity.class);
                                                intent.putExtra(EquipmentInfo.TYPE_EXTRA, equipmentInfo);
                                                intent.putExtra(AddInspectionPresenter.TRANSACTION_MODE_KEY, AddInspectionPresenter.OFFLINE_MODE);
                                                intent.addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);
                                                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                                startActivity(intent);
                                                finish();
                                            }
                                        }).dismissListener(new DialogInterface.OnDismissListener() {
                                            @Override
                                            public void onDismiss(DialogInterface dialogInterface) {
                                                Intent intent = new Intent(ScanQRActivity.this, EquipmentOrderListActivity.class);
                                                intent.putExtra(EquipmentInfo.TYPE_EXTRA, equipmentInfo);
                                                intent.putExtra(AddInspectionPresenter.TRANSACTION_MODE_KEY, AddInspectionPresenter.OFFLINE_MODE);
                                                intent.addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);
                                                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                                startActivity(intent);
                                                finish();
                                            }
                                        })
                                        .build();
                                mDialog.show();
                                cspPreview.stop();
                            }
                        }
                    });
                }
            }
            Snackbar.make(graphicOverlay, "Equipment tidak ditemukan",
                    Snackbar.LENGTH_SHORT)
                    .show();
            //finish();
        } else {
            Snackbar.make(graphicOverlay, "Equipment tidak ditemukan",
                    Snackbar.LENGTH_SHORT)
                    .show();
            //finish();
        }
    }

    private void findDetailEquipment(String equipmentNumber){

        MsoService msoService = ServiceGenerator.getInstance().getRetrofit().create(MsoService.class);
        Call<ListResponse<EquipmentList>>
                call = msoService.getEquipmentList(1, 1, "",equipmentNumber);
        if (TextUtils.isNullOrEmpty(equipmentNumber)) {
            return;
        }

        if (call.isExecuted()) {
            call.cancel();
        }

        call.enqueue(new Callback<ListResponse<EquipmentList>>() {
            @Override
            public void onResponse(Call<ListResponse<EquipmentList>> call, Response<ListResponse<EquipmentList>> response) {
                if (response.isSuccessful()) {
                    final List<EquipmentList> nomenclatures = response.body().getData();

                    if(nomenclatures != null && !nomenclatures.isEmpty() ) {

                       Intent intent = new Intent(ScanQRActivity.this, EquipmentDetailActivity.class);
                        intent.putExtra("detail",nomenclatures.get(0).getDetail() );
                        startActivity(intent);
                        finish();
                    }else{
                        Snackbar.make(graphicOverlay, "Equipment tidak ditemukan di plant anda",
                                Snackbar.LENGTH_LONG).show();
                        showProgress(false);
                        Intent intent = new Intent();
                        setResult(RESULT_FIRST_USER, intent);
                        finish();
                    }
                }
                else{
                    handleErrorResponse(response);
                }
            }

            @Override
            public void onFailure(Call<ListResponse<EquipmentList>> call, Throwable t) {
                Toast.makeText(ScanQRActivity.this, "Failed Load Data, MSG: " + t.toString(),
                        Toast.LENGTH_SHORT).show();
                showProgress(false);
            }




        });

    }

    private boolean handleErrorResponse(Response response) {
        if (response.code() == OkHttpsURLConnection.HTTP_UNAUTHORIZED || response.code() == OkHttpsURLConnection.HTTP_INTERNAL_ERROR) {
            logout();
            return false;
        } else {
            Snackbar.make(graphicOverlay, "Equipment tidak ditemukan di plant anda",
                    Snackbar.LENGTH_SHORT)
                    .show();
            return false;
        }
    }

    private void logout() {
        mAppDataManager.logOut();
        onLogoutSuccess();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    private class CaptureGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            return onTap(e.getRawX(), e.getRawY()) || super.onSingleTapConfirmed(e);
        }
    }

    private class ScaleListener implements ScaleGestureDetector.OnScaleGestureListener {

        /**
         * Responds to scaling events for a gesture in progress.
         * Reported by pointer motion.
         *
         * @param detector The detector reporting the event - use this to
         *                 retrieve extended info about event state.
         * @return Whether or not the detector should consider this event
         * as handled. If an event was not handled, the detector
         * will continue to accumulate movement until an event is
         * handled. This can be useful if an application, for example,
         * only wants to update scaling factors if the change is
         * greater than 0.01.
         */
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            return false;
        }

        /**
         * Responds to the beginning of a scaling gesture. Reported by
         * new pointers going down.
         *
         * @param detector The detector reporting the event - use this to
         *                 retrieve extended info about event state.
         * @return Whether or not the detector should continue recognizing
         * this gesture. For example, if a gesture is beginning
         * with a focal point outside of a region where it makes
         * sense, onScaleBegin() may return false to ignore the
         * rest of the gesture.
         */
        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            return true;
        }

        /**
         * Responds to the end of a scale gesture. Reported by existing
         * pointers going up.
         * <p/>
         * Once a scale has ended, {@link ScaleGestureDetector#getFocusX()}
         * and {@link ScaleGestureDetector#getFocusY()} will return focal point
         * of the pointers remaining on the screen.
         *
         * @param detector The detector reporting the event - use this to
         *                 retrieve extended info about event state.
         */
        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {
            mCameraSource.doZoom(detector.getScaleFactor());
        }
    }
}
