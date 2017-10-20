package com.lakeel.altla.ghost.alpha.mock.view.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.SpannableStringBuilder;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.MultiAutoCompleteTextView;

import com.github.bassaer.chatmessageview.models.Message;
import com.github.bassaer.chatmessageview.models.User;
import com.github.bassaer.chatmessageview.views.MessageView;
import com.lakeel.altla.ghost.alpha.mock.R;
import com.lakeel.altla.ghost.alpha.mock.helper.ContextHelper;

import butterknife.BindView;
import butterknife.ButterKnife;

public final class FeedbackActivity extends AppCompatActivity {

    @BindView(R.id.messageView)
    MessageView messageView;

    @BindView(R.id.textViewInput)
    MultiAutoCompleteTextView textViewInput;

    @BindView(R.id.layoutSend)
    View layoutSend;

    private static final int USER_ID_OTHER = 99;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        toolbar.setNavigationOnClickListener(v -> finish());

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        setTitle(getString(R.string.title_send_feedback));

        ButterKnife.bind(this);

        User you = new User(USER_ID_OTHER, null, null);
        Message message = new Message.Builder()
                .setUser(you)
                .setRightMessage(false)
                .setMessageText(getString(R.string.message_feedback))
                .hideIcon(true)
                .build();

        messageView.setMessage(message);

        layoutSend.setOnClickListener(v -> {
            layoutSend.setClickable(false);

            SpannableStringBuilder builder = (SpannableStringBuilder) textViewInput.getText();
            String inputtedText = builder.toString();

            textViewInput.setText(null);

            InputMethodManager inputMethodManager = ContextHelper.getInputMethodManager(getApplicationContext());
            inputMethodManager.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

            // userId/userName/icon
            final User me = new User(2, null, null);
            Message myMessage = new Message.Builder()
                    .setUser(me)
                    .setRightMessage(true)
                    .setMessageText(inputtedText)
                    .hideIcon(true)
                    .build();

            final User other = new User(USER_ID_OTHER, null, null);
            Message otherMessage = new Message.Builder()
                    .setUser(other)
                    .setRightMessage(false)
                    .setMessageText(getString(R.string.message_feedback_reply))
                    .hideIcon(true)
                    .build();

            messageView.setMessage(myMessage);
            messageView.setMessage(otherMessage);

            layoutSend.setClickable(true);
        });
    }
}