package com.example.wgutermtracker.Activities.Term;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.wgutermtracker.Entities.CourseStatus;
import com.example.wgutermtracker.R;
import com.example.wgutermtracker.Utils.AlarmHandler;
import com.example.wgutermtracker.Utils.DBDataManager;
import com.example.wgutermtracker.Utils.DBOpenHelper;
import com.example.wgutermtracker.Utils.DBProvider;

import java.util.GregorianCalendar;
import java.util.Objects;

public class TermListActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final int TERM_EDITOR_ACTIVITY_CODE = 11111;
    public static final int TERM_VIEWER_ACTIVITY_CODE = 22222;

    private CursorAdapter cursorAdapter;
    private DBProvider database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_term_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        String[] from = {DBOpenHelper.TERM_NAME, DBOpenHelper.TERM_START, DBOpenHelper.TERM_END};
        int[] to = {R.id.tv_Term, R.id.tv_TermStartDate, R.id.tv_TermEndDate};

        cursorAdapter = new SimpleCursorAdapter(this, R.layout._term_list_item, null, from, to, 0);
        database = new DBProvider();

        ListView list = (ListView) findViewById(android.R.id.list);
        list.setAdapter(cursorAdapter);

        list.setOnItemClickListener((parent, view, position, id) -> {
            Intent intent = new Intent(TermListActivity.this, TermViewerActivity.class);
            Uri uri = Uri.parse(DBProvider.TERMS_URI + "/" + id);
            intent.putExtra(DBProvider.TERM_CONTENT_TYPE, uri);
            startActivityForResult(intent, TERM_VIEWER_ACTIVITY_CODE);
        });
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            restartLoader();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_create_sample:
                return createSampleData();
            case R.id.action_delete_all_terms:
                return deleteAllTerms();
            case R.id.action_create_test_alarm:
                return createTestAlarm();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private boolean createSampleData() {
        Uri term1Uri = DBDataManager.insertTerm(this, "Spring 2018", "2018-01-01", "2018-06-30", 1);
        Uri term2Uri = DBDataManager.insertTerm(this, "Fall 2018", "2018-07-01", "2018-12-31", 0);
        Uri term3Uri = DBDataManager.insertTerm(this, "Spring 2019", "2019-01-01", "2019-06-30", 0);
        Uri term4Uri = DBDataManager.insertTerm(this, "Fall 2019", "2019-07-01", "2019-12-31", 0);
        Uri term5Uri = DBDataManager.insertTerm(this, "Spring 2020", "2020-01-01", "2020-06-30", 0);
        Uri term6Uri = DBDataManager.insertTerm(this, "Fall 2020", "2020-07-01", "2020-12-31", 0);

        Uri course1Uri = DBDataManager.insertCourse(this, Long.parseLong(term1Uri.getLastPathSegment()),
                "C196: Mobile Application Development", "2018-01-01", "2018-02-01",
                "Pubali Banerjee", "(801) 924-4710", "pubali.banerjee@wgu.edu",
                CourseStatus.IN_PROGRESS);

        DBDataManager.insertCourse(this, Long.parseLong(term1Uri.getLastPathSegment()),
                "C193: Client-Server Application Development", "2018-02-01", "2018-03-01",
                "Course Mentor Group", " ", "cmprogramming@wgu.edu",
                CourseStatus.PLANNED);

        DBDataManager.insertCourse(this, Long.parseLong(term1Uri.getLastPathSegment()),
                "C195: Software II - Advanced Java Concepts", "2018-03-01", "2018-06-30",
                "Course Mentor Group", "", "cmprogramming@wgu.edu",
                CourseStatus.PLANNED);

        DBDataManager.insertCourseNote(this, Long.parseLong(course1Uri.getLastPathSegment()),
                "This is a short test note");

        DBDataManager.insertCourseNote(this, Long.parseLong(course1Uri.getLastPathSegment()),
                getString(R.string.long_test_note));

        Uri ass1Uri = DBDataManager.insertAssessment(this, Long.parseLong(course1Uri.getLastPathSegment()), "CLP1",
                "Mobile Application Development", "Objective Assessment",
                "As a competent mobile application developer, your understanding of mobile application structure " +
                        "and design will help you to develop applications to meet customer requirements. The following " +
                        "project to develop a student scheduler/student progress tracking application, will help you to " +
                        "apply these skills in a familiar, real-world scenario. This task will allow you to demonstrate " +
                        "your ability to apply the skills learned in the course.\n \nYou will develop a multiple-screen " +
                        "mobile application for WGU students to track their terms, courses associated with each term, and " +
                        "assessment(s) associated with each course. The application will allow students to enter, edit, " +
                        "and delete term, course, and assessment data. It should provide summary and detailed views of " +
                        "courses for each term and provide alerts for upcoming performance and objective assessments. " +
                        "This application will use a SQLite database.", "2018-10-01 2:30 PM");

        Uri ass2Uri = DBDataManager.insertAssessment(this, Long.parseLong(course1Uri.getLastPathSegment()), "ABC3",
                "Second Assessment, although this one has a name that won't fit on the grid", "Performance Assessment",
                "Assessment Description",  "2018-10-01 10:30 AM");

        restartLoader();
        return true;
    }

    private boolean deleteAllTerms() {
        DialogInterface.OnClickListener dialogClickListener = (dialog, button) -> {
            if (button == DialogInterface.BUTTON_POSITIVE) {
                getContentResolver().delete(DBProvider.TERMS_URI, null, null);
                getContentResolver().delete(DBProvider.COURSES_URI, null, null);
                getContentResolver().delete(DBProvider.COURSE_NOTES_URI, null, null);
                getContentResolver().delete(DBProvider.ASSESSMENTS_URI, null, null);
                restartLoader();
                Toast.makeText(TermListActivity.this, getString(R.string.all_terms_deleted), Toast.LENGTH_SHORT).show();
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.confirm_delete_all_terms))
                .setPositiveButton(getString(android.R.string.yes), dialogClickListener)
                .setNegativeButton(getString(android.R.string.no), dialogClickListener)
                .show();
        return true;
    }

    private boolean createTestAlarm() {
        // Sets alarm for 5 seconds in the future
        long time = new GregorianCalendar().getTimeInMillis() + 5000;

        Intent intent = new Intent(this, AlarmHandler.class);
        intent.putExtra("title", "Test Alarm");
        intent.putExtra("text", "This is a test alarm.");

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        Objects.requireNonNull(alarmManager).set(AlarmManager.RTC_WAKEUP, time,
                PendingIntent.getBroadcast(this, 1, intent, PendingIntent.FLAG_ONE_SHOT));

        Toast.makeText(this, getString(R.string.test_alarm), Toast.LENGTH_SHORT).show();
        return true;
    }

    public void openNewTermEditor(View view) {
        Intent intent = new Intent(this, TermEditorActivity.class);
        startActivityForResult(intent, TERM_EDITOR_ACTIVITY_CODE);
    }

    private void restartLoader() {
        getLoaderManager().restartLoader(0, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this, DBProvider.TERMS_URI, null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        cursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        cursorAdapter.swapCursor(null);
    }
}
