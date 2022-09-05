package com.schedule.ui;

import static android.content.Context.MODE_PRIVATE;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.schedule.AddSubject;
import com.schedule.R;

public class SubjectsFragment extends Fragment {

    ActivityResultLauncher<Intent> mStartForResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() == Activity.RESULT_OK)
            reload("Предмет успешно добавлен");
    });

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
        } catch (android.database.sqlite.SQLiteException exception) {
            v = inflater.inflate(R.layout.fragment_subjects_empty, container, false);
            FloatingActionButton fab = v.findViewById(R.id.fab);
            fab.setOnClickListener(this::startNew);
            return v;
        }
        if (!query.moveToFirst()) {
            v = inflater.inflate(R.layout.fragment_subjects_empty, container, false);
        } else {
            v = inflater.inflate(R.layout.fragment_subjects, container, false);
            LinearLayout cl = v.findViewById(R.id.subjects_list);
            do {
                MaterialCardView card = (MaterialCardView) inflater.inflate(R.layout.subject_card, container, false);
                ((TextView) card.findViewById(R.id.subject_name_on_card)).setText(query.getString(1));
                if (query.getString(2) != null)
                    ((TextView) card.findViewById(R.id.lection_text_on_card)).setText("Лектор: " + query.getString(2));
                else
                    card.findViewById(R.id.lection_text_on_card).setVisibility(View.GONE);
                if (query.getString(3) != null)
                    ((TextView) card.findViewById(R.id.practics_text_on_card)).setText("Преподаватель ПЗ: " + query.getString(3));
                else
                    card.findViewById(R.id.practics_text_on_card).setVisibility(View.GONE);
                if (query.getString(4) != null)
                    ((TextView) card.findViewById(R.id.labs_text_on_card)).setText("Преподаватель ЛР: " + query.getString(4));
                else
                    card.findViewById(R.id.labs_text_on_card).setVisibility(View.GONE);
                int id = query.getInt(0);
                ((TextView) card.findViewById(R.id.subject_id_on_card)).setText(String.valueOf(id));
                AppCompatImageButton button = card.findViewById(R.id.more_button);
                button.setOnClickListener(this::showMenu);
                cl.addView(card);
            } while (query.moveToNext());
            query.close();
        }
        FloatingActionButton fab = v.findViewById(R.id.fab);
        fab.setOnClickListener(this::startNew);
        return v;
    }

    private void showMenu(View view) {
        PopupMenu popupMenu = new PopupMenu(this.getContext(), view);
        popupMenu.inflate(R.menu.subject_popup_menu);

        String subjectId = ((TextView) ((ConstraintLayout) view.getParent()).findViewById(R.id.subject_id_on_card)).getText().toString();
        popupMenu.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.edit_subject) {
                Intent intent = new Intent(this.getContext(), AddSubject.class);
                intent.putExtra("EDIT", true);
                intent.putExtra("SUBJECT_ID", subjectId);
                mStartForResult.launch(intent);
            } else if (item.getItemId() == R.id.delete_subject) {
                SQLiteDatabase db = requireContext().openOrCreateDatabase("app.db", MODE_PRIVATE, null);
                db.execSQL("DELETE FROM subjects WHERE id=" + subjectId);
                reload("Предмет успешно удалён");
            }
            return true;
        });

        popupMenu.show();
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

    void startNew(View view) {
        Intent intent = new Intent(this.getContext(), AddSubject.class);
        intent.putExtra("EDIT", false);
        mStartForResult.launch(intent);
    }

}