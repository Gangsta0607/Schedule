package com.schedule.ui;

import static android.content.Context.MODE_PRIVATE;

import android.annotation.SuppressLint;
import android.content.Context;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.card.MaterialCardView;
import com.schedule.R;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Objects;

public class HomeFragment extends Fragment {

    String[] days = {"mon", "tue", "wed", "thu", "fri", "sat", "sun"};
    String[] days_full = {"Понедельник", "Вторник", "Среда", "Четверг", "Пятница", "Суббота", "Воскресенье"};
    SwipeRefreshLayout layout;

    public HomeFragment() {
        // Required empty public constructor
    }

    @SuppressLint({"DefaultLocale", "Recycle", "SetTextI18n", "InflateParams"})
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v;
        SharedPreferences sf = requireContext().getSharedPreferences("timetable", Context.MODE_PRIVATE);
        if (sf.getAll().size() == 0) {
            v = inflater.inflate(R.layout.fragment_home_empty, container, false);
        } else {
            Calendar calendar = new GregorianCalendar();
            int day_of_week = (calendar.get(Calendar.DAY_OF_WEEK) + 5) % 7;

            String filename = "timetable";
            if (sf.getBoolean("altTimetable", false)) {
                if (sf.getBoolean("firstTimetable", false)) {
                    if (calendar.get(Calendar.WEEK_OF_YEAR) % 2 == 0)
                        filename = "timetable";
                    else filename = "timetable_alt";
                } else {
                    if (calendar.get(Calendar.WEEK_OF_YEAR) % 2 != 0)
                        filename = "timetable";
                    else filename = "timetable_alt";
                }
            }

            String timeline_filename;
            SQLiteDatabase db;
            try {
                requireContext();
                db = requireContext().openOrCreateDatabase("app.db", MODE_PRIVATE, null);
                Cursor query = db.rawQuery(String.format("SELECT * FROM timeline_exceptions WHERE day_name='%s'", days[day_of_week]), null);
                query.moveToFirst();
                if (query.getCount() > 0 && query.getInt(2) == calendar.get(Calendar.WEEK_OF_YEAR) % 2) {
                    timeline_filename = "timeline_exception" + query.getInt(0);
                }
                else {
                    timeline_filename = "timeline";
                }
            } catch (android.database.sqlite.SQLiteException e) {
                timeline_filename = "timeline";
            }

            SharedPreferences timeline = requireContext().getSharedPreferences(timeline_filename, Context.MODE_PRIVATE);
            SharedPreferences sp = requireContext().getSharedPreferences(filename, Context.MODE_PRIVATE);

            boolean differentTimelines = false;
            if (timeline_filename.equals("timeline")) {
                if (timeline.getBoolean("not_one_day", false)) {
                    differentTimelines = true;
                }
            }
            differentTimelines = !differentTimelines;
            int lessonsCount = sp.getInt("lessonsCount", 0);
            //START_TIME
            String startTime = "";
            for (int i = 0; i < lessonsCount; i++) {

                Log.i(String.format("days[%d]", day_of_week), days[day_of_week]);
                if (!Objects.equals(sp.getString(String.format("%s_%d", days[day_of_week], i + 1), ""), "[нет пары]")) {
                    startTime = timeline.getString(differentTimelines ? "start" + (i + 1) : "start" + (i + 1) + days[day_of_week], "");
                    break;
                }
            }
            String[] start_time_split = Objects.requireNonNull(startTime).split(":");
            Calendar start_time = new GregorianCalendar();
            start_time.set(Calendar.HOUR_OF_DAY, Integer.parseInt(start_time_split[0]));
            start_time.set(Calendar.MINUTE, Integer.parseInt(start_time_split[1]));
            //END_TIME
            String endTime = "";
            for (int i = lessonsCount - 1; i >= 0; i--) {
                if (!Objects.equals(sp.getString(String.format("%s_%d", days[day_of_week], i + 1), ""), "[нет пары]")) {
                    endTime = timeline.getString(differentTimelines ? "end" + (i + 1) : "end" + (i + 1) + days[day_of_week], "");
                    break;
                }
            }
            Log.i("startTime", startTime);
            Log.i("endTime", endTime);
            String[] end_time_split = Objects.requireNonNull(endTime).split(":");
            Calendar end_time = new GregorianCalendar();
            end_time.set(Calendar.HOUR_OF_DAY, Integer.parseInt(end_time_split[0]));
            end_time.set(Calendar.MINUTE, Integer.parseInt(end_time_split[1]));

            boolean showtime = checkIfTime(calendar, end_time) && sp.getBoolean(String.format("%s_show", days[day_of_week]), false);
            if (showtime) {
                v = inflater.inflate(R.layout.fragment_home_studytime, container, false);
                layout = v.findViewById(R.id.swiperefresh);
                layout.setOnRefreshListener(() -> {
                    if (getParentFragment() != null) {
                        FragmentManager fm = getParentFragment().getChildFragmentManager();
                        Fragment frg = fm.getFragments().get(0);
                        fm.beginTransaction().detach(frg).commit();
                        fm.beginTransaction().attach(frg).commit();
                    }
                    layout.setRefreshing(false);
                });
                MaterialCardView day = v.findViewById(R.id.day_timetable_main);
                ((TextView) day.findViewById(R.id.day_name)).setText(String.format("%s, %d", days_full[day_of_week], calendar.get(Calendar.DATE)));
                boolean markedAsCurrent = false;
                for (int j = 0; j < lessonsCount; j++) {
                    String sbj_name = sp.getString(days[day_of_week] + "_" + (j + 1), "");
                    if (!Objects.equals(sbj_name, "[нет пары]")) {
                        ConstraintLayout subject = (ConstraintLayout) inflater.inflate(R.layout.subject_part_for_showing, day, false);
                        ((TextView) subject.findViewById(R.id.excercise_number)).setText(String.valueOf(j + 1));
                        ((TextView) subject.findViewById(R.id.subject_name)).setText(sbj_name);

                        if (!markedAsCurrent) {
                            String start_Time = timeline.getString(differentTimelines ? "start" + (j + 1) : "end" + (j + 1) + days[day_of_week], "");
                            String[] start_Time_split_ = Objects.requireNonNull(start_Time).split(":");
                            Calendar start_time_ = new GregorianCalendar();
                            start_time_.set(Calendar.HOUR_OF_DAY, Integer.parseInt(start_Time_split_[0]));
                            start_time_.set(Calendar.MINUTE, Integer.parseInt(start_Time_split_[1]));

                            String end_Time = timeline.getString(differentTimelines ? "end" + (j + 1) : "end" + (j + 1) + days[day_of_week], "");
                            String[] end_time_split_ = Objects.requireNonNull(end_Time).split(":");
                            Calendar end_time_ = new GregorianCalendar();
                            end_time_.set(Calendar.HOUR_OF_DAY, Integer.parseInt(end_time_split_[0]));
                            end_time_.set(Calendar.MINUTE, Integer.parseInt(end_time_split_[1]));
                            if (checkIfTime(start_time_, calendar) && checkIfTime(calendar, end_time_)) {
                                subject.setBackgroundColor(getResources().getColor(R.color.light_gray_background));
                                markedAsCurrent = true;
                            }
                        }
                        String auditory = sp.getString(days[day_of_week] + "_auditory", "");
                        ((TextView) subject.findViewById(R.id.auditory)).setText(auditory);

                        String start_time_ = timeline.getString(differentTimelines ? "end" + (j + 1) : "end" + (j + 1) + days[day_of_week], "");
                        ((TextView) subject.findViewById(R.id.startTime)).setText(start_time_);

                        ((LinearLayout) day.findViewById(R.id.day_layout)).addView(subject);
                    }
                }
            } else {
                v = inflater.inflate(R.layout.fragment_home_after, container, false);
                int NextDay = day_of_week < 6 ? day_of_week + 1 : 0;
                layout = v.findViewById(R.id.swiperefresh);
                layout.setOnRefreshListener(() -> {
                    if (getParentFragment() != null) {
                        FragmentManager fm = getParentFragment().getChildFragmentManager();
                        Fragment frg = fm.getFragments().get(0);
                        fm.beginTransaction().detach(frg).commit();
                        fm.beginTransaction().attach(frg).commit();
                    }
                    layout.setRefreshing(false);
                });
                MaterialCardView day = v.findViewById(R.id.day_timetable_main);
                if (sp.getBoolean(days[NextDay] + "_show", false))
                    for (int j = 0; j < lessonsCount; j++) {
                        ConstraintLayout subject = (ConstraintLayout) inflater.inflate(R.layout.subject_part_for_showing, day, false);
                        ((TextView) subject.findViewById(R.id.excercise_number)).setText(String.valueOf(j + 1));

                        String sbj_name = sp.getString(days[NextDay] + "_" + (j + 1), "");
                        if (!Objects.equals(sbj_name, "[нет пары]")) {
                            ((TextView) subject.findViewById(R.id.subject_name)).setText(sbj_name);
                            String auditory = sp.getString(days[NextDay] + "_auditory", "");
                            ((TextView) subject.findViewById(R.id.auditory)).setText(auditory);
                            String start_time_ = timeline.getString(differentTimelines ? "start" + (j + 1) : "start" + (j + 1) + days[day_of_week], "");
                            ((TextView) subject.findViewById(R.id.startTime)).setText(start_time_);
                            ((LinearLayout) day.findViewById(R.id.day_layout)).addView(subject);
                        }
                    }
                if (((LinearLayout) day.findViewById(R.id.day_layout)).getChildCount() > 1) {
                    ((TextView) day.findViewById(R.id.day_name)).setText("Пары на завтра");
                } else ((TextView) day.findViewById(R.id.day_name)).setText("Пар завтра нет");

                MaterialCardView duties_on_tomorrow = v.findViewById(R.id.duties_on_next_day);
                LinearLayout dutiesList = duties_on_tomorrow.findViewById(R.id.duties_list);

                //!!
                calendar.add(Calendar.DAY_OF_YEAR, 1);
                if (calendar.get(Calendar.DAY_OF_MONTH) == 1)
                    calendar.add(Calendar.MONTH, 1);
                calendar.add(Calendar.MONTH, 1);
                String day_ = String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));
                if (day_.length() == 1) day_ = "0" + day_;
                String month = String.valueOf(calendar.get(Calendar.MONTH));
                if (month.length() == 1) month = "0" + month;

