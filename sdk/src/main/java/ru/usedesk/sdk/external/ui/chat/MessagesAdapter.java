package ru.usedesk.sdk.external.ui.chat;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.List;

import ru.usedesk.sdk.R;
import ru.usedesk.sdk.external.entity.chat.ChatFeedbackListener;
import ru.usedesk.sdk.external.entity.chat.Feedback;
import ru.usedesk.sdk.external.entity.chat.Message;
import ru.usedesk.sdk.internal.utils.DownloadUtils;
import ru.usedesk.sdk.internal.utils.ImageUtils;
import ru.usedesk.sdk.internal.utils.TimeUtils;

public class MessagesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_USER_TEXT = 1;
    private static final int TYPE_USER_FILE = 11;
    private static final int TYPE_USER_TEXT_FILE = 111;

    private static final int TYPE_OPERATOR_TEXT = 2;
    private static final int TYPE_OPERATOR_FILE = 22;
    private static final int TYPE_OPERATOR_TEXT_FILE = 222;
    private static final int TYPE_OPERATOR_FEEDBACK = 2222;

    private static final int TYPE_SERVICE_TEXT = 3;

    private List<Message> messages;
    private ChatFeedbackListener chatFeedbackListener;
    private DownloadUtils downloadUtils;

    MessagesAdapter(Context context, List<Message> messages,
                    ChatFeedbackListener chatFeedbackListener) {
        this.messages = messages;
        this.chatFeedbackListener = chatFeedbackListener;
        downloadUtils = new DownloadUtils(context);
    }

    @Override
    @NonNull
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());

        switch (viewType) {
            case TYPE_USER_TEXT:
                return new ItemUserTextMessageHolder(layoutInflater.inflate(
                        R.layout.usedesk_item_user_text_message, parent, false));
            case TYPE_USER_FILE:
                return new ItemUserFileMessageHolder(layoutInflater.inflate(
                        R.layout.usedesk_item_user_file_message, parent, false));
            case TYPE_USER_TEXT_FILE:
                return new ItemUserTextFileMessageHolder(layoutInflater.inflate(
                        R.layout.usedesk_item_user_text_file_message, parent, false));
            case TYPE_OPERATOR_TEXT:
                return new ItemOperatorTextMessageHolder(layoutInflater.inflate(
                        R.layout.usedesk_item_operator_text_message, parent, false));
            case TYPE_OPERATOR_TEXT_FILE:
                return new ItemOperatorTextFileMessageHolder(layoutInflater.inflate(
                        R.layout.usedesk_item_operator_text_file_message, parent, false));
            case TYPE_OPERATOR_FILE:
                return new ItemOperatorFileMessageHolder(layoutInflater.inflate(
                        R.layout.usedesk_item_operator_file_message, parent, false));
            case TYPE_OPERATOR_FEEDBACK:
                return new ItemOperatorFeedbackMessageHolder(layoutInflater.inflate(
                        R.layout.usedesk_item_operator_feedback_message, parent, false));
            case TYPE_SERVICE_TEXT:
            default:
                return new ItemServiceTextMessageHolder(layoutInflater.inflate(
                        R.layout.usedesk_item_service_text_message, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        final Message message = messages.get(position);

        BaseItemMessageHolder baseItemMessageHolder = (BaseItemMessageHolder) holder;
        if (message.getCreatedAt() != null) {
            String time = TimeUtils.parseTime(message.getCreatedAt());
            if (TextUtils.isEmpty(time)) {
                baseItemMessageHolder.timeTextView.setVisibility(View.GONE);
            } else {
                baseItemMessageHolder.timeTextView.setVisibility(View.VISIBLE);
                baseItemMessageHolder.timeTextView.setText(time);
            }
        }

        switch (getItemViewType(position)) {
            case TYPE_USER_TEXT:
                ItemUserTextMessageHolder itemUserTextMessageHolder = (ItemUserTextMessageHolder) holder;
                itemUserTextMessageHolder.textTextView.setText(message.getText());
                break;
            case TYPE_USER_FILE:
                ItemUserFileMessageHolder itemUserFileMessageHolder = (ItemUserFileMessageHolder) holder;
                if (message.getUsedeskFile().isImage()) {
                    itemUserFileMessageHolder.progressBar.setVisibility(View.VISIBLE);
                    ImageUtils.checkForDisplayImage(
                            itemUserFileMessageHolder.fileImageView,
                            itemUserFileMessageHolder.progressBar,
                            message.getUsedeskFile().getContent());
                } else {
                    itemUserFileMessageHolder.progressBar.setVisibility(View.GONE);
                    itemUserFileMessageHolder.fileImageView.setImageResource(R.drawable.ic_document_black);
                }
                break;
            case TYPE_USER_TEXT_FILE:
                ItemUserTextFileMessageHolder itemUserTextFileMessageHolder = (ItemUserTextFileMessageHolder) holder;

                itemUserTextFileMessageHolder.textTextView.setText(message.getText());

                if (message.getUsedeskFile().isImage()) {
                    itemUserTextFileMessageHolder.progressBar.setVisibility(View.VISIBLE);
                    ImageUtils.checkForDisplayImage(
                            itemUserTextFileMessageHolder.fileImageView,
                            itemUserTextFileMessageHolder.progressBar,
                            message.getUsedeskFile().getContent());
                } else {
                    itemUserTextFileMessageHolder.progressBar.setVisibility(View.GONE);
                    itemUserTextFileMessageHolder.fileImageView.setImageResource(R.drawable.ic_document_black);
                }
                break;
            case TYPE_OPERATOR_TEXT:
                ItemOperatorTextMessageHolder itemOperatorTextMessageHolder = (ItemOperatorTextMessageHolder) holder;
                itemOperatorTextMessageHolder.textTextView.setText(message.getText());
                itemOperatorTextMessageHolder.nameTextView.setText(message.getName());

                checkForDisplayImageOperatorAvatar(message, itemOperatorTextMessageHolder.iconImageView);
                break;
            case TYPE_OPERATOR_FILE:
                ItemOperatorFileMessageHolder itemOperatorFileMessageHolder = (ItemOperatorFileMessageHolder) holder;

                itemOperatorFileMessageHolder.nameTextView.setText(message.getName());

                checkForDisplayImageOperatorAvatar(message, itemOperatorFileMessageHolder.iconImageView);

                if (message.getUsedeskFile().isImage()) {
                    itemOperatorFileMessageHolder.progressBar.setVisibility(View.VISIBLE);
                    ImageUtils.checkForDisplayImage(
                            itemOperatorFileMessageHolder.fileImageView,
                            itemOperatorFileMessageHolder.progressBar,
                            message.getUsedeskFile().getContent());
                } else {
                    itemOperatorFileMessageHolder.progressBar.setVisibility(View.GONE);
                    itemOperatorFileMessageHolder.fileImageView.setImageResource(R.drawable.ic_document_black);
                    itemOperatorFileMessageHolder.itemView.setOnClickListener(view -> {
                        if (message.getUsedeskFile() != null) {
                            downloadUtils.download(message.getUsedeskFile().getName(),
                                    message.getUsedeskFile().getContent());
                        }
                    });
                }
                break;
            case TYPE_OPERATOR_TEXT_FILE:
                ItemOperatorTextFileMessageHolder itemOperatorTextFileMessageHolder = (ItemOperatorTextFileMessageHolder) holder;

                itemOperatorTextFileMessageHolder.textTextView.setText(message.getText());
                itemOperatorTextFileMessageHolder.nameTextView.setText(message.getName());

                checkForDisplayImageOperatorAvatar(message, itemOperatorTextFileMessageHolder.iconImageView);

                if (message.getUsedeskFile().isImage()) {
                    itemOperatorTextFileMessageHolder.progressBar.setVisibility(View.VISIBLE);
                    ImageUtils.checkForDisplayImage(
                            itemOperatorTextFileMessageHolder.fileImageView,
                            itemOperatorTextFileMessageHolder.progressBar,
                            message.getUsedeskFile().getContent());
                } else {
                    itemOperatorTextFileMessageHolder.progressBar.setVisibility(View.GONE);
                    itemOperatorTextFileMessageHolder.fileImageView.setImageResource(R.drawable.ic_document_black);
                    itemOperatorTextFileMessageHolder.itemView.setOnClickListener(view -> {
                        if (message.getUsedeskFile() != null) {
                            downloadUtils.download(message.getUsedeskFile().getName(),
                                    message.getUsedeskFile().getContent());
                        }
                    });
                }
                break;
            case TYPE_OPERATOR_FEEDBACK:
                final ItemOperatorFeedbackMessageHolder itemOperatorFeedbackMessageHolder = (ItemOperatorFeedbackMessageHolder) holder;
                itemOperatorFeedbackMessageHolder.nameTextView.setText(message.getName());
                itemOperatorFeedbackMessageHolder.textTextView.setText(message.getText());

                checkForDisplayImageOperatorAvatar(message, itemOperatorFeedbackMessageHolder.iconImageView);

                if (message.getPayload().hasFeedback()) {
                    itemOperatorFeedbackMessageHolder.likeButton.setEnabled(false);
                    itemOperatorFeedbackMessageHolder.dislikeButton.setEnabled(false);
                } else {
                    itemOperatorFeedbackMessageHolder.likeButton.setEnabled(true);
                    itemOperatorFeedbackMessageHolder.dislikeButton.setEnabled(true);

                    itemOperatorFeedbackMessageHolder.likeButton.setOnClickListener(view -> {
                        chatFeedbackListener.onFeedbackSet(Feedback.LIKE);
                        view.setEnabled(false);
                        itemOperatorFeedbackMessageHolder.dislikeButton.setEnabled(false);
                    });
                    itemOperatorFeedbackMessageHolder.dislikeButton.setOnClickListener(view -> {
                        chatFeedbackListener.onFeedbackSet(Feedback.DISLIKE);
                        view.setEnabled(false);
                        itemOperatorFeedbackMessageHolder.likeButton.setEnabled(false);
                    });
                }
                break;
            case TYPE_SERVICE_TEXT:
                ItemServiceTextMessageHolder itemServiceTextMessageHolder = (ItemServiceTextMessageHolder) holder;
                itemServiceTextMessageHolder.textTextView.setText(message.getText());
                break;
        }
    }

    @Override
    public int getItemViewType(int position) {
        int itemViewType = TYPE_SERVICE_TEXT;
        Message message = messages.get(position);
        boolean hasText = !TextUtils.isEmpty(message.getText());
        boolean hasFile = message.getUsedeskFile() != null;

        switch (message.getType()) {
            case CLIENT_TO_OPERATOR:
            case CLIENT_TO_BOT:
                if (hasText && hasFile) {
                    itemViewType = TYPE_USER_TEXT_FILE;
                } else if (hasText) {
                    itemViewType = TYPE_USER_TEXT;
                } else if (hasFile) {
                    itemViewType = TYPE_USER_FILE;
                }
                break;
            case OPERATOR_TO_CLIENT:
            case BOT_TO_CLIENT:
                boolean hasFeedbackButtons = message.getPayload().getFeedbackButtons() != null;

                if (hasFeedbackButtons) {
                    itemViewType = TYPE_OPERATOR_FEEDBACK;
                } else if (hasText && hasFile) {
                    itemViewType = TYPE_OPERATOR_TEXT_FILE;
                } else if (hasText) {
                    itemViewType = TYPE_OPERATOR_TEXT;
                } else if (hasFile) {
                    itemViewType = TYPE_OPERATOR_FILE;
                }
                break;
            case SERVICE:
                itemViewType = TYPE_SERVICE_TEXT;
                break;
        }

        return itemViewType;
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    private void checkForDisplayImageOperatorAvatar(Message message, ImageView imageView) {
        if (message.getPayload() != null) {
            ImageUtils.checkForDisplayImage(
                    imageView,
                    message.getPayload().getAvatar(),
                    R.drawable.ic_operator_black);
        }
    }

    private class ItemOperatorFeedbackMessageHolder extends BaseItemMessageHolder {

        ImageView iconImageView;
        TextView nameTextView;
        TextView textTextView;
        ImageButton likeButton;
        ImageButton dislikeButton;

        ItemOperatorFeedbackMessageHolder(View itemView) {
            super(itemView);
            iconImageView = itemView.findViewById(R.id.icon_image_view);
            nameTextView = itemView.findViewById(R.id.name_text_view);
            textTextView = itemView.findViewById(R.id.text_text_view);
            likeButton = itemView.findViewById(R.id.like_button);
            dislikeButton = itemView.findViewById(R.id.dislike_button);
        }
    }

    private class ItemUserTextMessageHolder extends BaseItemTextMessageHolder {

        ItemUserTextMessageHolder(View itemView) {
            super(itemView);
        }
    }

    private class ItemOperatorTextMessageHolder extends BaseItemTextMessageHolder {

        ImageView iconImageView;
        TextView nameTextView;

        ItemOperatorTextMessageHolder(View itemView) {
            super(itemView);
            iconImageView = itemView.findViewById(R.id.icon_image_view);
            nameTextView = itemView.findViewById(R.id.name_text_view);
        }
    }

    private class ItemServiceTextMessageHolder extends BaseItemTextMessageHolder {

        ItemServiceTextMessageHolder(View itemView) {
            super(itemView);
        }
    }

    private abstract class BaseItemTextMessageHolder extends BaseItemMessageHolder {

        TextView textTextView;

        BaseItemTextMessageHolder(View itemView) {
            super(itemView);
            textTextView = itemView.findViewById(R.id.text_text_view);
        }
    }

    private class ItemUserFileMessageHolder extends BaseItemFileMessageHolder {

        ItemUserFileMessageHolder(View itemView) {
            super(itemView);
        }
    }

    private class ItemOperatorFileMessageHolder extends BaseItemFileMessageHolder {

        ImageView iconImageView;
        TextView nameTextView;

        ItemOperatorFileMessageHolder(View itemView) {
            super(itemView);
            iconImageView = itemView.findViewById(R.id.icon_image_view);
            nameTextView = itemView.findViewById(R.id.name_text_view);
        }
    }

    private abstract class BaseItemFileMessageHolder extends BaseItemMessageHolder {

        ImageView fileImageView;
        ProgressBar progressBar;

        BaseItemFileMessageHolder(View itemView) {
            super(itemView);
            fileImageView = itemView.findViewById(R.id.file_image_view);
            progressBar = itemView.findViewById(R.id.progress_bar);
        }
    }

    private class ItemUserTextFileMessageHolder extends BaseItemTextFileMessageHolder {

        ItemUserTextFileMessageHolder(View itemView) {
            super(itemView);
        }
    }

    private class ItemOperatorTextFileMessageHolder extends BaseItemTextFileMessageHolder {

        ImageView iconImageView;
        TextView nameTextView;

        ItemOperatorTextFileMessageHolder(View itemView) {
            super(itemView);
            iconImageView = itemView.findViewById(R.id.icon_image_view);
            nameTextView = itemView.findViewById(R.id.name_text_view);
        }
    }

    private abstract class BaseItemTextFileMessageHolder extends BaseItemMessageHolder {

        TextView textTextView;
        ImageView fileImageView;
        ProgressBar progressBar;

        BaseItemTextFileMessageHolder(View itemView) {
            super(itemView);
            textTextView = itemView.findViewById(R.id.text_text_view);
            fileImageView = itemView.findViewById(R.id.file_image_view);
            progressBar = itemView.findViewById(R.id.progress_bar);
        }
    }

    private abstract class BaseItemMessageHolder extends RecyclerView.ViewHolder {

        TextView timeTextView;

        BaseItemMessageHolder(View itemView) {
            super(itemView);
            timeTextView = itemView.findViewById(R.id.time_text_view);
        }
    }
}