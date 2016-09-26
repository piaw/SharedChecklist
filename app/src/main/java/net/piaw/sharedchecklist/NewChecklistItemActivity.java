package net.piaw.sharedchecklist;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class NewChecklistItemActivity extends AppCompatActivity {

    EditText mChecklistLabel;
    Checklist mChecklist;

    private void AddNewChecklistItem() {
        String new_checklist_label = mChecklistLabel.getText().toString();
        ChecklistItem item = new ChecklistItem();
        item.setLabel(new_checklist_label);
        item.setCreator(Database.getDB().getEmail());
        Database.getDB().AddChecklistItem(mChecklist, item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mChecklist = (Checklist) getIntent().getSerializableExtra("checklist");
        assert (mChecklist != null);
        setContentView(R.layout.activity_new_checklist_item);
        mChecklistLabel = (EditText) findViewById(R.id.new_checklist_item);
        mChecklistLabel.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                AddNewChecklistItem();
                return true;
            }
        });
        Button saveButton = (Button) findViewById(R.id.new_checklist_save);
        saveButton.setOnClickListener(new ButtonOnClickListener());
    }

    class ButtonOnClickListener implements View.OnClickListener {

        public void onClick(View view) {
            AddNewChecklistItem();
        }
    }


}
