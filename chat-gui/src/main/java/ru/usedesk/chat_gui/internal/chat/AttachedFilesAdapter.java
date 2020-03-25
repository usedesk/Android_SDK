package ru.usedesk.chat_gui.internal.chat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import ru.usedesk.chat_gui.R;
import ru.usedesk.common_gui.internal.ImageUtils;
import ru.usedesk.chat_sdk.external.entity.UsedeskFileInfo;


public class AttachedFilesAdapter extends RecyclerView.Adapter<AttachedFilesAdapter.ViewHolder> {

    private final ChatViewModel chatViewModel;
    private List<UsedeskFileInfo> files = new ArrayList<>();

    public AttachedFilesAdapter(@NonNull ChatViewModel chatViewModel,
                                @NonNull RecyclerView recyclerView) {
        this.chatViewModel = chatViewModel;

        recyclerView.setAdapter(this);
    }

    public void update(@NonNull List<UsedeskFileInfo> attachedFiles) {
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
            ImageUtils.setImageCenter(imageViewPreview, usedeskFileInfo.getUri(), R.drawable.ic_document_black);

            imageViewDetach.setOnClickListener(v -> chatViewModel.detachFile(usedeskFileInfo));
        }
    }
}
