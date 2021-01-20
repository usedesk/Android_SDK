package ru.usedesk.common_sdk.internal;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.webkit.MimeTypeMap;

public class UsedeskFileUtil {
    public static String getFileName(Context context, Uri uri) {
        return getFileName(context.getContentResolver(), uri);
    }

    public static String getFileName(ContentResolver contentResolver, Uri uri) {
        String result = null;
        String schema = uri.getScheme();
        if (schema != null && schema.equals("content")) {
            try (Cursor cursor = contentResolver.query(uri,
                    null,
                    null,
                    null,
                    null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            }
        }
        if (result == null) {
            String path = uri.getPath();
            if (path != null) {
                int cut = path.lastIndexOf('/');
                if (cut >= 0) {
                    result = path.substring(cut + 1);
                } else {
                    result = path;
                }
            }
        }
        if (result != null) {
            return result;
        } else {
            return "";
        }
    }

    public static String getMimeType(Context context, Uri uri) {
        return getMimeType(context.getContentResolver(), uri);
    }


    public static String getMimeType(ContentResolver contentResolver, Uri uri) {
        String mimeType;
        if (ContentResolver.SCHEME_CONTENT.equals(uri.getScheme())) {
            mimeType = contentResolver.getType(uri);
        } else {
            String extension = MimeTypeMap.getFileExtensionFromUrl(uri.toString()).toLowerCase();
            mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        if (mimeType != null) {
            return mimeType;
        } else {
            return "";
        }
    }
}
