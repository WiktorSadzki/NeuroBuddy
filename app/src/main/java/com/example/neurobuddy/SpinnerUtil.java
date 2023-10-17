package com.example.neurobuddy;

import android.content.Context;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

public class SpinnerUtil {

    public static void setupSpinner(Context context, Spinner spinner, int arrayResource, final SpinnerItemSelectedListener listener) {
        // Load the array from resources
        CharSequence[] originalArray = context.getResources().getTextArray(arrayResource);

        // Create a new array in reverse order
        CharSequence[] reversedArray = new CharSequence[originalArray.length];
        for (int i = 0; i < originalArray.length; i++) {
            reversedArray[i] = originalArray[originalArray.length - i - 1];
        }

        // Initialize the ArrayAdapter with the reversed array
        ArrayAdapter<CharSequence> adapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, reversedArray);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            private int previousPosition = -1;

            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                if (position != previousPosition) {
                    if (listener != null) {
                        listener.onItemSelected(position);
                    }
                    previousPosition = position;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Do nothing
            }
        });
    }

    public interface SpinnerItemSelectedListener {
        void onItemSelected(int position);
    }
}
