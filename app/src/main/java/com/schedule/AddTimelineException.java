package com.schedule;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.TimePickerDialog;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Objects;

@SuppressLint({"InflateParams", "DefaultLocale"})
public class AddTimelineException extends AppCompatActivity {
    Calendar dateAndTime = new GregorianCalendar();
    Button button;
    int lessonsCount;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_timeline_exception);
        setTitle("Добавление исключения");
        LinearLayout day = findViewById(R.id.day_card);
        LinearLayout lessons = day.findViewById(R.id.lessons_list);
        ((TextView) day.findViewById(R.id.day_name)).setText("Расписание пар");
        SharedPreferences sf = getSharedPreferences("timetable", MODE_PRIVATE);
        lessonsCount = sf.getInt("lessonsCount", 5);
        for (int j = 0; j < lessonsCount; j++) {
            LinearLayout v = (LinearLayout) getLayoutInflater().inflate(R.layout.timeline_lesson_time, null);
            ((TextView) v.findViewById(R.id.lesson_number)).setText(String.format("Пара %d", j + 1));
            lessons.addView(v);
        }
    }

    TimePickerDialog.OnTimeSetListener t = (view, hourOfDay, minute) -> {
        dateAndTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
        dateAndTime.set(Calendar.MINUTE, minute);
        String str = (String.valueOf(hourOfDay).length() == 1 ? "0" + hourOfDay : String.valueOf(hourOfDay)) + ":" + (String.valueOf(minute).length() == 1 ? "0" + minute : String.valueOf(minute));
        button.setText(str);
    };

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
        new TimePickerDialog(AddTimelineException.this, t,
                hour,
                minute, true)
                .show();
    }

    public void createTimelineException(View view) {
        SQLiteDatabase db = openOrCreateDatabase("app.db", MODE_PRIVATE, null);
        db.execSQL("CREATE TABLE IF NOT EXISTS timeline_exceptions (id INTEGER PRIMARY KEY, day_name TEXT, week_number INTEGER)");
        String dayName = (String) ((Spinner) findViewById(R.id.days_spinner)).getSelectedItem();
        Log.i("dd", String.format("INSERT OR IGNORE INTO timeline_exceptions(day_name, week_number) VALUES ('%s', '%d')", dayName, ((RadioButton) findViewById(R.id.even_week)).isChecked() ? 0 : 1));
        db.execSQL(String.format("INSERT OR IGNORE INTO timeline_exceptions(day_name, week_number) VALUES ('%s', %d)", dayName, ((RadioButton) findViewById(R.id.even_week)).isChecked() ? 0 : 1));
        Cursor query = db.rawQuery("SELECT * FROM timeline_exceptions", null);
        query.moveToFirst();
        SharedPreferences exception = getSharedPreferences("timeline_exception" + query.getCount(), MODE_PRIVATE);
        SharedPreferences.Editor e = exception.edit();
        LinearLayout dayLessons = findViewById(R.id.day_card).findViewById(R.id.lessons_list);
        Button button;
        for (int q = 0; q < lessonsCount; q++) {
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
        e.apply();
        query.close();
        db.close();
        setResult(RESULT_OK);
        finish();
    }
}