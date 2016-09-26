package net.piaw.sharedchecklist;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ListView;

public class ChecklistDisplay extends AppCompatActivity {
    final String Tag = "ChecklistDisplay";
    Checklist mChecklist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checklist_display);
        Toolbar checklistToolbar = (Toolbar) findViewById(R.id.checklist_toolbar);
        setSupportActionBar(checklistToolbar);
        ListView lv = (ListView) findViewById(R.id.checklistview);
        mChecklist = (Checklist) getIntent().getSerializableExtra("checklist");
        assert (mChecklist != null);

        lv.setAdapter(new ChecklistAdapter(getBaseContext(),
                mChecklist.getItems()));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_checklist_display, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                // User chose the "Settings" item, show the app settings UI...
                return true;

            case R.id.action_add:
                // User chose the "Favorite" action, mark the current item
                // as a favorite...
                return true;

            case R.id.action_share:
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }
}
