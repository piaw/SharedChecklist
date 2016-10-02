package net.piaw.sharedchecklist;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

public class ChecklistDisplay extends AppCompatActivity implements ValueEventListener,
        Database.SharedChecklistCallback, Database.FetchChecklistCallback {
    public final int DISPLAY_SETTINGS = 0;
    final String Tag = "ChecklistDisplay";
    Checklist mChecklist;
    ListView mLV;
    ChecklistAdapter mAdapter;
    ShareActionProvider mShareActionProvider;

    @Override
    public void onSharedChecklist(String checklistid) {
        // fetch checklist
        Database.getDB().FetchChecklist(this, checklistid);
    }

    public void onChecklistLoaded(Checklist cl) {
        if (cl == null) return;
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this)
                        .setAutoCancel(true)
                        .setSmallIcon(R.drawable.notify)
                        .setContentTitle("SharedChecklist")
                        .setContentText(cl.getChecklist_name() + "(" +
                                Database.unEscapeEmailAddress(cl.getOwner()) + ")" + " shared.");
        Intent resultIntent = new Intent(this, ViewPendingActivity.class);
        resultIntent.putExtra("checklist", cl);
        TaskStackBuilder stackbuilder = TaskStackBuilder.create(this);
        stackbuilder.addParentStack(ViewPendingActivity.class);
        stackbuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent = stackbuilder.getPendingIntent(0,
                PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(resultPendingIntent);
        NotificationManager notification_manager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notification_manager.notify(0, builder.build());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checklist_display);
        Toolbar checklistToolbar = (Toolbar) findViewById(R.id.checklist_toolbar);
        setSupportActionBar(checklistToolbar);
        mLV = (ListView) findViewById(R.id.checklistview);
        mLV.setItemsCanFocus(true);
        mChecklist = (Checklist) getIntent().getSerializableExtra("checklist");
        getSupportActionBar().setTitle("Checklist:" + mChecklist.getChecklist_name());
        if (BuildConfig.DEBUG && mChecklist == null) {
            throw new RuntimeException("ASSERTION FAILED: mChecklist is NULL!");
        }

        mLV.setAdapter(mAdapter = new ChecklistAdapter(this, mChecklist));
        Database.getDB().SharedChecklistNotificationOn(this);
        Database.getDB().getChecklistDB().child(mChecklist.getId())
                .addValueEventListener(this);

    }

    @Override
    public void onDataChange(DataSnapshot snapshot) {
        Log.v(Tag, "onDataChange!");
        mChecklist = snapshot.getValue(Checklist.class);
        Log.v(Tag, "setting adapter!");
        mLV.setAdapter(mAdapter = new ChecklistAdapter(this, mChecklist));
    }

    public void onCancelled(DatabaseError dberr) {
        Log.v(Tag, "onCancelled");
        Toast.makeText(this, "Database error!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_checklist_display, menu);
        if (mChecklist.getId().equals("")) {
            Log.e(Tag, "checklist id is null!");
            Toast.makeText(this, "Checklist is Corrupt!", Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // might as well refresh everything
        mLV.setAdapter(mAdapter = new ChecklistAdapter(this, mChecklist));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                // User chose the "Settings" item, show the app settings UI...
                Intent intent = new Intent(this, SettingsActivity.class);
                intent.putExtra("checklist", mChecklist);
                startActivityForResult(intent, DISPLAY_SETTINGS);
                return true;

            case R.id.action_add:
                // Add a new checklist
                mAdapter.addNewChecklistItem();
                return true;

            case R.id.action_manage:
                // manage checklists
                intent = new Intent(this, ManageChecklists.class);
                startActivity(intent);
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }
}
