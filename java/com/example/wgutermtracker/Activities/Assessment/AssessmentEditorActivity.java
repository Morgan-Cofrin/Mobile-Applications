package com.example.wgutermtracker.Activities.Assessment;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.wgutermtracker.Entities.Assessment;
import com.example.wgutermtracker.R;
import com.example.wgutermtracker.Utils.DBDataManager;
import com.example.wgutermtracker.Utils.DBProvider;
import com.example.wgutermtracker.Utils.DateUtil;

import java.util.Calendar;
import java.util.Objects;
import java.util.TimeZone;

public class AssessmentEditorActivity extends AppCompatActivity implements View.OnClickListener {

    private Assessment assessment;
    private long courseId;
    private EditText et_AssessmentCode;
    private EditText et_AssessmentName;
    private EditText et_AssessmentDescription;
    private EditText et_AssessmentDatetime;
    private RadioButton radio_Objective;
    private RadioButton radio_Performance;
    private DatePickerDialog assessmentDateDialog;
    private TimePickerDialog assessmentTimeDialog;
    private String action;

    //A7a. Assessments for each course (obj/perf)
    //EDIT: Added radio buttons for selecting either a performance assessment and an objective assessment
    //A7b, Assessment Information, (editing)
    //EDIT: All editing functionality is present.
    // Navigation to the assessment editing is found by clicking the floating action button on the assessment details screen
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assessment_editor);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        et_AssessmentCode = (EditText) findViewById(R.id.et_AssessmentCode);
        et_AssessmentName = (EditText) findViewById(R.id.et_AssessmentName);
        et_AssessmentDescription = (EditText) findViewById(R.id.et_AssessmentDescription);
        et_AssessmentDatetime = (EditText) findViewById(R.id.et_AssessmentDatetime);
        radio_Objective = (RadioButton) findViewById(R.id.radio_Objective);
        radio_Performance = (RadioButton) findViewById(R.id.radio_Performance);

        Uri assessmentUri = getIntent().getParcelableExtra(DBProvider.ASSESSMENT_CONTENT_TYPE);
        if (assessmentUri == null) {
            setTitle(getString(R.string.new_assessment));
            action = Intent.ACTION_INSERT;
            Uri courseUri = getIntent().getParcelableExtra(DBProvider.COURSE_CONTENT_TYPE);
            courseId = Long.parseLong(courseUri.getLastPathSegment());
            assessment = new Assessment();
        }
        else {
            setTitle(getString(R.string.edit_assessment));
            action = Intent.ACTION_EDIT;
            long assessmentId = Long.parseLong(assessmentUri.getLastPathSegment());
            assessment = DBDataManager.getAssessment(this, assessmentId);
            courseId = assessment.courseId;
            fillAssessmentForm();
        }
        setupDateAndTimePickers();
    }

    private void fillAssessmentForm() {
        if (assessment != null) {
            et_AssessmentCode.setText(assessment.assessmentCode);
            et_AssessmentName.setText(assessment.assessmentName);
            et_AssessmentDescription.setText(assessment.assessmentDescription);
            et_AssessmentDatetime.setText(assessment.assessmentDatetime);
            if (assessment.assessmentType.equals("Objective Assessment")) {
                radio_Objective.toggle();
            }
            if (assessment.assessmentType.equals("Performance Assessment")) {
                radio_Performance.toggle();
            }
        }
    }

    private void setupDateAndTimePickers() {
        et_AssessmentDatetime.setOnClickListener(this);

        Calendar calendar = Calendar.getInstance();
        assessmentDateDialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            Calendar calendar2 = Calendar.getInstance();
            Calendar newDate = Calendar.getInstance();
            newDate.set(year, month, dayOfMonth);
            et_AssessmentDatetime.setText(DateUtil.dateFormat.format(newDate.getTime()));
            assessmentTimeDialog = new TimePickerDialog(AssessmentEditorActivity.this, (view1, hourOfDay, minute) -> {
                String AM_PM;
                if (hourOfDay < 12) {
                    AM_PM = "AM";
                }
                else {
                    AM_PM = "PM";
                }
                if (hourOfDay > 12) {
                    hourOfDay = hourOfDay - 12;
                }
                if (hourOfDay == 0) {
                    hourOfDay = 12;
                }
                String minuteString = Integer.toString(minute);
                if (minute < 10) {
                    minuteString = "0" + minuteString;
                }
                String datetime = et_AssessmentDatetime.getText().toString() + " " + hourOfDay + ":" + minuteString
                        + " " + AM_PM + " " + TimeZone.getDefault().getDisplayName(false, TimeZone.SHORT);
                et_AssessmentDatetime.setText(datetime);
            }, calendar2.get(Calendar.HOUR_OF_DAY), calendar2.get(Calendar.MINUTE), false);
            assessmentTimeDialog.show();
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

        et_AssessmentDatetime.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                assessmentDateDialog.show();
            }
        });
    }

    public void saveAssessmentChanges(View view) {
        getValuesFromFields();
        switch (action) {
            case Intent.ACTION_INSERT:
                DBDataManager.insertAssessment(this, courseId, assessment.assessmentCode, assessment.assessmentName,
                        assessment.assessmentType, assessment.assessmentDescription, assessment.assessmentDatetime);
                setResult(RESULT_OK);
                finish();
                break;
            case Intent.ACTION_EDIT:
                assessment.saveChanges(this);
                setResult(RESULT_OK);
                finish();
                break;
            default:
                throw new UnsupportedOperationException();
        }
    }

    private void getValuesFromFields() {
        assessment.assessmentCode = et_AssessmentCode.getText().toString().trim();
        assessment.assessmentName = et_AssessmentName.getText().toString().trim();
        assessment.assessmentDescription = et_AssessmentDescription.getText().toString().trim();
        assessment.assessmentDatetime = et_AssessmentDatetime.getText().toString().trim();
        if (radio_Objective.isChecked()) {
            assessment.assessmentType = "Objective Assessment";
        }
        if (radio_Performance.isChecked()) {
            assessment.assessmentType = "Performance Assessment";
        }
    }

    @Override
    public void onClick(View view) {
        if (view == et_AssessmentDatetime) {
            assessmentDateDialog.show();
        }
    }

}
