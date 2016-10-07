package net.piaw.sharedchecklist;

import android.app.Fragment;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.appinvite.AppInviteInvitation;

import java.util.ArrayList;

import static android.app.Activity.RESULT_OK;

/**
 * Created by piaw on 10/6/2016.
 */

public class ShareChecklistFragment extends Fragment {
    public final static int REQUEST_INVITE = 0;
    public final static int CONTACT_PICKER_RESULT = 1;
    public final static String Tag = "ShareChecklistFragment";

    Checklist mChecklist;
    ListView mLV;
    Button mBrowseContacts;
    Button mShare;
    EditText mEmailEntry;

    public ShareChecklistFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_share_checklist, container, false);
    }

    private void refreshmACLView() {
        ArrayList<String> converted_emails = new ArrayList<>();
        for (int i = 0; i < mChecklist.getAcl().size(); i++) {
            converted_emails.add(Database.unEscapeEmailAddress(mChecklist.getAcl().get(i)));
        }
        mLV.setAdapter(new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1,
                converted_emails));
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Toolbar checklistToolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(checklistToolbar);
        mChecklist = (Checklist) getArguments().getSerializable("checklist");
        checklistToolbar.setTitle(mChecklist.getChecklist_name());
        mBrowseContacts = (Button) getActivity().findViewById(R.id.browse_contacts);
        mShare = (Button) getActivity().findViewById(R.id.share_button);
        mEmailEntry = (EditText) getActivity().findViewById(R.id.sharee_email);
        mLV = (ListView) getActivity().findViewById(R.id.acl_view);
        mShare.setOnClickListener(new ShareButtonClicked());
        mBrowseContacts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LaunchContactPicker(v);
            }
        });
    }

    private void LaunchContactPicker(View view) {
        Intent contactPickerIntent = new Intent(Intent.ACTION_PICK,
                ContactsContract.Contacts.CONTENT_URI);
        startActivityForResult(contactPickerIntent, CONTACT_PICKER_RESULT);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.v(Tag, "onActivityResult");
        if (resultCode == RESULT_OK) {
            if (requestCode == CONTACT_PICKER_RESULT) {
                Uri result = data.getData();
                // query for everything email
                Cursor cursor = null;
                try {
                    Log.v(Tag, "performing query");
                    String id = result.getLastPathSegment();
                    String[] selectionArgs = {id};
                    cursor = getActivity().getContentResolver().query(
                            ContactsContract.CommonDataKinds.Email.CONTENT_URI, null,
                            ContactsContract.CommonDataKinds.Email.CONTACT_ID + "=?",
                            selectionArgs, null);
                    if (cursor.moveToFirst()) {
                        Log.v(Tag, "found it!");
                        int emailIdx =
                                cursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA);
                        String email = cursor.getString(emailIdx);
                        Log.v(Tag, "Got email: " + email);
                        mEmailEntry.setText(email);
                    }
                } finally {
                    if (cursor != null) cursor.close();
                }
            }
        }
    }

    class ShareButtonClicked implements View.OnClickListener, Database.FetchUserCallback {
        String mEmail;

        @Override
        public void onClick(View v) {
            mEmail = Database.escapeEmailAddress(mEmailEntry.getText().toString());
            Database.getDB().FetchUser(this, mEmail);
        }

        public void onUserLoaded(User user) {
            if (user == null) {
                Toast.makeText(getActivity(), "No user " + mEmail,
                        Toast.LENGTH_SHORT).show();
                Intent intent = new AppInviteInvitation.IntentBuilder("Please use ShareChecklist")
                        .setMessage("I have a checklist to share with you. Please install " +
                                "the app ShareChecklist so we can share it.")
                        .build();
                startActivityForResult(intent, REQUEST_INVITE);
                getFragmentManager().popBackStackImmediate();
                return;
            }

            // user exists, add that to the acl, then add the checklist to the pending list
            if (!mChecklist.getAcl().contains(mEmail)) {
                mChecklist.getAcl().add(mEmail);
                Database.getDB().UpdateChecklist(mChecklist);
                user.addPending_checklist(mChecklist.getId());
                Database.getDB().UpdateUser(user);
                refreshmACLView();
            }
            getFragmentManager().popBackStackImmediate();
        }
    }
}
