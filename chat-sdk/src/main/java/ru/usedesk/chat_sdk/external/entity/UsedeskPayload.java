package ru.usedesk.chat_sdk.external.entity;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class UsedeskPayload {
    private static final String KEY_TICKET_ID = "ticket_id";
    private static final String KEY_FEEDBACK_BUTTONS = "buttons";

    @SerializedName(KEY_TICKET_ID)
    private long ticketId;

    @SerializedName(KEY_FEEDBACK_BUTTONS)
    private List<UsedeskFeedbackButton> feedbackButtons;

    private boolean csi;
    private String userRating;
    private String avatar;

    public UsedeskPayload() {
    }

    public long getTicketId() {
        return ticketId;
    }

    public List<UsedeskFeedbackButton> getFeedbackButtons() {
        return feedbackButtons;
    }

    public boolean isCsi() {
        return csi;
    }

    public boolean hasFeedback() {
        return feedbackButtons != null && (userRating == null || userRating.isEmpty());
    }

    public String getAvatar() {
        return avatar;
    }
}