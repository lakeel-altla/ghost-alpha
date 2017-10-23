package com.lakeel.altla.ghost.alpha.mock.view.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.util.ArrayMap;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.lakeel.altla.ghost.alpha.mock.R;
import com.lakeel.altla.ghost.alpha.mock.view.imageloader.TextDrawableImageLoader;

import butterknife.BindView;
import butterknife.ButterKnife;

public final class MyObjectActivity extends AppCompatActivity {

    @BindView(R.id.container)
    View container;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.collapsingToolbar)
    CollapsingToolbarLayout collapsingToolbarLayout;

    @BindView(R.id.imageViewObject)
    ImageView imageViewObject;

    @BindView(R.id.textViewUrl)
    TextView textViewUrl;

    @BindView(R.id.map)
    View map;

    @BindView(R.id.fabEdit)
    FloatingActionButton fabEdit;

    private static final String BUNDLE_OBJECT_ID = "objectId";

    private static final int REQUEST_CODE_EDIT_OBJECT = 1;

    private TextDrawableImageLoader imageLoader;

    private VirtualObject object;

    public static Intent newIntent(@NonNull Context context, @NonNull String objectId) {
        Intent intent = new Intent(context, MyObjectActivity.class);
        intent.putExtra(BUNDLE_OBJECT_ID, objectId);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Hide status bar.
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_my_object);

        ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        toolbar.setNavigationIcon(R.drawable.ic_clear);
        toolbar.setNavigationOnClickListener(v -> finish());

        Intent intent = getIntent();
        String objectId = intent.getStringExtra(BUNDLE_OBJECT_ID);
        object = getVirtualObject(objectId);

        collapsingToolbarLayout.setTitle(object.objectName);

        imageLoader = new TextDrawableImageLoader(imageViewObject, object.url, object.objectName);
        imageLoader.loadImage();

        textViewUrl.setText(object.url);

        SupportMapFragment supportMapFragment = SupportMapFragment.newInstance();
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.map, supportMapFragment)
                .commit();

        supportMapFragment.getMapAsync(googleMap -> {
            CameraUpdate cameraUpdate = CameraUpdateFactory.zoomTo(14);
            googleMap.moveCamera(cameraUpdate);

            VirtualObject.Location location = object.location;
            LatLng latLng = new LatLng(location.latitude, location.longitude);

            googleMap.addMarker(new MarkerOptions()
                    .position(latLng)
                    .draggable(true));

            googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        });

        fabEdit.setOnClickListener(v -> startActivityForResult(EditObjectActivity.newIntent(getApplicationContext(), objectId), REQUEST_CODE_EDIT_OBJECT));
    }

    @Override
    public void onStop() {
        super.onStop();
        imageLoader.cancel();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_my_object, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_remove:
                Intent intent = MyObjectListActivity.newIntent(object.objectId);
                setResult(RESULT_OK, intent);
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_EDIT_OBJECT) {
            if (resultCode == RESULT_OK) {
                Snackbar.make(container, R.string.snackbar_saved, Snackbar.LENGTH_SHORT).show();
            }
        }
    }

    private VirtualObject getVirtualObject(@NonNull String objectId) {
        ArrayMap<String, VirtualObject> objectMap = new ArrayMap<>();

        {
            VirtualObject object = new VirtualObject("objectA", "Instagram投稿の投稿者: Keisuke Sunada さん 日時: 2017 5月 5 12:39午後 UTC", "https://www.instagram.com/p/BTtgNJMgYpY/?hl=ja&taken-by=sunada.chan", new VirtualObject.Location(35.66811, 139.7401657));
            objectMap.put(object.objectId, object);
        }

        {
            VirtualObject object = new VirtualObject("objectB", "インビスハライコ", "https://tabelog.com/tokyo/A1307/A130701/13110227/", new VirtualObject.Location(35.666468, 139.73775));
            objectMap.put(object.objectId, object);
        }

        return objectMap.get(objectId);
    }

    private static final class VirtualObject {

        String objectId;

        String objectName;

        String url;

        VirtualObject.Location location;

        VirtualObject(@NonNull String objectId,
                      @NonNull String objectName,
                      @NonNull String url,
                      @NonNull VirtualObject.Location location) {
            this.objectId = objectId;
            this.objectName = objectName;
            this.url = url;
            this.location = location;
        }

        static final class Location {

            private final double latitude;

            private final double longitude;

            Location(double latitude, double longitude) {
                this.latitude = latitude;
                this.longitude = longitude;
            }
        }
    }
}
