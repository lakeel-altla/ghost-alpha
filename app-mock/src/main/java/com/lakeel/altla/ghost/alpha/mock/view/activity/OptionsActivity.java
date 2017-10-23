package com.lakeel.altla.ghost.alpha.mock.view.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.lakeel.altla.ghost.alpha.mock.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public final class OptionsActivity extends AppCompatActivity {

    @BindView(R.id.container)
    View container;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    private static final int REQUEST_SEND_FEEDBACK = 1;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_options);

        ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        toolbar.setNavigationIcon(R.drawable.ic_clear);
        toolbar.setNavigationOnClickListener(v -> finish());

        setTitle(getString(R.string.title_option));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_SEND_FEEDBACK) {
            if (resultCode == RESULT_OK) {
                Snackbar.make(container, R.string.snackbar_feedback_reply, Snackbar.LENGTH_SHORT).show();
            }
        }
    }

    @OnClick(R.id.layoutObjectManagement)
    public void onClickObjectManagement(View view) {
        Intent intent = new Intent(getApplicationContext(), MyObjectListActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.layoutSettings)
    public void onClickSettings(View view) {
        Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.layoutFeedback)
    public void onClickFeedback(View view) {
        Intent intent = new Intent(getApplicationContext(), FeedbackActivity.class);
        startActivityForResult(intent, REQUEST_SEND_FEEDBACK);
    }
}
