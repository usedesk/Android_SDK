package ru.usedesk.sdk.external.entity.chat;

public class UsedeskFile {

    private static final String IMAGE_TYPE = "image/";

    private String content;
    private String type;
    private String size;
    private String name;

    public UsedeskFile() {
    }

    public UsedeskFile(String content, String type, String size, String name) {
        this.content = content;
        this.type = type;
        this.size = size;
        this.name = name;
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

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isImage() {
        return type != null && type.startsWith(IMAGE_TYPE) ||
                name != null && endsLikeImage();
    }

    private boolean endsLikeImage() {
        return name.endsWith(".png") || name.endsWith(".jpg") ||
                name.endsWith(".bmp") || name.endsWith(".jpeg");
    }
}