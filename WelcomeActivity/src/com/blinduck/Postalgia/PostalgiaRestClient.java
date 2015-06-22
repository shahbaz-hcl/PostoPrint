package com.blinduck.Postalgia;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

/**
 * Created with IntelliJ IDEA.
 * User: deepan
 * Date: 7/5/13
 * Time: 11:24 AM
 * To change this template use File | Settings | File Templates.
 */
public class PostalgiaRestClient {

    private static final String BASE_URL = "http://www.postalgiaprints.com/api/v1/";

    private static AsyncHttpClient client = new AsyncHttpClient();



    public static void get(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.get(getAbsoluteUrl(url), params, responseHandler);
    }

    public static void post(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.post(getAbsoluteUrl(url), params, responseHandler);
    }

    public static void put(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.put(getAbsoluteUrl(url), params, responseHandler);
    }


    public static void jsonPost (String url, RequestParams params, JsonHttpResponseHandler handler) {
        client.post(getAbsoluteUrl(url), params, handler);
    }



    private static String getAbsoluteUrl(String relativeUrl) {
        return BASE_URL + relativeUrl;
    }

}
