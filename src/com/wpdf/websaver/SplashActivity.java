package com.wpdf.websaver;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.ayz4sci.androidfactory.permissionhelper.PermissionHelper;
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
import com.google.firebase.auth.GoogleAuthProvider;
import com.wpdf.dbConfig.Dbcon;
import com.wpdf.libs.Utils;

import pl.tajchert.nammu.PermissionCallback;

public class SplashActivity extends AppCompatActivity implements
        GoogleApiClient.OnConnectionFailedListener, View.OnClickListener {


    private static final int RC_SIGN_IN = 1001;
    private static final String TAG = "SplashActivity";
    private static GoogleApiClient mGoogleApiClient = null;
    private static boolean isAuthenticated = true;
    private static String strAccount;
    private Dbcon db = null;
    private SignInButton signInButton;
    private TextView textViewUserName;
    private PermissionHelper permissionHelper;
    private Handler mHandler = new Handler();
    private FirebaseAuth mAuth;

    private Runnable mGoToApp = new Runnable() {
        public void run() {
            startActivity(new Intent(SplashActivity.this, PDFActivity.class));
            finish();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_splash);

        db = new Dbcon(this);

        // Set the dimensions of the sign-in button.
        signInButton = (SignInButton) findViewById(R.id.sign_in_button);
        if (signInButton != null) {
            signInButton.setSize(SignInButton.SIZE_STANDARD);
        }

        permissionHelper = PermissionHelper.getInstance(this);

        findViewById(R.id.sign_in_button).setOnClickListener(this);

        textViewUserName = (TextView) findViewById(R.id.userName);

        mAuth = FirebaseAuth.getInstance();
    }


    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        permissionHelper.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        if (signInButton != null) {
            signInButton.setVisibility(View.VISIBLE);
        }
    }

    private void goToApp() {
        YoYo.with(Techniques.DropOut)
                .duration(800)
                .repeat(1)
                .playOn(findViewById(R.id.fullscreen_content));

        mHandler.postDelayed(mGoToApp, 890);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.sign_in_button:
                signIn();
                break;
        }
    }

    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        } else {
            permissionHelper.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void handleSignInResult(GoogleSignInResult result) {
        Log.d(TAG, "handleSignInResult:" + result.isSuccess());
        if (result.isSuccess()) {
            GoogleSignInAccount acct = result.getSignInAccount();

            if (acct != null && acct.getDisplayName() != null) {

                strAccount = acct.getDisplayName();
                signInButton.setVisibility(View.GONE);
                textViewUserName.setVisibility(View.VISIBLE);
                textViewUserName.setText(strAccount);

                if (ActivityCompat.checkSelfPermission(this,
                        android.Manifest.permission.READ_PHONE_STATE) ==
                        PackageManager.PERMISSION_GRANTED) {
                    firebaseAuthWithGoogle(acct);
                } else {
                    checkPermission();
                }

            } else {
                finish();
            }
        } else {
            finish();
        }
    }

    private void checkPermission() {
        permissionHelper.verifyPermission(
                new String[]{getString(R.string.phone_state_request)},
                new String[]{android.Manifest.permission.READ_PHONE_STATE},
                new PermissionCallback() {
                    @Override
                    public void permissionGranted() {
                        updateAccount();
                    }

                    @Override
                    public void permissionRefused() {
                        finish();
                    }
                }
        );
    }

    @SuppressLint("HardwareIds")
    private void updateAccount() {

        TelephonyManager tManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {

            String uuid = "NULL";
            if (tManager != null) {
                uuid = tManager.getDeviceId();
            }

            try {

                String fieldValues[] = new String[]{uuid, strAccount};
                String a[] = new String[]{"uuid", "account"};
                db.insert(fieldValues, a, "sys");

                goToApp();

            } catch (Exception e) {
                e.printStackTrace();
                finish();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.stopAutoManage(this);
            mGoogleApiClient.disconnect();
        }

        permissionHelper.finish();
    }

    @Override
    protected void onStart() {
        super.onStart();

        Cursor dataCursor = null;

        try {

            String a[] = new String[]{"uuid", "account"};

            dataCursor = db.fetch("sys", a, null, null, "sysId DESC");

            if (dataCursor.getCount() <= 0) {
                isAuthenticated = false;
            }

            dataCursor.close();
        } catch (Exception e) {
            if (dataCursor != null && !dataCursor.isClosed())
                dataCursor.close();
        }

        if (!isAuthenticated) {

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

            signIn();
        } else {
            goToApp();
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getIdToken());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            //FirebaseUser user = mAuth.getCurrentUser();
                            updateAccount();
                        } else {
                            Utils.toast(1, 1, "Failed", SplashActivity.this);
                        }
                    }
                });
    }

}
