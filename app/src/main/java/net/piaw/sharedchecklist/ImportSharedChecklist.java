package net.piaw.sharedchecklist;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

public class ImportSharedChecklist extends AppCompatActivity
        implements Database.FetchChecklistCallback {

    static final int IMPORT_REQUEST = 0;

    private final String Tag = "ImportSharedChecklist";
    ListView mLV;
    Button mAccept;
    Button mReject;
    Uri mData = null;
    Checklist mChecklist = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.v(Tag, "onCreate Entered");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_import_shared_checklist);

        mLV = (ListView) findViewById(R.id.shared_checklist);

        Intent intent = getIntent();
        String action = intent.getAction();
        Uri data = intent.getData();
        Log.v(Tag, "Getting data:" + data.toString());
        mData = data;

        if (Database.getDB() == null) {
            // need to login first! Yes, this violates "first click free", but we need to validate
            // users for security reasons
            Log.d(Tag, "DB is empty, creating");
            Intent login = new Intent(this, FacebookLoginActivity.class);
            login.putExtra("import", true);
            startActivityForResult(login, IMPORT_REQUEST);
            return;
        }

        // otherwise, we're already logged in and can import
        Database.getDB().fetchChecklistFromURI(mData, this);

    }

    @Override
    public void onChecklistLoaded(Checklist cl) {
        Log.v(Tag, "onChecklist Loaded!");
        if (cl == null) {
            Toast.makeText(this, "Checklist database error", Toast.LENGTH_LONG).show();
            return;
        }

        /* TODO: fixACL issue
        if (!cl.getAcl().contains(Database.getDB().getEmail())) {
            Toast.makeText(this, "No permission to import this Checklist",
                    Toast.LENGTH_LONG).show();
            return;
        }*/

        Log.v(Tag, "checklistID:" + cl.getId());
        mChecklist = cl;
        // show checklist and choose to accept or reject
        // we only activate these buttons at this point when the user
        // can see the checklist
        mAccept = (Button) findViewById(R.id.accept_share);
        mReject = (Button) findViewById(R.id.reject_share);

        mLV.setAdapter(new ChecklistAdapter(getBaseContext(),
                cl.getItems()));
        mAccept.setOnClickListener(new AcceptShare());
        mReject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK)
            return; // user backed out
        Log.v(Tag, "onActivityResult --- need to wait for checklist loading");
        Database.getDB().fetchChecklistFromURI(mData, this);
    }

    public class AcceptShare implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            Log.v(Tag, "accept share clicked!");
            Database.getDB().setDefaultChecklist(mChecklist);
            Intent intent = new Intent(getApplicationContext(), ChecklistDisplay.class);
            intent.putExtra("checklist", mChecklist);
            startActivity(intent);
        }
    }
}
