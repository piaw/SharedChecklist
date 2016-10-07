package net.piaw.sharedchecklist;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class ViewPendingActivity extends Fragment {
    ListView cl_preview;
    TextView cl_name_view;
    Button accept;
    Button reject;
    Checklist mChecklist;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_view_pending, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mChecklist = (Checklist) getArguments().getSerializable("checklist");
        getActivity().setTitle("Accept/Reject Shared Checklist");
        cl_preview = (ListView) getView().findViewById(R.id.pending_cl_view);
        accept = (Button) getView().findViewById(R.id.accept_share);
        reject = (Button) getView().findViewById(R.id.reject_share);
        ArrayList<String> checklist_items = new ArrayList<>();
        for (int i = 0; i < mChecklist.getItems().size(); ++i) {
            checklist_items.add(mChecklist.getItems().get(i).getLabel());
        }
        cl_preview.setAdapter(new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_list_item_1, checklist_items));
        accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Database.getDB().getUser().addChecklist(mChecklist.getId());
                Database.getDB().getUser().getPending_checklists().remove(mChecklist.getId());
                Database.getDB().UpdateUser();
                getActivity().finish();
            }
        });

        reject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Database.getDB().getUser().getPending_checklists().remove(mChecklist.getId());
                Database.getDB().UpdateUser();
                getActivity().finish();
            }
        });
    }
}
