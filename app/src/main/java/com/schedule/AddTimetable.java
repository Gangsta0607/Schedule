package com.schedule;

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
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;

@SuppressLint({"InflateParams", "DefaultLocale"})
public class AddTimetable extends AppCompatActivity {
    ArrayList<String> subjects = new ArrayList<>();
    ArrayAdapter<String> adapter;
    SeekBar lessonsCount;

    String[] days = {"Понедельник", "Вторник", "Среда", "Четверг", "Пятница", "Суббота"};
    String[] days_short = {"mon", "tue", "wed", "thu", "fri", "sat"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_timetable);

        Boolean edit = (Boolean) getIntent().getSerializableExtra("EDIT");
        if (!edit) this.setTitle("Добавление расписания");
        else this.setTitle("Редактирование расписания");

        createSubjectsList();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, subjects);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        lessonsCount = findViewById(R.id.lessons_count);
        lessonsCount.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                changeLessonCount();
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });
        if (edit) {
            SharedPreferences timetable_ = getSharedPreferences("timetable", MODE_PRIVATE);
            lessonsCount.setProgress(timetable_.getInt("lessonsCount", 3));
        }
        ((TextView) findViewById(R.id.lessons_count_hint)).setText(String.format("Кол-во пар: %d", lessonsCount.getProgress()));

        int[] timetable_ids = {R.id.timetable, R.id.timetable_alt};
        String[] fileNames = {"timetable", "timetable_alt"};
        for (int id = 0; id < 2; id++) {
            LinearLayout days_layout = findViewById(timetable_ids[id]);
            SharedPreferences timetable = getSharedPreferences(fileNames[id], MODE_PRIVATE);
            for (int i = 0; i < 6; i++) {
                MaterialCardView day_card = (MaterialCardView) getLayoutInflater().inflate(R.layout.timetable_add_daycard, days_layout, false);
                LinearLayout subjects_list = day_card.findViewById(R.id.subjects_on_addcard);
                ((TextView) day_card.findViewById(R.id.day_name)).setText(days[i]);
                ((CheckBox) day_card.findViewById(R.id.if_add_day)).setOnCheckedChangeListener(this::checkBoxClick);
                if (edit) {
                    boolean ifShow = timetable.getBoolean(days_short[i] + "_show", true);
                    subjects_list.setVisibility(ifShow ? View.VISIBLE : View.GONE);
                    ((CheckBox) day_card.findViewById(R.id.if_add_day)).setChecked(ifShow);
                }

                for (int j = 0; j < lessonsCount.getProgress(); j++) {
                    ConstraintLayout subject = (ConstraintLayout) getLayoutInflater().inflate(R.layout.subject_part_for_adding, null);
                    ((TextView) subject.findViewById(R.id.excercise_number)).setText(String.valueOf(j + 1));
                    Spinner spinner = subject.findViewById(R.id.spinner);
                    spinner.setAdapter(adapter);
                    if (edit) {
                        String subj = timetable.getString(days_short[i] + "_" + (j + 1), "");
                        if (subj.length() == 0) spinner.setSelection(subjects.size() - 1, true);
                        else spinner.setSelection(adapter.getPosition(subj));
                        String auditory = timetable.getString(days_short[i] + "_" + (j + 1) + "_auditory", "");
                        EditText auditory_ = subject.findViewById(R.id.auditory);
                        if (subj.length() > 0) auditory_.setText(auditory);
                    } else spinner.setSelection(subjects.size() - 1, true);
                    subjects_list.addView(subject);
                }
                days_layout.addView(day_card);
            }
        }

        findViewById(R.id.fab).setOnClickListener(this::createTimetable);
        findViewById(R.id.two_timetables).setOnClickListener(this::showAltTimetable);
    }

    private void createSubjectsList() {
        SQLiteDatabase db = openOrCreateDatabase("app.db", MODE_PRIVATE, null);
        Cursor query;
        try {
            query = db.rawQuery("SELECT * FROM subjects", null);
        } catch (android.database.sqlite.SQLiteException exception) {
            return;
        }
        query.moveToFirst();

        do {
            String subjectName = query.getString(1);
            Cursor query1 = db.rawQuery(String.format("SELECT * FROM subjects WHERE subject_name='%s'", subjectName), null);
            if (query1.getCount() > 1) subjectName += String.format(" [%s]", query.getString(2));
            query1.close();
            if (query.getString(2) != null) subjects.add(subjectName);
            if (query.getString(3) != null) subjects.add(subjectName + " (ПЗ)");
            if (query.getString(4) != null) subjects.add(subjectName + " (ЛР)");
        } while (query.moveToNext());
        query.close();
        subjects.add("[Форточка]");
        subjects.add("[нет пары]");
    }

    private void showAltTimetable(View view) {
        findViewById(R.id.timetable_alt).setVisibility(((CheckBox) view).isChecked() ? View.VISIBLE : View.GONE);
        findViewById(R.id.timetable1_hint).setVisibility(((CheckBox) view).isChecked() ? View.VISIBLE : View.GONE);
        findViewById(R.id.timetable2_hint).setVisibility(((CheckBox) view).isChecked() ? View.VISIBLE : View.GONE);
        findViewById(R.id.first_schedule_to_show).setVisibility(((CheckBox) view).isChecked() ? View.VISIBLE : View.GONE);
    }

    private void checkBoxClick(CompoundButton compoundButton, boolean b) {
        ((LinearLayout) compoundButton.getParent().getParent()).findViewById(R.id.subjects_on_addcard).setVisibility(b ? View.VISIBLE : View.GONE);
    }

    private void changeLessonCount() {
        ((TextView) findViewById(R.id.lessons_count_hint)).setText(String.format("Кол-во пар: %d", lessonsCount.getProgress()));
        LinearLayout timetable = findViewById(R.id.timetable);
        for (int i = 0; i < 6; i++) {
            MaterialCardView timetable_day = (MaterialCardView) timetable.getChildAt(i);
            if (timetable_day == null) return;
            LinearLayout lessonsList = timetable_day.findViewById(R.id.subjects_on_addcard);
            while (lessonsList.getChildCount() < lessonsCount.getProgress()) {
                ConstraintLayout subject = (ConstraintLayout) getLayoutInflater().inflate(R.layout.subject_part_for_adding, null);
                ((TextView) subject.findViewById(R.id.excercise_number)).setText(String.valueOf(lessonsList.getChildCount() + 1));
                Spinner spinner = subject.findViewById(R.id.spinner);
                spinner.setAdapter(adapter);
                spinner.setSelection(subjects.size() - 1, true);
                lessonsList.addView(subject);
            }

            while (lessonsList.getChildCount() > lessonsCount.getProgress()) {
                lessonsList.removeViewAt(lessonsList.getChildCount() - 1);
            }
        }
    }


    @SuppressLint("DefaultLocale")
    private void createTimetable(View view) {
        SharedPreferences sp = getSharedPreferences("timetable", Context.MODE_PRIVATE);
        SharedPreferences.Editor e = sp.edit();
        String[] days_short = {"mon", "tue", "wed", "thu", "fri", "sat"};
        int[] timetable_ids = {R.id.timetable, R.id.timetable_alt};
        String[] snackbarHints = {"", "второго расписания"};
        for (int i = 0; i < 2; i++) {
            for (int day = 0; day < 6; day++) {
                MaterialCardView day_card = (MaterialCardView) ((LinearLayout) this.findViewById(timetable_ids[i])).getChildAt(day);
                LinearLayout subjects_list = day_card.findViewById(R.id.subjects_on_addcard);
                if (((CheckBox) day_card.findViewById(R.id.if_add_day)).isChecked()) {
                    boolean ifAny = false;
                    for (int lesson = 0; lesson < lessonsCount.getProgress(); lesson++) {
                        ConstraintLayout subject = (ConstraintLayout) subjects_list.getChildAt(lesson);
                        Spinner spinner = subject.findViewById(R.id.spinner);
                        String item = spinner.getSelectedItem().toString();
                        if (item.length() > 0) {
                            ifAny = true;
                        }
                    }
                    if (!ifAny) {
                        Snackbar.make(view, String.format("Ни одной пары %d дня %s не выбрано. Выберите как минимум одну пару или выключите день", day + 1, snackbarHints[i]), Snackbar.LENGTH_SHORT).show();
                        return;
                    }
                }
            }
            if (!((CheckBox) findViewById(R.id.two_timetables)).isChecked()) break;
        }
        for (int q = 0; q < 2; q++) {
            for (int i = 0; i < 6; i++) {
                MaterialCardView day_card = (MaterialCardView) ((LinearLayout) this.findViewById(timetable_ids[q])).getChildAt(i);
                LinearLayout subjects_list = day_card.findViewById(R.id.subjects_on_addcard);
                if (!((CheckBox) day_card.findViewById(R.id.if_add_day)).isChecked()) {
                    e.putBoolean(days_short[i] + "_show", false);
                    continue;
                } else e.putBoolean(days_short[i] + "_show", true);

                e.putBoolean("altTimetable", ((CheckBox) findViewById(R.id.two_timetables)).isChecked());
                boolean fst = ((RadioButton) findViewById(R.id.first_is_first)).isChecked();
                e.putBoolean("firstTimetable", fst);

                for (int j = 0; j < lessonsCount.getProgress(); j++) {
                    ConstraintLayout subject = (ConstraintLayout) subjects_list.getChildAt(j);
                    Spinner spinner = subject.findViewById(R.id.spinner);
                    String item = spinner.getSelectedItem().toString();
                    e.putString(days_short[i] + "_" + (j + 1), item);
                    String auditory = ((EditText) subject.findViewById(R.id.auditory)).getText().toString();
                    e.putString(days_short[i] + "_" + (j + 1) + "_auditory", auditory);
                }
            }
            e.putInt("lessonsCount", lessonsCount.getProgress());
            e.apply();
            if (!((CheckBox) findViewById(R.id.two_timetables)).isChecked()) break;
        }
        setResult(RESULT_OK);
        finish();
    }

}