package net.piaw.sharedchecklist;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ManageChecklists extends AppCompatActivity implements Database.FetchChecklistCallback,
        ValueEventListener {
    public final String Tag = "ManageChecklists";
    public final int REFRESH_REQUIRED = 0;
    ArrayList<Checklist> mChecklists;
    ListView mLV;
    ManageChecklistsAdapter mAdapter;
    User mUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.v(Tag, "onCreate called");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_checklists);
        Toolbar manage_checklist_toolbar = (Toolbar) findViewById(R.id.manage_checklists_toolbar);
        setSupportActionBar(manage_checklist_toolbar);
        getSupportActionBar().setTitle("Manage Checklists");
        mChecklists = new ArrayList<>();
        mUser = Database.getDB().getUser();
        Database.getDB().getUserDB().child(mUser.getEmail()).addValueEventListener(this);
        mLV = (ListView) findViewById(R.id.manage_checklists_listview);
        mAdapter = new ManageChecklistsAdapter(this, mChecklists, new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Checklist cl = (Checklist) v.getTag();
                Intent intent = new Intent(ManageChecklists.this, ChecklistDisplay.class);
                intent.putExtra("checklist", cl);
                startActivity(intent);
                return true;
            }
        });
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
            mAdapter = new ManageChecklistsAdapter(this, mChecklists, new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    Checklist cl = (Checklist) v.getTag();
                    Intent intent = new Intent(ManageChecklists.this, ChecklistDisplay.class);
                    intent.putExtra("checklist", cl);
                    startActivity(intent);
                    return true;
                }
            });
            mLV.setAdapter(mAdapter);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Checklist cl;
        switch (item.getItemId()) {
            case R.id.checklist_add:
                // User chose the "Settings" item, show the app settings UI...
                Intent intent = new Intent(this, NewChecklistActivity.class);
                startActivityForResult(intent, REFRESH_REQUIRED);
                return true;

            case R.id.checklist_delete:
                //  delete a checklist
                cl = mAdapter.getCurrentSelected();
                if (cl == null) {
                    Toast.makeText(this, "No checklist selected!", Toast.LENGTH_SHORT).show();
                    return true;
                }
                User user = Database.getDB().getUser();
                if (cl.getId().equals(user.getDefault_checklist())) {
                    Toast.makeText(this, "Cannot delete default checklist!", Toast.LENGTH_SHORT)
                            .show();
                    return true;
                }
                if (!cl.getOwner().equals(user.getEmail())) {
                    Toast.makeText(this, "Not owner. Cannot delete." + cl.getOwner(),
                            Toast.LENGTH_SHORT).show();
                    Toast.makeText(this, "you:" + user.getEmail(), Toast.LENGTH_SHORT).show();
                    return true;
                }
                user.getChecklists().remove(cl);
                Database.getDB().UpdateUser();
                Database.getDB().DeleteChecklist(cl);

                return true;

            case R.id.checklist_copy:
                // manage checklists
                return true;

            case R.id.checklist_share:
                // share with another user
                cl = mAdapter.getCurrentSelected();
                if (cl == null) {
                    Toast.makeText(this, "No checklist selected!", Toast.LENGTH_SHORT).show();
                    return true;
                }
                intent = new Intent(this, ShareChecklistActivity.class);
                intent.putExtra("checklist", cl);
                startActivity(intent);
                return true;

            case R.id.show_pending:
                intent = new Intent(this, ManagePendingActivity.class);
                startActivityForResult(intent, REFRESH_REQUIRED);
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }
}
