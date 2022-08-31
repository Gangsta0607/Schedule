package com.schedule;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import com.google.android.material.card.MaterialCardView;

public class SubjectShowActivity extends AppCompatActivity {
    Integer subjectId;
    SQLiteDatabase db;
    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subject_show);
        String subjectName = (String) getIntent().getSerializableExtra("subjectName");
        db = openOrCreateDatabase("app.db", MODE_PRIVATE, null);
        String sql_req = String.format("SELECT * FROM subjects WHERE subject_name='%s'", subjectName);
        Log.i("sql_req", sql_req);
        @SuppressLint("Recycle") Cursor query = db.rawQuery(sql_req, null);
        this.setTitle(subjectName);
        query.moveToFirst();
        subjectId = query.getInt(0);

        MaterialCardView card = findViewById(R.id.subject_card);
        if (query.getString(2) != null)
            ((TextView) card.findViewById(R.id.lection_text_on_card)).setText("Лектор: " + query.getString(2));
        else
            ((TextView) card.findViewById(R.id.lection_text_on_card)).setVisibility(View.GONE);
        if (query.getString(3) != null)
            ((TextView) card.findViewById(R.id.practics_text_on_card)).setText("Преподаватель ПЗ: " + query.getString(3));
        else
            ((TextView) card.findViewById(R.id.practics_text_on_card)).setVisibility(View.GONE);
        if (query.getString(4) != null)
            ((TextView) card.findViewById(R.id.labs_text_on_card)).setText("Преподаватель ЛР: " + query.getString(4));
        else
            ((TextView) card.findViewById(R.id.labs_text_on_card)).setVisibility(View.GONE);

        findViewById(R.id.edit_subject_button).setOnClickListener(this::editSubject);
        findViewById(R.id.delete_subject_button).setOnClickListener(this::deleteSubject);
    }

    private void deleteSubject(View view) {
        Log.i("l", "DELETE FROM subjects WHERE id=" + subjectId);
        db.execSQL("DELETE FROM subjects WHERE id=" + subjectId);
        setResult(-2);
        finish();
    }

    ActivityResultLauncher<Intent> mStartForResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if(result.getResultCode() == Activity.RESULT_OK) {
            SubjectShowActivity.this.recreate();
        }
    });

    private void editSubject(View view) {
        Intent intent = new Intent(this.getBaseContext(), AddSubject.class);
        intent.putExtra("EDIT", true);
        intent.putExtra("SUBJECT_ID", subjectId);
        mStartForResult.launch(intent);
    }
}