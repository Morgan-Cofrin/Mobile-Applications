package com.example.wgutermtracker.Activities.CourseNote;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.wgutermtracker.Entities.CourseNote;
import com.example.wgutermtracker.R;
import com.example.wgutermtracker.Utils.DBDataManager;
import com.example.wgutermtracker.Utils.DBProvider;

import java.util.Objects;

public class CourseNoteEditorActivity extends AppCompatActivity {

    private long courseId;
    private Uri courseUri;
    private long courseNoteId;
    private Uri courseNoteUri;
    private CourseNote courseNote;
    private EditText noteTextField;
    private String action;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_note_editor);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        noteTextField = (EditText) findViewById(R.id.etCourseNoteText);
        courseNoteUri = getIntent().getParcelableExtra(DBProvider.COURSE_NOTE_CONTENT_TYPE);

        if (courseNoteUri == null) {
            setTitle(getString(R.string.enter_new_note));
            courseUri = getIntent().getParcelableExtra(DBProvider.COURSE_CONTENT_TYPE);
            courseId = Long.parseLong(courseUri.getLastPathSegment());
            action = Intent.ACTION_INSERT;
        }
        else {
            setTitle(getString(R.string.edit_note));
            courseNoteId = Long.parseLong(courseNoteUri.getLastPathSegment());
            courseNote = DBDataManager.getCourseNote(this, courseNoteId);
            courseId = courseNote.courseId;
            noteTextField.setText(courseNote.courseNoteText);
            action = Intent.ACTION_EDIT;
        }
    }

    public void saveCourseNote(View view) {
        if (action.equals(Intent.ACTION_INSERT)) {
            DBDataManager.insertCourseNote(this, courseId, noteTextField.getText().toString().trim());
            setResult(RESULT_OK);
            finish();
        }
        if (action.equals(Intent.ACTION_EDIT)) {
            courseNote.courseNoteText = noteTextField.getText().toString().trim();
            courseNote.saveChanges(this);
            setResult(RESULT_OK);
            finish();
        }
    }

}
