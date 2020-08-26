package com.example.wgutermtracker.Utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.example.wgutermtracker.Entities.Assessment;
import com.example.wgutermtracker.Entities.Course;
import com.example.wgutermtracker.Entities.CourseNote;
import com.example.wgutermtracker.Entities.CourseStatus;
import com.example.wgutermtracker.Entities.Term;

import java.util.Objects;

public class DBDataManager {

    // Terms
    public static Uri insertTerm(Context context, String termName, String termStart, String termEnd, int termActive) {
        ContentValues values = new ContentValues();
        values.put(DBOpenHelper.TERM_NAME, termName);
        values.put(DBOpenHelper.TERM_START, termStart);
        values.put(DBOpenHelper.TERM_END, termEnd);
        values.put(DBOpenHelper.TERM_ACTIVE, termActive);
        Uri termUri = context.getContentResolver().insert(DBProvider.TERMS_URI, values);
        return termUri;
    }

    public static Term getTerm(Context context, long termId) {
        Cursor cursor = context.getContentResolver().query(DBProvider.TERMS_URI, DBOpenHelper.TERMS_COLUMNS,
                DBOpenHelper.TERMS_TABLE_ID + " = " + termId, null, null);
        Objects.requireNonNull(cursor).moveToFirst();
        String termName = cursor.getString(cursor.getColumnIndex(DBOpenHelper.TERM_NAME));
        String termStartDate = cursor.getString(cursor.getColumnIndex(DBOpenHelper.TERM_START));
        String termEndDate = cursor.getString(cursor.getColumnIndex(DBOpenHelper.TERM_END));
        int termActive = cursor.getInt(cursor.getColumnIndex(DBOpenHelper.TERM_ACTIVE));
        cursor.close();

        Term t = new Term();
        t.termId = termId;
        t.termName = termName;
        t.termStart = termStartDate;
        t.termEnd = termEndDate;
        t.active = termActive;
        return t;
    }

    // Courses
    public static Uri insertCourse(Context context, long termId, String courseName, String courseStart, String courseEnd,
                                   String courseMentor, String courseMentorPhone, String courseMentorEmail, CourseStatus status) {
        ContentValues values = new ContentValues();
        values.put(DBOpenHelper.COURSE_TERM_ID, termId);
        values.put(DBOpenHelper.COURSE_NAME, courseName);
        values.put(DBOpenHelper.COURSE_START, courseStart);
        values.put(DBOpenHelper.COURSE_END, courseEnd);
        values.put(DBOpenHelper.COURSE_MENTOR, courseMentor);
        values.put(DBOpenHelper.COURSE_MENTOR_PHONE, courseMentorPhone);
        values.put(DBOpenHelper.COURSE_MENTOR_EMAIL, courseMentorEmail);
        values.put(DBOpenHelper.COURSE_STATUS, status.toString());
        Uri courseUri = context.getContentResolver().insert(DBProvider.COURSES_URI, values);
        return courseUri;
    }

    public static Course getCourse(Context context, long courseId) {
        Cursor cursor = context.getContentResolver().query(DBProvider.COURSES_URI, DBOpenHelper.COURSES_COLUMNS,
                DBOpenHelper.COURSES_TABLE_ID + " = " + courseId, null, null);
        Objects.requireNonNull(cursor).moveToFirst();
        long termId = cursor.getLong(cursor.getColumnIndex(DBOpenHelper.COURSE_TERM_ID));
        String courseName = cursor.getString(cursor.getColumnIndex(DBOpenHelper.COURSE_NAME));
        String courseDescription = cursor.getString(cursor.getColumnIndex(DBOpenHelper.COURSE_DESCRIPTION));
        String courseStart = cursor.getString(cursor.getColumnIndex(DBOpenHelper.COURSE_START));
        String courseEnd = cursor.getString(cursor.getColumnIndex(DBOpenHelper.COURSE_END));
        CourseStatus courseStatus = CourseStatus.valueOf(cursor.getString(cursor.getColumnIndex(DBOpenHelper.COURSE_STATUS)));
        String courseMentor = cursor.getString(cursor.getColumnIndex(DBOpenHelper.COURSE_MENTOR));
        String courseMentorPhone = cursor.getString(cursor.getColumnIndex(DBOpenHelper.COURSE_MENTOR_PHONE));
        String courseMentorEmail = cursor.getString(cursor.getColumnIndex(DBOpenHelper.COURSE_MENTOR_EMAIL));
        int startNotifications = (cursor.getInt(cursor.getColumnIndex(DBOpenHelper.COURSE_START_NOTIFICATIONS)));
        int endNotifications = (cursor.getInt(cursor.getColumnIndex(DBOpenHelper.COURSE_END_NOTIFICATIONS)));
        cursor.close();

        Course c = new Course();
        c.courseId = courseId;
        c.termId = termId;
        c.courseName = courseName;
        c.courseDescription = courseDescription;
        c.courseStart = courseStart;
        c.courseEnd = courseEnd;
        c.courseStatus = courseStatus;
        c.courseMentor = courseMentor;
        c.mentorPhone = courseMentorPhone;
        c.mentorEmail = courseMentorEmail;
        c.startNotifications = startNotifications;
        c.endNotifications = endNotifications;
        return c;
    }

