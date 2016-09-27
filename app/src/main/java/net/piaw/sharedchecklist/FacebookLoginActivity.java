package net.piaw.sharedchecklist;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONObject;

import static com.facebook.internal.FacebookDialogFragment.TAG;

/**
 * Created by piaw on 9/21/2016.
 * This is a test
 */

public class FacebookLoginActivity extends Activity {
    final String Tag = "FacebookLoginActivity";
    private CallbackManager mCallbackManager;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private String mEmail;
    private String mId;

    protected void onCreate(Bundle savedInstanceData) {
        super.onCreate(savedInstanceData);

        FacebookSdk.sdkInitialize(getApplicationContext());
        mAuth = FirebaseAuth.getInstance();
        mCallbackManager = CallbackManager.Factory.create();
        setContentView(R.layout.facebook_login);
        Log.d(Tag, "Configuring LoginButton");
        final LoginButton loginButton = (LoginButton) findViewById(R.id.login_button);
        loginButton.setReadPermissions("email", "public_profile");
        loginButton.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(final LoginResult loginResult) {
                Log.d(Tag, "facebook:onSuccess:" + loginResult);
                getEmailAndIdGraphRequest(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Log.d(Tag, "facebook:onCancel");
                // ...
            }

            @Override
            public void onError(FacebookException error) {
                Log.d(Tag, "facebook:onError", error);
                // ...
            }
        });

        if (AccessToken.getCurrentAccessToken() != null &&
                !AccessToken.getCurrentAccessToken().getUserId().equals("")) {
            Log.d(Tag, "User already logged in!");
            GraphRequest request = getEmailAndIdGraphRequest(AccessToken.getCurrentAccessToken());
        }

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                    Log.d(Tag, "onAuthStateChanged:email:" + mEmail);
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
                // ...
            }
        };

    }

    private String escapeEmailAddress(String email) {
        // Replace '.' (not allowed in a Firebase key) with ',' (not allowed in an email address)
        return email.toLowerCase().replaceAll("\\.", ",");
    }

    private GraphRequest getEmailAndIdGraphRequest(final AccessToken accessToken) {
        GraphRequest request = GraphRequest.newMeRequest(
                accessToken, new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject me, GraphResponse response) {
                        if (response.getError() != null) {
                            Toast.makeText(FacebookLoginActivity.this, "Cannot retrieve user e-mail",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            mEmail = escapeEmailAddress(me.optString("email"));
                            mId = me.optString("id");
                            Log.d(Tag, "email:" + mEmail);
                            handleFacebookAccessToken(accessToken);
                            // send email and id to your web server
                        }
                    }
                });
        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,name,link,email,picture");
        request.setParameters(parameters);
        request.executeAsync();
        return request;
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode,
                                    final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    private void handleFacebookAccessToken(AccessToken token) {
        Log.d(TAG, "handleFacebookAccessToken:" + token);

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(Tag, "signInWithCredential:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Log.d(Tag, "signInWithCredential", task.getException());
                            Toast.makeText(FacebookLoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            Log.d(Tag, "sign in successful!");
                            Database db = new Database(mEmail, FacebookLoginActivity.this,
                                    // only show on fetch if import!
                                    !(FacebookLoginActivity.this.getIntent()
                                            .getBooleanExtra("import", false)));
                            Database.setDB(db);
                            if (getParent() == null) {
                                setResult(Activity.RESULT_OK);
                            } else {
                                getParent().setResult(Activity.RESULT_OK);
                            }
                            finish();
                        }

                        // ...
                    }
                });
    }
}
