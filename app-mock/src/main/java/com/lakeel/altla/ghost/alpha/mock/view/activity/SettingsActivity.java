package com.lakeel.altla.ghost.alpha.mock.view.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.lakeel.altla.ghost.alpha.mock.R;
import com.mikepenz.aboutlibraries.Libs;
import com.mikepenz.aboutlibraries.LibsBuilder;

import butterknife.ButterKnife;
import butterknife.OnClick;

public final class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        toolbar.setNavigationOnClickListener(v -> finish());

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        setTitle(getString(R.string.title_settings));

        ButterKnife.bind(this);
    }

    @OnClick(R.id.layoutPrivacyPolicy)
    public void onClickPrivacyPolicy() {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.url_privacy_policy)));
        startActivity(intent);
    }

    @OnClick(R.id.layoutOpenSourceLibrary)
    public void onClickOpenSourceLibrary() {
        //
        // The AboutLibraries (https://github.com/mikepenz/AboutLibraries) shows libraries used by the app.
        //

        // Not supported libraries by The AboutLibraries, place files in the res/values directory.
        // values/library_chipcloud.xml is generated on http://def-builder.mikepenz.com/
        String[] libraries = {"library_chipcloud"};
        new LibsBuilder()
                .withActivityStyle(Libs.ActivityStyle.LIGHT_DARK_TOOLBAR)
                .withLibraries(libraries)
                .withAboutIconShown(true)
                .withActivityTitle(getString(R.string.title_open_source_libraries))
                .withAboutVersionShown(true)
                .start(getApplicationContext());
    }
}