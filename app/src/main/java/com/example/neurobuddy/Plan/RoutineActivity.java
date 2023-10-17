package com.example.neurobuddy.Plan;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Spinner;

import com.example.neurobuddy.R;
import com.example.neurobuddy.SpinnerUtil;

public class RoutineActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_routine);

        final Spinner activityTypeSpinner = findViewById(R.id.activityTypeSpinner);

        activityTypeSpinner.post(new Runnable() {
            @Override
            public void run() {
                activityTypeSpinner.setSelection(1);
            }
        });

        Log.v("ABCD", String.valueOf(activityTypeSpinner));

        SpinnerUtil.setupSpinner(
                this,
                activityTypeSpinner,
                R.array.activity_types,
                new SpinnerUtil.SpinnerItemSelectedListener() {
                    @Override
                    public void onItemSelected(int position) {
                        updateRecyclerView(position);
                    }
                });
    }

    private void updateRecyclerView(int selectedPosition) {
        switch (selectedPosition) {
            case 0:
                if (!isCurrentActivity(SpecificPlanActivity.class)) {
                    Intent intent = new Intent(this, SpecificPlanActivity.class);
                    startActivity(intent);
                }
                break;
            case 1:
                if (!isCurrentActivity(RoutineActivity.class)) {
                    Intent intent = new Intent(this, RoutineActivity.class);
                    startActivity(intent);
                    break;
                }
            default:
                // Update RecyclerView logic for other cases
                break;
        }
    }

    private boolean isCurrentActivity(Class<?> activityClass) {
        return getClass().equals(activityClass);
    }
}
