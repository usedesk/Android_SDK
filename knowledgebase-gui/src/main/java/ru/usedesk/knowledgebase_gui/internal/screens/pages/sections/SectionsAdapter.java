package ru.usedesk.knowledgebase_gui.internal.screens.pages.sections;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import ru.usedesk.common_gui.external.IUsedeskViewCustomizer;
import ru.usedesk.common_gui.internal.ImageUtils;
import ru.usedesk.knowledgebase_gui.R;
import ru.usedesk.knowledgebase_sdk.external.entity.UsedeskSection;

public class SectionsAdapter extends RecyclerView.Adapter<SectionsAdapter.SectionViewHolder> {

    private final List<UsedeskSection> sectionList;
    private final IOnSectionClickListener onSectionClickListener;
    private final IUsedeskViewCustomizer usedeskViewCustomizer;

    SectionsAdapter(@NonNull List<UsedeskSection> sectionList,
                    @NonNull IOnSectionClickListener onSectionClickListener,
                    @NonNull IUsedeskViewCustomizer usedeskViewCustomizer) {
        this.sectionList = sectionList;
        this.onSectionClickListener = onSectionClickListener;
        this.usedeskViewCustomizer = usedeskViewCustomizer;
    }

    @NonNull
    @Override
    public SectionViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = usedeskViewCustomizer.createView(viewGroup, R.layout.usedesk_item_section, R.style.Usedesk_Theme_KnowledgeBase);

        return new SectionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SectionViewHolder sectionViewHolder, int i) {
        sectionViewHolder.bind(sectionList.get(i));
    }

    @Override
    public int getItemCount() {
        return sectionList.size();
    }

    class SectionViewHolder extends RecyclerView.ViewHolder {

        private final View rootView;
        private final ImageView imageViewIcon;
        private final TextView textViewTitle;

        SectionViewHolder(@NonNull View itemView) {
            super(itemView);

            rootView = itemView;
            imageViewIcon = itemView.findViewById(R.id.iv_icon);
            textViewTitle = itemView.findViewById(R.id.tv_title);
        }

        void bind(@NonNull final UsedeskSection section) {
            imageViewIcon.setImageBitmap(null);
            textViewTitle.setText(section.getTitle());
            ImageUtils.setImage(imageViewIcon, section.getImage());

            rootView.setOnClickListener(v -> onSectionClickListener.onSectionClick(section.getId()));
        }
    }
}
