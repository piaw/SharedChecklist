package net.piaw.sharedchecklist;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ManagePendingActivity extends AppCompatActivity implements Database.FetchChecklistCallback,
        ValueEventListener {
    public static final String Tag = "ManagePendingActivity";
    public static final int ACCEPT_OR_REJECT = 0;
    ArrayList<Checklist> mChecklists;
    ListView mLV;
    ManageChecklistsAdapter mAdapter;
    User mUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_pending);
        mChecklists = new ArrayList<>();
        mUser = Database.getDB().getUser();
        mLV = (ListView) findViewById(R.id.pending_view);
        mAdapter = new ManageChecklistsAdapter(this, mChecklists, new StartViewPendingActivity());
        mLV.setAdapter(mAdapter);
        refreshChecklists();
    }

    private void refreshChecklists() {
        mChecklists = new ArrayList<>();
        // fetch all checklists
        User user = Database.getDB().getUser();
        Log.v(Tag, "getting checklists for user:" + user.getEmail());
        for (int i = 0; i < user.getPending_checklists().size(); ++i) {
            Log.v(Tag, "fetching:" + user.getPending_checklists().get(i));
            Database.getDB().FetchChecklist(this, user.getPending_checklists().get(i));
        }
        if (user.getPending_checklists().size() == 0) {
            Checklist cl = new Checklist();
            cl.setChecklist_name("No pending checklists");
            mChecklists.add(cl);
            mAdapter.notifyDataSetChanged();
        }
    }

    public void onDataChange(DataSnapshot snapshot) {
        Log.v(Tag, "onDataChange!");
        mUser = snapshot.getValue(User.class);
        Log.v(Tag, "setting adapter!");
        refreshChecklists();
        mAdapter.notifyDataSetChanged();
    }

    public void onCancelled(DatabaseError dberr) {
        Log.v(Tag, "onCancelled");
        Toast.makeText(this, "Database error!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onChecklistLoaded(Checklist cl) {
        Log.v(Tag, "onChecklist Loaded!");
        if (cl != null) {
            Log.v(Tag, "adding checklist" + cl.getId());
            synchronized (mChecklists) {
                mChecklists.add(cl);
            }
            mAdapter = new ManageChecklistsAdapter(this, mChecklists,
                    new StartViewPendingActivity());
            mLV.setAdapter(mAdapter);
        }
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode,
                                    final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // refresh
        refreshChecklists();
    }

    class StartViewPendingActivity implements View.OnLongClickListener {
        public boolean onLongClick(View v) {
            Checklist cl = (Checklist) v.getTag();
            Intent intent = new Intent(ManagePendingActivity.this, ViewPendingActivity.class);
            intent.putExtra("checklist", cl);
            startActivityForResult(intent, ACCEPT_OR_REJECT);
            return true;
        }
    }
}
