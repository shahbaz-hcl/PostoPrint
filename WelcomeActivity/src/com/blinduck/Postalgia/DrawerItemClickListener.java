package com.blinduck.Postalgia;

import android.app.Fragment;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

/**
 * Created with IntelliJ IDEA.
 * User: deepan
 * Date: 6/21/13
 * Time: 12:30 AM
 * To change this template use File | Settings | File Templates.
 */
public class DrawerItemClickListener implements ListView.OnItemClickListener {
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        selectItem (position);
    }

    private void selectItem( int position) {
        Log.i("Menu", "Clicked:" + position);


    }
}
