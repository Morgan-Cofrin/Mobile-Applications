package com.example.wgutermtracker.Entities;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.example.wgutermtracker.Utils.DBOpenHelper;
import com.example.wgutermtracker.Utils.DBProvider;

import java.util.Objects;

public class Term {
    public long termId;
    public String termName;
    public String termStart;
    public String termEnd;
    public int active;

    public void saveChanges(Context context) {
        ContentValues values = new ContentValues();
        values.put(DBOpenHelper.TERM_NAME, termName);
        values.put(DBOpenHelper.TERM_START, termStart);
        values.put(DBOpenHelper.TERM_END, termEnd);
        values.put(DBOpenHelper.TERM_ACTIVE, active);
        context.getContentResolver().update(DBProvider.TERMS_URI, values, DBOpenHelper.TERMS_TABLE_ID
                + " = " + termId, null);
    }

    public long getClassCount(Context context) {
        Cursor cursor = context.getContentResolver().query(DBProvider.COURSES_URI, DBOpenHelper.COURSES_COLUMNS,
                DBOpenHelper.COURSE_TERM_ID + " = " + this.termId, null, null);
        int numRows = Objects.requireNonNull(cursor).getCount();
        cursor.close();
        return numRows;
    }

    public void deactivate(Context context) {
        this.active = 0;
        saveChanges(context);
    }

    public void activate(Context context) {
        this.active = 1;
        saveChanges(context);
    }

}
