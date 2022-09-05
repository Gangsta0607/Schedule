package com.schedule.ui;

import static android.content.Context.MODE_PRIVATE;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import com.google.android.material.snackbar.Snackbar;
import com.schedule.R;

public class ProfileFragment extends Fragment {
    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        Button button = view.findViewById(R.id.delete_subjects);
        button.setOnClickListener(v -> {
            SQLiteDatabase db = requireContext().openOrCreateDatabase("app.db", MODE_PRIVATE, null);
            db.execSQL("DROP TABLE subjects");
            db.close();
            reload("Предметы успешно удалены");
        });

        button = view.findViewById(R.id.delete_duties);
        button.setOnClickListener(v -> {
            SQLiteDatabase db = requireContext().openOrCreateDatabase("app.db", MODE_PRIVATE, null);
            db.execSQL("DROP TABLE duties");
            db.close();
            reload("Долги успешно удалены");
        });

        button = view.findViewById(R.id.delete_timetable);
        button.setOnClickListener(v -> {
            requireContext();
            SharedPreferences sp = requireContext().getSharedPreferences("timetable", MODE_PRIVATE);
            SharedPreferences.Editor e = sp.edit();
            e.clear();
            e.apply();

            requireContext();
            sp = requireContext().getSharedPreferences("timetable_alt", MODE_PRIVATE);
            e = sp.edit();
            e.clear();
            e.apply();
            reload("Расписание успешно удалено");
        });

        button = view.findViewById(R.id.delete_timeline);
        button.setOnClickListener(v -> {
            requireContext();
            SharedPreferences sp = requireContext().getSharedPreferences("timeline", MODE_PRIVATE);
            SharedPreferences.Editor e = sp.edit();
            e.clear();
            e.apply();
            reload("Расписание звонков успешно удалено");
        });

        button = view.findViewById(R.id.delete_timeline_exceptions);
        button.setOnClickListener(v -> {
            SQLiteDatabase db = requireContext().openOrCreateDatabase("app.db", MODE_PRIVATE, null);
            Cursor query = db.rawQuery("SELECT * FROM timeline_exceptions", null);
            query.moveToFirst();
            do {
                SharedPreferences exception = requireContext().getSharedPreferences("timeline_exception" + query.getCount(), MODE_PRIVATE);
                SharedPreferences.Editor e = exception.edit();
                e.clear();
                e.apply();
            } while (query.moveToNext());
            query.close();
            db.execSQL("DROP TABLE timeline_exceptions");
            reload("Исключения расписания звонков успешно удалены");
        });

        return view;
    }

    private void reload(String snackbarText) {
        Snackbar.make(requireView(), snackbarText, Snackbar.LENGTH_SHORT).show();
        if (getParentFragment() != null) {
            FragmentManager fm = getParentFragment().getChildFragmentManager();
            Fragment frg = fm.getFragments().get(0);
            fm.beginTransaction().detach(frg).commit();
            fm.beginTransaction().attach(frg).commit();
        }
    }
}