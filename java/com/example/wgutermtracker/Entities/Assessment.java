package com.example.wgutermtracker.Entities;

import android.content.ContentValues;
import android.content.Context;

import com.example.wgutermtracker.Utils.DBOpenHelper;
import com.example.wgutermtracker.Utils.DBProvider;

public class Assessment {
    public long assessmentId;
    public long courseId;
    public String assessmentCode;
    public String assessmentName;
    public String assessmentType;
    public String assessmentDescription;
    public String assessmentDatetime;
    public int notifications;

    public void saveChanges(Context context) {
        ContentValues values = new ContentValues();
        values.put(DBOpenHelper.ASSESSMENT_COURSE_ID, courseId);
        values.put(DBOpenHelper.ASSESSMENT_CODE, assessmentCode);
        values.put(DBOpenHelper.ASSESSMENT_NAME, assessmentName);
        values.put(DBOpenHelper.ASSESSMENT_TYPE, assessmentType);
        values.put(DBOpenHelper.ASSESSMENT_DESCRIPTION, assessmentDescription);
        values.put(DBOpenHelper.ASSESSMENT_DATETIME, assessmentDatetime);
        values.put(DBOpenHelper.ASSESSMENT_NOTIFICATIONS, notifications);
        context.getContentResolver().update(DBProvider.ASSESSMENTS_URI, values, DBOpenHelper.ASSESSMENTS_TABLE_ID
                + " = " + assessmentId, null);
    }

}
