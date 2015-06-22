package com.blinduck.Postalgia;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

/**
 * Created with IntelliJ IDEA.
 * User: deepan
 * Date: 8/7/13
 * Time: 12:19 PM
 * To change this template use File | Settings | File Templates.
 */
public class CheckoutConfirmationDialog extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        String message = "Photos will be printed as seen on the Customize page.\nSelect Back to double check, or Proceed to continue";

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setMessage(message)
                .setPositiveButton("Proceed", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        getTargetFragment().onActivityResult(getTargetRequestCode(), 1, null);
                    }
                })
                .setNegativeButton("Back", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        getTargetFragment().onActivityResult(getTargetRequestCode(), 0, null);
                    }
                });

        return builder.create();

    }
}
