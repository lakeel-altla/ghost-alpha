package com.lakeel.altla.ghost.alpha.mock.view.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.lakeel.altla.ghost.alpha.mock.R;

import butterknife.ButterKnife;
import butterknife.OnClick;

public final class OthersActivity extends AppCompatActivity {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_others);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        toolbar.setNavigationOnClickListener(v -> finish());

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        setTitle(getString(R.string.title_others));

        ButterKnife.bind(this);
    }

    @OnClick(R.id.layoutObjectManagement)
    public void onClickObjectManagement(View view) {
        Intent intent = new Intent(getApplicationContext(), ObjectManagementActivity.class);
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
        startActivity(intent);
    }
}
