package com.example.wgutermtracker.Activities.Course;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.wgutermtracker.Activities.Assessment.AssessmentListActivity;
import com.example.wgutermtracker.Activities.CourseNote.CourseNoteListActivity;
import com.example.wgutermtracker.Entities.Course;
import com.example.wgutermtracker.Entities.CourseStatus;
import com.example.wgutermtracker.R;
import com.example.wgutermtracker.Utils.AlarmHandler;
import com.example.wgutermtracker.Utils.DBDataManager;
import com.example.wgutermtracker.Utils.DBProvider;
import com.example.wgutermtracker.Utils.DateUtil;

import java.util.Objects;

public class CourseViewerActivity extends AppCompatActivity {

    private static final int COURSE_NOTE_LIST_ACTIVITY_CODE = 11111;
    private static final int ASSESSMENT_LIST_ACTIVITY_CODE = 22222;
    private static final int COURSE_EDITOR_ACTIVITY_CODE = 33333;

    private Menu menu;
    private Uri courseUri;
    private long courseId;
    private Course course;

    private TextView tv_CourseName;
    private TextView tv_StartDate;
    private TextView tv_EndDate;
    private TextView tv_Status;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_viewer);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        courseUri = intent.getParcelableExtra(DBProvider.COURSE_CONTENT_TYPE);
        courseId = Long.parseLong(courseUri.getLastPathSegment());
        course = DBDataManager.getCourse(this, courseId);

        setStatusLabel();
        findElements();
    }

    private void setStatusLabel() {
        tv_Status = (TextView) findViewById(R.id.tv_Status);
        String status = "";
        switch (course.courseStatus.toString()) {
            case "PLANNED":
                status = "Planned to Take";
                break;
            case "IN_PROGRESS":
                status = "In Progress";
                break;
            case "COMPLETED":
                status = "Completed";
                break;
            case "DROPPED":
                status = "Dropped";
                break;
        }
        tv_Status.setText("Status: " + status);
    }

    private void findElements() {
        tv_CourseName = (TextView) findViewById(R.id.tv_CourseName);
        tv_CourseName.setText(course.courseName);
        tv_StartDate = (TextView) findViewById(R.id.tv_CourseStart);
        tv_StartDate.setText(course.courseStart);
        tv_EndDate = (TextView) findViewById(R.id.tv_CourseEnd);
        tv_EndDate.setText(course.courseEnd);
    }

    private void updateElements() {
        course = DBDataManager.getCourse(this, courseId);
        tv_CourseName.setText(course.courseName);
        tv_StartDate.setText(course.courseStart);
        tv_EndDate.setText(course.courseEnd);
    }

    public void openClassNotesList(View view) {
        Intent intent = new Intent(CourseViewerActivity.this, CourseNoteListActivity.class);
        Uri uri = Uri.parse(DBProvider.COURSES_URI + "/" + courseId);
        intent.putExtra(DBProvider.COURSE_CONTENT_TYPE, uri);
        startActivityForResult(intent, COURSE_NOTE_LIST_ACTIVITY_CODE);
    }

    public void openAssessments(View view) {
        Intent intent = new Intent(CourseViewerActivity.this, AssessmentListActivity.class);
        Uri uri = Uri.parse(DBProvider.COURSES_URI + "/" + courseId);
        intent.putExtra(DBProvider.COURSE_CONTENT_TYPE, uri);
        startActivityForResult(intent, ASSESSMENT_LIST_ACTIVITY_CODE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_course_viewer, menu);
        this.menu = menu;
        showAppropriateMenuOptions();
        return true;
    }

    private void showAppropriateMenuOptions() {
        SharedPreferences sp = getSharedPreferences(AlarmHandler.courseAlarmFile, Context.MODE_PRIVATE);
        menu.findItem(R.id.action_enable_start_notifications).setVisible(true);
        menu.findItem(R.id.action_disable_start_notifications).setVisible(true);
        menu.findItem(R.id.action_enable_end_notifications).setVisible(true);
        menu.findItem(R.id.action_disable_end_notifications).setVisible(true);

        if (course.startNotifications == 1) {
            menu.findItem(R.id.action_enable_start_notifications).setVisible(false);
        }
        else {
            menu.findItem(R.id.action_disable_start_notifications).setVisible(false);
        }

        if (course.endNotifications == 1) {
            menu.findItem(R.id.action_enable_end_notifications).setVisible(false);
        }
        else {
            menu.findItem(R.id.action_disable_end_notifications).setVisible(false);
        }

        if (course.courseStatus == null) {
            course.courseStatus = CourseStatus.PLANNED;
            course.saveChanges(this);
        }

        switch (course.courseStatus.toString()) {
            case "PLANNED":
                menu.findItem(R.id.action_drop_course).setVisible(false);
                menu.findItem(R.id.action_start_course).setVisible(true);
                menu.findItem(R.id.action_mark_course_completed).setVisible(false);
                break;
            case "IN_PROGRESS":
                menu.findItem(R.id.action_drop_course).setVisible(true);
                menu.findItem(R.id.action_start_course).setVisible(false);
                menu.findItem(R.id.action_mark_course_completed).setVisible(true);
                break;
            case "COMPLETED":
                menu.findItem(R.id.action_drop_course).setVisible(false);
                menu.findItem(R.id.action_start_course).setVisible(false);
                menu.findItem(R.id.action_mark_course_completed).setVisible(false);
                break;
            case "DROPPED":
                menu.findItem(R.id.action_drop_course).setVisible(false);
                menu.findItem(R.id.action_start_course).setVisible(false);
                menu.findItem(R.id.action_mark_course_completed).setVisible(false);
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_edit_course:
                return editCourse();
            case R.id.action_delete_course:
                return deleteCourse();
            case R.id.action_enable_start_notifications:
                return enableStartNotifications();
            case R.id.action_disable_start_notifications:
                return disableStartNotifications();
            case R.id.action_enable_end_notifications:
                return enableEndNotifications();
            case R.id.action_disable_end_notifications:
                return disableEndNotifications();
            case R.id.action_drop_course:
                return dropCourse();
            case R.id.action_start_course:
                return startCourse();
            case R.id.action_mark_course_completed:
                return markCourseCompleted();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private boolean editCourse() {
        Intent intent = new Intent(this, CourseEditorActivity.class);
        Uri uri = Uri.parse(DBProvider.COURSES_URI + "/" + course.courseId);
        intent.putExtra(DBProvider.COURSE_CONTENT_TYPE, uri);
        startActivityForResult(intent, COURSE_EDITOR_ACTIVITY_CODE);
        return true;
    }

    private boolean deleteCourse() {
        DialogInterface.OnClickListener dialogClickListener = (dialog, button) -> {
            if (button == DialogInterface.BUTTON_POSITIVE) {
                DBDataManager.deleteCourse(CourseViewerActivity.this, courseId);
                setResult(RESULT_OK);
                finish();
                Toast.makeText(CourseViewerActivity.this, getString(R.string.course_deleted), Toast.LENGTH_SHORT).show();
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.confirm_delete_course)
                .setPositiveButton(getString(android.R.string.yes), dialogClickListener)
                .setNegativeButton(getString(android.R.string.no), dialogClickListener)
                .show();
        return true;
    }

    //A6f. Alerts for Courses (start/end)
    //EDIT: Split the enableNotifications button into two separate buttons, one for the start date and one for the end date.
    private boolean enableStartNotifications() {
        long now = DateUtil.todayLong();

        if (now <= DateUtil.getDateTimestamp(course.courseStart)) {
            AlarmHandler.scheduleCourseAlarm(getApplicationContext(), courseId, DateUtil.getDateTimestamp(course.courseStart),
                    "Course starts today!", course.courseName + " begins on " + course.courseStart);
        }
        if (now <= DateUtil.getDateTimestamp(course.courseStart) - 3 * 24 * 60 * 60 * 1000) {
            AlarmHandler.scheduleCourseAlarm(getApplicationContext(), courseId, DateUtil.getDateTimestamp(course.courseStart),
                    "Course starts in three days!", course.courseName + " begins on " + course.courseStart);
        }
        if (now <= DateUtil.getDateTimestamp(course.courseStart) - 21 * 24 * 60 * 60 * 1000) {
            AlarmHandler.scheduleCourseAlarm(getApplicationContext(), courseId, DateUtil.getDateTimestamp(course.courseStart),
                    "Course starts in three weeks!", course.courseName + " begins on " + course.courseStart);
        }

        course.startNotifications = 1;
        course.saveChanges(this);
        showAppropriateMenuOptions();
        return true;
    }

    private boolean enableEndNotifications() {
        long now = DateUtil.todayLong();

        if (now <= DateUtil.getDateTimestamp(course.courseEnd)) {
            AlarmHandler.scheduleCourseAlarm(getApplicationContext(), courseId, DateUtil.getDateTimestamp(course.courseEnd),
                    "Course ends today!", course.courseName + " ends on " + course.courseStart);
        }
        if (now <= DateUtil.getDateTimestamp(course.courseEnd) - 3 * 24 * 60 * 60 * 1000) {
            AlarmHandler.scheduleCourseAlarm(getApplicationContext(), courseId, DateUtil.getDateTimestamp(course.courseEnd),
                    "Course ends in three days!", course.courseName + " ends on " + course.courseStart);
        }
        if (now <= DateUtil.getDateTimestamp(course.courseEnd) - 21 * 24 * 60 * 60 * 1000) {
            AlarmHandler.scheduleCourseAlarm(getApplicationContext(), courseId, DateUtil.getDateTimestamp(course.courseEnd),
                    "Course ends in three weeks!", course.courseName + " ends on " + course.courseStart);
        }

        course.endNotifications = 1;
        course.saveChanges(this);
        showAppropriateMenuOptions();
        return true;
    }

    private boolean disableStartNotifications() {
        course.startNotifications = 0;
        course.saveChanges(this);
        showAppropriateMenuOptions();
        return true;
    }

    private boolean disableEndNotifications() {
        course.endNotifications = 0;
        course.saveChanges(this);
        showAppropriateMenuOptions();
        return true;
    }

    private boolean dropCourse() {
        course.courseStatus = CourseStatus.DROPPED;
        course.saveChanges(this);
        setStatusLabel();
        showAppropriateMenuOptions();
        return true;
    }

    private boolean startCourse() {
        course.courseStatus = CourseStatus.IN_PROGRESS;
        course.saveChanges(this);
        setStatusLabel();
        showAppropriateMenuOptions();
        return true;
    }

    private boolean markCourseCompleted() {
        course.courseStatus = CourseStatus.COMPLETED;
        course.saveChanges(this);
        setStatusLabel();
        showAppropriateMenuOptions();
        return true;
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            updateElements();
        }
    }
}
