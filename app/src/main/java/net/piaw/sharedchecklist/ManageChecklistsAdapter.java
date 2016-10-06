package net.piaw.sharedchecklist;

import android.app.Activity;
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
    ArrayList<ManageChecklistItem> mChecklistItems;
    ArrayList<Checklist> mChecklists;
    View.OnLongClickListener longClickListener;
    View.OnClickListener clickListener;
    int mBGColor;

    ManageChecklistsAdapter(Activity activity, ArrayList<Checklist> checklists,
                            View.OnClickListener clicklistener,
                            View.OnLongClickListener longClickListener,
                            int bgColor) {
        mActivity = activity;
        mChecklistItems = new ArrayList<>();
        mChecklists = checklists;
        mBGColor = bgColor;
        for (int i = 0; i < mChecklists.size(); ++i) {
            mChecklistItems.add(new ManageChecklistItem(mChecklists.get(i)));
        }
        this.longClickListener = longClickListener;
        this.clickListener = clicklistener;
    }

    ManageChecklistsAdapter(Activity activity, ArrayList<Checklist> checklists,
                            View.OnClickListener clicklistener,
                            View.OnLongClickListener longClickListener) {
        this(activity, checklists, clicklistener, longClickListener, Color.WHITE);
    }

    public void addMenuItem(String menuItem, View.OnClickListener listener) {
        ManageChecklistItem new_item = new ManageChecklistItem(menuItem, listener);

        // don't add if already exists
        if (!mChecklistItems.contains(new_item)) {
            mChecklistItems.add(new ManageChecklistItem(menuItem, listener));
            notifyDataSetChanged();
        }
    }

    public Checklist getCurrentSelected() {
        return null;
    }

    @Override
    public int getCount() {
        return mChecklistItems.size();
    }

    private ManageChecklistItem fetchItemAt(int i) {
        return mChecklistItems.get(i);
    }

    public Object getItem(int pos) {
        return fetchItemAt(pos);
    }

    public long getItemId(int position) {
        return 0;
    }

    public View getView(int pos, View view, final ViewGroup parent) {
        Log.v(Tag, "getView:" + pos);
        ManageChecklistItem item = fetchItemAt(pos);
        if (item.isMenu) {
            view = mActivity.getLayoutInflater().inflate(R.layout.checklist_menu_item, null);
            TextView tv = (TextView) view.findViewById(R.id.checklist_menu_item);
            tv.setText(item.getMenuString());
            view.setBackgroundColor(mBGColor);
            view.setOnClickListener(item.getMenuListener());
            return view;
        }

        // else
        view = mActivity.getLayoutInflater().inflate(R.layout.manage_checklists_multi, null);
        Checklist cl = item.getChecklist();
        TextView cl_name = (TextView) view.findViewById(R.id.cl_name);
        TextView cl_owner = (TextView) view.findViewById(R.id.cl_owner);
        TextView cl_num_entries = (TextView) view.findViewById(R.id.cl_num_entries);
        cl_name.setText(cl.getChecklist_name());
        cl_owner.setText(Database.unEscapeEmailAddress(cl.getOwner()));
        cl_num_entries.setText(Integer.toString(cl.getItems().size()));
        view.setTag(cl);
        view.setLongClickable(true);
        view.setOnClickListener(clickListener);
        view.setOnLongClickListener(longClickListener);
        view.setBackgroundColor(mBGColor);
        return view;
    }

    class ManageChecklistItem {
        boolean isMenu;
        String menuString;
        View.OnClickListener menuListener;
        Checklist cl;

        ManageChecklistItem(Checklist cl) {
            isMenu = false;
            this.cl = cl;
        }

        ManageChecklistItem(String menuString, View.OnClickListener listener) {
            isMenu = true;
            this.menuString = menuString;
            menuListener = listener;
        }

        Checklist getChecklist() {
            return cl;
        }

        String getMenuString() {
            return menuString;
        }

        View.OnClickListener getMenuListener() {
            return menuListener;
        }
    }
}
