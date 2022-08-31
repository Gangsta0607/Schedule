package com.schedule;

import androidx.appcompat.app.AppCompatActivity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.Spinner;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import java.util.ArrayList;
import java.util.Objects;

public class AddDuty extends AppCompatActivity {
    SQLiteDatabase db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_duty);
        this.setTitle("Добавление долга");
        findViewById(R.id.fab).setOnClickListener(this::addDuty);
        db = getBaseContext().openOrCreateDatabase("app.db", MODE_PRIVATE, null);
        Cursor query = db.rawQuery("SELECT * FROM subjects", null);
        query.moveToFirst();
        ArrayList<String> subjects = new ArrayList<>();
        do subjects.add(query.getString(1));
        while (query.moveToNext());
        query.close();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, subjects);
        ((Spinner) findViewById(R.id.subject_name_spinner)).setAdapter(adapter);
    }

    public void addDuty(View view) {
        String dutyName = Objects.requireNonNull(((TextInputEditText) findViewById(R.id.duty_name)).getText()).toString();
        if (dutyName.length() == 0) {
            Snackbar.make(view, "Введите название долга", Snackbar.LENGTH_LONG).show();
            return;
        }
        DatePicker datePicker = ((DatePicker) findViewById(R.id.datePicker));
        String month = String.valueOf(datePicker.getMonth() + 1);
        if (month.length() == 1) month = "0" + month;
        String day = String.valueOf(datePicker.getDayOfMonth());
        if (day.length() == 1) day = "0" + day;
        String passTime = String.format("%s.%s.%s", day, month, datePicker.getYear());
        String subjectName = (String) ((Spinner) findViewById(R.id.subject_name_spinner)).getSelectedItem();

        db.execSQL("CREATE TABLE IF NOT EXISTS duties (id INTEGER PRIMARY KEY, subject_name TEXT, duty_name TEXT, pass_date TEXT, extra TEXT)");
        db.execSQL(String.format("INSERT OR IGNORE INTO duties(subject_name, duty_name, pass_date, extra) VALUES ('%s', '%s', '%s', '%s')", subjectName, dutyName, passTime, ""));

        setResult(RESULT_OK);
        finish();
    }
}