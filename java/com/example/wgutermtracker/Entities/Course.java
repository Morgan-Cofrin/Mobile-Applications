package com.example.wgutermtracker.Entities;

import android.content.ContentValues;
import android.content.Context;

import com.example.wgutermtracker.Utils.DBOpenHelper;
import com.example.wgutermtracker.Utils.DBProvider;

public class Course {
    public long courseId;
    public long termId;
    public String courseName;
    public String courseDescription;
    public String courseStart;
    public String courseEnd;
    public CourseStatus courseStatus;
    public String courseMentor;
    public String mentorPhone;
    public String mentorEmail;
    public int startNotifications;
    public int endNotifications;

    public void saveChanges(Context context) {
        ContentValues values = new ContentValues();
        values.put(DBOpenHelper.COURSE_TERM_ID, termId);
        values.put(DBOpenHelper.COURSE_NAME, courseName);
        values.put(DBOpenHelper.COURSE_DESCRIPTION, courseDescription);
        values.put(DBOpenHelper.COURSE_START, courseStart);
        values.put(DBOpenHelper.COURSE_END, courseEnd);
        values.put(DBOpenHelper.COURSE_STATUS, courseStatus.toString());
        values.put(DBOpenHelper.COURSE_MENTOR, courseMentor);
        values.put(DBOpenHelper.COURSE_MENTOR_PHONE, mentorPhone);
        values.put(DBOpenHelper.COURSE_MENTOR_EMAIL, mentorEmail);
        values.put(DBOpenHelper.COURSE_START_NOTIFICATIONS, startNotifications);
        values.put(DBOpenHelper.COURSE_END_NOTIFICATIONS, endNotifications);
        context.getContentResolver().update(DBProvider.COURSES_URI, values, DBOpenHelper.COURSES_TABLE_ID
                + " = " + courseId, null);
    }
}
