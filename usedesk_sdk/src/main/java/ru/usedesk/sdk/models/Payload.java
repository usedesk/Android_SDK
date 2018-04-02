package ru.usedesk.sdk.models;

import android.text.TextUtils;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import static ru.usedesk.sdk.Constants.KEY_FEEDBACK_BUTTONS;
import static ru.usedesk.sdk.Constants.KEY_TICKET_ID;

public class Payload {

    @SerializedName(KEY_TICKET_ID)
    private long ticketId;

    @SerializedName(KEY_FEEDBACK_BUTTONS)
    private List<FeedbackButton> feedbackButtons;

    private boolean csi;
    private String userRating;
    private String avatar;

    public Payload() {
    }

    public long getTicketId() {
        return ticketId;
    }

    public List<FeedbackButton> getFeedbackButtons() {
        return feedbackButtons;
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