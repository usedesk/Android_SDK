package ru.usedesk.common_gui.internal;

import android.Manifest;
import android.view.View;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

public class PermissionUtil {

    public static void needWriteExternalPermission(View view,
                                                   Runnable permissionListener,
                                                   Runnable errorListener) {
        needPermission(view, permissionListener, errorListener,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }

    public static void needReadExternalPermission(View view, Runnable permissionListener,
                                                  Runnable errorListener) {
        needPermission(view, permissionListener, errorListener,
                Manifest.permission.READ_EXTERNAL_STORAGE);
    }

    public static void needCameraPermission(View view, Runnable permissionListener,
                                            Runnable errorListener) {
        needPermission(view, permissionListener, errorListener,
                Manifest.permission.CAMERA);
    }

    public static void needPermission(View view,
                                      Runnable permissionListener,
                                      Runnable errorListener,
                                      String permission) {
        Dexter.withContext(view.getContext())
                .withPermission(permission)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                        permissionListener.run();
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {
                        errorListener.run();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {

                    }
                })
                .check();
    }
}
