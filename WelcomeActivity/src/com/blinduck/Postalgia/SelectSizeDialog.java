package com.blinduck.Postalgia;

import android.app.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.*;
import android.view.inputmethod.EditorInfo;
import android.widget.*;
import com.actionbarsherlock.app.ActionBar;

/**
 * Created with IntelliJ IDEA.
 * User: deepan
 * Date: 6/28/13
 * Time: 12:00 PM
 * To change this template use File | Settings | File Templates.
 */
public class SelectSizeDialog extends DialogFragment {

    private ListView mListView;

    //Empty constructor
    public SelectSizeDialog() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Bundle bundle = getArguments();

        final Intent intent = new Intent();
        intent.putExtras(bundle);

        View view = inflater.inflate(R.layout.dialog_layout, container);
        mListView = (ListView) view.findViewById(R.id.edit_dialog_list);

        mListView.setAdapter(new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_list_item_1,
                EdittingGridFragment.options));

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                getTargetFragment().onActivityResult(getTargetRequestCode(),
                        position,
                        intent);

                getDialog().dismiss();
            }
        });

        return view;
    }



}