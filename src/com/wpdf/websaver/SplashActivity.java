package com.wpdf.websaver;

import android.animation.Animator;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.wpdf.dbConfig.Dbcon;
import com.wpdf.dbConfig.Dbhelper;
import com.wpdf.libs.Utils;

public class SplashActivity extends AppCompatActivity implements
        GoogleApiClient.OnConnectionFailedListener, View.OnClickListener {


    private static final int RC_SIGN_IN = 1001;
    private static GoogleApiClient mGoogleApiClient = null;
    private static boolean isAuthenticated;
    private static String strAccount;
    private Dbcon db = null;
    private SignInButton signInButton;
    private Button buttonSkip;
    private TextView textViewUserName;
    private FirebaseAuth mAuth;
    private ProgressDialog mProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_splash);

        db = new Dbcon(this);
        mAuth = FirebaseAuth.getInstance();
        mProgress = new ProgressDialog(SplashActivity.this);

        // Set the dimensions of the sign-in button.
        signInButton = findViewById(R.id.sign_in_button);
        textViewUserName = findViewById(R.id.userName);
        buttonSkip = findViewById(R.id.buttonSkip);

        if (signInButton != null) {
            findViewById(R.id.sign_in_button).setOnClickListener(this);
        }

        if (buttonSkip != null) {
            findViewById(R.id.buttonSkip).setOnClickListener(this);
        }

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Utils.toast(1, 1, connectionResult.getErrorMessage(), SplashActivity.this);
    }

    private void goToApp() {

        YoYo.with(Techniques.FadeIn)
                .duration(1500)
                .repeat(1)
                .onEnd(new YoYo.AnimatorCallback() {
                    @Override
                    public void call(Animator animator) {
                        startActivity(new Intent(SplashActivity.this, PDFActivity.class));
                        finish();
                    }
                })
                .playOn(findViewById(R.id.fullscreen_content));
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.sign_in_button:
                signIn();
                break;
            case R.id.buttonSkip:
                skip();
                break;
        }
    }

    private void skip() {
        buttonSkip.setEnabled(false);
        buttonSkip.setText(getString(R.string.skipped));
        String fieldValues[] = new String[]{"NULL", "NULL"};
        String a[] = new String[]{"account", "uuid"};
        long l = db.insert(fieldValues, a, "sys");
        if (l > 0) {
            goToApp();
        } else {
            Utils.toast(1, 1, getString(R.string.databse_error), SplashActivity.this);
        }
    }

    private void signIn() {

        if (!Utils.isConnectingToInternet(this)) {
            Utils.toast(1, 1, getString(R.string.no_internet), SplashActivity.this);
        } else {
            mProgress.setMessage(getString(R.string.please_wait));
            mProgress.show();
            mProgress.setCancelable(false);

            // Configure sign-in to request the user's ID, email address, and basic
            // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(
                    GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(getString(R.string.firebase_web_client_id))
                    .requestEmail()
                    .build();

            if (mGoogleApiClient == null) {
                // Build a GoogleApiClient with access to the Google Sign-In API and the
                // options specified by gso.
                mGoogleApiClient = new GoogleApiClient.Builder(this)
                        .enableAutoManage(this /* FragmentActivity */,
                                this /* OnConnectionFailedListener */)
                        .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                        .build();
            }

            Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
            startActivityForResult(signInIntent, RC_SIGN_IN);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }
    }

    private void handleSignInResult(GoogleSignInResult result) {

        if (mProgress != null && mProgress.isShowing()) {
            mProgress.dismiss();
        }

        if (result.isSuccess()) {
            GoogleSignInAccount acct = result.getSignInAccount();

            if (acct != null && acct.getDisplayName() != null) {
                firebaseAuthWithGoogle(acct);
            } else {
                Utils.toast(1, 1, getString(R.string.unknown_error), SplashActivity.this);
            }
        } else {
            Utils.toast(1, 1, getString(R.string.google_sign_in), SplashActivity.this);
        }
    }

    @SuppressLint("HardwareIds")
    private void updateAccount() {

        try {

            String fieldValues[] = new String[]{"NULL", strAccount};
            String a[] = new String[]{"uuid", "account"};
            long l = db.insert(fieldValues, a, "sys");

            if (l > 0) {
                goToApp();
            } else {
                Utils.toast(1, 1, getString(R.string.databse_error), SplashActivity.this);
            }

        } catch (Exception e) {
            e.printStackTrace();
            Utils.toast(1, 1, getString(R.string.unknown_error), SplashActivity.this);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.stopAutoManage(this);
            mGoogleApiClient.disconnect();
        }

        db.close();
    }

    @Override
    protected void onResume() {
        super.onResume();

        Cursor dataCursor = null;

        try {

            String fieldNames[] = new String[]{"uuid", "account"};

            dataCursor = db.fetch(Dbhelper.SYS, fieldNames, null, null, "sysId DESC");

            Utils.Log(" C ", String.valueOf(dataCursor.getCount()));

            if (dataCursor.getCount() <= 0) {
                isAuthenticated = false;
            } else {
                isAuthenticated = true;
                if (!dataCursor.getString(1).equalsIgnoreCase("NULL")) {
                    textViewUserName.setVisibility(View.VISIBLE);
                    textViewUserName.setText(dataCursor.getString(1));
                }
            }

            db.closeCursor(dataCursor);
        } catch (Exception e) {
            db.closeCursor(dataCursor);
        }

        if (!isAuthenticated) {
            buttonSkip.setVisibility(View.VISIBLE);
            signInButton.setVisibility(View.VISIBLE);
        } else {
            buttonSkip.setVisibility(View.VISIBLE);
            signInButton.setVisibility(View.VISIBLE);
            //goToApp();
        }
    }

    private void firebaseAuthWithGoogle(final GoogleSignInAccount acct) {

        mProgress.setMessage(getString(R.string.updating_websaver));
        mProgress.show();
        mProgress.setCancelable(false);

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (mProgress != null && mProgress.isShowing()) {
                            mProgress.dismiss();
                        }
                        if (task.isSuccessful()) {

                            FirebaseUser user = mAuth.getCurrentUser();

                            if (user != null) {
                                strAccount = user.getEmail();

                                signInButton.setVisibility(View.GONE);
                                buttonSkip.setVisibility(View.GONE);

                                textViewUserName.setVisibility(View.VISIBLE);
                                textViewUserName.setText(strAccount);

                                updateAccount();
                            } else {
                                Utils.toast(1, 1, getString(R.string.firebase_failed), SplashActivity.this);
                            }
                        } else {
                            Utils.toast(1, 1, getString(R.string.firebase_failed), SplashActivity.this);
                        }
                    }
                });
    }

}
