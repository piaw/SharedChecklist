package net.piaw.sharedchecklist;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;

public class SettingsActivity extends AppCompatActivity {
    CheckBox mHideIfChecked;
    CheckBox mStrikethroughIfChecked;
    Button mSaveSettings;
    Button mCancelSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Settings prefs = Settings.getInstance(this);
        mHideIfChecked = (CheckBox) findViewById(R.id.hide_on_checked);
        mStrikethroughIfChecked = (CheckBox) findViewById(R.id.strikethrough_on_checked);
        mSaveSettings = (Button) findViewById(R.id.save_settings);
        mCancelSettings = (Button) findViewById(R.id.cancel_settings);
        mHideIfChecked.setChecked(Settings.getInstance(this).isHideIfChecked());
        mStrikethroughIfChecked.setChecked(Settings.getInstance(this).isStrikethroughIfChecked());
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


}
