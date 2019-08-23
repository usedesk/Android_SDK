package ru.usedesk.sdk.external.ui.knowledgebase.pages.sections;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.List;

import ru.usedesk.sdk.R;
import ru.usedesk.sdk.external.entity.knowledgebase.Section;
import ru.usedesk.sdk.external.ui.UsedeskViewCustomizer;

public class SectionsAdapter extends RecyclerView.Adapter<SectionsAdapter.SectionViewHolder> {

    private final List<Section> sectionList;
    private final IOnSectionClickListener onSectionClickListener;
    private final UsedeskViewCustomizer usedeskViewCustomizer;

    SectionsAdapter(@NonNull List<Section> sectionList,
                    @NonNull IOnSectionClickListener onSectionClickListener,
                    @NonNull UsedeskViewCustomizer usedeskViewCustomizer) {
        this.sectionList = sectionList;
        this.onSectionClickListener = onSectionClickListener;
        this.usedeskViewCustomizer = usedeskViewCustomizer;
    }

    @NonNull
    @Override
    public SectionViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = usedeskViewCustomizer.createView(viewGroup, R.layout.usedesk_item_section);

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

        void bind(@NonNull final Section section) {
            imageViewIcon.setImageBitmap(null);
            Glide.with(imageViewIcon)
                    .load(section.getImage())
                    .into(imageViewIcon);
            textViewTitle.setText(section.getTitle());

            rootView.setOnClickListener(v -> onSectionClickListener.onSectionClick(section.getId()));
        }
    }
}
