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
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.snackbar.Snackbar;
import com.schedule.AddDuty;
import com.schedule.R;

import java.util.Objects;

public class DutiesFragment extends Fragment {

    ActivityResultLauncher<Intent> mStartForResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() == Activity.RESULT_OK)
            reload("Предмет успешно добавлен");
    });

    public DutiesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @SuppressLint({"Recycle", "SetTextI18n"})
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_duties, container, false);
        view.findViewById(R.id.fab).setOnClickListener(this::addDuty);
        SQLiteDatabase db = requireContext().openOrCreateDatabase("app.db", MODE_PRIVATE, null);
        Cursor query;
        try {
            query = db.rawQuery("SELECT * FROM duties", null);
        } catch (android.database.sqlite.SQLiteException exception) {
            view = inflater.inflate(R.layout.fragment_duties_empty, container, false);
            view.findViewById(R.id.fab).setOnClickListener(this::addDuty);
            return view;
        }
        if (!query.moveToFirst()) {
            view = inflater.inflate(R.layout.fragment_duties_empty, container, false);
            view.findViewById(R.id.fab).setOnClickListener(this::addDuty);
        } else {
            LinearLayout duties = view.findViewById(R.id.duties_list);
            do {
                MaterialCardView card = (MaterialCardView) inflater.inflate(R.layout.duty_card, duties, false);
                ((TextView) card.findViewById(R.id.subjectId)).setText(query.getString(1));
                ((TextView) card.findViewById(R.id.dutyId)).setText(String.valueOf(query.getInt(0)));

                ((TextView) card.findViewById(R.id.subject_name_on_card)).setText(query.getString(1));
                ((TextView) card.findViewById(R.id.duty_name_on_card)).setText(query.getString(2));
                ((TextView) card.findViewById(R.id.pass_time_on_card)).setText("Срок сдачи: " + query.getString(3));
                if (!Objects.equals(query.getString(4), ""))
                    ((TextView) card.findViewById(R.id.extra_on_card)).setText("Дополнительные данные: " + query.getString(4));
                AppCompatImageButton button = card.findViewById(R.id.more_button);
                button.setOnClickListener(this::showMenu);
                duties.addView(card);
            } while (query.moveToNext());
        }
        view.findViewById(R.id.fab).setOnClickListener(this::addDuty);
        return view;
    }

    private void showMenu(View view) {
        PopupMenu popupMenu = new PopupMenu(this.getContext(), view);
        popupMenu.inflate(R.menu.subject_popup_menu);

        String dutyId = ((TextView) ((ConstraintLayout) view.getParent()).findViewById(R.id.dutyId)).getText().toString();
        popupMenu.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.edit_subject) {
                Intent intent = new Intent(this.getContext(), AddDuty.class);
                intent.putExtra("EDIT", true);
                intent.putExtra("DUTY_ID", dutyId);
                mStartForResult.launch(intent);
            } else if (item.getItemId() == R.id.delete_subject) {
                SQLiteDatabase db = requireContext().openOrCreateDatabase("app.db", MODE_PRIVATE, null);
                db.execSQL("DELETE FROM duties WHERE id=" + dutyId);
                reload("Долг успешно удалён");
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

    public void addDuty(View view) {
        Intent intent = new Intent(this.getContext(), AddDuty.class);
        intent.putExtra("EDIT", false);
        mStartForResult.launch(intent);
    }

}