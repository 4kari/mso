package id.sisi.si.mso.ui.inspection.add;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.jakewharton.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;
import com.stepstone.stepper.Step;
import com.stepstone.stepper.VerificationError;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import id.sisi.si.mso.R;
import id.sisi.si.mso.data.DataHelper;
import id.sisi.si.mso.data.api.MsoService;
import id.sisi.si.mso.data.api.ServiceGenerator;
import id.sisi.si.mso.data.model.EquipmentInfo;
import id.sisi.si.mso.data.model.InspectionActivity;
import id.sisi.si.mso.data.model.InspectionDetail;
import id.sisi.si.mso.data.model.Type;
import id.sisi.si.mso.ui.base.BaseFragment;
import id.sisi.si.mso.utils.DialogUtil;
import id.sisi.si.mso.utils.FileHelper;
import id.sisi.si.mso.utils.ImageCompressionTask;
import id.sisi.si.mso.utils.TextUtils;
import timber.log.Timber;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;
import static id.sisi.si.mso.R.layout.list_item_spinner;

/**
 * Created by durrrr on 11-Oct-17.
 * Email: cab.purnama@gmail.com
 */
public class InspectionFragment extends BaseFragment
        implements Step, View.OnClickListener, ImageCompressionTask.CompressionCallback {

    final List<String> conditionOptions = Arrays.asList("GOOD", "BAD");
    private final int REQUEST_CAMERA_CODE = 201;
    private final int REQUEST_GALLERY_CODE = 301;
    private final int ALL_PERMISSIONS_KEY = 101;

    private final String INSPECTION_MODEL_RESTORE_KEY = "inspection_model_restore_key";
    private final String CAMERA_FILE_RESTORE_KEY = "camera_file_restore_key";
    private final String ACTIVITIES_RESTORE_KEY = "activities_restore_key";
    private final String IMAGE_UPLOAD_STATUS_KEY = "image_upload_status_key";

    private final int NO_IMAGE = 0;
    private final int UPLOAD_IMAGE_IN_PROGESS = 1;
    private final int IMAGE_UPLOADED = 2;

    @BindView(R.id.tvresultcode)
    TextView tvResultCode;
    @BindView(R.id.tvequiptmenttypename)
    TextView tvEquiptmentTypeName;
    @BindView(R.id.tvdate)
    TextView tvDate;
    @BindView(R.id.txtKondisi)
    TextView txtKondisi;
    @BindView(R.id.spcondition)
    Spinner spCondition;
    @BindView(R.id.eddescription)
    EditText edDescription;
    /*@BindView(R.id.lladdview)
    LinearLayout llAddView;*/
    @BindView(R.id.sp_activity)
    Spinner spActivity;
    @BindView(R.id.pictinspection)
    ImageButton ibInspection;
    @BindView(R.id.btn_reupload_pictinspection)
    Button bReupload;
    @BindView(R.id.progressbar_pictinspection)
    ProgressBar pbPict;

    private EquipmentInfo mEquipmentInfo;
    private InspectionDetail mInspectionDetailModel;

    private File mCameraFile;

    private ActivityCallback mActivityCallback;
    /*private List<View> activityViewList;
    private ArrayList<String> activities;*/
    private List<InspectionActivity> activityList;
    private int mImageUploadStatus = NO_IMAGE;

    public static InspectionFragment newInstance(EquipmentInfo equipmentInfo, ArrayList<InspectionActivity> inspectionList) {
        InspectionFragment instance = new InspectionFragment();
        Bundle args = new Bundle();
        args.putParcelable(EquipmentInfo.TYPE_EXTRA, equipmentInfo);
        args.putParcelableArrayList(InspectionActivity.TYPE_EXTRA, inspectionList);
        instance.setArguments(args);
        return instance;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_new_inspection, container, false);
        setUnbinder(ButterKnife.bind(this, view));


        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(INSPECTION_MODEL_RESTORE_KEY))
                mInspectionDetailModel = savedInstanceState.getParcelable(INSPECTION_MODEL_RESTORE_KEY);

            String absolutePath = savedInstanceState.getString(CAMERA_FILE_RESTORE_KEY, "");
            if (!TextUtils.isNullOrEmpty(absolutePath))
                mCameraFile = new File(absolutePath);

            /*if (savedInstanceState.containsKey(ACTIVITIES_RESTORE_KEY))
                activities = savedInstanceState.getStringArrayList(ACTIVITIES_RESTORE_KEY);*/

            if (savedInstanceState.containsKey(IMAGE_UPLOAD_STATUS_KEY)) {
                mImageUploadStatus = savedInstanceState.getInt(IMAGE_UPLOAD_STATUS_KEY);
                if (mImageUploadStatus == IMAGE_UPLOADED) {
                    if (!TextUtils.isNullOrEmpty(mInspectionDetailModel.getPhotoPath())) {
                        String imageLink = new StringBuilder().append(MsoService.URL_PATH).append(mInspectionDetailModel.getPhotoPath().replace("media/dtupload/", "getImage/")).toString();
                        new Picasso.Builder(getContext()).downloader(new OkHttp3Downloader(ServiceGenerator.getInstance().getClient())).build().load(imageLink).into(ibInspection);
                        //Picasso.with(getContext()).load(MsoService.URL_PICT_PATH + mInspectionDetailModel.getPhotoPath()).into(ibInspection);
                    }

                    if (!TextUtils.isNullOrEmpty(mInspectionDetailModel.getLocalPhotoPath()) & mCameraFile != null)
                        Picasso.with(getContext()).load(mCameraFile).into(ibInspection);
                }
            }
        }

        if (getArguments().containsKey(EquipmentInfo.TYPE_EXTRA)
                & getArguments().containsKey(InspectionActivity.TYPE_EXTRA)) {
            mEquipmentInfo = getArguments()
                    .getParcelable(EquipmentInfo.TYPE_EXTRA);
            activityList = getArguments()
                    .getParcelableArrayList(InspectionActivity.TYPE_EXTRA);

            initView();
        }

        return view;
    }

    private void initView() {
        if (mEquipmentInfo.getCode() != null) {
            tvEquiptmentTypeName.setText(mEquipmentInfo.getCode());
        }

        mActivityCallback = (ActivityCallback) getActivity();

        if (!TextUtils.isNullOrEmpty(mEquipmentInfo.getName()))
            tvResultCode.setText(mEquipmentInfo.getName());
        tvDate.setText(new SimpleDateFormat("dd MMMM yyyy", new Locale("id", "ID")).format(new Date()));
        ibInspection.setOnClickListener(this);

        if(mEquipmentInfo.getType() == null) {
            txtKondisi.setVisibility(View.VISIBLE);
            spCondition.setVisibility(View.VISIBLE);
            spCondition.setAdapter(DataHelper.createAdapter(getActivity(), conditionOptions));
            spCondition.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    mActivityCallback.onConditionChanged(i);
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });
        }
        ArrayAdapter<InspectionActivity> activityAdapter = new ArrayAdapter<>(getContext(),
                list_item_spinner, android.R.id.text1, activityList);
        spActivity.setAdapter(activityAdapter);
        spActivity.setSelection(1);

        /*if (activityViewList == null)
            activityViewList = new ArrayList<>();

        if (activities == null) {
            activities = new ArrayList<>();
            final View addActivityView = getLayoutInflater().inflate(R.layout.tambah_view_activity_default, null);
            ImageView tvAddView = addActivityView.findViewById(R.id.addActivity);

            tvAddView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final View addView = getLayoutInflater().inflate(R.layout.tambah_view_activity, null);
                    activityViewList.add(addView);

                    ImageView tvRemove = addView.findViewById(R.id.remove);
                    tvRemove.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ((LinearLayout) addView.getParent()).removeView(addView);
                            activityViewList.remove(addView);
                        }
                    });

                    EditText etActivityAdd = addActivityView.findViewById(R.id.matActivity);
                    EditText etActivityNew = addView.findViewById(R.id.matActivity);
                    etActivityNew.setText(etActivityAdd.getText().toString());
                    etActivityAdd.setText("");

                    llAddView.addView(addView, activityViewList.size() - 2);
                }
            });

            llAddView.addView(addActivityView, activityViewList.size() - 1);
            activityViewList.add(addActivityView);

        } else {
            final View addActivityView = getLayoutInflater().inflate(R.layout.tambah_view_activity_default, null);
            ImageView imAddView = addActivityView.findViewById(R.id.addActivity);

            if (activities != null & activities.size() > 0) {
                EditText etActivity = addActivityView.findViewById(R.id.matActivity);
                etActivity.setText(activities.get(0));
            }

            imAddView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final View addView = getLayoutInflater().inflate(R.layout.tambah_view_activity, null);
                    activityViewList.add(addView);

                    ImageView tvRemove = addView.findViewById(R.id.remove);
                    tvRemove.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ((LinearLayout) addView.getParent()).removeView(addView);
                            activityViewList.remove(addView);
                        }
                    });

                    EditText etActivityAdd = addActivityView.findViewById(R.id.matActivity);
                    EditText etActivityNew = addView.findViewById(R.id.matActivity);
                    etActivityNew.setText(etActivityAdd.getText().toString());
                    etActivityAdd.setText("");

                    llAddView.addView(addView, activityViewList.size() - 2);
                }
            });

            llAddView.addView(addActivityView, activityViewList.size() - 1);
            activityViewList.add(addActivityView);

            for (int i = 1; i < activities.size(); i++) {
                final View addView = getLayoutInflater().inflate(R.layout.tambah_view_activity, null);
                activityViewList.add(addView);

                EditText etActivityNew = addView.findViewById(R.id.matActivity);
                etActivityNew.setText(activities.get(i));

                ImageView tvRemove = addView.findViewById(R.id.remove);
                tvRemove.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ((LinearLayout) addView.getParent()).removeView(addView);
                        activityViewList.remove(addView);
                    }
                });

                llAddView.addView(addView, i - 1);
            }

            activities.clear();
        }*/

        /*if (activities != null)
            activities = new ArrayList<>();

        if (activityViewList == null) {
            activityViewList = new ArrayList<>();

            final View activityView = getLayoutInflater().inflate(R.layout.view_add_activity, null);
            final ImageView imAdd = activityView.findViewById(R.id.im_add);

            imAdd.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    view.setVisibility(View.INVISIBLE);
                    view.setOnClickListener(null);
                    activityView.findViewById(R.id.im_remove).setVisibility(View.VISIBLE);
                    activityView.findViewById(R.id.im_remove).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                        }
                    });

                    final View newActivityView = getLayoutInflater().inflate(R.layout.view_add_activity, null);
                    newActivityView.findViewById(R.id.im_add).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                        }
                    });

                    llAddView.addView(newActivityView);

                }
            });

            llAddView.addView(activityView);
            activityViewList.add(activityView);*/
    }

    private void captureImage() {
        if (checkPermission()) {
            final CharSequence[] dialogChoice = {"Take Photo","From Gallery",
                    "Cancel"};
            AlertDialog alertDialog = new AlertDialog.Builder(getContext())
                    .setItems(dialogChoice, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            if (dialogChoice[i].equals(dialogChoice[0])) {
                                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                mCameraFile = FileHelper.getOutputMediaFile(MEDIA_TYPE_IMAGE);
                                Uri uri = FileProvider.getUriForFile(
                                        getContext(), getActivity().getApplicationContext().getPackageName() + ".provider", mCameraFile);

                                List<ResolveInfo> resolvedIntentActivities = getContext().getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
                                for (ResolveInfo resolvedIntentInfo : resolvedIntentActivities) {
                                    String packageName = resolvedIntentInfo.activityInfo.packageName;
                                    getContext().grantUriPermission(packageName, uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                }

                                intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                                startActivityForResult(intent, REQUEST_CAMERA_CODE);
                            }else if (dialogChoice[i].equals(dialogChoice[1])) {
                                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                                intent.setType("image/*");
                                startActivityForResult(intent, REQUEST_GALLERY_CODE);
                            }
                            else if (dialogChoice[i].equals(dialogChoice[2])) {
                                dialogInterface.dismiss();
                            }
                        }
                    }).create();

            alertDialog.show();
        }
    }


    private boolean checkPermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            int permissionCAMERA = ContextCompat.checkSelfPermission(getContext(),
                    Manifest.permission.CAMERA);
            int storagePermission = ContextCompat.checkSelfPermission(getContext(),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE);


            List<String> listPermissionsNeeded = new ArrayList<>();
            if (storagePermission != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
            if (permissionCAMERA != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(Manifest.permission.CAMERA);
            }

            if (!listPermissionsNeeded.isEmpty()) {
                ActivityCompat.requestPermissions(getActivity(),
                        listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), ALL_PERMISSIONS_KEY);
                return false;
            }
        }

        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mImageUploadStatus = UPLOAD_IMAGE_IN_PROGESS;
        switch (requestCode) {
            case REQUEST_CAMERA_CODE:
                if (resultCode == Activity.RESULT_OK) {
                    ImageCompressionTask compressionTask = new ImageCompressionTask(mCameraFile, null, this);
                    compressionTask.execute(mCameraFile.getAbsolutePath());

                    /*ImageUtil.ImageCompressionAsyncTask imageCompression = new ImageUtil.ImageCompressionAsyncTask() {
                        @Override
                        protected void onPostExecute(byte[] imageBytes) {
                            try {
                                FileOutputStream fos = new FileOutputStream(mCameraFile.getAbsolutePath());
                                fos.write(imageBytes);
                                fos.close();

                                Picasso.with(getContext()).load(mCameraFile).into(ibInspection);

                                pbPict.setVisibility(View.VISIBLE);
                                bReupload.setVisibility(View.INVISIBLE);
                                ibInspection.setEnabled(false);

                                mActivityCallback.attemptUploadImage(mCameraFile);
                            } catch (Exception e) {
                                e.printStackTrace();
                                mImageUploadStatus = NO_IMAGE;
                            }
                        }
                    };

                    imageCompression.execute(mCameraFile.getAbsolutePath());*/
                } else {
                    mImageUploadStatus = NO_IMAGE;
                }
                break;
            case REQUEST_GALLERY_CODE:
                if (resultCode == Activity.RESULT_OK) {
                    final Uri uri = data.getData();
                    final File galleryImageFile = FileHelper.getOutputMediaFile(MEDIA_TYPE_IMAGE);
                    String imagePath = getRealPathFromURI(uri);
                    showLoadingImage(true);

                    ImageCompressionTask imageCompressionTask = new ImageCompressionTask(galleryImageFile, null, this);
                    imageCompressionTask.execute(imagePath);

                } else if (resultCode == Activity.RESULT_CANCELED) {
                    showToast("Pick image canceled");
                } else {
                    showToast("Sorry, pick image failed");
                }
            default:
                break;
        }
    }

    public void showLoadingImage(boolean isShow) {
        try {
            pbPict.setVisibility(isShow ? View.VISIBLE : View.INVISIBLE);
        } catch (NullPointerException e){

        }
    }

    public String getRealPathFromURI(Uri contentUri) {
        String[] proj = {MediaStore.Audio.Media.DATA};
        Cursor cursor = getActivity().getContentResolver().query(contentUri, proj, null, null, null); //Since manageQuery is deprecated
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    @Override
    public void postImageCompressed(File file, Integer loadType) {
        Picasso.with(getContext()).load(file).into(ibInspection);

        pbPict.setVisibility(View.VISIBLE);
        bReupload.setVisibility(View.INVISIBLE);
        ibInspection.setEnabled(false);

        mActivityCallback.attemptUploadImage(file);
    }

    @Override
    public void onCompressingFailed(Integer loadType) {
        mImageUploadStatus = NO_IMAGE;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.pictinspection:
                if (getActivity().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA))
                    captureImage();
                else
                    Toast.makeText(getContext(), "Device tidak mendukung kamera", Toast.LENGTH_SHORT);
                break;
        }
    }

    public boolean isConditionBad() {
        return !spCondition.getSelectedItem().toString().equals(conditionOptions.get(0));
    }

    public InspectionDetail setDataToModel() {
        if (mInspectionDetailModel == null)
            mInspectionDetailModel = new InspectionDetail();

        if (mEquipmentInfo.getType() == null) {
            mInspectionDetailModel.setEquipmentTypeId("0");
        } else {
            mInspectionDetailModel.setEquipmentTypeId(mEquipmentInfo.getType().getId());
        }

        mInspectionDetailModel.setEquipmentName(mEquipmentInfo.getName());
        mInspectionDetailModel.setDate(new SimpleDateFormat("MM/dd/yyyy HH:mm").format(new Date()));
        if(mEquipmentInfo.getType() == null)
            mInspectionDetailModel.setCondition(isConditionBad() ? 0 : 1);
        mInspectionDetailModel.setDescription(edDescription.getText().toString());

        /*ArrayList<String> activities = new ArrayList<>();
        Iterator<View> iActivityViews = activityViewList.iterator();
        while (iActivityViews.hasNext()) {
            View viewActivity = iActivityViews.next();
            EditText etActivity = viewActivity.findViewById(R.id.matActivity);
            activities.add(etActivity.getText().toString());
        }
        activities.add(activities.get(0));
        activities.remove(0);*/
        //mInspectionDetailModel.setActivities(activities);
        mInspectionDetailModel.setActivity(((InspectionActivity) spActivity.getSelectedItem()).getValue());

        return mInspectionDetailModel;
    }

    public void uploadImageResult(int result, @Nullable String imagePath, @Nullable String imageUrl) {
        pbPict.setVisibility(View.INVISIBLE);
        ibInspection.setEnabled(true);

        if (mInspectionDetailModel == null)
            mInspectionDetailModel = new InspectionDetail();

        if (result == AddInspectionPresenter.UPLOAD_IMAGE_SUCCESS) {
            if (imagePath != null)
                mInspectionDetailModel.setLocalPhotoPath(imagePath);

            if (imageUrl != null)
                mInspectionDetailModel.setPhotoPath(imageUrl);

            mImageUploadStatus = IMAGE_UPLOADED;
        } else if (result == AddInspectionPresenter.UPLOAD_IMAGE_FAILED) {
            mImageUploadStatus = NO_IMAGE;

            bReupload.setVisibility(View.VISIBLE);
            bReupload.setOnClickListener(null);
            bReupload.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    pbPict.setVisibility(View.VISIBLE);
                    bReupload.setVisibility(View.INVISIBLE);
                    ibInspection.setEnabled(false);

                    mActivityCallback.attemptUploadImage(mCameraFile);
                }
            });
        }
    }

    @Nullable
    @Override
    public VerificationError verifyStep() {
        String errorMessage = null;

        if (mImageUploadStatus == NO_IMAGE | mImageUploadStatus == UPLOAD_IMAGE_IN_PROGESS) {
            if (mImageUploadStatus == NO_IMAGE)
                errorMessage = getString(R.string.error_image_required);
            else
                errorMessage = getString(R.string.alert_uploading_inprogress);
        }

        if (TextUtils.isNullOrEmpty(edDescription.getText().toString())) {
            edDescription.setError(getString(R.string.error_field_description));
            errorMessage = getString(R.string.alert_any_field_required);
        } else {
            edDescription.setError(null);
        }

        /*for (int i = 0; i < activityViewList.size(); i++) {
            EditText edActivity = activityViewList.get(i).findViewById(R.id.matActivity);

            if (TextUtils.isNullOrEmpty(edActivity.getText().toString())) {
                edActivity.setError(getString(R.string.error_field_activity));
                errorMessage = getString(R.string.alert_any_field_required);
            } else {
                edActivity.setError(null);
            }
        }*/

        Timber.d(errorMessage);

        if (errorMessage == null)
            return null;
        else
            return new VerificationError(errorMessage);
    }

    @Override
    public void onSelected() {

    }

    @Override
    public void onError(@NonNull VerificationError error) {
        DialogUtil.showBasicDialog(getContext(), "Perhatian", error.getErrorMessage());
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mInspectionDetailModel != null)
            outState.putParcelable(INSPECTION_MODEL_RESTORE_KEY, mInspectionDetailModel);

        if (mCameraFile != null)
            outState.putString(CAMERA_FILE_RESTORE_KEY, mCameraFile.getAbsolutePath());

        /*if (activityViewList != null) {
            boolean firstIsEmpty = false;

            for (int i = 0; i < activityViewList.size(); i++) {
                if (activities == null)
                    activities = new ArrayList<>();

                EditText et = activityViewList.get(i).findViewById(R.id.matActivity);
                if (!TextUtils.isNullOrEmpty(et.getText().toString()))
                    activities.add(et.getText().toString());
                else {
                    if (i == 0)
                        firstIsEmpty = true;
                }
            }

            if (firstIsEmpty & activities.size() > 0) {
                activities.add(0, activities.get(activities.size() - 1));
                activities.remove(activities.size() - 1);
            }

            outState.putStringArrayList(ACTIVITIES_RESTORE_KEY, activities);
        }*/

        outState.putInt(IMAGE_UPLOAD_STATUS_KEY, mImageUploadStatus);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    interface ActivityCallback {

        void onConditionChanged(int condition);
        void onConditionChanged2(int condition, int[] index);
        void attemptUploadImage(File imageFile);

    }

    class LoadBitmapIntoIV extends AsyncTask<String, Void, String> {

        Bitmap myBitmap = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... s) {
            myBitmap = BitmapFactory.decodeFile(mCameraFile.getAbsolutePath());

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            ibInspection.setImageBitmap(myBitmap);
//                DialogUtils.closeDialog();
        }
    }
}
