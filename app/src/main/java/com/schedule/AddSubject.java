package com.schedule;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.schedule.databinding.ActivityAddSubjectBinding;
import java.util.Objects;

public class AddSubject extends AppCompatActivity {
    Boolean edit;
    Integer subjectId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        com.schedule.databinding.ActivityAddSubjectBinding binding = ActivityAddSubjectBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        edit = (Boolean) getIntent().getSerializableExtra("EDIT");
        if (edit) subjectId = getIntent().getIntExtra("SUBJECT_ID", 0);
        if (!edit) this.setTitle("Добавление предмета");
        else this.setTitle("Редактирование предмета");

        ((CheckBox)findViewById(R.id.if_lections)).setOnCheckedChangeListener((buttonView, isChecked) -> findViewById(R.id.lector_name).setVisibility(((CheckBox)findViewById(R.id.if_lections)).isChecked() ? View.VISIBLE : View.GONE));
        ((CheckBox)findViewById(R.id.if_practics)).setOnCheckedChangeListener((buttonView, isChecked) -> findViewById(R.id.practics_teacher_name).setVisibility(((CheckBox)findViewById(R.id.if_practics)).isChecked() ? View.VISIBLE : View.GONE));
        ((CheckBox)findViewById(R.id.if_labs)).setOnCheckedChangeListener((buttonView, isChecked) -> findViewById(R.id.labs_teacher_name).setVisibility(((CheckBox)findViewById(R.id.if_labs)).isChecked() ? View.VISIBLE : View.GONE));

        if (edit) {
            SQLiteDatabase db = getBaseContext().openOrCreateDatabase("app.db", MODE_PRIVATE, null);
            Cursor query = db.rawQuery("SELECT * FROM subjects WHERE id=" + subjectId, null);
            query.moveToFirst();
            String subjectName = query.getString(1);
            String lectorName = query.getString(2);
            String practicsName = query.getString(3);
            String labsName = query.getString(4);

            ((TextView) findViewById(R.id.subject_name)).setText(subjectName);

            ((CheckBox) findViewById(R.id.if_lections)).setChecked(lectorName != null);
            if (((CheckBox) findViewById(R.id.if_lections)).isChecked()) {
                findViewById(R.id.lector_name).setVisibility(View.VISIBLE);
                ((TextView) findViewById(R.id.lector_name)).setText(lectorName);
            }
            ((CheckBox) findViewById(R.id.if_practics)).setChecked(practicsName != null);
            if (((CheckBox) findViewById(R.id.if_practics)).isChecked()) {
                findViewById(R.id.practics_teacher_name).setVisibility(View.VISIBLE);
                ((TextView) findViewById(R.id.practics_teacher_name)).setText(practicsName);
            }
            ((CheckBox) findViewById(R.id.if_labs)).setChecked(labsName != null);
            if (((CheckBox) findViewById(R.id.if_labs)).isChecked()) {
                findViewById(R.id.labs_teacher_name).setVisibility(View.VISIBLE);
                ((TextView) findViewById(R.id.labs_teacher_name)).setText(labsName);
            }
            query.close();
        }
    }

    @SuppressLint("DefaultLocale")
    public void addSubject(View view) {
        TextInputEditText subject_name = findViewById(R.id.subject_name);
        if (Objects.requireNonNull(subject_name.getText()).length() == 0) {
            Snackbar.make(view, "Введите название предмета", Snackbar.LENGTH_LONG).show();
            return;
        }
        CheckBox[] checkboxes = new CheckBox[3];
        checkboxes[0] = findViewById(R.id.if_lections);
        checkboxes[1] = findViewById(R.id.if_practics);
        checkboxes[2] = findViewById(R.id.if_labs);
        if (!checkboxes[0].isChecked() && !checkboxes[1].isChecked() && !checkboxes[2].isChecked()) {
            Snackbar.make(view, "Выберите один из видов деятельности", Snackbar.LENGTH_LONG).show();
            return;
        }

        TextInputEditText[] names = new TextInputEditText[3];
        names[0] = findViewById(R.id.lector_name);
        names[1] = findViewById(R.id.practics_teacher_name);
        names[2] = findViewById(R.id.labs_teacher_name);
        String[] sfx = new String[] {"лектора", "преподавателя на ПЗ", "преподавателя на ЛР"};
        for (int i = 0; i < 3; i++) {
            if (checkboxes[i].isChecked()) {
                if (Objects.requireNonNull(names[i].getText()).length() == 0) {
                    Snackbar.make(view, "Введите ФИО " + sfx[i], Snackbar.LENGTH_LONG).show();
                    return;
                }
            }
        }
        //actual writing to file
        String subjectName = subject_name.getText().toString();
        String lectorName = checkboxes[0].isChecked() ? "'" + Objects.requireNonNull(names[0].getText()) + "'" : "NULL";
        String practicsName = checkboxes[1].isChecked() ? "'" + Objects.requireNonNull(names[1].getText()) + "'" : "NULL";
        String labsName = checkboxes[2].isChecked() ? "'" + Objects.requireNonNull(names[2].getText()) + "'" : "NULL";

        SQLiteDatabase db = getBaseContext().openOrCreateDatabase("app.db", MODE_PRIVATE, null);
        db.execSQL("CREATE TABLE IF NOT EXISTS subjects (id INTEGER PRIMARY KEY, subject_name TEXT, lector_name TEXT, practics_teacher_name TEXT, labs_teacher_name TEXT)");
        if (edit) {
            db.execSQL(String.format("UPDATE subjects SET subject_name='%s', lector_name=%s, practics_teacher_name=%s, labs_teacher_name=%s WHERE ID=%d", subjectName, lectorName, practicsName, labsName, subjectId));
        }
        else {
            db.execSQL(String.format("INSERT OR IGNORE INTO subjects(subject_name, lector_name, practics_teacher_name, labs_teacher_name) VALUES ('%s', %s, %s, %s)", subjectName, lectorName, practicsName, labsName));
        }

        setResult(RESULT_OK);
        finish();
    }
}