package com.schedule;

import android.annotation.SuppressLint;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;

import java.util.Calendar;
import java.util.Objects;

@SuppressLint({"DefaultLocale", "InflateParams"})
public class AddTimeline extends AppCompatActivity {
    Calendar dateAndTime = Calendar.getInstance();
    SeekBar lessonsCount;
    Button button;
    TimePickerDialog.OnTimeSetListener t = (view, hourOfDay, minute) -> {
        dateAndTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
        dateAndTime.set(Calendar.MINUTE, minute);
        String str = (String.valueOf(hourOfDay).length() == 1 ? "0" + hourOfDay : String.valueOf(hourOfDay)) + ":" + (String.valueOf(minute).length() == 1 ? "0" + minute : String.valueOf(minute));
        button.setText(str);
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_timeline);
        findViewById(R.id.checkBox).setOnClickListener(this::showOthers);
        String[] days = {"Понедельник", "Вторник", "Среда", "Четверг", "Пятница", "Суббота"};
        int[] ids = {R.id.day1, R.id.day2, R.id.day3, R.id.day4, R.id.day5, R.id.day6};
        lessonsCount = findViewById(R.id.lessons_count);
        SharedPreferences sp = getSharedPreferences("timetable", MODE_PRIVATE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            lessonsCount.setMin(sp.getInt("lessonsCount", 5));
        }
        else {
            lessonsCount.setProgress(sp.getInt("lessonsCount", 5));
        }
        lessonsCount.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                changeLessonCount();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        ((TextView) findViewById(R.id.lessons_count_hint)).setText(String.format("Кол-во пар: %d", lessonsCount.getProgress()));
        LinearLayout day;
        for (int i = 0; i < 6; i++) {
            day = findViewById(ids[i]);
            ((TextView) day.findViewById(R.id.day_name)).setText(days[i]);
            LinearLayout lessonsList = day.findViewById(R.id.lessons_list);
            for (int j = 0; j < lessonsCount.getProgress(); j++) {
                LinearLayout v = (LinearLayout) getLayoutInflater().inflate(R.layout.timeline_lesson_time, null);
                ((TextView) v.findViewById(R.id.lesson_number)).setText(String.format("Пара %d", j + 1));
                lessonsList.addView(v);
            }
        }
        day = findViewById(ids[0]);
        day.findViewById(R.id.day_name).setVisibility(View.GONE);

