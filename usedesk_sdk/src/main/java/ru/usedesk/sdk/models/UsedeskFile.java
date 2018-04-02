package ru.usedesk.sdk.models;

public class UsedeskFile {

    private static final String IMAGE_TYPE = "image/";

    private String content;
    private String type;
    private Long size;
    private String name;

    public UsedeskFile() {
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isImage() {
        return type != null && type.startsWith(IMAGE_TYPE);
    }
}