package net.piaw.sharedchecklist;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class ViewPendingActivity extends AppCompatActivity {
    ListView cl_preview;
    TextView cl_name_view;
    Button accept;
    Button reject;
    Checklist mChecklist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_pending);
        mChecklist = (Checklist) getIntent().getSerializableExtra("checklist");
        View pending_cl_header = getLayoutInflater().inflate(R.layout.pending_lv_header, null);
        TextView header = (TextView) pending_cl_header.findViewById(R.id.pending_lv_header);
        header.setText(mChecklist.getChecklist_name());
        cl_preview = (ListView) findViewById(R.id.pending_cl_view);
        accept = (Button) findViewById(R.id.accept_share);
        reject = (Button) findViewById(R.id.reject_share);
        ArrayList<String> checklist_items = new ArrayList<>();
        for (int i = 0; i < mChecklist.getItems().size(); ++i) {
            checklist_items.add(mChecklist.getItems().get(i).getLabel());
        }
        cl_preview.addHeaderView(pending_cl_header);
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
