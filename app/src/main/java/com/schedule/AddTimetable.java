package com.schedule;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;

public class AddTimetable extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_timetable);

        Boolean edit = (Boolean) getIntent().getSerializableExtra("EDIT");
        if (!edit) this.setTitle("Добавление расписания");
        else this.setTitle("Редактирование расписания");


        String[] days = { "Понедельник", "Вторник", "Среда", "Четверг", "Пятница", "Суббота" };
        String[] days_short = { "mon", "tue", "wed", "thu", "fri", "sat" };

        //creating list of all subjects
        ArrayList<String> subjects = new ArrayList<>();
        SQLiteDatabase db = openOrCreateDatabase("app.db", MODE_PRIVATE, null);
        Cursor query;
        try {
            query = db.rawQuery("SELECT * FROM subjects", null);
        }
        catch (android.database.sqlite.SQLiteException exception) {
            return;
        }
        query.moveToFirst();
        String subjectName;
        do {
            subjectName = query.getString(1);
            if (query.getString(2) != null) subjects.add(subjectName);
            if (query.getString(3) != null) subjects.add(subjectName + " (ПЗ)");
            if (query.getString(4) != null) subjects.add(subjectName + " (ЛР)");
        } while (query.moveToNext());
        query.close();
        subjects.add("");

        LinearLayout days_layout = findViewById(R.id.timetable);
        SharedPreferences yum = getSharedPreferences("timetable", Context.MODE_PRIVATE);

        for (int i = 0; i < 6; i++) {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, subjects);
            //creating and setting up card
            MaterialCardView day_card = (MaterialCardView) getLayoutInflater().inflate(R.layout.timetable_add_daycard, days_layout, false);
            ((TextView) day_card.findViewById(R.id.day_name)).setText(days[i]);
            ((CheckBox) day_card.findViewById(R.id.if_add_day)).setOnCheckedChangeListener(this::checkBoxClick);

            if (!yum.getBoolean(days_short[i] + "_show", true)) {
                ((CheckBox) day_card.findViewById(R.id.if_add_day)).setChecked(false);
                day_card.findViewById(R.id.subjects_on_addcard).setVisibility(View.GONE);
            }

            int[] subject_parts = { R.id.sbj1, R.id.sbj2, R.id.sbj3, R.id.sbj4, R.id.sbj5 };
            //setting up subjects
            for (int j = 0; j < 5; j++) {
                ConstraintLayout subject = day_card.findViewById(subject_parts[j]);
                ((TextView) subject.findViewById(R.id.excercise_number)).setText(String.valueOf(j + 1));
                Spinner spinner = subject.findViewById(R.id.spinner);
                spinner.setAdapter(adapter);
                if (edit) {
                    String subj = yum.getString(days_short[i] + "_" + (j + 1), "");
                    query = db.rawQuery(String.format("select * from subjects where subject_name='%s'", subj), null);
                    query.moveToFirst();
                    if (query.getPosition() > 0) spinner.setSelection(subjects.indexOf(""));
                    else {
                        spinner.setSelection(adapter.getPosition(subj) != -1 ? adapter.getPosition(subj) : subjects.size() - 1);
                        String auditory = yum.getString(days_short[i] + "_" + (j + 1) + "_auditory", "");
                        if (spinner.getSelectedItem() != "") {
                            EditText auditory_ = subject.findViewById(R.id.auditory);
                            auditory_.setText(auditory);
                        }
                    }
                    query.close();
                }
                else spinner.setSelection(subjects.size() - 1, true);
            }
            days_layout.addView(day_card);
        }

        days_layout = findViewById(R.id.timetable_alt);
        yum = getSharedPreferences("timetable_alt", Context.MODE_PRIVATE);

        for (int i = 0; i < 6; i++) {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, subjects);
            //creating and setting up card
            MaterialCardView day_card = (MaterialCardView) getLayoutInflater().inflate(R.layout.timetable_add_daycard, days_layout, false);
            ((TextView) day_card.findViewById(R.id.day_name)).setText(days[i]);
            ((CheckBox) day_card.findViewById(R.id.if_add_day)).setOnCheckedChangeListener(this::checkBoxClick);

            if (!yum.getBoolean(days_short[i] + "_show", true)) {
                ((CheckBox) day_card.findViewById(R.id.if_add_day)).setChecked(false);
                day_card.findViewById(R.id.subjects_on_addcard).setVisibility(View.GONE);
            }

            int[] subject_parts = { R.id.sbj1, R.id.sbj2, R.id.sbj3, R.id.sbj4, R.id.sbj5 };
            //setting up subjects
            for (int j = 0; j < 5; j++) {
                ConstraintLayout subject = day_card.findViewById(subject_parts[j]);
                ((TextView) subject.findViewById(R.id.excercise_number)).setText(String.valueOf(j + 1));
                Spinner spinner = subject.findViewById(R.id.spinner);
                spinner.setAdapter(adapter);
                if (edit) {
                    String subj = yum.getString(days_short[i] + "_" + (j + 1), "");
                    query = db.rawQuery(String.format("select * from subjects where subject_name='%s'", subj), null);
                    query.moveToFirst();
                    if (query.getPosition() > 0) spinner.setSelection(subjects.indexOf(""));
                    else {
                        spinner.setSelection(adapter.getPosition(subj) != -1 ? adapter.getPosition(subj) : subjects.size() - 1);
                        String auditory = yum.getString(days_short[i] + "_" + (j + 1) + "_auditory", "");
                        if (spinner.getSelectedItem() != "") {
                            EditText auditory_ = subject.findViewById(R.id.auditory);
                            auditory_.setText(auditory);
                        }
                    }
                    query.close();
                }
                else spinner.setSelection(subjects.size() - 1, true);
            }
            days_layout.addView(day_card);
        }

        ((FloatingActionButton) findViewById(R.id.fab)).setOnClickListener(this::createTimetable);
        ((CheckBox) findViewById(R.id.two_timetables)).setOnClickListener(this::showAltTimetable);
    }

    private void showAltTimetable(View view) {
        findViewById(R.id.timetable_alt).setVisibility(((CheckBox) view).isChecked() ? View.VISIBLE : View.GONE);
        findViewById(R.id.timetable1_hint).setVisibility(((CheckBox) view).isChecked() ? View.VISIBLE : View.GONE);
        findViewById(R.id.timetable2_hint).setVisibility(((CheckBox) view).isChecked() ? View.VISIBLE : View.GONE);
        findViewById(R.id.first_schedule_to_show).setVisibility(((CheckBox) view).isChecked() ? View.VISIBLE : View.GONE);
    }

    private void checkBoxClick(CompoundButton compoundButton, boolean b) {
        ((LinearLayout) compoundButton.getParent().getParent()).findViewById(R.id.subjects_on_addcard).setVisibility(b? View.VISIBLE : View.GONE);
    }


    @SuppressLint("DefaultLocale")
    private void createTimetable(View view) {
        SharedPreferences sp = getSharedPreferences("timetable", Context.MODE_PRIVATE);
        SharedPreferences.Editor e = sp.edit();
        String[] days_short = { "mon", "tue", "wed", "thu", "fri", "sat" };
        int[] subject_parts = { R.id.sbj1,R.id.sbj2, R.id.sbj3, R.id.sbj4, R.id.sbj5 };

        for (int day = 0; day < 6; day++) {
            MaterialCardView day_card = (MaterialCardView) ((LinearLayout) this.findViewById(R.id.timetable)).getChildAt(day);
            if (((CheckBox) day_card.findViewById(R.id.if_add_day)).isChecked()) {
                boolean ifAny = false;
                for (int lesson = 0; lesson < 5; lesson++) {
                    ConstraintLayout subject = day_card.findViewById(subject_parts[lesson]);
                    Spinner spinner = subject.findViewById(R.id.spinner);
                    String item = spinner.getSelectedItem().toString();
                    if (item.length() > 0) {
                        ifAny = true;
                    }
                }
                if (!ifAny) {
                    Snackbar.make(view, String.format("Ни одной пары %d дня не выбрано. Выберите как минимум одну пару или выключите день", day + 1), Snackbar.LENGTH_SHORT).show();
                    return;
                }
            }
        }

        if (((CheckBox) findViewById(R.id.two_timetables)).isChecked()) {
            for (int day = 0; day < 6; day++) {
                MaterialCardView day_card = (MaterialCardView) ((LinearLayout) this.findViewById(R.id.timetable_alt)).getChildAt(day);
                if (((CheckBox) day_card.findViewById(R.id.if_add_day)).isChecked()) {
                    boolean ifAny = false;
                    for (int lesson = 0; lesson < 5; lesson++) {
                        ConstraintLayout subject = day_card.findViewById(subject_parts[lesson]);
                        Spinner spinner = subject.findViewById(R.id.spinner);
                        String item = spinner.getSelectedItem().toString();
                        if (item.length() > 0) {
                            ifAny = true;
                        }
                    }
                    if (!ifAny) {
                        Snackbar.make(view, String.format("Ни одной пары %d дня второго расписания не выбрано. Выберите как минимум одну пару или выключите день", day + 1), Snackbar.LENGTH_SHORT).show();
                        return;
                    }
                }
            }
        }

        for (int i = 0; i < 6; i++) {
            MaterialCardView day_card = (MaterialCardView) ((LinearLayout) this.findViewById(R.id.timetable)).getChildAt(i);
            if (!((CheckBox) day_card.findViewById(R.id.if_add_day)).isChecked()) {
                e.putBoolean(days_short[i] + "_show", false);
                continue;
            }
            else e.putBoolean(days_short[i] + "_show", true);

            e.putBoolean("altTimetable", ((CheckBox) findViewById(R.id.two_timetables)).isChecked());
            boolean fst = ((RadioButton) findViewById(R.id.first_is_first)).isChecked();
            e.putBoolean("firstTimetable", fst);

            for (int j = 0; j < 5; j++) {
                ConstraintLayout subject = day_card.findViewById(subject_parts[j]);
                Spinner spinner = subject.findViewById(R.id.spinner);
                String item = spinner.getSelectedItem().toString();
                e.putString(days_short[i] + "_" + (j + 1), item);
                String auditory = ((EditText) subject.findViewById(R.id.auditory)).getText().toString();
                e.putString(days_short[i] + "_" + (j + 1) + "_auditory", auditory);
            }
        }
        e.apply();
        if (((CheckBox) findViewById(R.id.two_timetables)).isChecked()) {
            sp = getSharedPreferences("timetable_alt", Context.MODE_PRIVATE);
            e = sp.edit();
            for (int i = 0; i < 6; i++) {
                MaterialCardView day_card = (MaterialCardView) ((LinearLayout) this.findViewById(R.id.timetable_alt)).getChildAt(i);
                if (!((CheckBox) day_card.findViewById(R.id.if_add_day)).isChecked()) {
                    e.putBoolean(days_short[i] + "_show", false);
                    continue;
                }
                else e.putBoolean(days_short[i] + "_show", true);

                for (int j = 0; j < 5; j++) {
                    ConstraintLayout subject = day_card.findViewById(subject_parts[j]);
                    Spinner spinner = subject.findViewById(R.id.spinner);
                    String item = spinner.getSelectedItem().toString();
                    e.putString(days_short[i] + "_" + (j + 1), item);
                    String auditory = ((EditText) subject.findViewById(R.id.auditory)).getText().toString();
                    e.putString(days_short[i] + "_" + (j + 1) + "_auditory", auditory);
                }
            }
            e.apply();
        }
        setResult(RESULT_OK);
        finish();
    }

}