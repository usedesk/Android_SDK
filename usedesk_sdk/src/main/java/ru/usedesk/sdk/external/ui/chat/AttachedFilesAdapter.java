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
import ru.usedesk.sdk.external.entity.chat.UsedeskFile;

class AttachedFilesAdapter extends RecyclerView.Adapter<AttachedFilesAdapter.ViewHolder> {

    private final ChatViewModel chatViewModel;
    private List<UsedeskFile> files = new ArrayList<>();

    AttachedFilesAdapter(@NonNull ChatViewModel chatViewModel,
                         @NonNull RecyclerView recyclerView) {
        this.chatViewModel = chatViewModel;

        recyclerView.setAdapter(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(recyclerView.getContext()));
    }

    void update(@NonNull List<UsedeskFile> attachedFiles) {
        if (this.files != attachedFiles) {
            this.files = attachedFiles;

            notifyDataSetChanged();
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        ViewHolder viewHolder = new ViewHolder(LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.usedesk_item_chat_attached_file, viewGroup, false));


        return viewHolder;
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

        private void bind(@NonNull UsedeskFile usedeskFile) {
            if (usedeskFile.isImage()) {
                imageViewPreview.setImageResource(R.drawable.ic_attachment_photo_black);
            } else {
                imageViewPreview.setImageResource(R.drawable.ic_attachment_document_black);
            }
            imageViewDetach.setOnClickListener(v -> chatViewModel.detachFile(usedeskFile));
        }
    }
}
