package com.example.wgutermtracker.Activities.Term;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.wgutermtracker.Activities.Course.CourseListActivity;
import com.example.wgutermtracker.Entities.Term;
import com.example.wgutermtracker.R;
import com.example.wgutermtracker.Utils.DBOpenHelper;
import com.example.wgutermtracker.Utils.DBDataManager;
import com.example.wgutermtracker.Utils.DBProvider;

import java.util.ArrayList;
import java.util.Objects;

public class TermViewerActivity extends AppCompatActivity {

    private static final int TERM_EDITOR_ACTIVITY_CODE = 11111;
    private static final int COURSE_LIST_ACTIVITY_CODE = 22222;

    private Uri termUri;
    private Term term;

    private CursorAdapter cursorAdapter;

    private TextView tv_title;
    private TextView tv_start;
    private TextView tv_end;
    private Menu menu;

    private long termId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_term_viewer);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        termUri = intent.getParcelableExtra(DBProvider.TERM_CONTENT_TYPE);
        findElements();
        loadTermData();
    }

    private void findElements() {
        tv_title = (TextView) findViewById(R.id.tv_TermViewTermTitle);
        tv_start = (TextView) findViewById(R.id.tv_TermViewStartDate);
        tv_end = (TextView) findViewById(R.id.tv_TermViewEndDate);
    }

    private void loadTermData() {
        if (termUri == null) {
            setResult(RESULT_CANCELED);
            finish();
        }
        else {
            termId = Long.parseLong(termUri.getLastPathSegment());
            term = DBDataManager.getTerm(this, termId);

            setTitle(getString(R.string.view_term));
            tv_title.setText(term.termName);
            tv_start.setText(term.termStart);
            tv_end.setText(term.termEnd);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_term_viewer, menu);
        this.menu = menu;
        showAppropriateMenuOptions();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_mark_term_active:
                return markTermActive();
            case R.id.action_edit_term:
                Intent intent = new Intent(this, TermEditorActivity.class);
                Uri uri = Uri.parse(DBProvider.TERMS_URI + "/" + term.termId);
                intent.putExtra(DBProvider.TERM_CONTENT_TYPE, uri);
                startActivityForResult(intent, TERM_EDITOR_ACTIVITY_CODE);
                break;
            case R.id.action_delete_term:
                return deleteTerm();
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    private boolean markTermActive() {
        Cursor cursor = getContentResolver().query(DBProvider.TERMS_URI, null, null, null, null);
        ArrayList<Term> termList = new ArrayList<>();
        while (Objects.requireNonNull(cursor).moveToNext()) {
            termList.add(DBDataManager.getTerm(this, cursor.getLong(cursor.getColumnIndex(DBOpenHelper.TERMS_TABLE_ID))));
        }

        for (Term term : termList) {
            term.deactivate(this);
        }

        this.term.activate(this);
        showAppropriateMenuOptions();

        Toast.makeText(TermViewerActivity.this, getString(R.string.term_marked_active), Toast.LENGTH_SHORT).show();

        cursor.close();
        return true;
    }

    private boolean deleteTerm() {
        DialogInterface.OnClickListener dialogClickListener = (dialog, button) -> {
            if (button == DialogInterface.BUTTON_POSITIVE) {
                long classCount = term.getClassCount(TermViewerActivity.this);
                if (classCount == 0) {
                    getContentResolver().delete(DBProvider.TERMS_URI, DBOpenHelper.TERMS_TABLE_ID + " = " + termId, null);

                    Toast.makeText(TermViewerActivity.this, getString(R.string.term_deleted), Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                }
                else {
                    Toast.makeText(TermViewerActivity.this, getString(R.string.need_to_remove_courses), Toast.LENGTH_SHORT).show();
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.confirm_delete_term)
                .setPositiveButton(getString(android.R.string.yes), dialogClickListener)
                .setNegativeButton(getString(android.R.string.no), dialogClickListener)
                .show();
        return true;
    }

    public void showAppropriateMenuOptions() {
        if (term.active == 1) {
            menu.findItem(R.id.action_mark_term_active).setVisible(false);
        }
    }

    public void openClassList(View view) {
        Intent intent = new Intent(this, CourseListActivity.class);
        intent.putExtra(DBProvider.TERM_CONTENT_TYPE, termUri);
        startActivityForResult(intent, COURSE_LIST_ACTIVITY_CODE);
    }
}