                String nextDay = String.format("%s.%s.%s", day_, month, calendar.get(Calendar.YEAR));
                //!!

                db = requireContext().openOrCreateDatabase("app.db", MODE_PRIVATE, null);
                Cursor query;
                try {
                    query = db.rawQuery(String.format("SELECT * FROM duties WHERE pass_date='%s'", nextDay), null);
                } catch (android.database.sqlite.SQLiteException e) {
                    ((TextView) duties_on_tomorrow.findViewById(R.id.duties_on_next_day_hint)).setText("Долгов на завтра нет");
                    return v;
                }
                if (!query.moveToFirst()) {
                    ((TextView) duties_on_tomorrow.findViewById(R.id.duties_on_next_day_hint)).setText("Долгов на завтра нет");
                } else {
                    ((TextView) duties_on_tomorrow.findViewById(R.id.duties_on_next_day_hint)).setText("Долги на завтра");
                    do {
                        @SuppressLint("InflateParams") MaterialCardView card = (MaterialCardView) inflater.inflate(R.layout.duty_card, null);
                        ((TextView) card.findViewById(R.id.subjectId)).setText(query.getString(1));
                        ((TextView) card.findViewById(R.id.dutyId)).setText(String.valueOf(query.getInt(0)));

                        ((TextView) card.findViewById(R.id.subject_name_on_card)).setText(query.getString(1));
                        ((TextView) card.findViewById(R.id.duty_name_on_card)).setText(query.getString(2));
                        card.findViewById(R.id.pass_time_on_card).setVisibility(View.GONE);
                        if (!Objects.equals(query.getString(4), ""))
                            ((TextView) card.findViewById(R.id.extra_on_card)).setText("Дополнительные данные: " + query.getString(4));
                        dutiesList.addView(card);
                    } while (query.moveToNext());
                }
                query.close();
                db.close();
            }

        }
        return v;
    }

    private Boolean checkIfTime(Calendar calendar, Calendar end_time) {
        boolean showtime = false;
        if (calendar.get(Calendar.HOUR_OF_DAY) < end_time.get(Calendar.HOUR_OF_DAY)) {
            showtime = true;
        } else if (calendar.get(Calendar.HOUR_OF_DAY) == end_time.get(Calendar.HOUR_OF_DAY)) {
            if (calendar.get(Calendar.MINUTE) <= end_time.get(Calendar.MINUTE))
                showtime = true;
        }
        return showtime;
    }
}