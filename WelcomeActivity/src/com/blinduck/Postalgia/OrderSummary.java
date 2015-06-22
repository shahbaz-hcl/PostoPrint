package com.blinduck.Postalgia;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragment;
import com.blinduck.Postalgia.sqlite.ImageDataSource;
import com.blinduck.Postalgia.sqlite.SQLiteHelper;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: deepan
 * Date: 7/4/13
 * Time: 10:40 AM
 * To change this template use File | Settings | File Templates.
 */
public class OrderSummary  extends SherlockFragment {

    private OrderSummaryInterface listener;

    private final String checkDiscountUrl = "http://www.postalgiaprints.com/api/v1/voucher/";
    public static final String SCALED_IMAGE_SERVICE_COMPLETE = "scaled_image_service_complete";


    ImageButton paypalButton;

    ImageDataSource dataSource;
    SQLiteDatabase database;

    TextView size4RQuantityView;
    TextView sizeWalletQuantityView;
    TextView sizeSquareQuantityView;

    TextView size4RCostView;
    TextView sizeWalletCostView;
    TextView sizeSquareCostView;
    TextView deliveryCostView;

    TextView size4RDiscountView;
    TextView sizeWalletDiscountView;
    TextView sizeSquareDiscountView;

    TextView summaryAddressText;
    ImageButton changeSettings;

    public static final int CONFIRM_DIALOG = 1;


    TextView totalCostView;

    EditText discountField;
    Button checkDiscountButton;


    SharedPreferences prefs;
    SharedPreferences.Editor editor;
    Boolean settingsValid;

