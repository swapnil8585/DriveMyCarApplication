package com.driver_hiring.user.fragments;


import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.driver_hiring.user.helper.PreferenceManager;
import com.driver_hiring.user.R;
import com.driver_hiring.user.webservices.CabHiringAPI;
import com.driver_hiring.user.webservices.JSONParse;
import com.driver_hiring.user.webservices.Utility;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class TransactionFragment extends Fragment {
    private static final String TAG = "TransactionFragment";
    private ListView transactionList;
    private TextView error;
    private String driverId = "";
    private ArrayList<String> city, dname, cost, status, date, time;

    public TransactionFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        driverId = PreferenceManager.getUserId(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_past_trips, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        view.findViewById(R.id.pastDate).setVisibility(View.GONE);
        transactionList = (ListView) view.findViewById(R.id.past_trips);
        transactionList.setPadding(5, 5, 5, 5);
        transactionList.setBackgroundColor(getResources().getColor(R.color.primaryDarkColor));
        error = (TextView) view.findViewById(R.id.text_past_trips);
    }

    @Override
    public void onResume() {
        super.onResume();
        new GetTransaction().execute(driverId);
    }

    private class GetTransaction extends AsyncTask<String, JSONObject, String> {
        private ProgressDialog progressDialog;

        public GetTransaction() {
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setCancelable(false);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.setMessage("Please Wait");
            progressDialog.show();
            transactionList.setAdapter(null);
        }

        @Override
        protected String doInBackground(String... params) {
            String a = "back";
            CabHiringAPI api = new CabHiringAPI();
            try {
                JSONObject json = api.UgetTransactions(params[0]);
                JSONParse jp = new JSONParse();
                a = jp.Parse(json);
            } catch (Exception e) {
                a = e.getMessage();
            }
            return a;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (progressDialog.isShowing()) {
                progressDialog.cancel();
            }
            Log.d(TAG, "onPostExecute: " + s);
            if (Utility.checkConnection(s)) {
                Pair<String, String> pair = Utility.GetErrorMessage(s);
                Utility.ShowAlertDialog(getActivity(), pair.first, pair.second, false);
            } else {
                try {
                    JSONObject json = new JSONObject(s);
                    String ans = json.getString("status");
//                    Log.d("ANS", s);
                    if (ans.compareTo("ok") == 0) {
                        //Aid,Sender/Reciever,cost,status,date,time
                        //name,city,cost,date,time,status
                        dname = new ArrayList<String>();
                        cost = new ArrayList<String>();
                        status = new ArrayList<String>();

                        JSONArray jarray = json.getJSONArray("Data");
                        for (int i = 0; i < jarray.length(); i++) {
                            JSONObject jdata = jarray.getJSONObject(i);
                            dname.add(jdata.getString("data0"));
                            cost.add(jdata.getString("data1"));
                            status.add(jdata.getString("data2"));

                        }
                        if (dname.size() > 0) {
                            if (error.getVisibility() == View.VISIBLE) {
                                error.setVisibility(View.GONE);
                            }
                            if (transactionList.getVisibility() == View.GONE) {
                                transactionList.setVisibility(View.VISIBLE);
                            }
                            Adapter adapter = new Adapter(getActivity(), dname);
                            transactionList.setAdapter(adapter);
                        }
                    } else if (ans.compareTo("no") == 0) {

                        error.setText("You have not performed any transactions.");

                        if (error.getVisibility() == View.GONE) {
                            error.setVisibility(View.VISIBLE);
                        }

                        if (transactionList.getVisibility() == View.VISIBLE) {
                            transactionList.setVisibility(View.GONE);
                        }

                    } else if (ans.compareTo("error") == 0) {
                        String error = json.getString("Data");
                        Toast.makeText(getActivity(), error, Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private class Adapter extends ArrayAdapter<String> {
        Context con;

        public Adapter(@NonNull Context context, ArrayList<String> a) {
            super(context, R.layout.transation_view_item, a);
            con = context;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            View v = LayoutInflater.from(con).inflate(R.layout.transation_view_item, null, true);
            TextView fname = (TextView) v.findViewById(R.id.tName);
            TextView tprice = (TextView) v.findViewById(R.id.tPrice);

            fname.setText(dname.get(position));
            tprice.setText(getResources().getString(R.string.currency)
                    + " " + cost.get(position));

            if (status.get(position).compareTo("Booked") == 0
                    || status.get(position).compareTo("Finished") == 0) {
                tprice.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_icon_out, 0);
                tprice.setTextColor(getResources().getColor(R.color.colorRed));
            } else if (status.get(position).compareTo("Cancelled") == 0) {
                tprice.setTextColor(getResources().getColor(R.color.colorGreen));
                tprice.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_icon_in, 0);
            }

            return v;
        }
    }

}
