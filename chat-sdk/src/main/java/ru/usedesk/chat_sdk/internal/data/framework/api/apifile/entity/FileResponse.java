package ru.usedesk.chat_sdk.internal.data.framework.api.apifile.entity;

import com.google.gson.annotations.SerializedName;

public class FileResponse {
    Integer status;
    @SerializedName("file_link")
    String fileLink;
    String size;
    String id;
    String type;
    String name;
}
