package net.piaw.sharedchecklist;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by piaw on 9/28/2016.
 */

public class ManageChecklistsAdapter extends BaseAdapter {
    public final String Tag = "ManageCLAdapter";
    Activity mActivity;
    ArrayList<Checklist> mChecklists;
    Checklist mCurrentSelected;

    ManageChecklistsAdapter(Activity activity, ArrayList<Checklist> checklists) {
        mActivity = activity;
        mChecklists = checklists;
        mCurrentSelected = null;
    }

    @Override
    public int getCount() {
        return mChecklists.size();
    }

    private String unEscapeEmailAddress(String email) {
        // Replace ',' (not allowed in a Firebase key) with '.' (not allowed in an email address)
        return email.toLowerCase().replaceAll(",", "\\.");
    }

    private Checklist fetchChecklistAt(int i) {
        return mChecklists.get(i);
    }

    public Checklist getCurrentSelected() {
        return mCurrentSelected;
    }

    public Object getItem(int pos) {
        return null;
    }

    public long getItemId(int position) {
        return 0;
    }

    public View getView(int pos, View view, final ViewGroup parent) {
        Log.v(Tag, "getView:" + pos);
        if (view == null) {
            view = mActivity.getLayoutInflater().inflate(R.layout.manage_checklists_multi, null);
        }
        TextView cl_name = (TextView) view.findViewById(R.id.cl_name);
        TextView cl_owner = (TextView) view.findViewById(R.id.cl_owner);
        TextView cl_num_entries = (TextView) view.findViewById(R.id.cl_num_entries);
        Checklist cl = fetchChecklistAt(pos);
        cl_name.setText(cl.getChecklist_name());
        cl_owner.setText(unEscapeEmailAddress(cl.getOwner()));
        cl_num_entries.setText(Integer.toString(cl.getItems().size()));
        if (mCurrentSelected == cl) {
            view.setBackgroundColor(Color.YELLOW);
        } else {
            view.setBackgroundColor(Color.WHITE);
        }
        view.setTag(cl);
        view.setLongClickable(true);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setSelected(true);
                mCurrentSelected = (Checklist) v.getTag();
                ManageChecklistsAdapter.this.notifyDataSetChanged();
            }
        });
        view.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Checklist cl = (Checklist) v.getTag();
                Intent intent = new Intent(mActivity, ChecklistDisplay.class);
                intent.putExtra("checklist", cl);
                mActivity.startActivity(intent);
                return true;
            }
        });
        return view;
    }
}
