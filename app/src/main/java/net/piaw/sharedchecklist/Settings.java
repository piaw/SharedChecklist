package net.piaw.sharedchecklist;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by piaw on 9/27/2016.
 */

public class Settings {
    private static Settings mInstance = null;
    private boolean mHideIfChecked = false;
    private boolean mStrikethroughIfChecked = false;
    private SharedPreferences mSharedPref;

    private Settings(Activity activity) {
        mSharedPref = activity.getSharedPreferences("settings", Context.MODE_PRIVATE);
        setHideIfChecked(mSharedPref.getBoolean("hide-if-checked", isHideIfChecked()));
        setStrikethroughIfChecked(mSharedPref.getBoolean("strikethrough-if-checked",
                isStrikethroughIfChecked()));
    }

    public static Settings getInstance(Activity activity) {
        if (mInstance == null) {
            mInstance = new Settings(activity);
        }
        return mInstance;
    }

    public boolean isHideIfChecked() {
        return mHideIfChecked;
    }

    public void setHideIfChecked(boolean mHideIfChecked) {
        this.mHideIfChecked = mHideIfChecked;
    }

    public boolean isStrikethroughIfChecked() {
        return mStrikethroughIfChecked;
    }

    public void setStrikethroughIfChecked(boolean mStrikethroughIfChecked) {
        this.mStrikethroughIfChecked = mStrikethroughIfChecked;
    }

    public void Save() {
        SharedPreferences.Editor editor = mSharedPref.edit();
        editor.putBoolean("hide-if-checked", isHideIfChecked());
        editor.putBoolean("strikethrough-if-checked", isStrikethroughIfChecked());
        editor.apply();
    }

}
