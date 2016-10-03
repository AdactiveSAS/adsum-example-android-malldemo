package com.adactive.DemoAdsum.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

/**
 * Example of permission handling :
 * ask user to confirm permissions requested by the app.
 *
 * Note that since Android 6, it has become mandatory for the app to ask the end user explicitly
 */
public class PermissionsUtils {

    public static boolean checkAndRequest(@NonNull final Activity activity,
                                          @NonNull final String[] permissions,
                                          String messagePermission,
                                          final int requestCode,
                                          DialogInterface.OnClickListener onCancelListener) {
        boolean result = false;

        // get the permissions to request
        List<String> permissionsToRequest = new ArrayList<>();
        boolean needExplanation = false;

        for (int i = 0; i < permissions.length; i++) {
            String permission = permissions[i];
            if (ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(permission);
                needExplanation = true;
            }
        }
        final String[] permissionsToRequestArray = permissionsToRequest.toArray(new String[permissionsToRequest.size()]);

        if (permissionsToRequestArray.length > 0) {
            if (needExplanation) {
                // Shows an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                builder.setMessage(messagePermission).setPositiveButton(activity.getResources().getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityCompat.requestPermissions(activity, permissionsToRequestArray, requestCode);
                    }
                }).setNegativeButton(activity.getResources().getString(android.R.string.cancel), onCancelListener).show();
            } else {
                ActivityCompat.requestPermissions(activity, permissionsToRequestArray, requestCode);
            }
        }else{
            return true;
        }

        return result;
    }
}