package net.piaw.sharedchecklist;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckedTextView;

import java.util.ArrayList;

/**
 * Created by piaw on 9/23/2016.
 */

public class ChecklistAdapter extends BaseAdapter {
    private ArrayList<ChecklistItem> mItems;
    private Context mContext;

    public ChecklistAdapter(Context context, ArrayList<ChecklistItem> items) {
        mItems = items;
        mContext = context;
    }

    @Override
    public int getCount() {
        return mItems.size();
    }

    public Object getItem(int pos) {
        return null;
    }

    public long getItemId(int position) {
        return 0;
    }

    public View getView(int pos, View view, ViewGroup parent) {
        view = ((LayoutInflater) mContext.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.list_items,
                null);
        final CheckedTextView simpleCheckedTextView =
                (CheckedTextView) view.findViewById(R.id.simpleCheckedTextView);
        simpleCheckedTextView.setText(mItems.get(pos).getLabel());
        simpleCheckedTextView.setOnClickListener(
                new PosBasedOnClickListener(simpleCheckedTextView, pos));
        return view;
    }

    class PosBasedOnClickListener implements CheckedTextView.OnClickListener {
        int mPos;
        View mView;

        PosBasedOnClickListener(View view, int pos) {
            mPos = pos;
            mView = view;
        }

        @Override
        public void onClick(View v) {
            CheckedTextView simpleCheckedTextView = (CheckedTextView) v;

            if (simpleCheckedTextView.isChecked()) {
                // set cheek mark drawable and set checked property to false
                mItems.get(mPos).setChecked(false);
                simpleCheckedTextView.setCheckMarkDrawable(0);
                simpleCheckedTextView.setChecked(false);
            } else {
                // set check mark drawable and set checked property to true
                mItems.get(mPos).setChecked(true);
                simpleCheckedTextView.setCheckMarkDrawable(R.drawable.checked);
                simpleCheckedTextView.setChecked(true);
            }
        }
    }
}
