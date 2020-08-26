package com.example.wgutermtracker.Activities.Assessment;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.widget.CursorAdapter;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.widget.SimpleCursorAdapter;

import com.example.wgutermtracker.R;
import com.example.wgutermtracker.Utils.DBOpenHelper;
import com.example.wgutermtracker.Utils.DBProvider;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Objects;

public class AssessmentListActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int ASSESSMENT_VIEWER_ACTIVITY_CODE = 11111;
    private static final int ASSESSMENT_EDITOR_ACTIVITY_CODE = 22222;

    private CursorAdapter cursorAdapter;
    private long courseId;
    private Uri courseUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assessment_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        courseUri = getIntent().getParcelableExtra(DBProvider.COURSE_CONTENT_TYPE);
        courseId = Long.parseLong(courseUri.getLastPathSegment());

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(v -> {
            Intent intent = new Intent(AssessmentListActivity.this, AssessmentEditorActivity.class);
            intent.putExtra(DBProvider.COURSE_CONTENT_TYPE, courseUri);
            startActivityForResult(intent, ASSESSMENT_EDITOR_ACTIVITY_CODE);
        });

        bindAssessmentList();
        getLoaderManager().initLoader(0, null, this);
    }

    protected void bindAssessmentList() {
        String[] from = {DBOpenHelper.ASSESSMENT_CODE, DBOpenHelper.ASSESSMENT_NAME, DBOpenHelper.ASSESSMENT_TYPE, DBOpenHelper.ASSESSMENT_DATETIME};
        int[] to = {R.id.tv_AssessmentCode, R.id.tv_AssessmentName, R.id.tv_AssessmentType, R.id.tv_AssessmentDatetime};

        cursorAdapter = new SimpleCursorAdapter(this, R.layout._assessment_list_item, null, from, to, 0);
        DBProvider database = new DBProvider();

        ListView list = (ListView) findViewById(R.id.assessmentListView);
        list.setAdapter(cursorAdapter);

        list.setOnItemClickListener((parent, view, position, id) -> {
            Intent intent = new Intent(AssessmentListActivity.this, AssessmentViewerActivity.class);
            Uri uri = Uri.parse(DBProvider.ASSESSMENTS_URI + "/" + id);
            intent.putExtra(DBProvider.ASSESSMENT_CONTENT_TYPE, uri);
            startActivityForResult(intent, ASSESSMENT_VIEWER_ACTIVITY_CODE);
        });
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this, DBProvider.ASSESSMENTS_URI, DBOpenHelper.ASSESSMENTS_COLUMNS,
                DBOpenHelper.ASSESSMENT_COURSE_ID + " = " + this.courseId, null, null);
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
        restartLoader();
    }

    private void restartLoader() {
        getLoaderManager().restartLoader(0, null, this);
    }

}
