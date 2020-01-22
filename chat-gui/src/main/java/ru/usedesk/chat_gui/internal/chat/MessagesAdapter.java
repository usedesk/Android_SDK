package ru.usedesk.chat_gui.internal.chat;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import ru.usedesk.chat_sdk.external.entity.Feedback;
import ru.usedesk.chat_sdk.external.entity.Message;
import ru.usedesk.chat_sdk.external.entity.MessageButtons;

public class MessagesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_USER_MESSAGE = 1;
    private static final int TYPE_OPERATOR_MESSAGE = 2;
    private static final int TYPE_OPERATOR_FEEDBACK = 3;
    private static final int TYPE_SERVICE_TEXT = 4;
    private final ChatViewModel viewModel;
    private List<Message> messages;
    private DownloadUtils downloadUtils;
    private RecyclerView recyclerView;

    public MessagesAdapter(@NonNull View parentView,
                           @NonNull List<Message> messages,
                           @NonNull ChatViewModel viewModel) {
        this.viewModel = viewModel;
        this.messages = messages;

        recyclerView = parentView.findViewById(R.id.messages_recycler_view);
        downloadUtils = new DownloadUtils(recyclerView.getContext());

        recyclerView.setAdapter(this);
        recyclerView.addOnLayoutChangeListener(
                (v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
                    if (bottom < oldBottom) {
                        recyclerView.postDelayed(this::scrollToBottom, 100);
                    }
                });
    }

    @Override
    @NonNull
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType) {
            case TYPE_USER_MESSAGE:
                return new UserMessageHolder(parent);
            case TYPE_OPERATOR_MESSAGE:
                return new OperatorMessageHolder(parent);
            case TYPE_SERVICE_TEXT:
            default:
                return new ItemServiceTextMessageHolder(parent);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ((TimeHolder) holder).bind(messages.get(position));
    }

    @Override
    public int getItemViewType(int position) {
        int itemViewType = TYPE_SERVICE_TEXT;
        Message message = messages.get(position);

        switch (message.getType()) {
            case CLIENT_TO_OPERATOR:
            case CLIENT_TO_BOT:
                itemViewType = TYPE_USER_MESSAGE;
                break;
            case OPERATOR_TO_CLIENT:
            case BOT_TO_CLIENT:
                boolean hasFeedbackButtons = message.getPayload().getFeedbackButtons() != null;

                if (hasFeedbackButtons) {
                    itemViewType = TYPE_OPERATOR_FEEDBACK;
                } else {
                    itemViewType = TYPE_OPERATOR_MESSAGE;
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

    public void updateMessages(@NonNull List<Message> messages, int messagesCountDif) {
        int cur = this.messages.size();
        this.messages = messages;
        notifyItemRangeChanged(cur, messagesCountDif);
        scrollToBottom();
    }

    private void scrollToBottom() {
        if (!messages.isEmpty()) {
            recyclerView.post(() -> recyclerView.scrollToPosition(messages.size() - 1));
        }
    }

    private class ItemServiceTextMessageHolder extends MessageHolder {
        ItemServiceTextMessageHolder(@NonNull ViewGroup viewGroup) {
            super(viewGroup, R.layout.usedesk_item_message_service_text);
        }
    }

    private class UserMessageHolder extends MessageHolder {
        UserMessageHolder(@NonNull ViewGroup viewGroup) {
            super(viewGroup, R.layout.usedesk_item_message_user);
        }
    }

    private class OperatorMessageHolder extends MessageHolder {
        private final ImageView ivAvatar;
        private final TextView tvName;
        private final ImageButton ivLike;
        private final ImageButton ivDislike;
        private final LinearLayout layoutButtons;

        OperatorMessageHolder(@NonNull ViewGroup viewGroup) {
            super(viewGroup, R.layout.usedesk_item_message_operator);

            ivAvatar = itemView.findViewById(R.id.iv_avatar);
            tvName = itemView.findViewById(R.id.tv_name);

            layoutButtons = itemView.findViewById(R.id.layout_buttons);

            ivLike = itemView.findViewById(R.id.btn_like);
            ivDislike = itemView.findViewById(R.id.btn_dislike);
        }

        @Override
        void bind(@NonNull Message message) {
            super.bind(message);

            tvName.setText(message.getName()/*.replace(' ', '\n')*/);

            ImageUtils.checkForDisplayImage(ivAvatar,
                    message.getPayload().getAvatar(),
                    R.drawable.ic_operator_black);

            if (message.getPayload().hasFeedback()) {
                ivLike.setVisibility(View.VISIBLE);
                ivDislike.setVisibility(View.VISIBLE);
            } else {
                ivLike.setVisibility(View.GONE);
                ivDislike.setVisibility(View.GONE);

                ivLike.setOnClickListener(view -> {
                    viewModel.sendFeedback(Feedback.LIKE);
                    ivLike.setEnabled(false);
                    ivDislike.setEnabled(false);
                });
                ivDislike.setOnClickListener(view -> {
                    viewModel.sendFeedback(Feedback.DISLIKE);
                    ivLike.setEnabled(false);
                    ivDislike.setEnabled(false);
                });
            }

            layoutButtons.removeAllViews();
            if (message.getMessageButtons().getMessageText() != null) {
                for (MessageButtons.MessageButton messageButton : message.getMessageButtons().getMessageButtons()) {
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

            }
        }
    }

    private abstract class MessageHolder extends TimeHolder {
        private final TextView tvMessage;
        private final ImageView ivPreview;

        MessageHolder(@NonNull ViewGroup viewGroup, int id) {
            super(viewGroup, id);

            tvMessage = itemView.findViewById(R.id.tv_message);
            ivPreview = itemView.findViewById(R.id.iv_preview);
        }

        @Override
        void bind(@NonNull Message message) {
            super.bind(message);

            if (message.getMessageButtons().getMessageText() != null) {
                tvMessage.setText(message.getMessageButtons().getMessageText());
            } else {
                tvMessage.setText(message.getText());
            }

            if (message.getUsedeskFile() != null) {
                ivPreview.setVisibility(View.VISIBLE);
                ImageUtils.checkForDisplayImage(ivPreview,
                        message.getUsedeskFile().getContent(),
                        R.drawable.ic_document_black);
            } else {
                ivPreview.setVisibility(View.GONE);
            }

            ivPreview.setOnClickListener(view -> {
                if (message.getUsedeskFile() != null) {
                    downloadUtils.download(message.getUsedeskFile().getName(),
                            message.getUsedeskFile().getContent());
                }
            });
        }
    }

    private abstract class TimeHolder extends RecyclerView.ViewHolder {

        private final TextView timeTextView;

        TimeHolder(@NonNull ViewGroup viewGroup, int id) {
            super(LayoutInflater.from(viewGroup.getContext()).inflate(id, viewGroup, false));

            timeTextView = itemView.findViewById(R.id.tv_time);
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