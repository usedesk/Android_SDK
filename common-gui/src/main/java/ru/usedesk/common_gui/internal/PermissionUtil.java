package ru.usedesk.common_gui.internal;

import android.Manifest;
import android.view.View;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.karumi.dexter.listener.single.SnackbarOnDeniedPermissionListener;

public class PermissionUtil {

    public static void needWriteExternalPermission(View view,
                                                   Runnable permissionListener,
                                                   int errorTitleId,
                                                   int errorButtonId) {
        needPermission(view, permissionListener, errorTitleId, errorButtonId,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }

    public static void needReadExternalPermission(View view,
                                                  Runnable permissionListener,
                                                  int errorTitleId,
                                                  int errorButtonId) {
        needPermission(view, permissionListener, errorTitleId, errorButtonId,
                Manifest.permission.READ_EXTERNAL_STORAGE);
    }

    public static void needCameraPermission(View view,
                                            Runnable permissionListener,
                                            int errorTitleId,
                                            int errorButtonId) {
        needPermission(view, permissionListener, errorTitleId, errorButtonId,
                Manifest.permission.CAMERA);
    }

    public static void needPermission(View view,
                                      Runnable permissionListener,
                                      int errorTitleId,
                                      int errorButtonId,
                                      String permission) {
        Dexter.withContext(view.getContext())
                .withPermission(permission)
                .withListener(new SnackbarPermissionListener(view, errorTitleId, errorButtonId, permissionListener))
                .check();
    }

    private static class SnackbarPermissionListener implements PermissionListener {
        private final PermissionListener permissionListener;
        private final Runnable onGranted;

        SnackbarPermissionListener(View view,
                                   int errorTitleId,
                                   int errorButtonId,
                                   Runnable onGranted) {
            this.permissionListener = SnackbarOnDeniedPermissionListener.Builder
                    .with(view, errorTitleId)
                    .withOpenSettingsButton(errorButtonId).build();
            this.onGranted = onGranted;
        }

        @Override
        public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
            permissionListener.onPermissionGranted(permissionGrantedResponse);
            onGranted.run();
        }

        @Override
        public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {
            permissionListener.onPermissionDenied(permissionDeniedResponse);
        }

        @Override
        public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest,
                                                       PermissionToken permissionToken) {
            permissionListener.onPermissionRationaleShouldBeShown(permissionRequest, permissionToken);
        }
    }
}
