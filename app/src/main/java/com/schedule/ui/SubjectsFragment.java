package com.schedule.ui;

import static android.content.Context.MODE_PRIVATE;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.schedule.AddSubject;
import com.schedule.R;
import com.schedule.SubjectShowActivity;

public class SubjectsFragment extends Fragment {

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return redraw(inflater, container);
    }

    @SuppressLint("SetTextI18n")
    View redraw(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        View v;
        SQLiteDatabase db = requireContext().openOrCreateDatabase("app.db", MODE_PRIVATE, null);
        Cursor query;
        try {
            query = db.rawQuery("SELECT * FROM subjects", null);
        }
        catch (android.database.sqlite.SQLiteException exception) {
            v = inflater.inflate(R.layout.fragment_subjects_empty, container, false);
            FloatingActionButton fab = v.findViewById(R.id.fab);
            fab.setOnClickListener(this::startNew);
            return v;
        }
        if (!query.moveToFirst()) {
            v = inflater.inflate(R.layout.fragment_subjects_empty, container, false);
        }
        else {
            v = inflater.inflate(R.layout.fragment_subjects, container, false);
            LinearLayout cl = v.findViewById(R.id.sbj);
            do {
                MaterialCardView card = (MaterialCardView) inflater.inflate(R.layout.subject_card, container, false);
                card.setOnClickListener(this::openSubject);
                ((TextView) card.findViewById(R.id.subject_name_on_card)).setText(query.getString(1));
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
                cl.addView(card);
            } while (query.moveToNext());
            query.close();
        }
        FloatingActionButton fab = v.findViewById(R.id.fab);
        fab.setOnClickListener(this::startNew);
        return v;
    }

    ActivityResultLauncher<Intent> mStartForResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if(result.getResultCode() == Activity.RESULT_OK) {
            Snackbar.make(requireView(), "Предмет успешно добавлен", Snackbar.LENGTH_SHORT).show();
            if (getParentFragment() != null) {
                FragmentManager fm = getParentFragment().getChildFragmentManager();
                Fragment frg = fm.getFragments().get(0);
                fm.beginTransaction().detach(frg).commit();
                fm.beginTransaction().attach(frg).commit();
            }
        }
        if(result.getResultCode() == -2) {
            Snackbar.make(requireView(), "Предмет успешно удалён", Snackbar.LENGTH_SHORT).show();
            if (getParentFragment() != null) {
                FragmentManager fm = getParentFragment().getChildFragmentManager();
                Fragment frg = fm.getFragments().get(0);
                fm.beginTransaction().detach(frg).commit();
                fm.beginTransaction().attach(frg).commit();
            }
        }
    });

    void openSubject(View view) {
        Intent intent = new Intent(this.getContext(), SubjectShowActivity.class);
        intent.putExtra("subjectName", ((TextView) view.findViewById(R.id.subject_name_on_card)).getText());
        mStartForResult.launch(intent);
    }

    void startNew(View view) {
        Intent intent = new Intent(this.getContext(), AddSubject.class);
        intent.putExtra("EDIT", false);
        mStartForResult.launch(intent);
    }

}