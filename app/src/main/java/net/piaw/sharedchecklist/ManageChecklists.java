package net.piaw.sharedchecklist;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

public class ManageChecklists extends AppCompatActivity implements Database.FetchChecklistCallback {
    public final String Tag = "ManageChecklists";
    ArrayList<Checklist> mChecklists;
    ListView mLV;

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
        // fetch all checklists
        User user = Database.getDB().getUser();
        Log.v(Tag, "getting checklists for user:" + user.getEmail());
        for (int i = 0; i < user.getChecklists().size(); ++i) {
            Log.v(Tag, "fetching:" + user.getChecklists().get(i));
            Database.getDB().FetchChecklist(this, user.getChecklists().get(i));
        }
        mLV.setAdapter(new ManageChecklistsAdapter(this, mChecklists));
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
    public void onChecklistLoaded(Checklist cl) {
        Log.v(Tag, "onChecklist Loaded!");
        if (cl != null) {
            Log.v(Tag, "adding checklist" + cl.getId());
            synchronized (mChecklists) {
                mChecklists.add(cl);
            }
            mLV.setAdapter(new ManageChecklistsAdapter(this, mChecklists));
        }
    }
}
