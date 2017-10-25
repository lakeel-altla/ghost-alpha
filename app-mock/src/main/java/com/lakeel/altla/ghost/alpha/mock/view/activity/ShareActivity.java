package com.lakeel.altla.ghost.alpha.mock.view.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.lakeel.altla.android.log.Log;
import com.lakeel.altla.android.log.LogFactory;
import com.lakeel.altla.ghost.alpha.mock.R;
import com.lakeel.altla.ghost.alpha.mock.view.fragment.EditObjectFragment;

import butterknife.BindView;
import butterknife.ButterKnife;

public final class ShareActivity extends AppCompatActivity {

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    private static final Log LOG = LogFactory.getLog(ShareActivity.class);

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share);

        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_clear);

        setTitle(null);

        // Receive intent that include an url from browser.
        if (Intent.ACTION_SEND.equals(getIntent().getAction())) {
            CharSequence uri = getIntent().getExtras().getCharSequence(Intent.EXTRA_TEXT);
            if (uri == null) {
                LOG.e("The variable 'uri' is null.");
                finish();
                return;
            }

            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragmentPlaceholder, EditObjectFragment.newInstanceWithUri(uri.toString()), EditObjectFragment.class.getSimpleName());
            transaction.commit();
        } else {
            LOG.e("An Invalid action:" + getIntent().getAction());
            finish();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
            case R.id.action_save:
                Intent intent = new Intent(getApplicationContext(), NearbySearchActivity.class);
                startActivity(intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}