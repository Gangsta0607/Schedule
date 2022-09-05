package com.schedule.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.schedule.AddTimeline;
import com.schedule.AddTimelineException;
import com.schedule.AddTimetable;
import com.schedule.R;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Map;
import java.util.Objects;


public class TimetableFragment extends Fragment {

    ActivityResultLauncher<Intent> mStartForResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() == Activity.RESULT_OK) {
            Snackbar.make(requireView(), "Расписание успешно создано", Snackbar.LENGTH_SHORT).show();
            FragmentManager fm = requireParentFragment().getChildFragmentManager();
            Fragment frg = fm.getFragments().get(0);
            fm.beginTransaction().detach(frg).commit();
            fm.beginTransaction().attach(frg).commit();
        }
    });

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        Calendar calendar = new GregorianCalendar();
        SharedPreferences sf = requireContext().getSharedPreferences("timetable", Context.MODE_PRIVATE);
        boolean altTimetable = sf.getBoolean("altTimetable", false);
        Map<String, ?> allPreferences = sf.getAll();
        boolean s = allPreferences.size() > 0;
        View v;
        if (!s) {
            v = inflater.inflate(R.layout.fragment_timetable_empty, container, false);
            FloatingActionButton fab = v.findViewById(R.id.fab);
            fab.setOnClickListener(this::startNew);
        } else {
            v = inflater.inflate(R.layout.fragment_timetable, container, false);
            LinearLayout cl = v.findViewById(R.id.timetable);
            String filename = "timetable";
            int week_number = 0;
            if (altTimetable) {
                if (sf.getBoolean("firstTimetable", false)) {
                    if (calendar.get(Calendar.WEEK_OF_YEAR) % 2 == 0) {
                        filename = "timetable";
                    }
                    else {
                        filename = "timetable_alt";
                        week_number = 1;
                    }
                } else {
                    if (calendar.get(Calendar.WEEK_OF_YEAR) % 2 != 0) {
                        filename = "timetable";
                    }
                    else {
                        filename = "timetable_alt";
                        week_number = 1;
                    }
                }
            }
            SharedPreferences timetable = requireContext().getSharedPreferences(filename, Context.MODE_PRIVATE);
            SharedPreferences timeline = requireContext().getSharedPreferences("timeline", Context.MODE_PRIVATE);
            boolean notOneDay = timeline.getBoolean("not_one_day", false);
            String[] days = {"mon", "tue", "wed", "thu", "fri", "sat"};
            String[] days_full = {"Понедельник", "Вторник", "Среда", "Четверг", "Пятница", "Суббота"};
            for (int i = 0; i < 6; i++) {
                if (!timetable.getBoolean(days[i] + "_show", false)) {
                    continue;
                }
                int lessonsCount = timetable.getInt("lessonsCount", 0);
                MaterialCardView day = (MaterialCardView) inflater.inflate(R.layout.timetable_day, (ViewGroup) v, false);
                ((TextView) day.findViewById(R.id.day_name)).setText(days_full[i]);
                for (int j = 0; j < lessonsCount; j++) {
                    String sbj_name = timetable.getString(days[i] + "_" + (j + 1), "");
                    if (!Objects.equals(sbj_name, "[нет пары]")) {
                        ConstraintLayout subject = (ConstraintLayout) inflater.inflate(R.layout.subject_part_for_showing, day, false);
                        ((TextView) subject.findViewById(R.id.excercise_number)).setText(String.valueOf(j + 1));
                        ((TextView) subject.findViewById(R.id.subject_name)).setText(sbj_name);

                        String auditory = timetable.getString(days[i] + "_" + (j + 1) + "_auditory", "");
                        ((TextView) subject.findViewById(R.id.auditory)).setText(auditory);
                        String start_time;

                        SQLiteDatabase db;
                        try {
                            requireContext();
                            db = requireContext().openOrCreateDatabase("app.db", Context.MODE_PRIVATE, null);
                            Log.i("q2", String.format("SELECT * FROM timeline_exceptions WHERE day_name='%s'", days_full[i]));
                            @SuppressLint("Recycle") Cursor query = db.rawQuery(String.format("SELECT * FROM timeline_exceptions WHERE day_name='%s'", days_full[i]), null);
                            query.moveToFirst();
                            if (query.getCount() > 0 && query.getInt(2) == week_number) {
                                requireContext();
                                SharedPreferences qq = requireContext().getSharedPreferences("timeline_exception" + query.getInt(0), Context.MODE_PRIVATE);
                                    Log.i("qq", qq.getAll().toString());
                                    start_time = qq.getString("start" + (j + 1), "");
                                    ((TextView) subject.findViewById(R.id.startTime)).setText(start_time);
                                }
                                else {
                                    if (notOneDay)
                                        start_time = timeline.getString("start" + (j + 1) + days[i], "");
                                    else start_time = timeline.getString("start" + (j + 1), "");
                                    ((TextView) subject.findViewById(R.id.startTime)).setText(start_time);
                                }
                        } catch (android.database.sqlite.SQLiteException e) {
                            if (notOneDay)
                                start_time = timeline.getString("start" + (j + 1) + days[i], "");
                            else start_time = timeline.getString("start" + (j + 1), "");
                            ((TextView) subject.findViewById(R.id.startTime)).setText(start_time);
                        }

                        ((LinearLayout) day.findViewById(R.id.day_layout)).addView(subject);
                    }
                }

                cl.addView(day);
            }
            FloatingActionButton fab = v.findViewById(R.id.fab);
            fab.setOnClickListener(this::edit);
            FloatingActionButton fab2 = v.findViewById(R.id.fab2);
            fab2.setOnClickListener(this::edit_timeline);
            fab2.setOnLongClickListener(this::add_timeline_exception);
        }
        return v;
    }

    private boolean add_timeline_exception(View view) {
        Intent intent = new Intent(this.getContext(), AddTimelineException.class);
        intent.putExtra("EDIT", false);
        mStartForResult.launch(intent);
        return true;
    }

    private void edit_timeline(View view) {
        Intent intent = new Intent(this.getContext(), AddTimeline.class);
        intent.putExtra("EDIT", false);
        mStartForResult.launch(intent);
    }

    private void edit(View view) {
        Intent intent = new Intent(this.getContext(), AddTimetable.class);
        intent.putExtra("EDIT", true);
        mStartForResult.launch(intent);
    }

    void startNew(View view) {
        Intent intent = new Intent(this.getContext(), AddTimetable.class);
        intent.putExtra("EDIT", false);
        mStartForResult.launch(intent);
    }

}