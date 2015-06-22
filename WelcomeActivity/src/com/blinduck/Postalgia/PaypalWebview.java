package com.blinduck.Postalgia;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragment;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import org.apache.http.*;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

/**
 * Created with IntelliJ IDEA.
 * User: deepan
 * Date: 7/9/13
 * Time: 10:58 AM
 * To change this template use File | Settings | File Templates.
 */
public class PaypalWebview extends SherlockFragment {

    private PaypalInterface listener;
    private WebView webview;
    private  SharedPreferences prefs;




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.paypal_webview, container, false);
        webview = (WebView)view.findViewById(R.id.paypal_webview);
        webview.requestFocus();
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        ActionBar actionBar = getSherlockActivity().getSupportActionBar();
        actionBar.setTitle("Payment");


        super.onActivityCreated(savedInstanceState);
        prefs = getActivity().getSharedPreferences(WelcomeActivity.PREFS_NAME, Context.MODE_PRIVATE);
        final String baseUrl = "https://www.paypal.com/webscr&cmd=_express-checkout&token=";
        final String serverUrl = "http://www.postalgiaprints.com/api/v1/orders/";
        webview.getSettings().setJavaScriptEnabled(true);

        final Activity activity = getActivity();

        webview.setWebChromeClient(new WebChromeClient(){
            @Override
            public void onProgressChanged(WebView view, int progress) {
                activity.setTitle("Loading...");
                activity.setProgress(progress * 100);
                //Todo this set progress should be attached to something

                if(progress == 100)
                    activity.setTitle(R.string.app_name);
            }
        });

        webview.setWebViewClient(new WebViewClient(){

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                if (Uri.parse(url).getHost().equals("54.251.47.201")) {
                    Log.d("Url", "Finished Paypal");
                    webview.stopLoading();
                    if (url.contains("success")){
                        Log.d ("Url", "Trigger Success");
                        listener.paymentSuccessful();
                    }
                    else if  (url.contains("cancel")) {
                        Log.d ("Url", "Cancelled");
                        listener.paymentUnsucessful();
                    }
                }
                Log.d("Url", "Page Started Url:" + url);
                Log.d("Url", "Host:" + Uri.parse(url).getHost());
                Log.d("Url", "Path segments: " +  Uri.parse(url).getQueryParameter("PayerID"));
            }

        });

        CreateOrderAsyncTask task = new CreateOrderAsyncTask() {
            @Override
            public void paypalTokenReceived(String paypal_token) {
                //Toast.makeText(getActivity(), paypal_token, Toast.LENGTH_SHORT).show();
                if (paypal_token  !=  null && !paypal_token.isEmpty()) webview.loadUrl(baseUrl + paypal_token);
                else listener.paypalNetworkUnavailable();
            }
        };
        if (networkAvailable()) {
            task.execute(serverUrl);
        } else {
            Toast.makeText(getActivity(),
                    "Network not available, please try again later",
                    Toast.LENGTH_LONG).show();
            listener.paypalNetworkUnavailable();
        }

    }


    public interface PaypalInterface {
        public void paymentSuccessful ();
        public void paymentUnsucessful();
        public void paypalNetworkUnavailable ();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            listener = (PaypalInterface) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + "must implement listener");
        }
    }


    public class CreateOrderAsyncTask extends AsyncTask <String, Void, String> implements AsyncInterface {

        private ProgressDialog pd;

        @Override
        protected void onPreExecute() {
            pd = new ProgressDialog(getActivity());
            pd.setTitle("Connecting to Paypal...");
            pd.setMessage("Please wait.");
            pd.setCancelable(false);
            pd.setIndeterminate(true);
            pd.show();
        }

        @Override
        protected String doInBackground(String... values) {
            String url = values[0];


            //Check if user has been created
            if (!prefs.getBoolean(WelcomeActivity.USERCREATED, false)) {
                Log.d("Postal", "User being created");
                createUser(prefs.getString("uid", ""));
            }


            //Update the user details
            if (updateUserDetails(prefs.getString("uid", ""))) {
                //if user details successfully created proceed to create order
                Log.d ("Postal", "Update User details returned true");
            //then create the order
                return createOrder (url);
            } else {
                listener.paymentUnsucessful();
                Log.d("Postal", "update user details returned false");
                //Todo add in error check
            };

            return null;


        }

        private String createOrder(String url) {
            HttpClient httpClient = new DefaultHttpClient();
            HttpPost httppost  = new HttpPost(url);
            String paypal_token =  "";

            try {
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(3);
                nameValuePairs.add(new BasicNameValuePair("uid", prefs.getString("uid", "")));
                nameValuePairs.add(new BasicNameValuePair("cartCount", String.valueOf(prefs.getInt("cartCount", 0))));
                nameValuePairs.add(new BasicNameValuePair("totalCost", String.valueOf(prefs.getFloat("totalCost", 0f))));
                String full_name = prefs.getString("firstName", "") + " " +  prefs.getString("lastName", "");
                Log.d("Postal", "fullname: " + full_name);
                nameValuePairs.add(new BasicNameValuePair("full_name", full_name));


                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                HttpResponse response = httpClient.execute(httppost);

                BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(),"UTF-8"));
                StringBuilder builder = new StringBuilder();
                for (String line = null; (line = reader.readLine()) != null; ) {
                    builder.append(line).append("\n");
                }


                JSONObject result = new JSONObject(builder.toString());

                paypal_token = (String) result.get("paypal_token");
                String orderId = (String)result.get("orderId");

                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("paypal_token", paypal_token);
                editor.putString("orderId", orderId);

                editor.commit();
                Log.d("Postal","JSON Builder Text from create order: " +  builder.toString());
            }


            //TODO checks needed for cannot get network connection
            catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (JSONException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
            return paypal_token;
        }


        private void createUser (String uid) {
            HttpClient client = new DefaultHttpClient();
            String url = "http://www.postalgiaprints.com/api/v1/appUsers/" +uid + "/";
            HttpPut put = new HttpPut(url);



            try {
                List <NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
                nameValuePairs.add(new BasicNameValuePair("uid", uid));
                put.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                HttpResponse response = client.execute(put);
            }  catch (ClientProtocolException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }


        }

        private boolean updateUserDetails(String uid) {
            HttpClient client = new DefaultHttpClient();
            String url = "http://www.postalgiaprints.com/api/v1/appUsers/" +uid + "/";
            HttpPut put = new HttpPut(url);

            Log.d("Postal", "Update user details url:" + url);

            HttpResponse response = null;

            try {
                List <NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(9);
                nameValuePairs.add(new BasicNameValuePair("uid", uid));
                nameValuePairs.add(new BasicNameValuePair("firstName", prefs.getString("firstName", "")));
                nameValuePairs.add(new BasicNameValuePair("lastName", prefs.getString("lastName", "")));
                nameValuePairs.add(new BasicNameValuePair("phoneNumber", prefs.getString("phoneNumber", "")));
                nameValuePairs.add(new BasicNameValuePair("email", prefs.getString("email", "")));
                nameValuePairs.add(new BasicNameValuePair("block", prefs.getString("block", "")));
                nameValuePairs.add(new BasicNameValuePair("streetName", prefs.getString("street", "")));
                nameValuePairs.add(new BasicNameValuePair("unitNumber", prefs.getString("unit","")));
                nameValuePairs.add(new BasicNameValuePair("postalCode", prefs.getString("postal", "")));

                put.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                response = client.execute(put);

                Log.d("Postal", "Update user details response:" + response.toString());
                Log.d("Postal", "Update user details response status line: " + response.getStatusLine());





            } catch (ClientProtocolException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }

            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) return true;
            else return false;

        }



        @Override
        protected void onPostExecute(String s) {
            pd.dismiss();
            paypalTokenReceived(s);
        }


        @Override
        public void paypalTokenReceived(String paypal_token) {
        }
    }

    public interface AsyncInterface {
        public void paypalTokenReceived(String paypal_token);
    }


    private boolean networkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
    }


}

