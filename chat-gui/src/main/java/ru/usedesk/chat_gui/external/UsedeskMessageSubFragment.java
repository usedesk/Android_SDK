package ru.usedesk.chat_gui.external;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import ru.usedesk.chat_gui.R;

public class UsedeskMessageSubFragment extends Fragment {

    private Button attachButton;
    private Button sendButton;
    private EditText messageEditText;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.usedesk_sub_fragment_message, container, false);

        attachButton = rootView.findViewById(R.id.button_attach);
        sendButton = rootView.findViewById(R.id.button_send);
        messageEditText = rootView.findViewById(R.id.edit_text_message);

        return super.onCreateView(inflater, container, savedInstanceState);
    }
}
