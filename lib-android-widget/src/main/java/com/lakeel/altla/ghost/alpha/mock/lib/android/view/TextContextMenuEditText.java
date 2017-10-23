package com.lakeel.altla.ghost.alpha.mock.lib.android.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TextContextMenuEditText extends android.support.v7.widget.AppCompatEditText {

    public interface TextListener {

        void beforeTextChanged(@NonNull CharSequence s, int start, int count, int after);

        void onTextChanged(@NonNull CharSequence s, int start, int before, int count);

        void afterTextChanged(@NonNull Editable editable);

        void onCut();

        void onPaste(@Nullable Editable editable);

        void onCopy();
    }

    public static class EmptyTextCallback implements TextListener {

        @Override
        public void beforeTextChanged(@NonNull CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(@NonNull CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(@NonNull Editable editable) {
        }

        @Override
        public void onCut() {
        }

        @Override
        public void onPaste(@Nullable Editable editable) {
        }

        @Override
        public void onCopy() {
        }
    }

    private final List<TextListener> listeners = Collections.synchronizedList(new ArrayList<TextListener>());

    private TextWatcher textWatcher = new TextWatcher() {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            notifyBeforeTextChanged(s, start, count, after);
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            notifyTextChanged(s, start, before, count);
        }

        @Override
        public void afterTextChanged(Editable s) {
            notifyAfterTextChanged(s);
        }
    };

    public TextContextMenuEditText(@NonNull Context context) {
        super(context);
        addTextChangedListener(textWatcher);
    }

    public TextContextMenuEditText(@NonNull Context context, @NonNull AttributeSet attrs) {
        super(context, attrs);
        addTextChangedListener(textWatcher);
    }

    public TextContextMenuEditText(@NonNull Context context, @NonNull AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        addTextChangedListener(textWatcher);
    }

    @Override
    public boolean onTextContextMenuItem(int id) {
        boolean consumed = super.onTextContextMenuItem(id);
        switch (id) {
            case android.R.id.cut:
                notifyCut();
                break;
            case android.R.id.paste:
                notifyPaste();
                break;
            case android.R.id.copy:
                notifyCopy();
        }
        return consumed;
    }

    public void addTextListener(@NonNull TextListener listener) {
        listeners.add(listener);
    }

    public void removeTextListener(@NonNull TextListener listener) {
        listeners.remove(listener);
    }

    private void notifyCut() {
        for (TextListener listener : listeners) {
            listener.onCut();
        }
    }

    private void notifyPaste() {
        for (TextListener listener : listeners) {
            listener.onPaste(getEditableText());
        }
    }

    private void notifyCopy() {
        for (TextListener listener : listeners) {
            listener.onCopy();
        }
    }

    private void notifyBeforeTextChanged(CharSequence s, int start, int count, int after) {
        for (TextListener listener : listeners) {
            listener.beforeTextChanged(s, start, count, after);
        }
    }

    private void notifyTextChanged(CharSequence s, int start, int before, int count) {
        for (TextListener listener : listeners) {
            listener.onTextChanged(s, start, before, count);
        }
    }

    private void notifyAfterTextChanged(Editable editable) {
        for (TextListener listener : listeners) {
            listener.afterTextChanged(editable);
        }
    }
}