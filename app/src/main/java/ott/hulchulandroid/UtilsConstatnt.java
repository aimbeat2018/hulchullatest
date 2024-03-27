package ott.hulchulandroid;


import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.text.TextUtils;
import android.view.View;
import android.widget.RelativeLayout;

import androidx.core.app.ActivityCompat;

public class UtilsConstatnt {


    public static boolean checkPermissions(Activity context) {


        String[] PERMISSIONS;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            PERMISSIONS = new String[]{
                    android.Manifest.permission.READ_MEDIA_IMAGES,
                    android.Manifest.permission.CAMERA,
            };


        }else{
            PERMISSIONS = new String[]{
                    android.Manifest.permission.READ_EXTERNAL_STORAGE,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
//                    Manifest.permission.CAMERA,
            };
        }


        if (!hasPermissions(context, PERMISSIONS)) {
            Dialog dialog = new Dialog(context);
            dialog.setCancelable(true);
            dialog.setContentView(R.layout.item_permission_dialog);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

            final RelativeLayout okBtn = dialog.findViewById(R.id.okBtn);

            okBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        context.requestPermissions(PERMISSIONS, 2);
                    }
                    dialog.cancel();
                }
            });
            dialog.show();
        } else {
            return true;
        }

        return false;
    }


    public static boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

}
