package ru.usedesk.sdk.domain.entity;

import android.text.TextUtils;

import java.util.List;

public class Payload {

    private long ticketId;

    private List<FeedbackButton> buttons;

    private boolean csi;
    private String userRating;
    private String avatar;

    public Payload() {
    }

    public long getTicketId() {
        return ticketId;
    }

    public List<FeedbackButton> getFeedbackButtons() {
        return buttons;
    }

    public boolean isCsi() {
        return csi;
    }

    public boolean hasFeedback() {
        return !TextUtils.isEmpty(userRating);
    }

    public String getAvatar() {
        return avatar;
    }
}