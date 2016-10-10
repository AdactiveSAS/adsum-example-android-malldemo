package com.adactive.DemoAdsum.ui;


import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;

import com.adactive.DemoAdsum.R;

import java.io.File;

public class StoreDescriptionDialog extends DialogFragment {

    private View rootView;
    public static final String ARG_STORE_NAME = "store_name";
    public static final String ARG_STORE_DESCRIPTION = "store_description";
    public static final String ARG_LOGO_PATH = "logo_path";
    private TextView poiNametv;

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
        if ((getArguments().getString(ARG_STORE_NAME)) != null) {

            rootView = inflater.inflate(R.layout.dialog_poi_clicked, null);
            poiNametv = (TextView) rootView.findViewById(R.id.poiName);
            WebView poiMessage = (WebView) rootView.findViewById(R.id.webV);

            //set name
            poiNametv.setText(getArguments().getString(ARG_STORE_NAME));
            File pathFile = getContext().getFilesDir();
            Log.d("eipnf", pathFile.getAbsolutePath());
            //this part is necessary to have the description justified
            String text = "<html><body style='text-align:justify'>"
                    + ((getArguments().getString(ARG_STORE_DESCRIPTION)))
                    + "</body></html>";
            poiMessage.loadDataWithBaseURL(null, (text), "text/html", "utf-8", null);
            String path = getArguments().getString(ARG_LOGO_PATH);
            //loading logo
            File temp = new File(path);

            ImageView myImage = (ImageView) rootView.findViewById(R.id.logo_dialog);
            myImage.setImageURI(Uri.fromFile(temp));

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
