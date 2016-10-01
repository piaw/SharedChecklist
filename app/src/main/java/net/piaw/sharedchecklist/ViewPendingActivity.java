package net.piaw.sharedchecklist;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;

public class ViewPendingActivity extends AppCompatActivity {
    ListView cl_preview;
    Button accept;
    Button reject;
    Checklist mChecklist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_pending);
        mChecklist = (Checklist) getIntent().getSerializableExtra("checklist");
        cl_preview = (ListView) findViewById(R.id.pending_cl_view);
        accept = (Button) findViewById(R.id.accept_share);
        reject = (Button) findViewById(R.id.reject_share);
        ArrayList<String> checklist_items = new ArrayList<>();
        for (int i = 0; i < mChecklist.getItems().size(); ++i) {
            checklist_items.add(mChecklist.getItems().get(i).getLabel());
        }
        cl_preview.setAdapter(new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, checklist_items));
        accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Database.getDB().getUser().addChecklist(mChecklist.getId());
                Database.getDB().getUser().getPending_checklists().remove(mChecklist.getId());
                Database.getDB().UpdateUser();
                setResult(ManagePendingActivity.ACCEPT_OR_REJECT);
                finish();
            }
        });

        reject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Database.getDB().getUser().getPending_checklists().remove(mChecklist.getId());
                Database.getDB().UpdateUser();
                setResult(ManagePendingActivity.ACCEPT_OR_REJECT);
                finish();
            }
        });
    }
}
