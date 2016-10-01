package net.piaw.sharedchecklist;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

public class ShareChecklistActivity extends AppCompatActivity {
    public String Tag = "ShareChecklistActivity";
    EditText mShareeEmail;
    Button mShareButton;
    ListView mACLView;
    Checklist mCL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share_checklist);
        mCL = (Checklist) getIntent().getSerializableExtra("checklist");
        mShareeEmail = (EditText) findViewById(R.id.sharee_email);
        mShareButton = (Button) findViewById(R.id.share_button);
        mACLView = (ListView) findViewById(R.id.acl_view);
        refreshmACLView();
        mShareButton.setOnClickListener(new ShareButtonClicked());
    }

    private void refreshmACLView() {
        ArrayList<String> converted_emails = new ArrayList<>();
        for (int i = 0; i < mCL.getAcl().size(); i++) {
            converted_emails.add(Database.unEscapeEmailAddress(mCL.getAcl().get(i)));
        }
        mACLView.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,
                converted_emails));
    }

    class ShareButtonClicked implements View.OnClickListener, Database.FetchUserCallback {
        String mEmail;

        @Override
        public void onClick(View v) {
            mEmail = Database.escapeEmailAddress(mShareeEmail.getText().toString());
            Database.getDB().FetchUser(this, mEmail);
        }

        public void onUserLoaded(User user) {
            if (user == null) {
                Toast.makeText(ShareChecklistActivity.this, "No user " + mEmail,
                        Toast.LENGTH_SHORT).show();
                return;
            }

            // user exists, add that to the acl, then add the checklist to the pending list
            if (!mCL.getAcl().contains(mEmail)) {
                mCL.getAcl().add(mEmail);
                Database.getDB().UpdateChecklist(mCL);
                user.addPending_checklist(mCL.getId());
                Database.getDB().UpdateUser(user);
                refreshmACLView();
            }
        }
    }
}
