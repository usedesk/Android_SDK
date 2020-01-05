package ru.usedesk.chat_gui.screens.utils;

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;

public class DownloadUtils {

    private Context context;

    public DownloadUtils(Context context) {
        this.context = context;
    }

    public void download(String name, String url) {
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        request.setTitle(name);

        request.allowScanningByMediaScanner();
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

        DownloadManager manager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        manager.enqueue(request);
    }
}