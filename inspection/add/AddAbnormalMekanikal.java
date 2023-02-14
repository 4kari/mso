package id.sisi.si.mso.ui.inspection.add;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.stepstone.stepper.StepperLayout;

import butterknife.BindView;
import butterknife.ButterKnife;
import id.sisi.si.mso.R;
import id.sisi.si.mso.data.AppDataManager;
import id.sisi.si.mso.data.model.Parent;
import id.sisi.si.mso.data.model.User;
import id.sisi.si.mso.ui.base.BaseActivity;
import id.sisi.si.mso.ui.inspection.add.adapter.AddInspectionAdapter;
import io.realm.Realm;

public class AddAbnormalMekanikal extends BaseActivity {

    private AppBarConfiguration appBarConfiguration;
//    @BindView(R.id.toolbar)
//    Toolbar toolbar;
    private Realm mRealm;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_abnormal_mekanikal);
//        setUnbinder(ButterKnife.bind(this));

//        Toolbar toolbar = findViewById(R.id.toolbar);
//        toolbar.setTitle("Add Abnormalitas");
//        setSupportActionBar(toolbar);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mRealm = Realm.getDefaultInstance();
        EditText etPlant = findViewById(R.id.etPlant);
        User user = mRealm.where(Parent.class).findFirst().getLoggedInUser();
        etPlant.setText(user.getPlant());
//        AddInspectionAdapter mAdapter = new AddInspectionAdapter(getSupportFragmentManager(), this);
//        mAdapter.addAdditionalStep(new AbnormalityFragment());
//        loadFragment((Fragment) new AbnormalityFragment());
    }
//    private void loadFragment(Fragment fragment) {
// create a FragmentManager
//        FragmentManager fm = getFragmentManager();
// create a FragmentTransaction to begin the transaction and replace the Fragment
//        FragmentTransaction fragmentTransaction = fm.beginTransaction();
// replace the FrameLayout with new Fragment
//        fragmentTransaction.replace(R.id.abnorF, fragment);
//        fragmentTransaction.commit(); // save the changes
//    }
}