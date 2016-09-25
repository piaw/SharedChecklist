package net.piaw.sharedchecklist;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import static com.facebook.FacebookSdk.getApplicationContext;

/**
 * Created by piaw on 9/23/2016.
 */

public class Database {
    private final String Tag = "Database";
    private DatabaseReference mDatabase;
    private DatabaseReference mUserDB;
    private DatabaseReference mChecklistDB;
    private Activity mActivity;
    private String mEmail;
    private User mUser;

    Database(String email, Activity activity) {
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mActivity = activity;
        mUserDB = FirebaseDatabase.getInstance().getReference().child("users");
        mChecklistDB = FirebaseDatabase.getInstance().getReference().child("checklists");
        mEmail = email;
        ValueEventListener userListener = new UserListener();
        mUserDB.child(mEmail).addValueEventListener(userListener);
    }

    private void createDefaultChecklist() {
        Checklist checklist = new Checklist();
        checklist.setCreator(mUser.getEmail());
        checklist.setOwner(mUser.getEmail());
        checklist.setItems(new ArrayList<ChecklistItem>());
        checklist.addAcl(mEmail);

        String checklist_id = mChecklistDB.push().getKey();
        mChecklistDB.child(checklist_id).setValue(checklist);
        mUser.setDefault_checklist(checklist_id);
        mUser.addChecklist(checklist_id);
        mUserDB.child(mEmail).setValue(mUser);
        mChecklistDB.child(checklist_id).setValue(checklist);
    }

    class UserListener implements ValueEventListener {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            if (dataSnapshot.exists()) {
                mUser = dataSnapshot.getValue(User.class);

                // now retrieve default checklist
                if (mUser.getDefault_checklist() != "") {
                    ValueEventListener cl_listener = new ChecklistListener();
                    mChecklistDB.child(mUser.getDefault_checklist()).
                            addValueEventListener(cl_listener);

                }
            } else {
                // user doesn't exist, now create new user
                mUser = new User();
                mUser.setEmail(mEmail);
                // create new checklist as the default checklist
                // note that createDefaultChecklist() also writes to the UserDB
                // so we don't have to do it
                createDefaultChecklist();
            }
        }

        @Override
        public void onCancelled(DatabaseError dbErr) {
            Log.w(Tag, "User retrieval failed", dbErr.toException());
            Toast.makeText(getApplicationContext(), "Failed to load user",
                    Toast.LENGTH_SHORT).show();
        }
    }

    class ChecklistListener implements ValueEventListener {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            if (dataSnapshot.exists()) {
                Checklist checklist = dataSnapshot.getValue(Checklist.class);
                Intent intent = new Intent(getApplicationContext(),
                        ChecklistDisplay.class);
                intent.putExtra("checklist", checklist);
                mActivity.startActivity(intent);
            } else {
                // no existing checklist! create it. For now, just stick it into the
                // Default checklist
                createDefaultChecklist();
            }
        }

        @Override
        public void onCancelled(DatabaseError dbErr) {
            Log.w(Tag, "User retrieval failed", dbErr.toException());
            Toast.makeText(getApplicationContext(), "Failed to load checklist",
                    Toast.LENGTH_SHORT).show();
        }
    }
}
