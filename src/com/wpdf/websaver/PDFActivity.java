package com.wpdf.websaver;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.text.ClipboardManager;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.pdfcrowd.Client;
import com.pdfcrowd.PdfcrowdError;
import com.wpdf.adapter.ViewListAdapter;
import com.wpdf.application.Config;
import com.wpdf.dbConfig.Dbcon;
import com.wpdf.libs.ConnectionDetector;
import com.wpdf.model.ViewModel;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.fabric.sdk.android.Fabric;

public class PDFActivity extends Activity {

    private final static String TAG = "PDFActivity";
    private static List<ViewModel> list = new ArrayList<>();
    private EditText urlEditText;
    private ProgressDialog mProgress = null;
    private ConnectionDetector cd;
    private String urlMake, gmail, uuid;
    private FirebaseAuth mAuth;
    private Dbcon db = null;
    private ListView listView;
    private FirebaseAnalytics mFirebaseAnalytics;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_pdf);

        Fabric.with(this, new Crashlytics());
        // Obtain the FirebaseAnalytics instance.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        mAuth = FirebaseAuth.getInstance();
        cd = new ConnectionDetector(getApplicationContext());
        mProgress = new ProgressDialog(this);

        //
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        //

        urlEditText = findViewById(R.id.editText1);
        listView = findViewById(R.id.listFiles);

        getModel();

        ViewListAdapter adapter = new ViewListAdapter(this, list);
        listView.setAdapter(adapter);

        db = new Dbcon(this);


        TextView.OnEditorActionListener exampleListener = new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView exampleView, int actionId, KeyEvent event) {

                if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                        actionId == EditorInfo.IME_ACTION_DONE ||
                        event.getAction() == KeyEvent.ACTION_DOWN &&
                                event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                    Web2PDF();
                }
                return true;
            }
        };

        urlEditText.setOnEditorActionListener(exampleListener);

        ClipboardManager clipboard;

        clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);

        if (clipboard != null && clipboard.getText() != null) {
            String textOnClipboard;

            textOnClipboard = clipboard.getText().toString();

            if (!textOnClipboard.trim().equalsIgnoreCase("")) {
                if (textOnClipboard.trim().indexOf(".") != -1) {
                    urlEditText.setText(textOnClipboard);
                }
            }

        }
        findViewById(R.id.button1).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Web2PDF();
                    }
                });

        findViewById(R.id.button2).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        urlEditText.setText("");
                    }
                });

        findViewById(R.id.button3).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        String strMess = getString(R.string.about) + Config.iSdkVersion +
                                getString(R.string.contact);

                        toast(1, 1, strMess);
                    }
                });


    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    public void onContentChanged() {
        super.onContentChanged();

        View empty = findViewById(android.R.id.empty);
        listView = findViewById(R.id.listFiles);
        listView.setEmptyView(empty);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        db.close();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    public void Web2PDF() {
        try {

            if (!urlEditText.getText().toString().trim().equalsIgnoreCase("")) {
                if (urlEditText.getText().toString().trim().indexOf(".") != -1) {

                    urlMake = urlEditText.getText().toString().trim();

                    if (urlMake.indexOf("http://") == -1 || urlMake.indexOf("www.") == -1) {

                        if (urlMake.indexOf("www.") == -1) {
                            if (urlMake.indexOf("http://") != -1) {
                                urlMake = urlMake.substring(7);
                            }

                            urlMake = "http://www." + urlMake;
                        } else {
                            if (urlMake.indexOf("http://") == -1) {
                                urlMake = "http://" + urlMake;
                            }
                        }
                    }

                    if (!cd.isConnectingToInternet()) {
                        // Internet Connection is not present
                        Toast.makeText(PDFActivity.this, "Check Your Internet Connection!!!", Toast.LENGTH_LONG).show();
                        // stop executing code by return
                    }

                    // Check if Internet present
                    if (cd.isConnectingToInternet()) {

                        InputMethodManager imm = (InputMethodManager) getSystemService(
                                Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(urlEditText.getWindowToken(), 0);

                        mProgress.setMessage("Saving...");
                        mProgress.show();
                        mProgress.setCancelable(false);

                        Bundle params = new Bundle();
                        params.putString("EVENT", "PDF Requested");
                        mFirebaseAnalytics.logEvent("CREATE", params);

                        mProgress.dismiss();

                        // DownloadPDF downloadPDF = new DownloadPDF();

                        //downloadPDF.execute();
                    } else {
                        Toast.makeText(PDFActivity.this, getString(R.string.no_internet), Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(PDFActivity.this, "Invalid URL!!!", Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(PDFActivity.this, "No URL!!!", Toast.LENGTH_LONG).show();
            }

        } catch (Exception e) {
            Toast.makeText(PDFActivity.this, "Something Went Wrong. Try Again Later!!!", Toast.LENGTH_LONG).show();
        }
    }

    private void getModel() {

        list.clear();

        Cursor dataCursor = null;

        try {

            String a[] = new String[]{"path", "pdfId"};

            dataCursor = db.fetch("pdf_list", a, null, null, "pdfId DESC");

            dataCursor.moveToFirst();

            if (dataCursor.getCount() > 0) {
                String path, tempPath, pdfId, time, fullPath;

                if (isExternalStorageAvailable()) {
                    File root = new File(Environment.getExternalStorageDirectory() + File.separator + "webSaver" + File.separator);

                    File f = null;

                    Date lastModified = null;

                    SimpleDateFormat df1 = new SimpleDateFormat("E yyyy-MM-dd HH:mm:ss");

                    while (!dataCursor.isAfterLast()) {

                        if (dataCursor.getString(0) != null && !dataCursor.getString(0).trim().equalsIgnoreCase("")) {
                            path = dataCursor.getString(0);

                            pdfId = String.valueOf(dataCursor.getInt(1));

                            f = new File(root, path);

                            lastModified = new Date(f.lastModified());

                            tempPath = path.substring(0, path.length() - 4);

                            if (f.exists()) {
                                time = df1.format(lastModified);
                                fullPath = Uri.fromFile(f).toString().substring(7);
                                list.add(get(tempPath, fullPath, time));//.substring(0,19)
                            } else {
                                db.delete("pdf_list", "pdfId=" + pdfId, null);
                            }
                            dataCursor.moveToNext();
                        }
                        dataCursor.close();
                    }
                }
            }

        } catch (Exception e) {
            //list = null;

            if (dataCursor != null && !dataCursor.isClosed())
                dataCursor.close();
        }

//        return list;
    }

    private ViewModel get(String n, String p, String t) {
        return new ViewModel(n, p, t);
    }

    private boolean isExternalStorageAvailable() {
        boolean mExternalStorageAvailable = false;

        String state = Environment.getExternalStorageState();

        if (Environment.MEDIA_MOUNTED.equals(state)) {
            // We can read and write the media
            mExternalStorageAvailable = true;
        } else // We can only read the media
// Something else is wrong. It may be one of many other states, but all we need
//  to know is we can neither read nor write
            mExternalStorageAvailable = Environment.MEDIA_MOUNTED_READ_ONLY.equals(state);

        return mExternalStorageAvailable;
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        //updateUI(currentUser);
    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    public void toast(int type, int duration, String message) {

        String strColor = "#ffffff";

        if (type == 2)
            strColor = "#fcc485";

        try {
            LayoutInflater inflater = getLayoutInflater();
            View layout = inflater.inflate(R.layout.custom_toast, (ViewGroup)
                    findViewById(R.id.toast_layout_root));

            TextView text = layout.findViewById(R.id.text);
            text.setText(message);
            text.setTextColor(Color.parseColor(strColor));

            Toast toast = new Toast(PDFActivity.this);
            toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);

            if (duration == 2)
                toast.setDuration(Toast.LENGTH_LONG);
            else
                toast.setDuration(Toast.LENGTH_SHORT);

            toast.setView(layout);
            toast.show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(PDFActivity.this, message, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Represents an asynchronous Task to download PDF
     */
    public class DownloadPDF extends AsyncTask<Void, Void, Boolean> {

        private String result = null, urlTest = "";


        @Override
        protected Boolean doInBackground(Void... params) {

            boolean isSuccess = false;

            try {
                FileOutputStream fileStream;

                // create an API client instance
                Client client = new Client("balamscint", "134f7114a3bc77c60c8adaeb3ca9d91f");

                // convert a web page and save the PDF to a file
                fileStream = openFileOutput("data.pdf", MODE_APPEND); //new FileOutputStream("example.pdf");
                client.convertURI("http://vinavu.com/", fileStream);
                fileStream.close();

                // retrieve the number of credits in your account
                //Integer ncredits = client.numTokens();
            } catch (PdfcrowdError why) {
                why.printStackTrace();
            } catch (IOException exc) {
                // handle the exception
                exc.printStackTrace();
            }

            return isSuccess;
        }

        @Override
        protected void onPostExecute(Boolean result) {

            mProgress.dismiss();


            /*File root = new File(Environment.getExternalStorageDirectory() + File.separator + "webSaver" + File.separator);

            if(!root.exists()&&!root.isDirectory())
            root.mkdirs();

            File f = new File (root,result);

            String fieldNames[] = new String[]{"path"};

            String fieldValues[] = new String[]{result};
            db.insert(fieldValues,fieldNames,"pdf_list");

            Intent myIntent = new Intent(Intent.ACTION_VIEW);
            String extension = android.webkit.MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(f).toString());
            String mimetype = android.webkit.MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
            myIntent.setDataAndType(Uri.fromFile(f),mimetype);


            startActivity(myIntent);

            Toast.makeText(PDFActivity.this,getString(R.string.file_name)+result, Toast.LENGTH_LONG).show();*/

        }

    }
}