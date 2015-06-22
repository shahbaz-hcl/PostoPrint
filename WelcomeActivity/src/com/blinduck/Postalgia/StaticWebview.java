package com.blinduck.Postalgia;

import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.os.Bundle;
import android.webkit.WebView;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

import java.io.FileReader;

/**
 * Created with IntelliJ IDEA.
 * User: deepan
 * Date: 7/16/13
 * Time: 7:27 PM
 * To change this template use File | Settings | File Templates.
 */
public class StaticWebview extends SherlockActivity {

    private WebView webView;

    public void onCreate(Bundle savedInstanceState) {
        Intent intent = getIntent();
        int type = intent.getIntExtra("type", 0);
        String url = "";

        switch (type) {
            case 1:
                url = "file:///android_asset/faq.html";
                break;
            case 2:
                url = "file:///android_asset/pricing.html";
                break;

        }


        super.onCreate(savedInstanceState);
        setContentView(R.layout.static_webview);

        webView = (WebView)findViewById(R.id.static_webview);
        webView.loadUrl(url);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getSupportMenuInflater();
        inflater.inflate(R.menu.static_webview, menu);

        return super.onCreateOptionsMenu(menu);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.static_back) {
            finish();
        }

        return super.onOptionsItemSelected(item);    //To change body of overridden methods use File | Settings | File Templates.
    }
}