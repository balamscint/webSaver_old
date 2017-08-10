package com.wpdf.websaver;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.text.ClipboardManager;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ayz4sci.androidfactory.permissionhelper.PermissionHelper;
import com.crashlytics.android.Crashlytics;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.pdfcrowd.Client;
import com.pdfcrowd.PdfcrowdError;
import com.wpdf.adapter.ViewListAdapter;
import com.wpdf.dbConfig.Dbcon;
import com.wpdf.dbConfig.Dbhelper;
import com.wpdf.libs.Utils;
import com.wpdf.model.ViewModel;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.fabric.sdk.android.Fabric;
import pl.tajchert.nammu.PermissionCallback;

public class PDFActivity extends Activity {

    private final static String TAG = "PDFActivity";
    private static List<ViewModel> list = new ArrayList<>();
    private static String urlMake;
    private EditText urlEditText;
    private ProgressDialog mProgress = null;
    private Dbcon db = null;
    private ListView listView;
    private FirebaseAnalytics mFirebaseAnalytics;
    private PermissionHelper permissionHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_pdf);

        Fabric.with(this, new Crashlytics());
        // Obtain the FirebaseAnalytics instance.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        mProgress = new ProgressDialog(this);

        urlEditText = findViewById(R.id.editText1);
        listView = findViewById(R.id.listFiles);

        db = new Dbcon(this);

        permissionHelper = PermissionHelper.getInstance(this);

        getModel();

        ViewListAdapter adapter = new ViewListAdapter(this, list);
        listView.setAdapter(adapter);

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
                if (textOnClipboard.trim().contains(".")) {
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

        /*findViewById(R.id.button3).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        String strMess = getString(R.string.about) + WebSaverApplication.iSdkVersion +
                                getString(R.string.contact);

                        Utils.toast(1, 1, strMess, PDFActivity.this);
                    }
                });*/


        permissionHelper.verifyPermission(
                new String[]{getString(R.string.internet_request),
                        getString(R.string.storage_request)},
                new String[]{Manifest.permission.ACCESS_NETWORK_STATE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE},
                new PermissionCallback() {
                    @Override
                    public void permissionGranted() {

                    }

                    @Override
                    public void permissionRefused() {

                    }
                }
        );
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        permissionHelper.onRequestPermissionsResult(requestCode, permissions, grantResults);
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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        permissionHelper.onActivityResult(requestCode, resultCode, data);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        //db.close();
        permissionHelper.finish();
    }

    public void Web2PDF() {
        try {

            if (!urlEditText.getText().toString().trim().equalsIgnoreCase("")) {
                if (urlEditText.getText().toString().trim().contains(".")) {

                    urlMake = urlEditText.getText().toString().trim();

                    if (!urlMake.contains("http://") || !urlMake.contains("www.")) {

                        if (!urlMake.contains("www.")) {
                            if (urlMake.contains("http://")) {
                                urlMake = urlMake.substring(7);
                            }

                            urlMake = "http://www." + urlMake;
                        } else {
                            if (!urlMake.contains("http://")) {
                                urlMake = "http://" + urlMake;
                            }
                        }
                    }

                    if (!Utils.isConnectingToInternet(this)) {
                        Utils.toast(1, 1, getString(R.string.no_internet), PDFActivity.this);
                    }

                    // Check if Internet present
                    if (Utils.isConnectingToInternet(this)) {

                        InputMethodManager imm = (InputMethodManager) getSystemService(
                                Context.INPUT_METHOD_SERVICE);
                        if (imm != null) {
                            imm.hideSoftInputFromWindow(urlEditText.getWindowToken(), 0);
                        }

                        mProgress.setMessage("Saving...");
                        mProgress.show();
                        mProgress.setCancelable(false);

                        Bundle params = new Bundle();
                        params.putString("EVENT", "PDF Requested");
                        mFirebaseAnalytics.logEvent("CREATE", params);

                        DownloadPDF downloadPDF = new DownloadPDF();

                        downloadPDF.execute();
                    } else {
                        Utils.toast(1, 1, getString(R.string.no_internet), PDFActivity.this);
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

                Utils.Log(TAG, dataCursor.getString(0));

                //if (isExternalStorageAvailable()) {
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
                // }
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

    private void signOut() {
        FirebaseAuth.getInstance().signOut();
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
                //FileOutputStream fileStream;

                Utils.Log(TAG, "F");

                File root = android.os.Environment.getExternalStorageDirectory();

                File dir = new File(root.getAbsolutePath() + "/webSaver");
                dir.mkdirs();
                File file = new File(dir, "data.pdf");

                // create an API client instance
                Client client = new Client(getString(R.string.pdfcrowd_user_name),
                        getString(R.string.pdfcrowd_key));

                // convert a web page and save the PDF to a file
                FileOutputStream fileStream = new FileOutputStream(file);
                fileStream = openFileOutput("data.pdf", MODE_APPEND); //new FileOutputStream("example.pdf");
                client.convertURI(urlMake, fileStream);
                fileStream.close();

                String filePath = dir.getAbsolutePath() + "/data.pdf";

                Utils.Log(TAG, filePath);

                db.insert(new String[]{filePath}, new String[]{"path"}, Dbhelper.PDF_LIST);

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