        SharedPreferences timeline = getSharedPreferences("timeline", MODE_PRIVATE);
        if (timeline.getAll().size() > 0) {
            lessonsCount.setProgress(timeline.getInt("lessonsCount", 5));
            if (timeline.getBoolean("not_one_day", false)) {
                CheckBox otherDays = findViewById(R.id.checkBox);
                otherDays.setChecked(true);
                showOthers(otherDays);
                for (int day_ = 0; day_ < 6; day_++) {
                    for (int i = 0; i < lessonsCount.getProgress(); i++) {
                        ((Button) ((LinearLayout) findViewById(ids[day_]).findViewById(R.id.lessons_list)).getChildAt(i).findViewById(R.id.start)).setText(timeline.getString("start" + (i + 1) + days[day_], "Время начала"));
                        ((Button) ((LinearLayout) findViewById(ids[day_]).findViewById(R.id.lessons_list)).getChildAt(i).findViewById(R.id.end)).setText(timeline.getString("start" + (i + 1)  + days[day_], "Время конца"));
                    }
                }
            }
            else {
                for (int i = 0; i < lessonsCount.getProgress(); i++) {
                    ((Button) ((LinearLayout) findViewById(R.id.day1).findViewById(R.id.lessons_list)).getChildAt(i).findViewById(R.id.start)).setText(timeline.getString("start" + (i + 1), "Время начала"));
                    ((Button) ((LinearLayout) findViewById(R.id.day1).findViewById(R.id.lessons_list)).getChildAt(i).findViewById(R.id.end)).setText(timeline.getString("start" + (i + 1), "Время конца"));
                }
            }
        }
    }

    private void changeLessonCount() {
        ((TextView) findViewById(R.id.lessons_count_hint)).setText(String.format("Кол-во пар: %d", lessonsCount.getProgress()));
        int[] ids = {R.id.day1, R.id.day2, R.id.day3, R.id.day4, R.id.day5, R.id.day6};
        for (int i = 0; i < 6; i++) {
            View day = findViewById(ids[i]);
            LinearLayout lessonsList = day.findViewById(R.id.lessons_list);

            while (lessonsList.getChildCount() < lessonsCount.getProgress()) {
                LinearLayout v = (LinearLayout) getLayoutInflater().inflate(R.layout.timeline_lesson_time, null);
                ((TextView) v.findViewById(R.id.lesson_number)).setText(String.format("Пара %d", lessonsList.getChildCount() + 1));
                lessonsList.addView(v);
            }

            while (lessonsList.getChildCount() > lessonsCount.getProgress()) {
                lessonsList.removeViewAt(lessonsList.getChildCount() - 1);
            }
        }
    }

    private void showOthers(View view) {
        findViewById(R.id.other_days).setVisibility(((CheckBox) view).isChecked() ? View.VISIBLE : View.GONE);
        findViewById(R.id.day1).findViewById(R.id.day_name).setVisibility(((CheckBox) view).isChecked() ? View.VISIBLE : View.GONE);

    }

    public void askForTime(View view) {
        button = (Button) view;
        String buttonText = (String) ((Button) view).getText();
        int hour, minute;
        if (Objects.equals(buttonText, "Время начала") || Objects.equals(buttonText, "Время конца")) {
            hour = dateAndTime.get(Calendar.HOUR_OF_DAY);
        } else {
            String[] time_split = buttonText.split(":");
            hour = Integer.parseInt(time_split[0]);
        }
        if (Objects.equals(buttonText, "Время начала") || Objects.equals(buttonText, "Время конца")) {
            minute = dateAndTime.get(Calendar.MINUTE);
        } else {
            String[] time_split = buttonText.split(":");
            minute = Integer.parseInt(time_split[1]);
        }
        new TimePickerDialog(AddTimeline.this, t,
                hour,
                minute, true)
                .show();
    }

    @SuppressLint("DefaultLocale")
    public void createTimeline(View view) {
        SharedPreferences sp = getSharedPreferences("timeline", Context.MODE_PRIVATE);
        SharedPreferences.Editor e = sp.edit();
        Button button;
        String[] days = {"mon", "tue", "wed", "thu", "fri", "sat"};
        boolean notOneDay = ((CheckBox) findViewById(R.id.checkBox)).isChecked();
        if (notOneDay) {
            int[] day_ids = {R.id.day1, R.id.day2, R.id.day3, R.id.day4, R.id.day5, R.id.day6};
            for (int j = 0; j < 6; j++) {
                LinearLayout dayLessons = findViewById(day_ids[j]).findViewById(R.id.lessons_list);
                for (int q = 0; q < lessonsCount.getProgress(); q++) {
                    //start
                    button = dayLessons.getChildAt(q).findViewById(R.id.start);
                    if (button.getText().toString().equals("Время начала")) {
                        Snackbar.make(view,
                                String.format("Время начала %d пары %d дня не задано", q + 1, j + 1),
                                Snackbar.LENGTH_SHORT).show();
                        return;
                    }
                    e.putString("start" + (q + 1) + days[j], (String) button.getText());
                    //end
                    button = dayLessons.getChildAt(q).findViewById(R.id.end);
                    if (button.getText().toString().equals("Время конца")) {
                        Snackbar.make(view,
                                String.format("Время конца %d пары %d дня не задано", q + 1, j + 1),
                                Snackbar.LENGTH_SHORT).show();
                        return;
                    }
                    e.putString("end" + (q + 1) + days[j], (String) button.getText());
                }
            }
            e.putBoolean("not_one_day", notOneDay);
        } else {
            e.putBoolean("not_one_day", false);
            for (int i = 0; i < 5; i++) {
                LinearLayout dayLessons = findViewById(R.id.day1).findViewById(R.id.lessons_list);
                for (int q = 0; q < lessonsCount.getProgress(); q++) {
                    //start
                    button = dayLessons.getChildAt(q).findViewById(R.id.start);
                    if (button.getText().toString().equals("Время начала")) {
                        Snackbar.make(view,
                                String.format("Время начала %d пары не задано", q + 1),
                                Snackbar.LENGTH_SHORT).show();
                        return;
                    }
                    e.putString("start" + (q + 1), (String) button.getText());
                    //end
                    button = dayLessons.getChildAt(q).findViewById(R.id.end);
                    if (button.getText().toString().equals("Время конца")) {
                        Snackbar.make(view,
                                String.format("Время конца %d пары не задано", q + 1),
                                Snackbar.LENGTH_SHORT).show();
                        return;
                    }
                    e.putString("end" + (q + 1), (String) button.getText());
                }

            }
        }
        e.apply();
        setResult(RESULT_OK);
        finish();
    }
}