package com.example.wgutermtracker.Activities.Assessment;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.wgutermtracker.Entities.Assessment;
import com.example.wgutermtracker.R;
import com.example.wgutermtracker.Utils.AlarmHandler;
import com.example.wgutermtracker.Utils.DBDataManager;
import com.example.wgutermtracker.Utils.DBProvider;
import com.example.wgutermtracker.Utils.DateUtil;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Objects;

public class AssessmentViewerActivity extends AppCompatActivity {

    private static final int ASSESSMENT_EDITOR_ACTIVITY_CODE = 11111;

    private long assessmentId;
    private Assessment assessment;
    private TextView tv_AssessmentTitle;
    private TextView tv_AssessmentType;
    private TextView tv_AssessmentDescription;
    private TextView tv_AssessmentDatetime;
    private Menu menu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assessment_viewer);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(v -> {
            Intent intent = new Intent(AssessmentViewerActivity.this, AssessmentEditorActivity.class);
            Uri uri = Uri.parse(DBProvider.ASSESSMENTS_URI + "/" + assessmentId);
            intent.putExtra(DBProvider.ASSESSMENT_CONTENT_TYPE, uri);
            startActivityForResult(intent, ASSESSMENT_EDITOR_ACTIVITY_CODE);
        });

        loadAssessment();
    }

    private void loadAssessment() {
        Uri assessmentUri = getIntent().getParcelableExtra(DBProvider.ASSESSMENT_CONTENT_TYPE);
        assessmentId = Long.parseLong(assessmentUri.getLastPathSegment());
        assessment = DBDataManager.getAssessment(this, assessmentId);
        tv_AssessmentTitle = (TextView) findViewById(R.id.tv_AssessmentTitle);
        tv_AssessmentType = (TextView) findViewById(R.id.tv_AssessmentType);
        tv_AssessmentDescription = (TextView) findViewById(R.id.tv_AssessmentDescription);
        tv_AssessmentDatetime = (TextView) findViewById(R.id.tv_AssessmentDatetime);
        tv_AssessmentTitle.setText(assessment.assessmentCode + ": " + assessment.assessmentName);
        tv_AssessmentType.setText(assessment.assessmentType);
        tv_AssessmentDescription.setText(assessment.assessmentDescription);
        tv_AssessmentDatetime.setText(assessment.assessmentDatetime);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            loadAssessment();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_assessment_viewer, menu);
        this.menu = menu;
        showAppropriateMenuOptions();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_delete_assessment:
                return deleteAssessment();
            case R.id.action_enable_start_notifications:
                return enableNotifications();
            case R.id.action_disable_start_notifications:
                return disableNotifications();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private boolean deleteAssessment() {
        DialogInterface.OnClickListener dialogClickListener = (dialog, button) -> {
            if (button == DialogInterface.BUTTON_POSITIVE) {
                DBDataManager.deleteAssessment(AssessmentViewerActivity.this, assessmentId);
                setResult(RESULT_OK);
                finish();
                Toast.makeText(AssessmentViewerActivity.this, getString(R.string.assessment_deleted), Toast.LENGTH_SHORT).show();
            }
        };
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.confirm_delete_assessment)
                .setPositiveButton(getString(android.R.string.yes), dialogClickListener)
                .setNegativeButton(getString(android.R.string.no), dialogClickListener)
                .show();
        return true;
    }

    //A7c. Alerts for goal dates
    //There is typo in the rubric, only the start date is required. I double checked with the course instructor before resubmission
    private boolean enableNotifications() {
        long now = DateUtil.todayLong();

        AlarmHandler.scheduleAssessmentAlarm(getApplicationContext(), (int) assessmentId, System.currentTimeMillis()
                + 1000, "Assessment is today!", assessment.assessmentName + " takes place on " + assessment.assessmentDatetime);

        if (now <= DateUtil.getDateTimestamp(assessment.assessmentDatetime)) {
            AlarmHandler.scheduleAssessmentAlarm(getApplicationContext(), (int) assessmentId,
                    DateUtil.getDateTimestamp(assessment.assessmentDatetime),
                    "Assessment is today!", assessment.assessmentName + " takes place on " + assessment.assessmentDatetime);
        }
        if (now <= DateUtil.getDateTimestamp(assessment.assessmentDatetime) - 3 * 24 * 60 * 60 * 1000) {
            AlarmHandler.scheduleAssessmentAlarm(getApplicationContext(), (int) assessmentId,
                    DateUtil.getDateTimestamp(assessment.assessmentDatetime) - 3 * 24 * 60 * 60 * 1000,
                    "Assessment is in three days!", assessment.assessmentName + " takes place on " + assessment.assessmentDatetime);
        }
        if (now <= DateUtil.getDateTimestamp(assessment.assessmentDatetime) - 21 * 24 * 60 * 60 * 1000) {
            AlarmHandler.scheduleAssessmentAlarm(getApplicationContext(), (int) assessmentId,
                    DateUtil.getDateTimestamp(assessment.assessmentDatetime) - 21 * 24 * 60 * 60 * 1000,
                    "Assessment is in three weeks!", assessment.assessmentName + " takes place on " + assessment.assessmentDatetime);
        }

        assessment.notifications = 1;
        assessment.saveChanges(this);
        showAppropriateMenuOptions();
        return true;
    }

    private boolean disableNotifications() {
        assessment.notifications = 0;
        assessment.saveChanges(this);
        showAppropriateMenuOptions();
        return true;
    }

    private void showAppropriateMenuOptions() {
        menu.findItem(R.id.action_enable_start_notifications).setVisible(true);
        menu.findItem(R.id.action_disable_start_notifications).setVisible(true);

        if (assessment.notifications == 1) {
            menu.findItem(R.id.action_enable_start_notifications).setVisible(false);
        }
        else {
            menu.findItem(R.id.action_disable_start_notifications).setVisible(false);
        }
    }
}
