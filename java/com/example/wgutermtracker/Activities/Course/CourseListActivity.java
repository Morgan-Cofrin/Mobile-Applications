package com.example.wgutermtracker.Activities.Course;

import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.wgutermtracker.Entities.Term;
import com.example.wgutermtracker.R;
import com.example.wgutermtracker.Utils.DBOpenHelper;
import com.example.wgutermtracker.Utils.DBDataManager;
import com.example.wgutermtracker.Utils.DBProvider;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Objects;

public class CourseListActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int COURSE_VIEWER_ACTIVITY_CODE = 11111;
    private static final int COURSE_EDITOR_ACTIVITY_CODE = 22222;

    private long termId;
    private Uri termUri;
    private Term term;
    private MySimpleCursorAdapter cursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(view -> {
            Intent intent = new Intent(CourseListActivity.this, CourseEditorActivity.class);
            intent.putExtra(DBProvider.TERM_CONTENT_TYPE, termUri);
            startActivityForResult(intent, COURSE_EDITOR_ACTIVITY_CODE);
        });

        Intent intent = getIntent();
        termUri = intent.getParcelableExtra(DBProvider.TERM_CONTENT_TYPE);
        loadTermData();
        bindClassList();
        getLoaderManager().initLoader(0, null, this);
    }

    private void loadTermData() {
        if (termUri == null) {
            setResult(RESULT_CANCELED);
            finish();
        }
        else {
            termId = Long.parseLong(Objects.requireNonNull(termUri.getLastPathSegment()));
            term = DBDataManager.getTerm(this, termId);
            setTitle(getString(R.string.courses));
        }
    }

    private void bindClassList() {
        String[] from = {DBOpenHelper.COURSE_NAME, DBOpenHelper.COURSE_START, DBOpenHelper.COURSE_END, DBOpenHelper.COURSE_STATUS};
        int[] to = {R.id.tv_CourseName, R.id.tvCourseStartDate, R.id.tvCourseEndDate, R.id.tvCourseStatus};

        cursorAdapter = new MySimpleCursorAdapter(this, R.layout._course_list_item, null, from, to);
        DBProvider database = new DBProvider();

        ListView list = (ListView) findViewById(android.R.id.list);
        list.setAdapter(cursorAdapter);
        list.setOnItemClickListener((parent, view, position, id) -> {
            Intent intent = new Intent(CourseListActivity.this, CourseViewerActivity.class);
            Uri uri = Uri.parse(DBProvider.COURSES_URI + "/" + id);
            intent.putExtra(DBProvider.COURSE_CONTENT_TYPE, uri);
            startActivityForResult(intent, COURSE_VIEWER_ACTIVITY_CODE);
        });
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this, DBProvider.COURSES_URI, DBOpenHelper.COURSES_COLUMNS, DBOpenHelper.COURSE_TERM_ID + " = " + this.termId, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        cursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        cursorAdapter.swapCursor(null);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        loadTermData();
        restartLoader();
    }

    private void restartLoader() {
        getLoaderManager().restartLoader(0, null, this);
    }

    public static class MySimpleCursorAdapter extends android.widget.SimpleCursorAdapter {

        public MySimpleCursorAdapter(Context context, int layout, Cursor c, String[] from, int[] to) {
            super(context, layout, c, from, to);
        }

        @Override
        public void setViewText(TextView view, String text) {
            if (view.getId() == R.id.tvCourseStatus) {
                String status = "";
                switch (text) {
                    case "PLANNED":
                        status = "Plan to Take";
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
                view.setText("Status: " + status);
            }
            else {
                view.setText(text);
            }
        }
    }
}
