package net.piaw.sharedchecklist;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ManagePendingActivity extends Fragment implements Database.FetchChecklistCallback,
        ValueEventListener {
    public static final String Tag = "ManagePendingActivity";
    public static final int ACCEPT_OR_REJECT = 0;
    ArrayList<Checklist> mChecklists;
    ListView mLV;
    ManageChecklistsAdapter mAdapter;
    User mUser;

    public ManagePendingActivity() {
        // empty
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_manage_pending, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getActivity().setTitle("Manage Shared Checklists");
        mChecklists = new ArrayList<>();
        mUser = Database.getDB().getUser();
        mLV = (ListView) getView().findViewById(R.id.pending_view);
        mAdapter = new ManageChecklistsAdapter(getActivity(), mChecklists,
                new StartViewPendingActivity(),
                new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                // long clicks don't do anything
                return true;
            }
                      });
        mLV.setAdapter(mAdapter);
        refreshChecklists();
        AdView mAdView = (AdView) getView().findViewById(R.id.adView2);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
    }

    private void refreshChecklists() {
        mChecklists = new ArrayList<>();
        // fetch all checklists
        User user = Database.getDB().getUser();

        if (user.getPending_checklists().size() == 0) {
            Checklist cl = new Checklist();
            cl.setChecklist_name("No pending checklists");
            mChecklists.add(cl);
            mAdapter.notifyDataSetChanged();
            return;
        }

        Log.v(Tag, "getting checklists for user:" + user.getEmail());
        for (int i = 0; i < user.getPending_checklists().size(); ++i) {
            Log.v(Tag, "fetching:" + user.getPending_checklists().get(i));
            Database.getDB().FetchChecklist(this, user.getPending_checklists().get(i));
        }
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
        Toast.makeText(getActivity(), "Database error!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onChecklistLoaded(Checklist cl) {
        Log.v(Tag, "onChecklist Loaded!");
        if (cl != null) {
            Log.v(Tag, "adding checklist" + cl.getId());
            synchronized (mChecklists) {
                mChecklists.add(cl);
            }
            mAdapter = new ManageChecklistsAdapter(getActivity(), mChecklists,
                    new StartViewPendingActivity(),
                    new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View v) {
                            // long clicks don't do anything
                            return true;
                        }
                    });
            mLV.setAdapter(mAdapter);
        }
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode,
                                 final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // refresh
        refreshChecklists();
    }

    class StartViewPendingActivity implements View.OnClickListener {
        public void onClick(View v) {
            Checklist cl = (Checklist) v.getTag();
            Fragment fragment = new ViewPendingActivity();
            Bundle args = new Bundle();
            args.putSerializable("checklist", cl);
            fragment.setArguments(args);
            getFragmentManager().beginTransaction()
                    .replace(R.id.content_view, fragment)
                    .addToBackStack("manage pending")
                    .commit();
        }
    }
}
