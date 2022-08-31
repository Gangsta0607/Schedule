package com.schedule;

import androidx.appcompat.app.AppCompatActivity;
import android.annotation.SuppressLint;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import java.util.Objects;

public class DutyShowActivity extends AppCompatActivity {
    String dutyId, subjectName;
    SQLiteDatabase db;
    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_duty_show);

        dutyId = (String) getIntent().getSerializableExtra("dutyId");
        subjectName = (String) getIntent().getSerializableExtra("subjectName");

        db = openOrCreateDatabase("app.db", MODE_PRIVATE, null);
        String sql_req = String.format("SELECT * FROM duties WHERE id='%s'", dutyId);
        Cursor query = db.rawQuery(sql_req, null);
        query.moveToFirst();
        setTitle(query.getString(2));

        ((TextView) findViewById(R.id.pass_time_on_card)).setText("Срок сдачи: " + query.getString(3));

        Cursor query2 = db.rawQuery(String.format("select * from subjects where subject_name='%s'", subjectName), null);
        query2.moveToFirst();
        ((TextView) findViewById(R.id.subject_name_on_card)).setText(query2.getString(1));
        query2.close();
        ((TextInputEditText) findViewById(R.id.extra_on_card)).setText(query.getString(4));
        query.close();

        ((Button) findViewById(R.id.save_extra)).setOnClickListener(this::saveExtra);
        ((Button) findViewById(R.id.delete_duty)).setOnClickListener(this::deleteDuty);
    }

    private void deleteDuty(View view) {
        db.execSQL("DELETE FROM duties WHERE id=" + dutyId);
        db.close();
        setResult(-2);
        finish();
    }

    private void saveExtra(View view) {
        db.execSQL(String.format("UPDATE duties SET extra='%s' WHERE id=%s", Objects.requireNonNull(((TextInputEditText) findViewById(R.id.extra_on_card)).getText()), dutyId));
        Snackbar.make(this.getCurrentFocus(), "Успешно изменено", Snackbar.LENGTH_SHORT).show();
    }
}