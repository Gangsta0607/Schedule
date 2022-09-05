package com.schedule;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Objects;

@SuppressLint("CutPasteId")
public class AddDuty extends AppCompatActivity {
    SQLiteDatabase db;
    boolean edit;
    String dutyId;
    Button button;
    Calendar dateAndTime = new GregorianCalendar();
    // установка обработчика выбора даты
    DatePickerDialog.OnDateSetListener d = new DatePickerDialog.OnDateSetListener() {
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            dateAndTime.set(Calendar.YEAR, year);
            dateAndTime.set(Calendar.MONTH, monthOfYear);
            dateAndTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            String month = String.valueOf(dateAndTime.get(Calendar.MONTH) + 1);
            if (month.length() == 1) month = "0" + month;
            String day = String.valueOf(dateAndTime.get(Calendar.DAY_OF_MONTH));
            if (day.length() == 1) day = "0" + day;
            String date = String.format("%s.%s.%s", day, month, dateAndTime.get(Calendar.YEAR));
            button.setText(date);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_duty);
        edit = (Boolean) getIntent().getSerializableExtra("EDIT");
        findViewById(R.id.fab).setOnClickListener(this::addDuty);
        db = getBaseContext().openOrCreateDatabase("app.db", MODE_PRIVATE, null);
        Cursor query = db.rawQuery("SELECT * FROM subjects", null);
        query.moveToFirst();
        ArrayList<String> subjects = new ArrayList<>();
        do {
            String subjectName = query.getString(1);
            Cursor query1 = db.rawQuery(String.format("SELECT * FROM subjects WHERE subject_name='%s'", subjectName), null);
            if (query1.getCount() > 1) subjectName += String.format(" [%s]", query.getString(2));
            query1.close();
            subjects.add(subjectName);
        }
        while (query.moveToNext());
        query.close();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, subjects);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        ((Spinner) findViewById(R.id.subject_name_spinner)).setAdapter(adapter);
        findViewById(R.id.datePicker).setOnClickListener(this::askForDate);
        if (edit) {
            this.setTitle("Редактирование долга");
            dutyId = (String) getIntent().getSerializableExtra("DUTY_ID");
            Log.i("query", "SELECT * FROM duties where id=" + dutyId);
            Cursor query1 = db.rawQuery("SELECT * FROM duties where id=" + dutyId, null);
            Log.i("query_", query1.toString());
            query1.moveToFirst();
            ((TextInputEditText) findViewById(R.id.duty_name)).setText(query1.getString(2));
            ((EditText) findViewById(R.id.extra_data)).setText(query1.getString(4));
            ((Button) findViewById(R.id.datePicker)).setText(query1.getString(3));
            String[] split = ((Button) findViewById(R.id.datePicker)).getText().toString().split("\\.");
            dateAndTime.set(Integer.parseInt(split[2]), Integer.parseInt(split[1]) - 1, Integer.parseInt(split[0]));
            ((Spinner) findViewById(R.id.subject_name_spinner)).setSelection(adapter.getPosition(query1.getString(1)));
            query1.close();
        } else this.setTitle("Добавление долга");
        button = findViewById(R.id.datePicker);
    }

    public void askForDate(View view) {
        new DatePickerDialog(AddDuty.this, d, dateAndTime.get(Calendar.YEAR), dateAndTime.get(Calendar.MONTH), dateAndTime.get(Calendar.DAY_OF_MONTH)).show();
    }

    public void addDuty(View view) {
        String dutyName = Objects.requireNonNull(((TextInputEditText) findViewById(R.id.duty_name)).getText()).toString();
        if (dutyName.length() == 0) {
            Snackbar.make(view, "Введите название долга", Snackbar.LENGTH_LONG).show();
            return;
        }
        String passDate = (String) button.getText();
        if (Objects.equals(passDate, "Выберите дату сдачи")) {
            Snackbar.make(view, "Выберите дату сдачи", Snackbar.LENGTH_LONG).show();
            return;
        }
        String subjectName = (String) ((Spinner) findViewById(R.id.subject_name_spinner)).getSelectedItem();
        String extraData = ((EditText) findViewById(R.id.extra_data)).getText().toString();
        db.execSQL("CREATE TABLE IF NOT EXISTS duties (id INTEGER PRIMARY KEY, subject_name TEXT, duty_name TEXT, pass_date TEXT, extra TEXT)");
        if (edit) {
            db.execSQL(String.format("UPDATE duties SET subject_name='%s', duty_name='%s', pass_date='%s', extra='%s' WHERE ID=%s", subjectName, dutyName, passDate, extraData, dutyId));
        } else
            db.execSQL(String.format("INSERT OR IGNORE INTO duties(subject_name, duty_name, pass_date, extra) VALUES ('%s', '%s', '%s', '%s')", subjectName, dutyName, passDate, extraData));

        setResult(RESULT_OK);
        finish();
    }
}