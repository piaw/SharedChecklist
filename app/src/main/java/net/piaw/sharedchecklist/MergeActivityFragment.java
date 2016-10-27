package net.piaw.sharedchecklist;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

public class MergeActivityFragment extends Fragment implements Database.FetchChecklistCallback {
    public final String Tag = "MergeActivityFragment";
    Checklist mChecklist;
    Checklist mCLToMerge;
    ArrayList<Checklist> mChecklists;
    ManageChecklistsAdapter mCLAdapter;
    Button mergeButton;
    ListView mergeChecklistView;
    ListView mergePreview;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_merge, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mCLToMerge = null;
        Toolbar checklistToolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(checklistToolbar);
        mChecklist = (Checklist) getArguments().getSerializable("checklist");
        checklistToolbar.setTitle("Merge:" + mChecklist.getChecklist_name());
        mergeButton = (Button) getActivity().findViewById(R.id.merge_button);
        mergeButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCLToMerge == null) {
                    Toast.makeText(getActivity(), "No changelist selected!", Toast.LENGTH_SHORT)
                            .show();
                    return;
                }

                for (int i = 0; i < mCLToMerge.getItems().size(); i++) {
                    mChecklist.addItem(mCLToMerge.getItems().get(i));
                }

                Database.getDB().UpdateChecklist(mChecklist);

                // this is ugly, but I couldn't get SupportManager.popbackstack to work!
                getActivity().onBackPressed();
            }
        });
        mergeChecklistView = (ListView) getActivity().findViewById(R.id.merge_checklist_view);
        mergePreview = (ListView) getActivity().findViewById(R.id.merge_preview);
        mChecklists = new ArrayList<Checklist>();
        mCLAdapter = new ManageChecklistsAdapter(getActivity(), mChecklists,
                new PreviewChecklist(),
                new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        // long clicks don't do anything
                        return true;
                    }
                });
        mergeChecklistView.setAdapter(mCLAdapter);
        refreshChecklists();
    }

    @Override
    public void onChecklistLoaded(Checklist cl) {
        Log.v(Tag, "onChecklist Loaded!");
        if (cl != null) {
            Log.v(Tag, "adding checklist" + cl.getId());
            synchronized (mChecklists) {
                mChecklists.add(cl);
            }
            mCLAdapter = new ManageChecklistsAdapter(getActivity(), mChecklists,
                    new PreviewChecklist(),
                    new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View v) {
                            // long clicks don't do anything
                            return true;
                        }
                    });
            mergeChecklistView.setAdapter(mCLAdapter);
            mCLAdapter.notifyDataSetChanged();
        }
    }

    private void refreshChecklists() {
        mChecklists = new ArrayList<>();
        // fetch all checklists
        User user = Database.getDB().getUser();

        if (user.getChecklists().size() == 0) {
            Checklist cl = new Checklist();
            cl.setChecklist_name("No checklists");
            mChecklists.add(cl);
            mCLAdapter.notifyDataSetChanged();
            return;
        }

        Log.v(Tag, "getting checklists for user:" + user.getEmail());
        for (int i = 0; i < user.getChecklists().size(); ++i) {
            String clid = user.getChecklists().get(i);

            if (!clid.equals(mChecklist.getId())) {
                Log.v(Tag, "fetching:" + user.getChecklists().get(i));
                Database.getDB().FetchChecklist(this, clid);
            }
        }
    }

    class PreviewChecklist implements View.OnClickListener,
            Database.FetchChecklistCallback {
        ChecklistAdapter mAdapter;

        @Override
        public void onClick(View v) {
            Checklist cl = (Checklist) v.getTag();
            mCLToMerge = cl;
            Database.getDB().FetchChecklist(this, cl.getId());
        }

        @Override
        public void onChecklistLoaded(Checklist cl) {
            Log.v(Tag, "onChecklist Loaded!");
            if (cl == null) return;
            Log.v(Tag, "adding checklist" + cl.getId());
            synchronized (mChecklists) {
                mChecklists.add(cl);
            }
            mAdapter = new ChecklistAdapter(getActivity(), cl);
            mergePreview.setAdapter(mAdapter);
            mAdapter.notifyDataSetChanged();
        }
    }
}
