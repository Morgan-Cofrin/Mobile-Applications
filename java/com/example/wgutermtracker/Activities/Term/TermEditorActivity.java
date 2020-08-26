package com.example.wgutermtracker.Activities.Term;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.wgutermtracker.Entities.Term;
import com.example.wgutermtracker.R;
import com.example.wgutermtracker.Utils.DBDataManager;
import com.example.wgutermtracker.Utils.DBProvider;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;

public class TermEditorActivity extends AppCompatActivity implements View.OnClickListener {

    private String action;
    private Term term;

    private EditText termNameField;
    private EditText termStartDateField;
    private EditText termEndDateField;

    private DatePickerDialog termStartDateDialog;
    private DatePickerDialog termEndDateDialog;
    private SimpleDateFormat dateFormat;

    private DBProvider database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_term_editor);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        database = new DBProvider();

        termNameField = (EditText) findViewById(R.id.et_termName);
        termStartDateField = (EditText) findViewById(R.id.et_termStartDate);
        termStartDateField.setInputType(InputType.TYPE_NULL);
        termEndDateField = (EditText) findViewById(R.id.et_termEndDate);
        termEndDateField.setInputType(InputType.TYPE_NULL);

        dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

        Intent intent = getIntent();
        Uri uri = intent.getParcelableExtra(DBProvider.TERM_CONTENT_TYPE);

        if (uri == null) {
            action = intent.ACTION_INSERT;
            setTitle(getString(R.string.add_new_term));
        }
        else {
            action = Intent.ACTION_EDIT;
            setTitle(getString(R.string.edit_term_title));
            long termId = Long.parseLong(uri.getLastPathSegment());
            term = DBDataManager.getTerm(this, termId);
            fillTermForm(term);
        }
        setupDatePickers();
    }

    private void fillTermForm(Term term) {
        termNameField.setText(term.termName);
        termStartDateField.setText(term.termStart);
        termEndDateField.setText(term.termEnd);
    }

    private void getTermFromForm() {
        term.termName = termNameField.getText().toString().trim();
        term.termStart = termStartDateField.getText().toString().trim();
        term.termEnd = termEndDateField.getText().toString().trim();
    }

    private void setupDatePickers() {
        termStartDateField.setOnClickListener(this);
        termEndDateField.setOnClickListener(this);

        Calendar calendar = Calendar.getInstance();
        termStartDateDialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            Calendar newDate = Calendar.getInstance();
            newDate.set(year, month, dayOfMonth);
            termStartDateField.setText(dateFormat.format(newDate.getTime()));
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

        termEndDateDialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            Calendar newDate = Calendar.getInstance();
            newDate.set(year, month, dayOfMonth);
            termEndDateField.setText(dateFormat.format(newDate.getTime()));
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

        termStartDateField.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                termStartDateDialog.show();
            }
        });
    }

    public void saveTermChanges(View view) {
        if (action.equals(Intent.ACTION_INSERT)) {
            term = new Term();
            getTermFromForm();

            DBDataManager.insertTerm(this, term.termName, term.termStart, term.termEnd, term.active);
            Toast.makeText(this, getString(R.string.term_saved), Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
        }
        else if (action.equals(Intent.ACTION_EDIT)) {
            getTermFromForm();
            term.saveChanges(this);
            Toast.makeText(this, getString(R.string.term_updated), Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
        }
        finish();
    }

    @Override
    public void onClick(View view) {
        if (view == termStartDateField) {
            termStartDateDialog.show();
        }
        if (view == termEndDateField) {
            termEndDateDialog.show();
        }
    }
}
