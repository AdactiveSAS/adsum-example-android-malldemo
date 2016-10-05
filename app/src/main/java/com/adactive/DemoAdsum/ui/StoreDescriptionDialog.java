package com.adactive.DemoAdsum.ui;


import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.adactive.DemoAdsum.R;

public class StoreDescriptionDialog extends DialogFragment {

    private View rootView;
    public static final String ARG_STORE_NAME = "store_name";
    private TextView poiNametv;

    // public static final String ARG_STORE_DESCRIPTION = "store_description";

    public interface DialogListener {
        void onDialogClick(DialogFragment dialog, int id);
    }

    DialogListener mListener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mListener = (DialogListener) ((MainActivity) context).getSupportFragmentManager()
                    .findFragmentById(R.id.container);
        } catch (ClassCastException e) {
            throw new ClassCastException(getActivity().toString()
                    + " must implement DialogListener");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        LayoutInflater inflater = getActivity().getLayoutInflater();
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        if ((getArguments().getString(ARG_STORE_NAME)) != null/* && ((getArguments().getString(ARG_STORE_DESCRIPTION))) != null*/) {
            rootView = inflater.inflate(R.layout.dialog_poi_clicked, null);

            poiNametv = (TextView) rootView.findViewById(R.id.poiName);
            TextView poiMessage = (TextView) rootView.findViewById(R.id.poiMessage);

            poiNametv.setText(getArguments().getString(ARG_STORE_NAME));
            poiMessage.setText("Here will be written the description of the Place");

            final Integer PoiID = getArguments().getInt("PoiID");

            final AlertDialog.Builder builder1 = builder.setView(rootView)
                    .setPositiveButton("Show me the way", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            mListener.onDialogClick(StoreDescriptionDialog.this, PoiID);
                        }
                    });

            return builder1.create();

        } else {
            builder.setTitle("Store not referenced")
                    .setMessage(android.text.Html.fromHtml("Not found").toString())
                    .setNegativeButton(R.string.close, null);
            return builder.create();
        }
    }
}
