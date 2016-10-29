package net.piaw.sharedchecklist;

import android.app.Activity;
import android.content.Context;
import android.text.Spannable;
import android.text.Spanned;
import android.text.style.StrikethroughSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by piaw on 9/23/2016.
 */

public class ChecklistAdapter extends BaseAdapter {
    private static final StrikethroughSpan STRIKE_THROUGH_SPAN = new StrikethroughSpan();
    public final String Tag = "ChecklistAdapter";
    boolean new_entry = false;
    private Checklist mChecklist;
    private Activity mActivity;
    private Settings mSettings;
    private ArrayList<ChecklistAdapterItem> mShownItems;

    public ChecklistAdapter(Activity activity, Checklist checklist) {
        mSettings = Settings.getInstance(activity);
        mChecklist = checklist;
        mActivity = activity;
        notifyDataSetChanged();
    }

    public void addNewChecklistItem() {
        new_entry = true;
        notifyDataSetChanged();
    }

    @Override
    public void notifyDataSetChanged() {
        mShownItems = new ArrayList<>();

        // shallow-copy all checklist items into the array list
        // if hide is checked is set, then we filter out the ones
        // that shouldn't be shown at this point
        for (int i = 0; i < mChecklist.getItems().size(); ++i) {
            ChecklistItem item = mChecklist.getItems().get(i);
            if (mSettings.isHideIfChecked()) {
                if (!item.isChecked()) {
                    mShownItems.add(new ChecklistAdapterItem(item));
                }
            } else {
                // add everything
                mShownItems.add(new ChecklistAdapterItem(item));
            }
        }

        if (new_entry) {
            ChecklistAdapterItem item = new ChecklistAdapterItem(true);
            mShownItems.add(item);
            new_entry = false;
        }
        super.notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mShownItems.size();
    }

    private ChecklistItem fetchItemAt(int i) {
        return mShownItems.get(i).getItem();
    }

    public Object getItem(int pos) {
        return null;
    }

    public long getItemId(int position) {
        return 0;
    }

    public View getView(int pos, View view, ViewGroup parent) {
        view = ((LayoutInflater) mActivity.getApplicationContext().getSystemService(
                Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.list_items,
                null);
        final CheckedTextView simpleCheckedTextView =
                (CheckedTextView) view.findViewById(R.id.simpleCheckedTextView);

        if (mShownItems.get(pos).isNewEntry()) {
            // create a new edit text view
            view = ((LayoutInflater) mActivity.getApplicationContext().getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.list_edititem,
                    null);
            final EditText new_entry = (EditText) view.findViewById(R.id.editTextView);
            Button done = (Button) view.findViewById(R.id.doneButton);
            done.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.v(Tag, "AddNewChecklistItem");
                    String new_checklist_label = new_entry.getText().toString();
                    Log.v(Tag, "New label:" + new_checklist_label);
                    ChecklistItem item = new ChecklistItem();
                    item.setLabel(new_checklist_label);
                    item.setCreator(Database.getDB().getEmail());
                    Database.getDB().AddChecklistItem(mChecklist, item);
                    InputMethodManager imm =
                            (InputMethodManager) mActivity.getSystemService(
                                    Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            });
            mShownItems.get(pos).setEditText(new_entry);
            new_entry.requestFocus();
            InputMethodManager imm =
                    (InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,
                    InputMethodManager.HIDE_IMPLICIT_ONLY);
            return view;
        } else if (mShownItems.get(pos).isEditEntry()) {
            // everything already set in long click listener
            return mShownItems.get(pos).getEditView();
        }
        String label = fetchItemAt(pos).getLabel();
        simpleCheckedTextView.setText(label);
        if (fetchItemAt(pos).isChecked() && mSettings.isStrikethroughIfChecked()) {
            makeTextStrikethrough(simpleCheckedTextView, label);
        }

        // draw the checkmark
        if (fetchItemAt(pos).isChecked()) {
            simpleCheckedTextView.setCheckMarkDrawable(R.drawable.checked);
            simpleCheckedTextView.setChecked(true);
        }
        simpleCheckedTextView.setOnClickListener(
                new PosBasedOnClickListener(simpleCheckedTextView, pos));
        simpleCheckedTextView.setOnLongClickListener(
                new PosBasedOnLongClickListener(simpleCheckedTextView, pos));

        return view;
    }

