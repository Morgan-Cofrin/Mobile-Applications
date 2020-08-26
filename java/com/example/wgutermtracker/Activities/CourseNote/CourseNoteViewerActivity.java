package com.example.wgutermtracker.Activities.CourseNote;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.ShareActionProvider;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.MenuItemCompat;

import com.example.wgutermtracker.Entities.Course;
import com.example.wgutermtracker.Entities.CourseNote;
import com.example.wgutermtracker.R;
import com.example.wgutermtracker.Utils.DBDataManager;
import com.example.wgutermtracker.Utils.DBProvider;

import java.util.Objects;

public class CourseNoteViewerActivity extends AppCompatActivity {

    private static final int COURSE_NOTE_EDITOR_ACTIVITY_CODE = 11111;

    private long courseNoteId;
    private Uri courseNoteUri;
    private TextView tvCourseNoteText;
    private ShareActionProvider shareActionProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_note_viewer);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        tvCourseNoteText = (TextView) findViewById(R.id.tv_CourseNoteText);
        courseNoteUri = getIntent().getParcelableExtra(DBProvider.COURSE_NOTE_CONTENT_TYPE);

        if (courseNoteUri != null) {
            courseNoteId = Long.parseLong(Objects.requireNonNull(courseNoteUri.getLastPathSegment()));
            setTitle(getString(R.string.view_course_note));
            loadNote();
        }
    }

    private void loadNote() {
        CourseNote courseNote = DBDataManager.getCourseNote(this, courseNoteId);
        tvCourseNoteText.setText(courseNote.courseNoteText);
        tvCourseNoteText.setMovementMethod(new ScrollingMovementMethod());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            loadNote();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_course_note_viewer, menu);
        MenuItem item = menu.findItem(R.id.menu_item_share);
        shareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);
        CourseNote courseNote = DBDataManager.getCourseNote(this, courseNoteId);
        Course course = DBDataManager.getCourse(this, courseNote.courseId);

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        String shareSubject = course.courseName + ": Course Note";
        String shareBody = courseNote.courseNoteText;
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, shareSubject);
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
        shareActionProvider.setShareIntent(shareIntent);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_delete_course_note:
                return deleteCourseNote();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private boolean deleteCourseNote() {
        DialogInterface.OnClickListener dialogClickListener = (dialog, button) -> {
            if (button == DialogInterface.BUTTON_POSITIVE) {
                DBDataManager.deleteCourseNote(CourseNoteViewerActivity.this, courseNoteId);
                setResult(RESULT_OK);
                finish();
                Toast.makeText(CourseNoteViewerActivity.this, getString(R.string.note_deleted), Toast.LENGTH_SHORT).show();
            }
        };
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.confirm_delete_note)
                .setPositiveButton(getString(android.R.string.yes), dialogClickListener)
                .setNegativeButton(getString(android.R.string.no), dialogClickListener)
                .show();
        return true;
    }

    public void handleEditNote(View view) {
        Intent intent = new Intent(this, CourseNoteEditorActivity.class);
        intent.putExtra(DBProvider.COURSE_NOTE_CONTENT_TYPE, courseNoteUri);
        startActivityForResult(intent, COURSE_NOTE_EDITOR_ACTIVITY_CODE);
    }

}
