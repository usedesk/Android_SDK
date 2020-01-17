package ru.usedesk.chat_gui.internal.messages;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import ru.usedesk.chat_gui.R;
import ru.usedesk.chat_gui.internal.utils.DownloadUtils;
import ru.usedesk.chat_gui.internal.utils.ImageUtils;
import ru.usedesk.chat_gui.internal.utils.TimeUtils;
import ru.usedesk.chat_sdk.external.UsedeskChatSdk;
import ru.usedesk.chat_sdk.external.entity.ChatFeedbackListener;
import ru.usedesk.chat_sdk.external.entity.Feedback;
import ru.usedesk.chat_sdk.external.entity.Message;
import ru.usedesk.chat_sdk.external.entity.MessageButtons;

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
    private RecyclerView recyclerView;

    public MessagesAdapter(@NonNull RecyclerView recyclerView,
                           @NonNull List<Message> messages,
                           @NonNull ChatFeedbackListener chatFeedbackListener) {
        this.recyclerView = recyclerView;
        this.messages = messages;
        this.chatFeedbackListener = chatFeedbackListener;
        downloadUtils = new DownloadUtils(recyclerView.getContext());
    }

    @Override
    @NonNull
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType) {
            case TYPE_USER_TEXT:
                return new ItemUserTextMessageHolder(parent);
            case TYPE_USER_FILE:
                return new ItemUserFileMessageHolder(parent);
            case TYPE_USER_TEXT_FILE:
                return new ItemUserTextFileMessageHolder(parent);
            case TYPE_OPERATOR_TEXT:
                return new ItemOperatorTextMessageHolder(parent);
            case TYPE_OPERATOR_TEXT_FILE:
                return new ItemOperatorTextFileMessageHolder(parent);
            case TYPE_OPERATOR_FILE:
                return new ItemOperatorFileMessageHolder(parent);
            case TYPE_OPERATOR_FEEDBACK:
                return new ItemOperatorFeedbackMessageHolder(parent);
            case TYPE_SERVICE_TEXT:
            default:
                return new ItemServiceTextMessageHolder(parent);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ((MessageHolder) holder).bind(messages.get(position));
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

    public void updateMessages(@NonNull List<Message> messages, int messagesCountDif) {
        this.messages = messages;
        notifyItemInserted(messages.size() - messagesCountDif);
        scrollToBottom();
    }

    public void scrollToBottom() {
        if (!messages.isEmpty()) {
            recyclerView.post(() -> recyclerView.scrollToPosition(messages.size() - 1));
        }
    }

    private class ItemOperatorFeedbackMessageHolder extends MessageHolder {
        ImageView iconImageView;
        TextView nameTextView;
        TextView textTextView;
        ImageButton likeButton;
        ImageButton dislikeButton;

        ItemOperatorFeedbackMessageHolder(@NonNull ViewGroup viewGroup) {
            super(viewGroup, R.layout.usedesk_item_message_operator_feedback);

            iconImageView = itemView.findViewById(R.id.icon_image_view);
            nameTextView = itemView.findViewById(R.id.name_text_view);
            textTextView = itemView.findViewById(R.id.text_text_view);
            likeButton = itemView.findViewById(R.id.like_button);
            dislikeButton = itemView.findViewById(R.id.dislike_button);
        }

        @Override
        void bind(@NonNull Message message) {
            super.bind(message);
            nameTextView.setText(message.getName().replace(' ', '\n'));
            textTextView.setText(message.getText());

            checkForDisplayImageOperatorAvatar(message, iconImageView);

            if (message.getPayload().hasFeedback()) {
                likeButton.setEnabled(false);
                dislikeButton.setEnabled(false);
            } else {
                likeButton.setEnabled(true);
                dislikeButton.setEnabled(true);

                likeButton.setOnClickListener(view -> {
                    chatFeedbackListener.onFeedbackSet(Feedback.LIKE);
                    view.setEnabled(false);
                    dislikeButton.setEnabled(false);
                });
                dislikeButton.setOnClickListener(view -> {
                    chatFeedbackListener.onFeedbackSet(Feedback.DISLIKE);
                    view.setEnabled(false);
                    likeButton.setEnabled(false);
                });
            }
        }
    }

    private class ItemUserTextMessageHolder extends TextMessageHolder {

        ItemUserTextMessageHolder(@NonNull ViewGroup viewGroup) {
            super(viewGroup, R.layout.usedesk_item_message_user_text);
        }

        @Override
        void bind(@NonNull Message message) {
            super.bind(message);
            textTextView.setText(message.getText());
        }
    }

    private class ItemOperatorTextMessageHolder extends TextMessageHolder {

        ImageView iconImageView;
        TextView nameTextView;
        LinearLayout layoutButtons;

        ItemOperatorTextMessageHolder(@NonNull ViewGroup viewGroup) {
            super(viewGroup, R.layout.usedesk_item_message_operator_text);

            iconImageView = itemView.findViewById(R.id.icon_image_view);
            nameTextView = itemView.findViewById(R.id.name_text_view);
            layoutButtons = itemView.findViewById(R.id.layout_buttons);
        }

        @Override
        void bind(@NonNull Message message) {
            super.bind(message);
            MessageButtons messageButtons = message.getMessageButtons();
            layoutButtons.removeAllViews();
            if (messageButtons.getMessageText() != null) {
                textTextView.setText(messageButtons.getMessageText());

                for (MessageButtons.MessageButton messageButton : messageButtons.getMessageButtons()) {
                    Button button = new Button(layoutButtons.getContext());

                    button.setText(messageButton.getText());
                    button.setOnClickListener(v ->
                            UsedeskChatSdk.getInstance().sendRx(messageButton)
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe());

                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

                    layoutButtons.addView(button, layoutParams);
                }

            } else {
                textTextView.setText(message.getText());
            }
            nameTextView.setText(message.getName().replace(' ', '\n'));

            checkForDisplayImageOperatorAvatar(message, iconImageView);
        }
    }

    private class ItemServiceTextMessageHolder extends TextMessageHolder {

        ItemServiceTextMessageHolder(@NonNull ViewGroup viewGroup) {
            super(viewGroup, R.layout.usedesk_item_message_service_text);
        }

        @Override
        void bind(@NonNull Message message) {
            super.bind(message);
            textTextView.setText(message.getText());
        }
    }

    private abstract class TextMessageHolder extends MessageHolder {

        TextView textTextView;

        TextMessageHolder(@NonNull ViewGroup viewGroup, int id) {
            super(viewGroup, id);

            textTextView = itemView.findViewById(R.id.text_text_view);
        }
    }

    private class ItemUserFileMessageHolder extends FileMessageHolder {

        ItemUserFileMessageHolder(@NonNull ViewGroup viewGroup) {
            super(viewGroup, R.layout.usedesk_item_message_user_file);
        }

        @Override
        void bind(@NonNull Message message) {
            super.bind(message);
            if (message.getUsedeskFile().isImage()) {
                progressBar.setVisibility(View.VISIBLE);
                ImageUtils.checkForDisplayImage(
                        fileImageView,
                        progressBar,
                        message.getUsedeskFile().getContent());
            } else {
                progressBar.setVisibility(View.GONE);
                fileImageView.setImageResource(R.drawable.ic_document_black);
            }
        }
    }

    private class ItemOperatorFileMessageHolder extends FileMessageHolder {

        ImageView iconImageView;
        TextView nameTextView;

        ItemOperatorFileMessageHolder(@NonNull ViewGroup viewGroup) {
            super(viewGroup, R.layout.usedesk_item_message_operator_file);

            iconImageView = itemView.findViewById(R.id.icon_image_view);
            nameTextView = itemView.findViewById(R.id.name_text_view);
        }

        @Override
        void bind(@NonNull Message message) {
            super.bind(message);
            nameTextView.setText(message.getName().replace(' ', '\n'));

            checkForDisplayImageOperatorAvatar(message, iconImageView);

            if (message.getUsedeskFile().isImage()) {
                progressBar.setVisibility(View.VISIBLE);
                ImageUtils.checkForDisplayImage(
                        fileImageView,
                        progressBar,
                        message.getUsedeskFile().getContent());
            } else {
                progressBar.setVisibility(View.GONE);
                fileImageView.setImageResource(R.drawable.ic_document_black);
                itemView.setOnClickListener(view -> {
                    if (message.getUsedeskFile() != null) {
                        downloadUtils.download(message.getUsedeskFile().getName(),
                                message.getUsedeskFile().getContent());
                    }
                });
            }
        }
    }

    private abstract class FileMessageHolder extends MessageHolder {

        ImageView fileImageView;
        ProgressBar progressBar;

        FileMessageHolder(@NonNull ViewGroup viewGroup, int id) {
            super(viewGroup, id);

            fileImageView = itemView.findViewById(R.id.file_image_view);
            progressBar = itemView.findViewById(R.id.progress_bar);
        }
    }

    private class ItemUserTextFileMessageHolder extends TextFileMessageHolder {
        ItemUserTextFileMessageHolder(@NonNull ViewGroup viewGroup) {
            super(viewGroup, R.layout.usedesk_item_message_user_text_file);
        }

        @Override
        void bind(@NonNull Message message) {
            super.bind(message);
            textTextView.setText(message.getText());

            if (message.getUsedeskFile().isImage()) {
                progressBar.setVisibility(View.VISIBLE);
                ImageUtils.checkForDisplayImage(
                        fileImageView,
                        progressBar,
                        message.getUsedeskFile().getContent());
            } else {
                progressBar.setVisibility(View.GONE);
                fileImageView.setImageResource(R.drawable.ic_document_black);
            }
        }
    }

    private class ItemOperatorTextFileMessageHolder extends TextFileMessageHolder {

        ImageView iconImageView;
        TextView nameTextView;

        ItemOperatorTextFileMessageHolder(@NonNull ViewGroup viewGroup) {
            super(viewGroup, R.layout.usedesk_item_message_operator_text_file);

            iconImageView = itemView.findViewById(R.id.icon_image_view);
            nameTextView = itemView.findViewById(R.id.name_text_view);
        }

        @Override
        void bind(@NonNull Message message) {
            super.bind(message);
            textTextView.setText(message.getText());
            nameTextView.setText(message.getName().replace(' ', '\n'));

            checkForDisplayImageOperatorAvatar(message, iconImageView);

            if (message.getUsedeskFile().isImage()) {
                progressBar.setVisibility(View.VISIBLE);
                ImageUtils.checkForDisplayImage(
                        fileImageView,
                        progressBar,
                        message.getUsedeskFile().getContent());
            } else {
                progressBar.setVisibility(View.GONE);
                fileImageView.setImageResource(R.drawable.ic_document_black);
                itemView.setOnClickListener(view -> {
                    if (message.getUsedeskFile() != null) {
                        downloadUtils.download(message.getUsedeskFile().getName(),
                                message.getUsedeskFile().getContent());
                    }
                });
            }
        }
    }

    private abstract class TextFileMessageHolder extends MessageHolder {

        TextView textTextView;
        ImageView fileImageView;
        ProgressBar progressBar;

        TextFileMessageHolder(@NonNull ViewGroup viewGroup, int id) {
            super(viewGroup, id);

            textTextView = itemView.findViewById(R.id.text_text_view);
            fileImageView = itemView.findViewById(R.id.file_image_view);
            progressBar = itemView.findViewById(R.id.progress_bar);
        }
    }

    private abstract class MessageHolder extends RecyclerView.ViewHolder {

        TextView timeTextView;

        MessageHolder(@NonNull ViewGroup viewGroup, int id) {
            super(LayoutInflater.from(viewGroup.getContext()).inflate(id, viewGroup, false));
            timeTextView = itemView.findViewById(R.id.time_text_view);
        }

        void bind(@NonNull Message message) {
            if (message.getCreatedAt() != null) {
                String time = TimeUtils.parseTime(message.getCreatedAt());
                if (TextUtils.isEmpty(time)) {
                    timeTextView.setVisibility(View.GONE);
                } else {
                    timeTextView.setVisibility(View.VISIBLE);
                    timeTextView.setText(time);
                }
            }
        }
    }
}