    private void makeTextStrikethrough(CheckedTextView simpleCheckedTextView, String label) {
        simpleCheckedTextView.setText(label, TextView.BufferType.SPANNABLE);
        Spannable spannable = (Spannable) simpleCheckedTextView.getText();
        spannable.setSpan(STRIKE_THROUGH_SPAN, 0, label.length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    class ChecklistAdapterItem {
        ChecklistItem item;
        EditText new_entry;
        View edit_view;
        boolean isNewEntry;
        boolean isEditEntry;

        public ChecklistAdapterItem(ChecklistItem item) {
            this.item = item;
            edit_view = null;
            isNewEntry = false;
            isEditEntry = false;
        }

        public ChecklistAdapterItem(boolean isNewEntry) {
            this.isNewEntry = isNewEntry;
        }

        public ChecklistItem getItem() {
            return item;
        }

        public boolean isNewEntry() {
            return isNewEntry;
        }

        public boolean isEditEntry() {
            return isEditEntry;
        }

        public EditText getEditText() {
            return this.new_entry;
        }

        public void setEditText(EditText editText) {
            this.new_entry = editText;
        }

        public View getEditView() {
            return edit_view;
        }

        public void setEditView(View v) {
            edit_view = v;
        }

        public void setEditEntry() {
            isEditEntry = true;
        }
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
                // set check mark drawable and set checked property to false
                fetchItemAt(mPos).setChecked(false);
                simpleCheckedTextView.setCheckMarkDrawable(0);
                simpleCheckedTextView.setChecked(false);
                // reset so that strike through is gone
                String label = simpleCheckedTextView.getText().toString();
                simpleCheckedTextView.setText(label);
            } else {
                // set check mark drawable and set checked property to true
                fetchItemAt(mPos).setChecked(true);
                simpleCheckedTextView.setCheckMarkDrawable(R.drawable.checked);
                simpleCheckedTextView.setChecked(true);
                if (mSettings.isStrikethroughIfChecked()) {
                    String label = simpleCheckedTextView.getText().toString();
                    makeTextStrikethrough(simpleCheckedTextView, label);
                }
                Database.getDB().UpdateChecklist(mChecklist);
            }
        }
    }


    class PosBasedOnLongClickListener implements CheckedTextView.OnLongClickListener {
        int mPos;
        View mView;
        ChecklistAdapterItem mItem;

        PosBasedOnLongClickListener(View view, int pos) {
            mPos = pos;
            mView = view;
            mItem = mShownItems.get(mPos);
        }

        @Override
        public boolean onLongClick(View v) {
            CheckedTextView ctv = (CheckedTextView) v;

            View view = ((LayoutInflater) mActivity.getApplicationContext().getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.list_edititem,
                    null);
            final EditText new_entry = (EditText) view.findViewById(R.id.editTextView);
            new_entry.setText(ctv.getText());
            Button done = (Button) view.findViewById(R.id.doneButton);
            done.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.v(Tag, "EditChecklistItem");
                    String new_checklist_label = new_entry.getText().toString();
                    Log.v(Tag, "New label:" + new_checklist_label);
                    mItem.getItem().setLabel(new_checklist_label);
                    Database.getDB().UpdateChecklist(mChecklist);
                    InputMethodManager imm =
                            (InputMethodManager) mActivity.getSystemService(
                                    Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            });
            mShownItems.get(mPos).setEditView(view);
            mShownItems.get(mPos).setEditEntry();
            notifyDataSetChanged();
            new_entry.requestFocus();
            InputMethodManager imm =
                    (InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,
                    InputMethodManager.HIDE_IMPLICIT_ONLY);
            return true;
        }
    }
}
