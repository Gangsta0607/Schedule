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
import com.schedule.AddDuty;
import com.schedule.DutyShowActivity;
import com.schedule.R;

import java.util.Objects;

public class DutiesFragment extends Fragment {

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
        }
        catch (android.database.sqlite.SQLiteException exception) {
            view = inflater.inflate(R.layout.fragment_duties_empty, container, false);
            ((FloatingActionButton) view.findViewById(R.id.fab)).setOnClickListener(this::addDuty);
            return view;
        }
        if (!query.moveToFirst()) {
            view = inflater.inflate(R.layout.fragment_duties_empty, container, false);
            ((FloatingActionButton) view.findViewById(R.id.fab)).setOnClickListener(this::addDuty);
        }
        else {
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
                card.setOnClickListener(this::openDuty);
                duties.addView(card);
            } while (query.moveToNext());
        }
        ((FloatingActionButton) view.findViewById(R.id.fab)).setOnClickListener(this::addDuty);
        return view;
    }

    ActivityResultLauncher<Intent> mStartForResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if(result.getResultCode() == Activity.RESULT_OK) {
            Snackbar.make(requireView(), "Долг успешно добавлен", Snackbar.LENGTH_SHORT).show();
            if (getParentFragment() != null) {
                FragmentManager fm = getParentFragment().getChildFragmentManager();
                Fragment frg = fm.getFragments().get(0);
                fm.beginTransaction().detach(frg).commit();
                fm.beginTransaction().attach(frg).commit();
            }
        }
        if(result.getResultCode() == -2) {
            Snackbar.make(requireView(), "Долг успешно удалён", Snackbar.LENGTH_SHORT).show();
            if (getParentFragment() != null) {
                FragmentManager fm = getParentFragment().getChildFragmentManager();
                Fragment frg = fm.getFragments().get(0);
                fm.beginTransaction().detach(frg).commit();
                fm.beginTransaction().attach(frg).commit();
            }
        }
    });

    public void addDuty(View view) {
        Intent intent = new Intent(this.getContext(), AddDuty.class);
        intent.putExtra("EDIT", false);
        mStartForResult.launch(intent);
    }

    void openDuty(View view) {
        Intent intent = new Intent(this.getContext(), DutyShowActivity.class);
        intent.putExtra("dutyId", ((TextView) view.findViewById(R.id.dutyId)).getText());
        intent.putExtra("subjectName", ((TextView) view.findViewById(R.id.subjectId)).getText());
        mStartForResult.launch(intent);
    }

}