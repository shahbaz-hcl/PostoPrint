package com.blinduck.Postalgia;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

public class EditAllDialogFragment extends DialogFragment {



    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        String [] choices = new String[] {"Set All: 4R", "Set All: Wallet", "Set All: Square", "Delete All"};

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setItems(choices, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int position) {
                getTargetFragment().onActivityResult(getTargetRequestCode(), position, null );

            }
        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dismiss();
            }
        });


        return builder.create();
    }
}
