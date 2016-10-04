package net.piaw.sharedchecklist;

import android.app.Activity;
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
    View.OnLongClickListener longClickListener;
    View.OnClickListener clickListener;

    ManageChecklistsAdapter(Activity activity, ArrayList<Checklist> checklists,
                            View.OnClickListener clicklistener,
                            View.OnLongClickListener longClickListener) {
        mActivity = activity;
        mChecklists = checklists;
        this.longClickListener = longClickListener;
        this.clickListener = clicklistener;
    }

    public Checklist getCurrentSelected() {
        return null;
    }

    @Override
    public int getCount() {
        return mChecklists.size();
    }

    private Checklist fetchChecklistAt(int i) {
        return mChecklists.get(i);
    }

    public Object getItem(int pos) {
        return fetchChecklistAt(pos);
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
        cl_owner.setText(Database.unEscapeEmailAddress(cl.getOwner()));
        cl_num_entries.setText(Integer.toString(cl.getItems().size()));
        view.setTag(cl);
        view.setLongClickable(true);
        view.setOnClickListener(clickListener);
        view.setOnLongClickListener(longClickListener);
        return view;
    }
}
