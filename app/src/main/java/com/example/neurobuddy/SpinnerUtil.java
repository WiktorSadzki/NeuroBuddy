package com.example.neurobuddy;

import static com.firebase.ui.auth.AuthUI.getApplicationContext;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.example.neurobuddy.Plan.RoutineActivity;
import com.example.neurobuddy.Plan.SpecificPlanActivity;

public class SpinnerUtil {
    public static void setupSpinner(Context context, Spinner spinner, int arrayId, SpinnerCallback callback) {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(context, arrayId, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                callback.onItemSelected(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Do nothing here
            }
        });
    }

    public interface SpinnerCallback {
        void onItemSelected(int position);
    }

    public static boolean isCurrentActivity(Activity activity, Class<?> activityClass) {
        return activityClass.isInstance(activity);
    }
}