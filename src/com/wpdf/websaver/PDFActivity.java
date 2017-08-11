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
import com.wpdf.model.PdfModel;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import io.fabric.sdk.android.Fabric;
import pl.tajchert.nammu.PermissionCallback;

public class PDFActivity extends Activity {

    private static List<PdfModel> pdfList = new ArrayList<>();
    private static String strUrl;
    private EditText urlEditText;
    private Dbcon db = null;
    private ListView listView;
    private FirebaseAnalytics mFirebaseAnalytics;
    private PermissionHelper permissionHelper;
    private ViewListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_pdf);

        Fabric.with(this, new Crashlytics());
        // Obtain the FirebaseAnalytics instance.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        urlEditText = findViewById(R.id.editText1);
        listView = findViewById(R.id.listFiles);

        db = new Dbcon(this);

        permissionHelper = PermissionHelper.getInstance(this);

        getPdfList();

        adapter = new ViewListAdapter(this, pdfList);
        listView.setAdapter(adapter);

        TextView.OnEditorActionListener editTextListener = new TextView.OnEditorActionListener() {
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

        urlEditText.setOnEditorActionListener(editTextListener);

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
        db.close();
        permissionHelper.finish();
    }

    public void Web2PDF() {
        try {

            if (!urlEditText.getText().toString().trim().equalsIgnoreCase("")) {
                if (urlEditText.getText().toString().trim().contains(".")) {

                    strUrl = urlEditText.getText().toString().trim();

                    if (!strUrl.contains("http://") || !strUrl.contains("www.")) {

                        if (!strUrl.contains("www.")) {
                            if (strUrl.contains("http://")) {
                                strUrl = strUrl.substring(7);
                            }

                            strUrl = "http://www." + strUrl;
                        } else {
                            if (!strUrl.contains("http://")) {
                                strUrl = "http://" + strUrl;
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


                        Bundle params = new Bundle();
                        params.putString("EVENT", "PDF Requested");
                        mFirebaseAnalytics.logEvent("CREATE", params);

                        DownloadPDF downloadPDF = new DownloadPDF();

                        downloadPDF.execute();
                    } else {
                        Utils.toast(1, 1, getString(R.string.no_internet), PDFActivity.this);
                    }
                } else {
                    Utils.toast(1, 1, getString(R.string.invalid_url), PDFActivity.this);
                }
            } else {
                Utils.toast(1, 1, getString(R.string.no_url), PDFActivity.this);
            }

        } catch (Exception e) {
            Utils.toast(1, 1, getString(R.string.unknown_error), PDFActivity.this);
        }
    }

    private void getPdfList() {

        Cursor dataCursor = null;

        try {

            String fieldNames[] = new String[]{"path", "pdfId"};

            dataCursor = db.fetch(Dbhelper.PDF_LIST, fieldNames, null, null, "pdfId DESC");

            Utils.Log("DF", String.valueOf(dataCursor.getCount()));

            if (dataCursor.getCount() > 0) {

                String path, tempPath, pdfId, time, fullPath;

                if (Utils.isExternalStorageAvailable()) {

                    //pdfList.clear();

                    File f;

                    Date lastModified;

                    SimpleDateFormat dateTime = new SimpleDateFormat("E yyyy-MM-dd HH:mm:ss", Locale.getDefault());

                    while (!dataCursor.isAfterLast()) {

                        if (dataCursor.getString(0) != null && !dataCursor.getString(0).trim().equalsIgnoreCase("")) {
                            path = dataCursor.getString(0);

                            Utils.Log("DF 1 ", path);

                            pdfId = String.valueOf(dataCursor.getInt(1));

                            f = new File(Utils.getFile(this), path);

                            lastModified = new Date(f.lastModified());

                            tempPath = path.substring(0, path.length() - 4);

                            if (f.exists()) {
                                time = dateTime.format(lastModified);
                                fullPath = Uri.fromFile(f).toString().substring(7);

                                pdfList.add(get(tempPath, fullPath, time));
                            } else {
                                db.delete("pdf_list", "pdfId=" + pdfId, null);
                            }
                        }
                        dataCursor.moveToNext();
                    }
                    db.closeCursor(dataCursor);
                }
            }

        } catch (Exception e) {
            db.closeCursor(dataCursor);
        }
    }

    private PdfModel get(String n, String p, String t) {
        return new PdfModel(n, p, t);
    }

    private void signOut() {
        FirebaseAuth.getInstance().signOut();
    }


    /**
     * Represents an asynchronous Task to download PDF
     */
    private class DownloadPDF extends AsyncTask<Void, Void, Boolean> {

        private File file;
        private ProgressDialog mProgress;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgress = new ProgressDialog(PDFActivity.this);

            mProgress.setMessage("Saving...");
            mProgress.show();
            mProgress.setCancelable(false);
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            boolean isSuccess = false;

            try {

                String requestedURL[] = strUrl.split("://");
                String domain[] = requestedURL[1].split("\\.");

                String fileName = domain[1] + "_" + System.currentTimeMillis();
                file = new File(Utils.getFile(PDFActivity.this), fileName);

                // create an API client instance
                Client client = new Client(getString(R.string.pdfcrowd_user_name),
                        getString(R.string.pdfcrowd_key));

                // convert a web page and save the PDF to a file
                FileOutputStream fileStream = new FileOutputStream(file);
                client.convertURI(strUrl, fileStream);
                fileStream.close();

                db.insert(new String[]{fileName}, new String[]{"path"}, Dbhelper.PDF_LIST);

                // retrieve the number of credits in your account
                //Integer ncredits = client.numTokens();

                isSuccess = true;
            } catch (PdfcrowdError why) {
                why.printStackTrace();
            } catch (IOException exc) {
                exc.printStackTrace();
            }

            return isSuccess;
        }

        @Override
        protected void onPostExecute(Boolean result) {

            if (mProgress != null && mProgress.isShowing()) {
                mProgress.dismiss();
            }

            if (result) {
                getPdfList();
                adapter.notifyDataSetChanged();

                Intent myIntent = new Intent(Intent.ACTION_VIEW);
                String extension = android.webkit.MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(file).toString());
                String mimetype = android.webkit.MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
                myIntent.setDataAndType(Uri.fromFile(file), mimetype);
                startActivity(myIntent);

                Utils.toast(1, 1, getString(R.string.file_name) + result, PDFActivity.this);
            } else {
                Utils.toast(1, 1, getString(R.string.unknown_error), PDFActivity.this);
            }
        }

    }
}