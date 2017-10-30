package com.lakeel.altla.ghost.alpha.mock.view.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.lakeel.altla.ghost.alpha.mock.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public final class FilterActivity extends AppCompatActivity {

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter);

        ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        setTitle(R.string.title_filter);

        toolbar.setNavigationIcon(R.drawable.ic_clear);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_filter, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
            case R.id.action_done:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
