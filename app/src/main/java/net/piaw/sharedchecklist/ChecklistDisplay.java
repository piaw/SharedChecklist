package net.piaw.sharedchecklist;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ListView;

public class ChecklistDisplay extends AppCompatActivity {
    final String Tag = "ChecklistDisplay";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checklist_display);
        ListView lv = (ListView) findViewById(R.id.checklistview);
        ChecklistItem[] dummy = new ChecklistItem[3];
        lv.setAdapter(new ChecklistAdapter(getBaseContext(),
                ChecklistItem.DummyTestItemData.toArray(dummy)));
    }
}
