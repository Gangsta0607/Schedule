package com.schedule;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.GridLayout;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;

import java.util.Calendar;

public class AddTimeline extends AppCompatActivity {
    Calendar dateAndTime = Calendar.getInstance();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_timeline);
        findViewById(R.id.checkBox).setOnClickListener(this::showOthers);
        String[] days = { "Понедельник", "Вторник", "Среда", "Четверг", "Пятница", "Суббота" };
        int[] ids = {R.id.day1, R.id.day2, R.id.day3, R.id.day4, R.id.day5, R.id.day6};
        for (int i = 0; i < 6; i++) {
            GridLayout gl = findViewById(ids[i]);
            ((TextView) gl.findViewById(R.id.day_name)).setText(days[i]);
        }
    }

    private void showOthers(View view) {
        findViewById(R.id.other_days).setVisibility(((CheckBox) view).isChecked() ? View.VISIBLE : View.GONE);
    }

    int id, parentId;
    public void askForTime(View view) {
        id = view.getId();
        parentId = ((GridLayout) view.getParent()).getId();
        new TimePickerDialog(AddTimeline.this, t,
                dateAndTime.get(Calendar.HOUR_OF_DAY),
                dateAndTime.get(Calendar.MINUTE), true)
                .show();
    }

    TimePickerDialog.OnTimeSetListener t= (view, hourOfDay, minute) -> {
        dateAndTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
        dateAndTime.set(Calendar.MINUTE, minute);
        Button button = ((GridLayout) findViewById(parentId)).findViewById(id);
        String str = (String.valueOf(hourOfDay).length() == 1 ? "0" + hourOfDay : String.valueOf(hourOfDay)) + ":" + (String.valueOf(minute).length() == 1 ? "0" + minute : String.valueOf(minute));
        button.setText(str);
    };

    @SuppressLint("DefaultLocale")
    public void createTimeline(View view) {
        int[] starts = { R.id.start1, R.id.start2, R.id.start3, R.id.start4, R.id.start5 };
        int[] ends = { R.id.end1, R.id.end2, R.id.end3, R.id.end4, R.id.end5 };
        SharedPreferences sp = getSharedPreferences("timeline", Context.MODE_PRIVATE);
        SharedPreferences.Editor e = sp.edit();
        Button button;
        String[] days = { "mon", "tue", "wed", "thu", "fri", "sat" };
        boolean notOneDay = ((CheckBox) findViewById(R.id.checkBox)).isChecked();
        if (notOneDay) {
            int[] ids = {R.id.day1, R.id.day2, R.id.day3, R.id.day4, R.id.day5, R.id.day6};
            for (int j = 0; j < 6; j++) {
                GridLayout gl = findViewById(ids[j]);
                for (int i = 0; i < 5; i++) {
                    button = gl.findViewById(starts[i]);
                    if (button.getText().toString().equals("Время начала")) {
                        Snackbar.make(view,
                                String.format("Время начала %d пары %d дня не задано", i + 1, j + 1),
                                Snackbar.LENGTH_SHORT).show();
                        return;
                    }
                    e.putString("start" + (i + 1) + days[j], (String) button.getText());
                    button = gl.findViewById(ends[i]);
                    if (button.getText().toString().equals("Время конца")) {
                        Snackbar.make(view,
                                String.format("Время конца %d пары %d дня не задано", i + 1, j + 1),
                                Snackbar.LENGTH_SHORT).show();
                        return;
                    }
                    e.putString("end" + (i + 1) + days[j], (String) button.getText());
                }
            }
            e.putBoolean("not_one_day", notOneDay);

        }
        else {
            e.putBoolean("not_one_day", notOneDay);
            for (int i = 0; i < 5; i++) {
                button = findViewById(starts[i]);
                if (button.getText().toString().equals("Время начала")) {
                    Snackbar.make(view, String.format("Время начала %d пары не задано", i + 1), Snackbar.LENGTH_SHORT).show();
                    return;
                }
                e.putString("start" + (i + 1), (String) button.getText());
                button = findViewById(ends[i]);
                if (button.getText().toString().equals("Время конца")) {
                    Snackbar.make(view, String.format("Время конца %d пары не задано", i + 1), Snackbar.LENGTH_SHORT).show();
                    return;
                }
                e.putString("end" + (i + 1), (String) button.getText());

            }
        }
        e.apply();
        setResult(RESULT_OK);
        finish();
    }
}