package com.example.wgutermtracker.Activities.Main;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.wgutermtracker.Activities.Term.TermListActivity;
import com.example.wgutermtracker.Activities.Term.TermViewerActivity;
import com.example.wgutermtracker.R;
import com.example.wgutermtracker.Utils.DBOpenHelper;
import com.example.wgutermtracker.Utils.DBProvider;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private static final int TERM_VIEWER_ACTIVITY_CODE = 11111;
    private static final int TERM_LIST_ACTIVITY_CODE = 22222;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    public void openCurrentTerm(View view) {
        Cursor c = getContentResolver().query(DBProvider.TERMS_URI, null, DBOpenHelper.TERM_ACTIVE
                + " =1", null, null);
        while (Objects.requireNonNull(c).moveToNext()) {
            Intent intent = new Intent(this, TermViewerActivity.class);
            long id = c.getLong(c.getColumnIndex(DBOpenHelper.TERMS_TABLE_ID));
            Uri uri = Uri.parse(DBProvider.TERMS_URI + "/" + id);
            intent.putExtra(DBProvider.TERM_CONTENT_TYPE, uri);
            startActivityForResult(intent, TERM_VIEWER_ACTIVITY_CODE);
            c.close();
            return;
        }
        Toast.makeText(this, getString(R.string.no_active_term_set),
                Toast.LENGTH_SHORT).show();
    }

    public void openTermList(View view) {
        Intent intent = new Intent(this, TermListActivity.class);
        startActivityForResult(intent, TERM_LIST_ACTIVITY_CODE);
    }
}
