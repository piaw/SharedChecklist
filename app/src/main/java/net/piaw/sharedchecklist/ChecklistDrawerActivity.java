package net.piaw.sharedchecklist;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ChecklistDrawerActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        ValueEventListener, Database.FetchChecklistCallback {
    public final String Tag = "ChecklistDrawerActivity";
    ArrayList<Checklist> mChecklists;
    User mUser;
    ListView mLV;
    ManageChecklistsAdapter mAdapter;

    @Override
    public void onChecklistLoaded(Checklist cl) {
        Log.v(Tag, "onChecklist Loaded!");
        if (cl != null) {
            Log.v(Tag, "adding checklist" + cl.getId());
            synchronized (mChecklists) {
                mChecklists.add(cl);
            }
            mAdapter = new ManageChecklistsAdapter(this, mChecklists, new ClickListener(),
                    new LongClickListener());
            mLV.setAdapter(mAdapter);
        }
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

    private void ViewChecklist(Checklist cl) {
        Intent intent = new Intent(this, ChecklistDisplay.class);
        intent.putExtra("checklist", cl);
        startActivity(intent);
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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checklist_drawer);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        mChecklists = new ArrayList<>();
        mUser = Database.getDB().getUser();
        Database.getDB().getUserDB().child(mUser.getEmail()).addValueEventListener(this);
        mLV = (ListView) navigationView.findViewById(R.id.manage_checklist_lv);
        mLV.setBackgroundColor(Color.parseColor("#81C784"));
        mAdapter = new ManageChecklistsAdapter(this, mChecklists, new ClickListener(),
                new LongClickListener());
        mLV.setAdapter(mAdapter);
        TextView email = (TextView) findViewById(R.id.userid);
        email.setText(Database.unEscapeEmailAddress(mUser.getEmail()));
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.checklist_drawer, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private class LongClickListener implements View.OnLongClickListener {
        @Override
        public boolean onLongClick(View v) {
            Checklist cl = (Checklist) v.getTag();
            ViewChecklist(cl);
            return true;
        }
    }

    private class ClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            Checklist cl = (Checklist) v.getTag();
            Fragment fragment = new ChecklistDisplay();
            Bundle args = new Bundle();
            args.putSerializable("checklist", cl);
            fragment.setArguments(args);

            FragmentManager fragmentManager = getFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.content_view, fragment)
                    .commit();
            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            drawer.closeDrawer(GravityCompat.START);
        }
    }
}
