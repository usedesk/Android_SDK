package ru.usedesk.chat_gui.internal.chat;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import ru.usedesk.chat_gui.R;
import ru.usedesk.chat_gui.internal.utils.DownloadUtils;
import ru.usedesk.chat_gui.internal.utils.ImageUtils;
import ru.usedesk.chat_gui.internal.utils.TimeUtils;
import ru.usedesk.chat_sdk.external.UsedeskChatSdk;
import ru.usedesk.chat_sdk.external.entity.UsedeskFeedback;
import ru.usedesk.chat_sdk.external.entity.UsedeskMessage;
import ru.usedesk.chat_sdk.external.entity.UsedeskMessageButtons;

public class MessagesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_USER_MESSAGE = 1;
    private static final int TYPE_OPERATOR_MESSAGE = 2;

    private final ChatViewModel viewModel;
    private List<UsedeskMessage> messages;
    private DownloadUtils downloadUtils;
    private RecyclerView recyclerView;
    private Set<Integer> feedbacks;

    public MessagesAdapter(@NonNull View parentView, @NonNull ChatViewModel viewModel,
                           @Nullable List<UsedeskMessage> messages, @Nullable Set<Integer> feedbacks) {
        this.viewModel = viewModel;
        this.feedbacks = feedbacks == null
                ? new HashSet<>()
                : feedbacks;
        this.messages = messages == null
                ? new ArrayList<>()
                : messages;

        recyclerView = parentView.findViewById(R.id.rv_messages);
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
                return new UserTextHolder(parent);
            case TYPE_OPERATOR_MESSAGE:
                return new OperatorTextHolder(parent);
        }
        throw new RuntimeException("Unknown message type: " + viewType);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ((TimeHolder) holder).bind(messages.get(position), position);
    }

    @Override
    public int getItemViewType(int position) {
        UsedeskMessage message = messages.get(position);

        switch (messages.get(position).getType()) {
            case CLIENT_TO_OPERATOR:
            case CLIENT_TO_BOT:
                return TYPE_USER_MESSAGE;
            case OPERATOR_TO_CLIENT:
            case BOT_TO_CLIENT:
                return TYPE_OPERATOR_MESSAGE;
        }

        throw new RuntimeException("Unknown message type: " + message.getType());
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public void updateMessages(@NonNull List<UsedeskMessage> messages) {
        int cur = this.messages.size();
        int messagesCountDif = messages.size() - cur;
        this.messages = messages;

        notifyItemRangeChanged(cur, messagesCountDif);
        scrollToBottom();
    }

    public void updateFeedbacks(@NonNull Set<Integer> feedbacks) {
        this.feedbacks = feedbacks;
        for (Integer index : feedbacks) {
            notifyItemChanged(index);
        }
    }

    private void scrollToBottom() {
        if (!messages.isEmpty()) {
            recyclerView.post(() -> recyclerView.scrollToPosition(messages.size() - 1));
        }
    }

    private class UserTextHolder extends MessageHolder {
        UserTextHolder(@NonNull ViewGroup viewGroup) {
            super(viewGroup, R.layout.usedesk_item_message_user);
        }
    }

    private class OperatorTextHolder extends MessageHolder {
        private final ImageView ivAvatar;
        private final TextView tvName;

        private final LinearLayout ltButtons;

        private final TextView tv_feedback_answer;
        private final ViewGroup ltFeedback;
        private final ImageView ivLike;
        private final ImageView ivDislike;

        OperatorTextHolder(@NonNull ViewGroup viewGroup) {
            super(viewGroup, R.layout.usedesk_item_message_operator);

            ivAvatar = itemView.findViewById(R.id.iv_avatar);
            tvName = itemView.findViewById(R.id.tv_name);

            ltButtons = itemView.findViewById(R.id.lt_buttons);

            tv_feedback_answer = itemView.findViewById(R.id.tv_feedback_answer);
            ltFeedback = itemView.findViewById(R.id.lt_feedback);
            ivLike = itemView.findViewById(R.id.btn_like);
            ivDislike = itemView.findViewById(R.id.btn_dislike);
        }

        @Override
        void bind(@NonNull UsedeskMessage message, int position) {
            super.bind(message, position);

            boolean bFeedbackReceived = feedbacks.contains(position);

            tvName.setText(message.getName());

            ivAvatar.setImageResource(R.drawable.ic_operator_black);
            if (message.getUsedeskPayload() != null) {
                ImageUtils.checkForDisplayImage(ivAvatar,
                        message.getUsedeskPayload().getAvatar(),
                        R.drawable.ic_operator_black);

                if (!message.getUsedeskPayload().hasFeedback() || bFeedbackReceived) {
                    ltFeedback.setVisibility(View.GONE);
                } else {
                    ltFeedback.setVisibility(View.VISIBLE);

                    ivLike.setOnClickListener(view -> viewModel.sendFeedback(position, UsedeskFeedback.LIKE));
                    ivDislike.setOnClickListener(view -> viewModel.sendFeedback(position, UsedeskFeedback.DISLIKE));
                }
            }

            tv_feedback_answer.setVisibility(bFeedbackReceived
                    ? View.VISIBLE
                    : View.GONE);
            tvMessage.setVisibility(!bFeedbackReceived
                    ? View.VISIBLE
                    : View.GONE);

            ltButtons.removeAllViews();
            if (message.getMessageButtons().getMessageText() != null && message.getMessageButtons().getMessageButtons().size() > 0) {
                ltButtons.setVisibility(View.VISIBLE);
                for (UsedeskMessageButtons.MessageButton messageButton : message.getMessageButtons().getMessageButtons()) {
                    Button button = new Button(ltButtons.getContext());

                    button.setText(messageButton.getText());
                    button.setOnClickListener(v ->
                            UsedeskChatSdk.getInstance().sendRx(messageButton)
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe());

                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

                    ltButtons.addView(button, layoutParams);
                }
            } else {
                ltButtons.setVisibility(View.GONE);
            }
        }
    }

    private abstract class MessageHolder extends TimeHolder {
        protected final TextView tvMessage;
        protected final ImageView ivPreview;

        MessageHolder(@NonNull ViewGroup viewGroup, int id) {
            super(viewGroup, id);

            tvMessage = itemView.findViewById(R.id.tv_message);
            ivPreview = itemView.findViewById(R.id.iv_preview);
        }

        @Override
        void bind(@NonNull UsedeskMessage message, int position) {
            super.bind(message, position);

            if (message.getMessageButtons().getMessageText() != null) {
                tvMessage.setText(message.getMessageButtons().getMessageText());
            } else {
                tvMessage.setText(message.getText());
            }
            if (tvMessage.getText().length() > 0) {
                tvMessage.setVisibility(View.VISIBLE);
            } else {
                tvMessage.setVisibility(View.GONE);
            }

            if (message.getFile() != null) {
                ivPreview.setVisibility(View.VISIBLE);
                ImageUtils.checkForDisplayImage(ivPreview,
                        message.getFile().getContent(),
                        R.drawable.ic_document_black);
            } else {
                ivPreview.setVisibility(View.GONE);
            }

            ivPreview.setOnClickListener(view -> {
                if (message.getFile() != null) {
                    downloadUtils.download(message.getFile().getName(),
                            message.getFile().getContent());
                }
            });
        }
    }

    private abstract class TimeHolder extends RecyclerView.ViewHolder {

        protected final TextView timeTextView;

        TimeHolder(@NonNull ViewGroup viewGroup, int id) {
            super(LayoutInflater.from(viewGroup.getContext()).inflate(id, viewGroup, false));

            timeTextView = itemView.findViewById(R.id.tv_time);
        }

        void bind(@NonNull UsedeskMessage message, int position) {
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