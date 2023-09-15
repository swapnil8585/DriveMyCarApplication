package com.driver_hiring.driver.fragments;


import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.provider.MediaStore;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.driver_hiring.driver.R;
import com.driver_hiring.driver.helper.ImageHelper;
import com.driver_hiring.driver.helper.PreferenceManager;
import com.driver_hiring.driver.webservices.CabHiringAPI;
import com.driver_hiring.driver.webservices.JSONParse;
import com.driver_hiring.driver.webservices.Utility;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.io.CopyStreamAdapter;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.SocketException;
import java.net.URLEncoder;
import java.util.ArrayList;


public class DocumentActivity extends AppCompatActivity {
    public static final int CAMERA_PERMISSION_REQUEST = 103;
    private static final String TAG = "Document";
    public static final int CAMERA_REQUEST_CODE = 100;
    public static final int ACTION_CHOOSE_IMAGE = 101;
    public static final int GALLERY_PERMISSION_CODE = 104;
    private ArrayList<String> documentType, documentPath;
    private ProgressDialog mProgressDialog;

    private GridView mDocumentView;
    private FloatingActionButton mAddDocument;
    private TextView mErrorView;

    private String docImage = "";
    private Uri fileUri;
    private AddDocDailog mAddDialog;
    private String mFileName = "";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_document);

        getSupportActionBar().setTitle("Documents");
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mDocumentView = (GridView) findViewById(R.id.doc_list);
        mAddDocument = (FloatingActionButton) findViewById(R.id.doc_add);
        mErrorView = (TextView) findViewById(R.id.doc_error);


        mAddDocument.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAddDialog = new AddDocDailog(DocumentActivity.this);
                mAddDialog.show();
            }
        });

        new GetDocumentTask().execute(PreferenceManager.getUserId(DocumentActivity.this));
    }


    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home)
            finish();

        return super.onOptionsItemSelected(item);
    }

    private class GetDocumentTask extends AsyncTask<String, JSONObject, String> {

        public GetDocumentTask() {
            mProgressDialog = new ProgressDialog(DocumentActivity.this);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressDialog.setMessage("Loading documents..");
        }

        @Override
        protected String doInBackground(String... strings) {
            CabHiringAPI cabHiringAPI = new CabHiringAPI();
            try {
                JSONObject jsonObject = cabHiringAPI.DgetDocuments(strings[0]);
                return new JSONParse().Parse(jsonObject);
            } catch (Exception e) {
                e.printStackTrace();
                return e.getMessage();
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            mProgressDialog.dismiss();
            Log.d(TAG, "onPostExecute: ");
            if (Utility.checkConnection(s)) {
                Pair<String, String> pair = Utility.GetErrorMessage(s);
                Utility.ShowAlertDialog(DocumentActivity.this, pair.first, pair.second, false);
            } else {
                try {
                    JSONObject object = new JSONObject(s);
                    String exp = object.getString("status");
                    if (exp.compareTo("ok") == 0) {
                        JSONArray result = object.getJSONArray("Data");

                        if (mErrorView.getVisibility() == View.VISIBLE)
                            mErrorView.setVisibility(View.GONE);

                        if (mDocumentView.getVisibility() == View.GONE)
                            mDocumentView.setVisibility(View.VISIBLE);

                        documentType = new ArrayList<>();
                        documentPath = new ArrayList<>();
                        for (int i = 0; i < result.length(); i++) {
                            JSONObject jsonObject = result.getJSONObject(i);
                            documentType.add(jsonObject.getString("data0"));
                            documentPath.add(jsonObject.getString("data1"));
                        }
                        mDocumentView.setAdapter(null);
                        mDocumentView.setAdapter(new DocumentAdapter(DocumentActivity.this));

                    } else if (exp.compareTo("no") == 0) {
                        if (mErrorView.getVisibility() == View.GONE)
                            mErrorView.setVisibility(View.VISIBLE);

                        if (mDocumentView.getVisibility() == View.VISIBLE)
                            mDocumentView.setVisibility(View.GONE);
                    } else {
                        if (mErrorView.getVisibility() == View.GONE)
                            mErrorView.setVisibility(View.VISIBLE);

                        if (mDocumentView.getVisibility() == View.VISIBLE)
                            mDocumentView.setVisibility(View.GONE);

                        String error = object.getString("Data");
                        Log.d(TAG, "onPostExecute: Error : " + error);
                    }
                } catch (Exception e) {
                    Log.d(TAG, "onPostExecute: Exceptions : " + e.getMessage());
                }
            }
        }


    }

    private class DocumentAdapter extends BaseAdapter {
        private Context adapterContext;

        public DocumentAdapter(@NonNull Context context) {
            this.adapterContext = context;
        }

        @Override
        public int getCount() {
            return documentType.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final ViewHolder viewHolder;
            if (convertView == null) {
                convertView = LayoutInflater.from(adapterContext).inflate(R.layout.doc_item, null);
                viewHolder = new ViewHolder();
                viewHolder.docImage = convertView.findViewById(R.id.doc_image);
                viewHolder.docType = convertView.findViewById(R.id.doc_type);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            Log.d(TAG, "getView: URL : " + getUrl(documentPath.get(position)));
            viewHolder.docType.setText(documentType.get(position));
            Picasso.get()
                    .load(getUrl(documentPath.get(position)))
                    .placeholder(getResources().getDrawable(R.drawable
                            .ic_icon_load))
                    .into(new Target() {
                        @Override
                        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                            viewHolder.docImage.setImageBitmap(bitmap);
                        }

                        @Override
                        public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                            viewHolder.docImage.setImageDrawable(errorDrawable);
                        }

                        @Override
                        public void onPrepareLoad(Drawable placeHolderDrawable) {
                            viewHolder.docImage.setImageDrawable(placeHolderDrawable);
                        }
                    });

            return convertView;
        }

        private String getUrl(String fileName) {
            String URL = "http://adriver.hostoise.com/Documents/";
            try {
                fileName = URLEncoder.encode(fileName, "UTF-8");

            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            return URL + fileName;
        }

        private class ViewHolder {
            public ImageView docImage;
            public TextView docType;
        }
    }

    private class AddDocDailog extends Dialog {
        public Context context;
        public Button doc_camera, doc_gallery, doc_AddButton, doc_Cancel, doc_OK;
        public ImageView doc_Image;
        public File mImageTempFile;
        public Spinner mTypeSpinner;
        public ProgressBar mProgressBar;
        public TextView mMessageView;
        public LinearLayout mLinearLayout;


        public AddDocDailog(@NonNull Context context) {
            super(context);
            this.context = context;
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            getWindow().requestFeature(Window.FEATURE_NO_TITLE);
            setContentView(R.layout.dialog_add_docuements);
            setCancelable(false);
            initView();
        }

        private void initView() {
            doc_Image = findViewById(R.id.add_doc_image);
            doc_gallery = findViewById(R.id.add_doc_gallery);
            doc_camera = findViewById(R.id.add_doc_camera);
            doc_OK = findViewById(R.id.add_doc_final);
            doc_AddButton = findViewById(R.id.add_doc_positive);
            doc_Cancel = findViewById(R.id.add_doc_negative);

            mProgressBar = findViewById(R.id.add_doc_progress);
            mMessageView = findViewById(R.id.add_doc_message);
            mLinearLayout = findViewById(R.id.add_doc_holder);
            mTypeSpinner = findViewById(R.id.add_doc_type);
        }

        @Override
        protected void onStart() {
            super.onStart();

            doc_camera.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (weHavePermission()) {
                        startCamera();
                    } else {
                        requestPermission(CAMERA_PERMISSION_REQUEST);
                    }
                }
            });

            doc_gallery.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (weHavePermission()) {
                        startGallery();
                    } else {
                        requestPermission(GALLERY_PERMISSION_CODE);
                    }
                }
            });

            doc_AddButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mImageTempFile == null) {
                        Snackbar.make(v, "Please select or capture image.", Snackbar.LENGTH_SHORT).show();
                    } else if (documentType != null && documentType.size() > 0
                            && documentType.contains(mTypeSpinner.getSelectedItem().toString())) {

                        Snackbar.make(v, "Document Already Uploaded"
                                , Snackbar.LENGTH_SHORT).show();
                    } else {

                        String fileName =
                                PreferenceManager.getUserId(DocumentActivity.this) + "_"
                                        + (mAddDialog.mTypeSpinner.getSelectedItemPosition() == 0 ? "Driving" : "PhotoId") + "";
                        new AddDocumentTask(fileName).execute(
                                PreferenceManager.getUserId(DocumentActivity.this)
                                , mAddDialog.mTypeSpinner.getSelectedItem().toString()
                                , fileName);

                    }
                }
            });

            doc_Cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mImageTempFile = null;
                    if (isShowing()) {
                        dismiss();
                    }
                }
            });

            doc_OK.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isShowing()) {
                        cancel();
                    }

                    new GetDocumentTask().execute(PreferenceManager.getUserId(DocumentActivity.this));
                }
            });
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: ");
        String mFileName = PreferenceManager.getUserId(DocumentActivity.this) + "_"
                + (mAddDialog.mTypeSpinner.getSelectedItemPosition() == 0 ? "Driving" : "PhotoId") + "";
        Log.d(TAG, "onActivityResult: File Name " + mFileName);
        if (requestCode == CAMERA_REQUEST_CODE) {
            Log.d(TAG, "onActivityResult: Camera");
            try {
                Bitmap mBitmap = ImageHelper.loadSizeLimitedBitmapFromUri(fileUri, getContentResolver());
                if (mBitmap != null) {
                    mAddDialog.doc_Image.setImageBitmap(mBitmap);
                    docImage = "";
                    docImage = Utility.encodeBitmap(mBitmap);
                    mAddDialog.mImageTempFile = CreateTempFile(mFileName, mBitmap);
                } else {
                    docImage = "";
                    mAddDialog.doc_Image.setImageDrawable(getResources().getDrawable(R.mipmap.ic_launcher));
                }
            } catch (Exception e) {
                docImage = "";
                mAddDialog.doc_Image.setImageDrawable(getResources().getDrawable(R.mipmap.ic_launcher));
            }
        } else if (requestCode == ACTION_CHOOSE_IMAGE) {
            try {
                mAddDialog.doc_Image.setImageBitmap(null);
                Bitmap mBitmap = ImageHelper.loadSizeLimitedBitmapFromUri(data.getData(), getContentResolver());
                if (mBitmap != null) {
//                    img.setImageBitmap(mBitmap);
                    mAddDialog.doc_Image.setImageBitmap(mBitmap);
                    docImage = Utility.encodeBitmap(mBitmap);
                    mAddDialog.mImageTempFile = CreateTempFile(mFileName, mBitmap);
                } else {
                    docImage = "";
                    mAddDialog.doc_Image.setImageDrawable(getResources().getDrawable(R.mipmap.ic_launcher));
                }
            } catch (Exception e) {
                docImage = "";
                mAddDialog.doc_Image.setImageDrawable(getResources().getDrawable(R.mipmap.ic_launcher));
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.d(TAG, "onRequestPermissionsResult: ");
        if (requestCode == CAMERA_PERMISSION_REQUEST) {
            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera();
            } else {
                requestPermission(CAMERA_PERMISSION_REQUEST);
            }
        } else if (requestCode == GALLERY_PERMISSION_CODE) {
            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startGallery();
            } else {
                requestPermission(GALLERY_PERMISSION_CODE);
            }
        }
    }

    private File CreateTempFile(@NonNull String mImageFile, @NonNull Bitmap mBitmap) {
        try {
            File mFolder = new ContextWrapper(DocumentActivity.this).getDir("Images", Context.MODE_PRIVATE);
            if (!mFolder.exists()) {
                if (mFolder.mkdir()) {
                    Log.d(TAG, "CreateFile: Folder Created");
                }
            }
            File mTempFile = new File(mFolder.getAbsolutePath(), mImageFile + ".png");
            if (!mTempFile.exists()) {
                if (mTempFile.createNewFile()) {
                    Log.d(TAG, "CreateFile: File Created");
                }
            }
            FileOutputStream fOut = new FileOutputStream(mTempFile);
            mBitmap.compress(Bitmap.CompressFormat.PNG, 90, fOut);
            fOut.close();
            Log.d(TAG, "CreateTempFile: File Created at - " + mTempFile.getAbsolutePath());
            return mTempFile;

        } catch (IOException exp) {
            exp.printStackTrace();
            return null;
        }
    }

    private void DeleteTempFile(@NonNull File mTempFile) {
        if (mTempFile.exists()) {
            if (mTempFile.delete())
                Log.d(TAG, "DeleteTempFile: File Deleted");
        }
    }

    private void startGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture")
                , ACTION_CHOOSE_IMAGE);
    }

    private void startCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(DocumentActivity.this.getPackageManager()) != null) {
            ContentValues values = new ContentValues(1);
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpg");
            fileUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            startActivityForResult(intent, CAMERA_REQUEST_CODE);
        }
    }

    private boolean weHavePermission() {
        return (ContextCompat.checkSelfPermission(DocumentActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) &&
                (ContextCompat.checkSelfPermission(DocumentActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) &&
                ContextCompat.checkSelfPermission(DocumentActivity.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission(int PERMISSION_REQUEST_CODE) {
        ActivityCompat.requestPermissions(DocumentActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA}, PERMISSION_REQUEST_CODE);
    }

    private class FileUpload extends AsyncTask<String, String, String> {

        private FTPClient mFTPClient;
        private CopyStreamAdapter streamListener;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mAddDialog.mMessageView.setText("Uploading Image...");
        }

        @Override
        protected String doInBackground(String... strings) {
            String ans = "";
            try {
                mFTPClient = new FTPClient();
                mFTPClient.connect(getResources().getString(R.string.sitename));

                if (mFTPClient.login(getResources().getString(R.string.usernme), getResources().getString(R.string.pass))) {
                    mFTPClient.setFileType(FTP.BINARY_FILE_TYPE);

                    BufferedInputStream buffIn = null;
                    final File file = new File(strings[0]);
                    Log.d(TAG, "doInBackground: File Path : " + file.getAbsolutePath());
                    buffIn = new BufferedInputStream(new FileInputStream(file));
                    mFTPClient.enterLocalPassiveMode();
                    streamListener = new CopyStreamAdapter() {

                        @Override
                        public void bytesTransferred(long totalBytesTransferred,
                                                     int bytesTransferred, long streamSize) {

                            int percent = (int) (totalBytesTransferred * 100 / file.length());
                            mAddDialog.mProgressBar.setProgress(percent);
                            publishProgress();

                            if (totalBytesTransferred == file.length()) {
                                System.out.println("100% transfered");

                                removeCopyStreamListener(streamListener);

                            }
                        }
                    };
                    mFTPClient.setCopyStreamListener(streamListener);
                    Log.d(TAG, "doInBackground: Server Path : " + strings[1]);
                    Boolean status = mFTPClient.storeFile(strings[1], buffIn);
                    if (status) {
                        ans = "true";
                    } else {
                        ans = "false";
                    }

                    buffIn.close();
                    mFTPClient.logout();
                    mFTPClient.disconnect();

                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                ans = "file not found-" + "\n" + e.getMessage();
            } catch (SocketException e) {
                e.printStackTrace();
                ans = "socket-" + "\n" + e.getMessage();
            } catch (IOException e) {
                e.printStackTrace();
                ans = "IO-" + "\n" + e.getMessage();
            }
            return ans;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(final String s) {
            super.onPostExecute(s);
            Log.d(TAG, "onPostExecute: " + s);
            mAddDialog.mMessageView.setText("Successfully updated.");

            if (mAddDialog.mLinearLayout.getVisibility() == View.VISIBLE) {
                mAddDialog.mLinearLayout.setVisibility(View.GONE);
            }
            if (s.compareTo("true") == 0) {
                DeleteTempFile(mAddDialog.mImageTempFile);

                mAddDialog.doc_AddButton.setVisibility(View.GONE);
                mAddDialog.doc_Cancel.setVisibility(View.GONE);
                mAddDialog.doc_OK.setVisibility(View.VISIBLE);
            } else {
                Log.d(TAG, "onPostExecute: " + s);
                mAddDialog.dismiss();
                Toast.makeText(DocumentActivity.this, s, Toast.LENGTH_SHORT).show();
            }
        }


    }

    private class AddDocumentTask extends AsyncTask<String, String, String> {
        private String fileName = "";

        AddDocumentTask(@NonNull String name) {
            this.fileName = name;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mAddDialog.mProgressBar.setProgress(0);
            mAddDialog.mMessageView.setText("Please wait updating your profile...");
        }

        @Override
        protected String doInBackground(String... strings) {
            CabHiringAPI cabHiringAPI = new CabHiringAPI();
            try {
                JSONObject jsonObject = cabHiringAPI.DaddDocs(strings[0], strings[1], strings[2]);
                return new JSONParse().Parse(jsonObject);
            } catch (Exception e) {
                e.printStackTrace();
                return e.getMessage();
            }
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            Log.d(TAG, "onProgressUpdate: " + values[0]);
            mAddDialog.mProgressBar.setProgress(Integer.parseInt(values[0]));
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Log.d(TAG, "onPostExecute: " + s);

            if (Utility.checkConnection(s)) {
                Pair<String, String> pair = Utility.GetErrorMessage(s);
                Utility.ShowAlertDialog(DocumentActivity.this, pair.first, pair.second, false);
            } else {
                try {
                    JSONObject object = new JSONObject(s);
                    String exp = object.getString("status");
                    if (exp.compareTo("true") == 0) {

                        if (mAddDialog.mLinearLayout.getVisibility() == View.GONE) {
                            mAddDialog.mLinearLayout.setVisibility(View.VISIBLE);
                        }

                        mAddDialog.mMessageView.setText("Updated...");

                        new FileUpload().execute(mAddDialog.mImageTempFile.getAbsoluteFile().getAbsolutePath(), "/ADriver/Documents" +
                                "/" + fileName);

                    } else if (exp.compareTo("already") == 0) {

                        if (mAddDialog.mLinearLayout.getVisibility() == View.VISIBLE) {
                            mAddDialog.mLinearLayout.setVisibility(View.GONE);
                        }
                        Snackbar.make(mAddDialog.doc_AddButton, "Document Already Uploaded"
                                , Snackbar.LENGTH_SHORT).show();
                    } else {
                        String error = object.getString("Data");
                        Log.d(TAG, "onPostExecute: Error : " + error);
                    }
                } catch (Exception e) {
                    Log.d(TAG, "onPostExecute: Exceptions : " + e.getMessage());
                }
            }
        }


    }
}
