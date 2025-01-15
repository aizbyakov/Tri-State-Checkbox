package com.ai.android.checkbox3.demo;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.widget.CheckBox;

import com.ai.android.checkbox3.CheckBox3;

public class MainActivity extends Activity {

    private CheckBox3 checkBox1;
    private CheckBox checkBox2;
    private CheckBox checkBox3;
    private CheckBox checkBox4;

    private CheckBox[] checkbox_array;

    private boolean listenToUpdates = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        checkBox1 = findViewById(R.id.checkBox1);
        checkBox2 = findViewById(R.id.checkBox2);
        checkBox3 = findViewById(R.id.checkBox3);
        checkBox4 = findViewById(R.id.checkBox4);

        init();

        checkBox1.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (listenToUpdates) {
                listenToUpdates = false;

                for (CheckBox cb : checkbox_array) {
                    cb.setChecked(isChecked);
                }

                checkBox1.setText(isChecked ? "Select None" : "Select All");

                checkBox1.setCycle(com.ai.android.checkbox3.R.array.ai_checkbox3_cycleCheckedUncheckedOnly);
                listenToUpdates = true;
            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void init() {
        checkbox_array = new CheckBox[]{checkBox2, checkBox3, checkBox4};

        for (CheckBox checkBox : checkbox_array) {
            checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (listenToUpdates) {
                    listenToUpdates = false;

                    int checkedCount = 0;

                    for (CheckBox cb : checkbox_array) {
                        if (cb.isChecked())
                            checkedCount++;
                    }

                    if (checkedCount == checkbox_array.length) {
                        checkBox1.setCycle(com.ai.android.checkbox3.R.array.ai_checkbox3_cycleCheckedUncheckedOnly);
                        checkBox1.setChecked(true, false);
                        checkBox1.setText("Select None");
                    } else if (checkedCount == 0) {
                        checkBox1.setCycle(com.ai.android.checkbox3.R.array.ai_checkbox3_cycleCheckedUncheckedOnly);
                        checkBox1.setChecked(false, false);
                        checkBox1.setText("Select All");
                    } else {
                        checkBox1.setCycle(com.ai.android.checkbox3.R.array.ai_checkbox3_cycleAll);
                        checkBox1.setChecked(false, true);
                        checkBox1.setText("Select All");
                    }
                    listenToUpdates = true;
                }
            });
        }
    }
}
