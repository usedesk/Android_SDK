package ru.usedesk.sdk.ui.knowledgebase.sections;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import ru.usedesk.sdk.R;
import ru.usedesk.sdk.domain.entity.knowledgebase.Section;

public class SectionsAdapter extends RecyclerView.Adapter<SectionsAdapter.SectionViewHolder> {

    private final List<Section> sectionList;
    private final IOnSectionClickListener IOnSectionClickListener;

    SectionsAdapter(@NonNull List<Section> sectionList,
                    @NonNull IOnSectionClickListener IOnSectionClickListener) {
        this.sectionList = sectionList;
        this.IOnSectionClickListener = IOnSectionClickListener;
    }

    @NonNull
    @Override
    public SectionViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.section_item, viewGroup, false);

        return new SectionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SectionViewHolder sectionViewHolder, int i) {
        sectionViewHolder.bind(sectionList.get(i), IOnSectionClickListener);
    }

    @Override
    public int getItemCount() {
        return sectionList.size();
    }

    class SectionViewHolder extends RecyclerView.ViewHolder {

        private ImageView imageViewIcon;
        private TextView textViewTitle;

        SectionViewHolder(@NonNull View itemView) {
            super(itemView);

            imageViewIcon = itemView.findViewById(R.id.iv_icon);
            textViewTitle = itemView.findViewById(R.id.tv_title);
        }

        void bind(@NonNull final Section section,
                  @NonNull final IOnSectionClickListener IOnSectionClickListener) {
            //imageViewIcon.setImageURI(section.getImage());//TODO:set icon
            textViewTitle.setText(section.getTitle());

            itemView.setOnClickListener(v -> IOnSectionClickListener.onSectionClick(section.getId()));
        }
    }
}
