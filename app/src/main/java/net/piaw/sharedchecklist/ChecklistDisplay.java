package net.piaw.sharedchecklist;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

public class ChecklistDisplay extends AppCompatActivity {
    final String Tag = "ChecklistDisplay";
    Checklist mChecklist;
    ShareActionProvider mShareActionProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checklist_display);
        Toolbar checklistToolbar = (Toolbar) findViewById(R.id.checklist_toolbar);
        setSupportActionBar(checklistToolbar);
        ListView lv = (ListView) findViewById(R.id.checklistview);
        mChecklist = (Checklist) getIntent().getSerializableExtra("checklist");
        if (BuildConfig.DEBUG && mChecklist == null) {
            throw new RuntimeException("ASSERTION FAILED: mChecklist is NULL!");
        }

        lv.setAdapter(new ChecklistAdapter(getBaseContext(),
                mChecklist.getItems()));

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_checklist_display, menu);
        MenuItem item = menu.findItem(R.id.action_share);
        mShareActionProvider = (ShareActionProvider) item.getActionProvider();
        if (mChecklist.getId().equals("")) {
            Log.e(Tag, "checklist id is null!");
            Toast.makeText(this, "Checklist is Corrupt!", Toast.LENGTH_LONG).show();
            return false;
        }
        // create URI
        Uri.Builder build = new Uri.Builder();
        build.scheme("http")
                .authority("scl.piaw.net")
                .appendPath("checklists")
                .appendPath(mChecklist.getId());
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, build.toString());
        mShareActionProvider.setShareIntent(shareIntent);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                // User chose the "Settings" item, show the app settings UI...
                return true;

            case R.id.action_add:
                // Add a new checklist
                Intent intent = new Intent(this, NewChecklistItemActivity.class);
                intent.putExtra("checklist", mChecklist);
                startActivity(intent);
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
