package net.piaw.sharedchecklist;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

@SuppressLint("ValidFragment")
public class ChecklistDisplay extends Fragment implements ValueEventListener,
        Database.SharedChecklistCallback, Database.FetchChecklistCallback {
    private static final int MENU_MERGE_ITEM = 100;
    public final int DISPLAY_SETTINGS = 0;
    final String Tag = "ChecklistDisplay";
    Checklist mChecklist;
    ListView mLV;
    ChecklistAdapter mAdapter;
    ShareActionProvider mShareActionProvider;

    public ChecklistDisplay() {
    }

    @Override
    public void onSharedChecklist(String checklistid) {
        // fetch checklist
        Database.getDB().FetchChecklist(this, checklistid);
    }

    public void onChecklistLoaded(Checklist cl) {
        if (cl == null) return;
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(getActivity())
                        .setAutoCancel(true)
                        .setSmallIcon(R.drawable.notify)
                        .setContentTitle("SharedChecklist")
                        .setContentText(cl.getChecklist_name() + "(" +
                                Database.unEscapeEmailAddress(cl.getOwner()) + ")" + " shared.");
        Intent resultIntent = new Intent(getActivity(), ChecklistDrawerActivity.class);
        resultIntent.putExtra("checklist", cl);
        TaskStackBuilder stackbuilder = TaskStackBuilder.create(getActivity());
        stackbuilder.addParentStack(ChecklistDrawerActivity.class);
        stackbuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent = stackbuilder.getPendingIntent(0,
                PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(resultPendingIntent);
        NotificationManager notification_manager =
                (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
        notification_manager.notify(0, builder.build());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        return inflater.inflate(R.layout.activity_checklist_display, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (Database.getDB() == null) {
            Intent intent = new Intent(getActivity(), FacebookLoginActivity.class);
            startActivity(intent);
            return;
        }

        Toolbar checklistToolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(checklistToolbar);
        mChecklist = (Checklist) getArguments().getSerializable("checklist");
        mLV = (ListView) getActivity().findViewById(R.id.checklistview);
        mLV.setItemsCanFocus(true);
        checklistToolbar.setTitle(mChecklist.getChecklist_name());
        if (BuildConfig.DEBUG && mChecklist == null) {
            throw new RuntimeException("ASSERTION FAILED: mChecklist is NULL!");
        }

        mLV.setAdapter(mAdapter = new ChecklistAdapter(getActivity(), mChecklist));
        Database.getDB().SharedChecklistNotificationOn(this);
        Database.getDB().getChecklistDB().child(mChecklist.getId())
                .addValueEventListener(this);

        AdView mAdView = (AdView) getActivity().findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
    }

    @Override
    public void onDataChange(DataSnapshot snapshot) {
        Log.v(Tag, "onDataChange!");
        mChecklist = snapshot.getValue(Checklist.class);
        if (mChecklist == null) return; // checklist was deleted!
        Log.v(Tag, "setting adapter!");
        mLV.setAdapter(mAdapter = new ChecklistAdapter(getActivity(), mChecklist));
    }

    public void onCancelled(DatabaseError dberr) {
        Log.v(Tag, "onCancelled");
        Toast.makeText(getActivity(), "Database error!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (mChecklist == null) return; // yikes!
        inflater.inflate(R.menu.menu_checklist_display, menu);
        if (mChecklist.getId().equals("")) {
            Log.e(Tag, "checklist id is null!");
            Toast.makeText(getActivity(), "Checklist is Corrupt!", Toast.LENGTH_LONG).show();
            return;
        }

        String lhs = mChecklist.getOwner();
        String rhs = Database.getDB().getEmail();

        if (lhs.equals(rhs)) {
            // add merge icon
            menu.add(Menu.NONE, MENU_MERGE_ITEM, Menu.NONE, "merge").setIcon(R.drawable.merge);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // might as well refresh everything
        mLV.setAdapter(mAdapter = new ChecklistAdapter(getActivity(), mChecklist));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {


            case R.id.action_add:
                // Add a new checklist
                mAdapter.addNewChecklistItem();
                return true;

            case R.id.action_copy:
                new CopyChecklistDialogFragment().show(getFragmentManager(), "Confirm");
                return true;

            case R.id.action_delete:
                new DeleteChecklistDialogFragment().show(getFragmentManager(), "Confirm");
                return true;

            case R.id.action_share:
                Fragment fragment = new ShareChecklistFragment();
                Bundle args = new Bundle();
                args.putSerializable("checklist", mChecklist);
                fragment.setArguments(args);
                getFragmentManager().beginTransaction()
                        .replace(R.id.content_view, fragment)
                        .addToBackStack("share checklist")
                        .commit();
                return true;

            case MENU_MERGE_ITEM:
                fragment = new MergeActivityFragment();
                args = new Bundle();
                args.putSerializable("checklist", mChecklist);
                fragment.setArguments(args);
                getFragmentManager().beginTransaction()
                        .replace(R.id.content_view, fragment)
                        .addToBackStack("merge checklist")
                        .commit();
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    public class CopyChecklistDialogFragment extends DialogFragment {
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage("Copy Checklist?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            Checklist copy = mChecklist.DeepCopy();
                            String email = Database.getDB().getEmail();
                            copy.setOwner(email);
                            copy.setCreator(email);
                            copy = Database.getDB().CreateChecklist(copy);
                            DrawerLayout drawer =
                                    (DrawerLayout) getActivity().findViewById(R.id.drawer_layout);
                            drawer.openDrawer(GravityCompat.START);

                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                        }
                    });
            // Create the AlertDialog object and return it
            return builder.create();
        }
    }

    public class DeleteChecklistDialogFragment extends DialogFragment {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the Builder class for convenient dialog construction
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage("Delete entire checklist?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            User user = Database.getDB().getUser();
                            if (!mChecklist.getOwner().equals(user.getEmail())) {
                                // just remove it from the user's checklists but do not
                                // delete the checklist itself from the database
                                user.getChecklists().remove(mChecklist.getId());
                                Database.getDB().UpdateUser();
                                DrawerLayout drawer =
                                        (DrawerLayout) getActivity().findViewById(R.id.drawer_layout);
                                drawer.openDrawer(GravityCompat.START);
                                getActivity().getFragmentManager().beginTransaction()
                                        .remove(ChecklistDisplay.this).commit();
                                return;
                            }
                            user.getChecklists().remove(mChecklist.getId());
                            Database.getDB().UpdateUser();
                            Database.getDB().DeleteChecklist(mChecklist);
                            DrawerLayout drawer =
                                    (DrawerLayout) getActivity().findViewById(R.id.drawer_layout);
                            drawer.openDrawer(GravityCompat.START);
                            getActivity().getFragmentManager().beginTransaction()
                                    .remove(ChecklistDisplay.this).commit();

                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                        }
                    });
            // Create the AlertDialog object and return it
            return builder.create();
        }
    }
}
