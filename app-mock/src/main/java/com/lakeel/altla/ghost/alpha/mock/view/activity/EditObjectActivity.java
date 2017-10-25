package com.lakeel.altla.ghost.alpha.mock.view.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.lakeel.altla.ghost.alpha.mock.R;
import com.lakeel.altla.ghost.alpha.mock.view.fragment.EditObjectFragment;

import butterknife.BindView;
import butterknife.ButterKnife;

public final class EditObjectActivity extends AppCompatActivity {

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    private static final String BUNDLE_OBJECT_ID = "objectId";

    public static Intent newIntent(@NonNull Context context, @NonNull String objectId) {
        Intent intent = new Intent(context, EditObjectActivity.class);
        intent.putExtra(BUNDLE_OBJECT_ID, objectId);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_object);

        ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        setTitle(null);

        toolbar.setNavigationIcon(R.drawable.ic_clear);

        String objectId = getIntent().getStringExtra(BUNDLE_OBJECT_ID);
        EditObjectFragment fragment;
        if (objectId == null) {
            fragment = EditObjectFragment.newInstance();
        } else {
            fragment = EditObjectFragment.newInstanceWithObjectId(objectId);
        }

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragmentPlaceholder, fragment, fragment.getClass().getSimpleName());
        transaction.commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
            case R.id.action_save:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
