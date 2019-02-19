package ru.usedesk.sdk.ui.knowledgebase.sections;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import java.util.List;

import ru.usedesk.sdk.domain.entity.knowledgebase.Section;

public class SectionsViewModel extends ViewModel {

    private MutableLiveData<List<Section>> sectionsLiveData = new MutableLiveData<>();

    public SectionsViewModel() {
    }

    public LiveData<List<Section>> getSectionsLiveData() {
        return sectionsLiveData;
    }
}