    public static boolean deleteCourse(Context context, long courseId) {
        Cursor notesCursor = context.getContentResolver().query(DBProvider.COURSE_NOTES_URI,
                DBOpenHelper.COURSE_NOTES_COLUMNS, DBOpenHelper.COURSE_NOTE_COURSE_ID + " = " + courseId,
                null, null);
        while (Objects.requireNonNull(notesCursor).moveToNext()) {
            deleteCourseNote(context, notesCursor.getLong(notesCursor.getColumnIndex(DBOpenHelper.COURSE_NOTES_TABLE_ID)));
        }
        context.getContentResolver().delete(DBProvider.COURSES_URI, DBOpenHelper.COURSES_TABLE_ID + " = "
                + courseId, null);
        notesCursor.close();
        return true;
    }

    // Course Notes
    public static Uri insertCourseNote(Context context, long courseId, String text) {
        ContentValues values = new ContentValues();
        values.put(DBOpenHelper.COURSE_NOTE_COURSE_ID, courseId);
        values.put(DBOpenHelper.COURSE_NOTE_TEXT, text);
        Uri courseNoteUri = context.getContentResolver().insert(DBProvider.COURSE_NOTES_URI, values);
        return courseNoteUri;
    }

    public static CourseNote getCourseNote(Context context, long courseNoteId) {
        Cursor cursor = context.getContentResolver().query(DBProvider.COURSE_NOTES_URI, DBOpenHelper.COURSE_NOTES_COLUMNS,
                DBOpenHelper.COURSE_NOTES_TABLE_ID + " = " + courseNoteId, null, null);
        Objects.requireNonNull(cursor).moveToFirst();
        long courseId = cursor.getLong(cursor.getColumnIndex(DBOpenHelper.COURSE_NOTE_COURSE_ID));
        String text = cursor.getString(cursor.getColumnIndex(DBOpenHelper.COURSE_NOTE_TEXT));
        cursor.close();

        CourseNote c = new CourseNote();
        c.courseNoteId = courseNoteId;
        c.courseId = courseId;
        c.courseNoteText = text;
        return c;
    }

    public static boolean deleteCourseNote(Context context, long courseNoteId) {
        context.getContentResolver().delete(DBProvider.COURSE_NOTES_URI, DBOpenHelper.COURSE_NOTES_TABLE_ID + " = " + courseNoteId, null);
        return true;
    }

    // Assessments
    public static Uri insertAssessment(Context context, long courseId, String code, String name, String type, String description, String datetime) {
        ContentValues values = new ContentValues();
        values.put(DBOpenHelper.ASSESSMENT_COURSE_ID, courseId);
        values.put(DBOpenHelper.ASSESSMENT_CODE, code);
        values.put(DBOpenHelper.ASSESSMENT_NAME, name);
        values.put(DBOpenHelper.ASSESSMENT_TYPE, type);
        values.put(DBOpenHelper.ASSESSMENT_DESCRIPTION, description);
        values.put(DBOpenHelper.ASSESSMENT_DATETIME, datetime);
        Uri assessmentUri = context.getContentResolver().insert(DBProvider.ASSESSMENTS_URI, values);
        return assessmentUri;
    }

    public static Assessment getAssessment(Context context, long assessmentId) {
        Cursor cursor = context.getContentResolver().query(DBProvider.ASSESSMENTS_URI, DBOpenHelper.ASSESSMENTS_COLUMNS,
                DBOpenHelper.ASSESSMENTS_TABLE_ID + " = " + assessmentId, null, null);
        Objects.requireNonNull(cursor).moveToFirst();
        long courseId = cursor.getLong(cursor.getColumnIndex(DBOpenHelper.ASSESSMENT_COURSE_ID));
        String name = cursor.getString(cursor.getColumnIndex(DBOpenHelper.ASSESSMENT_NAME));
        String type = cursor.getString(cursor.getColumnIndex(DBOpenHelper.ASSESSMENT_TYPE));
        String description = cursor.getString(cursor.getColumnIndex(DBOpenHelper.ASSESSMENT_DESCRIPTION));
        String code = cursor.getString(cursor.getColumnIndex(DBOpenHelper.ASSESSMENT_CODE));
        String datetime = cursor.getString(cursor.getColumnIndex(DBOpenHelper.ASSESSMENT_DATETIME));
        int notifications = cursor.getInt(cursor.getColumnIndex(DBOpenHelper.ASSESSMENT_NOTIFICATIONS));
        cursor.close();

        Assessment a = new Assessment();
        a.assessmentId = assessmentId;
        a.courseId = courseId;
        a.assessmentName = name;
        a.assessmentType = type;
        a.assessmentDescription = description;
        a.assessmentCode = code;
        a.assessmentDatetime = datetime;
        a.notifications = notifications;
        return a;
    }

    public static boolean deleteAssessment(Context context, long assessmentId) {
        context.getContentResolver().delete(DBProvider.ASSESSMENTS_URI, DBOpenHelper.ASSESSMENTS_TABLE_ID
                + " = " + assessmentId, null);
        return true;
    }

}
