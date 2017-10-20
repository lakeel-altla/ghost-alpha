package com.lakeel.altla.ghost.alpha.mock.view.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.lakeel.altla.android.log.Log;
import com.lakeel.altla.android.log.LogFactory;
import com.lakeel.altla.ghost.alpha.mock.R;
import com.lakeel.altla.ghost.alpha.mock.helper.ContextHelper;
import com.lakeel.altla.ghost.alpha.mock.lib.android.view.TextContextMenuEditText;
import com.lakeel.altla.ghost.alpha.mock.lib.android.view.TextContextMenuEditText.TextContextMenuListener;
import com.lakeel.altla.ghost.alpha.mock.view.imageloader.TextDrawableImageLoader;
import com.lakeel.altla.ghost.alpha.richlink.RichLinkLoader;
import com.stepstone.stepper.Step;
import com.stepstone.stepper.VerificationError;
import com.wang.avi.AVLoadingIndicatorView;

import org.jdeferred.DeferredManager;
import org.jdeferred.android.AndroidDeferredManager;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static android.view.KeyEvent.ACTION_DOWN;
import static android.view.KeyEvent.KEYCODE_ENTER;

public final class InputUrlStepFragment extends Fragment implements Step {

    @BindView(R.id.textInputLayout)
    TextInputLayout textInputLayout;

    @BindView(R.id.editTextUrl)
    TextContextMenuEditText editTextUrl;

    @BindView(R.id.imageViewPhoto)
    ImageView imageViewPhoto;

    @BindView(R.id.textViewNoImage)
    TextView textViewNoImage;

    @BindView(R.id.indicatorView)
    AVLoadingIndicatorView indicatorView;

    @BindView(R.id.imageViewLinkThumbnail)
    ImageView imageViewLinkThumbnail;

    @BindView(R.id.textViewLinkTitle)
    TextView textViewLinkTitle;

    @BindView(R.id.textViewObjectManager)
    TextView textViewObjectManager;

    private static final Log LOG = LogFactory.getLog(InputUrlStepFragment.class);

    public static InputUrlStepFragment newInstance() {
        return new InputUrlStepFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_input_url_step, container, false);

        ButterKnife.bind(this, view);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        editTextUrl.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                initPreview();
                validateUrl(editable.toString());
            }
        });

        editTextUrl.addTextContextMenuListener(new TextContextMenuListener() {

            @Override
            public void onCut() {
            }

            @Override
            public void onPaste(@Nullable Editable editable) {
                if (editable == null) return;
                validateUrl(editable.toString());
            }

            @Override
            public void onCopy() {
            }
        });

        editTextUrl.setOnKeyListener((v, keyCode, event) -> {
            if (event.getAction() == ACTION_DOWN || event.getAction() == KEYCODE_ENTER) {
                ContextHelper.getInputMethodManager(getContext()).hideSoftInputFromWindow(editTextUrl.getWindowToken(), InputMethodManager.RESULT_UNCHANGED_SHOWN);
            }
            return false;
        });
    }

    @OnClick(R.id.buttonShowPreview)
    public void onClickShowPreviewButton() {
        String url = editTextUrl.getText().toString();
        if (!url.isEmpty() && textInputLayout.getError() == null) {
            showPreview(url);
        }
    }

    @Nullable
    @Override
    public VerificationError verifyStep() {
//        return new VerificationError("verifyStep");
        return null;
    }

    @Override
    public void onSelected() {
//        LOG.d("", "onSelected");
    }

    @Override
    public void onError(@NonNull VerificationError error) {
//        LOG.e("onSelected", error);
    }

    private void validateUrl(@NonNull String url) {
        if (url.isEmpty()) {
            textInputLayout.setError(getString(R.string.error_required));
            return;
        }
        if (Patterns.WEB_URL.matcher(url).matches()) {
            textInputLayout.setError(null);
        } else {
            textInputLayout.setError(getString(R.string.error_invalid_url));
        }
    }

    private void showPreview(@NonNull String url) {
        initPreview();

        indicatorView.setVisibility(View.VISIBLE);
        indicatorView.show();

        // Fetch rich link.
        DeferredManager dm = new AndroidDeferredManager();
        RichLinkLoader loader = new RichLinkLoader.Builder().build();
        dm.when(() -> loader.load(url))
                .done(richLink -> {
                    indicatorView.hide();
                    textViewLinkTitle.setText(richLink.getTitle());

                    TextDrawableImageLoader photoImageLoader = new TextDrawableImageLoader(imageViewPhoto, richLink.getUri(), richLink.getTitle());
                    photoImageLoader.loadImage();

                    TextDrawableImageLoader thumbnailImageLoader = new TextDrawableImageLoader(imageViewLinkThumbnail, richLink.getUri());
                    thumbnailImageLoader.loadImage();
                })
                .fail(e -> {
                    LOG.e("Failed to fetch rich link.", e);

                    textViewLinkTitle.setText(url);
                    textViewNoImage.setVisibility(View.VISIBLE);
                })
                .always((state, resolved, rejected) -> indicatorView.smoothToHide());
    }

    private void initPreview() {
        textViewLinkTitle.setText(null);
        imageViewPhoto.setImageDrawable(null);
        imageViewLinkThumbnail.setImageDrawable(null);
        textViewNoImage.setVisibility(View.INVISIBLE);
    }
}
