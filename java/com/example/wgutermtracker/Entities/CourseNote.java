package com.example.wgutermtracker.Entities;

import android.content.ContentValues;
import android.content.Context;

import com.example.wgutermtracker.Utils.DBOpenHelper;
import com.example.wgutermtracker.Utils.DBProvider;

public class CourseNote {
    public long courseNoteId;
    public long courseId;
    public String courseNoteText;

    public void saveChanges(Context context) {
        ContentValues values = new ContentValues();
        values.put(DBOpenHelper.COURSE_NOTE_COURSE_ID, courseId);
        values.put(DBOpenHelper.COURSE_NOTE_TEXT, courseNoteText);
        context.getContentResolver().update(DBProvider.COURSE_NOTES_URI, values, DBOpenHelper.COURSE_NOTES_TABLE_ID
                + " = " + courseNoteId, null);
    }
}
