package net.piaw.sharedchecklist;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
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
    @SuppressLint("StaticFieldLeak")
    private static Database mDB = null;
    private final String Tag = "Database";
    private boolean mShowOnFetch;
    private DatabaseReference mUserDB;
    private DatabaseReference mChecklistDB;
    private Activity mActivity;
    private String mEmail;
    private User mUser;

    Database(String email, Activity activity, boolean showOnFetch) {
        Log.v(Tag, "instantiating database");
        mShowOnFetch = showOnFetch;
        mActivity = activity;
        mUserDB = FirebaseDatabase.getInstance().getReference().child("users");
        mChecklistDB = FirebaseDatabase.getInstance().getReference().child("checklists");
        mEmail = email;
        ValueEventListener userListener = new UserListener();
        Log.d(Tag, "AddingValueEventListener for user");
        mUserDB.child(mEmail).addValueEventListener(userListener);
    }

    public static Database getDB() {
        return mDB;
    }

    public static void setDB(Database db) {
        mDB = db;
    }

    public static Uri ConstructUriFromChecklistId(String cl_id) {
        Uri.Builder build = new Uri.Builder();
        build.scheme("http")
                .authority("scl.piaw.net")
                .appendPath("checklists")
                .appendPath(cl_id);
        return build.build();
    }

    DatabaseReference getChecklistDB() {
        return mChecklistDB;
    }

    public User getUser() {
        return mUser;
    }

    public String getEmail() {
        return mEmail;
    }

    public void setDefaultChecklist(Checklist cl) {
        if (cl.getId().equals("")) {
            Log.e(Tag, "checklist id is null!");
            Toast.makeText(getApplicationContext(),
                    "Checklist is corrupt", Toast.LENGTH_LONG).show();
            return;
        }
        // assert mUser is valid!
        mUser.setDefault_checklist(cl.getId());
        mUserDB.child(mEmail).setValue(mUser);
    }

    public void fetchChecklistFromURI(Uri uri, FetchChecklistCallback cb) {
        Log.d(Tag, "fetchChecklistfromURI:" + uri.toString());
        String url = uri.toString();
        String[] parts = url.split("/");
        // the tail is the checklistID
        String checklistId = parts[parts.length - 1];
        Log.d(Tag, "fetching:" + checklistId);
        FetchChecklist(cb, checklistId);
    }

    public void FetchChecklist(FetchChecklistCallback cb, String checklistId) {
        Log.v(Tag, "Fetching checklist:" + checklistId);
        mChecklistDB.child(checklistId)
                .addValueEventListener(new FetchChecklistCallbackListener(cb));
    }

    private Checklist createDefaultChecklist() {
        Checklist checklist = new Checklist();
        checklist.setCreator(mUser.getEmail());
        checklist.setOwner(mUser.getEmail());
        checklist.setItems(new ArrayList<ChecklistItem>());
        checklist.addAcl(mEmail);

        String checklist_id = mChecklistDB.push().getKey();
        checklist.setId(checklist_id);
        mChecklistDB.child(checklist_id).setValue(checklist);
        mUser.setDefault_checklist(checklist_id);
        mUser.addChecklist(checklist_id);
        mUserDB.child(mEmail).setValue(mUser);
        mChecklistDB.child(checklist_id).setValue(checklist);

        return checklist;
    }

    private void ShowChecklist(Checklist checklist) {
        if (!mShowOnFetch) return;
        Log.d(Tag, "Showing checklist:" + checklist.getId());
        Intent intent = new Intent(getApplicationContext(),
                ChecklistDisplay.class);
        intent.putExtra("checklist", checklist);
        mActivity.startActivity(intent);
    }

    public void AddChecklistItem(Checklist cl, ChecklistItem item) {
        Log.d(Tag, "Adding checklist item:" + item.getLabel() + " to:" + cl.getId());
        cl.addItem(item);
        mChecklistDB.child(cl.getId()).setValue(cl);
    }

    public void UpdateChecklist(Checklist cl) {
        Log.d(Tag, "Updating checklist:" + cl.getId());
        mChecklistDB.child(cl.getId()).setValue(cl);
    }

    public interface FetchChecklistCallback {
        void onChecklistLoaded(Checklist cl);
    }

    private class FetchChecklistCallbackListener implements
            ValueEventListener {
        FetchChecklistCallback mCB;

        FetchChecklistCallbackListener(FetchChecklistCallback cb) {
            mCB = cb;
        }

        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            Log.v(Tag, "FetchCLCB:onDataChange");
            Checklist checklist = dataSnapshot.getValue(Checklist.class);
            mCB.onChecklistLoaded(checklist);
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            Log.w(Tag, "FetchChecklist:cancelled!");
            mCB.onChecklistLoaded(null);
        }
    }

    class UserListener implements ValueEventListener {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            Log.d(Tag, "UserListener:onDataChange");
            if (dataSnapshot.exists()) {
                Log.d(Tag, "User exists!");
                mUser = dataSnapshot.getValue(User.class);

                // now retrieve default checklist
                if (!mUser.getDefault_checklist().equals("")) {
                    Log.d(Tag, "User has default checklist!");
                    ValueEventListener cl_listener = new ChecklistListener();
                    mChecklistDB.child(mUser.getDefault_checklist()).
                            addValueEventListener(cl_listener);

                } else {
                    Log.d(Tag, "Creating default checklist for existing user");
                    ShowChecklist(createDefaultChecklist());
                }
            } else {
                Log.d(Tag, "Creating new user");
                // user doesn't exist, now create new user
                mUser = new User();
                mUser.setEmail(mEmail);
                // create new checklist as the default checklist
                // note that createDefaultChecklist() also writes to the UserDB
                // so we don't have to do it
                ShowChecklist(createDefaultChecklist());
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
            Log.d(Tag, "ChecklistListener: onDataChange");
            if (dataSnapshot.exists()) {
                Log.d(Tag, "Checklist exists!");
                Checklist checklist = dataSnapshot.getValue(Checklist.class);
                ShowChecklist(checklist);
            } else {
                Log.d(Tag, "No default checklist --- creating");
                // no existing checklist! create it. For now, just stick it into the
                // Default checklist
                ShowChecklist(createDefaultChecklist());
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
