package com.example.wgutermtracker.Activities.Course;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.wgutermtracker.Entities.Course;
import com.example.wgutermtracker.Entities.CourseStatus;
import com.example.wgutermtracker.R;
import com.example.wgutermtracker.Utils.DBDataManager;
import com.example.wgutermtracker.Utils.DBProvider;
import com.example.wgutermtracker.Utils.DateUtil;

import java.util.Calendar;
import java.util.Objects;

public class CourseEditorActivity extends AppCompatActivity implements View.OnClickListener {

    private String action;
    private Uri courseUri;
    private Uri termUri;
    private Course course;

    private EditText et_CourseName;
    private EditText et_CourseStart;
    private EditText et_CourseEnd;
    private EditText et_CourseMentor;
    private EditText et_CourseMentorPhone;
    private EditText et_CourseMentorEmail;
    private DatePickerDialog courseStartDateDialog;
    private DatePickerDialog courseEndDateDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_editor);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        findViews();
        Intent intent = getIntent();
        courseUri = intent.getParcelableExtra(DBProvider.COURSE_CONTENT_TYPE);
        termUri = intent.getParcelableExtra(DBProvider.TERM_CONTENT_TYPE);

        if (courseUri == null) {
            action = Intent.ACTION_INSERT;
            setTitle(getString(R.string.add_new_course));
        }
        else {
            action = Intent.ACTION_EDIT;
            setTitle(getString(R.string.edit_course_title));
            long classId = Long.parseLong(Objects.requireNonNull(courseUri.getLastPathSegment()));
            course = DBDataManager.getCourse(this, classId);
            fillCourseForm(course);
        }
        setupDatePickers();
    }

    private void findViews() {
        et_CourseName = (EditText) findViewById(R.id.et_CourseName);
        et_CourseStart = (EditText) findViewById(R.id.et_CourseStart);
        et_CourseEnd = (EditText) findViewById(R.id.et_CourseEnd);
        et_CourseMentor = (EditText) findViewById(R.id.et_CourseMentor);
        et_CourseMentorPhone = (EditText) findViewById(R.id.et_CourseMentorPhone);
        et_CourseMentorEmail = (EditText) findViewById(R.id.et_CourseMentorEmail);
    }

    private void fillCourseForm(Course course) {
        et_CourseName.setText(course.courseName);
        et_CourseStart.setText(course.courseStart);
        et_CourseEnd.setText(course.courseEnd);
        et_CourseMentor.setText(course.courseMentor);
        et_CourseMentorPhone.setText(course.mentorPhone);
        et_CourseMentorEmail.setText(course.mentorEmail);
    }

    private void setupDatePickers() {
        et_CourseStart.setOnClickListener(this);
        et_CourseEnd.setOnClickListener(this);

        Calendar calendar = Calendar.getInstance();

        courseStartDateDialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            Calendar newDate = Calendar.getInstance();
            newDate.set(year, month, dayOfMonth);
            et_CourseStart.setText(DateUtil.dateFormat.format(newDate.getTime()));
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

        courseEndDateDialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            Calendar newDate = Calendar.getInstance();
            newDate.set(year, month, dayOfMonth);
            et_CourseEnd.setText(DateUtil.dateFormat.format(newDate.getTime()));
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

        et_CourseStart.setOnFocusChangeListener((view, hasFocus) -> {
            if (hasFocus) {
                courseStartDateDialog.show();
            }
        });

        et_CourseEnd.setOnFocusChangeListener((view, hasFocus) -> {
            if (hasFocus) {
                courseEndDateDialog.show();
            }
        });
    }

    public void saveCourseChanges(View view) {
        if (action.equals(Intent.ACTION_INSERT)) {
            long termId = Long.parseLong(Objects.requireNonNull(termUri.getLastPathSegment()));
            DBDataManager.insertCourse(this, termId,
                    et_CourseName.getText().toString().trim(),
                    et_CourseStart.getText().toString().trim(),
                    et_CourseEnd.getText().toString().trim(),
                    et_CourseMentor.getText().toString().trim(),
                    et_CourseMentorPhone.getText().toString().trim(),
                    et_CourseMentorEmail.getText().toString().trim(),
                    CourseStatus.PLANNED);
        }
        else if (action.equals(Intent.ACTION_EDIT)) {
            course.courseName = et_CourseName.getText().toString().trim();
            course.courseStart = et_CourseStart.getText().toString().trim();
            course.courseEnd = et_CourseEnd.getText().toString().trim();
            course.courseMentor = et_CourseMentor.getText().toString().trim();
            course.mentorPhone = et_CourseMentorPhone.getText().toString().trim();
            course.mentorEmail = et_CourseMentorEmail.getText().toString().trim();
            course.saveChanges(this);
            setResult(RESULT_OK);
        }
        finish();
    }

    @Override
    public void onClick(View view) {
        if (view == et_CourseStart) {
            courseStartDateDialog.show();
        }
        if (view == et_CourseEnd) {
            courseEndDateDialog.show();
        }
    }
}
