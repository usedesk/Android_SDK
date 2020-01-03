package ru.usedesk.sdk.external.ui.chat;

import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

import ru.usedesk.sdk.R;
import ru.usedesk.sdk.external.entity.chat.UsedeskFileInfo;
import ru.usedesk.sdk.internal.utils.GlideApp;

class AttachedFilesAdapter extends RecyclerView.Adapter<AttachedFilesAdapter.ViewHolder> {

    private final ChatViewModel chatViewModel;
    private List<UsedeskFileInfo> files = new ArrayList<>();

    AttachedFilesAdapter(@NonNull ChatViewModel chatViewModel,
                         @NonNull RecyclerView recyclerView) {
        this.chatViewModel = chatViewModel;

        recyclerView.setAdapter(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(recyclerView.getContext(), LinearLayoutManager.HORIZONTAL, false));
    }

    void update(@NonNull List<UsedeskFileInfo> attachedFiles) {
        if (this.files != attachedFiles) {
            this.files = attachedFiles;

            notifyDataSetChanged();
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new ViewHolder(LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.usedesk_item_chat_attached_file, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        viewHolder.bind(files.get(i));
    }

    @Override
    public int getItemCount() {
        return files.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imageViewPreview;
        private final ImageView imageViewDetach;

        private ViewHolder(@NonNull View itemView) {
            super(itemView);

            imageViewPreview = itemView.findViewById(R.id.iv_preview);
            imageViewDetach = itemView.findViewById(R.id.iv_detach);
        }

        private void bind(@NonNull UsedeskFileInfo usedeskFileInfo) {
            if (usedeskFileInfo.getType().equals(UsedeskFileInfo.Type.IMAGE)) {
                GlideApp.with(imageViewPreview)
                        .load(usedeskFileInfo.getUri())
                        .centerCrop()
                        .error(R.drawable.ic_attachment_photo_black)
                        .into(imageViewPreview);
            } else {
                GlideApp.with(imageViewPreview)
                        .load(R.drawable.ic_document_black)
                        .into(imageViewPreview);
            }

            imageViewDetach.setOnClickListener(v -> chatViewModel.detachFile(usedeskFileInfo));
        }
    }
}
