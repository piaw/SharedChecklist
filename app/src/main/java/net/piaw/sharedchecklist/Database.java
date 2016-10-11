package net.piaw.sharedchecklist;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

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
        mUserDB.child(mEmail).addListenerForSingleValueEvent(userListener);
    }

    public static Database getDB() {
        return mDB;
    }

    public static void setDB(Database db) {
        mDB = db;
    }

    public static String unEscapeEmailAddress(String email) {
        // Replace ',' (not allowed in a Firebase key) with '.' (not allowed in an email address)
        return email.toLowerCase().replaceAll(",", "\\.");
    }

    public static String escapeEmailAddress(String email) {
        // Replace '.' (not allowed in a Firebase key) with ',' (not allowed in an email address)
        return email.toLowerCase().replaceAll("\\.", ",");
    }

    DatabaseReference getChecklistDB() {
        return mChecklistDB;
    }

    DatabaseReference getUserDB() {
        return mUserDB;
    }

    public User getUser() {
        return mUser;
    }

    public String getEmail() {
        return mEmail;
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
                .addListenerForSingleValueEvent(new FetchChecklistCallbackListener(cb));
    }

    public void UpdateUser() {
        mUserDB.child(mEmail).setValue(mUser);
    }

    public void UpdateUser(User user) {
        mUserDB.child(user.getEmail()).setValue(user);
    }

    public void DeleteChecklist(Checklist cl) {
        mChecklistDB.child(cl.getId()).removeValue();
    }

    @NonNull
    public Checklist CreateChecklist(Checklist checklist) {
        String checklist_id = mChecklistDB.push().getKey();
        checklist.setId(checklist_id);
        mChecklistDB.child(checklist_id).setValue(checklist);
        mUser.addChecklist(checklist_id);
        mUserDB.child(mEmail).setValue(mUser);
        return checklist;
    }

    public void SharedChecklistNotificationOn(SharedChecklistCallback cb) {
        mUserDB.child(mEmail).child("pending_checklists").
                addChildEventListener(new SharedChecklistEventListener((cb)));
    }

    private void ShowChecklistDrawer() {
        Intent intent = new Intent(getApplicationContext(),
                ChecklistDrawerActivity.class);
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

    public void FetchUser(FetchUserCallback cb, String email) {
        mUserDB.child(email).addListenerForSingleValueEvent(new FetchUserCallbackListener(cb));
    }

    public interface SharedChecklistCallback {
        void onSharedChecklist(String clId);
    }

    public interface FetchUserCallback {
        void onUserLoaded(User user);
    }

    public interface FetchChecklistCallback {
        void onChecklistLoaded(Checklist cl);
    }

    class SharedChecklistEventListener implements ChildEventListener {
        SharedChecklistCallback mCB;

        SharedChecklistEventListener(SharedChecklistCallback cb) {
            mCB = cb;
        }

        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String prevChildName) {
            String checklist_id = dataSnapshot.getValue(String.class);
            mCB.onSharedChecklist(checklist_id);
        }

        @Override
        public void onCancelled(DatabaseError err) {
            // ignore
        }

        public void onChildChanged(DataSnapshot snapshot, String previousChildName) {
            // ignore
        }

        public void onChildMoved(DataSnapshot snapshot, String previousChildName) {
            // ignore
        }

        public void onChildRemoved(DataSnapshot snapshot) {
            // ignore
        }
    }

    private class FetchUserCallbackListener implements ValueEventListener {
        FetchUserCallback mCB;

        FetchUserCallbackListener(FetchUserCallback cb) {
            mCB = cb;
        }

        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            User user = dataSnapshot.getValue(User.class);
            mCB.onUserLoaded(user);
        }

        @Override
        public void onCancelled(DatabaseError dberr) {
            mCB.onUserLoaded(null);
        }
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
            } else {
                Log.d(Tag, "Creating new user");
                // user doesn't exist, now create new user
                mUser = new User();
                mUser.setEmail(mEmail);
                UpdateUser();
            }
            ShowChecklistDrawer();
        }

        @Override
        public void onCancelled(DatabaseError dbErr) {
            Log.w(Tag, "User retrieval failed", dbErr.toException());
            Toast.makeText(getApplicationContext(), "Failed to load user",
                    Toast.LENGTH_SHORT).show();
        }
    }
}
