package id.sisi.si.mso.ui.inspection.add.adapter;

import android.content.Context;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.ViewGroup;

import com.stepstone.stepper.Step;
import com.stepstone.stepper.adapter.AbstractFragmentStepAdapter;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by durrrr on 11-Oct-17.
 * Email: andaruwildan@gmail.com
 */
public class AddInspectionAdapter extends AbstractFragmentStepAdapter {

    private ArrayList<Step> mSteps = new ArrayList<>();


    public AddInspectionAdapter(@NonNull FragmentManager fm, @NonNull Context context) {
        super(fm, context);
    }

    @Override
    public int getCount() {
        return mSteps.size();
    }

    @Override
    public Step createStep(@IntRange(from = 0L) int position) {
        return mSteps.get(position);
    }

    @Override
    public Step findStep(@IntRange(from = 0L) int position) {
        return mSteps.get(position);
    }

    public void addAdditionalStep(Step step) {
        mSteps.add(step);
        notifyDataSetChanged();
    }
    public int findpos(Step step){
        return mSteps.indexOf(step);
    }
    public void checkstep(){
        Log.d("checkstep: ", mSteps.toString());
    }
    public void removeindex(int index){
        mSteps.remove(index);
    }
    public void removeStep(Step step) {
        if(step!=null){
            Log.d("removeStep: ",step.toString());}
        mSteps.remove(step);
        notifyDataSetChanged();
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        mSteps.set(position, (Step) super.instantiateItem(container, position));
        return mSteps.get(position);
        // save the appropriate reference depending on position
    }
}
