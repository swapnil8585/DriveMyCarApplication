package com.driver_hiring.user.activities;


import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.driver_hiring.user.R;
import com.driver_hiring.user.webservices.CabHiringAPI;
import com.driver_hiring.user.webservices.JSONParse;
import com.driver_hiring.user.webservices.Utility;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;


public class DocumentActivity extends AppCompatActivity {

    private static final String TAG = "Document";

    private ArrayList<String> documentType, documentPath;
    private ProgressDialog mProgressDialog;

    private GridView mDocumentView;
    private TextView mErrorView;


    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_document);

        getSupportActionBar().setTitle("Driver Documents");
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mDocumentView = (GridView) findViewById(R.id.doc_list);
        mErrorView = (TextView) findViewById(R.id.doc_error);

        String DID = getIntent().getStringExtra(DriverProfileActivity.DRIVER_ID);

        new GetDocumentTask().execute(DID);
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
                            viewHolder.docImage.setScaleType(ImageView.ScaleType.FIT_XY);
                            viewHolder.docImage.setImageBitmap(bitmap);
                        }

                        @Override
                        public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                            viewHolder.docImage.setScaleType(ImageView.ScaleType.FIT_CENTER);
                            viewHolder.docImage.setImageDrawable(errorDrawable);
                        }

                        @Override
                        public void onPrepareLoad(Drawable placeHolderDrawable) {
                            viewHolder.docImage.setScaleType(ImageView.ScaleType.FIT_CENTER);
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
}
