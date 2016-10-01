package net.piaw.sharedchecklist;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

public class SettingsActivity extends AppCompatActivity {
    EditText mDefaultChecklist;
    CheckBox mHideIfChecked;
    CheckBox mStrikethroughIfChecked;
    Button mSetDefault;
    Button mSaveSettings;
    Button mCancelSettings;
    Checklist mChecklist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mChecklist = (Checklist) getIntent().getSerializableExtra("checklist");
        setContentView(R.layout.activity_settings);
        Settings prefs = Settings.getInstance(this);
        mDefaultChecklist = (EditText) findViewById(R.id.default_checklist);
        mHideIfChecked = (CheckBox) findViewById(R.id.hide_on_checked);
        mStrikethroughIfChecked = (CheckBox) findViewById(R.id.strikethrough_on_checked);
        mSetDefault = (Button) findViewById(R.id.set_default);
        mSaveSettings = (Button) findViewById(R.id.save_settings);
        mCancelSettings = (Button) findViewById(R.id.cancel_settings);

        mDefaultChecklist.setText(mChecklist.getId());
        mHideIfChecked.setChecked(Settings.getInstance(this).isHideIfChecked());
        mStrikethroughIfChecked.setChecked(Settings.getInstance(this).isStrikethroughIfChecked());
        mSetDefault.setOnClickListener(new SetDefaultChecklist());
        mSaveSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Settings settings = Settings.getInstance(SettingsActivity.this);
                settings.setHideIfChecked(mHideIfChecked.isChecked());
                settings.setStrikethroughIfChecked(mStrikethroughIfChecked.isChecked());
                settings.Save();
                finish();
            }
        });
        mCancelSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    class SetDefaultChecklist implements View.OnClickListener, Database.FetchChecklistCallback {
        @Override
        public void onChecklistLoaded(Checklist cl) {
            if (cl != null) {
                // valid checklist, set it up!
                Database.getDB().setDefaultChecklist(cl);
                Intent intent = new Intent(SettingsActivity.this, ChecklistDisplay.class);
                intent.putExtra("checklist", cl);
                startActivity(intent);
            } else {
                Toast.makeText(SettingsActivity.this, "Invalid ChecklistID",
                        Toast.LENGTH_SHORT).show();
                mDefaultChecklist.setText("");
            }
        }

        @Override
        public void onClick(View v) {
            String new_default_cl_id = mDefaultChecklist.getText().toString();

            Database.getDB().FetchChecklist(this, new_default_cl_id);
        }
    }
}
