package ru.usedesk.sdk.utils;

import android.util.Base64;
import android.util.Base64OutputStream;
import android.webkit.MimeTypeMap;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import ru.usedesk.sdk.models.UsedeskFile;

public class AttachmentUtils {

    private static final String CONTENT_FORMAT = "data:%1s;base64,%2s";

    private AttachmentUtils() {
    }

    public static String convertToBase64(String filePath) {
        File file = new File(filePath);
        InputStream inputStream;
        String encodedFile = "";
        String lastVal;
        try {
            inputStream = new FileInputStream(file.getAbsolutePath());

            byte[] buffer = new byte[10240];//specify the size to allow
            int bytesRead;
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            Base64OutputStream output64 = new Base64OutputStream(output, Base64.DEFAULT);

            while ((bytesRead = inputStream.read(buffer)) != -1) {
                output64.write(buffer, 0, bytesRead);
            }

            output64.close();

            encodedFile = output.toString();
        } catch (Exception e) {
            LogUtils.LOGE(AttachmentUtils.class.getSimpleName(), e);
        }

        lastVal = encodedFile;

        return lastVal;
    }

    public static List<UsedeskFile> createUsedeskFiles(ArrayList<String> paths) {
        List<UsedeskFile> usedeskFiles = new ArrayList<>();

        for (String path : paths) {
            if (path != null) {
                usedeskFiles.add(createUsedeskFile(path));
            }
        }

        return usedeskFiles;
    }

    public static UsedeskFile createUsedeskFile(String path) {
        File file = new File(path);
        String fileMimeType = getMimeType(path);

        UsedeskFile usedeskFile = new UsedeskFile();
        usedeskFile.setName(file.getName());
        usedeskFile.setContent(String.format(CONTENT_FORMAT, fileMimeType, convertToBase64(path)));
        usedeskFile.setType(fileMimeType);

        return usedeskFile;
    }

    private static String getMimeType(String path) {
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(path);
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        return type;
    }
}