    private float size4RPrice = 0.50f;
    private float sizeWalletPrice = 0.40f;
    private float  sizeSquarePrice = 0.60f;




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.order_summary, container, false);

        size4RQuantityView = (TextView)view.findViewById(R.id.summary_4R_quantity);
        sizeWalletQuantityView = (TextView)view.findViewById(R.id.summary_wallet_quantity);
        sizeSquareQuantityView = (TextView)view.findViewById(R.id.summary_square_quatity);

        size4RCostView = (TextView)view.findViewById(R.id.summary_4R_cost);
        sizeWalletCostView = (TextView)view.findViewById(R.id.summary_wallet_cost);
        sizeSquareCostView = (TextView)view.findViewById(R.id.summary_square_cost);

        size4RDiscountView = (TextView)view.findViewById(R.id.summary_4R_discount);
        sizeWalletDiscountView = (TextView)view.findViewById(R.id.summary_wallet_discount);
        sizeSquareDiscountView  = (TextView)view.findViewById(R.id.summary_square_discount);

        deliveryCostView = (TextView)view.findViewById(R.id.summary_delivery);

        totalCostView = (TextView)view.findViewById(R.id.summary_totalCost);

        summaryAddressText = (TextView)view.findViewById(R.id.summary_address_text);
        changeSettings = (ImageButton)view.findViewById(R.id.summary_change_address);
        paypalButton = (ImageButton)view.findViewById(R.id.summary_startPaypal);



        discountField = (EditText)view.findViewById(R.id.summary_discount_code);
        /*checkDiscountButton = (Button)view.findViewById(R.id.summary_checkVoucherCode);*/

        discountField.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE ||
                        event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_ENTER ) {
                    checkVoucherCode();
                    InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(
                            Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(discountField.getWindowToken(), 0);


                }
                return true;
            }


        });

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        dataSource = new ImageDataSource(getActivity());
        database = dataSource.getDatabase();

        ActionBar actionBar = getSherlockActivity().getSupportActionBar();
        actionBar.setTitle("Order Summary");

        prefs = getActivity().getSharedPreferences(WelcomeActivity.PREFS_NAME, Context.MODE_PRIVATE);
        int session = prefs.getInt(WelcomeActivity.SESSION, 0);

        //set the address and the quantity and cost of items
        setAddress();
        calculatePrice(null);

        editor = prefs.edit();
        editor.putInt("cartCount", getTotalCount(session));
        editor.remove("voucherCode");
        editor.commit();


        paypalButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showConfirmationPayDialog();

                /**/
            }
        });

        changeSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.updateParticulars();
            }
        });
        /*
        checkDiscountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(
                        Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(discountField.getWindowToken(), 0);
                checkVoucherCode();
            }
        });*/
    }

    private void showConfirmationPayDialog () {
        FragmentManager fm = getActivity().getSupportFragmentManager();
        CheckoutConfirmationDialog dialog = new CheckoutConfirmationDialog();
        dialog.setTargetFragment(this, CONFIRM_DIALOG);
        dialog.show(fm, "");
    }


    @Override
    public void onActivityResult(int requestCode, int result, Intent data) {
        switch (requestCode) {
            case CONFIRM_DIALOG:
                switch (result) {
                    case 0: //"Back"     cancelled, go back on screen
                        listener.dialogCancelPressed();
                        break;
                    case 1:  //"Proceed"   continue to payment
                        startPayment();
                        break;
                }

        }

    }

    private void startPayment() {
        editor.putBoolean(SCALED_IMAGE_SERVICE_COMPLETE, false);
        editor.commit();
        Log.d("Postal", "scaled image service set to false");
        listener.startCreateImagesService();
        listener.startPaypalFragment();
    }

    //sets the address in the textview on screem
    private void setAddress() {
        settingsValid = prefs.getBoolean(WelcomeActivity.ALLVALID, false);


        if (settingsValid)  {
            String text = prefs.getString("block", "") +  "\n" +
                    prefs.getString("street", "") +  "\n" +
                    prefs.getString("unit", "-") +
                    "\n Singapore(" +   prefs.getString("postal", "") + ")";

            summaryAddressText.setText(text);
            paypalButton.setEnabled(true);
        } else {
            summaryAddressText.setText("Please set up a valid address before continuing.");
            paypalButton.setEnabled(false);
        }
    }

    //gets number of images for each type
    private int getCount(int size, int session) {
        int count = 0;
        //cursor of all the images in current session of required size
        Cursor cursor = database.query(SQLiteHelper.IMAGE_TABLE, new String[] {SQLiteHelper._ID, SQLiteHelper.TYPE, SQLiteHelper.QUANTITY},
                String.format("%s = ? AND %s = ?", SQLiteHelper.TYPE, SQLiteHelper.SESSION),
                new String[]{String.valueOf(size), String.valueOf(session)},
                null, null, null);

        if (cursor.moveToFirst()){
            do {
                count += cursor.getInt(cursor.getColumnIndex(SQLiteHelper.QUANTITY));

            }while (cursor.moveToNext());
        }
        cursor.close();
        return count;

    }

    private int getTotalCount(int session) {
        return database.query(SQLiteHelper.IMAGE_TABLE, new String[] { SQLiteHelper._ID},
               SQLiteHelper.SESSION +"=?", new String[] {String.valueOf(session)}, null, null, null).getCount();

    }


    public void checkVoucherCode () {
        final String checkCode = discountField.getText().toString();

        CheckDiscountCode checkCodeTask = new CheckDiscountCode() {
            @Override
            public void onResult(JSONObject result) {
                String valid = "";

                try {
                    valid = result.getString("validity");
                } catch (JSONException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }

                if (valid.equals("YES")) {
                    Log.d("Discount", "Calculate price called");
                    editor = prefs.edit();
                    editor.putString("voucherCode", checkCode);
                    editor.commit();
                    calculatePrice(result);
                    Toast.makeText(getActivity(), "Voucher Code Applied!", Toast.LENGTH_SHORT).show();
                    discountField.setEnabled(false);


                } else {
                    Toast.makeText(getActivity(), "Not valid or already used...", Toast.LENGTH_SHORT).show();
                }

            }
        };
        checkCodeTask.execute(checkCode);
    }

    public void calculatePrice (JSONObject discounts) {
        int session = prefs.getInt(WelcomeActivity.SESSION, 0);

        Log.d("Tag", String.valueOf(session));

        //find total number of items
        int size4ROrigCount =  getCount (EdittingGridFragment.SIZE4R, session);
        int sizeWalletOrigCount  = getCount(EdittingGridFragment.SIZEWALLET, session);
        int sizeSquareOrigCount = getCount(EdittingGridFragment.SIZESQUARE, session);

        int size4RDiscount =0;
        int sizeWalletDiscount = 0;
        int sizeSquareDiscount = 0;


        //discounts only changes the number of
        if (discounts != null) {
            try {
                JSONArray sizesArray =  discounts.getJSONArray("sizes");
                for (int i = 0 ; i < sizesArray.length(); i++) {
                    JSONObject childObject = sizesArray.getJSONObject(i);
                    String sizeName = childObject.getString("sizeName");
                    int quantity = childObject.getInt("quantity");

                    if (sizeName.equals("k4R")  ) {
                        size4RDiscount = quantity;
                    } else if (sizeName.equals("kWallet") ) {
                        sizeWalletDiscount = quantity;
                    } else if (sizeName.equals("kSquare")) {
                        sizeSquareDiscount = quantity;
                    }
                    Log.d("Discount", String.format("Size: %s & Quantity: %s", sizeName, quantity));
                }
            } catch (JSONException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }

        int size4RCount = size4ROrigCount > size4RDiscount ? size4ROrigCount - size4RDiscount : 0;
        int sizeWalletCount = sizeWalletOrigCount > sizeWalletDiscount ? sizeWalletOrigCount - sizeWalletDiscount : 0;
        int sizeSquareCount = sizeSquareOrigCount > sizeSquareDiscount ? sizeSquareOrigCount - sizeSquareDiscount : 0;

        float size4RCost = calculate4RPrice(size4RCount);
        float sizeWalletCost = calculateWalletPrice(sizeWalletCount);
        float sizeSquareCost = calculateSquarePrice(sizeSquareCount);

        float totalPrintsCost = size4RCost + sizeWalletCost + sizeSquareCost;
        float totalCost  = totalPrintsCost >= 10.0f ? totalPrintsCost : totalPrintsCost + 1f;
        String deliveryCost = totalPrintsCost >= 10.0f ? "0.00" : "1.00";

        SharedPreferences.Editor editor = prefs.edit();
        editor.putFloat("totalCost", totalCost);
        editor.commit();

        //set the values
        size4RQuantityView.setText("(" +  String.valueOf(size4ROrigCount) + ")");
        sizeWalletQuantityView.setText("(" + String.valueOf(sizeWalletOrigCount) + ")");
        sizeSquareQuantityView.setText("("  + String.valueOf(sizeSquareOrigCount) + ")");


        //set discount numbers
        if (size4RDiscount > 0) {
            size4RDiscountView.setText(String.valueOf(size4RCount));
            size4RQuantityView.setPaintFlags(Paint.STRIKE_THRU_TEXT_FLAG);
            size4RQuantityView.setTextColor(Color.RED);
        }

        if (sizeWalletDiscount > 0) {
            sizeWalletDiscountView.setText(String.valueOf(sizeWalletCount));
            sizeWalletQuantityView.setPaintFlags(Paint.STRIKE_THRU_TEXT_FLAG);
            sizeWalletQuantityView.setTextColor(Color.RED);
        }

        if (sizeSquareDiscount > 0) {
            sizeSquareDiscountView.setText(String.valueOf(sizeSquareCount));
            sizeSquareQuantityView.setPaintFlags(Paint.STRIKE_THRU_TEXT_FLAG);
            sizeSquareQuantityView.setTextColor(Color.RED);
        }



        //set the numbers of images of each type
        size4RCostView.setText(formatPrice(size4RCost));
        sizeWalletCostView.setText(formatPrice(sizeWalletCost));
        sizeSquareCostView.setText(formatPrice(sizeSquareCost));
        deliveryCostView.setText(deliveryCost);

        totalCostView.setText(formatPrice(totalCost));
    }

    private float calculateSquarePrice(int sizeSquareCount) {
        float price = 0;
        if (sizeSquareCount <= 5) price = sizeSquareCount * 0.70f;
        else if (sizeSquareCount > 5 && sizeSquareCount <= 10) price = sizeSquareCount * 0.60f;
        else if (sizeSquareCount >=10 ) price = sizeSquareCount * 0.50f;

        return price;
    }

    private float calculateWalletPrice(int sizeWalletCount) {
        float price = 0;
        if (sizeWalletCount <= 5) price =  sizeWalletCount * 0.60f;
        else if (sizeWalletCount > 5 && sizeWalletCount <= 10) price = sizeWalletCount * 0.50f;
        else if (sizeWalletCount >10 ) price = sizeWalletCount * 0.40f;


        return price;
    }

    private float calculate4RPrice(int size4RCount) {
        float price = 0;
        if (size4RCount <= 10) price = size4RCount * 0.50f;
        else if (size4RCount > 10 && size4RCount <=20) price =  size4RCount * 0.45f;
        else if (size4RCount> 20) price = size4RCount *0.40f;

        return price;

    }



    private String formatPrice (float f) {
        DecimalFormat decimalFormat = new DecimalFormat("#0.00");

        return decimalFormat.format(f);
    }




    public class CheckDiscountCode extends AsyncTask<String, Void, JSONObject> implements CheckDiscountCodeInterface {
        @Override
        protected JSONObject doInBackground(String... params) {
            String checkCode = params[0];

            JSONObject result = null;

            HttpClient client = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(checkDiscountUrl);

            try {
                List<NameValuePair> values = new ArrayList<NameValuePair>(2);
                values.add (new BasicNameValuePair("voucherCode", checkCode));
                values.add (new BasicNameValuePair("uid", prefs.getString("uid", "")));

                httpPost.setEntity(new UrlEncodedFormEntity(values));
                HttpResponse response = client.execute(httpPost);
                BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
                StringBuilder builder = new StringBuilder();
                for (String line = null; (line = reader.readLine()) != null; ) builder.append(line).append("\n");
                result = new JSONObject(builder.toString());

            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (JSONException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
            return result;
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            onResult(jsonObject);
        }

        @Override
        public void  onResult(JSONObject result) {
        }
    }


    public interface CheckDiscountCodeInterface  {
        public void onResult(JSONObject result);
    }




    public interface OrderSummaryInterface {
        public void startPaypalFragment();
        public void updateParticulars();
        public void startCreateImagesService();
        public void dialogCancelPressed();
    }

    @Override
    public void onPause() {
        if (database.isOpen())  database.close();
        super.onPause();    //To change body of overridden methods use File | Settings | File Templates.
    }



    @Override
    public void onResume() {
        if (!database.isOpen()) {
            dataSource = new ImageDataSource(getActivity());
            database = dataSource.getDatabase();
        }

        setAddress();

        super.onResume();    //To change body of overridden methods use File | Settings | File Templates.
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            listener = (OrderSummaryInterface) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + "must implement listener");
        }
    }


}
