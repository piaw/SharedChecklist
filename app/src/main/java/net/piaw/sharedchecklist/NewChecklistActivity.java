package net.piaw.sharedchecklist;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

public class NewChecklistActivity extends AppCompatActivity {
    EditText mChecklistName;
    CheckBox mMakeDefault;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_checklist);
        mChecklistName = (EditText) findViewById(R.id.new_checklist_name);
        mMakeDefault = (CheckBox) findViewById(R.id.makeDefault);
        Button save_button = (Button) findViewById(R.id.save_checklist);
        save_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String new_cl_name = mChecklistName.getText().toString();
                Checklist cl = new Checklist();
                cl.setChecklist_name(new_cl_name);
                cl.setOwner(Database.getDB().getEmail());
                cl.setCreator(Database.getDB().getEmail());
                cl.addAcl(Database.getDB().getEmail());
                Database.getDB().CreateChecklist(cl);
                if (mMakeDefault.isChecked()) {
                    Database.getDB().UpdateDefaultChecklist(cl);
                }
                finish();
                InputMethodManager imm =
                        (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
            }
        });
        mChecklistName.requestFocus();
        InputMethodManager imm =
                (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
    }
}
