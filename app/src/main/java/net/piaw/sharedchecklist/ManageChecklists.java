package net.piaw.sharedchecklist;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

public class ManageChecklists extends AppCompatActivity implements Database.FetchChecklistCallback {
    public final String Tag = "ManageChecklists";
    public final int REFRESH_REQUIRED = 0;
    ArrayList<Checklist> mChecklists;
    ListView mLV;
    ManageChecklistsAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.v(Tag, "onCreate called");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_checklists);
        Toolbar manage_checklist_toolbar = (Toolbar) findViewById(R.id.manage_checklists_toolbar);
        setSupportActionBar(manage_checklist_toolbar);
        getSupportActionBar().setTitle("Manage Checklists");
        mChecklists = new ArrayList<>();
        mLV = (ListView) findViewById(R.id.manage_checklists_listview);
        refreshChecklists();
        mAdapter = new ManageChecklistsAdapter(this, mChecklists);
        mLV.setAdapter(mAdapter);
    }

    private void refreshChecklists() {
        mChecklists = new ArrayList<>();
        // fetch all checklists
        User user = Database.getDB().getUser();
        Log.v(Tag, "getting checklists for user:" + user.getEmail());
        for (int i = 0; i < user.getChecklists().size(); ++i) {
            Log.v(Tag, "fetching:" + user.getChecklists().get(i));
            Database.getDB().FetchChecklist(this, user.getChecklists().get(i));
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        Log.v(Tag, "onCreateOptionsMenu");
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_manage_checklists, menu);
        if (Database.getDB() == null) {
            Log.e(Tag, "database invalid!");
            Toast.makeText(this, "Databaes Invalid!", Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode,
                                    final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // refresh
        refreshChecklists();
    }

    @Override
    public void onChecklistLoaded(Checklist cl) {
        Log.v(Tag, "onChecklist Loaded!");
        if (cl != null) {
            Log.v(Tag, "adding checklist" + cl.getId());
            synchronized (mChecklists) {
                mChecklists.add(cl);
            }
            mAdapter = new ManageChecklistsAdapter(this, mChecklists);
            mLV.setAdapter(mAdapter);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.checklist_add:
                // User chose the "Settings" item, show the app settings UI...
                Intent intent = new Intent(this, NewChecklistActivity.class);
                startActivityForResult(intent, REFRESH_REQUIRED);
                return true;

            case R.id.checklist_delete:
                // Add a new checklist
                return true;

            case R.id.checklist_copy:
                // manage checklists
                return true;

            case R.id.checklist_share:
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }
}
