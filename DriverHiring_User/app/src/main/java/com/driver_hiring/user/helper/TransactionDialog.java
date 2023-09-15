package com.driver_hiring.user.helper;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.driver_hiring.user.R;
import com.google.android.material.snackbar.Snackbar;

import java.text.SimpleDateFormat;
import java.util.Date;

public class TransactionDialog extends Dialog {

    private Context mAppContext;
    private static Transaction mTransation;
    private EditText tdname, tdcno, tdyear, tdmon, tdcvv;
    private TextView btn_submit, total;
    private EditText[] textboxes;
    private String mesg[] = new String[]{"Name on card", "16 digit Card Number"
            , "Year ( YYYY )", "Month ( MM )", "3 digit CVV number"};
    private String mainTotalCost;

    public TransactionDialog(@NonNull Context context, String mainCost) {
        super(context);
        this.mAppContext = context;
        this.mainTotalCost = mainCost;
    }

    public TransactionDialog(@NonNull Context context, int themeResId, String mainCost) {
        super(context, themeResId);
        this.mAppContext = context;
        this.mainTotalCost = mainCost;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.transaction_dialog);
        setCancelable(false);
        initViews();
    }

    private void initViews() {
        total = (TextView) findViewById(R.id.totalamount);
        tdname = (EditText) findViewById(R.id.pname);
        tdcno = (EditText) findViewById(R.id.pcno);
        tdyear = (EditText) findViewById(R.id.pyear);
        tdmon = (EditText) findViewById(R.id.pmon);
        tdcvv = (EditText) findViewById(R.id.pcvv);
        btn_submit = (TextView) findViewById(R.id.psubmit);

        textboxes = new EditText[]{tdname, tdcno, tdyear, tdmon, tdcvv};

        total.setText(String.format("Total Amount %s %s", mAppContext.getResources().getString(R.string.currency)
                , mainTotalCost));

        btn_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkFields()) {
                    mTransation.onSuccess(mainTotalCost);
                }
            }
        });
    }

    private boolean checkFields() {
        SimpleDateFormat sdfy = new SimpleDateFormat("yyyy");
        SimpleDateFormat sdfm = new SimpleDateFormat("MM");
        Integer YEAR = Integer.parseInt(sdfy.format(new Date().getTime()));
        Integer MONTH = Integer.parseInt(sdfm.format(new Date().getTime()));

        boolean ans = true;

        for (int i = 0; i < textboxes.length; i++) {
            if (textboxes[i].length() == 0) {
                Snackbar.make(btn_submit, "Enter " + mesg[i], Snackbar.LENGTH_SHORT).show();
                return false;
            } else {
                if (i == 1) {
                    if (textboxes[i].length() != 16) {
                        Snackbar.make(btn_submit, "card no Should have 16 Digits", Snackbar.LENGTH_SHORT).show();
                        return false;
                    }
                }

                if (i == 4) {
                    if (textboxes[i].length() != 3) {
                        Snackbar.make(btn_submit, "CVV Should have 3 Digits", Snackbar.LENGTH_SHORT).show();
                        return false;
                    }
                }

                if (i == 2) {
                    if (textboxes[i].length() != 4) {
                        Snackbar.make(btn_submit, "Year Should be in YYYY format", Snackbar.LENGTH_SHORT).show();
                        return false;
                    } else {
                        int y = Integer.parseInt(tdyear.getText().toString());
                        if (y < YEAR) {
                            Snackbar.make(btn_submit, "Your Card is Expired", Snackbar.LENGTH_SHORT).show();
                            return false;
                        }
                    }
                }

                if (i == 3) {
                    if (textboxes[i].length() != 2) {
                        Snackbar.make(btn_submit, "Month Should be in MM format", Snackbar.LENGTH_SHORT).show();
                        return false;
                    } else {
                        int y = Integer.parseInt(tdyear.getText().toString());
                        int m = Integer.parseInt(tdmon.getText().toString());
                        try {
                            if (m > 0 && m <= 12) {

                                if (y == YEAR) {
                                    if (m < MONTH) {
                                        Snackbar.make(btn_submit, "Your Card is Expired", Snackbar.LENGTH_SHORT).show();
                                        return false;
                                    }
                                }

                            } else {

                                Snackbar.make(btn_submit, "Invalid Month", Snackbar.LENGTH_SHORT).show();
                                return false;

                            }
                        } catch (Exception e) {
                        }
                    }
                }
            }

        }

        return ans;
    }

    public interface Transaction {
        void onSuccess(@NonNull String price);
    }

    public static void bindTransactionListener(TransactionDialog.Transaction transaction) {
        mTransation = transaction;
    }

}
